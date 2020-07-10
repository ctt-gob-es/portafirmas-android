package es.gob.afirma.android.signfolder;

import java.security.cert.X509Certificate;

/**
 * Interfaz que implementan los elementos con webview a los que se puede
 * solicitar desde el exterior que recarguen el contenido.
 */
public interface WebViewAuthorizable {

    /**
     * Permite el acceso a la pagina con el certificado indicado.
     * @param cert Certificado.
     */
    void allowPermission(X509Certificate cert);

    /**
     * Deniega el acceso a la pagina con el certificado indicado.
     * @param cert Certificado.
     */
    void denyPermission(X509Certificate cert);
}
