package es.gob.afirma.android.signfolder.proxy;

import androidx.annotation.NonNull;

/**
 * Resultado obtenido al cargar peticiones de firma en FIRe.
 */
public class FireLoadDataResult {

	/** Estado de la transacci&oacute;n. */
	private boolean statusOk;

	/** URL de autorizaci&oacute;n para la firma. */
	private String url;

	/**
	 * Construye un objeto de resultado de la carga de datos en FIRe.
	 * @param statusOk Estado de la transacci&oacute;n.
	 * @param url URL de autorizaci&oacute;n para la firma.
	 */
	public FireLoadDataResult(final boolean statusOk, final String url) {
		this.statusOk = statusOk;
		this.url = url;
	}

	/**
	 * Recupera el estado de la transacci&oacute;n despu&eacute;s de la carga de datos.
	 * @return {@code true} si los datos se han cargado correctamente, {@code false} si no se pudo
	 * cargar ning&uacute;n dato.
	 */
	public boolean isStatusOk() {
		return this.statusOk;
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

    @NonNull
	@Override
    public String toString() {
        return "FireLoadDataResult:" +
                "\n\tStatus: " + this.statusOk +
                "\n\tURL:" + this.url;
    }
}