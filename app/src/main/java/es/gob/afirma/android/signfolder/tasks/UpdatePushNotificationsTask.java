package es.gob.afirma.android.signfolder.tasks;

import android.os.AsyncTask;
import android.util.Log;

import es.gob.afirma.android.signfolder.proxy.CommManager;

/**
 * Clase que implementa la tarea encargada de actualizar el estado de las notificacione push.
 */
public class UpdatePushNotificationsTask extends AsyncTask<Void, Void, String> {

    /**
     * Atributo que indica si las notificaciones se deben activar o desactivar.
     */
    private boolean enableNots;

    /**
     * Listener de la tarea.
     */
    private UpdatePushNotsListener listener;

    /**
     * Atributo que representa el error producido durante la ejecución de la tarea.
     */
    private Throwable t;

    /**
     * Constructor por defecto.
     * @param enableNots Indica si se debe activar (<i>true</i>) o desactivar (<i>false</i>) las notificaciones.
     * @param listener Listener de la tarea.
     */
    public UpdatePushNotificationsTask(boolean enableNots, UpdatePushNotsListener listener) {
        this.enableNots = enableNots;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(Void... voids) {
        String res = null;
        try {
            res = CommManager.getInstance().updatePushNotifications(enableNots);
        } catch (Exception e) {
            Log.e("es.gob.afirma", "No ha sido posible actualizar el estado de las notificaciones push: " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            this.t = e;
        }
        return res;
    }

    @Override
    protected void onPostExecute(String s) {
        if (s != null) {
            this.listener.onUpdatePushNotsSuccess(s);
        } else {
            this.listener.onUpdatePushNotsError(this.t);
        }
    }

    /**
     * Listener de la tarea de actualizar el estado de las notificaciones push.
     */
    public interface UpdatePushNotsListener {

        /**
         * Método para los casos de éxito.
         * @param result Resultado de la operación.
         */
        void onUpdatePushNotsSuccess(String result);

        /**
         * Método para los casos de error.
         * @param exception Excepción lanzada durante la operación.
         */
        void onUpdatePushNotsError(Throwable exception);
    }
}
