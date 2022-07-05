/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 11/01/11
 * You may contact the copyright holder at: soporte.afirma5@mpt.es
 */

package es.gob.afirma.android.crypto;

import android.os.AsyncTask;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import javax.security.auth.callback.PasswordCallback;

/** Tarea de carga e inicializaci&oacute;n del gestor de claves y certificados en Android. */
public final class LoadKeyStoreManagerTask extends AsyncTask<Void, Void, Void> {

	private static final String ES_GOB_AFIRMA = "es.gob.afirma"; //$NON-NLS-1$

	private final KeyStoreManagerListener kmListener;
	private final FragmentActivity activity;
	private final PasswordCallback passwordCallback;

	/** Crea una tarea de carga e inicializaci&oacute;n del gestor de claves y certificados en Android.
	 * @param kml Clase a la que hay que notificar cuando se finaliza la
	 * carga e inicializaci&oacute;n del gestor de claves y certificados
	 * @param act Actividad padre */
	public LoadKeyStoreManagerTask(final KeyStoreManagerListener kml, final FragmentActivity act) {
		this.kmListener = kml;
		this.activity = act;
		this.passwordCallback = null;
	}

	/**
	 * Crea una tarea de carga e inicializaci&oacute;n del gestor de claves y certificados en Android.
	 * @param kml Clase a la que hay que notificar cuando se finaliza la
	 * carga e inicializaci&oacute;n del gestor de claves y certificados
	 * @param act Actividad padre
	 * @param  passwordCallback Callback con la contrase&ntilde;a del almac&eacute;n.
	 */
	public LoadKeyStoreManagerTask(final KeyStoreManagerListener kml, final FragmentActivity act, final PasswordCallback passwordCallback) {
		this.kmListener = kml;
		this.activity = act;
		this.passwordCallback = passwordCallback;
	}

	@Override
	protected Void doInBackground(Void[] params) {
		Log.i(ES_GOB_AFIRMA, "Inicializamos el almacen"); //$NON-NLS-1$
		// Se obtiene el KeyStore
		KeyStoreManagerFactory.initKeyStoreManager(this.activity, this.kmListener);

		return null;
	}
}
