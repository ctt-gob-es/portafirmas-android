package es.gob.afirma.android.signfolder.tasks;

import android.os.AsyncTask;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.GenericResponse;
import es.gob.afirma.android.signfolder.proxy.ServerControlledException;
import es.gob.afirma.android.user.configuration.Authorization;
import es.gob.afirma.android.user.configuration.ConfigurationRole;
import es.gob.afirma.android.user.configuration.GenericUser;
import es.gob.afirma.android.util.PfLog;

/**
 * Tarea para el alta de una nueva autorización.
 */
public final class CreateAuthorizationTask extends AsyncTask<Void, Void, GenericResponse> {

    /**
     * Codigo de error de autenticacion (perdida de sesion)
     */
    private static final String AUTH_ERROR = "ERR-11"; //$NON-NLS-1$
    private final Authorization auth;
    private final SaveAuthorizationListener listener;

    /**
     * Crea la tarea para el alta de la autorización..
     * @param listener    Objeto que procesara el resultado de la tarea.
     */
    public CreateAuthorizationTask(Authorization auth, final SaveAuthorizationListener listener) {
        this.auth = auth;
        this.listener = listener;
    }

    @Override
    protected GenericResponse doInBackground(final Void... args) {

        GenericResponse response;
        try {
            response = CommManager.getInstance().saveAuthorization(this.auth);
        } catch (final Throwable e) {
            PfLog.e(SFConstants.LOG_TAG, "No se pudo guardar la autorizacion", e); //$NON-NLS-1$
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
            this.listener.authorizationSaved();
        } else {
            this.listener.errorSavingAuthorization(result);
        }
    }

    /**
     * Listener para el procesado del resultado de la tarea de guardado de la autorización.
     */
    public interface SaveAuthorizationListener {
        void authorizationSaved();

        void errorSavingAuthorization(GenericResponse errorResponse);
    }
}
