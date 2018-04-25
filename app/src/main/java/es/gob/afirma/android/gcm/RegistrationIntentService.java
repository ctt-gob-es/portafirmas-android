package es.gob.afirma.android.gcm;

import android.app.IntentService;
import android.content.Intent;

import java.io.IOException;

import es.gob.afirma.android.signfolder.AppPreferences;
import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.SFConstants;

/**
 * Created by sergio.martinez on 16/08/2017.
 */
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};

    public static final String EXTRA_RESOURCE_CERT_B64 = "es.gob.afirma.signfolder.cert"; //$NON-NLS-1$
    public static final String EXTRA_RESOURCE_USER_PROXY_ID = "es.gob.afirma.signfolder.userproxy";

    private String certB64 = null;
    private String userProxyId = null;

    private String token = null;

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            // [START get_token]
            this.certB64 = intent.getStringExtra(EXTRA_RESOURCE_CERT_B64);
            this.userProxyId = intent.getStringExtra(EXTRA_RESOURCE_USER_PROXY_ID);
            InstanceID instanceID = InstanceID.getInstance(this);
            this.token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                   GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]

            Log.i( SFConstants.LOG_TAG, "Token de registro en GCM: " + token);

            // Notificamos el nuevo token al Portafirmas
            sendRegistrationToServer(token);

            // [END register_for_gcm]
        } catch (Exception e) {
            Log.e( SFConstants.LOG_TAG, "Error al recuperar el token de registro de Google", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            noticeResult(false);
        }
    }

    /** Establece y notifica el resultado de la operacion de registro. */
    public void noticeResult(final boolean registered) {

        // Registramos el estado de las notificaciones para este usuario/proxy y, en caso positivo,
        // el token de notrificacion
        if (this.userProxyId != null) {

            Log.i(SFConstants.LOG_TAG, "Se han activado las notificaciones para el usuario-portafirmas: " + registered);
            AppPreferences.getInstance().setPreferenceBool(
                    AppPreferences.PREFERENCES_KEY_PREFIX_NOTIFICATION_ACTIVE + this.userProxyId,
                    registered);

            if (registered && this.token != null) {
                AppPreferences.getInstance().setPreference(
                        AppPreferences.PREFERENCES_KEY_PREFIX_NOTIFICATION_TOKEN + this.userProxyId,
                        this.token);
            }
        }

        // Anuncinamos el resultado
        sendBroadcast(registered);
    }

    private void sendBroadcast (boolean success){
        Intent intent = new Intent ("message"); //put the same message as in the filter you used in the activity when registering the receiver
        intent.putExtra("success", success);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Se solicita al Portafirmas el alta en el sistema de notificaciones.
     * @param token Nuevo token.
     */
    private void sendRegistrationToServer(String token) {
        final String androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        Log.i( SFConstants.LOG_TAG, "Token usado para las notificaciones: " + token);
        new RegisterSIMServiceTask().execute(this, token, androidId, this.certB64);
    }
}