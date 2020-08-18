package es.gob.afirma.android.signfolder.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.adapter.AppAdapter;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.RequestAppConfiguration;
import es.gob.afirma.android.user.configuration.ConfigurationConstants;
import es.gob.afirma.android.user.configuration.ConfigurationRole;
import es.gob.afirma.android.user.configuration.UserConfiguration;

/**
 * Clase que gestiona la actividad de creación de nuevos validadores.
 */
public class CreateNewVerifierActivity extends Activity {

    /**
     * Atributo que representa el usuario seleccionado para la creación del rol.
     */
    private UserConfiguration user;

    /**
     * Atributo que representa la lista de aplicaciones.
     */
    private RequestAppConfiguration apps = new RequestAppConfiguration();

    /**
     * Atributo que representa la lista de los identificadores de las aplicaciones seleccionadas.
     */
    private List<String> appsSelected = new ArrayList<>();

    /**
     * Atributo que indica si se ha marcado la opción de seleccionar todas las aplicaciones
     * <i>True</i> o no <i>False</i>.
     */
    private boolean allSelected = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Recuperamos el intent.
        Intent intent = getIntent();

        // Recuperamos la información del usuario seleccionado.
        String[] userParams = intent.getStringArrayExtra(ConfigurationConstants.EXTRA_RESOURCE_USER_INFO);
        if (userParams == null || userParams.length != 4) {
            throw new IllegalArgumentException("No ha sido posible recuperar el usuario seleccionado previamente.");
        }
        user = new UserConfiguration();
        user.setID(userParams[0]);
        user.setName(userParams[1]);
        user.setSurname(userParams[2]);
        user.setSecondSurname(userParams[3]);

        // Recuperamos la lista de aplicaciones.
        if (intent.getStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_IDS) != null) {
            apps.setAppIdsList(intent.getStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_IDS));
        }
        if (intent.getStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_NAMES) != null) {
            apps.setAppNamesList(intent.getStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_NAMES));
        }

        // Eliminamos la aplicación sin nombre de la lista de aplicaciones.
        removeNoNameApp();

        // Restauramos el valor por defecto del resultado a devolver por la actividad.
        setResult(ConfigurationConstants.ACTIVITY_RESULT_CODE_NONE);

        // Mostramos la vista.
        setContentView(R.layout.activity_create_new_verifier);

        // Mostramos el usuario seleccionado.
        String userRepresentation = CreateNewAuthorizedActivity.buildUserRepresentation(user);
        ((TextView) findViewById(R.id.nameFieldValueId)).setText(userRepresentation);
        if(user.getID() != null){
            ((TextView) findViewById(R.id.identifierFieldValueId)).setText(user.getID());
        } else {
            ((TextView) findViewById(R.id.identifierFieldValueId)).setText(R.string.empty_field_value);
        }

        // Configuramos el comportamiento de los botones de la actividad.
        setupButtons();

        // Configuramos el comportamiento del checkbox de aplicaciones.
        setupCheckbox();
    }

    /**
     * Método que configura el comportamiento de los botones de la vista.
     */
    private void setupButtons() {
        this.findViewById(R.id.finishButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean opResult;
                try {
                    opResult = CommManager.getInstance().createNewRole(user, ConfigurationRole.VERIFIER, null, appsSelected);
                } catch (Exception e) {
                    Log.e("CreateRoleError", "Se ha producido un error durante la creación del validador", e);
                    opResult = false;
                }
                if (opResult) {
                    setResult(ConfigurationConstants.ACTIVITY_RESULT_CODE_VERIFIER_ROLE_OK);
                } else {
                    setResult(ConfigurationConstants.ACTIVITY_RESULT_CODE_VERIFIER_ROLE_KO);
                }
                finish();
            }
        });
    }

    /**
     * Método que configura el comportamiento del checkbox de habilitación de aplicaciones.
     */
    private void setupCheckbox() {
        CheckBox cb = findViewById(R.id.cb_enable_applications);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    CreateNewVerifierActivity.this.findViewById(R.id.headerLayoutId).setVisibility(View.VISIBLE);
                    buildListAppsView();
                } else {
                    CreateNewVerifierActivity.this.findViewById(R.id.headerLayoutId).setVisibility(View.GONE);
                    removeListAppView();
                }
            }
        });
    }

    /**
     * Método que realiza la construcción del listado de aplicaciones.
     */
    private void buildListAppsView() {
        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setVerticalScrollBarEnabled(true);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        recyclerView.setLayoutParams(lp);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(layoutManager);

        final RecyclerView.Adapter<?> mAdapter;

        if (apps != null && apps.getAppIdsList() != null) {
            mAdapter = new AppAdapter(apps, appsSelected, new AppAdapter.OnItemCheckListener() {

                @Override
                public void onItemCheck(String appId) {
                    appsSelected.add(appId);
                }

                @Override
                public void onItemUncheck(String appId) {
                    appsSelected.remove(appId);
                }
            });
        } else {
            throw new IllegalArgumentException("Error. La lista de aplicaciones a mostrar no puede ser nula.");
        }
        recyclerView.setAdapter(mAdapter);

        ((LinearLayout) findViewById(R.id.resultListId)).addView(recyclerView);
    }

    /**
     * Método que marca/desmarca todas las aplicaciones del listado de aplicaciones.
     *
     * @param view Vista seleccionada.
     */
    public void onClickSelectAll(final View view) {
        if (allSelected) {
            removeListAppView();
            appsSelected.removeAll(apps.getAppIdsList());
            buildListAppsView();
            allSelected = false;
        } else {
            removeListAppView();
            appsSelected.addAll(apps.getAppIdsList());
            buildListAppsView();
            allSelected = true;
        }
    }

    /**
     * Método que elimina de la vista de creación de validadores la lista de aplicaciones a seleccionar.
     */
    private void removeListAppView() {
        LinearLayout listView = this.findViewById(R.id.resultListId);
        listView.removeViewAt(1);
        this.appsSelected = new ArrayList<>();
    }

    /**
     * Método auxiliar que elimina la aplicación sin id (la cual representa TODAS las apps)
     * de la lista de aplicaciones.
     */
    private void removeNoNameApp() {
        if (apps.getAppIdsList().contains("")) {
            int indexElem = apps.getAppIdsList().indexOf("");
            apps.getAppIdsList().remove(indexElem);
            apps.getAppNamesList().remove(indexElem);
        }
    }
}
