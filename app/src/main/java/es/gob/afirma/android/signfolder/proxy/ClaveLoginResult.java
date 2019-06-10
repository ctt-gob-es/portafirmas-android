package es.gob.afirma.android.signfolder.proxy;

/**
 * Resultado parcial o final de una operaci&oacute;n de login con Cl@ve.
 */
public class ClaveLoginResult {

	private final boolean statusOk;

	private String sessionId;

	private String redirectionUrl;

	private String errorMsg;

	/**
	 * Resultado de una petici&oacute;n login.
	 * @param ok Resultado hasta el momento de la petici&oacute;n de login.
	 */
	public ClaveLoginResult(final boolean ok) {
		this.statusOk = ok;
	}

	/**
	 * Recupera el identificador de autenticaci&oacute;n.
	 * @return Identificador de autenticaci&oacute;n.
	 */
	public String getSessionId() {
		return this.sessionId;
	}

	/**
	 * Establece el identificador de autenticaci&oacute;n.
	 * @param id Identificador de autenticaci&oacute;n.
	 */
	void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * Recupera la URL de redirecci&oacute;n.
	 * @return URL de redireccion para la autenticaci&oacute;n.
	 */
	public String getRedirectionUrl() {
		return this.redirectionUrl;
	}

	/**
	 * Establece la URL de redirecci&oacute;n.
	 * @param redirectionUrl URL de redireccion para la autenticaci&oacute;n.
	 */
	void setRedirectionUrl(String redirectionUrl) {
		this.redirectionUrl = redirectionUrl;
	}

	/**
	 * Recupera el mensaje de error.
	 * @return Mensaje de error.
	 */
	public String getErrorMsg() {
		return errorMsg;
	}

	/**
	 * Establece el mensaje de error.
	 * @param errorMsg Mensaje de error.
	 */
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	/**
	 * Recupera el resultado de la operaci&oacute;n sobre la petici&oacute;n.
	 * @return {@code true} si la operaci&oacute;n finaliz&oacute; correctamente, {@code false}
	 * en caso contrario.
	 */
	public boolean isStatusOk() {
		return this.statusOk;
	}
}
