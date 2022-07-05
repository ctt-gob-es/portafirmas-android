package es.gob.afirma.android.signfolder.proxy.parsers;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import es.gob.afirma.android.signfolder.proxy.GenericResponse;
import es.gob.afirma.android.signfolder.proxy.XmlUtils;

/**
 * Analizador para el procesado de respuestas XML que indican si la operación finalizó
 * correctamente o no.
 */
public class GenericResponseParser {

    private static final String ROOT_RESPONSE_NODE = "rs"; //$NON-NLS-1$
    private static final String RESULT_NODE = "result"; //$NON-NLS-1$
    private static final String ERROR_NODE = "errorMsg"; //$NON-NLS-1$

    private GenericResponseParser() {
        // No instanciable
    }

    /**
     * Analiza un documento XML y, en caso de tener el formato correcto, obtiene el listado de
     * usuarios encontrados.
     * @param doc Documento XML.
     * @return Objeto con los datos del XML.
     * @throws IllegalArgumentException Cuando el XML no tiene el formato esperado.
     */
    public static GenericResponse parse(final Document doc) throws  IllegalArgumentException {

        if (doc == null) {
            throw new IllegalArgumentException("El documento proporcionado no puede ser nulo");  //$NON-NLS-1$
        }

        final Node requestNode = doc.getDocumentElement();

        if (!ROOT_RESPONSE_NODE.equalsIgnoreCase(requestNode.getNodeName())) {
            throw new IllegalArgumentException("El elemento raiz del XML debe ser '" + //$NON-NLS-1$
                    ROOT_RESPONSE_NODE + "' y aparece: " + //$NON-NLS-1$
                    doc.getDocumentElement().getNodeName());
        }

        // Buscamos el primer nodo elemento
        final NodeList childs = requestNode.getChildNodes();
        int i = XmlUtils.nextNodeElementIndex(childs, 0);
        if (i == -1 || !((Element) childs.item(i)).getNodeName().equals(RESULT_NODE)) {
            throw new IllegalArgumentException("No se encontro el nodo " + RESULT_NODE + " en la respuesta");
        }

        GenericResponse response;

        // Si se ha declarado una respuesta de exito, se prepara una respuesta de exito
        final boolean success = Boolean.parseBoolean(XmlUtils.getTextContent(childs.item(i)));
        if (success) {
            response = new GenericResponse(true);
        }
        else {
            // Si se declara un mensaje de error, se establece
            i = XmlUtils.nextNodeElementIndex(childs, ++i);
            if (i != -1 && childs.item(i).getNodeName().equals(ERROR_NODE)){
                String errorMessage = XmlUtils.getTextContent(childs.item(i));
                response = new GenericResponse(errorMessage);
            }
            // Si no, se declara unicamente que se recibio un error
            else {
                response = new GenericResponse(false);
            }
        }

        return response;
    }
}
