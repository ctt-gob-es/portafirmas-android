package es.gob.afirma.android.signfolder.activities;

import android.Manifest;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.security.KeyChain;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import es.gob.afirma.android.crypto.AuthenticationResult;
import es.gob.afirma.android.crypto.LoadKeyStoreManagerTask;
import es.gob.afirma.android.crypto.MobileKeyStoreManager.PrivateKeySelectionListener;
import es.gob.afirma.android.crypto.NfcHelper;
import es.gob.afirma.android.fcm.NotificationUtilities;
import es.gob.afirma.android.signfolder.AppPreferences;
import es.gob.afirma.android.signfolder.BuildConfig;
import es.gob.afirma.android.signfolder.ConfigureFilterDialogBuilder;
import es.gob.afirma.android.signfolder.LoginOptionsDialogBuilder;
import es.gob.afirma.android.signfolder.LoginOptionsDialogBuilder.LoginOptionsListener;
import es.gob.afirma.android.signfolder.MessageDialog;
import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.RequestAppConfiguration;
import es.gob.afirma.android.signfolder.tasks.LoadConfigurationDataTask;
import es.gob.afirma.android.signfolder.tasks.LoadUserConfigTask;
import es.gob.afirma.android.signfolder.tasks.LoginRequestValidationTask;
import es.gob.afirma.android.signfolder.tasks.OpenHelpDocumentTask;
import es.gob.afirma.android.user.configuration.ApplicationFilter;
import es.gob.afirma.android.user.configuration.ConfigurationConstants;
import es.gob.afirma.android.user.configuration.UserConfig;
import es.gob.afirma.android.util.Base64;
import es.gob.afirma.android.util.PfLog;

/**
 * Actividad para entrada con usuario y contraseña al servicio de Portafirmas.
 */
public final class LoginActivity extends AuthenticationFragmentActivity implements PrivateKeySelectionListener,
        LoginOptionsListener,
        LoadConfigurationDataTask.LoadConfigurationListener,
        LoadUserConfigTask.LoadUserConfigListener {

    private final static String EXTRA_RESOURCE_TITLE = "es.gob.afirma.signfolder.title"; //$NON-NLS-1$
    private final static String EXTRA_RESOURCE_EXT = "es.gob.afirma.signfolder.exts"; //$NON-NLS-1$

    private final static String CERTIFICATE_EXTS = ".p12,.pfx"; //$NON-NLS-1$

    private final static int SELECT_CERT_REQUEST_CODE = 1;

    private final static int PERMISSION_TO_BROWSE_FILE = 21;

    private final static int PERMISSION_TO_OPEN_HELP = 22;

    /** Di&aacute;logo para mostrar mensajes al usuario. */
    private MessageDialog messageDialog = null;
    /** Di&aacute;logo de espera. */
    private ProgressDialog progressDialog = null;

    private boolean notificationTokenChecked = false;


    boolean nfcAvailable = true;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle(getString(R.string.app_title, BuildConfig.VERSION_NAME));

        // Inicializamos si es necesario el listado de servidores proxy por defecto
        AppPreferences prefs = AppPreferences.getInstance();
        List<String> servers = prefs.getServersList();
        if (servers.size() == 0) {
            prefs.setDefaultServers();
        }

        //Comprobamos si la conectividad a la red es correcta
        final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conMgr == null || conMgr.getActiveNetworkInfo() == null ||
                !conMgr.getActiveNetworkInfo().isAvailable() ||
                !conMgr.getActiveNetworkInfo().isConnected()) {
            //Error en la conexion
            showErrorDialog(getString(R.string.error_msg_check_connection));
        }

        // Comprobamos si el dispositivo cuenta con NFC
        nfcAvailable = NfcHelper.isNfcServiceAvailable(this);

        // Una vez, tras el inicio de la aplicacion, obtenemos el token para el envio de
        // notificaciones a la aplicacion y lo registramos
        if (!notificationTokenChecked) {
            NotificationUtilities.registerCurrentToken();
            notificationTokenChecked = true;
        }

        // Comprobamos si tenemos que mostrar algún mensaje de error.
        String errorMsg = getIntent().getStringExtra(ConfigurationConstants.EXTRA_RESOURCE_ERROR_MSG);
        if (errorMsg != null) {
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
        }
    }

    MessageDialog getMessageDialog() {
        return this.messageDialog;
    }

    ProgressDialog getProgressDialog() {
        return this.progressDialog;
    }

    void setProgressDialog(final ProgressDialog pd) {
        this.progressDialog = pd;
    }

    /**
     * M&eacute;todo ejecutado al hacer clic sobre el bot&oacute;n Acceder.
     * @param v Vista sobre la que se hace clic.
     */
    public void onClickAccessButton(final View v) {
        access();
    }

    /**
     * Inicia el acceso de usuario.
     */
    private void access() {
        // Reiniciamos la conexion con el servicio proxy
        // y comprobamos que tenemos conexion con el
        CommManager.resetConfig();
        if (!CommManager.getInstance().verifyProxyUrl()) {
            showErrorDialog(getString(R.string.error_msg_proxy_no_config));
            return;
        }

        authenticate();
    }

    /**
     * M&eacute;todo ejecutado al hacer clic sobre el bot&oacute;n Acceder.
     * @param v Vista sobre la que se hace clic.
     */
    public void onClickImportCertButton(final View v) {
        // En Android 11 y superiores cargamos directamente el almacen usando el dialogo que lo
        // permite. En anteriores nos aseguramos de pedir permisos
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            browseKeyStore();
        }
        else {
            boolean storagePerm = (
                    ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
            );

            if (storagePerm) {
                browseKeyStore();
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_TO_BROWSE_FILE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_TO_BROWSE_FILE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    PfLog.i(SFConstants.LOG_TAG, "Permisos condedicos para abrir el explorador de ficheros");
                    browseKeyStore();
                } else {
                    Toast.makeText(
                            this,
                            getString(R.string.nopermtobrowsekeystore),
                            Toast.LENGTH_LONG
                    ).show();
                }
                break;
            }
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
                break;
            }
        }
    }

    /**
     * Carga el almacen de certificados del sistema. Se configura desde el layout para su ejecucion.
     */
    public void loadKeyStore() {

        LoadKeyStoreManagerTask lksmt = new LoadKeyStoreManagerTask(this, this);
        showProgressDialog(getString(R.string.dialog_msg_accessing_keystore), this, lksmt);
        lksmt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Abre un activity para la seleccion de un fichero PKCS#12 local.
     */
    public void browseKeyStore() {

        Intent intent;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setTypeAndNormalize("application/x-pkcs12"); //$NON-NLS-1$
        }
        else {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setClass(this, FileChooserActivity.class);
            intent.putExtra(EXTRA_RESOURCE_TITLE, getString(R.string.title_activity_cert_chooser));
            intent.putExtra(EXTRA_RESOURCE_EXT, CERTIFICATE_EXTS);
        }
        startActivityForResult(intent, SELECT_CERT_REQUEST_CODE);
    }

    @Override
    protected void authenticateWithCertificate(String alias, byte[] certEncoded, KeyStore.PrivateKeyEntry keyEntry) {
        try {
            // Proxy nuevo (v2.2)
            final LoginRequestValidationTask lrvt = new LoginRequestValidationTask(
                    Base64.encode(certEncoded),
                    alias,
                    CommManager.getInstance(),
                    this,
                    keyEntry);
            showProgressDialog(getString(R.string.dialog_msg_connecting), this, lrvt);
            lrvt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (IllegalArgumentException e) {
            // Proxy antiguo sin autenticacion (se valida cada peticion independiente)
            CommManager.getInstance().setOldProxy();
            final AuthenticationResult authResult = new AuthenticationResult(true);
            authResult.setCertificateB64(Base64.encode(certEncoded));
            authResult.setCertAlias(alias);
            loadConfiguration(authResult);
        } catch (Exception e) {
            PfLog.e(SFConstants.LOG_TAG, "Error al autenticar al usuario", e);
            // Error al conectar con el servidor
            showErrorDialog(getString(R.string.error_msg_communicating_server));
        }
    }

    // Definimos el menu de opciones de la aplicacion
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_login_options_menu, menu);

        // Comprobamos que origen de certificados esta configurado
        String certKeyStore = AppPreferences.getInstance().getCertKeyStore();

        // Si el dispositivo no tuviese NFC, desactivamos la opcion de DNIe. Ademas, si estuviese
        // configurado el uso de DNIe (cosa que no deberia ocurrir), se cambiaria para el uso de
        // certificados locales
        if (!nfcAvailable) {

            MenuItem item = menu.findItem(R.id.dnie);
            item.setVisible(false);

            if (AppPreferences.KEYSTORE_DNIE.equals(certKeyStore)) {
                certKeyStore = AppPreferences.KEYSTORE_LOCAL;
                AppPreferences.getInstance().setCertKeyStore(certKeyStore);
            }
        }

        // Activamos el almacen de claves configurado
        if (AppPreferences.KEYSTORE_CLOUD.equals(certKeyStore)) {
            MenuItem item = menu.findItem(R.id.clavefirma);
            item.setChecked(true);
        }
        else if (AppPreferences.KEYSTORE_DNIE.equals(certKeyStore)) {
            MenuItem item = menu.findItem(R.id.dnie);
            item.setChecked(true);
        } else {
            MenuItem item = menu.findItem(R.id.local_keystore);
            item.setChecked(true);
        }
        return true;
    }

    // Definimos que hacer cuando se pulsa una opcion del menu de opciones de la aplicacion
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Acceso a la configuracion
        if (item.getItemId() == R.id.configuration) {

            final LoginOptionsDialogBuilder dialogBuilder = new LoginOptionsDialogBuilder(this, this);
            dialogBuilder.show();
        }
        // Configurar el uso de Clave/FIRe
        else if (item.getItemId() == R.id.local_keystore) {
            if (!item.isChecked()) {
                item.setChecked(true);
            }
            AppPreferences.getInstance().setCertKeyStore(AppPreferences.KEYSTORE_LOCAL);
        }
        // Configurar el uso de Clave/FIRe
        else if (item.getItemId() == R.id.clavefirma) {
            if (!item.isChecked()) {
                item.setChecked(true);
            }
            AppPreferences.getInstance().setCertKeyStore(AppPreferences.KEYSTORE_CLOUD);
        }
        // Configurar el uso de DNIe
        else if (item.getItemId() == R.id.dnie) {
            if (!item.isChecked()) {
                item.setChecked(true);
            }
            AppPreferences.getInstance().setCertKeyStore(AppPreferences.KEYSTORE_DNIE);
        }
        // Abrir ayuda
        else if (item.getItemId() == R.id.help) {

            // En Android 11 y superiores guardamos la ayuda en el directorio de cache directamente.
            // En anteriores nos aseguramos de pedir permisos.
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                openHelp();
            }
            else {
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
        }
        return true;
    }

    /**
     * Abre el fichero de ayuda de la aplicaci&oacute;n.
     */
    private void openHelp() {
        PfLog.i(SFConstants.LOG_TAG, "Abrimos el fichero de ayuda");
        OpenHelpDocumentTask task = new OpenHelpDocumentTask(this);
        task.execute();
    }

    //metodo vacio para evitar bugs en versiones superiores al api11
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * Muestra un mensaje de advertencia al usuario.
     *
     * @param message Mensaje que se desea mostrar.
     */
    private void showErrorDialog(final String message) {
        showErrorDialog(message, null);
    }

    /**
     * Muestra un mensaje de advertencia al usuario.
     *
     * @param message Mensaje que se desea mostrar.
     */
    private void showErrorDialog(final String message, final String title) {

        dismissProgressDialog();

        if (this.messageDialog == null) {
            this.messageDialog = new MessageDialog();
        }
        this.messageDialog.setMessage(message);
        this.messageDialog.setContext(this);
        if (title != null) {
            this.messageDialog.setTitle(title);
        }

        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        getMessageDialog().show(getSupportFragmentManager(), "ErrorDialog"); //$NON-NLS-1$;
                    } catch (Exception e) {
                        PfLog.e(SFConstants.LOG_TAG, "No se ha podido mostrar el mensaje de error: " + e, e); //$NON-NLS-1$
                        Toast.makeText(LoginActivity.this.getApplicationContext(), getMessageDialog().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (final Exception e2) {
            PfLog.e(SFConstants.LOG_TAG, "Error en el hilo que muestra el mensaje de error: " + e2); //$NON-NLS-1$
        }
    }

    /**
     * Muestra un di&aacute;logo de espera con un mensaje.
     */
    @Override
    protected void showProgressDialog(final String message, final Context ctx, final AsyncTask<?, ?, ?>... tasks) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    setProgressDialog(ProgressDialog.show(ctx, null, message, true));
                } catch (final Exception e) {
                    PfLog.e(SFConstants.LOG_TAG, "No se ha podido mostrar el dialogo de progreso: " + e, e); //$NON-NLS-1$
                    return;
                }

                // Definimos el comportamiento para cancelar los dialogos de espera
                getProgressDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
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
                            dismissProgressDialog();
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
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_CERT_REQUEST_CODE && resultCode == RESULT_OK && data != null) {

            byte[] certContent;
            String filename = null;
            try {
                if (data.getStringExtra(FileChooserActivity.RESULT_DATA_STRING_FILENAME) != null) {
                    final String path = data.getStringExtra(FileChooserActivity.RESULT_DATA_STRING_FILENAME);
                    File certFile = new File(path);
                    filename = certFile.getName();
                    certContent = readDataFromFile(certFile);
                } else {
                    final Uri dataUri = data.getData();
                    filename = dataUri.getLastPathSegment();
                    certContent = readDataFromUri(dataUri);
                }
            } catch (final IOException e) {
                showErrorDialog(getString(R.string.error_loading_selected_file, filename));
                PfLog.e(SFConstants.LOG_TAG, "Error al cargar el fichero: " + e, e); //$NON-NLS-1$
                return;
            }

            final Intent intent = KeyChain.createInstallIntent();
            intent.putExtra(KeyChain.EXTRA_PKCS12, certContent);
            startActivity(intent);
        }
        else if (requestCode == OpenHelpDocumentTask.SHOW_HELP) {
            // A la vuelta de mostrar el fichero de ayuda, lo borramos
            try {
                String helpUrl = AppPreferences.getInstance().getHelpUrl();
                String helpFilename = Uri.parse(helpUrl).getLastPathSegment();
                File helpFile = new File(
                        getExternalCacheDir(),
                        helpFilename);
                if (helpFile.exists()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Files.delete(helpFile.toPath());
                    }
                    else {
                        if (!helpFile.delete()) {
                            PfLog.w(SFConstants.LOG_TAG, "No se pudo eliminar el fichero de ayuda");
                        }
                    }
                }
            }
            catch (Exception e) {
                PfLog.w(SFConstants.LOG_TAG, "No se pudo eliminar el fichero de ayuda", e);
                return;
            }
        }
    }

    @Override
    protected void enabledNfcCancelled() {
        dismissProgressDialog();
        Toast.makeText(getApplicationContext(), R.string.nfc_still_disabled, Toast.LENGTH_SHORT).show();
    }

    private byte[] readDataFromFile(File dataFile) throws IOException {
        int n;
        final byte[] buffer = new byte[1024];
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (final InputStream is = new FileInputStream(dataFile)) {
            while ((n = is.read(buffer)) > 0) {
                baos.write(buffer, 0, n);
            }
        }
        return baos.toByteArray();
    }

    private byte[] readDataFromUri(Uri uri) throws IOException {
        int n;
        final byte[] buffer = new byte[1024];
        final ByteArrayOutputStream baos;
        try (InputStream is = getContentResolver().openInputStream(uri)) {
            baos = new ByteArrayOutputStream();
            while ((n = is.read(buffer)) > 0) {
                baos.write(buffer, 0, n);
            }
        }
        return baos.toByteArray();
    }

    @Override
    public void onErrorLoginOptions(final String url) {
        try {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getBaseContext(), url, Toast.LENGTH_LONG).show();
                }
            });
        } catch (final Exception e) {
            PfLog.e(SFConstants.LOG_TAG, "No se ha podido mostrar el mensaje de error por configuracion incorrecta", e); //$NON-NLS-1$
        }
    }

    @Override
    public void processAuthenticationResult(AuthenticationResult result) {

        if (result.isStatusOk()) {
            // Cargamos la configuracion
            loadConfiguration(result);
        }
        else if (!result.isCancelled()) {
            String errMsg = result.getErrorMsg();
            if (errMsg == null || errMsg.isEmpty()) {
                errMsg = getString(R.string.error_loading_app_configuration);
            }
            // Si la aplicacion contiene un mensaje de excepcion o si trata de mostrar una URL
            // nos aseguramos de no mostrarselo a los usuarios
            else if (errMsg.contains("Exception:") || errMsg.toLowerCase(Locale.US).contains(SCHEME_SEPARATOR)) {
                errMsg = getString(R.string.error_loading_app_configuration);
            }
            showErrorDialog(errMsg);
        }
    }

    private static final String SCHEME_SEPARATOR = "://";

    /**
     * @param loginResult Resultado del proceso de login.
     */
    public void loadConfiguration(AuthenticationResult loginResult) {
        LoadUserConfigTask task = new LoadUserConfigTask(loginResult,this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void autheticateWithClave(int resultCode, Intent data) {

        dismissProgressDialog();

        if (resultCode == RESULT_OK) {
            PfLog.i(SFConstants.LOG_TAG, "Cl@ve autentico correctamente al usuario"); //$NON-NLS-1$

            // Recuperamos el DNI para poder utilizarlo en futuras operaciones
            String dni = data != null ? data.getStringExtra("dni") : null; //$NON-NLS-1$
            if (dni == null) {
                showErrorDialog(getString(R.string.error_logging_no_dni));
            } else {
                AuthenticationResult authResult = new AuthenticationResult(true);
                authResult.setDni(dni);
                loadConfiguration(authResult);
            }
        } else if (resultCode == RESULT_CANCELED) {
            PfLog.i(SFConstants.LOG_TAG, "Operacion de autenticacion con Cl@ve cancelada por el usuario");
        } else {
            PfLog.e(SFConstants.LOG_TAG, "Error al autenticar al usuario con Cl@ve"); //$NON-NLS-1$
            String errorType = data != null ? data.getStringExtra("type") : null; //$NON-NLS-1$
            String errorErrorMsg = data != null ? data.getStringExtra("msg") : null; //$NON-NLS-1$

            PfLog.e(SFConstants.LOG_TAG, "Tipo de error: " + errorType);
            PfLog.e(SFConstants.LOG_TAG, "Mensaje de error: " + errorErrorMsg);

            // Mostramos un mensaje u otro segun el tipo de error
            if ("validation".equals(errorType)) {
                showErrorDialog(getString(R.string.error_logging_with_clave));
            } else if ("claveerror".equals(errorType)) {
                showErrorDialog(getString(R.string.error_logging_clave_connection));
            } else {
                showErrorDialog(getString(R.string.error_logging_with_clave));
            }
        }
    }

    @Override
    public void userConfigLoadSuccess(UserConfig userConfig, AuthenticationResult loginResult) {
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        if (loginResult != null && loginResult.getDni() != null) {
            intent.putExtra(PetitionListActivity.EXTRA_RESOURCE_DNI, loginResult.getDni());
        }
        if (loginResult != null && loginResult.getCertAlias() != null) {
            intent.putExtra(PetitionListActivity.EXTRA_RESOURCE_CERT_ALIAS, loginResult.getCertAlias());
        }

        intent.putExtra(ConfigurationConstants.EXTRA_RESOURCE_USER_CONFIG, userConfig);

        // Extraemos la lista de aplicaciones y lo pasamos como un extra adicional.
        ArrayList<String> appIds = new ArrayList<>();
        ArrayList<String> appNames = new ArrayList<>();
        getIdsAndNameFromApps(userConfig.getUserFilers().getApplicationFilters(), appIds, appNames);
        intent.putStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_IDS, appIds);
        intent.putStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_NAMES, appNames);

        dismissProgressDialog();

        // Si el usuario tiene roles disponibles, llamamos primero a la actividad encargada de
        // gestionar la selección de roles.
        if (userConfig.getRoles() != null && !userConfig.getRoles().isEmpty()) {
            intent.setClass(this, LoginWithRoleActivity.class);
        } else {
            intent.setClass(this, PetitionListActivity.class);
        }

        startActivity(intent);
    }

    /**
     * Método auxiliar encargado de extraer los IDs y los nombres de las aplicaciones en 2 listas separadas.
     * @param src Lista de aplicaciones del tipo ApplicationFilter.
     * @param appIds Lista de IDs a completar.
     * @param appNames Lista de nombres a completar.
     */
    private void getIdsAndNameFromApps(List<ApplicationFilter> src, ArrayList<String> appIds, ArrayList<String> appNames) {
        if (src != null) {
            for (ApplicationFilter app : src) {
                appIds.add(app.getId());
                appNames.add(app.getName());
            }
        }
    }

    @Override
    public void userConfigLoadError(AuthenticationResult loginResult, Throwable t) {

        // Si ha fallada la optencion de la configuracion del usuario, interpretaremos que se debe
        // a que estamos ante un proxy que no soporta esa operacion. En ese caso, llamamos a la
        // operacion antigua de obtencion de la configuracion general (listado de aplicaciones)

        final LoadConfigurationDataTask lcdt = new LoadConfigurationDataTask(
                loginResult,
                CommManager.getInstance(),
                this,
                this);
        lcdt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void configurationLoadSuccess(final RequestAppConfiguration appConfig, final AuthenticationResult loginResult) {
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        if (loginResult != null && loginResult.getDni() != null) {
            intent.putExtra(PetitionListActivity.EXTRA_RESOURCE_DNI, loginResult.getDni());
        }
        if (loginResult != null && loginResult.getCertAlias() != null) {
            intent.putExtra(PetitionListActivity.EXTRA_RESOURCE_CERT_ALIAS, loginResult.getCertAlias());
        }
        intent.putStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_IDS, appConfig.getAppIdsList());
        intent.putStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_NAMES, appConfig.getAppNamesList());

        // Almacenamos la lista de aplicaciones, con su correspondiente numero asociado al picker
        ConfigureFilterDialogBuilder.updateApps(appConfig.getAppNamesList());

        intent.setClass(this, PetitionListActivity.class);
        startActivity(intent);
    }

    @Override
    public void configurationLoadError(final Throwable t) {
        dismissProgressDialog();
        if (t == null) {
            // Error en la conexion
            showErrorDialog(getString(R.string.error_loading_app_configuration), getString(R.string.error_loading_app_configuration_title));
        } else {
            // Error en las credenciales
            PfLog.w(SFConstants.LOG_TAG, "Error durante el proceso de login", t);
            showErrorDialog(getString(R.string.error_account_not_validated), getString(R.string.error_account_not_validated_title));
        }
    }
}
