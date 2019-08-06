package es.gob.afirma.android.signfolder;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.RequestAppConfiguration;
import es.gob.afirma.android.signfolder.proxy.ValidationLoginResult;

/** Carga los datos remotos necesarios para la configuraci&oacute;n de la aplicaci&oacute;n. */
final class LoadConfigurationDataTask extends AsyncTask<Void, Void, RequestAppConfiguration> {

	private final ValidationLoginResult loginResult;
	private final CommManager commManager;
	private final Context context;
	private final LoadConfigurationListener listener;
	private Throwable t = null;

	/**
	 * Crea la tarea para la carga de la configuraci&oacute;n de la aplicaci&oacute;n
	 * necesaria para su correcto funcionamiento.
	 * @param certB64 Certificado para la autenticaci&oacute;n de la petici&oacute;n.
	 * @param certAlias Alias del certificado para la autenticaci&oacute;n de la petici&oacute;n.
	 * @param commManager Manejador de los servicios de comunicaci&oacute;n con el portafirmas.
	 * @param context Contexto de la aplicaci&oacute;n.
	 * @param listener Manejador del resultado de la operaci&oacute;n.
	 */
	LoadConfigurationDataTask(final ValidationLoginResult loginResult,
							  final CommManager commManager, final Context context, final LoadConfigurationListener listener) {
		this.loginResult = loginResult;
		this.commManager = commManager;
		this.context = context;
		this.listener = listener;
	}

	/**
	 * Crea la tarea para la carga de la configuraci&oacute;n de la aplicaci&oacute;n
	 * necesaria para su correcto funcionamiento.
	 * @param commManager Manejador de los servicios de comunicaci&oacute;n con el portafirmas.
	 * @param context Contexto de la aplicaci&oacute;n.
	 * @param listener Manejador del resultado de la operaci&oacute;n.
	 */
	LoadConfigurationDataTask(final CommManager commManager, final Context context,
							  final LoadConfigurationListener listener) {
		this.loginResult = null;
		this.commManager = commManager;
		this.context = context;
		this.listener = listener;
	}

	@Override
	protected RequestAppConfiguration doInBackground(final Void... args) {

		RequestAppConfiguration config;
    	try {
    		config = this.commManager.getApplicationList(this.loginResult.getDni());
    	} catch (final Exception e) {
    		Log.w(SFConstants.LOG_TAG, "No se pudo obtener la lista de aplicaciones", e); //$NON-NLS-1$
    		config = null;
    		this.t = e;
    	}

    	// Agregamos la configuracion necesaria
		// Como primer elemento aparecera el elemento que desactiva el filtro
    	if (config != null) {
    		config.getAppIdsList().add(0, ""); //$NON-NLS-1$
    		config.getAppNamesList().add(0, this.context.getString(R.string.filter_app_all_request));
    	}

		return config;
	}

	@Override
	protected void onPostExecute(final RequestAppConfiguration appConfig) {
		if (appConfig != null) {
			this.listener.configurationLoadSuccess(appConfig, this.loginResult);
		}
		else {
			this.listener.configurationLoadError(this.t);
		}
	}

	/** Interfaz con los metodos para gestionar los resultados de la carga de la
	 * configuraci&oacute;n de la aplicaci&oacute;n. */
	interface LoadConfigurationListener {

		void configurationLoadSuccess(RequestAppConfiguration appConfig, ValidationLoginResult loginResult);

		void configurationLoadError(Throwable t);
	}
}
