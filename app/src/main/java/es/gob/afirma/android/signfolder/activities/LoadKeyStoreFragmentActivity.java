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

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import es.gob.afirma.android.crypto.DnieConnectionManager;
import es.gob.afirma.android.crypto.InitializingNfcCardException;
import es.gob.afirma.android.crypto.KeyStoreManagerListener;
import es.gob.afirma.android.crypto.LoadKeyStoreManagerTask;
import es.gob.afirma.android.crypto.LoadNfcKeyStoreManagerTask;
import es.gob.afirma.android.crypto.LoadingKeyStoreResult;
import es.gob.afirma.android.crypto.MobileKeyStoreManager;
import es.gob.afirma.android.crypto.NfcHelper;
import es.gob.afirma.android.crypto.UnsupportedNfcCardException;
import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.listeners.SettingNfcListener;
import es.gob.afirma.android.util.PfLog;
import es.gob.jmulticard.android.callbacks.CachePasswordCallback;

/*
 * Esta actividad abstracta integra las funciones necesarias para la cargar de un almacen de
 * certificados del dispositivo. La actividad integra la l&oacute;gica necesaria para utilizar
 * DNIe 3.0/4.0 v&iacute;a NFC, el almac&eacute;n de Android y el uso de certificado en la nube.
 */
public abstract class LoadKeyStoreFragmentActivity extends FragmentActivity
		implements KeyStoreManagerListener,
		MobileKeyStoreManager.PrivateKeySelectionListener {

	/** C&oacute;digo de solicitud de detecci&oacute;n de tarjeta por NFC. */
	private final static int REQUEST_CODE_DETECT_NFC_CARD = 2001;
	/** C&oacute;digo de solicitud de la habilitaci&oacute;n del NFC del dispositivo. */
	private final static int REQUEST_CODE_ENABLE_NFC = 2002;   // The request code

	public static final int WEBVIEW_REQUEST_CODE = 20;

	public static final String EXTRA_RESOURCE_URL = "url";
	public static final String EXTRA_RESOURCE_COOKIE_ID = "cookieId";
	public static final String EXTRA_RESOURCE_TITLE = "title";
	public static final String EXTRA_RESOURCE_NEED_JAVASCRIPT = "javascript";

	private SettingNfcListener settingNfcListener = null;

    //private CachePasswordCallback canPasswordCallback = null;
	private static String currentCertAlias = null;

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

		// Si volvemos de la pantalla de insercion de CAN y deteccion de tarjeta NFC
		if (requestCode == REQUEST_CODE_DETECT_NFC_CARD) {
			// Si el usuario inserto el CAN, lo guardamos y cargamos un almacen via NFC (que tendra
			// una gestion de errores distinta).
			// Si no se inserto el CAN o no se detecto un almacen nfc, nos aseguramos de que no
			// haya ningun CAN cacheado y cargamos el resto de almacenes.
			if (resultCode == RESULT_OK) {

				CachePasswordCallback canPasswordCallback = data != null
						? (CachePasswordCallback) data.getSerializableExtra(NFCDetectorActivity.INTENT_EXTRA_PASSWORD_CALLBACK)
						: null;
				DnieConnectionManager.getInstance().setCanPasswordCallback(canPasswordCallback);
				loadNfcKeyStore(canPasswordCallback);
			}
			else {
				// Si no se puede cargar el almacen y se establecido un CAN de DNIE, puede que
				// el problema es que sea erroneo, asi que lo borramos
				DnieConnectionManager.getInstance().clearCan();

				if (resultCode == RESULT_CANCELED) {
					PfLog.w(SFConstants.LOG_TAG, "Se cancelo el dialogo de insercion de CAN");
				}
				else {
					PfLog.w(SFConstants.LOG_TAG, "No se detecto tarjeta NFC");
				}
			}
		}
		// Si volvemos despues de pedirle al usuario que habilite el NFC
		else if (requestCode == REQUEST_CODE_ENABLE_NFC) {
			if (this.settingNfcListener != null) {
				this.settingNfcListener.detectNfcChanges(NfcHelper.isNfcServiceEnabled(this));
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * Busca una nueva tarjeta NFC. El resultado de la busqueda se obtiene en el
	 * metodo onActivityResult().
	 */
	public void searchNfcCard() {
		final Intent intentNFC = new Intent(this, NFCDetectorActivity.class);
		CachePasswordCallback canPasswordCallback = DnieConnectionManager.getInstance().getCanPasswordCallback();
		if (canPasswordCallback != null) {
			intentNFC.putExtra(NFCDetectorActivity.INTENT_EXTRA_CAN_VALUE, canPasswordCallback.getPassword());
		}
		startActivityForResult(intentNFC, REQUEST_CODE_DETECT_NFC_CARD);
	}

	protected void tryEnableNfc() {
		openNfcSystemSettings(new SettingNfcListener() {
			@Override
			public void detectNfcChanges(boolean nfcEnabled) {
				if (nfcEnabled) {
					// Abrimos la actividad de conexion con el DNIe
					searchNfcCard();
				}
				else {
					PfLog.i(SFConstants.LOG_TAG, "No se ha habilitado NFC. Se cancela la operacion");
					enabledNfcCancelled();
				}
			}
		});
	}

	/**
	 * Abre el dialogo del sistema para la configuracion de NFC. El resultado de si se ha
	 * activado o no se determina en el onActivityResult.
	 */
	private void openNfcSystemSettings(SettingNfcListener settingNfcListener) {
		this.settingNfcListener = settingNfcListener;
		Toast.makeText(getApplicationContext(), R.string.enable_nfc, Toast.LENGTH_SHORT).show();
		startActivityForResult(
				new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS),
				REQUEST_CODE_ENABLE_NFC);
	}

	/**
	 * Inicia el proceso de carga de certificados para firmar.
	 */
	protected void loadKeyStore() {
		LoadKeyStoreManagerTask lksmt = new LoadKeyStoreManagerTask(this, this);
		showProgressDialog(getString(R.string.dialog_msg_accessing_keystore), this, lksmt);
		lksmt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	/**
	 * Inicia el proceso de carga de certificados para firmar usando un almacen
	 * por conexion NFC.
	 */
	private void loadNfcKeyStore(CachePasswordCallback canPasswordCallback) {
		LoadNfcKeyStoreManagerTask nfcKsmt = new LoadNfcKeyStoreManagerTask(this,  this, canPasswordCallback);
		showProgressDialog(getString(R.string.dialog_msg_connecting_dnie), this, nfcKsmt);
		nfcKsmt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	/**
	 * Abre una actividad con un WebView en el que se carga una URL.
	 * @param webViewClass Clase de la actividad con el WebView.
	 * @param url URL que se desea cargar en el WebView.
	 * @param cookieId Identificador de la cookie de sesion de la aplicaci&oacute;n.
	 * @param titleStringId Identificador de la cadena con el t&iacute;tulo del WebView.
	 * @param needJavaScript Indica si se debe habilitar el uso de JavaScript en el Webview.
	 */
	public void openWebViewActivity(Class<?> webViewClass, String url, String cookieId,
									int titleStringId, boolean needJavaScript) {

		final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setClass(this, webViewClass);

		PfLog.i(SFConstants.LOG_TAG, "Cargamos el WebView con la URL: " + url);
		PfLog.i(SFConstants.LOG_TAG, "Cargamos el WebView con el id de sesion: " + cookieId);

		intent.putExtra(EXTRA_RESOURCE_URL, url);
		intent.putExtra(EXTRA_RESOURCE_NEED_JAVASCRIPT, needJavaScript);
		if (cookieId != null) {
			intent.putExtra(EXTRA_RESOURCE_COOKIE_ID, cookieId);
		}
		if (titleStringId != 0) {
			intent.putExtra(EXTRA_RESOURCE_TITLE, titleStringId);
		}

		startActivityForResult(intent, WEBVIEW_REQUEST_CODE);
	}

	@Override
	public void onLoadingKeyStoreResult(LoadingKeyStoreResult result) {

		dismissProgressDialog();

		if (result.isCancelled()) {
			PfLog.w(SFConstants.LOG_TAG, "Carga del almacen cancelada por el usuario"); //$NON-NLS-1$
			onKeyStoreCancelled();
		}
		else if (result.isError()) {
			// Si el error de carga es un error de conexion NFC, volvemos a mostrar el dialogo,
			// si no, indicamos un error en la firma.
			Throwable cause = result.getCause();
			if (cause instanceof UnsupportedNfcCardException) {
				Toast.makeText(this, R.string.unsupported_card, Toast.LENGTH_SHORT).show();
				// Reintentamos la conexion esperando encontrar la tarjeta adecuada
				searchNfcCard();
			} else if (cause instanceof InitializingNfcCardException) {
				Toast.makeText(this, R.string.nfc_card_initializing_error, Toast.LENGTH_SHORT).show();
				// Reintentamos la conexion, eliminando el CAN almacenado por si se hubiese insertado
				// incorrectamente
				DnieConnectionManager.getInstance().clearCan();
				searchNfcCard();
			} else {
				onKeyStoreError(KeyStoreOperation.LOAD_KEYSTORE, result.getErrorMessage(), cause);
			}
		}
		else {
			MobileKeyStoreManager msm = result.getMsm();
			if (msm == null) {
				PfLog.w(SFConstants.LOG_TAG, "Error al cargar el almacen de certificados. Es posible que el usuario cancelase la operacion."); //$NON-NLS-1$
				onKeyStoreCancelled();
				return;
			}
			msm.getPrivateKeyEntryAsynchronously(this);
		}
	}

	protected static void setCurrentCertAlias(String alias) {
		currentCertAlias = alias;
	}

	protected static String getCurrentCertAlias() {
		return currentCertAlias;
	}

    /**
     * Cuando se produce un error al operar con el almac&eacute;n de certificados.
     * @param op Operaci&oacute;n en la cual se produjo el error.
     * @param msg Mensaje de error.
     * @param t Error que origin&oacute; el problema.
     */
	public abstract void onKeyStoreError(KeyStoreOperation op, String msg, Throwable t);

	/**
	 * Cuando se se cancela el proceso de carga del almacen o de sus certificados.
	 */
	public abstract void onKeyStoreCancelled();

	/**
	 * Muestra un di&aacute;logo de progreso asociado a una tarea.
	 * @param message Mensaje que mostrar en el di&aacute;logo de progreso.
	 * @param ctx Contexto en el que mostrar el di&aacute;logo.
	 * @param tasks Tarea durante la cual se mostrar&aacute; el di&aacute;logo.
	 */
	protected abstract  void showProgressDialog(String message, Context ctx, AsyncTask<?, ?, ?>... tasks);

	/**
	 * Oculta el di&aacute;logo de progreso.
	 */
	protected abstract void dismissProgressDialog();

	/**
	 * Define el comportamiento si el usuario cancela la activaci&oacute;n del NFC cuando es
	 * requerido.
	 */
	protected abstract void enabledNfcCancelled();

	/** Operaci&oacute;n de firma. */
	protected enum KeyStoreOperation {
		/** Operaci&oacute;n de autenticaci&oacute;n. */
		AUTHENTICATION,
		/** Operaci&oacute;n de firma. */
		SIGN,
		/** Operaci&oacute;n de carga de almac&eacute;n. */
		LOAD_KEYSTORE,
		/** Operacion de seleccion de certificado. */
		SELECT_CERTIFICATE
	}
}
