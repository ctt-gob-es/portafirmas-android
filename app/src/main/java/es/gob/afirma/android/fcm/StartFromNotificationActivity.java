package es.gob.afirma.android.fcm;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.SSLHandshakeException;

import es.gob.afirma.android.signfolder.AppPreferences;
import es.gob.afirma.android.signfolder.activities.LoginActivity;
import es.gob.afirma.android.signfolder.activities.PetitionListActivity;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.user.configuration.ConfigurationConstants;
import es.gob.afirma.android.util.PfLog;


/**
 * Class that represents the notification activity manager.
 */
public class StartFromNotificationActivity extends FragmentActivity {

    public static final String EXTRA_RESOURCE_SERVERURL = "es.gob.afirma.signfolder.serverurl";
    public static final String EXTRA_RESOURCE_SERVERALIAS = "es.gob.afirma.signfolder.serveralias";
    public static final String EXTRA_RESOURCE_USERID = "es.gob.afirma.signfolder.userid";
    final static String EXTRA_RESOURCE_CERT_B64 = "es.gob.afirma.signfolder.cert"; //$NON-NLS-1$


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cargamos los datos de la notificacion proporcionados en la notificacion
        String serverUrl = getIntent().getStringExtra(EXTRA_RESOURCE_SERVERURL);
        String serverAlias = getIntent().getStringExtra(EXTRA_RESOURCE_SERVERALIAS);
        String userId = getIntent().getStringExtra(EXTRA_RESOURCE_USERID);

        // Comprobamos si el Portafirmas solicitado es el actual
        AppPreferences prefs = AppPreferences.getInstance();
        String selectedUrl = prefs.getSelectedProxyUrl();

        // Si estaba configurado, comprobamos si hay sesion y accedemos en dicho caso
        if (serverUrl.equals(selectedUrl) && CommManager.getInstance().isUserLogged()) {
            Intent intent = new Intent(getApplicationContext(), PetitionListActivity.class);
            intent.putExtra(ConfigurationConstants.EXTRA_RESOURCE_FORCE_REFRESH, true);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        // Si no estaba, lo configuramos y abrimos la pagina principal
        else {
            prefs.setSelectedProxy(serverAlias, selectedUrl);
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        finish();
    }
}
