package es.gob.afirma.android.signfolder.proxy.parsers;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import es.gob.afirma.android.signfolder.proxy.CreationRoleResponse;
import es.gob.afirma.android.signfolder.proxy.ServerControlledException;

/**
 * Clase que representa el parseador de las respuesta provenientes de Portafirma-proxy para la creación de roles.
 */
public final class CreateRoleResponseParser {

    /**
     * Constante que define el nombre del nodo principal de respuesta.
     */
    private static final String CREATE_RESPONSE_NODE = "crtnwrl"; //$NON-NLS-1$

    /**
     * Constante que define el nombre del nodo de respuesta.
     */
    private static final String RESPONSE_NODE = "resp"; //$NON-NLS-1$

    /**
     * Constante que define el nombre del nodo de respuesta de la operación.
     */
    private static final String OP_RESULT = "success"; //$NON-NLS-1$

    /**
     * Constante que define el nombre del elemento que representa el error producido durante el proceso.
     */
    private static final String ERROR_MESSAGE = "errorMsg"; //$NON-NLS-1$

    /**
     * Método encargado de realizar el parseo de la respuesta de creación de rol.
     *
     * @param doc Documento que representa la respuesta recibida.
     * @return un objeto de tipo CreationRoleResponse que representa la respuesta recibida.
     * @throws ServerControlledException si se produce algún fallo.
     */
    public static CreationRoleResponse parseResponse(final Document doc) throws ServerControlledException {
        if (doc == null) {
            throw new IllegalArgumentException("El documento proporcionado no puede ser nulo");  //$NON-NLS-1$
        }

        final Element docElement = doc.getDocumentElement();

        if (!CREATE_RESPONSE_NODE.equalsIgnoreCase(docElement.getNodeName())) {
            throw new IllegalArgumentException("El elemento raiz del XML debe ser '" + CREATE_RESPONSE_NODE + //$NON-NLS-1$
                    "' y aparece: " + doc.getDocumentElement().getNodeName()); //$NON-NLS-1$
        }

        Node response = docElement.getElementsByTagName(RESPONSE_NODE).item(0);
        if (response != null) {
            final String opResultAsString = response.getAttributes().getNamedItem(OP_RESULT).getNodeValue();
            final String errorMsg = response.getAttributes().getNamedItem(ERROR_MESSAGE).getNodeValue();
            boolean opResult = Boolean.parseBoolean(opResultAsString);
            return new CreationRoleResponse(opResult, errorMsg);
        }
        return null;
    }

}
