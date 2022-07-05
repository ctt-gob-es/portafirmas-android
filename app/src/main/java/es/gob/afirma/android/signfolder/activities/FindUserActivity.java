package es.gob.afirma.android.signfolder.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

import es.gob.afirma.android.signfolder.MessageDialog;
import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.adapter.UserAdapter;
import es.gob.afirma.android.signfolder.proxy.RequestAppConfiguration;
import es.gob.afirma.android.signfolder.proxy.ServerControlledException;
import es.gob.afirma.android.signfolder.tasks.FindUserTask;
import es.gob.afirma.android.user.configuration.ConfigurationConstants;
import es.gob.afirma.android.user.configuration.ConfigurationRole;
import es.gob.afirma.android.user.configuration.GenericUser;
import es.gob.afirma.android.util.PfLog;

/**
 * Actividad con el diálogo de búsqueda de usuarios.
 */
public final class FindUserActivity extends FragmentActivity implements FindUserTask.FindUserListener {

    public static final int REQUEST_CODE = 42;
    public static final String EXTRA_USER = "user";
    public static final String EXTRA_ROLE = "role";

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

        // Establecemos una accion para el Enter del teclado del campo de busqueda
        // Obtenemos la lista de usuarios de protafirmas proxy.
        EditText searchEditText = this.findViewById(R.id.searchUserField);
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                searchUser(v);
                return true;
            }
        });
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
    public void searchUser(View view) {
        // Recuperamos la vista donde se mostrará la lista de usuarios.
        LinearLayout listUserLayout = this.findViewById(R.id.resultListId);
        // Eliminamos los posibles elementos que pudiese tener de búsquedas previas.
        listUserLayout.removeAllViewsInLayout();

        // Obtenemos la lista de usuarios de protafirmas proxy.
        EditText filter = this.findViewById(R.id.searchUserField);
        try {
            FindUserTask findUserTask = new FindUserTask(this.selectedRole, filter.getText().toString(), this);
            findUserTask.execute();
        }
        catch (Exception e) {
            PfLog.w(SFConstants.LOG_TAG, "No se ha podido realizar la busqueda de usuarios", e);
            showEmptyList(true);
        }
    }

    /**
     * Método que realiza la construcción del listado de usuarios.
     *
     * @param parentLayout Layout padre sobre el que aplicar los cambios.
     * @param userList     Lista de usuarios.
     */
    private void buildListItems(LinearLayout parentLayout, List<GenericUser> userList) {
        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setVerticalScrollBarEnabled(true);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        recyclerView.setLayoutParams(lp);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(layoutManager);

        RecyclerView.Adapter<?> mAdapter;

        if (userList != null) {
            mAdapter = new UserAdapter(userList, this.selectedRole, this.apps, this.selectUserListerner);
        } else {
            throw new IllegalArgumentException("Error. La lista de usuarios a mostrar no puede ser nula.");
        }
        recyclerView.setAdapter(mAdapter);

        parentLayout.addView(recyclerView);
    }

    private UserAdapter.SelectUserListener selectUserListerner = new UserAdapter.SelectUserListener() {
        @Override
        public void selectNewAuthorizedUser(GenericUser user) {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_ROLE, ConfigurationRole.AUTHORIZED);
            intent.putExtra(EXTRA_USER, user.toBundle());
            setResult(RESULT_OK, intent);
            FindUserActivity.this.finish();
        }

        @Override
        public void selectNewValidatorUser(GenericUser user) {
            final MessageDialog dialog = new MessageDialog();
            dialog.setMessage(getString(R.string.dialog_msg_confirm_new_valitor, user.getName()));
            dialog.setContext(FindUserActivity.this);
            dialog.setNeedShowNegativeButton(true);
            dialog.setListeners(
                    // Click en boton OK
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.putExtra(FindUserActivity.EXTRA_ROLE, ConfigurationRole.VERIFIER);
                            intent.putExtra(FindUserActivity.EXTRA_USER, user.toBundle());
                            setResult(RESULT_OK, intent);
                            FindUserActivity.this.finish();
                        }
                    },
                    // Click en boton cancelar
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }
            );

            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            dialog.show(FindUserActivity.this.getSupportFragmentManager(), "QuestionDialog"); //$NON-NLS-1$;
                        } catch (Exception e) {
                            PfLog.e(SFConstants.LOG_TAG, "No se ha podido mostrar la consulta al usuario", e); //$NON-NLS-1$
                        }
                    }
                });
            } catch (final Exception e2) {
                PfLog.e(SFConstants.LOG_TAG, "Error en el hilo que muestra la consulta al usuario", e2); //$NON-NLS-1$
            }
        }
    };

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

    @Override
    public void loadedUserList(List<GenericUser> users) {
        if (users != null && !users.isEmpty()) {
            // Recuperamos la vista donde se mostrará la lista de usuarios.
            LinearLayout listUserLayout = this.findViewById(R.id.resultListId);
            buildListItems(listUserLayout, users);
        } else {
            showEmptyList(false);
        }
    }

    @Override
    public void errorLoadingUserList() {
        showEmptyList(true);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
