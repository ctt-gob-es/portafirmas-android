package es.gob.afirma.android.signfolder.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import es.gob.afirma.android.signfolder.ConfigureFilterDialogBuilder;
import es.gob.afirma.android.signfolder.MessageDialog;
import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.RequestAppConfiguration;
import es.gob.afirma.android.signfolder.proxy.ValidationLoginResult;
import es.gob.afirma.android.signfolder.tasks.LoadConfigurationDataTask;
import es.gob.afirma.android.user.configuration.ConfigurationConstants;
import es.gob.afirma.android.user.configuration.ConfigurationRole;
import es.gob.afirma.android.util.PfLog;

/**
 * Clase que gestiona la actividad asociada al logeado con roles.
 */
public class LoginWithRoleActivity extends Activity implements LoadConfigurationDataTask.LoadConfigurationListener {

    /**
     * Attributo que contiene el resultado de la validación del proceso login.
     */
    private ValidationLoginResult validationResult;

    /**
     * Dialogo para mostrar mensajes al usuario
     */
    private MessageDialog messageDialog = null;

    private ConfigurationRole selectedRole;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Recuperamos los parámetros de la validación de login.
        Intent intent = getIntent();
        String certAlias = intent.getStringExtra(ConfigurationConstants.VALIDATION_RESULT_CERT_ALIAS);
        String certB64 = intent.getStringExtra(ConfigurationConstants.VALIDATION_RESULT_CERT_B64);
        String dni = intent.getStringExtra(ConfigurationConstants.VALIDATION_RESULT_DNI);
        String errorMsg = intent.getStringExtra(ConfigurationConstants.VALIDATION_RESULT_ERROR_MSG);
        boolean statusOK = intent.getBooleanExtra(ConfigurationConstants.VALIDATION_STATUS_OK, false);
        String[] roles = intent.getStringArrayExtra(ConfigurationConstants.VALIDATION_RESULT_ROLES);
        List<ConfigurationRole> roleLs = new ArrayList<>();

        for (String role : roles) {
            ConfigurationRole r = ConfigurationRole.getValue(role);
            if (r != null) {
                roleLs.add(r);
            }
        }

        validationResult = new ValidationLoginResult(statusOK);
        validationResult.setCertAlias(certAlias);
        validationResult.setCertificateB64(certB64);
        validationResult.setDni(dni);
        validationResult.setErrorMsg(errorMsg);
        validationResult.setRoleLs(roleLs);

        // Cargamos la vista.
        setContentView(R.layout.activity_choose_role);

        // Recuperamos los elementos de la vista a modificar.
        ProgressBar pb = findViewById(R.id.progressBarId);
        pb.setVisibility(View.INVISIBLE);
        Button verifierBtn = findViewById(R.id.button_access_verifier);
        Button authBtn = findViewById(R.id.button_access_auth);
        LinearLayout btnLayout = findViewById(R.id.ChooseRoleButtonsId);

        // Si no existe un determinado role, eliminamos el botón asociado.
        if (!validationResult.getRoleLs().contains(ConfigurationRole.VERIFIER)) {
            btnLayout.removeView(verifierBtn);
        }
        if (!validationResult.getRoleLs().contains(ConfigurationRole.AUTHORIZED)) {
            btnLayout.removeView(authBtn);
        }
    }

    /**
     * Accede a la aplicación con el rol de autorizado.
     *
     * @param view vista actual.
     */
    public void accessAsAuthorized(final View view) {
        this.selectedRole = ConfigurationRole.AUTHORIZED;
        showProgressBar();
        loadConfigData();
    }

    /**
     * Accede a la aplicación con el rol de validador.
     *
     * @param view vista actual.
     */
    public void accessAsVerifier(final View view) {
        this.selectedRole = ConfigurationRole.VERIFIER;
        showProgressBar();
        loadConfigData();
    }

    /**
     * Accede a la aplicación con el rol de firmante.
     *
     * @param view vista actual.
     */
    public void accessAsSigner(final View view) {
        showProgressBar();
        loadConfigData();
    }

    /**
     * Método encargado de cargar toda la información de configuración.
     */
    private void loadConfigData() {
        final LoadConfigurationDataTask lcd = new LoadConfigurationDataTask(
                validationResult,
                CommManager.getInstance(),
                this,
                this, this.selectedRole);
        lcd.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        try {
            lcd.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.w("Load config data error", "No ha sido posible cargar la configuración de usuario.", e); //$NON-NLS-1$
            Intent intent = new Intent(this, LoginActivity.class);
            String errorMsg = "No ha sido posible cargar la configuración de usuario."; //$NON-NLS-1$
            intent.putExtra(ConfigurationConstants.EXTRA_RESOURCE_ERROR_MSG, errorMsg);
            startActivity(intent);
            finish();
        }
    }

    /**
     * Método encargado de mostrar la barra de progreso.
     */
    private void showProgressBar() {
        // Recuperamos todos los elementos a procesar.
        ProgressBar pb = findViewById(R.id.progressBarId);
        Button verifierBtn = findViewById(R.id.button_access_verifier);
        Button authBtn = findViewById(R.id.button_access_auth);
        Button signerBtn = findViewById(R.id.button_access_signer);

        // deshabilitamos los botones.
        if (verifierBtn != null) {
            verifierBtn.setEnabled(false);
        }
        if (authBtn != null) {
            authBtn.setEnabled(false);
        }
        signerBtn.setEnabled(false);

        // Mostramos la barra de progreso.
        pb.setVisibility(View.VISIBLE);
    }

    @Override
    public void configurationLoadSuccess(RequestAppConfiguration appConfig, ValidationLoginResult loginResult) {

        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setClass(this, PetitionListActivity.class);
        if (loginResult != null && loginResult.getDni() != null) {
            intent.putExtra(PetitionListActivity.EXTRA_RESOURCE_DNI, loginResult.getDni());
        }
        if (loginResult != null && loginResult.getCertAlias() != null) {
            intent.putExtra(PetitionListActivity.EXTRA_RESOURCE_CERT_ALIAS, loginResult.getCertAlias());
        }
        intent.putStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_IDS, appConfig.getAppIdsList());
        intent.putStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_NAMES, appConfig.getAppNamesList());

        // Propagamos el rol seleccionado.
        String role = this.selectedRole != null ? this.selectedRole.value : null;
        intent.putExtra(ConfigurationConstants.EXTRA_RESOURCE_ROLE_SELECTED, role);

        // Almacenamos la lista de aplicaciones, con su correspondiente numero asociado al picker
        ConfigureFilterDialogBuilder.updateApps(appConfig.getAppNamesList());

        // Cargamos la nueva actividad.
        startActivity(intent);

        // Finalizamos esta actividad, ya que no podemos volver a ella una vez seleccionado el rol.
        // Para cambiar de rol, hay que deslogearse y volver a entrar al sistema.
        finish();
    }

    @Override
    public void configurationLoadError(Throwable t) {
        if (t == null) {
            // Error en la conexion
            showErrorDialog(getString(R.string.error_loading_app_configuration), getString(R.string.error_loading_app_configuration_title));
        } else {
            // Error en las credenciales
            PfLog.w(SFConstants.LOG_TAG, "Error durante el proceso de login", t);
            finish();
            showErrorDialog(getString(R.string.error_account_not_validated), getString(R.string.error_account_not_validated_title));
        }
    }

    /**
     * Muestra un mensaje de advertencia al usuario.
     *
     * @param message Mensaje que se desea mostrar.
     */
    private void showErrorDialog(final String message, final String title) {

        if (this.messageDialog == null) {
            this.messageDialog = new MessageDialog();
        }
        this.messageDialog.setMessage(message);
        this.messageDialog.setContext(this);
        if (title != null) {
            this.messageDialog.setTitle(title);
        }
    }

}
