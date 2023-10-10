package es.gob.afirma.android.signfolder.tasks;

import android.os.AsyncTask;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.util.PfLog;

/** Vacia el directorio de temporales de la aplicaci&oacute;n. */
public final class CleanTempFilesTask extends AsyncTask<Void, Void, Void> {

	private final File tempDir;

	/**
	 * Crea la tarea para la eliminacion de los ficheros temporales de la aplicaci&oacute;n.
	 * @param context Manejador de los servicios de comunicaci&oacute;n con el portafirmas.
	 */
	public CleanTempFilesTask(File tempDir) {
		this.tempDir = tempDir;
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

		if (this.tempDir.isDirectory()) {
			// Se realiza el borrado, pero se aborta si tarda demasiado. Si quedase algo, ya se
			// eliminaria en futuras ejecuciones
			Timer timer = new Timer();
			timer.schedule(new TaskKiller(this), 5000);
			try {
				for (File tempFile : tempDir.listFiles()) {
					try {
						tempFile.delete();
					} catch (final Exception e) {
						PfLog.w(SFConstants.LOG_TAG, "No se pudo borrar el fichero " + tempFile, e); //$NON-NLS-1$
					}
					if (isCancelled()) {
						return null;
					}
				}
			} catch( final Exception e){
				PfLog.w(SFConstants.LOG_TAG, "No se pudo ejecutar el proceso de borrado de temporales", e); //$NON-NLS-1$
			}
			timer.cancel();
		}
		return null;
	}
}
