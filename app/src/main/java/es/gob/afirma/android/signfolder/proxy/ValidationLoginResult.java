package es.gob.afirma.android.signfolder.proxy;

/**
 * Resultado final del proceso de login.
 */
public class ValidationLoginResult {

	private boolean statusOk;

	private String dni;

	/**
     * Alias del certificado seleccionado por el usuario. Solo se usara cuando se utiliza
     * certificado local.
	 */
	private String certAlias;

    /**
     * Certificado local usado para auteticar al usuario.
     */
    private String certificateB64;

	private String errorMsg;

	/**
	 * Resultado de una petici&oacute;n login.
	 * @param ok Resultado del login.
	 */
	public ValidationLoginResult(final boolean ok) {
		this.statusOk = ok;
	}

	/**
	 * Establece el resultado de la operaci&oacute;n.
	 * @param statusOk Resultado de la operaci&oacute;n.
	 */
	public void setStatusOk(boolean statusOk) {
		this.statusOk = statusOk;
	}

	/**
	 * Recupera el DNI del usuario que accede a la aplicaci&oacute;n.
	 * @return DNI del usuario.
	 */
	public String getDni() {
		return this.dni;
	}

	/**
	 * Establece el DNI del usuario que accede a la aplicaci&oacute;n.
	 * @param dni DNI del usuario.
	 */
	public void setDni(String dni) {
		this.dni = dni;
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
     * Recupera el certificado local de autenticaci&oacute;n.
     * @return Certificado en base 64.
     */
    public String getCertificateB64() {
        return this.certificateB64;
    }

    /**
     * Establece el certificado de autenticaci&ooacute;n.
     * @param certB64 Certificado en base 64.
     */
    public void setCertificateB64(String certB64) {
        this.certificateB64 = certB64;
    }

    /**
     * Recupera el alias del certificado de autenticaci&ooacute;n y firma.
     * @return Alias del certificado local.
     */
    public String getCertAlias() {
        return this.certAlias;
    }

    /**
     * Establece el alias del certificado de autenticaci&ooacute;n y firma.
     * @param certAlias Alias del certificado local.
     */
    public void setCertAlias(String certAlias) {
        this.certAlias = certAlias;
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
