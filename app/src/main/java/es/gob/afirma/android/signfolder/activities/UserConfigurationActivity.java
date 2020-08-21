package es.gob.afirma.android.signfolder.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.gob.afirma.android.signfolder.CryptoConfiguration;
import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.adapter.AuthorizerAdapter;
import es.gob.afirma.android.signfolder.adapter.VerifierAdapter;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.RequestAppConfiguration;
import es.gob.afirma.android.signfolder.proxy.ServerControlledException;
import es.gob.afirma.android.signfolder.tasks.LogoutRequestTask;
import es.gob.afirma.android.signfolder.tasks.OpenHelpDocumentTask;
import es.gob.afirma.android.user.configuration.AuthorizedUser;
import es.gob.afirma.android.user.configuration.ConfigurationConstants;
import es.gob.afirma.android.user.configuration.ConfigurationRole;
import es.gob.afirma.android.user.configuration.VerifierUser;
import es.gob.afirma.android.util.PfLog;

/**
 * Clase que gestiona la actividad de configuración de usuario.
 */
public final class UserConfigurationActivity extends Activity {

    /**
     * Attributo que representa el conjunto de aplicaciones configuradas en portafirmas.
     */
    RequestAppConfiguration apps = new RequestAppConfiguration();

    /**
     * Atributo que representa el actual rol seleccionado en la vista de configuración.
     */
    private ConfigurationRole currentRoleSelected = ConfigurationRole.AUTHORIZED;

    /**
     * Atributo que representa las pestañas principales de selección de roles.
     */
    private TabHost tabHost;

    /**
     * Metodo para crear la pestana del tab customizada.
     *
     * @param context Contexto sobre el que construir la pestaña.
     * @param text    Texto de la pestaña.
     * @return una nueva vista que representa la pestaña creada.
     */
    private static View createTabView(final Context context, final String text) {
        @SuppressLint("InflateParams") final View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
        final TextView tv = view.findViewById(R.id.tabsText);
        tv.setText(text);
        return view;
    }

    /**
     * Método get del atributo <i>mainTab</i>.
     *
     * @return el valor del atributo.
     */
    public TabHost getTabHost() {
        return tabHost;
    }

    /**
     * Método set del atributo <i>mainTab</i>.
     *
     * @param tabHost nuevo valor del atributo.
     */
    public void setTabHost(TabHost tabHost) {
        this.tabHost = tabHost;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Recuperamos el intent.
        Intent intent = getIntent();

        // Restauramos el valor por defecto del código de resultado de la actividad.
        setResult(ConfigurationConstants.ACTIVITY_RESULT_CODE_NONE);

        // Recuperamos el rol seleccionado por el usuario durante la autenticación.
        String selectedRoleAsString = intent.
                getStringExtra(ConfigurationConstants.EXTRA_RESOURCE_ROLE_SELECTED);
        if (selectedRoleAsString != null) {
            setResult(ConfigurationConstants.ACTIVITY_RESULT_CODE_ACCESS_DENEGATED);
            finish();
        } else {
            // Recuperamos las aplicaciones.
            if (intent.getStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_IDS) != null) {
                apps.setAppIdsList(intent.getStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_IDS));
            }
            if (intent.getStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_NAMES) != null) {
                apps.setAppNamesList(intent.getStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_IDS));
            }

            // Mostramos la vista.
            setContentView(R.layout.activity_user_configuration);

            // Creamos la estructura de pestañas.
            @SuppressLint("InflateParams") View authTab = getLayoutInflater().inflate(R.layout.roles_tab_view, null);
            ((ScrollView) this.findViewById(R.id.tabContentViewId)).addView(authTab);

            setTabHost((TabHost) findViewById(android.R.id.tabhost));
            getTabHost().setup();
            getTabHost().getTabWidget().setDividerDrawable(R.drawable.tab_divider);

            buildMainTabs();

            getTabHost().setCurrentTab(0);

            // Cargamos la lista de roles disponibles.
            loadRoles(ConfigurationRole.AUTHORIZED);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(
                R.menu.activity_configuration_options_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Actualizar listado actual
        if (item.getItemId() == R.id.update) {
            loadRoles(this.currentRoleSelected);
            Toast.makeText(this, R.string.toast_msg_list_updated, Toast.LENGTH_LONG).show();
        }         // Abrir ayuda
        else if (item.getItemId() == R.id.help) {
            boolean storagePerm = (
                    ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
            );

            if (storagePerm) {
                openHelp();
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PetitionDetailsActivity.PERMISSION_TO_OPEN_HELP);
            }
        }
        // Cerrar sesion
        else if (item.getItemId() == R.id.logout) {
            showConfirmExitDialog();
        }
        return true;
    }

    /**
     * Muestra un mensaje al usuario pidiendo confirmacion para cerrar la
     * sesi&oacute;n del usuario.
     */
    private void showConfirmExitDialog() {

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UserConfigurationActivity.this);
        alertDialogBuilder.setTitle(R.string.dialog_title_close_session);

        final LayoutInflater layoutInflater = LayoutInflater.from(UserConfigurationActivity.this);
        final View view = layoutInflater.inflate(R.layout.dialog_info, null);
        ((TextView) view.findViewById(R.id.infoTextId)).setText(R.string.dialog_msg_close_session);
        ((TextView) view.findViewById(R.id.infoTextId)).setTextSize(18);

        alertDialogBuilder.setView(view);
        alertDialogBuilder.setNegativeButton(UserConfigurationActivity.this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.dismiss();
            }
        });
        alertDialogBuilder.setPositiveButton(UserConfigurationActivity.this.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int id) {
                CryptoConfiguration.setCertificateAlias(null);
                CryptoConfiguration.setCertificatePrivateKeyEntry(null);
                try {
                    LogoutRequestTask lrt = new LogoutRequestTask(CommManager.getInstance());
                    lrt.execute();
                } catch (Exception e) {
                    PfLog.e(SFConstants.LOG_TAG,
                            "No se ha podido cerrar sesion: " + e); //$NON-NLS-1$
                }
                setResult(PetitionDetailsActivity.RESULT_SESSION_CLOSED);
                finish();
            }
        });
        alertDialogBuilder.create();
        alertDialogBuilder.show();
    }

    /**
     * Abre el fichero de ayuda de la aplicaci&oacute;n.
     */
    private void openHelp() {
        OpenHelpDocumentTask task = new OpenHelpDocumentTask(this);
        task.execute();
    }

    /**
     * Método onActivityResult de la actividad.
     * Este método es llamado cuando cuando la actividad hija finaliza.
     *
     * @param requestCode Código identificador de la petición proporcionado
     *                    originalmente durante el inicio de la actividad hija.
     * @param resultCode  Código de la respuesta devuelta por la actividad hija.
     * @param data        Intent devuelto como resultado.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ConfigurationConstants.ACTIVITY_REQUEST_CODE_AUTH_CREATION) {
            if (resultCode == ConfigurationConstants.ACTIVITY_RESULT_CODE_AUTH_ROLE_OK) {
                Toast toast = Toast.makeText(this, R.string.toast_msg_creation_auth_role_ok, Toast.LENGTH_LONG);
                toast.getView().setBackgroundColor(getResources().getColor(R.color.lightGreen));
                toast.show();
            } else if (resultCode == ConfigurationConstants.ACTIVITY_RESULT_CODE_AUTH_ROLE_KO) {
                Toast toast = Toast.makeText(this, R.string.toast_msg_creation_auth_role_ko, Toast.LENGTH_LONG);
                toast.getView().setBackgroundColor(getResources().getColor(R.color.lightRed));
                toast.show();
            }
            loadRoles(currentRoleSelected);
        } else if (requestCode == ConfigurationConstants.ACTIVITY_REQUEST_CODE_VERIFIER_CREATION) {
            if (resultCode == ConfigurationConstants.ACTIVITY_RESULT_CODE_VERIFIER_ROLE_OK) {
                Toast toast = Toast.makeText(this, R.string.toast_msg_creation_verifier_role_ok, Toast.LENGTH_LONG);
                toast.getView().setBackgroundColor(getResources().getColor(R.color.lightGreen));
                toast.show();
            } else if (resultCode == ConfigurationConstants.ACTIVITY_RESULT_CODE_VERIFIER_ROLE_KO) {
                Toast toast = Toast.makeText(this, R.string.toast_msg_creation_verifier_role_ko, Toast.LENGTH_LONG);
                toast.getView().setBackgroundColor(getResources().getColor(R.color.lightRed));
                toast.show();
            }
            loadRoles(currentRoleSelected);
        }
    }

    /**
     * Listener method that manages the add new role button action.
     *
     * @param view Configuration view.
     */
    public void addRole(View view) {
        Intent intent = new Intent(this, CreateNewRoleActivity.class);
        intent.putExtra(ConfigurationConstants.EXTRA_RESOURCE_ROLE_SELECTED, this.currentRoleSelected == null ? null : this.currentRoleSelected.name());
        if (ConfigurationRole.AUTHORIZED.equals(this.currentRoleSelected)) {
            startActivityForResult(intent, ConfigurationConstants.ACTIVITY_REQUEST_CODE_AUTH_CREATION);
        } else if (ConfigurationRole.VERIFIER.equals(this.currentRoleSelected)) {
            intent.putStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_IDS, apps.getAppIdsList());
            intent.putStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_NAMES, apps.getAppNamesList());
            startActivityForResult(intent, ConfigurationConstants.ACTIVITY_REQUEST_CODE_VERIFIER_CREATION);
        }
    }

    /**
     * Method that loads the list of users of the selected role.
     *
     * @param role Role to load.
     */
    @SuppressWarnings("unchecked")
    private void loadRoles(ConfigurationRole role) {
        this.currentRoleSelected = role;
        // Recuperamos la lista de usuarios con dicho rol.
        List<?> userList;
        try {
            userList = getListUserByRole(role);
            // Si el rol es de tipo autorizados...
            if (userList.size() > 0 && ConfigurationRole.AUTHORIZED.equals(role)) {
                List<AuthorizedUser> authList = (List<AuthorizedUser>) userList;
                // Mostramos la lista de autorizados.
                showAuthorized(authList);
            }
            // Si el rol es de tipo validadores...
            else if (userList.size() > 0 && ConfigurationRole.VERIFIER.equals(role)) {
                List<VerifierUser> verifierList = (List<VerifierUser>) userList;
                // Mostramos la lista de validadores.
                showVerifiers(verifierList);
            }
            // Si la lista está vacía o es de un tipo no reconocido...
            else {
                // Generamos una respuesta genérica indicando que no se ha
                // encontrado ningún usuario para el rol indicado.
                setViewEmptyTable(role);
            }
        } catch (Exception e) {
            setViewErrorGettingUsers();
        }
    }

    /**
     * Método que muestra la lista de usuarios con el rol de autorizados.
     *
     * @param authList Lista de autorizados.
     */
    private void showAuthorized(List<AuthorizedUser> authList) {
        // Recuperamos la vista que contendrá toda la información a mostrar.
        LinearLayout parentLayout = findViewById(R.id.authData);

        // Limpiamos la vista.
        cleanDataListView(ConfigurationRole.AUTHORIZED);

        // Si hay datos a añadir en la vista...
        if (authList != null && authList.size() > 0) {
            buildListItems(parentLayout, null, authList);
        } else {
            // Si no hay elementos, mostramos mensaje de lista vacía.
            // Creamos la vista que representa el texto a mostrar.
            buildEmptyListView(parentLayout);
        }
    }

    /**
     * Método que muestra la lista de usuarios con el rol de validador.
     *
     * @param verifierList Lista de validadores.
     */
    private void showVerifiers(List<VerifierUser> verifierList) {
        // Recuperamos la vista que contedrá toda la información a mostrar.
        LinearLayout parentLayout = findViewById(R.id.verifierData);

        // Limpiamos la vista.
        cleanDataListView(ConfigurationRole.VERIFIER);

        // Si hay datos a añadir en la vista...
        if (verifierList != null && verifierList.size() > 0) {
            buildListItems(parentLayout, verifierList, null);
        } else {
            // Si no hay elementos, mostramos mensaje de lista vacía.
            // Creamos la vista que representa el texto a mostrar.
            buildEmptyListView(parentLayout);
        }
    }

    /**
     * Método que genera la vista de "lista vacía" para el listado de roles.
     *
     * @param parentLayout Layout padre sobre el que aplicar los cambios.
     */
    private void buildEmptyListView(LinearLayout parentLayout) {
        TextView textView = new TextView(this, null, R.style.PFEmptyTableMessage);
        textView.setText(R.string.error_msg_verifier_list_empty);

        // Añadimos el estilo a la vista que contendrá el texto.
        parentLayout.setGravity(Gravity.CENTER);
        parentLayout.setPadding(1, 1, 1, 1);

        // Añadimos la vista de texto a la vista que lo contendrá.
        parentLayout.addView(textView);
    }

    /**
     * Método que realiza la construcción del listado de roles.
     *
     * @param parentLayout Layout padre sobre el que aplicar los cambios.
     * @param verifierList Lista de validadores.
     * @param authList     Lista de autorizados.
     */
    private void buildListItems(LinearLayout parentLayout, List<VerifierUser> verifierList, List<AuthorizedUser> authList) {
        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setVerticalScrollBarEnabled(true);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        recyclerView.setLayoutParams(lp);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(layoutManager);

        RecyclerView.Adapter<?> mAdapter;

        if (verifierList != null) {
            mAdapter = new VerifierAdapter(verifierList);
        } else if (authList != null) {
            mAdapter = new AuthorizerAdapter(authList);
        } else {
            throw new IllegalArgumentException("Error. Todas las listas de roles a mostrar no pueden ser nulas.");
        }
        recyclerView.setAdapter(mAdapter);

        parentLayout.addView(recyclerView);
    }

    /**
     * Método que recupera la lista de usuarios para un determinado rol de portafirma-proxy.
     *
     * @param role Rol seleccionado.
     * @return la lista de usuarios asociados al rol proporcionado.
     * @throws ServerControlledException si algo falla.
     * @throws SAXException              si algo falla.
     * @throws IOException               si algo falla.
     */
    private List<?> getListUserByRole(ConfigurationRole role) throws ServerControlledException, SAXException, IOException {
        List<?> proxyResult = CommManager.getInstance().getListUserByRole(role, 1, 10);
        if (role.equals(ConfigurationRole.AUTHORIZED)) {
            return proxyResult;
        } else if (role.equals(ConfigurationRole.VERIFIER)) {
            return proxyResult;
        }
        return new ArrayList<>();
    }

    /**
     * Método que actualiza la vista actual donde se muestra la lista de roles y
     * muestra un mensaje de tabla vacía.
     *
     * @param role Pestaña seleccionada.
     */
    private void setViewEmptyTable(ConfigurationRole role) {
        View viewById;
        if (!ConfigurationRole.AUTHORIZED.equals(role)) {
            viewById = findViewById(R.id.verifierData);
        } else {
            // recuperamos el elemento  a actualizar.
            viewById = findViewById(R.id.authData);
        }
        LinearLayout dataView = (LinearLayout) viewById;

        // Eliminados el contenido que pueda tener.
        cleanDataListView(role);

        // Creamos la vista que representa el texto a mostrar.
        TextView textView = new TextView(this, null, R.style.PFEmptyTableMessage);
        textView.setText(R.string.no_user_available);

        // Añadimos el estilo a la vista que contendrá el texto.
        dataView.setPadding(10, 60, 10, 30);

        // Añadimos la vista de texto a la vista que lo contendrá.
        dataView.addView(textView);
    }

    /**
     * Método que elimina todos las vistas que contiene el elemento principal
     * donde se muestra la lista de roles.
     *
     * @param role Pestaña seleccionada.
     */
    private void cleanDataListView(ConfigurationRole role) {
        if (ConfigurationRole.AUTHORIZED.equals(role)) {
            LinearLayout dataView = findViewById(R.id.authData);
            dataView.removeAllViewsInLayout();
        } else if (ConfigurationRole.VERIFIER.equals(role)) {
            LinearLayout dataView = findViewById(R.id.verifierData);
            dataView.removeAllViewsInLayout();
        } else {
            LinearLayout dataView = findViewById(R.id.authData);
            dataView.removeAllViewsInLayout();
            dataView = findViewById(R.id.verifierData);
            dataView.removeAllViewsInLayout();
        }
    }

    /**
     * Método que modifica la vista actual y muestra un mensaje de error.
     */
    public void setViewErrorGettingUsers() {
        // recuperamos el elemento  a actualizar.
        View viewById;
        if(this.currentRoleSelected.equals(ConfigurationRole.AUTHORIZED)) {
            viewById = findViewById(R.id.authData);
        } else {
            viewById = findViewById(R.id.verifierData);
        }
        LinearLayout dataView = (LinearLayout) viewById;

        // Eliminados el contenido que pueda tener.
        dataView.removeAllViewsInLayout();

        // Creamos la vista que representa el texto a mostrar.
        TextView textView = new TextView(this, null, R.style.PFEmptyTableMessage);
        textView.setText(R.string.error_msg_get_users_by_rol);

        // Añadimos el estilo a la vista que contendrá el texto.
        dataView.setGravity(Gravity.CENTER);
        dataView.setPadding(1, 1, 1, 1);

        // Añadimos la vista de texto a la vista que lo contendrá.
        dataView.addView(textView);
    }

    /**
     * Método que construye la estructura principal de pestañas de roles.
     */
    private void buildMainTabs() {
        final View authTabview = createTabView(getTabHost().getContext(), this.getResources().getString(R.string.authorizedBtn));
        getTabHost().addTab(getTabHost().newTabSpec(String.valueOf(R.id.authData)).setIndicator(authTabview).setContent(R.id.authData));
        final View verifierTabView = createTabView(getTabHost().getContext(), this.getResources().getString(R.string.verifiersBtn));
        getTabHost().addTab(getTabHost().newTabSpec(String.valueOf(R.id.verifierData)).setIndicator(verifierTabView).setContent(R.id.verifierData));

        getTabHost().setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if (tabId.equals(String.valueOf(R.id.authData))) {
                    loadRoles(ConfigurationRole.AUTHORIZED);
                } else {
                    loadRoles(ConfigurationRole.VERIFIER);
                }
            }
        });
    }

}
