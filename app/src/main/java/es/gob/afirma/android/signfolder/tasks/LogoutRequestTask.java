package es.gob.afirma.android.signfolder.tasks;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.util.PfLog;

/** Carga los datos remotos necesarios para la configuraci&oacute;n de la aplicaci&oacute;n. */
public final class LogoutRequestTask extends AsyncTask<Void, Void, Void> {

	private final CommManager commManager;

	/**
	 * Crea la tarea para la carga de la configuraci&oacute;n de la aplicaci&oacute;n
	 * necesaria para su correcto funcionamiento.
	 * @param commManager Manejador de los servicios de comunicaci&oacute;n con el portafirmas.
	 */
	public LogoutRequestTask(CommManager commManager) {
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

		// Se realiza la peticion para realizar el logout
		Timer timer = new Timer();
		timer.schedule(new TaskKiller(this), 10000);
		try {
			this.commManager.logoutRequest();
		} catch (final Exception e) {
			PfLog.w(SFConstants.LOG_TAG, "No se pudo realizar el logout: " + e); //$NON-NLS-1$
		}
		timer.cancel();
		return null;
	}
}
