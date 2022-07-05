/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 11/01/11
 * You may contact the copyright holder at: soporte.afirma5@mpt.es
 */

package es.gob.afirma.android.signfolder.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.security.KeyChainException;

import java.security.KeyStore;
import java.security.KeyStoreException;

import es.gob.afirma.android.crypto.AuthenticationListener;
import es.gob.afirma.android.crypto.AuthenticationResult;
import es.gob.afirma.android.crypto.DnieConnectionManager;
import es.gob.afirma.android.crypto.MobileKeyStoreManager;
import es.gob.afirma.android.crypto.NfcHelper;
import es.gob.afirma.android.signfolder.AppPreferences;
import es.gob.afirma.android.signfolder.ErrorManager;
import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.proxy.ClaveLoginResult;
import es.gob.afirma.android.signfolder.tasks.ClaveLoginTask;
import es.gob.afirma.android.util.Base64;
import es.gob.afirma.android.util.PfLog;

/** Esta actividad abstracta integra las funciones necesarias para la ejecuci&oacute;n de
 * operaciones de firma en una actividad. La actividad integra la l&oacute;gica necesaria para
 * utilizar DNIe 3.0 v&iacute;a NFC, DNIe 2.0/3.0 a trav&eacute;s de lector de tarjetas y el
 * almac&eacute;n de Android. */
public abstract class AuthenticationFragmentActivity extends LoadKeyStoreFragmentActivity
		implements AuthenticationListener,
		ClaveLoginTask.ClaveLoginRequestListener {

	/**
	 * Inicia el proceso de autenticaci&oacute;n.
     */
	protected void authenticate() {

		String certKeyStore = AppPreferences.getInstance().getCertKeyStore();

		// Acceso con certificado en la nube
		if (AppPreferences.KEYSTORE_CLOUD.equals(certKeyStore)) {
			// Ejecutamos la tarea de conexion con Clave
			ClaveLoginTask loginTask = new ClaveLoginTask(this);
			showProgressDialog(getString(R.string.dialog_msg_clave), this, loginTask);
			loginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		// Acceso con DNIe
		else if (AppPreferences.KEYSTORE_DNIE.equals(certKeyStore)) {

			// Nos aseguramos de reiniciar por completo la configuracion del DNIe
			DnieConnectionManager.getInstance().clearCan();
			DnieConnectionManager.getInstance().reset();

			// Si no esta habilitado, intentamos habilitarlo
			if (!NfcHelper.isNfcServiceEnabled(AuthenticationFragmentActivity.this)) {
				tryEnableNfc();
			}
			// Si esta habilitado, buscamos la tarjeta NFC
			else {
				searchNfcCard();
			}
		}
		// Acceso con certificado local
		else {
			loadKeyStore();
		}
	}

	@Override
	public void claveLoginRequestResult(ClaveLoginResult loginResult) {

		dismissProgressDialog();

		// Si fallo la conexion, mostramos un error
		if (!loginResult.isStatusOk()) {
			AuthenticationResult authResult = new AuthenticationResult(ErrorManager.ERROR_AUTHENTICATION_WITH_CLAVE, loginResult.getErrorMsg());
			processAuthenticationResult(authResult);
			return;
		}

		// Abrimos un WebView que carga la URL recibida y desde la que el usuario
		// podra autenticarse
		openWebViewActivity(
				ClaveWebViewActivity.class,
				loginResult.getRedirectionUrl(),
				loginResult.getCookieId(),
				R.string.title_clave_login,
				true);
	}

	@Override
	public synchronized void keySelected(final MobileKeyStoreManager.KeySelectedEvent kse) {

		showProgressDialog(getString(R.string.dialog_msg_authenticating), this);

		final String alias;
		final byte[] certEncoded;
		final KeyStore.PrivateKeyEntry keyEntry;

		try {
			alias = kse.getCertificateAlias();
			certEncoded = kse.getCertificateEncoded();
			AppPreferences.getInstance().setLastCertificate(Base64.encode(certEncoded));
			keyEntry = kse.getPrivateKeyEntry();
		} catch (final KeyChainException e) {
			if ("4.1.1".equals(Build.VERSION.RELEASE) || "4.1.0".equals(Build.VERSION.RELEASE) || "4.1".equals(Build.VERSION.RELEASE)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				PfLog.e(SFConstants.LOG_TAG, "Error al obtener la clave privada del certificado en Android 4.1.X (asegurese de que no contiene caracteres no validos en el alias): " + e); //$NON-NLS-1$
				onKeyStoreError(KeyStoreOperation.AUTHENTICATION, ErrorManager.getErrorMessage(ErrorManager.ERROR_PKE_ANDROID_4_1), e);
			} else {
				PfLog.e(SFConstants.LOG_TAG, "Error al obtener la clave privada del certificado: " + e); //$NON-NLS-1$
				onKeyStoreError(KeyStoreOperation.AUTHENTICATION, ErrorManager.getErrorMessage(ErrorManager.ERROR_PKE), e);
			}
			return;
		} catch (final KeyStoreException e) {
			// Este caso se da cuando el usuario cancela el acceso al almacen o la seleccion de
			// un certificado. En el primer caso es posible que la activity se considere cerrada
			// asi que no se puede mostrar un dialogo de error. Nos limitamos a quitar el de espera.
			PfLog.e(SFConstants.LOG_TAG, "El usuario no selecciono un certificado: " + e); //$NON-NLS-1$
			onKeyStoreCancelled();
			return;
		}
		// Cuando se instala el certificado desde el dialogo de seleccion, Android da a elegir certificado
		// en 2 ocasiones y en la segunda se produce un "java.lang.AssertionError". Se ignorara este error.
		catch (final Throwable e) {
			PfLog.e(SFConstants.LOG_TAG, "Error al obtener el certificado para la autenticacion: " + e, e); //$NON-NLS-1$
			onKeyStoreError(KeyStoreOperation.AUTHENTICATION, ErrorManager.getErrorMessage(ErrorManager.ERROR_PKE), e);
			return;
		}

		dismissProgressDialog();

		// Ya cargado el certificado, eliminamos el CAN de memoria y el objeto para que se vuelva a pedir
//		if (getCanPasswordCallback() != null) {
//			getCanPasswordCallback().clearPassword();
//			setCanPasswordCallback(null);
//		}

		// Almacenamos el alias para que las operaciones de firma futuras se hagan con el mismo
		// certificado
		setCurrentCertAlias(alias);

		// Llamamos al metodo para que se procese la autenticacion con el certificado seleccionado
		authenticateWithCertificate(alias, certEncoded, keyEntry);
	}

	@Override
	public void onKeyStoreError(KeyStoreOperation op, String msg, Throwable t) {

		dismissProgressDialog();

		PfLog.w(SFConstants.LOG_TAG, "Error al cargar el almacen de certificados: " + msg, t); //$NON-NLS-1$
		AuthenticationResult authResult = new AuthenticationResult(false);
		authResult.setError(ErrorManager.ERROR_ESTABLISHING_KEYSTORE);
		authResult.setErrorMsg(msg);
		processAuthenticationResult(authResult);
	}

	@Override
	public void onKeyStoreCancelled() {
		dismissProgressDialog();

		AuthenticationResult authResult = new AuthenticationResult(false);
		authResult.setError(ErrorManager.ERROR_CANCELLED_OPERATION);
		authResult.setCancelled(true);
		processAuthenticationResult(authResult);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == WEBVIEW_REQUEST_CODE) {
			autheticateWithClave(resultCode, data);
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	protected abstract void autheticateWithClave(int resultCode, Intent data);

	protected abstract void authenticateWithCertificate(final String alias, final byte[] certEncoded, final KeyStore.PrivateKeyEntry keyEntry);
}
