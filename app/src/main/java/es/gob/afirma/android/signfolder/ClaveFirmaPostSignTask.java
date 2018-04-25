package es.gob.afirma.android.signfolder;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.FirePreSignResult;
import es.gob.afirma.android.signfolder.proxy.RequestResult;
import es.gob.afirma.android.signfolder.proxy.TriphaseRequest;
import es.gob.afirma.core.signers.TriphaseData;

/** Tarea de conexi&oacute;n con clave en Android. */
public final class ClaveFirmaPostSignTask extends AsyncTask<Void, Void, RequestResult[]> {

	private final Context context;
	private FirePreSignResult firePreSignResult;
	private Throwable t = null;
	static private ClaveFirmaPostSignListener listener;

	class TaskKiller extends TimerTask {
		private AsyncTask<?, ?, ?> mTask;
		public TaskKiller(AsyncTask<?, ?, ?> task) {
			this.mTask = task;
		}

		public void run() {
			mTask.cancel(true);
			ClaveFirmaPostSignTask.listener.firePostSignFailed(new InterruptedException("Tiempo de espera agotado"));
		}
	}

	/** Crea una tarea para la solicitud de firma de peticiones a Clave Firma.
	 * @param firePreSignResult Informaci&oacute;n de la prefirma obtenida despues de la llamada de prefirma con Clave Firma.
	 * @param context Contexto padre.
     * @param listener Listener para gestionar los resultados de la operaci&oacute;n.
	 */
	public ClaveFirmaPostSignTask(final FirePreSignResult firePreSignResult, final Context context, final ClaveFirmaPostSignListener listener) {
		this.firePreSignResult = firePreSignResult;
		this.context = context;
		this.listener = listener;
	}

	@Override
	protected RequestResult[] doInBackground(final Void... params) {
		Timer timer = new Timer();
		timer.schedule(new TaskKiller(this), 120000);

		CommManager com = CommManager.getInstance();

		// Recuperamos de FIRe los resultados de firma que deben ser los PKCS#1
		RequestResult[] requestResults = null;
		try {
			requestResults = com.claveFirmaPostSignRequests(this.firePreSignResult);
		} catch (Exception e) {
			Log.e(SFConstants.LOG_TAG, "Error en la llamada al servicio de PostFirma de FIRe", e);
			this.t = e;
		}

		timer.cancel();
		return requestResults;
	}

	@Override
	protected void onPostExecute(final RequestResult[] requestResults) {

		if (requestResults == null) {
			Log.w(SFConstants.LOG_TAG, "Se ejecuta la operacion de error en las postfirma de FIRe");
			this.listener.firePostSignFailed(this.t);
			return;
		}

		Log.w(SFConstants.LOG_TAG, "Revisamos el resultado de cada una de las firmas");

		// Comprobamos que todas las firmas hayan finalizado correctamente
		for (RequestResult requestResult : requestResults) {
			if (!requestResult.isStatusOk()) {
				this.listener.firePostSignFailed(this.t);
				Log.w(SFConstants.LOG_TAG, "Alguna de las firmas no finalizo correctamente. Se ejecuta la operacion de error en la postfirma");
				return;
			}
		}

		Log.w(SFConstants.LOG_TAG, "Se ejecuta la operacion de exito en las postfirma de FIRe");

		this.listener.firePostSignSuccess(requestResults);
	}


	/** Interfaz que gestiona la respuesta a las solicitudes de carga de peticiones de firma. */
	interface ClaveFirmaPostSignListener {

		/** Se ejecuta cuando se obtiene el resultado de la petici&oacute;n de firma a Clave Firma.
		 * @param requestResults Resultados de las peticiones de firma. */
		void firePostSignSuccess(RequestResult[] requestResults);

		/** Se ejecuta cuando ocurre un error durante la petici&oacute;n de firma a Clave Firma.
		 * @param cause Motivo del error. */
		void firePostSignFailed(Throwable cause);
	}
}
