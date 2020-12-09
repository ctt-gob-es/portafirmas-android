package es.gob.afirma.android.signfolder.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.security.KeyChain;
import android.security.KeyChainException;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import es.gob.afirma.android.crypto.LoadKeyStoreManagerTask;
import es.gob.afirma.android.crypto.LoadKeyStoreManagerTask.KeystoreManagerListener;
import es.gob.afirma.android.crypto.MobileKeyStoreManager;
import es.gob.afirma.android.crypto.MobileKeyStoreManager.KeySelectedEvent;
import es.gob.afirma.android.crypto.MobileKeyStoreManager.PrivateKeySelectionListener;
import es.gob.afirma.android.fcm.NotificationUtilities;
import es.gob.afirma.android.signfolder.AppPreferences;
import es.gob.afirma.android.signfolder.ConfigureFilterDialogBuilder;
import es.gob.afirma.android.signfolder.ErrorManager;
import es.gob.afirma.android.signfolder.LoginOptionsDialogBuilder;
import es.gob.afirma.android.signfolder.LoginOptionsDialogBuilder.LoginOptionsListener;
import es.gob.afirma.android.signfolder.MessageDialog;
import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.listeners.LoginListener;
import es.gob.afirma.android.signfolder.proxy.ClaveLoginResult;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.RequestAppConfiguration;
import es.gob.afirma.android.signfolder.proxy.ValidationLoginResult;
import es.gob.afirma.android.signfolder.tasks.ClaveLoginTask;
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
public final class LoginActivity extends WebViewParentActivity implements KeystoreManagerListener,
        PrivateKeySelectionListener,
        LoginOptionsListener,
        LoadConfigurationDataTask.LoadConfigurationListener,
        ClaveLoginTask.ClaveLoginRequestListener,
        LoginListener,
        LoadUserConfigTask.LoadUserConfigListener {

    private final static String EXTRA_RESOURCE_TITLE = "es.gob.afirma.signfolder.title"; //$NON-NLS-1$
    private final static String EXTRA_RESOURCE_EXT = "es.gob.afirma.signfolder.exts"; //$NON-NLS-1$

    private final static String CERTIFICATE_EXTS = ".p12,.pfx"; //$NON-NLS-1$

    private final static int SELECT_CERT_REQUEST_CODE = 1;

    private final static int PERMISSION_TO_BROWSE_FILE = 21;

    private final static int PERMISSION_TO_OPEN_HELP = 22;

    /**
     * Dialogo para mostrar mensajes al usuario
     */
    private MessageDialog messageDialog = null;
    private ProgressDialog progressDialog = null;

    private boolean notificationTokenChecked = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle(R.string.app_title);

        // Inicializamos si es necesario el listado de servidores proxy por defecto
        AppPreferences prefs = AppPreferences.getInstance();
        prefs.init(getApplicationContext());
        List<String> servers = prefs.getServersList();
        if (servers == null || servers.size() == 0) {
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

        // Una vez, tras el inicio de la aplicacion, obtenemos el token para el envio de
        // notificaciones a la aplicacion y lo registramos
        if (!notificationTokenChecked) {
            NotificationUtilities.checkCurrentToken();
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
     * @param v Vista sobre la que se hace clic.
     */
    public void onClick(final View v) {

        //Boton Acceder
        if (v.getId() == R.id.button_acceder) {

            // Reiniciamos la conexion con el servicio proxy
            // y comprobamos que tenemos conexion con el
            CommManager.resetConfig();
            if (!CommManager.getInstance().verifyProxyUrl()) {
                showErrorDialog(getString(R.string.error_msg_proxy_no_config));
                return;
            }

            // Acceso con certificado local
            if (!AppPreferences.getInstance().isCloudCertEnabled()) {
                // Iniciamos la carga del certificado
                loadKeyStore();
            }
            // Acceso con certificado en la nube
            else {
                // Ejecutamos la tarea de conexion con Clave
                ClaveLoginTask loginTask = new ClaveLoginTask(this);
                showProgressDialog(getString(R.string.dialog_msg_clave), this, loginTask);
                loginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
        // Boton importar certificados
        else {

            // Comprobamos si tenemos permisos para cargar el almacen de certificados en disco
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
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setClass(this, FileChooserActivity.class);
        intent.putExtra(EXTRA_RESOURCE_TITLE, getString(R.string.title_activity_cert_chooser));
        intent.putExtra(EXTRA_RESOURCE_EXT, CERTIFICATE_EXTS);
        startActivityForResult(intent, SELECT_CERT_REQUEST_CODE);
    }

    @Override
    public void setKeyStoreManager(final MobileKeyStoreManager msm) {

        dismissProgressDialog();

        if (msm == null) {
            PfLog.w(SFConstants.LOG_TAG, "Error al establecer el almacen de certificados. Es posible que el usuario cancelase la operacion."); //$NON-NLS-1$
            showErrorDialog(ErrorManager.getErrorMessage(ErrorManager.ERROR_ESTABLISHING_KEYSTORE));
        } else {
            msm.getPrivateKeyEntryAsynchronously(this);
        }
    }

    @Override
    public void onErrorLoadingKeystore(final String msg, final Throwable t) {
        showErrorDialog(ErrorManager.getErrorMessage(ErrorManager.ERROR_ESTABLISHING_KEYSTORE));
    }

    @Override
    public synchronized void keySelected(final KeySelectedEvent kse) {

        showProgressDialog(getString(R.string.dialog_msg_authenticating), this);

        final String alias;
        final byte[] certEncoded;
        final KeyStore.PrivateKeyEntry keyEntry;

        try {
            alias = kse.getCertificateAlias();
            certEncoded = kse.getCertificateEncoded();
            AppPreferences.getInstance().setLastCertificate(Base64.encode(certEncoded));
            keyEntry = kse.getPrivateKeyEntry();
        } catch (final KeyChainException e) {
            if ("4.1.1".equals(Build.VERSION.RELEASE) || "4.1.0".equals(Build.VERSION.RELEASE) || "4.1".equals(Build.VERSION.RELEASE)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                PfLog.e(SFConstants.LOG_TAG, "Error al obtener la clave privada del certificado en Android 4.1.X (asegurese de que no contiene caracteres no validos en el alias): " + e); //$NON-NLS-1$
                showErrorDialog(ErrorManager.getErrorMessage(ErrorManager.ERROR_PKE_ANDROID_4_1));
            } else {
                PfLog.e(SFConstants.LOG_TAG, "Error al obtener la clave privada del certificado: " + e); //$NON-NLS-1$
                showErrorDialog(ErrorManager.getErrorMessage(ErrorManager.ERROR_PKE));
            }
            return;
        } catch (final KeyStoreException e) {
            // Este caso se da cuando el usuario cancela el acceso al almacen o la seleccion de
            // un certificado. En el primer caso es posible que la activity se considere cerrada
            // asi que no se puede mostrar un dialogo de error. Nos limitamos a quitar el de espera.
            PfLog.e(SFConstants.LOG_TAG, "El usuario no selecciono un certificado: " + e); //$NON-NLS-1$
            dismissProgressDialog();
            onErrorLoginOptions(ErrorManager.getErrorMessage(ErrorManager.ERROR_NO_CERT_SELECTED));
            return;
        }
        // Cuando se instala el certificado desde el dialogo de seleccion, Android da a elegir certificado
        // en 2 ocasiones y en la segunda se produce un "java.lang.AssertionError". Se ignorara este error.
        catch (final Throwable e) {
            PfLog.e(SFConstants.LOG_TAG, "Error al obtener el certificado para la autenticacion: " + e, e); //$NON-NLS-1$
            showErrorDialog(ErrorManager.getErrorMessage(ErrorManager.ERROR_PKE));
            return;
        }

        dismissProgressDialog();

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
            final ValidationLoginResult loginResult = new ValidationLoginResult(true);
            loginResult.setCertificateB64(Base64.encode(certEncoded));
            loginResult.setCertAlias(alias);
            loadConfiguration(loginResult);
        } catch (Exception e) {
            // Error al conectar con el servidor
            showErrorDialog(getString(R.string.error_msg_communicating_server));
        }
    }

    // Definimos el menu de opciones de la aplicacion, cuyas opciones estan definidas
    // en el fichero activity_petition_list_options_menu.xml
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_login_options_menu, menu);
        MenuItem item = menu.findItem(R.id.clavefirma);
        boolean cloudCertEnabled = AppPreferences.getInstance().isCloudCertEnabled();
        item.setChecked(cloudCertEnabled);
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
        else if (item.getItemId() == R.id.clavefirma) {
            // Activamos/desactivamos la propiedad
            boolean cloudCertEnabled = AppPreferences.getInstance().isCloudCertEnabled();
            AppPreferences.getInstance().setCloudCertEnabled(!cloudCertEnabled);
            item.setChecked(!cloudCertEnabled);
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
        return true;
    }

    /**
     * Abre el fichero de ayuda de la aplicaci&oacute;n.
     */
    private void openHelp() {
        OpenHelpDocumentTask task = new OpenHelpDocumentTask(this);
        task.execute();
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
                    }
                }
            });
        } catch (final Exception e2) {
            PfLog.e(SFConstants.LOG_TAG, "Error en el hilo que muestra el mensaje de error: " + e2); //$NON-NLS-1$
        }
    }


//	/** Muestra un di&aacute;logo de espera con un mensaje. */
//	private void showProgressDialog(final String message, final LoadKeyStoreManagerTask lksmt,
//								final LoginRequestValidationTask lcdt) {
//		runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					setProgressDialog(ProgressDialog.show(LoginActivity.this, null, message, true));
//					getProgressDialog().setOnKeyListener(new OnKeyListener() {
//						@Override
//						public boolean onKey(final DialogInterface dialog, final int keyCode, final KeyEvent event) {
//							if (keyCode == KeyEvent.KEYCODE_BACK) {
//								if(lksmt != null){
//									lksmt.cancel(true);
//								}else if(lcdt != null){
//									lcdt.cancel(true);
//									if (lcdt.timer != null) {
//										lcdt.timer.cancel();
//									}
//								}
//								dismissProgressDialog();
//								return true;
//							}
//							return false;
//						}
//					});
//
//				}catch (final Exception e) {
//					PfLog.e(SFConstants.LOG_TAG, "No se ha podido mostrar el dialogo de progreso: " + e); //$NON-NLS-1$
//				}
//			}
//		});
//	}

    /**
     * Muestra un di&aacute;logo de espera con un mensaje.
     */
    private void showProgressDialog(final String message, final Context ctx, final AsyncTask<?, ?, ?>... tasks) {
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
                getProgressDialog().setOnKeyListener(new OnKeyListener() {
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


    /**
     * Cierra el di&aacute;logo de espera en caso de estar abierto.
     */
    void dismissProgressDialog() {
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

            final String filename = data.getStringExtra(FileChooserActivity.RESULT_DATA_STRING_FILENAME);

            int n;
            final byte[] buffer = new byte[1024];
            final ByteArrayOutputStream baos;
            try {
                baos = new ByteArrayOutputStream();
                final InputStream is = new FileInputStream(filename);
                while ((n = is.read(buffer)) > 0) {
                    baos.write(buffer, 0, n);
                }
                is.close();
            } catch (final IOException e) {
                showErrorDialog(getString(R.string.error_loading_selected_file, filename));
                PfLog.e(SFConstants.LOG_TAG, "Error al cargar el fichero: " + e, e); //$NON-NLS-1$
                return;
            }

            final Intent intent = KeyChain.createInstallIntent();
            intent.putExtra(KeyChain.EXTRA_PKCS12, baos.toByteArray());
            startActivity(intent);
        } else if (requestCode == WEBVIEW_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {
                PfLog.e(SFConstants.LOG_TAG, "Acceso correcto con Cl@ve"); //$NON-NLS-1$

                // Recuperamos el DNI para poder utilizarlo en futuras operaciones
                String dni = data != null ? data.getStringExtra("dni") : null; //$NON-NLS-1$
                if (dni == null) {
                    showErrorDialog(getString(R.string.error_logging_no_dni));
                } else {
                    ValidationLoginResult loginResult = new ValidationLoginResult(true);
                    loginResult.setDni(dni);
                    loadConfiguration(loginResult);
                }
            } else if (resultCode == RESULT_CANCELED) {
                PfLog.i(SFConstants.LOG_TAG, "Operacion de firma cancelada por el usuario");
                dismissProgressDialog();
            } else {
                PfLog.e(SFConstants.LOG_TAG, "Error al acceder con Cl@ve"); //$NON-NLS-1$
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
    }

    //metodo vacio para evitar bugs en versiones superiores al api11
    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        //No call for super(). Bug on API Level > 11.
        super.onSaveInstanceState(outState);
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
            PfLog.e(SFConstants.LOG_TAG, "No se ha podido mostrar el mensaje de error por configuracion incorrecta: " + e); //$NON-NLS-1$
            e.printStackTrace();
        }
    }

    @Override
    public void claveLoginRequestResult(ClaveLoginResult loginResult) {
        // Si fallo la conexion, mostramos un error
        if (!loginResult.isStatusOk()) {
            showErrorDialog(loginResult.getErrorMsg());
            return;
        }

        // Abrimos un WebView que carga la URL recibida y desde la que el usuario
        // podra autenticarse
        openWebViewActivity(
                ClaveWebViewActivity.class,
                loginResult.getRedirectionUrl(),
                loginResult.getCookieId(),
                R.string.title_clave_login,
                true);
    }

    @Override
    public void loginResult(ValidationLoginResult result) {

        if (result.isStatusOk()) {
            // Cargamos la configuracion
            loadConfiguration(result);
        } else {
            String errMsg = result.getErrorMsg();
            if (errMsg == null || errMsg.isEmpty()) {
                errMsg = getString(R.string.error_loading_app_configuration);
            }
            // Si la aplicacion contiene un mensaje de excepcion o si trata de mostrar una URL
            // nos aseguramos de no mostrarselo a los usuarios
            else if (errMsg.contains("Exception:") || errMsg.toLowerCase(Locale.US).contains("://")) {
                errMsg = getString(R.string.error_loading_app_configuration);
            }
            showErrorDialog(errMsg);
        }
    }


    /**
     * @param loginResult Resultado del proceso de login.
     */
    public void loadConfiguration(ValidationLoginResult loginResult) {
        LoadUserConfigTask task = new LoadUserConfigTask(loginResult,this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void userConfigLoadSuccess(UserConfig userConfig, ValidationLoginResult loginResult) {
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
        if (userConfig != null && userConfig.getRoles() != null && !userConfig.getRoles().isEmpty()) {
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
    public void userConfigLoadError(ValidationLoginResult loginResult, Throwable t) {

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
    public void configurationLoadSuccess(final RequestAppConfiguration appConfig, final ValidationLoginResult loginResult) {
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
