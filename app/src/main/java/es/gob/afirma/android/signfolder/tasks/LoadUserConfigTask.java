package es.gob.afirma.android.signfolder.tasks;

import android.os.AsyncTask;

import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.ValidationLoginResult;
import es.gob.afirma.android.user.configuration.UserConfig;
import es.gob.afirma.android.util.PfLog;

public class LoadUserConfigTask extends AsyncTask<Void, Void, UserConfig> {
    private final ValidationLoginResult loginResult;
    private final LoadUserConfigListener listener;
    private Throwable t = null;

    public LoadUserConfigTask(ValidationLoginResult loginResult, LoadUserConfigListener listener) {
        this.loginResult = loginResult;
        this.listener = listener;
    }

    @Override
    protected UserConfig doInBackground(Void... voids) {
        UserConfig userConfig;
        try {
            userConfig = CommManager.getInstance().getUserConfig();
            if(userConfig == null){
                throw new IllegalArgumentException("Configuración de usuario nula.");
            }
        } catch (Exception e) {
            PfLog.w(SFConstants.LOG_TAG, "No se pudo obtener la configuración de usuario.", e); //$NON-NLS-1$
            userConfig = null;
            this.t = e;
        }

        return userConfig;
    }

    @Override
    protected void onPostExecute(final UserConfig userConfig) {
        if (userConfig != null) {
            this.listener.userConfigLoadSuccess(userConfig, this.loginResult);
        } else {
            this.listener.userConfigLoadError(this.loginResult, this.t);
        }
    }

    /**
     * Interfaz con los metodos para gestionar los resultados de la carga de la
     * configuraci&oacute;n de usuario de la aplicaci&oacute;n.
     */
    public interface LoadUserConfigListener {

        void userConfigLoadSuccess(UserConfig userConfig, ValidationLoginResult loginResult);

        void userConfigLoadError(ValidationLoginResult loginResult, Throwable t);
    }
}
