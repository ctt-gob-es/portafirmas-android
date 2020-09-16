package es.gob.afirma.android.signfolder.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.user.configuration.ConfigurationConstants;
import es.gob.afirma.android.user.configuration.ConfigurationRole;

/**
 * Clase que gestiona la actividad asociada al logeado con roles.
 */
public class LoginWithRoleActivity extends Activity {

    private Intent intent;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Recuperamos los parámetros de la validación de login.
        intent = getIntent();

        // Comprobamos los roles a mostrar.
        List<String> roles = intent.getStringArrayListExtra(ConfigurationConstants.VALIDATION_RESULT_ROLES);
        List<ConfigurationRole> roleLs = new ArrayList<>();

        for (String role : roles) {
            ConfigurationRole r = ConfigurationRole.getValue(role);
            if (r != null) {
                roleLs.add(r);
            }
        }

        // Cargamos la vista.
        setContentView(R.layout.activity_choose_role);

        // Recuperamos los elementos de la vista a modificar.
        ProgressBar pb = findViewById(R.id.progressBarId);
        pb.setVisibility(View.INVISIBLE);
        Button verifierBtn = findViewById(R.id.button_access_verifier);
        Button authBtn = findViewById(R.id.button_access_auth);
        LinearLayout btnLayout = findViewById(R.id.ChooseRoleButtonsId);

        // Si no existe un determinado role, eliminamos el botón asociado.
        if (!roleLs.contains(ConfigurationRole.VERIFIER)) {
            btnLayout.removeView(verifierBtn);
        }
        if (!roleLs.contains(ConfigurationRole.AUTHORIZED)) {
            btnLayout.removeView(authBtn);
        }
    }

    /**
     * Accede a la aplicación con el rol de autorizado.
     *
     * @param view vista actual.
     */
    public void accessAsAuthorized(final View view) {
        showProgressBar();
        startListPetitionsActivity(ConfigurationRole.AUTHORIZED);
    }

    /**
     * Accede a la aplicación con el rol de validador.
     *
     * @param view vista actual.
     */
    public void accessAsVerifier(final View view) {
        showProgressBar();
        startListPetitionsActivity(ConfigurationRole.VERIFIER);
    }

    /**
     * Accede a la aplicación con el rol de firmante.
     *
     * @param view vista actual.
     */
    public void accessAsSigner(final View view) {
        showProgressBar();
        startListPetitionsActivity(null);
    }

    /**
     * Método que inicia la actividad principal de lista de peticiones con el rol seleccionado.
     *
     * @param role Rol seleccionado.
     */
    private void startListPetitionsActivity(ConfigurationRole role) {
        intent.putExtra(ConfigurationConstants.EXTRA_RESOURCE_ROLE_SELECTED, role != null ? role.value : null);
        intent.setClass(this, PetitionListActivity.class);
        startActivity(intent);
        finish();
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

}
