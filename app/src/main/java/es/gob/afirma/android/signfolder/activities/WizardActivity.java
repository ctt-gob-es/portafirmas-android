package es.gob.afirma.android.signfolder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import es.gob.afirma.android.signfolder.AppPreferences;
import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.util.PfLog;

/**
 * Pantalla del asistente de la aplicación.
 */
public class WizardActivity extends AppCompatActivity {

    /** C&oacute;digo de solicitud de la habilitaci&oacute;n del NFC del dispositivo. */
    private final static int REQUEST_CODE_ENABLE_NFC = 2002;   // The request code

    private int currentLayout = -1;

    private AppPreferences preferences = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (currentLayout == -1) {
            changeActivityLayout(R.layout.activity_wizard_signfolder);
        }

        // Ocultamos la opcion de firma ocn DNIe

        // Inicializamos las preferencias y establecemos los valores que deben existir de inicio
        if (preferences == null) {
            preferences = AppPreferences.getInstance();
            preferences.setDefaultServers();
        }
    }

    /**
     * Cambia el layout de la actividad y ejecuta las acciones que necesite el mismo.
     * @param activityLayout Identificador de recurso del layout a cargar.
     */
    private void changeActivityLayout(int activityLayout) {
        this.currentLayout = activityLayout;
        setContentView(this.currentLayout);
        TextView hyperlinkTv = findViewById(R.id.textViewHyperlink);
        if (hyperlinkTv != null) {
            hyperlinkTv.setMovementMethod(LinkMovementMethod.getInstance());
        }

        // La opcion de DNIe solo se muestra si el dispositivo cuenta con NFC
        View optionDnie = findViewById(R.id.optionDnie);

//TODO: Ocultamos la opcion de firma con DNIe por el momento
        if (optionDnie != null /*&& !NfcHelper.isNfcServiceAvailable(this)*/) {
            optionDnie.setVisibility(View.GONE);
        }
    }

    /**
     * Cierra el asistente.
     */
    private void finishWizard() {
        // Indicamos que el wizard ya se ha completado y no debe mostrarse en futuras ocasiones
        AppPreferences.getInstance().setFirstExecution(false);
        // Lanzamos la actividad de la pantalla principal
        Intent intent = new Intent(WizardActivity.this, LoginActivity.class);
        startActivity(intent);
        // Cerramos esta actividad
        finish();
    }

    /**
     * Acción a ejecutar cuando se selecciona el uso del portafirmas del la DTIC.
     * @param option Elemento sobre el que se ejecutó el evento onClick.
     */
    public void onClickSignfolderDtic(View option) {
        PfLog.i(SFConstants.LOG_TAG, "Se selecciona el uso del portafirmas de la DTIC");
        AppPreferences.getInstance().setSelectedProxy(
                AppPreferences.DEFAULT_PROXY_GOB_ALIAS, AppPreferences.DEFAULT_PROXY_GOB_URL);
        // Pasamos a la siguiente pantalla del asistente
        changeActivityLayout(R.layout.activity_wizard_certorigin);
    }

    /**
     * Acción a ejecutar cuando se selecciona el uso del portafirmas de RedSara.
     * @param option Elemento sobre el que se ejecutó el evento onClick.
     */
    public void onClickSignfolderRedSara(View option) {
        PfLog.i(SFConstants.LOG_TAG, "Se selecciona el uso del portafirmas de RedSara");
        // Configuramos el Portafirmas de RedSara
        AppPreferences.getInstance().setSelectedProxy(
                AppPreferences.DEFAULT_PROXY_REDSARA_ALIAS, AppPreferences.DEFAULT_PROXY_REDSARA_URL);
        // Pasamos a la siguiente pantalla del asistente
        changeActivityLayout(R.layout.activity_wizard_certorigin);
    }

    /**
     * Acción a ejecutar cuando se selecciona el uso de un portafirmas ajeno.
     * @param option Elemento sobre el que se ejecutó el evento onClick.
     */
    public void onClickSignfolderOther(View option) {
        PfLog.i(SFConstants.LOG_TAG, "Se selecciona el uso de un Portafirmas de un tercero");
        // No hacemos nada mas que pasar a la siguiente pantalla del asistente
        changeActivityLayout(R.layout.activity_wizard_certorigin);
    }

    /**
     * Acción a ejecutar cuando se selecciona el uso de certificado local.
     * @param option Elemento sobre el que se ejecutó el evento onClick.
     */
    public void onClickCertoriginLocal(View option) {
        PfLog.i(SFConstants.LOG_TAG, "Se selecciona el uso de certificado local");
        // Desactivamos el uso de certificado local
        AppPreferences.getInstance().setCertKeyStore(AppPreferences.KEYSTORE_LOCAL);
        // Finalizamos el asistente
        finishWizard();
    }

    /**
     * Acción a ejecutar cuando se selecciona el uso de certificado remoto.
     * @param option Elemento sobre el que se ejecutó el evento onClick.
     */
    public void onClickCertoriginCloud(View option) {
        PfLog.i(SFConstants.LOG_TAG, "Se selecciona el uso de certificado remoto");
        // Activamos el uso de certificado remoto
        AppPreferences.getInstance().setCertKeyStore(AppPreferences.KEYSTORE_CLOUD);
        // Finalizamos el asistente
        finishWizard();
    }

    /**
     * Acción a ejecutar cuando se selecciona el uso de certificado remoto.
     * @param option Elemento sobre el que se ejecutó el evento onClick.
     */
    public void onClickCertoriginDnie(View option) {
        PfLog.i(SFConstants.LOG_TAG, "Se selecciona el uso de DNIe");
        // Activamos el uso de DNIe
        AppPreferences.getInstance().setCertKeyStore(AppPreferences.KEYSTORE_DNIE);
        // Finalizamos el asistente
        finishWizard();
    }

    /**
     * Acción a ejecutar cuando se pulsa sobre el botón Omitir para saltarse el asistente.
     * @param option Elemento sobre el que se ejecutó el evento onClick.
     */
    public void onClickOmitButton(View option) {
        PfLog.i(SFConstants.LOG_TAG, "Se omite la comfiguracion mediante el asistente");
        // Finalizamos el asistente
        finishWizard();
    }

    @Override
    public void onBackPressed() {
        if (currentLayout == R.layout.activity_wizard_signfolder) {
            finish();
        } else if (currentLayout == R.layout.activity_wizard_certorigin) {
            changeActivityLayout(R.layout.activity_wizard_signfolder);
        }
        else {
            super.onBackPressed();
        }
    }
}
