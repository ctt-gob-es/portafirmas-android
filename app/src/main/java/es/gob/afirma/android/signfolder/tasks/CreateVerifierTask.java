package es.gob.afirma.android.signfolder.tasks;

import android.os.AsyncTask;

import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.GenericResponse;
import es.gob.afirma.android.user.configuration.Authorization;
import es.gob.afirma.android.user.configuration.GenericUser;
import es.gob.afirma.android.util.PfLog;

/**
 * Tarea para el alta de un nuevo validador.
 */
public final class CreateVerifierTask extends AsyncTask<Void, Void, GenericResponse> {

    /**
     * Codigo de error de autenticacion (perdida de sesion)
     */
    private static final String AUTH_ERROR = "ERR-11"; //$NON-NLS-1$
    private final GenericUser validator;
    private final SaveValidatorListener listener;

    /**
     * Crea la tarea para el alta de la autorización..
     * @param listener    Objeto que procesara el resultado de la tarea.
     */
    public CreateVerifierTask(GenericUser validator, final SaveValidatorListener listener) {
        this.validator = validator;
        this.listener = listener;
    }

    @Override
    protected GenericResponse doInBackground(final Void... args) {

        GenericResponse response;
        try {
            response = CommManager.getInstance().saveValidator(this.validator);
        } catch (final Throwable e) {
            PfLog.e(SFConstants.LOG_TAG, "No se pudo dar de alta el validador", e); //$NON-NLS-1$
            // Comprobamos si el error se debe a la perdida de sesion
            response = new GenericResponse(
                    e.getMessage() != null && e.getMessage().contains(AUTH_ERROR)
                            ? GenericResponse.ERROR_LOST_SESSION
                            : GenericResponse.ERROR_COMMUNICATION);
        }

        return response;
    }


    @Override
    protected void onPostExecute(final GenericResponse result) {
        // Si fue cancelada, no se llama a los metodos de resultado
        if (isCancelled()) {
            return;
        }

        if (result.isSuccess()) {
            this.listener.validatorSaved();
        } else {
            this.listener.errorSavingValidator(result);
        }
    }

    /**
     * Listener para el procesado del resultado del alta del validador.
     */
    public interface SaveValidatorListener {
        void validatorSaved();

        void errorSavingValidator(final GenericResponse result);
    }
}
