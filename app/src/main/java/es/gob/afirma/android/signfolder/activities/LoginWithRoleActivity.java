package es.gob.afirma.android.signfolder.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.adapter.RoleAdapter;
import es.gob.afirma.android.user.configuration.ConfigurationConstants;
import es.gob.afirma.android.user.configuration.RoleInfo;
import es.gob.afirma.android.user.configuration.UserConfig;

/**
 * Clase que gestiona la actividad asociada al logeado con roles.
 */
public class LoginWithRoleActivity extends Activity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Recuperamos los parámetros de la validación de login.
        Intent intent = getIntent();

        // Cargamos la vista a mostrar.
        setContentView(R.layout.activity_choose_role);

        // Cargamos la configuración de usuario.
        UserConfig userConfig = (UserConfig) intent.getSerializableExtra(ConfigurationConstants.EXTRA_RESOURCE_USER_CONFIG);

        // Recuperamos si es necesario limpiar la pila de actividades.
        boolean cleanStack = intent.getBooleanExtra(ConfigurationConstants.EXTRA_RESOURCE_CLEAN_STACK, false);

        // Mostramos la lista de roles.
        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setVerticalScrollBarEnabled(true);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        recyclerView.setLayoutParams(lp);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(layoutManager);
        orderRoles(userConfig);
        addSignerRole(userConfig);
        RoleAdapter adapter = new RoleAdapter(userConfig, intent, cleanStack);
        recyclerView.setAdapter(adapter);
        ((LinearLayout) findViewById(R.id.rolesView)).addView(recyclerView);
    }

    /**
     * Método que agrupa los roles de la lista de roles según su tipo (validador o autorizado).
     *
     * @param userConfig Configuración de usuario con la lista de roles a actualizar.
     */
    private void orderRoles(UserConfig userConfig) {
        List<RoleInfo> verifiers = new ArrayList<>();
        List<RoleInfo> authorized = new ArrayList<>();

        for (RoleInfo role : userConfig.getRoles()) {
            if (role != null && role.getRoleId().equalsIgnoreCase("VALIDADOR")) {
                verifiers.add(role);
            } else if (role != null && role.getRoleId().equalsIgnoreCase("AUTORIZADO")) {
                authorized.add(role);
            }
        }

        List<RoleInfo> res = new ArrayList<>();
        res.addAll(verifiers);
        res.addAll(authorized);

        userConfig.setRoles(res);
    }

    /**
     * Método encargado de comprobar si existe ya el rol de firmante en la lista de roles y lo añade si es necesario.
     *
     * @param userConfig Configuración de usuario actual.
     */
    private void addSignerRole(UserConfig userConfig) {
        boolean found = false;
        for (RoleInfo role : userConfig.getRoles()) {
            if (role.getRoleId().equalsIgnoreCase("FIRMANTE")) {
                found = true;
                break;
            }
        }
        if (!found) {
            RoleInfo signer = new RoleInfo("FIRMANTE", "FIRMANTE", null, null);
            userConfig.getRoles().add(0, signer);
        }
    }
}
