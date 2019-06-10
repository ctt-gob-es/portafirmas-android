package es.gob.afirma.android.signfolder;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.ValidationLoginResult;

/**
 * Tarea que realiza el proceso de login con el servicio proxy por medio de Cl@ve.
 */
public final class ClaveValidateLoginTask extends AsyncTask<Void, Void, ValidationLoginResult> {

	private String tokenSaml;
	private CommManager comm;
	private LoginListener claveListener;

	/**
	 * Construye la tarea de conexi&oacute;n con Cl@ve.
	 * @param claveListener Listener que atiende a los eventos generados durante la
	 *                         autenticaci&oacute;n con Cl@ve.
	 */
	ClaveValidateLoginTask(String tokenSaml, CommManager comm, LoginListener claveListener) {
		this.tokenSaml = tokenSaml;
		this.comm = comm;
		this.claveListener = claveListener;
	}

	@Override
	protected ValidationLoginResult doInBackground(final Void... params) {
        Timer timer = new Timer();
		timer.schedule(new TaskKiller(this, this.claveListener), 20000);

		ValidationLoginResult rr;
		try {
			rr = this.comm.claveLoginValidation(this.tokenSaml);
		} catch (Exception e) {
			Log.w(SFConstants.LOG_TAG, "No se pudo conectar con el servicio proxy", e);
			rr = new ValidationLoginResult(false);
			rr.setErrorMsg("No se pudo conectar con el servicio proxy");
		}
		timer.cancel();
		return rr;
	}

	@Override
	protected void onPostExecute(final ValidationLoginResult loginResult) {
		this.claveListener.loginResult(loginResult);
	}

	/**
	 * Subtarea encargada de finalizar la tarea principal si se excede una cantidad de tiempo limitada.
	 */
	class TaskKiller extends TimerTask {
		private AsyncTask<?, ?, ?> mTask;
		private LoginListener loginListener;
		TaskKiller(AsyncTask<?, ?, ?> task, LoginListener loginListener) {
			this.mTask = task;
			this.loginListener = loginListener;
		}

		public void run() {
			this.mTask.cancel(true);
            ValidationLoginResult loginResult = new ValidationLoginResult(false);
            loginResult.setErrorMsg("Tiempo de espera excedido");
			this.loginListener.loginResult(loginResult);
		}
	}
}
