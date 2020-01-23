package es.gob.afirma.android.signfolder.proxy;

/**
 * Excepci&oacute;n que se&ntilde;ala un error controlado en servidor.
 */
public class ServerControlledException extends Exception {

    private String errorCode;

    /**
     * Crea la excepci&oacute;n controlada.
     * @param errorCode C&oacute;digo de error.
     */
    public ServerControlledException(String errorCode) {
        super();
        this.errorCode = errorCode;
    }

    /**
     * Crea la excepci&oacute;n controlada.
     * @param errorCode C&oacute;digo de error.
     * @param cause M&oacute;tivo del error.
     */
    public ServerControlledException(String errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }

    /**
     * Crea la excepci&oacute;n controlada.
     * @param errorCode C&oacute;digo de error.
     * @param msg Mensaje de error.
     */
    public ServerControlledException(String errorCode, String msg) {
        super(msg);
        this.errorCode = errorCode;
    }

    /**
     * Crea la excepci&oacute;n controlada.
     * @param errorCode C&oacute;digo de error.
     * @param msg Mensaje de error
     * @param cause M&oacute;tivo del error.
     */
    public ServerControlledException(String errorCode, String msg, Throwable cause) {
        super(msg, cause);
        this.errorCode = errorCode;
    }

    /**
     * Recupera el c&oacute;digo de error indicado por el servidor.
     * @return C&oacute;digo de error.
     */
    public String getErrorCode() {
        return this.errorCode;
    }

    @Override
    public String toString() {
        return getClass().toString() + ": Cod " + this.errorCode + ": " + getMessage();
    }
}
