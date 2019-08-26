package es.gob.afirma.android.signfolder.proxy;

/**
 * Resultado de una operaci&oacute;n de firma con FIRe.
 */
public class FireSignResult {

    /** No se han producido errores. */
    public static final int NO_ERROR = 0;
    /** Error en la comunicaci&oacute;n con FIRe. */
    public static final int ERROR_COMMUNICATION = 1;
    /** Error en una o m&aacute;s peticiones de firma. */
    public static final int ERROR_REQUEST = 2;
    /** Error en uno o m&aacute;s de los documentos firmados. */
    public static final int ERROR_DOCUMENT = 3;

    private boolean status;
    private int errorType;

    /**
     * Crea el objeto con el resultado de la operaci&oacute;n.
     * @param status {@code true} si todas las peticiones se procesaron correctamente,
     * {@code false} en caso contrario.
     * @param errorType Tipo de error, si lo hay, que se ha detectado.
     */
    public FireSignResult(boolean status, int errorType) {
        this.status = status;
        this.errorType = errorType;
    }

    /**
     * Indica si se han firmado todas las peticiones correctamente.
     * @return {@code true} si todas las peticiones se procesaron correctamente,
     * {@code false} en caso contrario.
     */
    public boolean isStatus() {
        return status;
    }

    /**
     * Indica el tipo de error detectado si corresponde.
     * @return Tipo de error.
     */
    public int getErrorType() {
        return errorType;
    }
}
