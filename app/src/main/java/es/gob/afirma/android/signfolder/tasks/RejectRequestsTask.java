package es.gob.afirma.android.signfolder.tasks;

import android.os.AsyncTask;

import es.gob.afirma.android.signfolder.listeners.OperationRequestListener;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.RequestResult;
import es.gob.afirma.android.signfolder.proxy.SignRequest;
import es.gob.afirma.android.util.PfLog;

/** Tarea as&iacute;ncrona para el rechazo de peticiones de firma. Despu&eacute;s
 * del rechazo actualiza la lista con las peticiones pendientes. */
public final class RejectRequestsTask extends AsyncTask<Void, Void, RequestResult[]> {

	private final String[] requestIds;
	private final CommManager commManager;
	private final OperationRequestListener listener;
	private String reason;
	private Throwable t;

	/**
	 * Crea una tarea as&iacute;ncrona para el rechazo de peticiones.
	 * @param requests Listado de peticiones que se desean rechazar.
	 * @param commManager Manejador de las comunicaciones para el rechazo de las peticiones.
	 * @param listener Manejador que gestiona el comportamiento de la operaci&oacute;n al finalizar.
	 * @param reason Motivo del rechazo indicado por el usuario.
	 */
	public RejectRequestsTask(final SignRequest[] requests,
			 final CommManager commManager, 
			 final OperationRequestListener listener,
			 final String reason) {
		this.requestIds = new String[requests.length];
		this.commManager = commManager;
		this.listener = listener;
		this.reason = reason;
		this.t = null;

		for (int i = 0; i < requests.length; i++) {
			this.requestIds[i] = requests[i].getId();
		}
	}

	/**
	 * Crea una tarea as&iacute;ncrona para el rechazo de una petici&oacute;n.
	 * @param requestId Identificador de la petici&oacute;n a rechazar.
	 * @param commManager Manejador de las comunicaciones para el rechazo de las peticiones.
	 * @param listener Manejador que gestiona el comportamiento de la operaci&oacute;n al finalizar.
	 * @param reason Motivo del rechazo indicado por el usuario.
	 */
	public RejectRequestsTask(final String requestId,
			final CommManager commManager,
			final OperationRequestListener listener,
			final String reason) {
		this.requestIds = new String[] { requestId };
		this.commManager = commManager;
		this.listener = listener;
		this.reason = reason;
		this.t = null;
	}

    @Override
	protected RequestResult[] doInBackground(final Void... arg) {

        	// Enviamos la peticion de rechazo
    	RequestResult[] result = null;
        try {
			result = this.commManager.rejectRequests(this.requestIds, this.reason);
		} catch (final Exception e) {
			PfLog.w(SFConstants.LOG_TAG, "Ocurrio un error en el rechazo de las solicitudes de firma: " + e); //$NON-NLS-1$
			this.t = e;
		}

        return result;
    }

    @Override
	protected void onPostExecute(final RequestResult[] rejectedRequests) {

    	if (rejectedRequests != null) {
    		for (final RequestResult rResult : rejectedRequests) {
    			if (rResult.isStatusOk()) {
    				this.listener.requestOperationFinished(OperationRequestListener.REJECT_OPERATION, rResult);
    			}
    			else {
    				this.listener.requestOperationFailed(OperationRequestListener.REJECT_OPERATION, rResult, this.t);
    			}
    		}
    	}
    	else {
    		this.listener.requestOperationFailed(OperationRequestListener.REJECT_OPERATION, null, this.t);
    	}
    }
}
