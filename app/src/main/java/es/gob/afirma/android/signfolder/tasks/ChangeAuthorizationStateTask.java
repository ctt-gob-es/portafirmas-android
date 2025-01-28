package es.gob.afirma.android.signfolder.tasks;

import android.os.AsyncTask;

import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.GenericResponse;
import es.gob.afirma.android.user.configuration.Authorization;
import es.gob.afirma.android.user.configuration.AuthorizationState;
import es.gob.afirma.android.util.PfLog;

public class ChangeAuthorizationStateTask extends AsyncTask<Void, Void, GenericResponse> {

    private static final String OPERATION_REVOKE = "REVOKE";
    private static final String OPERATION_REJECT = "REJECT";

    private Authorization auth;
    private AuthorizationState state;
    private ChangeAuthorizationStateListener listener;

    public ChangeAuthorizationStateTask(Authorization auth, AuthorizationState state,
                                        ChangeAuthorizationStateListener changeAuthorizationStateListener) {
        this.auth = auth;
        this.state = state;
        this.listener = changeAuthorizationStateListener;
    }

    @Override
    protected GenericResponse doInBackground(Void... voids) {

        GenericResponse response = null;
        try {
            if (this.state == AuthorizationState.ACTIVE) {
                response = CommManager.getInstance().approveAuthorization(this.auth);
            } else if (this.state == AuthorizationState.REVOKED) {
                response = CommManager.getInstance().revokeAuthorization(this.auth, OPERATION_REVOKE);
            } else if (this.state == AuthorizationState.REJECTED) {
                response = CommManager.getInstance().revokeAuthorization(this.auth, OPERATION_REJECT);
            }
        }
        catch (Exception e) {
            PfLog.e(SFConstants.LOG_TAG, "Error al tratar de cambiar el estado de una autorizacion", e);
            response = new GenericResponse(GenericResponse.ERROR_COMMUNICATION);
        }
        return response;
    }

    @Override
    protected void onPostExecute(GenericResponse result) {

        if (this.listener != null) {
            if (result != null && result.isSuccess()) {
                this.listener.onSuccess();
            }
            else {
                this.listener.onError(result != null ? result.getErrorMessage() : null);
            }
        }
    }

    /**
     * Oyente del resultado de la tarea.
     */
    public static interface ChangeAuthorizationStateListener  {
        /** Operación ejecutada después del éxito del cambio de estado. */
        void onSuccess();

        /**
         * Operación ejecutada después del fallar el cambio de estado.
         * @param message Mensaje de error.
         */
        void onError(String message);
    }
}
