package es.gob.afirma.android.signfolder.tasks;

import android.os.AsyncTask;

import java.util.Timer;
import java.util.TimerTask;

import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.FireLoadDataResult;
import es.gob.afirma.android.signfolder.proxy.SignRequest;

/** Tarea para la generaci&oacute;n de prefirmas con Cl@ve Firma. Para obtener estas prefirma
 * primeramente se realizar&aacute; la prefirma de los datos normalmente (contra el servidor
 * trif&aacute;sico) y despu&eacute;s estas se mandaran a firmar con Cl@ve Firma indicando el
 * formato NONE para que realmente no se procesen (evitando que se vuelva a calcular las prefirmas).
 * Como resultado de esta operaci&oacute;n se obtendr&aacute; el identificador de transacci&oacute;n
 * y la URL de redirecci&oacute;n de FIRe en la que el usuario deber&aacute; autorizar la operaci&oacute;n. */
public final class FireLoadDataTask extends AsyncTask<Void, Void, FireLoadDataResult> {

	private final SignRequest[] signRequests;
	private Throwable t = null;
	private FireLoadDataListener listener;

	class TaskKiller extends TimerTask {
		private AsyncTask<?, ?, ?> mTask;
		private FireLoadDataListener loadDataListener;
		public TaskKiller(AsyncTask<?, ?, ?> task, FireLoadDataListener loadDataListener) {
			this.mTask = task;
			this.loadDataListener = loadDataListener;
		}

		public void run() {
			this.mTask.cancel(true);
			this.loadDataListener.fireLoadDataFailed(new InterruptedException("Tiempo de espera agotado"));
		}
	}

	/** Crea una tarea para la solicitud de firma de peticiones a Clave Firma.
     * @param listener Listener para gestionar los resultados de la operaci&oacute;n.
	 * @param reqs Peticiones de las que se desea realizar la firma. */
	public FireLoadDataTask(SignRequest[] reqs, final FireLoadDataListener listener) {
		this.signRequests = reqs;
		this.listener = listener;
	}

	@Override
	protected FireLoadDataResult doInBackground(final Void... params) {
		Timer timer = new Timer();
		final int TIME_LIMIT = 20000;
		timer.schedule(new TaskKiller(this, this.listener), TIME_LIMIT);

		final CommManager com = CommManager.getInstance();

		// Mandamos todas las prefirmas a FIRe que ya estara configurado para que
		// no vuelva a intentar generar las prefirmas y actue directamente sobre ellas
		FireLoadDataResult firePreSignResult = null;
		try {
			firePreSignResult = com.firePrepareSigns(this.signRequests);
		} catch (Exception e) {
			this.t = e;
		}
		timer.cancel();
		return firePreSignResult;
	}

	@Override
	protected void onPostExecute(final FireLoadDataResult firePreSignResult) {
		if (firePreSignResult != null && firePreSignResult.isStatusOk()) {
			this.listener.fireLoadDataSuccess(firePreSignResult);
		}
		else {
			this.listener.fireLoadDataFailed(this.t);
		}
	}

	/** Interfaz que gestiona la respuesta a las solicitudes de carga de peticiones de firma. */
	public interface FireLoadDataListener {

		/** Se ejecuta cuando se obtiene el resultado de la petici&oacute;n de firma a Clave Firma.
		 * @param firePreSignResult Informaci&oacute;n de prefirma y de la transaccion remitida por FIRe. */
		void fireLoadDataSuccess(FireLoadDataResult firePreSignResult);

		/** Se ejecuta cuando ocurre un error durante la petici&oacute;n de firma a Clave Firma.
		 * @param cause Motivo del error. */
		void fireLoadDataFailed(Throwable cause);
	}
}
