package es.gob.afirma.android.fcm;

import android.app.IntentService;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.installations.FirebaseInstallations;

import es.gob.afirma.android.signfolder.AppPreferences;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.util.PfLog;

/**
 * Created by sergio.martinez on 16/08/2017.
 */
public class RegistrationIntentService extends IntentService
        implements OnCompleteListener<String> {

    private static final String TAG = "RegIntentService";

    public static final String EXTRA_RESOURCE_DNI = "es.gob.afirma.signfolder.dni"; //$NON-NLS-1$
    public static final String EXTRA_RESOURCE_CERT_B64 = "es.gob.afirma.signfolder.cert"; //$NON-NLS-1$
    public static final String EXTRA_RESOURCE_USER_PROXY_ID = "es.gob.afirma.signfolder.userproxy";
    public static final String EXTRA_RESOURCE_NOTICE_USER = "es.gob.afirma.signfolder.noticeuser";

    private String dni = null;
    private String certB64 = null;
    private String userProxyId = null;
    private boolean noticeUser = false;

    protected static String token = null;

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            // Obtenemos los datos proporcionado en la llamada
            this.dni = intent.getStringExtra(EXTRA_RESOURCE_DNI);
            this.certB64 = intent.getStringExtra(EXTRA_RESOURCE_CERT_B64);
            this.userProxyId = intent.getStringExtra(EXTRA_RESOURCE_USER_PROXY_ID);
            this.noticeUser = intent.getBooleanExtra(EXTRA_RESOURCE_NOTICE_USER, false);
            FirebaseInstallations.getInstance().getId().addOnCompleteListener(this);

//            // Notificamos el nuevo token al Portafirmas
//            token = AppPreferences.getInstance().getCurrentToken();
//            sendRegistrationToServer(token);

        } catch (Exception e) {
            PfLog.e(SFConstants.LOG_TAG, "Error al recuperar el ID de instalacion de la aplicacion", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            noticeResult(false);
        }
    }

    /**
     * Se solicita al Portafirmas el alta en el sistema de notificaciones.
     * @param token Nuevo token.
     */
    public void sendRegistrationToServer(String token) {
        final String androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        PfLog.i(SFConstants.LOG_TAG, "Token usado para las notificaciones: " + token);
        new RegisterSIMServiceTask().execute(this, token, androidId, this.dni != null ? this.dni : this.certB64);
    }

    @Override
    public void onComplete(Task<String> task) {

        if (!task.isSuccessful()) {
            PfLog.e(SFConstants.LOG_TAG, "Error al recuperar el token de registro de Firebase");
            noticeResult(false);
            return;
        }
        // Almacenamos el token.
        if (task.getResult() != null) {
            token = task.getResult();
            PfLog.i(SFConstants.LOG_TAG, "Registramos el token de notificaciones proporcionado por FCM: " + token);
        }
        // Si no hemos podido recuperar el token, lo intentamos recuperar de las preferencias.
        if (token == null) {
            token = AppPreferences.getInstance().getCurrentToken();
            PfLog.i(SFConstants.LOG_TAG, "Registramos el token de notificaciones ya almacenado en las preferencias: " + token);
        }

        // Notificamos el nuevo token al Portafirmas
        sendRegistrationToServer(token);
    }

    /**
     * Establece y notifica el resultado de la operacion de registro.
     */
    public void noticeResult(final boolean registered) {

        // Registramos el estado de las notificaciones para este usuario/proxy y, en caso positivo,
        // el token de notrificacion
        if (this.userProxyId != null) {

            PfLog.i(SFConstants.LOG_TAG, "Se han activado las notificaciones para el usuario-portafirmas: " + registered);
            AppPreferences.getInstance().setPreferenceBool(
                    AppPreferences.PREFERENCES_KEY_PREFIX_NOTIFICATION_ACTIVE + this.userProxyId,
                    registered);

            if (registered && token != null) {
                AppPreferences.getInstance().setPreference(
                        AppPreferences.PREFERENCES_KEY_PREFIX_NOTIFICATION_TOKEN + this.userProxyId,
                        token);
                PfLog.i(SFConstants.LOG_TAG, "Establecemos en la tabla interna de tokens: "
                        + AppPreferences.PREFERENCES_KEY_PREFIX_NOTIFICATION_TOKEN + this.userProxyId
                        + "=" + token);
            }
        }

        // Anuncinamos el resultado
        sendBroadcast(registered);
    }

    private void sendBroadcast(boolean success) {
        Intent intent = new Intent("message"); //put the same message as in the filter you used in the activity when registering the receiver
        intent.putExtra("success", success);
        intent.putExtra("noticeUser", this.noticeUser);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}