package es.gob.afirma.android.signfolder.tasks;

import android.os.AsyncTask;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.ServerControlledException;
import es.gob.afirma.android.user.configuration.ConfigurationRole;
import es.gob.afirma.android.util.PfLog;

/**
 * Tarea para la carga de los listados de usuarios validadores y las autorizaciones emitidas por
 * el usuario.
 */
public final class LoadDelegationsTask extends AsyncTask<Void, Void, List<?>> {

    /**
     * Codigo de error de autenticacion (perdida de sesion)
     */
    private static final String AUTH_ERROR = "ERR-11"; //$NON-NLS-1$
    private final ConfigurationRole role;
    private final LoadDelegationsListener listener;
    private boolean lostSession = false;

    /**
     * Crea la tarea para la carga de los detalles de una petici&oacute;n en una pantalla para la
     * visualizaci&oacute;n de la descripci&oacute;n de peticiones.
     *
     * @param role Rol que identifica el tipo de datos que se debe recuperar (autorizaciones o validadores).
     * @param commManager Manejador de los servicios de comunicaci&oacute;n con el portafirmas.
     * @param listener    Actividad en la que es posible mostrar los datos.
     */
    public LoadDelegationsTask(ConfigurationRole role, final LoadDelegationsListener listener) {
        this.role = role;
        this.listener = listener;
    }

    @Override
    protected List<?> doInBackground(final Void... args) {

        List<?> delegations;
        try {
            delegations = getListUserByRole(this.role);
        } catch (final Exception e) {
            delegations = null;
            PfLog.e(SFConstants.LOG_TAG, "Ocurrio un error al recuperar el listado de delegaciones " + this.role, e); //$NON-NLS-1$
            // Si se ha perdido la sesion vuelve a la pantalla de login
            if (e.getMessage().contains(AUTH_ERROR)) {
                lostSession = true;
            }
        } catch (final Throwable e) {
            PfLog.w(SFConstants.LOG_TAG, "No se pudo obtener el listado de delegaciones: " + e); //$NON-NLS-1$
            delegations = null;
        }

        return delegations;
    }

    /**
     * Método que recupera la lista de usuarios que pueden emplearse para un rol.
     * @param role Rol seleccionado.
     * @return la lista de usuarios asociados al rol proporcionado.
     * @throws ServerControlledException si algo falla.
     * @throws SAXException              si algo falla.
     * @throws IOException               si algo falla.
     */
    private static List<?> getListUserByRole(ConfigurationRole role) throws ServerControlledException, SAXException, IOException {
        if (role.equals(ConfigurationRole.AUTHORIZED)) {
            return CommManager.getInstance().getAuthorizations();
        }
        else if (role.equals(ConfigurationRole.VERIFIER)) {
            return CommManager.getInstance().getValidators();
        }
        return new ArrayList<>();
    }

    @Override
    protected void onPostExecute(final List<?> result) {
        // Si fue cancelada, no se llama a los metodos de resultado
        if (isCancelled()) {
            return;
        }

        if (result != null) {
            this.listener.loadedDelegations(this.role, result);
        } else if (lostSession) {
            this.listener.lostSession();
        } else {
            this.listener.errorLoadingDelegations(this.role);
        }
    }

    /**
     * Interfaz con los métodos para gestionar los resultados de la carga del listado de
     * autorizaciones o validadores de un usuario.
     */
    public interface LoadDelegationsListener {
        void loadedDelegations(ConfigurationRole role, List<?> details);

        void errorLoadingDelegations(ConfigurationRole role);

        void lostSession();
    }
}
