package es.gob.afirma.android.signfolder.proxy;

import java.util.Set;

/**
 * Petici&oacute;n de fase de firma de documentos. */
public class TriphaseRequest {

	/** Referencia de la petici&oacute;n. */
	private final String ref;

	/** Resultado de la petici&oacute;n de la petici&oacute;n. */
	private boolean statusOk;

	/** Listado de documentos de la petici&oacute;n que se desean firmar. */
	private final TriphaseSignDocumentRequest[] documentsRequest;

	/** Resultado de la petici&oacute;n de la petici&oacute;n. */
	private String exception = null;

	/** C&oacute;digo que identifica al error obtenido cuando se requiere confirmaci&oacute;n del usuario. */
	private Set<SignaturePermission> permissions = null;

	/**
	 * Construye un objeto de petici&oacute;n de prefirma o postfirma de documentos.
	 * @param reference Referencia &uacute;nica de la petici&oacute;n.
	 * @param documentsRequest Listado de documentos para los que se solicita la operaci&oacute;n.
	 */
	public TriphaseRequest(final String reference, final TriphaseSignDocumentRequest[] documentsRequest) {
		this(reference, true, documentsRequest);
	}

	/**
	 * Construye un objeto de petici&oacute;n de firma de documentos.
	 * @param reference Referencia &uacute;nica de la petici&oacute;n.
	 * @param statusOk Estado de la petici&oacute;n.
	 * @param documentsRequest Listado de documentos para los que se solicita la firma.
	 */
	public TriphaseRequest(final String reference, final boolean statusOk, final TriphaseSignDocumentRequest[] documentsRequest) {
		this(reference, statusOk, documentsRequest, null);
	}

	/**
	 * Construye un objeto de petici&oacute;n de firma de documentos.
	 * @param reference Referencia &uacute;nica de la petici&oacute;n.
	 * @param statusOk Estado de la petici&oacute;n.
	 * @param exception Traza de la excepci&oacute;n que provoc&oacute; el error.
	 */
	public TriphaseRequest(final String reference, final boolean statusOk, final String exception) {
		this(reference, statusOk, null, null);
		this.exception = exception;
	}

	/**
	 * Construye un objeto de petici&oacute;n de firma de documentos.
	 * @param reference Referencia &uacute;nica de la petici&oacute;n.
	 * @param statusOk Estado de la petici&oacute;n.
	 * @param permissions Permisos necesarios par ala firma de los documentos de la petici&oacute;n.
	 */
	public TriphaseRequest(final String reference, final boolean statusOk, final TriphaseSignDocumentRequest[] documentsRequest, final Set<SignaturePermission> permissions) {
		this.ref = reference;
		this.statusOk = statusOk;
		this.documentsRequest = documentsRequest;
		this.permissions = permissions;
	}
	
	/**
	 * Recupera la referencia de la petici&oacute;n firma de documentos.
	 * @return Referencia de la petici&oacute;n.
	 */
	public String getRef() {
		return this.ref;
	}

	/** Indica si el estado de la petici&oacute;n es correcto.
	 * @return Indicador del estado de la petici&oacute;n. */
	public boolean isStatusOk() {
		return this.statusOk;
	}

	/** Establece el estado actual de la petici&oacute;n.
	 * @param ok <code>true</code> si la petici&oacute;n es correcta, <code>false</code> en caso contrario */
	public void setStatusOk(final boolean ok) {
		this.statusOk = ok;
	}

	/**
	 * Listado de peticiones de documentos para los que se desea la firma en multiples fases.
	 * @return Listado de peticiones o null si ha ocurrido alg&uacute;n problema al
	 * procesar la petici&oacute;n ({@code isStatusOk() == false}).
	 */
	public TriphaseSignDocumentRequest[] getDocumentsRequests() {
		return this.documentsRequest;
	}
	
	/**
	 * En caso de error, recupera la traza de la excepci&oacute;n que lo provoc&oacute;
	 * @return Traza de la excepci&oacute;n.
	 */
	public String getException() {
		return this.exception;
	}


	/**
	 * Indica si el error obtenido se debe a que se requiere confirmaci&oacute;n
	 * por parte del usuario.
	 * @return {@code true} si se requiere confirmaci&oacute;n,
	 * {@code false} en caso contrario.
	 */
	public boolean isNeedConfirmation() {
		return this.permissions != null && !this.permissions.isEmpty();
	}

	/**
	 * Recupera el conjunto de permisos que deben solicitarse al usuario.
	 * @return Conjunto de permisos o {@code null} si no son necesarios.
	 */
	public Set<SignaturePermission> getPermissions() {
		return this.permissions;
	}
}
