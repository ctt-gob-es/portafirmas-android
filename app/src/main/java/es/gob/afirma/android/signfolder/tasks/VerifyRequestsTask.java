package es.gob.afirma.android.signfolder.tasks;

import android.os.AsyncTask;

import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.listeners.OperationRequestListener;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.RequestResult;
import es.gob.afirma.android.signfolder.proxy.RequestVerifyResult;
import es.gob.afirma.android.signfolder.proxy.SignRequest;
import es.gob.afirma.android.util.PfLog;

/**
 * Tarea asíncrona que realiza la validación de un conjunto de peticiones de firma.
 */
public class VerifyRequestsTask extends AsyncTask<Void, Void, RequestVerifyResult[]> {

    private final String[] requestIds;
    private final CommManager commManager;
    private final OperationRequestListener listener;
    private Throwable t;

    /**
     * Crea una tarea aíncrona para la validación de peticiones.
     *
     * @param requests    Listado de peticiones que se desean validar.
     * @param commManager Manejador de las comunicaciones.
     * @param listener    Clase a notificar sobre las peticiones de validar del usuario.
     */
    public VerifyRequestsTask(final SignRequest[] requests, final CommManager commManager, final OperationRequestListener listener) {
        this.requestIds = new String[requests.length];
        this.commManager = commManager;
        this.listener = listener;
        this.t = null;

        for (int i = 0; i < requests.length; i++) {
            this.requestIds[i] = requests[i].getId();
        }
    }

    /**
     * Crea una tarea asíncrona para la validación de peticiones.
     *
     * @param requestId   Identificador de la petición a validar.
     * @param commManager Manejador de las comunicaciones.
     * @param listener    Clase a notificar sobre las peticiones de validación del usuario.
     */
    public VerifyRequestsTask(final String requestId, final CommManager commManager, final OperationRequestListener listener) {
        this.requestIds = new String[]{requestId};
        this.commManager = commManager;
        this.listener = listener;
        this.t = null;
    }

    @Override
    protected RequestVerifyResult[] doInBackground(final Void... arg) {

        RequestVerifyResult[] results = null;
        try {
            results = this.commManager.verifyRequests(this.requestIds);
        } catch (final Exception e) {
            PfLog.w(SFConstants.LOG_TAG, "Ocurrio un error en la validación de las solicitudes de firma: " + e);
            this.t = e;
        }
        return results;
    }

    @Override
    protected void onPostExecute(final RequestVerifyResult[] verifiedRequests) {

        if(verifiedRequests == null){
            this.listener.requestOperationFailed(OperationRequestListener.VERIFY_OPERATION, new RequestResult(null, false), this.t);
        } else {
            for (final RequestVerifyResult rResult : verifiedRequests) {
                RequestResult res = new RequestResult(null, rResult.isStatusOk());
                if (rResult.isStatusOk()) {
                    this.listener.requestOperationFinished(OperationRequestListener.VERIFY_OPERATION, res);
                } else {
                    this.listener.requestOperationFailed(OperationRequestListener.VERIFY_OPERATION, res, this.t);
                }
            }
        }
    }
}
