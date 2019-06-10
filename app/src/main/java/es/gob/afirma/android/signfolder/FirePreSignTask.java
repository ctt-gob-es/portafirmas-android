package es.gob.afirma.android.signfolder;

import android.content.Context;
import android.os.AsyncTask;

import java.util.Timer;
import java.util.TimerTask;

import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.FirePreSignResult;
import es.gob.afirma.android.signfolder.proxy.SignRequest;

/** Tarea para la generaci&oacute;n de prefirmas con Cl@ve Firma. Para obtener estas prefirma
 * primeramente se realizar&aacute; la prefirma de los datos normalmente (contra el servidor
 * trif&aacute;sico) y despu&eacute;s estas se mandaran a firmar con Cl@ve Firma indicando el
 * formato NONE para que realmente no se procesen (evitando que se vuelva a calcular las prefirmas).
 * Como resultado de esta operaci&oacute;n se obtendr&aacute; el identificador de transacci&oacute;n
 * y la URL de redirecci&oacute;n de FIRe en la que el usuario deber&aacute; autorizar la operaci&oacute;n. */
public final class FirePreSignTask extends AsyncTask<Void, Void, FirePreSignResult> {

	private final Context context;
	private final SignRequest[] signRequests;
	private Throwable t = null;
	static private ClaveFirmaPreSignListener listener;

	class TaskKiller extends TimerTask {
		private AsyncTask<?, ?, ?> mTask;
		public TaskKiller(AsyncTask<?, ?, ?> task) {
			this.mTask = task;
		}

		public void run() {
			mTask.cancel(true);
			FirePreSignTask.listener.firePreSignFailed(new InterruptedException("Tiempo de espera agotado"));
		}
	}

	/** Crea una tarea para la solicitud de firma de peticiones a Clave Firma.
	 * @param context Contexto padre.
     * @param listener Listener para gestionar los resultados de la operaci&oacute;n.
	 * @param reqs Peticiones de las que se desea realizar la firma. */
	public FirePreSignTask(SignRequest[] reqs, final Context context, final ClaveFirmaPreSignListener listener) {
		this.signRequests = reqs;
		this.context = context;
		this.listener = listener;
	}

	@Override
	protected FirePreSignResult doInBackground(final Void... params) {
		Timer timer = new Timer();
		timer.schedule(new TaskKiller(this), 20000);

		FirePreSignResult firePreSignResult = null;
		CommManager com = CommManager.getInstance();

		// Mandamos todas las prefirmas a FIRe que ya estara configurado para que
		// no vuelva a intentar generar las prefirmas y actue directamente sobre ellas
		try {
			firePreSignResult = com.firePreSignRequests(this.signRequests);
			// Decodificamos la URL que se recibe en Base64 para evitar problemas de codificacion
			firePreSignResult.setUrl(firePreSignResult.getURL());
		} catch (Exception e) {
			this.t = e;
		}
		timer.cancel();
		return firePreSignResult;
	}

	@Override
	protected void onPostExecute(final FirePreSignResult firePreSignResult) {
		if(firePreSignResult != null) {
			this.listener.firePreSignSuccess(firePreSignResult);
		}
		else {
			this.listener.firePreSignFailed(this.t);
		}
	}

	/** Interfaz que gestiona la respuesta a las solicitudes de carga de peticiones de firma. */
	interface ClaveFirmaPreSignListener {

		/** Se ejecuta cuando se obtiene el resultado de la petici&oacute;n de firma a Clave Firma.
		 * @param firePreSignResult Informaci&oacute;n de prefirma y de la transaccion remitida por FIRe. */
		void firePreSignSuccess(FirePreSignResult firePreSignResult);

		/** Se ejecuta cuando ocurre un error durante la petici&oacute;n de firma a Clave Firma.
		 * @param cause Motivo del error. */
		void firePreSignFailed(Throwable cause);
	}
}
