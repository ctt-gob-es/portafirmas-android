package es.gob.afirma.android.signfolder;

import android.content.Context;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import java.security.KeyStore;
import java.util.Timer;
import java.util.TimerTask;

import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.RequestAppConfiguration;
import es.gob.afirma.android.signfolder.proxy.RequestResult;
import es.gob.afirma.android.util.Base64;

/** Carga los datos remotos necesarios para la configuraci&oacute;n de la aplicaci&oacute;n. */
final class LoginRequestValidationTask extends AsyncTask<Void, Void, Boolean> {

	private final String certB64;
	private final String certAlias;
	private final CommManager commManager;
	private final Context context;
	private static LoadConfigurationDataTask.LoadConfigurationListener listener;
	private final KeyStore.PrivateKeyEntry privateKeyEntry;
	private static Throwable t;
	public Timer timer = null;
	static boolean timeoutConnection;

	class TaskKiller extends TimerTask {
		private AsyncTask<?, ?, ?> mTask;
		public TaskKiller(AsyncTask<?, ?, ?> task) {
			this.mTask = task;
		}

		public void run() {
			mTask.cancel(true);
			LoginRequestValidationTask.listener.configurationLoadError(LoginRequestValidationTask.t);
		}
	}

	/**
	 * Crea la tarea para la carga de la configuraci&oacute;n de la aplicaci&oacute;n
	 * necesaria para su correcto funcionamiento.
	 * @param certB64 Certificado para la autenticaci&oacute;n de la petici&oacute;n.
	 * @param commManager Manejador de los servicios de comunicaci&oacute;n con el portafirmas.
	 * @param context Contexto de la aplicaci&oacute;n.
	 * @param listener Manejador del resultado de la operaci&oacute;n.
	 * @param pke Clave privada del almac&eacute;n de claves.
	 */
	LoginRequestValidationTask(final String certB64, final String certAlias,
							   final CommManager commManager, final Context context,
							   final LoadConfigurationDataTask.LoadConfigurationListener listener,
							   final KeyStore.PrivateKeyEntry pke) {
		this.certB64 = certB64;
		this.certAlias = certAlias;
		this.commManager = commManager;
		this.context = context;
		this.listener = listener;
		this.privateKeyEntry = pke;
	}

	@Override
	protected Boolean doInBackground(final Void... args) {
		// Se realiza la peticion para realizar el login
		timer = new Timer();
		timer.schedule(new TaskKiller(this), 10000);
		t = null;
		timeoutConnection = true;
		boolean validLogin = false;
		try {
			RequestResult token = this.commManager.loginRequest();

			// Se realiza la firma del token y se envia
			validLogin = this.commManager.tokenValidation(token, this.certB64, privateKeyEntry);
			if(!validLogin) {
				t = new Exception("El proxy ha denegado el acceso");
			}
		} catch (IllegalArgumentException e) {
			// Proxy antiguo sin validacion
			CommManager.getInstance().setOldProxy();
			Log.w(SFConstants.LOG_TAG, "Login no necesario: Se trabaja con una version antigua del portafirmas"); //$NON-NLS-1$
			validLogin = true;
			timeoutConnection = false;
		} catch (final Exception e) {
			Log.w(SFConstants.LOG_TAG, "No se pudo realizar el login: " + e); //$NON-NLS-1$
		}
		timer.cancel();
		return new Boolean(validLogin);
	}

	@Override
	protected void onPostExecute(final Boolean validLogin) {
		if (validLogin.booleanValue()) {
			final LoadConfigurationDataTask lcdt = new LoadConfigurationDataTask(this.certB64, this.certAlias,
					this.commManager, this.context, this.listener);
			lcdt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		else {
			this.listener.configurationLoadError(this.t);
		}
	}
}
