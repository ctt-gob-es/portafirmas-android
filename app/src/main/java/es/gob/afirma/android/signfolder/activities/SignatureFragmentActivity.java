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
import java.security.cert.X509Certificate;

import es.gob.afirma.android.crypto.DnieConnectionManager;
import es.gob.afirma.android.crypto.MobileKeyStoreManager;
import es.gob.afirma.android.crypto.NfcHelper;
import es.gob.afirma.android.signfolder.AppPreferences;
import es.gob.afirma.android.signfolder.ErrorManager;
import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.listeners.OperationRequestListener;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.FireLoadDataResult;
import es.gob.afirma.android.signfolder.proxy.RequestResult;
import es.gob.afirma.android.signfolder.proxy.SignRequest;
import es.gob.afirma.android.signfolder.tasks.FireLoadDataTask;
import es.gob.afirma.android.signfolder.tasks.FireSignTask;
import es.gob.afirma.android.signfolder.tasks.LoadSelectedPrivateKeyTask;
import es.gob.afirma.android.signfolder.tasks.SignRequestTask;
import es.gob.afirma.android.util.Base64;
import es.gob.afirma.android.util.PfLog;

/** Esta actividad abstracta integra las funciones necesarias para la ejecuci&oacute;n de
 * operaciones de firma en una actividad. La actividad integra la l&oacute;gica necesaria para
 * utilizar DNIe 3.0 v&iacute;a NFC, DNIe 2.0/3.0 a trav&eacute;s de lector de tarjetas y el
 * almac&eacute;n de Android. */
public abstract class SignatureFragmentActivity extends LoadKeyStoreFragmentActivity
		implements FireLoadDataTask.FireLoadDataListener,
		FireSignTask.FireSignListener,
		OperationRequestListener {

	private SignRequest[] requestsToSign = null;

	/**
	 * Realizamos la firma de un listado de peticiones con el mecanismo configurado (certificado
	 * local, DNIe o FIRe).
	 * @param requests Listado de peticiones a firmar.
	 */
	protected void signRequests(SignRequest[] requests) {

		this.requestsToSign = requests;

		String certKeystore = AppPreferences.getInstance().getCertKeyStore();
		if (AppPreferences.KEYSTORE_CLOUD.equals(certKeystore)) {
			PfLog.i(SFConstants.LOG_TAG, "Iniciamos firma con Cl@ve Firma");
			signRequestWithFire();
		} else if (AppPreferences.KEYSTORE_DNIE.equals(certKeystore)) {
			PfLog.i(SFConstants.LOG_TAG, "Iniciamos firma con DNIe");
			signRequestWithDnie();
		} else {
			PfLog.i(SFConstants.LOG_TAG, "Iniciamos firma con certificado local");
			signRequestWithLocalCertificate();
		}
	}

	/**
	 * Inicia el proceso de firma con FIRe.
	 */
	private void signRequestWithFire() {
		FireLoadDataTask cct = new FireLoadDataTask(this.requestsToSign, this);
		cct.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	/**
	 * Inicia el proceso de firma con DNIe.
	 */
	private void signRequestWithDnie() {
		// Si no esta habilitado, intentamos habilitarlo
		if (!NfcHelper.isNfcServiceEnabled(SignatureFragmentActivity.this)) {
			tryEnableNfc();
		}
		// Si esta habilitado, comprobamos si la tarjeta esta conectada y se configuro el almacen
		else {
			DnieConnectionManager dnieManager = DnieConnectionManager.getInstance();
			if (dnieManager.getNfcConnection() != null
					&& dnieManager.getNfcConnection().isOpen()
					&& dnieManager.getKeyStoreManager() != null) {
				dnieManager.getKeyStoreManager().getPrivateKeyEntryAsynchronously(this);
			}
			// Si no, buscamos de nuevo la tarjeta NFC
			else {
				searchNfcCard();
			}
		}
	}

	private void signRequestWithLocalCertificate() {
		new LoadSelectedPrivateKeyTask(getCurrentCertAlias(), this, this).execute();
	}

	@Override
	public synchronized void keySelected(final MobileKeyStoreManager.KeySelectedEvent kse) {

		showProgressDialog(getString(R.string.dialog_msg_init_signing), this);

		final byte[] certEncoded;
		final KeyStore.PrivateKeyEntry keyEntry;

		try {
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

		showProgressDialog(getString(R.string.dialog_msg_processing_requests), this);

		// Iniciamos las tareas de firma de cada peticion
		for (final SignRequest req : this.requestsToSign) {
			new SignRequestTask(req,
					keyEntry.getPrivateKey(),
					(X509Certificate[]) keyEntry.getCertificateChain(),
					CommManager.getInstance(),
					this).execute();
		}
	}

	@Override
	public void onKeyStoreError(KeyStoreOperation op, String msg, Throwable cause) {
		PfLog.w(SFConstants.LOG_TAG, "Error al cargar el almacen de certificados: " + msg, cause); //$NON-NLS-1$
		dismissProgressDialog();
		requestOperationFailed(OperationRequestListener.SIGN_OPERATION, null, cause);
	}

	@Override
	public void onKeyStoreCancelled() {
		dismissProgressDialog();
		requestOperationCancelled(OperationRequestListener.SIGN_OPERATION);
	}

	/**
	 * Cuando se finaliza correctamente el llamada a FIRe que procesa las peticiones,
	 * recibimos el identificador de la transaccion de FIRe y la URL de redireccion
	 * a la pagina web desde la que hacer la autorizaci&oacute;n.
	 *
	 * @param firePreSignResult Informacion de prefirma y de la transaccion de FIRe para permitir la
	 *                          autorizaci&oacute;n del usuario.
	 */
	@Override
	public void fireLoadDataSuccess(FireLoadDataResult firePreSignResult) {
		PfLog.i(SFConstants.LOG_TAG, "Datos cargados en FIRe:\n" + firePreSignResult);

		// Abrimos una actividad con un WebView en la que se muestre la URL recibida
		openWebViewActivity(
				ClaveWebViewActivity.class,
				firePreSignResult.getURL(),
				null,
				R.string.title_fire_webview,
				true);
	}

	@Override
	public void fireLoadDataFailed(Throwable cause) {
		PfLog.e(SFConstants.LOG_TAG, "Ha fallado la carga de los datos en FIRe: " + cause, cause); //$NON-NLS-1$

		dismissProgressDialog();

		requestOperationFailed(OperationRequestListener.SIGN_OPERATION, null, cause);
	}

	@Override
	public void fireSignSuccess(boolean allOk) {
		if (allOk) {
			requestedSignatureSuccess(this.requestsToSign);
		} else {
			PfLog.e(SFConstants.LOG_TAG, "Ha fallado la firma con FIRe"); //$NON-NLS-1$
			requestedSignatureFailed(this.requestsToSign, null);
		}
	}

	@Override
	public void fireSignFailed(Throwable cause) {
		PfLog.e(SFConstants.LOG_TAG, "Ha fallado la operacion de firma con FIRe", cause); //$NON-NLS-1$
		requestedSignatureFailed(this.requestsToSign, cause);
	}

	abstract void requestedSignatureSuccess(SignRequest[] requests);

	abstract void requestedSignatureFailed(SignRequest[] requests, Throwable cause);
}
