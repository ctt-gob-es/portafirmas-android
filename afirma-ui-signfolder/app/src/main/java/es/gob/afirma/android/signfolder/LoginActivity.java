package es.gob.afirma.android.signfolder;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.security.KeyChain;
import android.security.KeyChainException;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import es.gob.afirma.android.crypto.LoadKeyStoreManagerTask;
import es.gob.afirma.android.crypto.LoadKeyStoreManagerTask.KeystoreManagerListener;
import es.gob.afirma.android.crypto.MobileKeyStoreManager;
import es.gob.afirma.android.crypto.MobileKeyStoreManager.KeySelectedEvent;
import es.gob.afirma.android.crypto.MobileKeyStoreManager.PrivateKeySelectionListener;
import es.gob.afirma.android.gcm.RecoverNotificationTokenAsyncTask;
import es.gob.afirma.android.gcm.RecoverNotificationTokenAsyncTask.RecoverNotificationTokenListener;
import es.gob.afirma.android.signfolder.LoadConfigurationDataTask.LoadConfigurationListener;
import es.gob.afirma.android.signfolder.LoginOptionsDialogBuilder.LoginOptionsListener;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.RequestAppConfiguration;
import es.gob.afirma.android.util.Base64;

/** Actividad para entrada con usuario y contrase&ntilde;a al servicio de Portafirmas. */
public final class LoginActivity extends FragmentActivity implements KeystoreManagerListener,
                                                                     	PrivateKeySelectionListener,
																		LoginOptionsListener,
																		LoadConfigurationListener,
																		RecoverNotificationTokenListener {

	private final static String EXTRA_RESOURCE_TITLE = "es.gob.afirma.signfolder.title"; //$NON-NLS-1$
	private final static String EXTRA_RESOURCE_EXT = "es.gob.afirma.signfolder.exts"; //$NON-NLS-1$

	private final static String CERTIFICATE_EXTS = ".p12,.pfx"; //$NON-NLS-1$

	private final static int SELECT_CERT_REQUEST_CODE = 1;

	/** Dialogo para mostrar mensajes al usuario */
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
		final ConnectivityManager conMgr = (ConnectivityManager) getSystemService (Context.CONNECTIVITY_SERVICE);
		if (conMgr == null || conMgr.getActiveNetworkInfo() == null ||
				!conMgr.getActiveNetworkInfo().isAvailable() ||
				!conMgr.getActiveNetworkInfo().isConnected()) {
			//Error en la conexion
			showErrorDialog(getString(R.string.error_msg_check_connection));
		}

		// Una vez, tras el inicio de la aplicacion, obtenemos el token para el envio de
		// notificaciones a la aplicacion y lo registramos
		if (!notificationTokenChecked) {
			RecoverNotificationTokenAsyncTask recoverTokenTask = new RecoverNotificationTokenAsyncTask(this, this);
			recoverTokenTask.execute();
			notificationTokenChecked = true;
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


	/** @param v Vista sobre la que se hace clic. */
	public void onClick(final View v) {

		//Boton Acceder con certificado local
		if(v.getId() == R.id.button_acceder_local){
			CommManager.resetConfig();
			if (!CommManager.getInstance().verifyProxyUrl()) {
				showErrorDialog(getString(R.string.error_msg_proxy_no_config));
				return;
			}

			loadKeyStore();
		}
		// TODO
		/*
		//Boton Acceder con Cl@ave
		else if(v.getId() == R.id.button_login_cloud){
				CommManager.resetConfig();
				if (!CommManager.getInstance().verifyProxyUrl()) {
					showErrorDialog(getString(R.string.error_msg_proxy_no_config));
					return;
				}

			ClaveFirmaPreSignTask cct = new ClaveFirmaPreSignTask(this, this, this);
			showProgressDialog(getString(R.string.dialog_msg_clave),null,null);
			cct.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}*/
		// Boton importar certificados
		else {
			browseKeyStore();
		}
	}

	/** Carga el almacen de certificados del sistema. Se configura desde el layout para su ejecucion. */
	public void loadKeyStore() {

		LoadKeyStoreManagerTask lksmt = new LoadKeyStoreManagerTask(this, this);
		showProgressDialog(getString(R.string.dialog_msg_accessing_keystore),lksmt,null);
		lksmt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}

	/** Abre un activity para la seleccion de un fichero PKCS#12 local. */
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

		if (msm == null){
			Log.w(SFConstants.LOG_TAG, "Error al establecer el almacen de certificados. Es posible que el usuario cancelase la operacion."); //$NON-NLS-1$
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

		showProgressDialog(getString(R.string.dialog_msg_authenticating),null,null);

		final String alias;
		final byte[] certEncoded;
		final KeyStore.PrivateKeyEntry keyEntry;

		try {
			alias = kse.getCertificateAlias();
			certEncoded = kse.getCertificateEncoded();
			AppPreferences.getInstance().setLastCertificate(Base64.encode(certEncoded));
			keyEntry = kse.getPrivateKeyEntry();
		}
		catch (final KeyChainException e) {
			if ("4.1.1".equals(Build.VERSION.RELEASE) || "4.1.0".equals(Build.VERSION.RELEASE) || "4.1".equals(Build.VERSION.RELEASE)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				Log.e(SFConstants.LOG_TAG, "Error al obtener la clave privada del certificado en Android 4.1.X (asegurese de que no contiene caracteres no validos en el alias): " + e); //$NON-NLS-1$
				showErrorDialog(ErrorManager.getErrorMessage(ErrorManager.ERROR_PKE_ANDROID_4_1));
			}
			else {
				Log.e(SFConstants.LOG_TAG, "Error al obtener la clave privada del certificado: " + e); //$NON-NLS-1$
				showErrorDialog(ErrorManager.getErrorMessage(ErrorManager.ERROR_PKE));
			}
			return;
		}
		catch (final KeyStoreException e) {
			// Este caso se da cuando el usuario cancela el acceso al almacen o la seleccion de
			// un certificado. En el primer caso es posible que la activity se considere cerrada
			// asi que no se puede mostrar un dialogo de error. Nos limitamos a quitar el de espera.
			Log.e(SFConstants.LOG_TAG, "El usuario no selecciono un certificado: " + e); //$NON-NLS-1$
			dismissProgressDialog();
			onErrorLoginOptions(ErrorManager.getErrorMessage(ErrorManager.ERROR_NO_CERT_SELECTED));
			return;
		}
		// Cuando se instala el certificado desde el dialogo de seleccion, Android da a elegir certificado
		// en 2 ocasiones y en la segunda se produce un "java.lang.AssertionError". Se ignorara este error.
		catch (final Throwable e) {
			Log.e(SFConstants.LOG_TAG, "Error al obtener el certificado para la autenticacion: " + e); //$NON-NLS-1$
			e.printStackTrace();
			showErrorDialog(ErrorManager.getErrorMessage(ErrorManager.ERROR_PKE));
			return;
		}

		dismissProgressDialog();

		try {
			// Se prueba si la operacion de login esta soportada (Proxy v2.2). Si no lo abordamos como proxy antiguo
			//showProgressDialog(getString(R.string.dialog_msg_connection), null, null);
			//CommManager.getInstance().loginRequest();

			// Proxy nuevo (v2.2)
			final LoginRequestValidationTask lrvt = new LoginRequestValidationTask(
					Base64.encode(certEncoded),
					alias,
					CommManager.getInstance(),
					this,
					this,
					keyEntry);
			showProgressDialog(getString(R.string.dialog_msg_configurating), null, lrvt);

			lrvt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} catch (IllegalArgumentException e) {
			// Proxy antiguo sin validacion
			final LoadConfigurationDataTask lcdt = new LoadConfigurationDataTask(Base64.encode(certEncoded), alias,
					CommManager.getInstance(), this, this);
			CommManager.getInstance().setOldProxy();
			lcdt.execute();
		} catch (Exception e1) {
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
		boolean claveFirmaEnabled = AppPreferences.getInstance().getEnabledClavefirma();
		if(claveFirmaEnabled) {
			item.setChecked(true);
		}
		else {
			item.setChecked(false);
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
    	else if (item.getItemId() == R.id.clavefirma) {
			boolean claveFirmaEnabled = AppPreferences.getInstance().getEnabledClavefirma();
			if(claveFirmaEnabled) {
				AppPreferences.getInstance().setEnabledClavefirma(false);
				item.setChecked(false);
			}
			else {
				AppPreferences.getInstance().setEnabledClavefirma(true);
				item.setChecked(true);
			}
		}
		// Abrir ayuda
		else if (item.getItemId() == R.id.help) {
    		OpenHelpDocumentTask task = new OpenHelpDocumentTask(this);
    		task.execute();
		}
    	return true;
    }

	/**
	 * Muestra un mensaje de advertencia al usuario.
	 * @param message Mensaje que se desea mostrar.
	 */
	private void showErrorDialog(final String message) {
		showErrorDialog(message, null);
	}
	/**
	 * Muestra un mensaje de advertencia al usuario.
	 * @param message Mensaje que se desea mostrar.
	 */
	private void showErrorDialog(final String message, final String title) {

		dismissProgressDialog();

		if (this.messageDialog == null) {
			this.messageDialog = new MessageDialog();
		}
		this.messageDialog.setMessage(message);
		this.messageDialog.setContext(this);
		this.messageDialog.setTitle(title == null ? getString(R.string.error) : title);

		try {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					getMessageDialog().show(getSupportFragmentManager(), "ErrorDialog"); //$NON-NLS-1$;
				}
			});
		} catch (final Exception e) {
			Log.e(SFConstants.LOG_TAG, "No se ha podido mostrar el mensaje de error: " + e); //$NON-NLS-1$
			e.printStackTrace();
		}
	}



	/** Muestra un di&aacute;logo de espera con un mensaje. */
	private void showProgressDialog(final String message, final LoadKeyStoreManagerTask lksmt,
								final LoginRequestValidationTask lcdt) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					//TODO: Descomentar si se quiere un titulo en el dialogo de espera
					//setProgressDialog(ProgressDialog.show(LoginActivity.this, LoginActivity.this.getString(R.string.loading), message, true));
					setProgressDialog(ProgressDialog.show(LoginActivity.this, null, message, true));
					getProgressDialog().setOnKeyListener(new OnKeyListener() {
						@Override
						public boolean onKey(final DialogInterface dialog, final int keyCode, final KeyEvent event) {
							if (keyCode == KeyEvent.KEYCODE_BACK) {
								if(lksmt!=null){
									lksmt.cancel(true);
								}else if(lcdt!=null){
									lcdt.cancel(true);
									if (lcdt.timer != null) {
										lcdt.timer.cancel();
									}
								}
								dismissProgressDialog();
								return true;
							}
							return false;
						}
					});

				}catch (final Exception e) {
					Log.e(SFConstants.LOG_TAG, "No se ha podido mostrar el dialogo de progreso: " + e); //$NON-NLS-1$
				}
			}
		});
	}


	/** Cierra el di&aacute;logo de espera en caso de estar abierto. */
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

		if (requestCode == SELECT_CERT_REQUEST_CODE && resultCode == RESULT_OK) {

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
				Log.e(SFConstants.LOG_TAG, "Error al cargar el fichero: " + e.toString()); //$NON-NLS-1$
				e.printStackTrace();
				return;
			}

			final Intent intent = KeyChain.createInstallIntent();
			intent.putExtra(KeyChain.EXTRA_PKCS12, baos.toByteArray());
			startActivity(intent);
		}
	}

	@Override
	public void dismissDialog() {
		dismissProgressDialog();
	}

	@Override
	public void configurationLoadSuccess(final RequestAppConfiguration appConfig, final String certEncoded, final String certAlias) {
		access(certEncoded, certAlias, appConfig);
	}
	@Override
	public void configurationLoadError(final Throwable t) {
		dismissProgressDialog();
		if (t == null) {
			// Error en la conexion
			showErrorDialog(getString(R.string.error_loading_app_configuration), getString(R.string.error_loading_app_configuration_title));
		}
		else {
			// Error en las credenciales
			Log.w(SFConstants.LOG_TAG, "Error durante el proceso de login", t);
			showErrorDialog(getString(R.string.error_account_not_validated), getString(R.string.error_account_not_validated_title));
		}
	}

	/**
	 * Valida la identidad del usuario y da acceso al portafirmas.
	 * @param certEncodedB64 Certificado de autenticaci&oacute;n codificado en Base64 .
	 */
	private void access(final String certEncodedB64, final String alias, final RequestAppConfiguration appConfig) {

		dismissProgressDialog();

		final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setClass(this, PetitionListActivity.class);
		intent.putExtra(PetitionListActivity.EXTRA_RESOURCE_CERT_B64, certEncodedB64);
		intent.putExtra(PetitionListActivity.EXTRA_RESOURCE_CERT_ALIAS, alias);
		intent.putExtra(PetitionListActivity.EXTRA_RESOURCE_APP_IDS, appConfig.getAppIdsList().toArray(new String[appConfig.getAppIdsList().size()]));
		intent.putExtra(PetitionListActivity.EXTRA_RESOURCE_APP_NAMES, appConfig.getAppNamesList().toArray(new String[appConfig.getAppNamesList().size()]));

		startActivity(intent);
	}

	//metodo vacio para evitar bugs en versiones superiores al api11
	@Override
	protected void onSaveInstanceState(final Bundle outState) {
	    //No call for super(). Bug on API Level > 11.
	}

	@Override
	public void onErrorLoginOptions(final String url) {
		try {
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(getBaseContext(), url, Toast.LENGTH_LONG).show();
				}
			});
		}
		catch (final Exception e) {
			Log.e(SFConstants.LOG_TAG, "No se ha podido mostrar el mensaje de error por configuracion incorrecta: " + e); //$NON-NLS-1$
			e.printStackTrace();
		}
	}

	@Override
	public void updateNotificationToken(String token) {
		Log.i(SFConstants.LOG_TAG, "Token para notificaciones: " + token);
		AppPreferences.getInstance().setCurrentToken(token);
	}
}
