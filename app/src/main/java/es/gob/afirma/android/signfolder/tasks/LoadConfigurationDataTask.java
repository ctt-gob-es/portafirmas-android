package es.gob.afirma.android.signfolder.tasks;

import android.content.Context;
import android.os.AsyncTask;

import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.RequestAppConfiguration;
import es.gob.afirma.android.signfolder.proxy.ValidationLoginResult;
import es.gob.afirma.android.util.PfLog;

/** Carga los datos remotos necesarios para la configuraci&oacute;n de la aplicaci&oacute;n. */
public final class LoadConfigurationDataTask extends AsyncTask<Void, Void, RequestAppConfiguration> {

	private final ValidationLoginResult loginResult;
	private final CommManager commManager;
	private final Context context;
	private final LoadConfigurationListener listener;
	private Throwable t = null;

	/**
	 * Crea la tarea para la carga de la configuraci&oacute;n de la aplicaci&oacute;n
	 * necesaria para su correcto funcionamiento.
	 * @param loginResult Resultado de la autenticaci&oacute;n del usuario.
	 * @param commManager Manejador de los servicios de comunicaci&oacute;n con el portafirmas.
	 * @param context Contexto de la aplicaci&oacute;n.
	 * @param listener Manejador del resultado de la operaci&oacute;n.
	 */
	public LoadConfigurationDataTask(final ValidationLoginResult loginResult,
							  final CommManager commManager, final Context context,
							  final LoadConfigurationListener listener) {
		this.loginResult = loginResult;
		this.commManager = commManager;
		this.context = context;
		this.listener = listener;
	}

	@Override
	protected RequestAppConfiguration doInBackground(final Void... args) {

		String userId = this.loginResult == null ?
				null :
				this.loginResult.getDni() != null ?
					this.loginResult.getDni() :
					this.loginResult.getCertificateB64();
		RequestAppConfiguration config;
    	try {
    		config = this.commManager.getApplicationList(userId);
    	} catch (final Exception e) {
    		PfLog.w(SFConstants.LOG_TAG, "No se pudo obtener la lista de aplicaciones", e); //$NON-NLS-1$
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
	public interface LoadConfigurationListener {

		void configurationLoadSuccess(RequestAppConfiguration appConfig, ValidationLoginResult loginResult);

		void configurationLoadError(Throwable t);
	}
}
