package es.gob.afirma.android.signfolder.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
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

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import es.gob.afirma.android.crypto.DnieConnectionManager;
import es.gob.afirma.android.signfolder.CryptoConfiguration;
import es.gob.afirma.android.signfolder.MessageDialog;
import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.SignfolderApp;
import es.gob.afirma.android.signfolder.adapter.AuthorizerAdapter;
import es.gob.afirma.android.signfolder.adapter.VerifierAdapter;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.GenericResponse;
import es.gob.afirma.android.signfolder.proxy.RequestAppConfiguration;
import es.gob.afirma.android.signfolder.tasks.CleanTempFilesTask;
import es.gob.afirma.android.signfolder.tasks.CreateVerifierTask;
import es.gob.afirma.android.signfolder.tasks.LoadDelegationsTask;
import es.gob.afirma.android.signfolder.tasks.LogoutRequestTask;
import es.gob.afirma.android.signfolder.tasks.OpenHelpDocumentTask;
import es.gob.afirma.android.signfolder.tasks.RemoveValidatorTask;
import es.gob.afirma.android.user.configuration.Authorization;
import es.gob.afirma.android.user.configuration.AuthorizationState;
import es.gob.afirma.android.user.configuration.ConfigurationConstants;
import es.gob.afirma.android.user.configuration.ConfigurationRole;
import es.gob.afirma.android.user.configuration.GenericUser;
import es.gob.afirma.android.user.configuration.RoleInfo;
import es.gob.afirma.android.user.configuration.Validator;
import es.gob.afirma.android.util.PfLog;

/**
 * Clase que gestiona la actividad de configuración de usuario.
 */
public final class UserConfigurationActivity extends FragmentActivity implements LoadDelegationsTask.LoadDelegationsListener {

    /** Codigo para la solicitud de datos a esta actividad. */
    static final int REQUEST_CODE = 5;

    /** Margen que aplicar a cada uno de los lados a la caja de texto en donde se mostraran los
     * mensajes cuando por algún motivo no haya elementos en un listado. */
    private static final int ALTERNATIVE_TEXT_MARGIN = 20;

    /** Attributo que representa el conjunto de aplicaciones configuradas en portafirmas. */
    RequestAppConfiguration apps = new RequestAppConfiguration();

    /** Atributo que representa el actual rol seleccionado en la vista de configuraci&oacute;n. */
    private ConfigurationRole currentRoleSelected = ConfigurationRole.AUTHORIZED;

    /** Atributo que representa las pestañas principales de selección de roles. */
    private TabHost tabHost;

    /** Tarea para la carga de los listados de autorizaciones y validadores. */
    private LoadDelegationsTask currentLoadDelegationsTask;

    /** Indica si al usuario ya se le ha avisado de que hay autorizaciones nuevas pendientes de gestionar. */
    private boolean warned = false;

    /**
     * Metodo para crear la pestana del tab customizada.
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
     * @return el valor del atributo.
     */
    public TabHost getTabHost() {
        return tabHost;
    }

    /**
     * Método set del atributo <i>mainTab</i>.
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
        RoleInfo roleInfo = (RoleInfo) intent.
                getSerializableExtra(ConfigurationConstants.EXTRA_RESOURCE_ROLE_SELECTED);

        if (roleInfo != null) {
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
            try {
                @SuppressLint("InflateParams")
                View authTab = getLayoutInflater().inflate(R.layout.roles_tab_view, null);
                ((ScrollView) this.findViewById(R.id.tabContentViewId)).addView(authTab);
            } catch (Throwable e) {
                PfLog.e(SFConstants.LOG_TAG, "No se ha podido crear la estructura de pestañas", e);
            }

            setTabHost(findViewById(android.R.id.tabhost));
            getTabHost().setup();
            getTabHost().getTabWidget().setDividerDrawable(R.drawable.tab_divider);

            buildMainTabs();

            getTabHost().setCurrentTab(0);

            // Cargamos la lista de roles disponibles.
            loadUsers(ConfigurationRole.AUTHORIZED);
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
        // Abrir ayuda
        if (item.getItemId() == R.id.help) {
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
                    CleanTempFilesTask cleanTask = new CleanTempFilesTask(SignfolderApp.getInternalTempDir());
                    cleanTask.execute();
                } catch (Exception e) {
                    PfLog.e(SFConstants.LOG_TAG,
                            "No se ha podido ejecutar la tarea de borrado de temporales", e); //$NON-NLS-1$
                }

                // Eliminamos la informacion de conexion con el DNIe si la hubiese
                DnieConnectionManager.getInstance().clearCan();
                DnieConnectionManager.getInstance().reset();

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
     * Obtiene los resultados emitidos por las actividades abiertas por la actual.
     * Este método es llamado cuando la actividad hija finaliza.
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
                Toast.makeText(this, R.string.toast_msg_creation_auth_role_ok, Toast.LENGTH_LONG).show();
            } else if (resultCode == ConfigurationConstants.ACTIVITY_RESULT_CODE_AUTH_ROLE_KO) {
                GenericResponse response = (GenericResponse) data.getSerializableExtra(CreateNewAuthorizedActivity.EXTRA_RESPONSE);
                String errorMessage = response != null && response.getErrorMessage() != null ?
                    response.getErrorMessage() : getString(R.string.toast_msg_creation_auth_role_ko);
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
            loadUsers(currentRoleSelected);
        } else if (requestCode == FindUserActivity.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                ConfigurationRole role = data != null ?
                        (ConfigurationRole) data.getSerializableExtra(FindUserActivity.EXTRA_ROLE) : null;
                if (role == ConfigurationRole.AUTHORIZED) {
                    // Mostramos un formulario para el usuario actual pueda configurar los datos
                    // para la autorizacion del usuario seleccionado
                    Bundle userBundle = data.getBundleExtra(FindUserActivity.EXTRA_USER);
                    Intent intent = new Intent(this, CreateNewAuthorizedActivity.class);
                    intent.putExtra(ConfigurationConstants.EXTRA_RESOURCE_USER_INFO, userBundle);
                    startActivityForResult(intent, ConfigurationConstants.ACTIVITY_REQUEST_CODE_AUTH_CREATION);
                }
                else if (role == ConfigurationRole.VERIFIER) {
                    // Asociamos al usuario seleccionado como validador del actual
                    GenericUser user = GenericUser.fromBundle(data.getBundleExtra(FindUserActivity.EXTRA_USER));
                    new CreateVerifierTask(user, this.saveValidatorListener).execute();
                }
            }
        } else if (requestCode == AuthorizationInfoActivity.REQUEST_CODE_INFO) {
            if (resultCode == AuthorizationInfoActivity.RESULT_OK) {
                Toast.makeText(this, R.string.toast_msg_change_auth_state_ok, Toast.LENGTH_LONG).show();
            } else if (resultCode == AuthorizationInfoActivity.RESULT_CODE_CHANGE_STATE_ERROR) {
                Toast.makeText(this, R.string.toast_msg_change_auth_state_error, Toast.LENGTH_LONG).show();
            }
            loadUsers(currentRoleSelected);
        }
    }

    /**
     * Listener method that manages the add new role button action.
     *
     * @param view Configuration view.
     */
    public void addRole(View view) {

        if (this.currentRoleSelected == null) {
            return;
        }

        Intent intent = new Intent(this, FindUserActivity.class);
        intent.putExtra(ConfigurationConstants.EXTRA_RESOURCE_ROLE_SELECTED, this.currentRoleSelected.getName());
        if (ConfigurationRole.AUTHORIZED.equals(this.currentRoleSelected)) {
            startActivityForResult(intent, FindUserActivity.REQUEST_CODE);
        } else if (ConfigurationRole.VERIFIER.equals(this.currentRoleSelected)) {
            intent.putStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_IDS, apps.getAppIdsList());
            intent.putStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_NAMES, apps.getAppNamesList());
            startActivityForResult(intent, FindUserActivity.REQUEST_CODE);
        }
    }

    /**
     * Method that loads the list of users of the selected role.
     * @param role Role to load.
     */
    private void loadUsers(ConfigurationRole role) {
        this.currentRoleSelected = role;

        // Si existe una tarea de carga en ejecucion, la detenemos
        if (this.currentLoadDelegationsTask != null
                && this.currentLoadDelegationsTask.getStatus() != AsyncTask.Status.FINISHED) {
            this.currentLoadDelegationsTask.cancel(true);
        }

        // Lanzamos la tarea para la recuperacion de los usuarios validadores o con autorizacion
        try {
            this.currentLoadDelegationsTask = new LoadDelegationsTask(role, this);
            this.currentLoadDelegationsTask.execute();
        } catch (Exception e) {
            setViewErrorGettingUsers();
        }
    }

    /**
     * Método que muestra la lista de usuarios con el rol de autorizados.
     *
     * @param authList Lista de autorizados.
     */
    private void showAuthorized(List<Authorization> authList) {
        // Recuperamos la vista que contendrá toda la información a mostrar.
        LinearLayout parentLayout = findViewById(R.id.authData);

        // Limpiamos la vista.
        cleanDataListView(ConfigurationRole.AUTHORIZED);

        // Poblamos la lista
        buildListItems(parentLayout, null, authList);
    }

    /**
     * Método que muestra la lista de usuarios con el rol de validador.
     *
     * @param verifierList Lista de validadores.
     */
    private void showVerifiers(List<Validator> verifierList) {
        // Recuperamos la vista que contedrá toda la información a mostrar.
        LinearLayout parentLayout = findViewById(R.id.verifierData);

        // Limpiamos la vista.
        cleanDataListView(ConfigurationRole.VERIFIER);

        // Poblamos la lista
        buildListItems(parentLayout, verifierList, null);
    }

    /**
     * Método que realiza la construcción del listado de roles.
     *
     * @param parentLayout Layout padre sobre el que aplicar los cambios.
     * @param verifierList Lista de validadores.
     * @param authList     Lista de autorizados.
     */
    private void buildListItems(LinearLayout parentLayout, List<Validator> verifierList, List<Authorization> authList) {

        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setVerticalScrollBarEnabled(true);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        recyclerView.setLayoutParams(lp);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(layoutManager);

        RecyclerView.Adapter<?> mAdapter;

        if (verifierList != null) {
            // Eliminamos el texto de error o de lista vacia si lo hubiese
            if (this.emptyValidatorsListTextView != null) {
                parentLayout.removeView(this.emptyValidatorsListTextView);
                this.emptyAuthorizationsListTextView = null;
            }
            mAdapter = new VerifierAdapter(verifierList, new VerifierAdapter.SelectValidatorListener() {
                @Override
                public void itemSelected(Validator validator) {
                    confirmRemoveValidator(validator);
                }
            });
        } else if (authList != null) {
            // Eliminamos el texto de error o de lista vacia si lo hubiese
            if (this.emptyAuthorizationsListTextView != null) {
                parentLayout.removeView(this.emptyAuthorizationsListTextView);
                this.emptyAuthorizationsListTextView = null;
            }
            mAdapter = new AuthorizerAdapter(authList, new AuthorizerAdapter.SelectAutorizationListener() {
                @Override
                public void itemSelected(Authorization auth) {
                    Intent intent = new Intent(UserConfigurationActivity.this, AuthorizationInfoActivity.class);
                    intent.putExtra(ConfigurationConstants.EXTRA_RESOURCE_AUTHORIZATION, auth.toBundle());
                    UserConfigurationActivity.this.startActivityForResult(intent, AuthorizationInfoActivity.REQUEST_CODE_INFO);
                }
            });
        } else {
            throw new IllegalArgumentException("Error. Todas las listas de roles a mostrar no pueden ser nulas.");
        }
        recyclerView.setAdapter(mAdapter);

        parentLayout.addView(recyclerView);
    }

    public void confirmRemoveValidator(Validator validator) {
        final MessageDialog dialog = new MessageDialog();
        dialog.setMessage(this.getString(R.string.dialog_msg_confirm_remove_validator, validator.getUser().getName()));
        dialog.setContext(UserConfigurationActivity.this);
        dialog.setNeedShowNegativeButton(true);
        dialog.setListeners(
                // Click en boton OK
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new RemoveValidatorTask(validator, new RemoveValidatorTask.RemoveValidatorListener() {
                            @Override
                            public void onSuccess() {
                                dialog.dismiss();

                                // Mostramos un mensaje de exito
                                Toast.makeText(UserConfigurationActivity.this, R.string.toast_msg_remove_validator_ok, Toast.LENGTH_LONG).show();

                                // Recargamos el listado
                                loadUsers(UserConfigurationActivity.this.currentRoleSelected);
                            }

                            @Override
                            public void onError(String message) {
                                dialog.dismiss();

                                // Mostramos un mensaje de error
                                Toast.makeText(UserConfigurationActivity.this, R.string.toast_msg_remove_validator_ko, Toast.LENGTH_LONG).show();
                            }
                        }).execute();
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
                        dialog.show(UserConfigurationActivity.this.getSupportFragmentManager(), "QuestionDialog"); //$NON-NLS-1$;
                    } catch (Exception e) {
                        PfLog.e(SFConstants.LOG_TAG, "No se ha podido mostrar la consulta al usuario", e); //$NON-NLS-1$
                    }
                }
            });
        } catch (final Exception e2) {
            PfLog.e(SFConstants.LOG_TAG, "Error en el hilo que muestra la consulta al usuario", e2); //$NON-NLS-1$
        }
    }

    /**
     * Método que actualiza la vista actual donde se muestra la lista de roles y
     * muestra un mensaje de tabla vacía.
     * @param role Pestaña seleccionada.
     */
    private void setViewEmptyTable(ConfigurationRole role) {

        // Creamos la vista que representa el texto a mostrar.
        TextView textView = new TextView(this, null, R.style.PFEmptyTableMessage);
        textView.setPadding(ALTERNATIVE_TEXT_MARGIN, ALTERNATIVE_TEXT_MARGIN, ALTERNATIVE_TEXT_MARGIN, ALTERNATIVE_TEXT_MARGIN);
        textView.setText(R.string.no_user_available);

        View viewById;
        if (!ConfigurationRole.AUTHORIZED.equals(role)) {
            viewById = findViewById(R.id.verifierData);
        } else {
            // recuperamos el elemento  a actualizar.
            viewById = findViewById(R.id.authData);
            this.emptyValidatorsListTextView = textView;
        }
        LinearLayout dataView = (LinearLayout) viewById;

        // Eliminados el contenido que pueda tener.
        cleanDataListView(role);

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

    TextView emptyAuthorizationsListTextView = null;

    TextView emptyValidatorsListTextView = null;

    /**
     * Método que modifica la vista actual y muestra un mensaje de error.
     */
    public void setViewErrorGettingUsers() {

        // Preparamos el texto de error
        TextView textView = new TextView(this, null, R.style.PFEmptyTableMessage);
        textView.setPadding(ALTERNATIVE_TEXT_MARGIN, ALTERNATIVE_TEXT_MARGIN, ALTERNATIVE_TEXT_MARGIN, ALTERNATIVE_TEXT_MARGIN);

        // recuperamos el elemento  a actualizar.
        View viewById;

        if (this.currentRoleSelected.equals(ConfigurationRole.AUTHORIZED)) {
            viewById = findViewById(R.id.authData);
            textView.setText(R.string.error_msg_get_authorizations);
            this.emptyAuthorizationsListTextView = textView;
        } else {
            viewById = findViewById(R.id.verifierData);
            textView.setText(R.string.error_msg_get_validators);
            this.emptyValidatorsListTextView = textView;
        }
        LinearLayout dataView = (LinearLayout) viewById;

        // Eliminados el contenido que pueda tener.
        dataView.removeAllViewsInLayout();

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
                    loadUsers(ConfigurationRole.AUTHORIZED);
                } else {
                    loadUsers(ConfigurationRole.VERIFIER);
                }
            }
        });
    }

    @Override
    public void loadedDelegations(ConfigurationRole requestedRole, List<?> delegationsList) {

        // Si el rol que se pidio es distinto del actual, entenderemos que se ha cambiado de pestaña
        // y ya no debe mostrarse el resultado obtenido
        if (requestedRole != this.currentRoleSelected) {
            return;
        }

        // Si el rol es de tipo autorizados...
        if (delegationsList.size() > 0 && ConfigurationRole.AUTHORIZED.equals(this.currentRoleSelected)) {
            List<Authorization> authList = (List<Authorization>) delegationsList;
            // Mostramos la lista de autorizados.
            showAuthorized(authList);
            // Advertimos al usuario si hay autorizaciones pendientes de gestionar
            checkPendingAuthorizations(authList);
        }
        // Si el rol es de tipo validadores...
        else if (delegationsList.size() > 0 && ConfigurationRole.VERIFIER.equals(this.currentRoleSelected)) {
            List<Validator> verifierList = (List<Validator>) delegationsList;
            // Mostramos la lista de validadores.
            showVerifiers(verifierList);
        }
        // Si la lista está vacía o es de un tipo no reconocido...
        else {
            // Generamos una respuesta genérica indicando que no se ha
            // encontrado ningún usuario para el rol indicado.
            setViewEmptyTable(this.currentRoleSelected);
        }
    }

    /**
     * Comprueba si hay autorizaciones pendientes de aceptar o rechazar y, si no se ha advertido
     * antes de ello, avisa al usuario.
     * @param authList Listado de autorizaciones.
     */
    private void checkPendingAuthorizations(List<Authorization> authList) {
        if (!this.warned) {
            for (Authorization auth : authList) {
                if (auth.getState() == AuthorizationState.PENDING && !auth.isSended()) {
                    this.warned = true;
                    warnPendingAuthorization(auth);
                    break;
                }
            }
        }
    }

    /**
     * Muestra una advertencia indicando que hay autorizaciones pendientes de autorizar o rechazar.
     * @param auth Autorizaci&oacute;n.
     */
    private void warnPendingAuthorization(Authorization auth) {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(R.string.dialog_msg_pending_authorizations)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }

    @Override
    public void errorLoadingDelegations(ConfigurationRole requestedRole) {
        // Si el rol que se pidio es distinto del actual, entenderemos que se ha cambiado de pestaña
        // y ya no debe mostrarse el error
        if (requestedRole != this.currentRoleSelected) {
            return;
        }
        setViewErrorGettingUsers();
    }

    @Override
    public void lostSession() {
        PfLog.w(SFConstants.LOG_TAG, "Se ha perdido la sesion con el Portafirmas");
    }

    /** Gestiona el resultado del alta de un validador. */
    private CreateVerifierTask.SaveValidatorListener saveValidatorListener = new CreateVerifierTask.SaveValidatorListener() {
        @Override
        public void validatorSaved() {
            // Mostramos el mensaje de exito
            Toast.makeText(UserConfigurationActivity.this, R.string.toast_msg_creation_verifier_role_ok, Toast.LENGTH_LONG).show();
            // Cargamos el listado de validadores
            loadUsers(ConfigurationRole.VERIFIER);
        }

        @Override
        public void errorSavingValidator(final GenericResponse response) {

            // Usamos el mensaje de error indicado en la respuesta o, si no lo habia,
            // uno por defecto
            String errorMessage = response != null && response.getErrorMessage() != null ?
                    response.getErrorMessage() : getString(R.string.toast_msg_creation_verifier_role_ko);

            // Mostramos el mensaje de error
            Toast.makeText(UserConfigurationActivity.this, errorMessage, Toast.LENGTH_LONG).show();
        }
    };
}
