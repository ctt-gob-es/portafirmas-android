package es.gob.afirma.android.fcm;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import es.gob.afirma.android.signfolder.AppPreferences;
import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.util.PfLog;

public final class NotificationUtilities {

    /**
     * Default vibration pattern for channel notificactions.
     */
    private static final long[] DEFAULT_VIBRATION_PATTERN = {100, 200, 300, 400, 500, 400, 300, 200, 400};

    /**
     * Notification manager object.
     */
    private static NotificationManager notManager = null;

    /**
     * Notification channel object that represents the default notification channel.
     */
    private static NotificationChannel defaultNotChannel = null;

    private static String token = null;

    /**
     * Intent used to display a message in the screen.
     */
    private static final String DISPLAY_MESSAGE_ACTION =
            "com.google.android.gcm.demo.app.DISPLAY_MESSAGE";

    /**
     * Intent's extra that contains the message to be displayed.
     */
    private static final String EXTRA_MESSAGE = "message";

    /**
     * Method that create a new notificaction.
     *
     * @param context            Application context.
     * @param contentTitle       Title of the notificaction. This parameter is only used for notifications older than 26 SDK version.
     * @param contentText        Text of the notification. This parameter is only used for notifications older than 26 SDK version.
     * @param icon               Icon to show in the notification. This parameter is only used for notifications older than 26 SDK version.
     * @param defaults           Default configuration of the notification. This parameter is only used for notifications older than 26 SDK version.
     * @param priority           Priority of the notification. This parameter is only used for notifications older than 26 SDK version.
     * @param channelName        Name of the channel. This parameter is only used for notifications newer or equal than 26 SDK version.
     * @param channelDescription Description of the channel. This parameter is only used for notifications newer or equal than 26 SDK version.
     * @param importance         Importance level of the notificacion channel. This parameter is only used for notifications newer or equal than 26 SDK version.
     * @param enableLights       Boolean that indicates if the lights must be enable or not. This parameter is only used for notifications newer or equal than 26 SDK version.
     * @param channelColor       Color of the notifications. This parameter is only used for notifications newer or equal than 26 SDK version.
     * @param vibrationPattern   Pattern that sets up the vibration of the notification. This parameter is only used for notifications newer or equal than 26 SDK version.
     * @return the identifier of the channel if the SDK version is greater than 26, or the notification builder in other case.
     */
    public static Object createNotification(Context context, CharSequence contentTitle, CharSequence contentText,
                                            Integer icon, Integer defaults, Integer priority,
                                            CharSequence channelName, String channelDescription,
                                            Integer importance, boolean enableLights,
                                            Color channelColor, long[] vibrationPattern) {
        String identifier = null;
        // Si la versión del SDK es 26 o superior, construimos la notificación mediante un channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Si el channel no está construido, lo construimos.
            if (defaultNotChannel == null) {
                identifier = context.getString(R.string.title_notifications);
                createChannelNotification(context, identifier, channelName, channelDescription, importance, enableLights, channelColor, vibrationPattern);
            } else {
                identifier = defaultNotChannel.getId();
            }
        }
        // Si es menor a la versión 26, construimos la notificación mediante el método Builder de NotificationCompat.
        return createCompatNotificationBuilder(context, contentTitle, contentText, icon, defaults, priority, identifier);
    }

    /**
     * Method that create a new notification channel.
     *
     * @param context            Application context.
     * @param idChannel          Channel identifier.
     * @param channelName        Name of the channel.
     * @param channelDescription Description of the channel.
     * @param importance         Importance level of the notificacion channel.
     * @param enableLights       Boolean that indicates if the lights must be enable or not.
     * @param channelColor       Color of the notifications.
     * @param vibrationPattern   Pattern that sets up the vibration of the notification.
     */
    @TargetApi(Build.VERSION_CODES.O)
    private static void createChannelNotification(Context context, String idChannel, CharSequence channelName,
                                                  String channelDescription, Integer importance,
                                                  boolean enableLights, Color channelColor, long[] vibrationPattern) {
        // Creamos el objeto de tipo channel y lo personalizamos con los parámetros de entrada recibidos.
        if (importance == null) {
            importance = NotificationManager.IMPORTANCE_DEFAULT;
        }
        defaultNotChannel = new NotificationChannel(idChannel, channelName, importance);
        defaultNotChannel.setDescription(channelDescription);
        defaultNotChannel.enableLights(enableLights);
//        if (channelColor != null) {
//            defaultNotChannel.setLightColor(channelColor.toArgb());
//        }
        if (vibrationPattern != null) {
            defaultNotChannel.setVibrationPattern(vibrationPattern);
        } else {
            defaultNotChannel.setVibrationPattern(DEFAULT_VIBRATION_PATTERN);
        }

        // Si aún no hemos inicializado el NotificationManager, lo hacemos.
        if (notManager == null) {
            notManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        // Creamos el channel.
        notManager.createNotificationChannel(defaultNotChannel);
    }

    /**
     * Method that create a new compat notification.
     *
     * @param context      Application context.
     * @param contentTitle Title of the notificaction.
     * @param contentText  Text of the notification.
     * @param icon         Icon to show in the notification. Optional.
     * @param defaults     Default configuration of the notification. Optional.
     * @param priority     Priority of the notification. Optional.
     * @param idChannel    Channel identifier. Optional.
     * @return the notification builder initialized.
     */

    private static NotificationCompat.Builder createCompatNotificationBuilder(Context context, CharSequence contentTitle,
                                                                              CharSequence contentText, Integer icon,
                                                                              Integer defaults, Integer priority, String idChannel) {
        // Creamos el objeto builder
        NotificationCompat.Builder mBuilder;
        // Si tenemos identificador de canal, significa que la notificación será generada para
        // una versión de SDK igual o superior a 26.
        if (idChannel == null) {
            //noinspection deprecation
            mBuilder = new NotificationCompat.Builder(context);
        } else {
            mBuilder = new NotificationCompat.Builder(context, idChannel);
        }
        // inicializamos sus valores con los parámetros recibidos.
        mBuilder.setContentTitle(contentTitle);
        mBuilder.setContentText(contentText);
        if (icon == null) {
            mBuilder.setSmallIcon(R.drawable.ic_notification);
        } else {
            mBuilder.setSmallIcon(icon);
        }
        if (defaults == null) {
            mBuilder.setDefaults(Notification.DEFAULT_ALL);
        } else {
            mBuilder.setDefaults(defaults);
        }
        if (priority == null) {
            mBuilder.setPriority(Notification.PRIORITY_HIGH);
        } else {
            mBuilder.setPriority(priority);
        }

        // Devolvemos el constructor de notificaciones.
        return mBuilder;
    }

    /**
     * Notifies UI to display a message.
     * <p>
     * This method is defined in the common helper because it's used both by
     * the UI and the background service.
     *
     * @param context application's context.
     * @param message message to be displayed.
     */
    static void displayMessage(Context context, String message) {
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }

    /**
     * Obtiene el token de recepci&oacute;n de notificaciones actual y lo registra internamente
     * como el token de notificaciones activo.
     */
    public static void registerCurrentToken() {

        if (token == null) {
/*
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                PfLog.w(SFConstants.LOG_TAG, "No se pudo recuperar el token de notificaciones actual", task.getException());
                                return;
                            }

                            // Recuperamos el token y lo almacenamos en el atributo.
                            if (task.getResult() != null) {
                                token = task.getResult().getToken();
                                PfLog.i(SFConstants.LOG_TAG, "Se obtiene el token de notificaciones actual (modo antiguo): " + token);
                                AppPreferences.getInstance().setCurrentToken(token);
                            }
                        }
                    });
*/
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(Task<String> task) {
                            if (!task.isSuccessful()) {
                                PfLog.w(SFConstants.LOG_TAG, "Fetching FCM registration token failed", task.getException());
                                return;
                            }

                            // Get new FCM registration token
                            token = task.getResult();

                            // Log
                            PfLog.i(SFConstants.LOG_TAG, "Se obtiene el token de notificaciones actual (modo nuevo): " + token);
                            AppPreferences.getInstance().setCurrentToken(token);
                        }
                    });
        }
    }
}
