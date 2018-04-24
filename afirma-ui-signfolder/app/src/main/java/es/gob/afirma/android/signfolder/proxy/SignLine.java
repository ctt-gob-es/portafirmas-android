package es.gob.afirma.android.signfolder.proxy;

import java.util.ArrayList;

/**
 * L&iacute;nea de firma.
 */
public class SignLine {

	private final String type;
	private final ArrayList<SignLineElement> signers;

	/**
	 * Crea la l&iacute;nea de firma indicando el firmante. Por defecto, se considera que
	 * la operaci&oacute;n son de Firma.
	 * @param type Tipo de operaci&oacute;n.
	 * @param signers Receptores de la petici&oacute;n.
	 */
	public SignLine(final String type, final ArrayList<SignLineElement> signers) {
		this.type = type;
		this.signers = signers;
	}

	/**
	 * Crea la l&iacute;nea de firma indicando el firmante. Por defecto, se considera que
	 * la operaci&oacute;n son de Firma.
	 * @param type Tipo de operaci&oacute;n.
	 * @param signers Receptores de la petici&oacute;n.
	 */
	public SignLine(final String type) {
		this.type = type;
		this.signers = new ArrayList<SignLineElement>();
	}

	/**
	 * Crea la l&iacute;nea de firma indicando el firmante. Por defecto, se considera que
	 * la operaci&oacute;n son de Firma.
	 * @param signers Estado de la operaci&oacute;n de firma.
	 */
	public SignLine(final ArrayList<SignLineElement> signers) {
		this.type = "FIRMA";
		this.signers = signers;
	}

	/**
	 * Recupera los firmantes de la operaci&oacute;n.
	 * @return Firmante.
	 */
	public ArrayList<SignLineElement> getSigners() {
		return this.signers;
	}

	/**
	 * Recupera el tipo de operaci&oacute;n que se ha realizado.
	 * @return Firma o Visto bueno
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * Agrega un nuevo elemento a la l&iacute;nea de firma.
	 * @param sle Elemento de la l&iacute;nea de firma.
	 */
	public void addElement(final SignLineElement sle) {
		this.signers.add(sle);
	}
}
