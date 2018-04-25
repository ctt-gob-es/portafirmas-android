package es.gob.afirma.android.gcm;

/**
 * Created by sergio.martinez on 21/09/2017.
 */

/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static es.gob.afirma.android.gcm.CommonUtilities.SENDER_ID;
import static es.gob.afirma.android.gcm.CommonUtilities.displayMessage;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.List;

import es.gob.afirma.android.signfolder.LoginActivity;
import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.AppPreferences;

/**
 * {@link IntentService} responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {

    public static final String KEY_COUNT = "notificationCount";
    public static final String NOTIF_ID = "notificationId";
    final static String EXTRA_RESOURCE_CERT_B64 = "es.gob.afirma.signfolder.cert"; //$NON-NLS-1$


    private static final String SEPARATOR = "\\$\\$";

    @SuppressWarnings("hiding")
    private static final String TAG = "GCMIntentService";

    public GCMIntentService() {
        super(SENDER_ID);
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "Device registered: regId = " + registrationId);
        displayMessage(context, getString(R.string.enable_notifications));
        ServerUtilities.register(context, registrationId);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
        displayMessage(context, getString(R.string.enable_notifications));
        if (GCMRegistrar.isRegisteredOnServer(context)) {
            ServerUtilities.unregister(context, registrationId);
        } else {
            // This callback results from the call to unregister made on
            // ServerUtilities when the registration to the server failed.
            Log.i(TAG, "Ignoring unregister callback");
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "Received message");
        String url = null;
        String dni = null;
        String op = null;
        Bundle data = intent.getExtras();

        // TODO: Se verifica que las notificaciones esten activadas para el usuario y proxy concreto
        // para mostrarla solo en caso afirmativo

        // Se obtienen los 3 parametros obtenidos: url + dni + codigoOperacion
        //if(notificationsOn && data != null) {
        if(data != null) {
            String body = (String) data.get("gcm.notification.body");
            if (body != null) {
                String[] param = body.split(SEPARATOR);
                if(param.length == 3) {
                    url = param[0];
                    dni = param[1];
                    op = param[2];
                }
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
        if(currentCount != 1) {
            message = context.getString(R.string.requests_notification, currentCount);
        }
        else {
            message = context.getString(R.string.request_notification);
        }

        // Verifica que el servidor proxy obtenido esta configurado en la aplicacion
        final List<String> servers = AppPreferences.getInstance().getServersList();
        int selectedServer = 0;
        boolean found = false;
        if (!servers.isEmpty()) {
            // Se obtienen los alias de todos los servidores
            CharSequence[] aliases = servers.toArray(new CharSequence[servers.size()]);
            String serverURL;
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
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle(context.getString(R.string.title_notifications) + " " + aliases[selectedServer - 1].toString())
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setPriority(Notification.PRIORITY_HIGH)
                            .setContentText(message);

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
                if(notId == 0) {
                    //Se guarda el identificador de la notificacion en preferencias
                    notId = (int) System.currentTimeMillis();
                    AppPreferences.getInstance().setPreferenceInt(NOTIF_ID + url, notId);
                }
                // Ejemplo: Borramos "2 peticiones pendientes" y escribimos "3 peticiones pendientes"
                mNotifyMgr.cancel(notId);
                mNotifyMgr.notify(notId, mBuilder.build());
            }
            else {
                // Si no es un servidor configurado no se cuentan ni se muestran las notificaciones
                Log.w(SFConstants.LOG_TAG, "Se ha recibido una notificacion de un servidor no registrado: " + url);
                AppPreferences.getInstance().setPreferenceInt(KEY_COUNT + url, currentCount - 1);
            }
        }
    }

    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
        String message = getString(R.string.enable_notifications, total);
        displayMessage(context, message);
        // notifies user
        generateNotification(context, message);
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
        displayMessage(context, getString(R.string.enable_notifications, errorId));
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
        displayMessage(context, getString(R.string.enable_notifications,
                errorId));
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    public static void generateNotification(Context context, String message) {
        int icon = R.drawable.arrow_first;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, when);
        Intent notificationIntent = new Intent(context, LoginActivity.class);
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
    }

}
