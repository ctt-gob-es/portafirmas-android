package es.gob.afirma.android.signfolder.proxy;

import androidx.annotation.NonNull;

/** Informaci&oacute;n de un documento de una solicitud de firma. */
public final class SignRequestDocument extends RequestDocument implements Cloneable {


	public static final String CRYPTO_OPERATION_SIGN = "sign"; //$NON-NLS-1$

	/** Operaci&oacute;n que se debe realizar sobre el documento (sign, cosign o countersign). */
	private final String cryptoOperation;

	/** Formato de firma a aplicar */
	private final String signFormat;

	/** Formato de firma a aplicar */
	private final String messageDigestAlgorithm;

	/** Par&aacute;metros de firma conforme a las especificaciones de los extraParams de @firma. */
	private String params;

	private boolean needConfirmation = false;

	/** Crea un documento englobado en una petici&oacute;n de firma/multifirma.
	 * @param id Identificador del documento.
	 * @param name Nombre.
	 * @param size Tama&ntilde;o.
	 * @param mimeType MimeType.
	 * @param signFormat Formato de firma a aplicar.
	 * @param messageDigestAlgorithm Algoritmo de huella digital que debe usarse en la firma del documento.
	 * @param params Par&aacute;metros de configuraci&oacute;n de la firma.
	 * @param cryptoOperation Identificador de la operaci&oacute;n que se debe realizar sobre le documento.
	 */
	public SignRequestDocument(final String id, final String name, final int size, final String mimeType, final String signFormat, final String messageDigestAlgorithm, final String params, final String cryptoOperation) {
		super(id, name, size, mimeType);
		this.signFormat = signFormat;
		this.messageDigestAlgorithm = messageDigestAlgorithm;
		this.params = params;
		this.cryptoOperation = cryptoOperation != null ? cryptoOperation : CRYPTO_OPERATION_SIGN;
	}

	/**
	 * Recupera la operaci&oacute;n que debe realizarse sobre el documento (firma, cofirma, contrafirma de hojas o contrafirma de arbol).
	 * @return Identificador del tipo de operaci&oacute;n.
	 */
	public String getCryptoOperation() {
		return this.cryptoOperation;
	}

	/**
	 * Recupera el formato de firma que se le debe aplicar al documento.
	 * @return Formato de firma que se le debe aplicar al documento.
	 */
	public String getSignFormat() {
		return this.signFormat;
	}

	/**
	 * Recupera el algoritmo de huella digital asociado al algoritmo de firma que se desea utilizar.
	 * @return Algoritmo de huella digial que se usara en la firma.
	 */
	public String getMessageDigestAlgorithm() {
		return this.messageDigestAlgorithm;
	}

	/**
	 * Recupera los par&aacute;metros de configuraci&oacute;n para la firma
	 * conforme al formato de extraParams de @firma.
	 * @return Par&aacute;metros de configuraci&oacute;n de la firma.
	 */
	public String getParams() {
		return this.params;
	}

	/**
	 * Establece los par&aacute;metros de configuraci&oacute;n para la firma
	 * conforme al formato de extraParams de @firma.
	 * @param params Par&aacute;metros de configuraci&oacute;n de la firma.
	 */
	public void setParams(String params) {
		this.params = params;
	}

	/**
	 * Indica si el documento necesit&oacute; alguna confirmaci&oacute;n para completar su firma.
	 * @return {@code true} indica que se requiere confirmaci&oacute;n, {@code false}
	 * en caso contrario.
	 */
	public boolean isNeedConfirmation() {
		return this.needConfirmation;
	}

	/**
	 * Establece si el documento necesit&oacute; alguna confirmaci&oacute;n para completar su firma.
	 * @param needConfirmation {@code true} indica que se requiere confirmaci&oacute;n, {@code false}
	 * en caso contrario.
	 */
	public void setNeedConfirmation(boolean needConfirmation) {
		this.needConfirmation = needConfirmation;
	}

	@Override
	public String toString() {
		return getName();
	}

	@NonNull
	@Override
	public Object clone() {
		try {
			return super.clone();
		}
		catch (CloneNotSupportedException e) {
			return null;
		}
	}
}
