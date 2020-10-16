package es.gob.afirma.android.signfolder.proxy.parsers;

import org.w3c.dom.Document;

/**
 * Clase encargada de parsear las respuesta recibidas del servicio de actualización de las notificaciones push.
 */
public final class UpdatePushNotsResponseParser {

    /**
     * Constante que define el nombre del nodo principal de la respuesta de la petición.
     */
    private static final String DOC_NODE = "pdtpshsttsrs"; //$NON-NLS-1$

    /**
     * Método encargado de parsear la respuesta de la petición.
     *
     * @param remoteDocument Documento a parsear.
     * @return Resultado de la operación recibido.
     */
    public static String parse(Document remoteDocument) {
        if (!DOC_NODE.equalsIgnoreCase(remoteDocument.getNodeName())) {
            throw new IllegalArgumentException("El elemento raiz del XML debe ser '" + //$NON-NLS-1$
                    DOC_NODE + "' y aparece: " + //$NON-NLS-1$
                    remoteDocument.getDocumentElement().getNodeName());
        }
        return remoteDocument.getDocumentElement().getTextContent();
    }
}
