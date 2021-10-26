package es.gob.afirma.android.signfolder.tasks;

import android.os.AsyncTask;

import java.util.List;

import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.user.configuration.ConfigurationRole;
import es.gob.afirma.android.user.configuration.GenericUser;
import es.gob.afirma.android.util.PfLog;

/**
 * Tarea para la búsqueda de usuarios para asignarlos como validadores o autorizados.
 */
public final class FindUserTask extends AsyncTask<Void, Void, List<GenericUser>> {

    /**
     * Codigo de error de autenticacion (perdida de sesion)
     */
    private static final String AUTH_ERROR = "ERR-11"; //$NON-NLS-1$

    private final ConfigurationRole role;
    private final String text;
    private final FindUserListener listener;

    /**
     * Crea la tarea para la búsqueda de usuarios.
     * @param role Rol que identifica la finalidad que se le desea asignar a los usuarios.
     * @param text Texto de búsqueda.
     * @param listener Objeto para procesar el resultado de la búsqueda.
     */
    public FindUserTask(ConfigurationRole role, String text, final FindUserListener listener) {
        this.role = role;
        this.text = text;
        this.listener = listener;
    }

    @Override
    protected List<GenericUser> doInBackground(final Void... args) {

        List<GenericUser> users;
        try {
            users = CommManager.getInstance().findUsers(this.role, this.text);
        } catch (final Throwable e) {
            users = null;
            PfLog.w(SFConstants.LOG_TAG, "No se pudo completar la busqueda de usuarios", e); //$NON-NLS-1$
        }

        return users;
    }


    @Override
    protected void onPostExecute(final List<GenericUser> result) {
        // Si fue cancelada, no se llama a los metodos de resultado
        if (isCancelled()) {
            return;
        }

        if (result != null) {
            this.listener.loadedUserList(result);
        } else {
            this.listener.errorLoadingUserList();
        }
    }

    /**
     * Interfaz con los m&eacute;todos para gestionar los resultados de la b&uacute;squeda de usuarios.
     */
    public interface FindUserListener {
        void loadedUserList(List<GenericUser> users);

        void errorLoadingUserList();
    }
}
