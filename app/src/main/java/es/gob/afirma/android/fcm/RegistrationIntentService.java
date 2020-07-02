package es.gob.afirma.android.fcm;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import es.gob.afirma.android.signfolder.AppPreferences;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.util.PfLog;

/**
 * Created by sergio.martinez on 16/08/2017.
 */
public class RegistrationIntentService extends IntentService
        implements OnCompleteListener<InstanceIdResult> {

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
            // [START register_for_fcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            // [START get_token]
            this.dni = intent.getStringExtra(EXTRA_RESOURCE_DNI);
            this.certB64 = intent.getStringExtra(EXTRA_RESOURCE_CERT_B64);
            this.userProxyId = intent.getStringExtra(EXTRA_RESOURCE_USER_PROXY_ID);
            this.noticeUser = intent.getBooleanExtra(EXTRA_RESOURCE_NOTICE_USER, false);
            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(this);

            // [END register_for_gcm]
        } catch (Exception e) {
            PfLog.e(SFConstants.LOG_TAG, "Error al recuperar el token de registro de Firebase", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            noticeResult(false);
        }
    }

    @Override
    public void onComplete(@NonNull Task<InstanceIdResult> task) {

        if (!task.isSuccessful()) {
            PfLog.e(SFConstants.LOG_TAG, "Error al recuperar el token de registro de Firebase");
            noticeResult(false);
            return;
        }
        // Almacenamos el token.
        if (task.getResult() != null) {
            token = task.getResult().getToken();
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

    /**
     * Se solicita al Portafirmas el alta en el sistema de notificaciones.
     * @param token Nuevo token.
     */
    public void sendRegistrationToServer(String token) {
        final String androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        PfLog.i(SFConstants.LOG_TAG, "Token usado para las notificaciones: " + token);
        new RegisterSIMServiceTask().execute(this, token, androidId, this.dni != null ? this.dni : this.certB64);
    }
}