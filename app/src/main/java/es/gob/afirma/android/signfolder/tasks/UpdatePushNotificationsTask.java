package es.gob.afirma.android.signfolder.tasks;

import android.os.AsyncTask;
import android.util.Log;

import es.gob.afirma.android.signfolder.proxy.CommManager;

/**
 * Clase que implementa la tarea encargada de actualizar el estado de las notificacione push.
 */
public class UpdatePushNotificationsTask extends AsyncTask<Void, Void, Boolean> {

    /**
     * Atributo que indica si las notificaciones se deben activar o desactivar.
     */
    private final boolean requestedState;

    /**
     * Listener de la tarea.
     */
    private final UpdatePushNotsListener listener;

    /**
     * Atributo que representa el error producido durante la ejecución de la tarea.
     */
    private Throwable t;

    /**
     * Constructor por defecto.
     * @param requestedState Indica si se debe activar (<i>true</i>) o desactivar (<i>false</i>) las notificaciones.
     * @param listener Listener de la tarea.
     */
    public UpdatePushNotificationsTask(boolean requestedState, UpdatePushNotsListener listener) {
        this.requestedState = requestedState;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        Boolean result = Boolean.FALSE;
        try {
            result = CommManager.getInstance().updatePushNotifications(requestedState);
        } catch (Exception e) {
            Log.e("es.gob.afirma", "No ha sido posible actualizar el estado de las notificaciones push: " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            this.t = e;
        }
        return result;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (this.t == null) {
            this.listener.onUpdatePushNotsSuccess(this.requestedState, result != null ? result.booleanValue() : false);
        } else {
            this.listener.onUpdatePushNotsError(this.requestedState, this.t);
        }
    }

    /**
     * Listener de la tarea de actualizar el estado de las notificaciones push.
     */
    public interface UpdatePushNotsListener {

        /**
         * Método para los casos de éxito.
         * @param enable Indica si se ha solicitado activar (true) o desactivar (false) las notificaciones.
         * @param result Si el cambio se completó ({@code true}) o no ({@code false}).
         */
        void onUpdatePushNotsSuccess(boolean enable, boolean result);

        /**
         * Método para los casos de error.
         * @param enable Indica si se ha solicitado activar (true) o desactivar (false) las notificaciones.
         * @param exception Excepción lanzada durante la operación.
         */
        void onUpdatePushNotsError(boolean enable, Throwable exception);
    }
}
