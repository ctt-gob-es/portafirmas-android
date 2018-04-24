package es.gob.afirma.android.gcm;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.SFConstants;

/**
 * Tarea que recupera de Google el token para el env&iacute;o de notificaciones.
 */
public class RecoverNotificationTokenAsyncTask extends AsyncTask<Object, Object, String> {

    private final Context context;
    private final RecoverNotificationTokenListener listener;

    public RecoverNotificationTokenAsyncTask(Context context, RecoverNotificationTokenListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(Object[] objects) {

        String token;
        try {
            InstanceID instanceID = InstanceID.getInstance(context);
            token = instanceID.getToken(
                    context.getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE);
        } catch (Exception e) {
            Log.w(SFConstants.LOG_TAG, "No se pudo obtener el token para notificaciones", e);
            token = null;
        }
        return token;
    }

    @Override
    protected void onPostExecute(String token) {
        if (this.listener != null) {
            this.listener.updateNotificationToken(token);
        }
    }

    /**
     * Interfaz con los m&eacute;todos para obtener el token de notificaci&oacute;n.
     */
    public interface RecoverNotificationTokenListener {
        /**
         * Informa del token para notificaciones.
         * @param token Token para notificaciones.
         */
        void updateNotificationToken(String token);
    }
}
