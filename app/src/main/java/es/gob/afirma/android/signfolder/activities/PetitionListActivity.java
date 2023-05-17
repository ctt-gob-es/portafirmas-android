package es.gob.afirma.android.signfolder.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.gob.afirma.android.crypto.DnieConnectionManager;
import es.gob.afirma.android.fcm.RegistrationIntentService;
import es.gob.afirma.android.signfolder.AppPreferences;
import es.gob.afirma.android.signfolder.ConfigureFilterDialogBuilder;
import es.gob.afirma.android.signfolder.ConfigureFilterDialogBuilder.FilterConfig;
import es.gob.afirma.android.signfolder.CryptoConfiguration;
import es.gob.afirma.android.signfolder.CustomAlertDialog;
import es.gob.afirma.android.signfolder.ErrorManager;
import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.SignfolderApp;
import es.gob.afirma.android.signfolder.listeners.DialogFragmentListener;
import es.gob.afirma.android.signfolder.listeners.OperationRequestListener;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.RequestResult;
import es.gob.afirma.android.signfolder.proxy.SignRequest;
import es.gob.afirma.android.signfolder.proxy.SignRequest.RequestType;
import es.gob.afirma.android.signfolder.tasks.ApproveRequestsTask;
import es.gob.afirma.android.signfolder.tasks.CleanTempFilesTask;
import es.gob.afirma.android.signfolder.tasks.FireSignTask;
import es.gob.afirma.android.signfolder.tasks.LoadSignRequestsTask;
import es.gob.afirma.android.signfolder.tasks.LoadSignRequestsTask.LoadSignRequestListener;
import es.gob.afirma.android.signfolder.tasks.LogoutRequestTask;
import es.gob.afirma.android.signfolder.tasks.OpenHelpDocumentTask;
import es.gob.afirma.android.signfolder.tasks.RejectRequestsTask;
import es.gob.afirma.android.signfolder.tasks.UpdatePushNotificationsTask;
import es.gob.afirma.android.signfolder.tasks.VerifyRequestsTask;
import es.gob.afirma.android.user.configuration.ConfigurationConstants;
import es.gob.afirma.android.user.configuration.ConfigurationRole;
import es.gob.afirma.android.user.configuration.RoleInfo;
import es.gob.afirma.android.user.configuration.UserConfig;
import es.gob.afirma.android.util.PfLog;

/**
 * Actividad que representa una lista de documentos pendientes de ser firmados o
 * rechazados. Al pulsar prolongadamente sobre un elemento se muestra un menu
 * contextual donde se puede ver los detalles del documento, firmar
 * individualmente o rechazar.
 * <p>
 * La visibilidad de la lista y la etiqueta de "No hay elementos" se delega en
 * el Layout por medio del uso de los elementos "list" y "empty" reconocidos por
 * el ListActivity.
 */
public final class PetitionListActivity extends SignatureFragmentActivity implements
        LoadSignRequestListener, OnItemClickListener, DialogFragmentListener,
        UpdatePushNotificationsTask.UpdatePushNotsListener {

    public final static String EXTRA_RESOURCE_DNI = "es.gob.afirma.signfolder.dni"; //$NON-NLS-1$
    public final static String EXTRA_RESOURCE_CERT_ALIAS = "es.gob.afirma.signfolder.alias"; //$NON-NLS-1$
    public final static String EXTRA_RESOURCE_CERT_B64 = "es.gob.afirma.signfolder.cert"; //$NON-NLS-1$
    public final static String EXTRA_RESOURCE_APP_IDS = "es.gob.afirma.signfolder.apps.ids"; //$NON-NLS-1$
    public final static String EXTRA_RESOURCE_APP_NAMES = "es.gob.afirma.signfolder.apps.names"; //$NON-NLS-1$
    public static final String KEY_COUNT = "notificationCount";

    /**
     * Clave para comprobaci&oacute;n del estado de las solicitudes de firma que
     * se muestran actualmente.
     */
    public static final String SIGN_REQUEST_STATE_KEY = "SignRequestState"; //$NON-NLS-1$
    /**
     * Clave usada internamente para guardar el estado de la propiedad
     * "needReload"
     */
    private static final String KEY_SAVEINSTANCE_NEED_RELOAD = "saveinstance_needReload"; //$NON-NLS-1$
    /**
     * Clave usada internamente para guardar el estado de la propiedad
     * "loadingRequests"
     */
    private static final String KEY_SAVEINSTANCE_LOADING = "saveinstance_loading"; //$NON-NLS-1$
    /**
     * Clave usada internamente para guardar el estado de la propiedad
     * "filterConfig".
     */
    private static final String KEY_SAVEINSTANCE_FILTER_CONFIG = "saveinstance_filterConfig"; //$NON-NLS-1$

    private static final String KEY_DAYS_TO_EXPIRE = "caducidad";

    private static final int DEFAULT_DAYS_TO_EXPIRE = 3;
    private final static int PAGE_SIZE = 50;
    /**
     * Di&aacute;logo para la configuraci&oacute;n de los filtros.
     */
    private final static int DIALOG_FILTER = 11;
    /**
     * Di&aacute;logo para confirmar el cierre de la sesi&oacute;n.
     */
    private final static int DIALOG_CONFIRM_EXIT = 12;
    /**
     * Di&aacute;logo para confirmar el rechazo de peticiones.
     */
    private final static int DIALOG_CONFIRM_REJECT = 13;
    /**
     * Di&aacute;logo para confirmar la firma de peticiones.
     */
    private final static int DIALOG_CONFIRM_SIGN = 15;
    /**
     * Di&aacute;logo de notificaci&oacute;n de error.
     */
    private final static int DIALOG_ERROR = 16;
    /**
     * Di&aacute;logo de notificaci&oacute;n de advertencia.
     */
    private final static int DIALOG_WARNING = 17;

    private final static int PERMISSION_TO_OPEN_HELP = 22;

    /**
     * Tag para la presentaci&oacute;n de di&aacute;logos
     */
    private final static String DIALOG_TAG = "dialog"; //$NON-NLS-1$
    private static final int FIRST_PAGE = 1;
    // Numero de peticiones solicitadas y procesadas
    int numRequestToSignPending;
    int numRequestToApprovePending;
    int numRequestToRejectPending;
    int numRequestToVerifyPending;
    /**
     * Indica si debe recargarse el listado de peticiones. Esto se usa cuando la logica de la propia
     * actividad obliga a la recarga.
     */
    private boolean needReload = true;
    /**
     * Indica si se ha forzado la recarga del listado desde el exterior de la actividad.
     */
    private boolean forceReload = true;
    private boolean loadingRequests = false;
    private boolean allSelected = false;
    private String currentState = null;
    private Menu menuRef;
    private int currentPage = FIRST_PAGE;

    /**
     * Se&ntilde;ala si se produjo alg&uacute;n error al procesar las peticiones.
     */
    private boolean anyError = false;

    /**
     * Tarea de carga en ejecuci&oacute;n.
     */
    private LoadSignRequestsTask loadingTask = null;
    private String dni = null;
    private String certB64 = null;
    private String certAlias = null;
    private List<String> appIds = null;
    private List<String> appNames = null;

    /** Configuración actual de filtros. */
    private FilterConfig filterConfig = null;

    private int numPages = -1;
    private ProgressDialog progressDialog = null;
    /**
     * Rol seleccionado durante la autenticaci&oacute;n.
     */
    private ConfigurationRole roleSelected;
    /**
     * Informaci&oacute;n del rol seleccionado.
     */
    private RoleInfo roleSelectedInfo;

    /**
     * Peticiones cargadas
     */
    List<SignRequest> loadedRequests;
    /**
     * Configuraci&oacute;n de usuario.
     */
    private UserConfig userConfig;
    private ConfigureFilterDialogBuilder filterDialogBuilder;
    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null && intent.getExtras().getBoolean("success")) {
//                menuRef.findItem(R.id.notifications).setVisible(false);
                menuRef.findItem(R.id.notifications).setTitle(R.string.disable_notifications);
                if (intent.getExtras().getBoolean("noticeUser")) {
                    Toast.makeText(context, R.string.toast_msg_gcm_connection_ok, Toast.LENGTH_SHORT).show();
                }
            } else {
                if (intent.getExtras() != null && intent.getExtras().getBoolean("noticeUser")) {
                    Toast.makeText(context, R.string.toast_msg_gcm_connection_ko, Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    String getCurrentState() {
        return this.currentState;
    }

    void setFilterConfig(final FilterConfig newFilterConfig) {
        if (!newFilterConfig.isEnabled()) {
            if (this.filterConfig != null) {
                this.filterConfig.reset(this.roleSelected, this.userConfig.isUserWithVerifiers());
                this.filterConfig.setOrderAttribute(newFilterConfig.getOrderAttribute());
            }
            if (this.filterDialogBuilder != null) {
                if (this.filterConfig != null) {
                    this.filterDialogBuilder.resetLayout(this.filterConfig);
                } else {
                    this.filterDialogBuilder.resetLayout();
                }
            }
        } else {
            this.filterConfig = newFilterConfig;
        }
    }

    ProgressDialog getProgressDialog() {
        return this.progressDialog;
    }

    void setProgressDialog(final ProgressDialog pd) {
        this.progressDialog = pd;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.userConfig = (UserConfig) getIntent().getSerializableExtra(ConfigurationConstants.EXTRA_RESOURCE_USER_CONFIG);
        if (this.userConfig == null) {
            this.userConfig = new UserConfig();
        }
        this.roleSelectedInfo = (RoleInfo) getIntent().getSerializableExtra(ConfigurationConstants.EXTRA_RESOURCE_ROLE_SELECTED);
        if (roleSelectedInfo != null) {
            this.roleSelected = ConfigurationRole.getValue(roleSelectedInfo.getRoleId());
        }
        this.currentState = getIntent().getStringExtra(SIGN_REQUEST_STATE_KEY);
        if (this.currentState == null) {
            this.currentState = SignRequest.STATE_UNRESOLVED;
        }
        this.forceReload = getIntent().getBooleanExtra(
                ConfigurationConstants.EXTRA_RESOURCE_FORCE_REFRESH, false);

        // Si esta configurado que no se necesita recargar la pagina, no lo
        // hacemos
        if (savedInstanceState != null) {
            loadSavedInstances(savedInstanceState);
        }

        // Cargamos los extras proporcionados a la actividad
        loadIntentExtra(getIntent());

        // Configuramos la vista segun el estado
        if (SignRequest.STATE_SIGNED.equals(this.currentState)) {
            setTitle(R.string.title_signed_petition_list);
            setContentView(R.layout.activity_resolved_petition_list);
        } else if (SignRequest.STATE_REJECTED.equals(this.currentState)) {
            setTitle(R.string.title_rejected_petition_list);
            setContentView(R.layout.activity_resolved_petition_list);
        } else {
            setTitle(R.string.title_unresolved_petition_list);
            setContentView(R.layout.activity_unresolved_petition_list);
        }

        // Si el rol seleccionado es el de validador, deshabilitamos el botón de rechazar petición,
        // y cambiamos el nombre del botón de firma.
        Button rejectBtn = findViewById(R.id.btnReject);
        Button signBtn = findViewById(R.id.btnSign);
        if (this.roleSelected != null && this.roleSelected.equals(ConfigurationRole.VERIFIER)) {
            rejectBtn.setEnabled(false);
            rejectBtn.setClickable(false);
            rejectBtn.setVisibility(View.GONE);
            signBtn.setText(R.string.verify);
        }

        getListView().setOnItemClickListener(this);

        // En el viejo proxy, si en algun momento se registraron las notificaciones, pero ahora ha
        // cambiado el token, se vuelven a registrar
        if (CommManager.getInstance().isOldProxy()) {
            String userProxyId = getUserProxyId();
            if (isNotificationTokenRegistered(userProxyId) && isNotificationTokenChanged(userProxyId)) {
                registryNotificationToken(userProxyId, false);
            }
        }
        // En el nuevo proxy, comprobaremos si esta la plataforma de notificaciones configurada
        else if (this.userConfig.isSimConfig() && this.userConfig.isPushActivated()) {
            String userProxyId = getUserProxyId();
            // Si las notificaciones en local estan activas, nos aseguramos de que las
            // notificaciones en el servidor estan activas y que el toquen este actualizado
            if (isNotificationTokenRegistered(userProxyId)) {
                // Si el token ha cambiado, pedimos el alta del nuevo token
                if (isNotificationTokenChanged(userProxyId)) {
                    registryNotificationToken(userProxyId, false);
                }
                // Si no ha cambiado el token, pero las notificaciones en servidor estan
                // desactivadas, las activamos
                else if (!this.userConfig.isPushActivated()) {
                    new UpdatePushNotificationsTask(true, this).execute();
                }
            }
        }

        // Si los filtros no estan inicializados, establecemos el valor por defecto que deben tener.
        // No se entrara aqui, por ejemplo, cuando se este recontruyendo la actividad despues de
        // cambiar de listado
        if (this.filterConfig == null) {
            // Comprobamos que los filtros tienen valores correctos.
            configureDefaultFilters();
        }

        // Establecemos la accion asociada a la actualizacion de la lista
        ((SwipeRefreshLayout) findViewById(R.id.swiperefresh)).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateCurrentList(FIRST_PAGE);
            }
        });
    }

    /**
     * Método para la creacion del filtro de peticiones por defecto.
     */
    private void initFilterConfig() {
        if (this.filterConfig == null) {
            this.filterConfig = new FilterConfig(this.roleSelected, this.userConfig.isUserWithVerifiers());
        }
        String userId = this.dni != null ? this.dni : "";
        this.filterConfig.setUserId(userId);
        String ownerId = this.roleSelectedInfo != null ? this.roleSelectedInfo.getOwnerDni() : "";
        this.filterConfig.setOwnerId(ownerId);
    }

    /**
     * Método encargado de comprobar que los filtros tienen unos valores correctos.
     */
    private void configureDefaultFilters() {

        // Inicializamos la configuracion de filtros
        initFilterConfig();

        // Si no se tiene establecida una configuracion particular de filtros, se aplican la
        // configuracion por defecto
        if (!this.filterConfig.isEnabled()) {
            if (this.filterConfig.getMonth() == null) {
                this.filterConfig.setMonth(ConfigureFilterDialogBuilder.VALUE_MONTH_ALL);
            }
            if (this.filterConfig.getAppType() == null) {
                if (ConfigurationRole.VERIFIER.equals(this.roleSelected)) {
                    this.filterConfig.setAppType(ConfigureFilterDialogBuilder.VALUE_APP_TYPE_VIEW_NO_VALIDATE);
                } else {
                    this.filterConfig.setAppType(ConfigureFilterDialogBuilder.VALUE_APP_TYPE_VIEW_ALL);
                }
            }
        }
    }

    /**
     * Indica si el usuario se ha dado previamente de alta en el sistema de notificaciones
     * para este portafirmas.
     * @param  userProxyId Identificador asociado a la combinación usuario/proxy.
     * @return {@code true} si el usuario se ha dado previamente de alta, {@code false} en caso
     * contrario.
     */
    private boolean isNotificationTokenRegistered(String userProxyId) {
        AppPreferences prefs = AppPreferences.getInstance();
        boolean tokenRegistered = prefs.getPreferenceBool(
                AppPreferences.PREFERENCES_KEY_PREFIX_NOTIFICATION_ACTIVE + userProxyId,
                false);

        PfLog.d(SFConstants.LOG_TAG, "El token de notificaciones esta registrado: " + tokenRegistered);

        return tokenRegistered;
    }


    /**
     * Desactiva las notificaciones en local al usuario de este portafirmas estableciendo
     * que el token de notificaciones no est&aacute; registrado. Esto har&aacute; que al volver
     * a habilitarlo se vuelva a dar el alta, aunque el token no haya cambiado.
     * @param  userProxyId Identificador asociado a la combinación usuario/proxy.
     */
    private void disableNotifications(String userProxyId) {
        AppPreferences prefs = AppPreferences.getInstance();
        prefs.setPreferenceBool(
                AppPreferences.PREFERENCES_KEY_PREFIX_NOTIFICATION_ACTIVE + userProxyId,
                false);

        PfLog.d(SFConstants.LOG_TAG, "Desactivada las notificaciones en local");
    }

    /**
     * Indica si el token de notificaciones actual ha cambiado con respecto al último que el usuario
     * dio de alta.
     * @param  userProxyId Identificador asociado a la combinación usuario/proxy.
     * @return {@code true} si el token actual no es igual al último que dio de alta el usuario,
     * {@code false} en caso contrario.
     */
    private boolean isNotificationTokenChanged(String userProxyId) {

        AppPreferences prefs = AppPreferences.getInstance();
        String registeredToken = prefs.getPreference(
                AppPreferences.PREFERENCES_KEY_PREFIX_NOTIFICATION_TOKEN + userProxyId,
                null);

        PfLog.i(SFConstants.LOG_TAG, "Recuperamos de la tabla interna de tokens: "
                + AppPreferences.PREFERENCES_KEY_PREFIX_NOTIFICATION_TOKEN + userProxyId
                + "=" + registeredToken);

        // Obtenemos el token actual que le corresponde a la aplicacion
        String currentToken = prefs.getCurrentToken();

        // Si el token es nulo (no se ha podido obtener) no podemos saber si ha cambiado, por lo
        // que lo negamos
        if (currentToken == null) {
            return false;
        }

        // Comprobamos si el token de notificaciones que tiene dado de alta ese usuario
        // es igual al actual. En caso contrario, se tendra que actualizar
        //boolean tokenChanged = !currentToken.equals(registeredToken);
        boolean tokenChanged = !currentToken.startsWith(registeredToken);

        PfLog.d(SFConstants.LOG_TAG, "El token ha cambiado: " + tokenChanged);

        return tokenChanged;
    }

    /**
     * Registra el nuevo token de notificaciones para que el usuario pueda recibirlas
     * para este portafirmas y dispositivo.
     * @param  userProxyId Identificador asociado a la combinación usuario/proxy.
     */
    private void registryNotificationToken(String userProxyId, boolean noticeUser) {

        PfLog.d(SFConstants.LOG_TAG, "Iniciamos el registro del nuevo token de notificacion");

        Intent intent = new Intent(this, RegistrationIntentService.class);
        if (this.certB64 != null) {
            intent.putExtra(RegistrationIntentService.EXTRA_RESOURCE_CERT_B64, this.certB64);
        }
        if (this.dni != null) {
            intent.putExtra(RegistrationIntentService.EXTRA_RESOURCE_DNI, this.dni);
        }
        intent.putExtra(RegistrationIntentService.EXTRA_RESOURCE_NOTICE_USER, noticeUser);
        intent.putExtra(RegistrationIntentService.EXTRA_RESOURCE_USER_PROXY_ID, userProxyId);
        startService(intent);
    }

    /**
     * Obtiene la clave con la que se guarda el token con el que actualmente est&aacute;
     * dado de alta el usuario para la recepci&oacute;n de las notificaciones del Portafirmas
     * actual.
     */
    private String getUserProxyId() {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            PfLog.w(SFConstants.LOG_TAG,
                    "No se puede comprobar el token dado de alta actualmente. " +
                            "No se actualizara el token: " + e);
            return null;
        }
        md.update(AppPreferences.getInstance().getSelectedProxyUrl().getBytes());
        if (this.dni != null) {
            md.update(this.dni.getBytes());
        }
        return Base64.encodeToString(md.digest(), Base64.NO_PADDING).trim();
    }

    private void loadSavedInstances(final Bundle savedInstanceState) {
        this.needReload =
                savedInstanceState.containsKey(KEY_SAVEINSTANCE_NEED_RELOAD) &&
                        savedInstanceState.getBoolean(KEY_SAVEINSTANCE_NEED_RELOAD);

        this.loadingRequests =
                !savedInstanceState.containsKey(KEY_SAVEINSTANCE_LOADING) ||
                        savedInstanceState.getBoolean(KEY_SAVEINSTANCE_LOADING);

        // Recuperamos la configuracion de filtros
        if (savedInstanceState.containsKey(KEY_SAVEINSTANCE_FILTER_CONFIG)) {
            this.filterConfig = (FilterConfig) savedInstanceState.getSerializable(KEY_SAVEINSTANCE_FILTER_CONFIG);
        }
    }

    private void loadIntentExtra(final Intent intent) {

        this.dni = intent.getStringExtra(EXTRA_RESOURCE_DNI);
        this.certB64 = intent.getStringExtra(EXTRA_RESOURCE_CERT_B64);
        this.certAlias = intent.getStringExtra(EXTRA_RESOURCE_CERT_ALIAS);
        this.appIds = intent.getStringArrayListExtra(EXTRA_RESOURCE_APP_IDS);
        this.appNames = intent.getStringArrayListExtra(EXTRA_RESOURCE_APP_NAMES);
        // Si solo esta documentado el certificado se viene de pinchar en una notificacion
        if (this.certAlias == null && this.appIds == null && this.appNames == null) {
            CommManager.getInstance().setNewProxy();
            CommManager.getInstance().setProxyURL(AppPreferences.getInstance().getSelectedProxyUrl());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (this.needReload || this.forceReload) {
            updateCurrentList(FIRST_PAGE);
            this.needReload = false;
            this.forceReload = false;
        }
    }

    /**
     * Metodo que define la accion a realizar al pulsar en el boton Reject
     *
     * @param v Vista desde la que se invoco el metodo
     */
    public void onClickReject(final View v) {

        final SignRequest[] reqs = getSelectedRequests();

        if (reqs == null || reqs.length == 0) {
            showWarningDialog(getString(R.string.dialog_msg_no_selected_requests));
            return;
        }

        // Mostramos el dialogo para confirmar el rechazo
        final CustomAlertDialog dialog = CustomAlertDialog.newInstance(
                DIALOG_CONFIRM_REJECT,
                getString(R.string.dialog_title_confirm_reject),
                getString(R.string.dialog_msg_reject_request),
                getString(android.R.string.ok),
                getString(android.R.string.cancel),
                this);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    dialog.show(getSupportFragmentManager(), DIALOG_TAG);
                }
                catch (Exception e) {
                    PfLog.w(SFConstants.LOG_TAG, "No se pudo mostrar el dialogo para confirmar el rechazo de la peticion", e); //$NON-NLS-1$
                }
            }
        });
    }

    private void showWarningDialog(final String message) {
        final CustomAlertDialog dialog = CustomAlertDialog.newInstance(
                DIALOG_WARNING, getString(R.string.aviso),
                message,
                getString(android.R.string.ok), null, this);

        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        dialog.show(getSupportFragmentManager(), DIALOG_TAG);
                    }
                    catch (Exception e) {
                        PfLog.w(SFConstants.LOG_TAG, "No se pudo mostrar el dialogo de error de elementos no seleccionados", e); //$NON-NLS-1$
                    }
                }
            });
        } catch (final Exception e) {
            PfLog.w(SFConstants.LOG_TAG,
                    "No se ha podido mostrar el dialogo informando de que no hay peticiones seleccionadas: " + e); //$NON-NLS-1$
        }
    }

    private void showErrorDialog(final String message) {

        final CustomAlertDialog dialog = CustomAlertDialog.newInstance(
                DIALOG_ERROR, getString(R.string.aviso), message,
                getString(android.R.string.ok), null, this);

        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        dialog.show(PetitionListActivity.this.getSupportFragmentManager(), DIALOG_TAG);
                    }
                    catch (Exception e) {
                        PfLog.w(SFConstants.LOG_TAG, "No se pudo mostrar el mensaje de error", e); //$NON-NLS-1$
                    }
                }
            });
        } catch (final Throwable e) {
            PfLog.w(SFConstants.LOG_TAG,
                    "No se ha podido mostrar el dialogo de error con el mensaje: " + message, e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Metodo que define la accion a realizar al pulsar en el boton Sign
     *
     * @param v Vista desde la que se invoco el metodo
     */
    public void onClickSign(final View v) {

        // Fijamos las peticiones seleccionadas
        final SignRequest[] requests = getSelectedRequests();

        if (requests == null || requests.length == 0) {
            showWarningDialog(getString(R.string.dialog_msg_no_selected_requests));
            return;
        }

        final CustomAlertDialog dialog = CustomAlertDialog.newInstance(
                DIALOG_CONFIRM_SIGN,
                getString(R.string.aviso),
                getViewContentDialogConfirmOperation(requests),
                getString(android.R.string.ok),
                getString(R.string.cancel),
                this);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    dialog.show(getSupportFragmentManager(), DIALOG_TAG);
                }
                catch (Exception e) {
                    PfLog.w(SFConstants.LOG_TAG, "No se pudo mostrar el dialogo de confirmacion de firma", e); //$NON-NLS-1$
                }
            }
        });

    }

    /**
     * Devuelve la vista con el contenido del di&aacute;logo de
     * confirmaci&oacute;n de rechazo, firma o visto bueno de las peticiones
     * seleccionadas
     *
     * @param requests Peticiones seleccionadas.
     * @return Vista del di&aacute;logo de confirmaci&oacute;n.
     */
    private View getViewContentDialogConfirmOperation(final SignRequest[] requests) {
        final LayoutInflater li = LayoutInflater.from(PetitionListActivity.this);
        final View view = li.inflate(R.layout.dialog_view_reject_sign_vb, null);
        int countApproved = 0;
        int countVerified = 0;
        int countSigned = 0;
        for (final SignRequest request : requests) {
            if (this.roleSelected != null && this.roleSelected.equals(ConfigurationRole.VERIFIER)) {
                request.setType(RequestType.VERIFY);
            }
            if (request.getType() == RequestType.APPROVE) {
                countApproved++;
            } else if (request.getType() == RequestType.VERIFY) {
                countVerified++;
            } else if (request.getType() == RequestType.SIGNATURE) {
                countSigned++;
            }
        }

        // Peticiones de firma
        if (countSigned > 0) {
            final TextView tvMsg = view.findViewById(R.id.tvNumSigns);
            tvMsg.setVisibility(View.VISIBLE);
            if (requests.length - countApproved == 1) {
                tvMsg.setText(R.string.dialog_msg_sign_petition_1);
            } else {
                tvMsg.setText(getString(R.string.dialog_msg_sign_petition_2, (requests.length - countApproved)));
            }
        }
        // Peticiones de visto bueno
        if (countApproved > 0) {
            final TextView tvMsg = view.findViewById(R.id.tvNumVB);
            tvMsg.setVisibility(View.VISIBLE);
            if (countApproved == 1) {
                tvMsg.setText(R.string.dialog_msg_approve_petition_1);
            } else {
                tvMsg.setText(getString(R.string.dialog_msg_approve_petition_2, countApproved));
            }
        }

        // Peticiones de validación
        if (countVerified > 0) {
            final TextView tvMsg = view.findViewById(R.id.tvNumValidations);
            tvMsg.setVisibility(View.VISIBLE);
            if (countVerified == 1) {
                tvMsg.setText(R.string.dialog_msg_verify_petition_1);
            } else {
                tvMsg.setText(getString(R.string.dialog_msg_verify_petition_2, countVerified));
            }
        }

        return view;
    }

    /**
     * Recupera el componente lista de la pantalla.
     *
     * @return Lista.
     */
    private ListView getListView() {
        return findViewById(R.id.userList);
    }

    /**
     * Recupera el componente lista de la pantalla.
     *
     * @return Lista.
     */
    private ListAdapter getListAdapter() {
        return getListView().getAdapter();
    }

    /**
     * Recupera el listado de solicitudes seleccionadas.
     *
     * @return Listado de solicitudes.
     */
    private SignRequest[] getSelectedRequests() {
        final ListView lv = getListView();
        lv.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

        final PetitionListArrayAdapter adapter = (PetitionListArrayAdapter) lv.getAdapter();
        if (adapter == null) {
            return null;
        }
        final List<SignRequest> requests = new ArrayList<>();

        for (int i = 0; i < adapter.getCount(); i++) {
            final PetitionListAdapterItem item = adapter.getItem(i);
            if (item instanceof PetitionElement) {
                final SignRequest request = ((PetitionElement) item)
                        .getSignRequest();
                if (request.isSelected()) {
                    requests.add(request);
                }
            }
        }

        return requests.toArray(new SignRequest[0]);
    }

    // Definimos el menu de opciones de la aplicacion, cuyas opciones estan
    // definidas
    // para cada listado de peticiones
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        menuRef = menu;
        if (SignRequest.STATE_SIGNED.equals(this.currentState)) {
            getMenuInflater().inflate(
                    R.menu.activity_petition_list_signed_options_menu, menu);

            // Ocultamos la lista de peticiones rechazadas
            // en caso de estar autenticado con el rol de validador.
            if (this.roleSelected != null && this.roleSelected.equals(ConfigurationRole.VERIFIER)) {
                menu.findItem(R.id.see_rejects).setVisible(false);
            }
        } else if (SignRequest.STATE_REJECTED.equals(this.currentState)) {
            getMenuInflater().inflate(
                    R.menu.activity_petition_list_rejected_options_menu, menu);

            // Si el rol con el que se ha accedido es "validador",
            // se deshabilita la visualización de las peticiones firmadas.
            if (this.roleSelected != null && this.roleSelected.equals(ConfigurationRole.VERIFIER)) {
                menu.findItem(R.id.see_signeds).setVisible(false);
            }
        } else {
            getMenuInflater()
                    .inflate(
                            R.menu.activity_petition_list_unresolved_options_menu,
                            menu);
            // Ocultamos la lista de peticiones rechazadas y firmadas
            // en caso de estar autenticado con el rol de validador.
            if (this.roleSelected != null && this.roleSelected.equals(ConfigurationRole.VERIFIER)) {
                menu.findItem(R.id.see_rejects).setVisible(false);
                menu.findItem(R.id.see_signeds).setVisible(false);
            }
        }

        // Si se utiliza el viejo proxy o si el actual tiene una version inferior a la 25, el
        // servicio proxy no permite la gestion de autorizaciones y validadores
        if (CommManager.getInstance().isOldProxy() || this.userConfig.getProxyVersion() < SignfolderApp.PROXY_VERSION_25) {
            menu.findItem(R.id.setting).setEnabled(false);
            menu.findItem(R.id.setting).setVisible(false);
        }

        // Si el rol es distinto del de firmante, tampoco permitiremos el acceso a la pantalla
        // de configuracion
        if (this.roleSelected != null && menu.findItem(R.id.setting) != null) {
            menu.findItem(R.id.setting).setEnabled(false);
            menu.findItem(R.id.setting).setVisible(false);
        }

        // Si solo existe el rol de firmante, no mostramos la opción de cambiar de rol.
        if (this.userConfig.getRoles().size() < 1) {
            menu.findItem(R.id.changeRole).setEnabled(false);
            menu.findItem(R.id.changeRole).setVisible(false);
        }

        // Si no hemos podido recuperar nuestro token de notificaciones,
        // no podemos dar la opcion de activarlas/desactivarlas
        // Obtenemos el token actual que le corresponde a la aplicacion
        String currentToken = AppPreferences.getInstance().getCurrentToken();
        if (currentToken == null) {
            menu.findItem(R.id.notifications).setVisible(false);
        }
        // En el proxy viejo, cuando tengamos token de notificacion, aparecera la opcion
        // de activarlas cuando no este ya registrado el usuario
        else if (CommManager.getInstance().isOldProxy()) {
            String userProxyId = getUserProxyId();
            boolean tokenRegistered = isNotificationTokenRegistered(userProxyId);
            if (!tokenRegistered) {
                menu.findItem(R.id.notifications).setTitle(R.string.enable_notifications);
            }
            menu.findItem(R.id.notifications).setVisible(!tokenRegistered);
        }
        // En el proxy nuevo, la opcion de  las notificaciones solo se mostrara cuando
        // el Portafirmas tenga configurada la conexion con la plataforma de notificaciones.
        // Segun si las notificaciones se encuentren activas o no se mostrara la opcion de
        // desactivarlas o volverlas a activar.
        else {
            // Comprobamos si al usuario se le está permitido habilitar las notificaciones push.
            boolean isSimConfigured = this.userConfig.isSimConfig();

            if (!isSimConfigured) {
                menu.findItem(R.id.notifications).setVisible(false);
            }
            else {
                // Comprobamos el estado de las notificaciones
                boolean isPushActived = this.userConfig.isPushActivated();
                // Si estan activadas, mostramos la opcion de desactivarlas
                if (isPushActived) {
                    menu.findItem(R.id.notifications).setTitle(R.string.disable_notifications);
                }
                // Si estan desactivadas, mostramos la opcion de activarlas
                else {
                    menu.findItem(R.id.notifications).setTitle(R.string.enable_notifications);
                }
                menu.findItem(R.id.notifications).setVisible(true);
            }
        }
        return true;
    }

    // Definimos que hacer cuando se pulsa una opcion del menu de opciones de la
    // aplicacion
    // En el ejemplo se indica la opcion seleccionada
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        // Seleccionar todos los elementos del listado actual
        if (item.getItemId() == R.id.select_all) {
            selectAllRequests(true);
            this.allSelected = true;
        }
        // Deseleccionar todos los elementos del listado actual
        else if (item.getItemId() == R.id.unselect_all) {
            selectAllRequests(false);
            this.allSelected = false;
        }
        // Actualizar listado actual
        else if (item.getItemId() == R.id.refresh) {
            ((SwipeRefreshLayout) findViewById(R.id.swiperefresh)).setRefreshing(true);
            updateCurrentList(FIRST_PAGE);
        }
        // Cambiar al listado de peticiones firmadas
        else if (item.getItemId() == R.id.see_signeds) {
            changeCurrentRequestList(SignRequest.STATE_SIGNED);
        }
        // Cambiar al listado de peticiones rechazadas
        else if (item.getItemId() == R.id.see_rejects) {
            changeCurrentRequestList(SignRequest.STATE_REJECTED);
        }
        // Cambiar al listado de peticiones pendientes
        else if (item.getItemId() == R.id.see_unresolveds) {
            changeCurrentRequestList(SignRequest.STATE_UNRESOLVED);
        }
        // Definir filtro
        else if (item.getItemId() == R.id.filter) {
            // Almacenamos el filtro actual de forma temporal para poder restaurarlo
            // en caso de que se pulse "cancelar".
            showDialog(DIALOG_FILTER, this.filterConfig.copyToBundle(null));
        }
        // Eliminar filtro
        else if (item.getItemId() == R.id.no_filter) {
            setFilterConfig(this.filterConfig.reset(this.roleSelected, this.userConfig.isUserWithVerifiers()));
            this.filterDialogBuilder.resetLayout();
            invalidateOptionsMenu();
            updateCurrentList(FIRST_PAGE);
        }
        // Habilitar/deshabilitar notificaciones
        else if (item.getItemId() == R.id.notifications) {
            // Si se ha solicitado activarlas...
            if (item.getTitle().equals(getString(R.string.enable_notifications))) {

                String userProxyId = getUserProxyId();

                // Al tratar de activar las notificaciones, registraremos el token de notificacion
                // si no esta ya registrado o si ha cambiado desde la ultima vez
                if (!isNotificationTokenRegistered(userProxyId) || isNotificationTokenChanged(userProxyId)) {
                    registryNotificationToken(userProxyId, true);
                }
                // Si el token de notificacion ya se habia registrado, simplemente cambiamos el estado
                else {
                    new UpdatePushNotificationsTask(true, this).execute();
                }
            }
            // Si se ha solicitado desactivarlas...
            else if (item.getTitle().equals(getString(R.string.disable_notifications))) {
                new UpdatePushNotificationsTask(false, this).execute();
            }
        }
        // Configuración de usuario
        else if (item.getItemId() == R.id.setting) {
            Intent intent = new Intent(this, UserConfigurationActivity.class);
            intent.putExtra(ConfigurationConstants.EXTRA_RESOURCE_ROLE_SELECTED, roleSelected);
            intent.putExtra(ConfigurationConstants.EXTRA_RESOURCE_USER_CONFIG, this.userConfig);
            intent.putExtra(SIGN_REQUEST_STATE_KEY, currentState);
            startActivityForResult(intent, UserConfigurationActivity.REQUEST_CODE);
        }
        // Vistas.
        else if (item.getItemId() == R.id.views) {
            String[] items = { getString(R.string.compact_view), getString(R.string.extended_view) };
            AlertDialog.Builder builder = new AlertDialog.Builder(PetitionListActivity.this);
            int checkedItem = AppPreferences.getInstance().getIsCompactView() ? 0 : 1;
            builder.setTitle(R.string.select_view);
            builder.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    if (i == 0 && !AppPreferences.getInstance().getIsCompactView()) {
                        AppPreferences.getInstance().setIsCompactView(true);
                    } else if (i == 1 && AppPreferences.getInstance().getIsCompactView()) {
                        AppPreferences.getInstance().setIsCompactView(false);
                    }

                    // Mostramos el listado de peticiones con la nueva vista seleccionada
                    getListView().setAdapter(preparePetitionList(
                            currentState, numPages > 1));

                    dialogInterface.cancel();
                }
            });

            if (!AppPreferences.getInstance().getIsCompactView()) {
                // Mostramos el listado de peticiones con la nueva vista seleccionada
                getListView().setAdapter(preparePetitionList(
                        currentState, numPages > 1));
            }

            builder.create();
            builder.show();
        }
        // Cambiar de rol.
        else if (item.getItemId() == R.id.changeRole) {
            Intent intent = new Intent(this, LoginWithRoleActivity.class);
            intent.putExtra(ConfigurationConstants.EXTRA_RESOURCE_USER_CONFIG, this.userConfig);
            intent.putExtra(PetitionListActivity.EXTRA_RESOURCE_DNI, this.dni);
            intent.putExtra(PetitionListActivity.EXTRA_RESOURCE_CERT_B64, this.certB64);
            intent.putExtra(PetitionListActivity.EXTRA_RESOURCE_CERT_ALIAS, this.certAlias);
            intent.putStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_IDS, new ArrayList<>(this.appIds));
            intent.putStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_NAMES, new ArrayList<>(this.appNames));
            intent.putExtra(ConfigurationConstants.EXTRA_RESOURCE_CLEAN_STACK, true);

            // Iniciamos la nueva actividad.
            startActivity(intent);
        }
        // Abrir ayuda
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
                        PERMISSION_TO_OPEN_HELP);
            }
        }
        // Cerrar sesion
        else if (item.getItemId() == R.id.logout) {
            showConfirmExitDialog();
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_TO_OPEN_HELP: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    PfLog.i(SFConstants.LOG_TAG, "Permisos concedidos para abrir el fichero de ayuda");
                    openHelp();
                } else {
                    Toast.makeText(
                            this,
                            getString(R.string.nopermtoopenhelp),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        }
    }

    /**
     * Abre el fichero de ayuda de la aplicaci&oacute;n.
     */
    private void openHelp() {
        OpenHelpDocumentTask task = new OpenHelpDocumentTask(this);
        task.execute();
    }

    /**
     * Actualiza la lista de peticiones que se muestra actualmente.
     */
    void updateCurrentList(final int page) {

        // Comprobamos que el usuario tiene permisos para realizar la operación.
        checkOperationPermissions(this.currentState, this.roleSelected);

        if (this.loadingTask == null || !this.loadingTask.isRunning()) {

            dismissProgressDialog();

            this.currentPage = page;

            // Configuramos los filtros
            final List<String> filters = ConfigureFilterDialogBuilder
                    .generateFilters(this.filterConfig);
            this.loadingTask = new LoadSignRequestsTask(
                    this.currentState, page, PAGE_SIZE, filters,
                    CommManager.getInstance(), this);
            showProgressDialog(getString(R.string.dialog_msg_loading_petitions), this, this.loadingTask);
            this.loadingTask.execute();

            // Se reinicia el contador de notificaciones para el servidor cargado
            AppPreferences.getInstance().setPreferenceInt(KEY_COUNT + AppPreferences.getInstance().getSelectedProxyUrl(), 0);
        }
    }

    /**
     * Método auxiliar que valida que el usuario tiene el rol correcto
     * para acceder a la lista de peticiones solicitada.
     *
     * @param currentState Tipo de lista solicitado.
     * @param role         Role con el que se ha autenticado el usuario.
     */
    private void checkOperationPermissions(String currentState, ConfigurationRole role) {
        if (role != null && role.equals(ConfigurationRole.VERIFIER)) {
            if (currentState.equals(SignRequest.STATE_REJECTED) || currentState.equals(SignRequest.STATE_SIGNED)) {
                throw new IllegalArgumentException("El usuario con rol de validador no permiso " +
                        "para acceder a la lista de peticiones solicitada."); //$NON-NLS-1$
            }
        }
    }

    /**
     * Cambia el listado de peticiones actual por el listado con las peticiones
     * con el estado indicado.
     *
     * @param stateSigned Estado de las peticiones que deben mostrarse.
     */
    private void changeCurrentRequestList(final String stateSigned) {
        if (this.loadingRequests && this.loadingTask != null) {
            dismissProgressDialog();
            this.loadingTask.cancel(true);
        }
        this.needReload = true;
        getIntent().putExtra(SIGN_REQUEST_STATE_KEY, stateSigned);
        recreate();
    }

    final void closeActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Selecciona o deselecciona todos los elementos del listado de peticiones en pantalla.
     *
     * @param selected {@code true} para seleccionar todas las peticiones,
     *                 {@code false} para deseleccionarlas todas.
     */
    private void selectAllRequests(final boolean selected) {

        // Seleccionamos cada elemento
        for (int i = 0; i < getListAdapter().getCount(); i++) {
            if (getListAdapter().getItem(i) instanceof PetitionElement) {
                ((PetitionElement) getListAdapter().getItem(i))
                        .getSignRequest().setSelected(selected);
            }
        }

        // Notificamos el cambio a la lista
        ((ArrayAdapter<?>) getListAdapter()).notifyDataSetChanged();

        // Notificamos el cambio a la pantalla
        onContentChanged();
    }

    @Override
    public void requestOperationFinished(final int operation,
                                         final RequestResult result) {

        synchronized (this) {

            if (operation == SIGN_OPERATION) {
                this.numRequestToSignPending--;
            } else if (operation == APPROVE_OPERATION) {
                this.numRequestToApprovePending--;
            } else if (operation == REJECT_OPERATION) {
                this.numRequestToRejectPending--;
            } else if (operation == VERIFY_OPERATION) {
                this.numRequestToVerifyPending--;
            }

            showResultIfFinished(operation);

        }
    }

    @Override
    public void requestOperationFailed(final int operation,
                                       final RequestResult result, final Throwable t) {

        synchronized (this) {

            this.anyError = true;

            if (operation == SIGN_OPERATION) {
                this.numRequestToSignPending--;
            } else if (operation == APPROVE_OPERATION) {
                this.numRequestToApprovePending--;
            } else if (operation == REJECT_OPERATION) {
                this.numRequestToRejectPending--;
            } else if (operation == VERIFY_OPERATION) {
                this.numRequestToVerifyPending--;
            }

            if (t != null) {
                PfLog.e(SFConstants.LOG_TAG, "Error al procesar las peticiones de firma", t); //$NON-NLS-1$
            }

            showResultIfFinished(operation);
        }
    }

    @Override
    public void requestOperationCancelled(int operation) {
        synchronized (this) {

            this.numRequestToSignPending = 0;
            this.numRequestToApprovePending = 0;
            this.numRequestToRejectPending = 0;
            this.numRequestToVerifyPending = 0;

            PfLog.i(SFConstants.LOG_TAG, "Operacion cancelada por el usuario"); //$NON-NLS-1$

            dismissProgressDialog();
        }
    }

    @Override
    protected void enabledNfcCancelled() {

        this.numRequestToSignPending = 0;
        Toast.makeText(getApplicationContext(), R.string.nfc_disabled_signing_requests, Toast.LENGTH_SHORT).show();

        showResultIfFinished(OperationRequestListener.SIGN_OPERATION);
    }

    /**
     * Comportamiento a seguir cuando han terminado el procesado de todas las operaciones.
     * @param lastOperation Tipo de la &uacute;ltima operaci&oacute;n realizada.
     */
    private void showResultIfFinished(int lastOperation) {
        if (this.numRequestToSignPending <= 0
                && this.numRequestToApprovePending <= 0
                && this.numRequestToRejectPending <= 0
                && this.numRequestToVerifyPending <= 0) {

            dismissProgressDialog();

            // Una vez finalizada una operacion, si se produjo algun error,
            // mostramos un dialogo informandolo. Si no, se recarga el listado
            if (this.anyError) {
                showProcessingError(lastOperation);
            } else {
                updateCurrentList(FIRST_PAGE);
            }
        }
    }

    private void showProcessingError(int operation) {
        String errorMsg;
        switch (operation) {
            case REJECT_OPERATION:
                errorMsg = getString(R.string.error_msg_rejecting_requests);
                break;
            case VERIFY_OPERATION:
                errorMsg = getString(R.string.error_msg_verifing_requests);
                break;
            default:
                errorMsg = getString(R.string.error_msg_procesing_requests);
        }
        PfLog.w(SFConstants.LOG_TAG, "Error: " + errorMsg); //$NON-NLS-1$
        showErrorDialog(errorMsg);
    }

    @Override
    public void onBackPressed() {
        // Preguntamos si debe cerrarse la sesion
        showConfirmExitDialog();
    }

    @Override
    public void onUpdatePushNotsSuccess(boolean requestedState, boolean result) {
        if (result) {
            if (requestedState) {
                Toast.makeText(this, R.string.toast_msg_push_activated, Toast.LENGTH_LONG).show(); //$NON-NLS-1$
                menuRef.findItem(R.id.notifications).setTitle(R.string.disable_notifications);
            } else {
                Toast.makeText(this, R.string.toast_msg_push_deactivated, Toast.LENGTH_LONG).show(); //$NON-NLS-1$
                menuRef.findItem(R.id.notifications).setTitle(R.string.enable_notifications);
            }
            this.userConfig.setPushActivated(requestedState);

        } else {
            Toast.makeText(this, R.string.toast_msg_push_error_changing_state, Toast.LENGTH_LONG).show(); //$NON-NLS-1$
            if (requestedState) {
                menuRef.findItem(R.id.notifications).setTitle(R.string.enable_notifications);
            } else {
                menuRef.findItem(R.id.notifications).setTitle(R.string.disable_notifications);
            }
        }
    }

    @Override
    public void onUpdatePushNotsError(boolean enable, Throwable exception) {
        PfLog.e(SFConstants.LOG_TAG, "No ha sido posible actualizar el estado de las notificaciones push: " + exception, exception); //$NON-NLS-1$ //$NON-NLS-2$
        Toast.makeText(this, R.string.toast_msg_update_push_nots_error, Toast.LENGTH_LONG).show();
        if (enable) {
            menuRef.findItem(R.id.notifications).setTitle(R.string.enable_notifications);
        } else {
            menuRef.findItem(R.id.notifications).setTitle(R.string.disable_notifications);
        }
    }

    /**
     * Muestra un mensaje al usuario pidiendo confirmacion para cerrar la
     * sesi&oacute;n del usuario.
     */
    private void showConfirmExitDialog() {

        final CustomAlertDialog dialog = CustomAlertDialog.newInstance(
                DIALOG_CONFIRM_EXIT,
                getString(R.string.dialog_title_close_session),
                getString(R.string.dialog_msg_close_session),
                getString(android.R.string.ok),
                getString(android.R.string.cancel),
                this);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    dialog.show(getSupportFragmentManager(), DIALOG_TAG);
                }
                catch (Exception e) {
                    PfLog.w(SFConstants.LOG_TAG, "No se pudo mostrar el dialogo de confirmacion de cierre de sesion", e); //$NON-NLS-1$
                }
            }
        });
    }

    @Override
    public void loadedSignRequest(final List<SignRequest> requests, final int pageNumber, final int numOfPages) {

        dismissProgressDialog();

        this.loadedRequests = requests;

        // Se termina la carga
        ((SwipeRefreshLayout) findViewById(R.id.swiperefresh)).setRefreshing(false);

        // Mostramos u ocultamos el texto de "No hay resultados" segun
        // corresponda
        final TextView emptyTextView = findViewById(R.id.empty);
        emptyTextView.setVisibility(requests == null || requests
                .size() == 0 ? View.VISIBLE : View.INVISIBLE);
        emptyTextView.setText(getString(R.string.no_request_avaible));


        // Ya no tenemos que recargar el listado
        this.needReload = false;

        // Guardamos el numero de paginas del listado
        this.numPages = numOfPages;

        // Mostramos el listado de peticiones
        getListView().setAdapter(preparePetitionList(
                this.currentState, numOfPages > 1));
    }

    /**
     * Preparamos las peticiones para mostrarlas, anteponiendo las proximas a caducar
     * y ajustando el layout de los datos.
     *
     * @param state          Estado de las peticiones (pendientes, procesadas o rechazadas).
     * @param needPagination Indica si debe mostrarse el elemento para la paginaci&oacute;n.
     * @return Listado de peticiones.
     */
    private PetitionListArrayAdapter preparePetitionList(final String state, final boolean needPagination) {

        final List<PetitionListAdapterItem> plAdapterItem = new ArrayList<>();

        // En el caso del listado de peticiones pendientes, situaremos al principio de la lista
        // las peticiones proximas a caducar. Para ello, las extraeremos de la lista, las
        // ordenaremos por fecha de caducidad y las agregaremos al principio del listado final

        //Segun el tipo de vista (compacta o expandida) cargamos el array adapter de una forma u otra
        int unresolvedRequestLayoutTypeAdapter;
        int resolvedRequestLayoutTypeAdapter;
        if (AppPreferences.getInstance().getIsCompactView()) {
            unresolvedRequestLayoutTypeAdapter = R.layout.array_adapter_unresolved_request;
            resolvedRequestLayoutTypeAdapter = R.layout.array_adapter_resolved_request;
        } else {
            unresolvedRequestLayoutTypeAdapter = R.layout.array_adapter_unresolved_request_extended;
            resolvedRequestLayoutTypeAdapter = R.layout.array_adapter_resolved_request_extended;
        }

        if (SignRequest.STATE_UNRESOLVED.equals(state)) {
            // Obtenemos el numero de dias para el cual consideramos una peticion que caduca proximamente
            int days = AppPreferences.getInstance().getPreferenceInt(KEY_DAYS_TO_EXPIRE, DEFAULT_DAYS_TO_EXPIRE);

            // Identificamos la fecha maxima de caducidad
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.add(Calendar.DATE, days);
            Date dateFuture = c.getTime();

            // Creamos una lista con las peticiones proximas a caducar
            List<SignRequest> priorityList = new ArrayList<>();
            for (int i = this.loadedRequests.size() - 1; i >= 0; i--) {
                SignRequest signRequest = this.loadedRequests.get(i);
                if (signRequest.getExpirationDate() != null) {
                    // Obtenemos la fecha de caducidad
                    Date date;
                    try {
                        date = dateFormat.parse(signRequest.getExpirationDate());
                    } catch (Exception e) {
                        PfLog.w(SFConstants.LOG_TAG, "Fecha de caducidad mal formada. Se ignorara: " + e);
                        continue;
                    }

                    // Comprobamos si la fecha de caducidad es menor que la fecha limite
                    if (dateFuture.compareTo(date) > -1) {
                        priorityList.add(signRequest);
                        this.loadedRequests.remove(i);
                    }
                }
            }

            // Si se han identificado fechas proximas a caducar, las ordenamos y las agregamos al listado final
            if (priorityList.size() > 0) {

                // Ordenamos
                Collections.sort(priorityList, new SignRequestComparator(dateFormat));

                // Insertamos la cabecera de peticiones proximas a caducar
                plAdapterItem.add(new PanelTitle(getString(R.string.exp_date_requests), this));

                // Insertamos en el listado las peticiones a punto de caducar
                for (final SignRequest request : priorityList) {
                    plAdapterItem.add(new PetitionElement(
                            request,
                            unresolvedRequestLayoutTypeAdapter,
                            this));
                }

                // Si hay fechas no proximas a caducar, agregamos otra cabecera. Esta es innecesaria,
                // si no las hubiese
                plAdapterItem.add(new PanelTitle(getString(R.string.reminding_requests), this));
            }
        }

        // Incluimos el resto de peticiones
        for (final SignRequest request : loadedRequests) {
            plAdapterItem.add(new PetitionElement(
                    request,
                    SignRequest.STATE_UNRESOLVED.equals(state) ?
                            unresolvedRequestLayoutTypeAdapter
                            : resolvedRequestLayoutTypeAdapter,
                    this));
        }

        // Agregamos pie de pagina si detectamos que es necesaria paginacion
        if (needPagination) {
            plAdapterItem.add(new PanelPaginationElement(this.numPages, this.currentPage, this));
        }

        return new PetitionListArrayAdapter(this, plAdapterItem);
    }

    @Override
    public void onItemClick(final AdapterView<?> adapter, final View view, final int position, final long id) {

        // Solo procesamos los click sobre los elementos de peticion, no sobre
        // la barra de paginacion
        if (((PetitionListAdapterItem) adapter.getItemAtPosition(position)).getViewType() ==
                PetitionListItemType.PETITION_ITEM.ordinal()) {

            final SignRequest signRequest = ((PetitionElement) adapter.getItemAtPosition(position))
                    .getSignRequest();
            signRequest.setViewed(true);
            showRequestDetails(signRequest.getId());
        }
    }

    /**
     * Abre un activity en donde muestra el detalle de la petici&oacute;n.
     *
     * @param requestId Identificador de la petici&oacute;n de la que se quiere ver el
     *                  detalle.
     */
    private void showRequestDetails(final String requestId) {

        final Intent changeActivityIntent = new Intent(this, PetitionDetailsActivity.class);
        changeActivityIntent.putExtra(PetitionDetailsActivity.EXTRA_RESOURCE_REQUEST_STATE, getCurrentState());
        changeActivityIntent.putExtra(PetitionDetailsActivity.EXTRA_RESOURCE_REQUEST_ID, requestId);
        if (this.dni != null) {
            changeActivityIntent.putExtra(PetitionDetailsActivity.EXTRA_RESOURCE_DNI, this.dni);
        }
        changeActivityIntent.putExtra(EXTRA_RESOURCE_CERT_B64, this.certB64);
        changeActivityIntent.putExtra(PetitionDetailsActivity.EXTRA_RESOURCE_CERT_ALIAS, this.certAlias);
        changeActivityIntent.putExtra(ConfigurationConstants.EXTRA_RESOURCE_ROLE_SELECTED, this.roleSelectedInfo);
        changeActivityIntent.putExtra(ConfigurationConstants.EXTRA_RESOURCE_USER_CONFIG, this.userConfig);
        changeActivityIntent.putStringArrayListExtra(EXTRA_RESOURCE_APP_IDS, new ArrayList<>(appIds));
        changeActivityIntent.putStringArrayListExtra(EXTRA_RESOURCE_APP_NAMES, new ArrayList<>(appNames));

        startActivityForResult(changeActivityIntent, PetitionDetailsActivity.REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PetitionDetailsActivity.REQUEST_CODE) {
            // Si se proceso le peticion correctamente actualizamos el listado
            if (resultCode == PetitionDetailsActivity.RESULT_SIGN_OK
                    || resultCode == PetitionDetailsActivity.RESULT_REJECT_OK
                    || resultCode == PetitionDetailsActivity.RESULT_VERIFY_OK) {
                updateCurrentList(FIRST_PAGE);
            }
            // Si ha caducado la sesion vuelve a la actividad principal
            else if (resultCode == PetitionDetailsActivity.RESULT_SESSION_FAILED) {
                this.lostSession();
            }
            // Si se ha cerrado la sesion, se vuelve a la actividad principal
            else if (resultCode == PetitionDetailsActivity.RESULT_SESSION_CLOSED) {
                this.closeSession();
            }
        }

        // Obtenemos la respuesta tras autorizar la operacion en Cl@ve Firma
        else if (requestCode == LoadKeyStoreFragmentActivity.WEBVIEW_REQUEST_CODE) {
            // Si la peticion ha sido correcta iniciamos la finalizacion de firma
            if (resultCode == RESULT_OK) {

                PfLog.i(SFConstants.LOG_TAG, "Se han cargado correctamente los datos en FIRe");

                FireSignTask signTask = new FireSignTask(this);
                signTask.execute();
            } else if (resultCode == RESULT_CANCELED) {
                PfLog.i(SFConstants.LOG_TAG, "Operacion de firma cancelada por el usuario");
                dismissProgressDialog();
            } else {
                PfLog.e(SFConstants.LOG_TAG, "Error al cargar los datos en FIRe"); //$NON-NLS-1$
                String errorType = data != null ? data.getStringExtra("type") : null; //$NON-NLS-1$

                PfLog.e(SFConstants.LOG_TAG, "Tipo de error: " + errorType);

                dismissProgressDialog();

                // TODO: Diferenciar segun tipo de error
                showToastMessage(getString(R.string.toast_msg_fire_comunication_ko));
            }
        } else if (requestCode == UserConfigurationActivity.REQUEST_CODE) {
            if (resultCode == ConfigurationConstants.ACTIVITY_RESULT_CODE_ACCESS_DENEGATED) {
                Toast toast = Toast.makeText(this, R.string.toast_error_access_denegated, Toast.LENGTH_LONG);
                toast.show();
            }
            // Si se ha cerrado la sesion, se vuelve a la actividad principal
            else if (resultCode == PetitionDetailsActivity.RESULT_SESSION_CLOSED) {
                this.closeSession();
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {

        menu.findItem(R.id.no_filter).setVisible(
                !FilterConfig.isDefaultConfig(this.filterConfig, this.roleSelected));

        // Mostramos el elemento para la seleccion o deseleccion de todas las
        // peticiones segun corresponda
        if (menu.findItem(R.id.select_all) != null) {
            menu.findItem(R.id.select_all).setVisible(!this.allSelected);
            menu.findItem(R.id.unselect_all).setVisible(this.allSelected);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void errorLoadingSignRequest() {

        dismissProgressDialog();

        // Se termina la carga
        ((SwipeRefreshLayout) findViewById(R.id.swiperefresh)).setRefreshing(false);

        // Ya no tenemos que recargar el listado
        this.needReload = false;

        // Mostramos texto de falta de peticiones
        final TextView emptyTextView = findViewById(R.id.empty);
        emptyTextView.setText(getString(R.string.error_msg_loading_requests));
        emptyTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void lostSession() {
        showToastMessage(ErrorManager.getErrorMessage(ErrorManager.ERROR_LOST_SESSION));
        this.closeActivity();
    }

    public void closeSession() {
        this.closeActivity();
    }

    @Override
    public void invalidCredentials() {
        showToastMessage(ErrorManager.getErrorMessage(ErrorManager.ERROR_INVALID_CREDENTIALS));
        this.closeActivity();
    }

    /**
     * Muestra un mensaje en un toast.
     *
     * @param message Mensaje a mostrar.
     */
    void showToastMessage(final String message) {
        dismissProgressDialog();
        this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(PetitionListActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(KEY_SAVEINSTANCE_NEED_RELOAD, this.needReload);
        outState.putBoolean(KEY_SAVEINSTANCE_LOADING, this.loadingRequests);

        outState.putSerializable(KEY_SAVEINSTANCE_FILTER_CONFIG, this.filterConfig);
    }

    ConfigureFilterDialogBuilder getFilterDialogBuilder() {
        return this.filterDialogBuilder;
    }

    @Override
    protected Dialog onCreateDialog(final int id,
                                    final Bundle savedInstanceState) {

        // Aqui solo deberiamos llegar cuando se usa el valor DIALOG_FILTER,
        // para crear el dialogo de configuracion de filtros
        this.filterDialogBuilder = new ConfigureFilterDialogBuilder(
                savedInstanceState, this.appIds.toArray(new String[0]),
                this.appNames.toArray(new String[0]), this.roleSelected, this.userConfig.isUserWithVerifiers(), this);
        this.filterDialogBuilder.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int identifier) {
                        setFilterConfig(getFilterDialogBuilder().getFilterConfig());
                        invalidateOptionsMenu();
                        updateCurrentList(FIRST_PAGE);
                    }
                });
        this.filterDialogBuilder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int identifier) {
                        restoreFilterConfigValues();
                        invalidateOptionsMenu();
                    }
                });

        return this.filterDialogBuilder.create();
    }

    /**
     * Method that replaces the values of the current filter configuration by its old values.
     */
    private void restoreFilterConfigValues() {
        if (this.filterConfig != null) {
            this.filterDialogBuilder.resetLayout(this.filterConfig);
        } else {
            this.filterDialogBuilder.resetLayout();
        }
    }

    @Override
    public void onDialogPositiveClick(final int dialogId, final String reason) {

        // Dialogo de confirmacion de cierre de sesion
        if (dialogId == DIALOG_CONFIRM_EXIT) {
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
            closeActivity();
        }
        // Dialogo de confirmacion de rechazo de peticiones.
        else if (dialogId == DIALOG_CONFIRM_REJECT) {

            final SignRequest[] selectedRequests = getSelectedRequests();
            if (selectedRequests == null || selectedRequests.length == 0) {
                // Esto no deberia ocurrir nunca, ya que se se comprobo anteriormente que
                // hubiese peticiones seleccionadas.
                showWarningDialog(getString(R.string.dialog_msg_no_selected_requests));
                return;
            }

            this.numRequestToRejectPending = selectedRequests.length;
            rejectRequests(reason, selectedRequests);

        }
        // Dialogo de confirmacion de firma de peticiones.
        else if (dialogId == DIALOG_CONFIRM_SIGN) {

            showProgressDialog(getString(R.string.dialog_msg_processing_requests), this);

            // Comprobamos que haya elementos seleccionados.
            final SignRequest[] requests = getSelectedRequests();
            if (requests == null || requests.length == 0) {
                dismissProgressDialog();
                showWarningDialog(getString(R.string.dialog_msg_no_selected_requests));
                return;
            }

            // Separamos entre peticiones de firma y de visto bueno.
            final List<SignRequest> requestToSign = new ArrayList<>();
            final List<SignRequest> requestToApprove = new ArrayList<>();
            final List<SignRequest> requestToVerify = new ArrayList<>();
            for (final SignRequest req : requests) {
                if (req.getType() == RequestType.SIGNATURE) {
                    requestToSign.add(req);
                } else if (req.getType() == RequestType.VERIFY) {
                    requestToVerify.add(req);
                } else {
                    requestToApprove.add(req);
                }
            }

            // Establecemos el valor bandera para identificar a posteriori los errores
            anyError = false;

            // Mandamos a aprobar las peticiones de visto bueno.
            this.numRequestToApprovePending = requestToApprove.size();
            if (this.numRequestToApprovePending > 0) {
                PfLog.i(SFConstants.LOG_TAG, "Peticiones para aprobar: " + this.numRequestToApprovePending); //$NON-NLS-1$
                approveRequests(requestToApprove.toArray(new SignRequest[0]));
            }

            // Mandamos a firmar las peticiones de firma.
            this.numRequestToSignPending = requestToSign.size();
            if (this.numRequestToSignPending > 0) {
                PfLog.i(SFConstants.LOG_TAG, "Peticiones para firmar: " + this.numRequestToSignPending); //$NON-NLS-1$
                signRequests(requestToSign.toArray(new SignRequest[0]));
            }

            // Mandamos a validar las peticiones de validación.
            this.numRequestToVerifyPending = requestToVerify.size();
            if (this.numRequestToVerifyPending > 0) {
                PfLog.i(SFConstants.LOG_TAG, "Peticiones para validar: " + this.numRequestToVerifyPending); //$NON-NLS-1$
                verifyRequests(requestToVerify.toArray(new SignRequest[0]));
            }
        }
        // Dialogo de error procesando peticiones
        else if (dialogId == DIALOG_ERROR) {
            // Actualizamos el listado
            updateCurrentList(FIRST_PAGE);
        }
    }

    /**
     * Manda a aprobar el listado de peticiones indicado. Llama a la
     * operaci&oacute;n de &eacute;xito o fallo del listener una vez por cada
     * petici&oacute;n.
     *
     * @param requests Listado de peticiones de visto bueno.
     */
    private void approveRequests(final SignRequest[] requests) {
        new ApproveRequestsTask(requests,
                CommManager.getInstance(), this).execute();
    }

    /**
     * Manda a validar el listado de peticiones indicado.
     * Llama a la operación de éxito o fallo del listener una vez por cada petición.
     *
     * @param requests Listado de peticiones de validación.
     */
    private void verifyRequests(final SignRequest[] requests) {
        new VerifyRequestsTask(requests, CommManager.getInstance(), this).execute();
    }

    /**
     * Manda a rechazar el listado de peticiones indicado.
     * Llama a la operación de &eacute;xito o fallo del listener una vez por cada petici&oacute;n.
     * @param reason Raz&oacute;n del rechazo.
     * @param signRequests Listado de peticiones de validaci&oacute;n.
     */
    protected RejectRequestsTask rejectRequests(final String reason, final SignRequest... signRequests) {
        // Antes de iniciar el proceso, reiniciamos la bandera que senala los errores
        anyError = false;
        final RejectRequestsTask rrt = new RejectRequestsTask(signRequests, CommManager.getInstance(), this, reason);
        showProgressDialog(getString(R.string.dialog_msg_processing_requests), this, rrt);
        rrt.execute();
        return rrt;
    }

    @Override
    public void onDialogNegativeClick(final int dialogId) {
        // No se implementa comportamiento
    }

    @Override
    protected void showProgressDialog(final String message, final Context ctx, final AsyncTask<?, ?, ?>... tasks) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                ProgressDialog currentProgressDialog = getProgressDialog();
                if (currentProgressDialog != null && currentProgressDialog.isShowing()) {
                    currentProgressDialog.dismiss();
                }

                try {
                    currentProgressDialog = ProgressDialog.show(ctx, null, message, true);
                    setProgressDialog(currentProgressDialog);
                } catch (final Exception e) {
                    PfLog.e(SFConstants.LOG_TAG, "No se ha podido mostrar el dialogo de progreso: " + e, e); //$NON-NLS-1$
                    return;
                }

                // Definimos el comportamiento para cancelar los dialogos de espera
                currentProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(final DialogInterface dialog, final int keyCode, final KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            if (tasks != null) {
                                for (AsyncTask<?, ?, ?> task : tasks) {
                                    if (task != null) {
                                        task.cancel(true);
                                    }
                                }
                            }
                            dialog.dismiss();
                            return true;
                        }
                        return false;
                    }
                });
            }
        });
    }

    @Override
    protected void dismissProgressDialog() {
        this.loadingRequests = false;
        if (getProgressDialog() != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getProgressDialog().dismiss();
                }
            });
        }
    }

    @Override
    void requestedSignatureSuccess(SignRequest[] signRequests) {
        synchronized (this) {
            this.numRequestToSignPending = 0;

            if (this.numRequestToApprovePending <= 0) {
                dismissProgressDialog();
                updateCurrentList(FIRST_PAGE);
            }
        }
    }

    @Override
    void requestedSignatureFailed(SignRequest[] signRequests, Throwable cause) {
        PfLog.e(SFConstants.LOG_TAG, "Ha fallado en la postfirma la operacion de firma con Cl@ve Firma", cause); //$NON-NLS-1$

        synchronized (this) {
            this.numRequestToSignPending = 0;

            if (this.numRequestToApprovePending <= 0) {

                dismissProgressDialog();

                final String errorMsg = getString(R.string.error_msg_procesing_requests);
                PfLog.w(SFConstants.LOG_TAG, "Error: " + errorMsg); //$NON-NLS-1$
                showErrorDialog(errorMsg);
            }
        }
    }

//    private void registerReceiver() {
//
//        // Comprobamos si el usuario ya esta registrado en el sistema de notificaciones
//
//        // TODO: Cuando este disponible un servicio para la baja en el sistema de notificaciones, lo usaremos
//        // Si el usuario ya esta registrado en el servicio de notificaciones, lo damos de baja
//
//        // Si aun no se ha registrado, lo hacemos
//        String userProxyId = getUserProxyId();
//        Intent intent = new Intent(this, RegistrationIntentService.class);
//        intent.putExtra(RegistrationIntentService.EXTRA_RESOURCE_DNI, this.dni);
//        intent.putExtra(RegistrationIntentService.EXTRA_RESOURCE_USER_PROXY_ID, userProxyId);
//        // Indicamos que se le debe mostrar al usuario un mensaje con el resultado del registro
//        intent.putExtra(RegistrationIntentService.EXTRA_RESOURCE_NOTICE_USER, true);
//
//        startService(intent);
//    }

    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, new IntentFilter("message"));
    }

    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bReceiver);
    }

    /**
     * Tipo de elemento de la lista de peticiones (petici&oacute;n, panel de
     * paginaci&oacute;n).
     */
    private enum PetitionListItemType {
        PETITION_ITEM, TITLE_ITEM, PAGINATION_PANEL
    }

    /**
     * Interfaz que implementan los tipos de elemento que componen la lista de
     * peticiones
     */
    private interface PetitionListAdapterItem {
        int getViewType();

        View getView(LayoutInflater inflater, View convertView,
                     int position);
    }

    private class PetitionElement implements PetitionListAdapterItem {

        final SignRequest request;
        private final int layoutId;
        private final Context context;

        public PetitionElement(final SignRequest signRequest, final int layoutResId, final Context context) {
            this.request = signRequest;
            this.layoutId = layoutResId;
            this.context = context;
        }

        @Override
        public int getViewType() {
            return PetitionListItemType.PETITION_ITEM.ordinal();
        }

        @Override
        public View getView(final LayoutInflater inflater, final View convertView,
                            final int position) {

            View v = convertView;

            if (v == null) {
                final LayoutInflater vi = (LayoutInflater) this.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(this.layoutId, null);
            }

            if (this.request != null) {

                final ImageView icon = v
                        .findViewById(R.id.priorityIcon);
                final TextView primaryText = v
                        .findViewById(R.id.primaryText);
                final TextView secondaryText = v
                        .findViewById(R.id.secondaryText);
                final ImageView typeIcon = v
                        .findViewById(R.id.typeIcon);
                final TextView dateText = v
                        .findViewById(R.id.dateText);

                primaryText.setText(this.request.getSender());

                if (this.request.getSubject() != null) {
                    secondaryText.setText(this.request.getSubject());
                }

                if (dateText != null && this.request.getDate() != null) {
                    dateText.setText(this.request.getDate());
                }

                // Cada tiene un icono
                if (this.request.getPriority() > 0
                        && this.request.getPriority() <= 4) {
                    int iconResourceId;
                    if (this.request.getPriority() == 1) {
                        // Icono en transparente
                        iconResourceId = R.drawable.icon_priority_1;
                    } else if (this.request.getPriority() == 2) {
                        // Icono amarillo
                        iconResourceId = R.drawable.icon_priority_2;
                    } else if (this.request.getPriority() == 3) {
                        // Icono naranja
                        iconResourceId = R.drawable.icon_priority_3;
                    } else {
                        // Icono rojo
                        iconResourceId = R.drawable.icon_priority_4;
                    }
                    icon.setImageResource(iconResourceId);
                }

                if (SignRequest.VIEW_NEW.equals(this.request.getView())) {
                    v.setBackgroundResource(R.drawable.array_adapter_selector_grey);
                    primaryText.setTextAppearance(this.context, R.style.NewRequestPrimaryText);
                    secondaryText.setTextAppearance(this.context, R.style.NewRequestSecondaryText);
                    if (dateText != null) {
                        dateText.setTextAppearance(this.context, R.style.NewRequestPrimaryText);
                    }
                } else if (SignRequest.VIEW_READED.equals(this.request.getView())) {
                    v.setBackgroundResource(R.drawable.array_adapter_selector_white);
                    primaryText.setTextAppearance(this.context, R.style.ReadedRequestPrimaryText);
                    secondaryText.setTextAppearance(this.context, R.style.ReadedRequestSecondaryText);
                    if (dateText != null) {
                        dateText.setTextAppearance(this.context, R.style.ReadedRequestPrimaryText);
                    }
                }

                // Icono diferente dependiendo de si la peticion es de fima o de
                // visto bueno
                if (this.request.getType() == RequestType.APPROVE) {
                    typeIcon.setImageResource(R.drawable.icon_vb);
                } else if (this.request.getType() == RequestType.SIGNATURE) {
                    typeIcon.setImageResource(R.drawable.icon_sign);
                }

                final CheckBox check = v.findViewById(R.id.check);
                if (check != null) {
                    // Si se pulsa el checkbutton, se selecciona/deselecciona el
                    // elemento
                    check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(final CompoundButton arg0,
                                                     final boolean checked) {
                            PetitionElement.this.request.setSelected(checked);
                        }
                    });
                    check.setChecked(this.request.isSelected());
                }

            }

            return v;
        }

        private SignRequest getSignRequest() {
            return this.request;
        }
    }

    /**
     * Adaptador para la lista de l&iacute;neas de firma.
     */
    static class PetitionListArrayAdapter extends ArrayAdapter<PetitionListAdapterItem> {

        private final LayoutInflater mInflater;

        private PetitionListArrayAdapter(final Context context, final List<PetitionListAdapterItem> objects) {
            super(context, 0, objects);
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getViewTypeCount() {
            return PetitionListItemType.values().length;
        }

        @Override
        public int getItemViewType(final int position) {
            return getItem(position) != null ? getItem(position).getViewType() : 0;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            return getItem(position).getView(this.mInflater, convertView, position);
        }
    }

    private class PanelPaginationElement implements PetitionListAdapterItem {

        final int nPages;
        final int page;
        final Context context;

        private PanelPaginationElement(final int numPages, final int currentPage, final Context context) {
            this.nPages = numPages;
            this.page = currentPage;
            this.context = context;
        }

        @Override
        public int getViewType() {
            return PetitionListItemType.PAGINATION_PANEL.ordinal();
        }

        @Override
        public View getView(final LayoutInflater inflater, final View convertView, final int position) {

            View paginationView = convertView;
            if (paginationView == null) {
                paginationView = ((LayoutInflater) this.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.array_adapter_panel_paginacion, null);
            }

            View pagButton = paginationView.findViewById(R.id.arrowFirst);
            if (pagButton != null) {
                pagButton.setVisibility(this.page == FIRST_PAGE ? View.INVISIBLE : View.VISIBLE);
                pagButton.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(final View v) {
                                v.setSelected(true);
                                updateCurrentList(FIRST_PAGE);
                            }
                        });
            }

            pagButton = paginationView.findViewById(R.id.arrowLeft);
            if (pagButton != null) {
                pagButton.setVisibility(this.page == FIRST_PAGE ? View.INVISIBLE : View.VISIBLE);
                pagButton.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(final View v) {
                                v.setSelected(true);
                                updateCurrentList(PanelPaginationElement.this.page - 1);
                            }
                        });
            }

            pagButton = paginationView.findViewById(R.id.arrowRigth);
            if (pagButton != null) {
                pagButton.setVisibility(this.page == this.nPages ? View.INVISIBLE : View.VISIBLE);
                pagButton.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(final View v) {
                                v.setSelected(true);
                                updateCurrentList(PanelPaginationElement.this.page + 1);
                            }
                        });
            }

            pagButton = paginationView.findViewById(R.id.arrowLast);
            if (pagButton != null) {
                pagButton.setVisibility(this.page == this.nPages ? View.INVISIBLE : View.VISIBLE);
                pagButton.setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(final View v) {
                                v.setSelected(true);
                                updateCurrentList(PanelPaginationElement.this.nPages);
                            }
                        });
            }

            TextView pagText = ((TextView) paginationView.findViewById(R.id.paginationText));
            if (pagText != null) {
                pagText.setText(
                        getString(R.string.pagination_separator, this.page, this.nPages));
            }

            return paginationView;
        }
    }

    private static class PanelTitle implements PetitionListAdapterItem {

        final String text;
        final Context context;

        private PanelTitle(final String text, final Context context) {
            this.text = text;
            this.context = context;
        }

        @Override
        public int getViewType() {
            return PetitionListItemType.TITLE_ITEM.ordinal();
        }

        @Override
        public View getView(final LayoutInflater inflater, final View convertView, final int position) {

            View titleView = convertView;
            if (titleView == null) {
                titleView = ((LayoutInflater) this.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.array_adapter_header, null);
            }

            ((TextView) titleView.findViewById(R.id.header)).setText(this.text);

            return titleView;
        }
    }

    /**
     * Clase para la comparacion de peticiones por fecha de caducidad.
     */
    private static class SignRequestComparator implements Comparator<SignRequest> {

        DateFormat dateFormat;

        private SignRequestComparator(DateFormat dateFormat) {
            this.dateFormat = dateFormat;
        }

        @Override
        public int compare(final SignRequest object1, final SignRequest object2) {
            Date expDate1;
            Date expDate2;
            try {
                expDate1 = this.dateFormat.parse(object1.getExpirationDate());
                expDate2 = this.dateFormat.parse(object2.getExpirationDate());
                return expDate1.compareTo(expDate2);
            } catch (ParseException e) {
                return object1.getExpirationDate().compareTo(object2.getExpirationDate());
            }
        }
    }
}