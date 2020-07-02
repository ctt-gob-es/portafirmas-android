package es.gob.afirma.android.fcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;
import java.util.Map;

import es.gob.afirma.android.signfolder.AppPreferences;
import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.util.PfLog;

public class FCMService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    public static final String KEY_COUNT = "notificationCount";
    private static final String DATA_KEY = "gcm.notification.body";
    private static final String SEPARATOR = "\\\\\\$\\\\\\$";
    private static final String NOTIF_ID = "notificationId";
    private final static String EXTRA_RESOURCE_CERT_B64 = "es.gob.afirma.signfolder.cert";

    public FCMService() {
    }

    /**
     * Method called when the application receives an notification from the FCM server.
     *
     * @param remoteMessage Received message.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        PfLog.i(TAG, "Received message");
        String url = null;
        String dni = null;
        String op = null;

        // Se obtienen los 3 parametros obtenidos: url + dni + codigoOperacion
        //if(notificationsOn && data != null) {
        Map<String, String> dataMap = remoteMessage != null ? remoteMessage.getData() : null;
        if (dataMap != null) {
            String body = dataMap.get(DATA_KEY);
            if (body == null) {
                return;
            }
            String[] param = body.split(SEPARATOR);
            if (param.length == 3) {
                url = param[0];
                dni = param[1];
                op = param[2];
            }
        }

        // Los 3 campos son obligatorios para gestionar la aplicacion
        if (url == null || dni == null || op == null) {
            return;
        }

        // Se guarda en el fichero de preferencias un contador de notificaciones para apilarlas en una sola
        int currentCount = AppPreferences.getInstance().getPreferenceInt(KEY_COUNT + url, 0);
        currentCount = currentCount + 1;
        AppPreferences.getInstance().setPreferenceInt(KEY_COUNT + url, currentCount);
        // Gets an instance of the NotificationManager service
        String message;
        Context context = getApplicationContext();
        if (currentCount != 1) {
            message = context.getString(R.string.requests_notification, currentCount);
        } else {
            message = context.getString(R.string.request_notification);
        }

        // Verifica que el servidor proxy obtenido esta configurado en la aplicacion
        final List<String> servers = AppPreferences.getInstance().getServersList();

        if (!servers.isEmpty()) {
            // Se obtienen los alias de todos los servidores
            CharSequence[] aliases = servers.toArray(new CharSequence[servers.size()]);
            String serverURL;
            boolean found = false;
            int selectedServer = 0;
            // Se recorren todos los servidores
            while (selectedServer < aliases.length && !found) {
                // Se obtiene la URL para cada alias
                serverURL = AppPreferences.getInstance().getServer(aliases[selectedServer].toString());
                // Si la URL del servidor configurado coincide con el que se envia en la
                // notificacion lo seleccionamos como proxy por defecto
                if (serverURL.equals(url)) {
                    AppPreferences.getInstance().setSelectedProxy(
                            aliases[selectedServer].toString(),
                            serverURL);
                    found = true;
                }
                selectedServer++;
            }

            //Creacion de la notificacion a mostrar
            Object resNotification = NotificationUtilities.createNotification(context,
                    context.getString(R.string.title_notifications) + aliases[selectedServer - 1].toString(),
                    message, R.drawable.ic_notification, Notification.DEFAULT_ALL, Notification.PRIORITY_HIGH,
                    context.getString(R.string.app_name), message, null, true,
                    null, null);


            NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) resNotification;
            NotificationManager mNotifyMgr =
                    (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

            // Si se encuentra si lanza la notificacion y se intenta cargar el listado de peticiones
            // (si la sesion estaba ya iniciada se cargaran correctamente)
            if (found) {
                Intent notificationIntent = new Intent(context, StartFromNotificationActivity.class);
                String lastCertUsed = AppPreferences.getInstance().getLastCertificate();
                notificationIntent.putExtra(EXTRA_RESOURCE_CERT_B64, lastCertUsed);
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                        notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                mBuilder.setContentIntent(contentIntent);
                mBuilder.setAutoCancel(true);
                // Obtenemos el identificador de la notificacion para borrarla y reeplazarla por la nueva
                int notId = AppPreferences.getInstance().getPreferenceInt(NOTIF_ID + url, 0);
                if (notId == 0) {
                    //Se guarda el identificador de la notificacion en preferencias
                    notId = (int) System.currentTimeMillis();
                    AppPreferences.getInstance().setPreferenceInt(NOTIF_ID + url, notId);
                }
                // Ejemplo: Borramos "2 peticiones pendientes" y escribimos "3 peticiones pendientes"
                mNotifyMgr.cancel(notId);
                mNotifyMgr.notify(notId, mBuilder.build());
            } else {
                // Si no es un servidor configurado no se cuentan ni se muestran las notificaciones
                PfLog.w(SFConstants.LOG_TAG, "Se ha recibido una notificacion de un servidor no registrado: " + url);
                AppPreferences.getInstance().setPreferenceInt(KEY_COUNT + url, currentCount - 1);
            }
        }
    }



    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        if (token != null && !token.isEmpty()) {
            PfLog.i(SFConstants.LOG_TAG, "Token para notificaciones: " + token);
            // Registramos el token en las preferencias de la app.
            AppPreferences.getInstance().setCurrentToken(token);
        } else {
            PfLog.e(SFConstants.LOG_TAG, "No ha sido posible generar el token de notificaciones FireBase.");
        }
    }


}
