package es.gob.afirma.android.signfolder.proxy;

import java.io.Serializable;

/**
 * Respuesta genérica de éxito o error.
 */
public class GenericResponse implements Serializable {

    /** Serial ID. */
    private static final long serialVersionUID = 6355977342461701185L;

    public static final int ERROR_GENERIC = 1;

    public static final int ERROR_COMMUNICATION = 2;

    public static final int ERROR_LOST_SESSION = 3;

    /**
     * Bandera que indica si la operación se ha realizado correctamente
     * <i>true</i> o no <i>false</i>.
     */
    private boolean success = false;

    /**
     * Indica si se ha producido un error durante la operación.
     */
    private String errorMessage = null;

    /**
     * Indica si se ha producido un error durante la operación.
     */
    private int errorType = ERROR_GENERIC;

    /**
     * Construye el objeto indicando que la operación finalizó con éxito.
     * @param success Bandera que indica si el resultado ha sido correcto.
     */
    public GenericResponse(final boolean success) {
        this.success = success;
    }

    /**
     * Construye el objeto indicando el mensaje del error obtenido.
     * @param errorMessage Mensaje de error.
     */
    public GenericResponse(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Construye el objeto indicando el tipo del error obtenido.
     * @param errorType Tipo de error.
     */
    public GenericResponse(final int errorType) {
        this.errorType = errorType;

        if (this.errorType == ERROR_GENERIC) {
            this.errorMessage = "No en la llamada al servicio proxy";
        }
        else if (this.errorType == ERROR_COMMUNICATION) {
            this.errorMessage = "Error en la comunicacion con el servicio proxy";
        }
        else if (this.errorType == ERROR_LOST_SESSION) {
            this.errorMessage = "Se ha perdido la sesion con el servicio proxy";
        }
        else {
            this.errorMessage = "Error desconocido en la llamada al servicio proxy";
        }
    }

    /**
     * Indica si la operación tuvo éxito o no.
     * @return {@code true} si la operación tuvo éxito, {@code false}
     * en caso contrario.
     */
    public boolean isSuccess() {
        return this.success;
    }

    /**
     * Recupera el mensaje de error producido.
     * @return Mensaje de error.
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }
}
