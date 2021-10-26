package es.gob.afirma.android.fcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;
import java.util.Map;

import es.gob.afirma.android.signfolder.AppPreferences;
import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.activities.LoginActivity;
import es.gob.afirma.android.util.PfLog;

public class FCMService extends FirebaseMessagingService {

    public static final String KEY_COUNT = "notificationCount";
    private static final String DATA_KEY = "gcm.notification.body";
    private static final String SEPARATOR = "\\$";
    private static final String NOTIF_ID = "notificationId";

    private static final String NOTIFICATION_PARAM_URL = "url";
    private static final String NOTIFICATION_PARAM_USERID = "userid";
    private static final String NOTIFICATION_PARAM_TYPE = "type";

    private static final String NOTIFICATION_TYPE_NEWREQUEST = "1";

    public FCMService() {

        PfLog.i(SFConstants.LOG_TAG, "Firebase Cloud Message service creado");

    }

    /**
     * Method called when the application receives an notification from the FCM server.
     *
     * @param remoteMessage Received message.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        PfLog.i(SFConstants.LOG_TAG, "Notificacion PUSH recibida");
        if (remoteMessage == null) {
            PfLog.w(SFConstants.LOG_TAG, "No hay mensaje en la notificacion");
            return;
        }
        NotificationData notificationData;
        try {
            notificationData = loadData(remoteMessage);
        }
        catch (Exception e) {
            PfLog.w(SFConstants.LOG_TAG, "No se ha podido extraer la informacion de la notificacion", e);
            return;
        }

        // Cargamos las preferencias de la aplicacion
        Context context = getApplicationContext();
        AppPreferences prefs = AppPreferences.getInstance();

        // Solo se mostrara la notificacion si la emitio uno de los servidores configurados, asi
        // que se comprueba que haya servidores configurados y que se encuentre entre ellos
        final List<String> servers = prefs.getServersList();
        if (servers.isEmpty()) {
            PfLog.i(SFConstants.LOG_TAG, "No se han encontrado Portafirmas configurados");
            return;
        }
        String serverAlias = null;
        for (String alias : servers.toArray(new String[0])) {
            if (notificationData.getUrl().equals(prefs.getServer(alias))) {
                serverAlias = alias;
                break;
            }
        }
        if (serverAlias == null) {
            PfLog.i(SFConstants.LOG_TAG, "No se ha encontrado el Portafirmas indicado en la notificacion");
            return;
        }

        // Se guarda en las preferencias un contador de notificaciones para apilarlas en una sola
        int currentCount = prefs.getPreferenceInt(
                KEY_COUNT + notificationData.getUrl(), 0);
        prefs.setPreferenceInt(
                KEY_COUNT + notificationData.getUrl(), ++currentCount);

        String message;
        if (currentCount != 1) {
            message = context.getString(R.string.requests_notification, currentCount);
        } else {
            message = context.getString(R.string.request_notification);
        }

        // Creacion de la notificacion a mostrar
        Object resNotification = NotificationUtilities.createNotification(context,
                context.getString(R.string.title_notifications) + serverAlias,
                message, R.drawable.ic_notification, Notification.DEFAULT_ALL, Notification.PRIORITY_HIGH,
                context.getString(R.string.app_name), message, null, true,
                null, null);

        // Construimos la notificacion
        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) resNotification;

        // Definimos la accion de la notificacion
        Intent notificationIntent;

        // Notificacion de tipo: Nueva peticion entrante
        if (NOTIFICATION_TYPE_NEWREQUEST.equals(notificationData.getOp())) {
            notificationIntent = new Intent(context, StartFromNotificationActivity.class);
            notificationIntent.putExtra(StartFromNotificationActivity.EXTRA_RESOURCE_SERVERURL, notificationData.getUrl());
            notificationIntent.putExtra(StartFromNotificationActivity.EXTRA_RESOURCE_USERID, notificationData.getDni());
            notificationIntent.putExtra(StartFromNotificationActivity.EXTRA_RESOURCE_SERVERALIAS, serverAlias);
        }
        // Notificacion de tipo desconocido
        else {
            notificationIntent = new Intent(context, LoginActivity.class);
        }
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setAutoCancel(true);

        // Obtenemos el identificador de la notificacion para borrarla y reeplazarla por la nueva
        int notId = prefs.getPreferenceInt(
                NOTIF_ID + notificationData.getUrl(), 0);
        if (notId == 0) {
            //Se guarda el identificador de la notificacion en preferencias
            notId = (int) System.currentTimeMillis();
            prefs.setPreferenceInt(
                    NOTIF_ID + notificationData.getUrl(), notId);
        }
        // Ejemplo: Borramos "2 peticiones pendientes" y escribimos "3 peticiones pendientes"
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(notId);
        mNotifyMgr.notify(notId, mBuilder.build());
    }

    private NotificationData loadData(RemoteMessage remoteMessage) {

        NotificationData notificationData = null;
        if (remoteMessage.getData() != null) {
            Map<String, String> data = remoteMessage.getData();
            if (hasDataValues(data)) {
                notificationData = parseMetadata(data);
            } else if (data.containsKey(DATA_KEY)) {
                notificationData = parseMetadata(data.get(DATA_KEY));
            }
        }
        if (notificationData == null && remoteMessage.getNotification() != null) {
            notificationData = parseMetadata(remoteMessage.getNotification().getBody());
        }

        if (notificationData == null) {
            throw new IllegalArgumentException("No se pudo recuperar el contenido de la notificacion");
        }

        return notificationData;
    }

    private boolean hasDataValues(Map<String, String> data) {
        return data.containsKey(NOTIFICATION_PARAM_URL)
                && data.containsKey(NOTIFICATION_PARAM_TYPE);
    }

    NotificationData parseMetadata(String body) {

        if (body == null) {
            return null;
        }

        String[] param = body.split(SEPARATOR);
        if (param.length != 4 || param[0] == null || param[3] == null) {
            return null;
        }

        NotificationData data = new NotificationData();
        data.setUrl(param[0]);
        data.setDni(param[1]);
        data.setOp(param[3]);

        return data;
    }

    NotificationData parseMetadata(Map<String, String> data) {

        NotificationData notificationData = new NotificationData();
        notificationData.setUrl(data.get(NOTIFICATION_PARAM_URL));
        notificationData.setDni(data.get(NOTIFICATION_PARAM_USERID));
        notificationData.setOp(data.get(NOTIFICATION_PARAM_TYPE));

        return notificationData;
    }

    private static class NotificationData {

        String url;
        String dni;
        String op;

        public String getUrl() {
            return url;
        }

        public String getDni() {
            return dni;
        }

        public String getOp() {
            return op;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public void setDni(String dni) {
            this.dni = dni;
        }

        public void setOp(String op) {
            this.op = op;
        }
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(@Nullable String token) {

        PfLog.i(SFConstants.LOG_TAG, "Generacion de nuevo token de notificaciones: " + token);

        if (token != null && !token.isEmpty()) {
            PfLog.i(SFConstants.LOG_TAG, "Token para notificaciones: " + token);
            // Registramos el token en las preferencias de la app.
            AppPreferences.getInstance().setCurrentToken(token);
        } else {
            PfLog.e(SFConstants.LOG_TAG, "No ha sido posible generar el token de notificaciones FireBase.");
        }
    }

    @Override
    public void onDeletedMessages() {
        PfLog.i(SFConstants.LOG_TAG, "Mensaje eliminado");
    }
}
