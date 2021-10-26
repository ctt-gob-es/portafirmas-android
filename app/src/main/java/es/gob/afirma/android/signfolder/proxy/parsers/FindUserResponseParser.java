package es.gob.afirma.android.signfolder.proxy.parsers;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import es.gob.afirma.android.signfolder.proxy.FireLoadDataResult;
import es.gob.afirma.android.signfolder.proxy.ServerControlledException;
import es.gob.afirma.android.signfolder.proxy.XmlUtils;
import es.gob.afirma.android.user.configuration.GenericUser;

public class FindUserResponseParser {

    private static final String ROOT_RESPONSE_NODE = "rsfinduser"; //$NON-NLS-1$
    private static final String USERS_NODE = "users"; //$NON-NLS-1$
    private static final String ERROR_NODE = "err"; //$NON-NLS-1$
    private static final String USER_NODE = "user"; //$NON-NLS-1$
    private static final String ID_NODE = "id"; //$NON-NLS-1$
    private static final String DNI_NODE = "dni"; //$NON-NLS-1$
    private static final String NAME_NODE = "name"; //$NON-NLS-1$

    private FindUserResponseParser() {
        // No instanciable
    }

    /**
     * Analiza un documento XML y, en caso de tener el formato correcto, obtiene el listado de
     * usuarios encontrados.
     * @param doc Documento XML.
     * @return Objeto con los datos del XML.
     * @throws IllegalArgumentException Cuando el XML no tiene el formato esperado.
     * @throws ServerControlledException Cuando el servidor devuelve un error controlado.
     */
    public static List<GenericUser> parse(final Document doc) throws  IllegalArgumentException,
            ServerControlledException {

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
        int i = 0;
        NodeList childs = requestNode.getChildNodes();
        while (i < childs.getLength() && childs.item(i).getNodeType() != Node.ELEMENT_NODE) {
            i++;
        }
        if (i >= childs.getLength()) {
            throw new IllegalArgumentException("Se ha obtenido un XML vacio");
        }

        Node elementNode = childs.item(i);
        if (elementNode.getNodeName().equals(ERROR_NODE)) {
            throw new ServerControlledException("", XmlUtils.getTextContent(elementNode));
        }

        // Si no encontramos el listado de usuarios devolvemos un error de XML invalido
        if (!elementNode.getNodeName().equals(USERS_NODE)) {
            throw new IllegalArgumentException("Se ha encontrado un nodo distinto al listado de elementos " + elementNode.getNodeName());
        }

        // Recorremos el listado de nodos de usuario recopilando la informacion de cada uno de ellos
        i = -1;
        childs = elementNode.getChildNodes();
        List<GenericUser> result = new ArrayList<>();
        while ((i = XmlUtils.nextNodeElementIndex(childs, ++i)) > -1) {

            Element userElement = (Element) childs.item(i);
            if (!userElement.getNodeName().equals(USER_NODE)) {
                throw new IllegalArgumentException("Se ha encontrado un nodo no valido en el listado de usuarios: " + userElement.getNodeName());
            }

            result.add(GenericUserParser.parse(userElement));
        }

        return result;
    }
}
