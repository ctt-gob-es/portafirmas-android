package es.gob.afirma.android.signfolder.listeners;

import es.gob.afirma.android.signfolder.proxy.RequestResult;

/** Escucha las operaciones realizadas sobre las peticiones del usuario y act&uacute;a seg&uacute;n el resultado. */
public interface OperationRequestListener {

	int REJECT_OPERATION = 1;
	int SIGN_OPERATION = 2;
	int APPROVE_OPERATION = 3;
	int VERIFY_OPERATION = 4;


	/**
	 * M&eacute;todo a ejecutar cuando termina la operaci&oacute;n.
	 * @param operation Tipo de operaci&oacute;n.
	 * @param requestResult Resultado de la operaci&oacute;n.
	 */
	void requestOperationFinished(int operation, RequestResult requestResult);

	/**
	 * M&eacute;todo a ejecutar cuando ocurri&oacute; un error en la operaci&oacute;n.
	 * @param operation Tipo de operaci&oacute;n.
	 * @param requestResult Resultado de la petici&oacute; procesada.
	 * @param t Excepcion/Error que hizo fallar el procesamiento de las peticiones.
	 */
	void requestOperationFailed(int operation, RequestResult requestResult, Throwable t);

	/**
	 * M&eacute;todo a ejecutar cuando el usuario cancela el procesado de la petici&oacute;n.
	 * @param operation Tipo de operaci&oacute;n.
	 * */
	void requestOperationCancelled(int operation);
}
