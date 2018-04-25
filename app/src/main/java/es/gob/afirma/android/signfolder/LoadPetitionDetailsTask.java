package es.gob.afirma.android.signfolder;

import android.os.AsyncTask;
import android.util.Log;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.RequestDetail;

/** Tarea para la carga de los detalles de una petici&oacute;n en una pantalla para la
 * visualizaci&oacute;n de la descripci&oacute;n de peticiones. */
final class LoadPetitionDetailsTask extends AsyncTask<Void, Void, RequestDetail> {

	private final String petitionId;
	private final String certB64;
	private final CommManager commManager;
	private final LoadSignRequestDetailsListener listener;
	private boolean lostSession = false;

	/**
	 * Codigo de error de autenticacion (perdida de sesion)
	 */
	private static final String AUTH_ERROR = "ERR-11"; //$NON-NLS-1$

	/** Crea la tarea para la carga de los detalles de una petici&oacute;n en una pantalla para la
	 * visualizaci&oacute;n de la descripci&oacute;n de peticiones.
	 * @param petitionId Identificados de la petici&oacute;n de la que se quiere el detalle.
	 * @param certB64 Certificado para la autenticaci&oacute;n de la petici&oacute;n.
	 * @param commManager Manejador de los servicios de comunicaci&oacute;n con el portafirmas.
	 * @param listener Actividad en la que es posible mostrar los datos. */
	LoadPetitionDetailsTask(final String petitionId, final String certB64,
			final CommManager commManager, final LoadSignRequestDetailsListener listener) {
		this.petitionId = petitionId;
		this.certB64 = certB64;
		this.commManager = commManager;
		this.listener = listener;
	}

	@Override
	protected RequestDetail doInBackground(final Void... args) {

    	RequestDetail requestDetail;
    	try {
    		requestDetail = this.commManager.getRequestDetail(this.petitionId);
    	} catch (final Exception e) {
			requestDetail = null;
			Log.e(SFConstants.LOG_TAG, "Ocurrio un error al recuperar las peticiones de firma: " + e); //$NON-NLS-1$
			// Si se ha perdido la sesion vuelve a la pantalla de login
			if(e.getMessage().contains(AUTH_ERROR)) {
				lostSession = true;
				this.listener.lostSession();
			}
		}
    	catch (final Throwable e) {
    		Log.w(SFConstants.LOG_TAG, "No se pudo obtener el detalle de la solicitud: " + e); //$NON-NLS-1$
    		requestDetail = null;
    	}

		return requestDetail;
	}

	@Override
	protected void onPostExecute(final RequestDetail result) {
		if (result != null) {
			this.listener.loadedSignRequestDetails(result);
		}
		else if (!lostSession){
			this.listener.errorLoadingSignRequestDetails();
		}
	}

	/** Interfaz con los metodos para gestionar los resultados de la peticion del detalle
	 * de una solicitud de firma. */
	interface LoadSignRequestDetailsListener {
		void loadedSignRequestDetails(RequestDetail details);
		void errorLoadingSignRequestDetails();
		void lostSession();
	}
}
