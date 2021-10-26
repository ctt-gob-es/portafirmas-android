package es.gob.afirma.android.signfolder;

import android.app.Application;
import android.content.Context;

/**
 * Wrapper de la aplicaci&oacute;n portafirmas.
 */
public class SignfolderApp extends Application {

    /** Versi&oacute;n del servicio proxy que introduce la gesti&oacute;n de las autorizaciones y los validadores. */
    public static final int PROXY_VERSION_25 = 25;

    private static Context context;

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getAppContext() {
        return context;
    }
}
