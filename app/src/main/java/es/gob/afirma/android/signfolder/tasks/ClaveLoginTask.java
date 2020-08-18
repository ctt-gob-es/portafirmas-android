package es.gob.afirma.android.signfolder.tasks;

import android.os.AsyncTask;

import java.util.Timer;
import java.util.TimerTask;

import es.gob.afirma.android.signfolder.AppPreferences;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.proxy.ClaveLoginResult;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.ServerControlledException;
import es.gob.afirma.android.signfolder.proxy.ServerErrors;
import es.gob.afirma.android.util.PfLog;

/**
 * Tarea que realiza el proceso de login con el servicio proxy por medio de Cl@ve.
 */
public final class ClaveLoginTask extends AsyncTask<Void, Void, ClaveLoginResult> {

	private ClaveLoginRequestListener claveListener;

	/**
	 * Construye la tarea de conexi&oacute;n con Cl@ve.
	 * @param claveListener Listener que atiende a los eventos generados durante la
	 *                         autenticaci&oacute;n con Cl@ve.
	 */
	public ClaveLoginTask(ClaveLoginRequestListener claveListener) {
		this.claveListener = claveListener;
	}

	@Override
	protected ClaveLoginResult doInBackground(final Void... params) {
        Timer timer = new Timer();
		timer.schedule(new TaskKiller(this, this.claveListener),
				AppPreferences.getInstance().getConnectionReadTimeout());

		CommManager com = CommManager.getInstance();
		ClaveLoginResult rr;
		try {
			rr = com.claveLoginRequest();
		} catch (ServerControlledException e) {
			PfLog.w(SFConstants.LOG_TAG, "Error devuelto por el servicio proxy: " + e.getMessage(), e);
			rr = new ClaveLoginResult(false);
			if (ServerErrors.ERROR_AUTHENTICATING_REQUEST.equals(e.getErrorCode())) {
				rr.setErrorMsg("El Portafirmas seleccionado no soporta el uso de certificados remotos");
			}
			else {
				rr.setErrorMsg("Error en la comunicaci\u00F3n con el servicio proxy");
			}
		} catch (IllegalArgumentException e) {
			PfLog.w(SFConstants.LOG_TAG, "Error en la comunicaci\u00F3n con el servicio proxy", e);
			rr = new ClaveLoginResult(false);
			rr.setErrorMsg("Error en la comunicaci\u00F3n con el servicio proxy");
		} catch (Exception e) {
			PfLog.w(SFConstants.LOG_TAG, "No se pudo conectar con el servicio proxy", e);
			rr = new ClaveLoginResult(false);
			rr.setErrorMsg("No se pudo conectar con el servicio proxy");
		}
		timer.cancel();
		return rr;
	}

	@Override
	protected void onPostExecute(final ClaveLoginResult loginResult) {
		this.claveListener.claveLoginRequestResult(loginResult);
	}

	/**
	 * Subtarea encargada de finalizar la tarea principal si se excede una cantidad de tiempo limitada.
	 */
	static class TaskKiller extends TimerTask {
		private AsyncTask<?, ?, ?> mTask;
		private ClaveLoginRequestListener loginListener;
		TaskKiller(AsyncTask<?, ?, ?> task, ClaveLoginRequestListener loginListener) {
			this.mTask = task;
			this.loginListener = loginListener;
		}

		public void run() {
			this.mTask.cancel(true);
            ClaveLoginResult loginResult = new ClaveLoginResult(false);
            loginResult.setErrorMsg("Tiempo de espera excedido");
			this.loginListener.claveLoginRequestResult(loginResult);
		}
	}

	/** Interfaz con los metodos para gestionar los resultados de la peticion del detalle
	 * de una solicitud de firma. */
	public interface ClaveLoginRequestListener {
		void claveLoginRequestResult(ClaveLoginResult cclr);
	}
}
