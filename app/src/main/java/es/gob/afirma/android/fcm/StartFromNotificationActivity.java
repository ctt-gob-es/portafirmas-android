package es.gob.afirma.android.fcm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.SSLHandshakeException;

import es.gob.afirma.android.signfolder.AppPreferences;
import es.gob.afirma.android.signfolder.LoginActivity;
import es.gob.afirma.android.signfolder.PetitionListActivity;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.util.PfLog;


/**
 * Class that represents the notification activity manager.
 */
public class StartFromNotificationActivity extends FragmentActivity {

    final static String EXTRA_RESOURCE_CERT_B64 = "es.gob.afirma.signfolder.cert"; //$NON-NLS-1$

    Thread thread = new Thread(new Runnable() {

        @Override
        public void run() {
            try {
                URL url = new URL(AppPreferences.getInstance().getSelectedProxyUrl());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int code = connection.getResponseCode();

                if (code == 200) {
                    Intent notificationIntent2 = new Intent(getApplicationContext(), PetitionListActivity.class);
                    notificationIntent2.putExtra(EXTRA_RESOURCE_CERT_B64, getIntent().getStringExtra(EXTRA_RESOURCE_CERT_B64));
                    notificationIntent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(notificationIntent2);
                }
            } catch (SSLHandshakeException e) {
                // Fallo por el protocolo SSL, pero tiene conexion
                Intent notificationIntent2 = new Intent(getApplicationContext(), PetitionListActivity.class);
                Intent intent = getIntent();
                String certb64 = intent.getStringExtra(EXTRA_RESOURCE_CERT_B64);
                notificationIntent2.putExtra(EXTRA_RESOURCE_CERT_B64, certb64);
                notificationIntent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(notificationIntent2);
            } catch (Exception e) {
                PfLog.e(SFConstants.LOG_TAG, "No se puede conectar con el portafirmas");
            }
            finish();
        }
    });

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Si inicia la actividad de login y la de peticion de firma
        Intent notificationIntent = new Intent(getApplicationContext(), LoginActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(notificationIntent);
        try {
            thread.start();
        } catch (Exception e) {
            //Se queda en la pantalla de login
            PfLog.e(SFConstants.LOG_TAG, "No se puede conectar con el portafirmas");
        }
    }
}
