package es.gob.afirma.android.signfolder.proxy;

/**
 * Clase que representa la respuesta de Portafirmas-proxy para la creación de roles.
 */
public class CreationRoleResponse {

    /**
     * Atributo que representa el resultado de la operación.
     */
    private boolean success;

    /**
     * Atributo que representa el mensaje de error de la operación.
     */
    private String errorMsg;

    /**
     * Constructor con 2 parámetro.
     *
     * @param operationResult Resultado de la operación.
     * @param errorMsgParam Mensaje de error de la operación.
     */
    public CreationRoleResponse(boolean operationResult, String errorMsgParam) {
        this.success = operationResult;
        this.errorMsg = errorMsgParam;
    }

    /**
     * Método get del atributo <i>success</i>.
     *
     * @return el valor del atributo.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Método set del atributo <i>success</i>.
     *
     * @param success nuevo valor del atributo.
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Método get del atributo <i>errorMsg</i>.
     *
     * @return el valor del atributo.
     */
    public String getErrorMsg() {
        return errorMsg;
    }

    /**
     * Método set del atributo <i>errorMsg</i>.
     *
     * @param errorMsg nuevo valor del atributo.
     */
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
