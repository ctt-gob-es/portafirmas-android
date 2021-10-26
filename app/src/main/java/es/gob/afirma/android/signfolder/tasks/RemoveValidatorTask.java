package es.gob.afirma.android.signfolder.tasks;

import android.os.AsyncTask;

import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.GenericResponse;
import es.gob.afirma.android.user.configuration.Authorization;
import es.gob.afirma.android.user.configuration.AuthorizationState;
import es.gob.afirma.android.user.configuration.Validator;
import es.gob.afirma.android.util.PfLog;

public class RemoveValidatorTask extends AsyncTask<Void, Void, GenericResponse> {

    private Validator validator;
    private RemoveValidatorListener listener;

    public RemoveValidatorTask(Validator validator, RemoveValidatorListener listener) {
        this.validator = validator;
        this.listener = listener;
    }

    @Override
    protected GenericResponse doInBackground(Void... voids) {

        GenericResponse response = null;
        try {
            response = CommManager.getInstance().removeValidator(this.validator);
        } catch (Exception e) {
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
     * Oyente del resultado de la tarea de eliminacion de validador.
     */
    public interface RemoveValidatorListener  {
        /** Operación ejecutada después del éxito en la eliminación. */
        void onSuccess();

        /**
         * Operación ejecutada después de fallar la eliminación.
         * @param message Mensaje de error.
         */
        void onError(String message);
    }
}
