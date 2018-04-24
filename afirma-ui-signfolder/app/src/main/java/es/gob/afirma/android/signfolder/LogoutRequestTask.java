package es.gob.afirma.android.signfolder;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.security.KeyStore;
import java.util.Timer;
import java.util.TimerTask;

import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.RequestResult;

/** Carga los datos remotos necesarios para la configuraci&oacute;n de la aplicaci&oacute;n. */
final class LogoutRequestTask extends AsyncTask<Void, Void, Void> {

	private final CommManager commManager;

	/**
	 * Crea la tarea para la carga de la configuraci&oacute;n de la aplicaci&oacute;n
	 * necesaria para su correcto funcionamiento.
	 * @param commManager Manejador de los servicios de comunicaci&oacute;n con el portafirmas.
	 */
	LogoutRequestTask(CommManager commManager) {
		this.commManager = commManager;
	}

	class TaskKiller extends TimerTask {
		private AsyncTask<?, ?, ?> mTask;
		public TaskKiller(AsyncTask<?, ?, ?> task) {
			this.mTask = task;
		}

		public void run() {
			mTask.cancel(true);
		}
	}

	@Override
	protected Void doInBackground(final Void... args) {
		// Se realiza la peticion para realizar el login
		// Se realiza la peticion para realizar el login
		Timer timer = new Timer();
		timer.schedule(new TaskKiller(this), 10000);
		try {
			this.commManager.logoutRequest();
		} catch (final Exception e) {
			Log.w(SFConstants.LOG_TAG, "No se pudo realizar el logout: " + e); //$NON-NLS-1$
		}
		timer.cancel();
		return null;
	}
}
