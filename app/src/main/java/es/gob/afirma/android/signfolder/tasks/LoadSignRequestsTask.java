package es.gob.afirma.android.signfolder.tasks;

import android.os.AsyncTask;

import java.util.List;

import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.PartialSignRequestsList;
import es.gob.afirma.android.signfolder.proxy.ServerControlledException;
import es.gob.afirma.android.signfolder.proxy.ServerErrors;
import es.gob.afirma.android.signfolder.proxy.SignRequest;
import es.gob.afirma.android.util.PfLog;

/** Tarea as&iacute;ncrona para la carga de peticiones de firma en una lista de peticiones. */
public final class LoadSignRequestsTask extends AsyncTask<Void, Void, PartialSignRequestsList> {

	private final String state;
	private final String[] filters;
	private final CommManager commManager;
	private final LoadSignRequestListener listener;
	private final int numPage;
	private final int pageSize;

	private boolean running;

	/**
	 * Crea la tarea asincrona para la carga de peticiones de firma.
	 *
	 * @param state Estado de las peticiones que se solicitan (pendiente, rechazadas o firmadas).
     * @param numPage N&uacute;mero de la p&aacute;gina de resultados del listado que se desea obtener.
     * @param pageSize Tama&ntilde;o de cada p&aacute;gina de resultados.
	 * @param filters Filtros que han de cumplir las peticiones.
     * @param commManager Manejador de las comunicaciones para el rechazo de las peticiones.
	 * @param listener Manejador para el postproceso de las peticiones de firma cargadas.
	 */
	public LoadSignRequestsTask(final String state, final int numPage, final int pageSize, final List<String> filters, final CommManager commManager, final LoadSignRequestListener listener) {
		this.state = state;
		this.filters = filters != null ? filters.toArray(new String[0]) : null;
		this.commManager = commManager;
		this.listener = listener;
		this.numPage = numPage;
		this.pageSize = pageSize;
		this.running = false;
	}

    @Override
    protected PartialSignRequestsList doInBackground(final Void... arg) {

		this.running = true;

    	// Aqui se carga la lista de peticiones de documentos
    	PartialSignRequestsList signRequests;
    	try {
    		signRequests = this.commManager.getSignRequests(
    				this.state,
    				this.filters,
    				this.numPage,
    				this.pageSize);
    	}
    	catch (final ServerControlledException e) {
			signRequests = null;
			this.running = false;
			PfLog.e(SFConstants.LOG_TAG, "Ocurrio un error controlado al recuperar las peticiones de firma", e); //$NON-NLS-1$
			// Si se ha perdido la sesion o el certificado de autenticacion no es valido vuelve a la pantalla de login
			if (ServerErrors.ERROR_AUTHENTICATING_REQUEST.equals(e.getErrorCode())) {
				if(this.commManager.isOldProxy()) {
					this.listener.invalidCredentials();
				}
				else {
					this.listener.lostSession();
				}
			}
		}
    	catch (final Exception e) {
    		signRequests = null;
			this.running = false;
    		PfLog.e(SFConstants.LOG_TAG, "Ocurrio un error al recuperar las peticiones de firma", e); //$NON-NLS-1$
			// Si se ha perdido la sesion o el certificado de autenticacion no es valido vuelve a la pantalla de login
			if(e.getMessage().contains(ServerErrors.ERROR_AUTHENTICATING_REQUEST)) {
                if(this.commManager.isOldProxy()) {
                    this.listener.invalidCredentials();
                }
                else {
                    this.listener.lostSession();
                }
			}
    	}
    	catch (final Throwable t) {
    		signRequests = null;
			this.running = false;
    		PfLog.e(SFConstants.LOG_TAG, "Problema grave al listar las peticiones", t); //$NON-NLS-1$
    	}

    	return signRequests;
    }

    @Override
	protected void onPostExecute(final PartialSignRequestsList partialSignRequests) {

		this.running = false;

    	// Si se cancela la operacion, no se actualiza el listado
    	if (isCancelled()) {
    		return;
    	}

		if (partialSignRequests == null) {
			this.listener.errorLoadingSignRequest();
			return;
		}

		final int numPages = partialSignRequests.getTotalSignRequests() / this.pageSize +
				(partialSignRequests.getTotalSignRequests() % this.pageSize == 0 ? 0 : 1);
		this.listener.loadedSignRequest(partialSignRequests.getCurrentSignRequests(), this.numPage, numPages);
    }

	/**
	 * Inidica si la tarea se esta procesando actualmente.
	 * @return {@code true} si la tarea se est&aacute; procesando, {@code false} en caso contrario.
	 */
	public boolean isRunning() {
		return this.running;
	}

    /** Interfaz que gestiona la respuesta a las solicitudes de carga de peticiones de firma. */
	public interface LoadSignRequestListener {

    	/** Se ejecuta cuando las peticiones de firma se han cargado correctamente.
    	 * @param signRequests Peticiones de firma cargadas.
    	 * @param pageNumber N&uacute;mero de p&aacute;gina.
    	 * @param numPages N&uacute;mero total de p&aacute;ginas. */
    	void loadedSignRequest(List<SignRequest> signRequests, int pageNumber, int numPages);

    	/** Se ejecuta cuando ocurre un error durante la carga de las peticiones de firma. */
    	void errorLoadingSignRequest();

		/** Se ejecuta cuando se pierde la sesion con el portafirmas. */
		void lostSession();

		void invalidCredentials();
	}
}
