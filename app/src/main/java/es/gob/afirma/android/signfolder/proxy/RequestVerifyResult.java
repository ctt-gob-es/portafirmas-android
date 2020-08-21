package es.gob.afirma.android.signfolder.proxy;

/**
 * Clase que representa el resultado de la llamada del servicio de validación de peticiones.
 */
public class RequestVerifyResult {

    /**
     * Atributo que indica si la operación se ha realizado correctamente.
     */
    private final boolean statusOk;

    /**
     * Atributo que representa el mensaje de error en caso de que la operación falle.
     */
    private final String errorMsg;

    /**
     * Constructor por defecto.
     * @param statusOk Resultado de la operación.
     * @param errorMsg Mensaje de error.
     */
    public RequestVerifyResult(boolean statusOk, String errorMsg) {
        this.statusOk = statusOk;
        this.errorMsg = errorMsg;
    }

    /**
     * Método get del atributo <i>statusOk</i>.
     * @return el valor del atributo.
     */
    public boolean isStatusOk() {
        return statusOk;
    }

    /**
     * Método get del atributo <i>errorMsg</i>
     * @return el valor del atributo.
     */
    public String getErrorMsg() {
        return errorMsg;
    }
}