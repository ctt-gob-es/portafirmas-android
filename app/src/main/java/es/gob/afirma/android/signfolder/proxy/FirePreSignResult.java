package es.gob.afirma.android.signfolder.proxy;

import java.util.List;

/**
 * Respuesta de url para firma mediante Cl@ve Firma.
 * @author Sergio Martinez Rico
 */
public class FirePreSignResult {

	/** Referencia de la petici&oacute;n. */
	private final String transactionId;

	/** URL de autenticacion para la firma. */
	private String url;

	private final TriphaseRequest[] triphaseRequests;

	/**
	 * Construye un objeto de petici&oacute;n de prefirma con Cl@ve Firma.
	 * @param transactionId Referencia &uacute;nica de la petici&oacute;n.
	 * @param url URL de autenticacion para la firma.
	 * @param triphaseRequests Informaci&oacute;n de las peticiones de
	 * firma y su resultado parcial.
	 */
	public FirePreSignResult(final String transactionId, final String url, final TriphaseRequest[] triphaseRequests) {
		this.transactionId = transactionId;
		this.url = url;
		this.triphaseRequests = triphaseRequests;
	}

	/**
	 * Recupera la referencia de la transaccion de FIRe para la firma de documentos.
	 * @return Identificador de la transacci&oacute;n.
	 */
	public String getTransactionId() {
		return this.transactionId;
	}

	/**
	 * Recupera la URL de autenticacion para la firma.
	 * @return URL de autenticacion para la firma.
	 */
	public String getURL() {
		return this.url;
	}

	/**
	 * Establece la URL del servicio de autorizaci&oacute;n.
	 * @param url URL al servicio de autorizaci&oacute;n.
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Obtiene las firmas informaci&oacute;n de las trif&aacute;sicas parciales
	 * de la petici&oacute;n.
	 * @return Listado de resultados parciales de las firmas trif&aacute;sicas.
	 */
	public TriphaseRequest[] getTriphaseRequests() {
		return this.triphaseRequests;
	}

    @Override
    public String toString() {
        return new StringBuilder().append("FirePreSignResult:")
                .append("\n\tTransactionId: ").append(this.transactionId)
                .append("\n\tURL:").append(this.url)
                .append("\n\tArray TriphaseRequests:\n")
                .append(this.triphaseRequests == null ? "null" : this.triphaseRequests.length)
                .toString();
    }
}