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
     * Constructor con 1 parámetro.
     *
     * @param operationResult Resultado de la operación.
     */
    public CreationRoleResponse(boolean operationResult) {
        this.success = operationResult;
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
}
