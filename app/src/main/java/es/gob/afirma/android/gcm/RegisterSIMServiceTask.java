package es.gob.afirma.android.gcm;

import android.os.AsyncTask;
import android.util.Log;

import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.NotificationRegistryResult;

/**
 * Created by sergio.martinez on 17/08/2017.
 */

class RegisterSIMServiceTask extends AsyncTask<Object, Object, Void> {
    @Override
    protected Void doInBackground(Object... params) {

        RegistrationIntentService register = (RegistrationIntentService) params[0];
        NotificationRegistryResult result;
        try {
            result = CommManager.getInstance().registerOnNotificationService(
                    params[1].toString(), // Token GCM
                    params[2].toString(), // Id dispositivo
                    params[3].toString() // Certificado de usuario
            );
        } catch (Exception e) {
            Log.w(SFConstants.LOG_TAG, "Error al registrar la aplicacion en el sistema de notificaciones", e);
            register.noticeResult(false);
            return null;
        }

        if (result != null && result.isOk()) {
            register.noticeResult(true);
        }
        else {
            Log.w(SFConstants.LOG_TAG, "El portafirmas notifico un error en el registro en el sistema de notificaciones: "
                    + result != null ? result.getError() : null);
            register.noticeResult(false);
        }

        return null;
    }
}
