package es.gob.afirma.android.crypto;

import java.util.List;

import es.gob.afirma.android.user.configuration.ConfigurationRole;

/**
 * Resultado del proceso de autenticaci&oacute;n.
 */
public class AuthenticationResult {

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

    private String error;
    private String errorMsg;

    private boolean cancelled = false;

    /**
     * Lista de roles asociados al usuario.
     */
    private List<ConfigurationRole> roleLs;

    /**
     * Resultado de una petici&oacute;n login.
     * @param ok Resultado del login.
     */
    public AuthenticationResult(final boolean ok) {
        this.statusOk = ok;
    }

    /**
     * Resultado de una petici&oacute;n login.
     * @param error C&oacute;digo del error producido.
     * @param errorMsg Mensaje de error.
     */
    public AuthenticationResult(final String error, final String errorMsg) {
        this.statusOk = false;
        this.error = error;
        this.errorMsg = errorMsg;
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
     * Recupera el error.
     * @return Error.
     */
    public String getError() {
        return error;
    }

    /**
     * Recupera el mensaje de error.
     * @return Mensaje de error.
     */
    public String getErrorMsg() {
        return errorMsg;
    }

    /**
     * Establece el  error.
     * @param error Error.
     */
    public void setError(String error) {
        this.error = error;
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
     * Establece el certificado de autenticaci&oacute;n.
     * @param certB64 Certificado en base 64.
     */
    public void setCertificateB64(String certB64) {
        this.certificateB64 = certB64;
    }

    /**
     * Recupera el alias del certificado de autenticaci&oacute;n y firma.
     * @return Alias del certificado local.
     */
    public String getCertAlias() {
        return this.certAlias;
    }

    /**
     * Establece el alias del certificado de autenticaci&oacute;n y firma.
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

    /**
     * Recupera la lista de roles asociados al usuario.
     * @return lista de roles asociados al usuario.
     */
    public List<ConfigurationRole> getRoleLs() {
        return roleLs;
    }

    /**
     * Establece la lista de roles asociados al usuario.
     * @param roleLs Lista de roles asociados al usuario.
     */
    public void setRoleLs(List<ConfigurationRole> roleLs) {
        this.roleLs = roleLs;
    }

    /**
     * Establece si la operaci&oacute;n se cancel&oacute; o no.
     * @param cancelled {@code true} si la operaci&oacute;n fue cancelada, {@code false} en caso
     * contrario.
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Indica si la operaci&oacute;n fue cancelada o no.
     * @return {@code true} si la operaci&oacute;n fue cancelada, {@code false} en caso
     * contrario.
     */
    public boolean isCancelled() {
        return cancelled;
    }
}
