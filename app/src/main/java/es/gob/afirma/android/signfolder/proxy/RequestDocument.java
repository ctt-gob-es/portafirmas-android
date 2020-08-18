package es.gob.afirma.android.signfolder.proxy;

/** Informaci&oacute;n de un documento de una solicitud de firma. */
public class RequestDocument {

	/** Identificador del documento */
	private final String id;

	/** Nombre del documento */
	private final String name;

	/** Tama&ntilde;o del documento */
	private final int size;

	/** MimeType del documento */
	private final String mimeType;

	/** Crea un documento englobado en una petici&oacute;n de firma/multifirma.
	 * @param id Identificador del documento.
	 * @param name Nombre.
	 * @param size Tama&ntilde;o.
	 * @param mimeType MimeType.
	 */
	public RequestDocument(final String id, final String name, final int size, final String mimeType) {
		this.id = id;
		this.name = name;
		this.size = size;
		this.mimeType = mimeType;
	}

	/** Recupera el identificador del documento.
	 * @return Identificador. */
	public String getId() {
		return this.id;
	}

	/** Recupera el nombre del documento.
	 * @return Nombre del documento. */
	public String getName() {
		return this.name;
	}

	/** Recupera el tama&ntilde;o del documento.
	 * @return Tama&ntilde;o del documento. */
	public int getSize() {
		return this.size;
	}

	/** Recupera el mimetype del documento.
	 * @return Mimetype del documento. */
	public String getMimeType() {
		return this.mimeType;
	}

	@Override
	public String toString() {
		return getName();
	}
}
