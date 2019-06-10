package es.gob.afirma.android.signfolder.proxy;

/**
 * Excepci&oacute;n que denota que el Portafirmas m&oacute;vil est&aacute; intentando conectarse
 * con un proxy antiguo que carece de una operaci&oacute;n a la que se intent&oacute; acceder.
 */
public class OldProxyException extends Exception {

    /**
     * Crea la excepci&oacute;n con un mensaje.
     * @param msg Mensaje que describe el problema producido.
     */
    public OldProxyException(String msg) {
        super(msg);
    }
}
