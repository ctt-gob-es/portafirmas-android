package es.gob.afirma.android.signfolder.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.adapter.UserAdapter;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.RequestAppConfiguration;
import es.gob.afirma.android.signfolder.proxy.ServerControlledException;
import es.gob.afirma.android.user.configuration.ConfigurationConstants;
import es.gob.afirma.android.user.configuration.ConfigurationRole;
import es.gob.afirma.android.user.configuration.UserConfiguration;

/**
 * Clase que gestiona la actividad encargada de crear nuevos roles.
 */
public final class CreateNewRoleActivity extends Activity {

    /**
     * Attributo que representa el conjunto de aplicaciones configuradas en portafirmas.
     */
    RequestAppConfiguration apps = null;

    /**
     * Atributo que representa el rol seleccionado en el proceso de creación.
     */
    private ConfigurationRole selectedRole = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        // Recuperamos el rol seleccionado.
        if (intent.getStringExtra(ConfigurationConstants.EXTRA_RESOURCE_ROLE_SELECTED) == null) {
            selectedRole = ConfigurationRole.AUTHORIZED;
        } else {
            ConfigurationRole selectedValueProp = ConfigurationRole.getValue(intent.getStringExtra(ConfigurationConstants.EXTRA_RESOURCE_ROLE_SELECTED));
            if (selectedValueProp == null) {
                selectedRole = ConfigurationRole.AUTHORIZED;
            } else {
                selectedRole = selectedValueProp;
            }
        }

        // Recuperamos la lista de aplicaciones.
        if (intent.getStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_IDS) != null &&
                intent.getStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_NAMES) != null) {
            apps = new RequestAppConfiguration();
            apps.setAppIdsList(intent.getStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_IDS));
            apps.setAppNamesList(intent.getStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_NAMES));
        }

        // Cambiamos el titulo de la actividad.
        if (selectedRole.equals(ConfigurationRole.VERIFIER)) {
            setTitle(R.string.title_activity_create_verifier_role);
        } else {
            setTitle(R.string.title_activity_create_authorizer_role);
        }

        // Cambiamos el valor a devolver por la actividad al por defecto.
        setResult(ConfigurationConstants.ACTIVITY_RESULT_CODE_NONE);

        // Mostramos la vista.
        setContentView(R.layout.activity_create_role);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ConfigurationConstants.ACTIVITY_REQUEST_CODE_AUTH_CREATION ||
                requestCode == ConfigurationConstants.ACTIVITY_REQUEST_CODE_VERIFIER_CREATION) {
            setResult(resultCode);
            finish();
        }
    }

    /**
     * Listener que gestiona la acción del botón de búsueda de usuario.
     *
     * @param view Vista actual.
     */
    public void searchUser(View view) throws ServerControlledException, SAXException, IOException {
        // Recuperamos la vista donde se mostrará la lista de usuarios.
        LinearLayout listUserLayout = this.findViewById(R.id.resultListId);
        // Eliminamos los posibles elementos que pudiese tener de búsquedas previas.
        listUserLayout.removeAllViewsInLayout();
        // Obtenemos la lista de usuarios de protafirmas proxy.
        EditText filter = this.findViewById(R.id.searchUserField);
        List<UserConfiguration> users = CommManager.getInstance().getListUser(1, 10, filter.getText().toString());
        if (users != null) {
            if (!users.isEmpty()) {
                buildListItems(listUserLayout, users);
            } else {
                showEmptyList(false);
            }
        } else {
            showEmptyList(true);
        }
    }

    /**
     * Método que realiza la construcción del listado de usuarios.
     *
     * @param parentLayout Layout padre sobre el que aplicar los cambios.
     * @param userList     Lista de usuarios.
     */
    private void buildListItems(LinearLayout parentLayout, List<UserConfiguration> userList) {
        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setVerticalScrollBarEnabled(true);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        recyclerView.setLayoutParams(lp);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(layoutManager);

        RecyclerView.Adapter<?> mAdapter;

        if (userList != null) {
            mAdapter = new UserAdapter(userList, this.selectedRole, this.apps);
        } else {
            throw new IllegalArgumentException("Error. La lista de usuarios a mostrar no puede ser nula.");
        }
        recyclerView.setAdapter(mAdapter);

        parentLayout.addView(recyclerView);
    }

    /**
     * Método que configura la vista actual de búsqueda para mostrar un mensaje de lista vacía.
     */
    private void showEmptyList(boolean error) {
        LinearLayout listUserLayout = this.findViewById(R.id.resultListId);
        listUserLayout.removeAllViewsInLayout();
        TextView textView = new TextView(this, null, R.style.PFEmptyTableMessage);
        if (!error) {
            textView.setText(R.string.empty_user_list_msg);
        } else {
            textView.setText(R.string.error_user_list_msg);
        }
        textView.setPadding(10, 60, 10, 30);
        listUserLayout.addView(textView);
    }

}
