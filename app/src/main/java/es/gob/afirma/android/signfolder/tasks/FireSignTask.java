package es.gob.afirma.android.signfolder.tasks;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.FireSignResult;
import es.gob.afirma.android.util.PfLog;

/** Tarea de conexi&oacute;n con clave en Android. */
public final class FireSignTask extends AsyncTask<Void, Void, FireSignResult> {

	private Throwable t = null;
	static private FireSignListener listener;

	class TaskKiller extends TimerTask {
		private AsyncTask<?, ?, ?> mTask;
		public TaskKiller(AsyncTask<?, ?, ?> task) {
			this.mTask = task;
		}

		public void run() {
			mTask.cancel(true);
			FireSignTask.listener.fireSignFailed(new InterruptedException("Tiempo de espera agotado"));
		}
	}

	/** Crea una tarea para la solicitud de firma de peticiones a Clave Firma.
     * @param listener Listener para gestionar los resultados de la operaci&oacute;n.
	 */
	public FireSignTask(final FireSignListener listener) {
		FireSignTask.listener = listener;
	}

	@Override
	protected FireSignResult doInBackground(final Void... params) {
		Timer timer = new Timer();
		final int TIME_LIMIT = 120000;
		timer.schedule(new TaskKiller(this), TIME_LIMIT);

		CommManager com = CommManager.getInstance();

		// Recuperamos de FIRe los resultados de firma que deben ser los PKCS#1
		FireSignResult signResult = null;
		try {
			signResult = com.fireSignRequests();
		} catch (Exception e) {
			PfLog.e(SFConstants.LOG_TAG, "Error en la llamada al servicio de firma con FIRe", e);
			this.t = e;
		}

		timer.cancel();
		return signResult;
	}

	@Override
	protected void onPostExecute(final FireSignResult signResult) {

		if (signResult == null) {
			PfLog.w(SFConstants.LOG_TAG, "Se ejecuta la operacion de error durante la firma con FIRe");
			FireSignTask.listener.fireSignFailed(this.t);
			return;
		}

		PfLog.w(SFConstants.LOG_TAG, "Revisamos el resultado de cada una de las firmas");

		if (!signResult.isStatus() &&
				signResult.getErrorType()== FireSignResult.ERROR_COMMUNICATION) {
			PfLog.w(SFConstants.LOG_TAG, "Error en la comunicacion con FIRe");
			FireSignTask.listener.fireSignFailed(new IOException("Error en la comunicacion con FIRe"));
			return;
		}

		PfLog.w(SFConstants.LOG_TAG, "Se ejecuta la operacion de exito en las postfirma de FIRe");

		FireSignTask.listener.fireSignSuccess(signResult.isStatus());
	}


	/** Interfaz que gestiona la respuesta a las solicitudes de carga de peticiones de firma. */
	public interface FireSignListener {

		/** Se ejecuta cuando se obtiene el resultado de la petici&oacute;n de firma a FIRe.
		 * @param result Resultado de la operaci&oacute;n de firma. {@code true} si todas las
		 *               se firmaron correctamente, {@code false} si se produjeron errores. */
		void fireSignSuccess(boolean result);

		/** Se ejecuta cuando ocurre un error durante la petici&oacute;n de firma a FIRe.
		 * @param cause Motivo del error. */
		void fireSignFailed(Throwable cause);
	}
}
