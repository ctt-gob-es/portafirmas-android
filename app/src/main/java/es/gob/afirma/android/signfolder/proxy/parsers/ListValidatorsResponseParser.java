package es.gob.afirma.android.signfolder.proxy.parsers;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import es.gob.afirma.android.signfolder.proxy.ServerControlledException;
import es.gob.afirma.android.signfolder.proxy.XmlUtils;
import es.gob.afirma.android.user.configuration.GenericUser;
import es.gob.afirma.android.user.configuration.Validator;

public class ListValidatorsResponseParser {

    private static final String ROOT_RESPONSE_NODE = "rsvalidlist"; //$NON-NLS-1$
    private static final String ERROR_NODE = "err"; //$NON-NLS-1$
    private static final String VALIDATORS_LIST_NODE = "validlist"; //$NON-NLS-1$
    private static final String VALIDATOR_NODE = "valid"; //$NON-NLS-1$
    private static final String VALIDATOR_FORAPPS_ATTRIBUTE = "forapps"; //$NON-NLS-1$

    private static final String USER_NODE = "user"; //$NON-NLS-1$

    private ListValidatorsResponseParser() {
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
    public static List<Validator> parse(final Document doc) throws  IllegalArgumentException,
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
        NodeList childs = requestNode.getChildNodes();
        int i = XmlUtils.nextNodeElementIndex(childs, 0);
        if (i == -1) {
            throw new IllegalArgumentException("Se ha obtenido un XML vacio");
        }

        Node elementNode = childs.item(i);
        if (elementNode.getNodeName().equals(ERROR_NODE)) {
            throw new ServerControlledException("", XmlUtils.getTextContent(elementNode));
        }

        // Si no encontramos el listado de validadores devolvemos un error de XML invalido
        if (!elementNode.getNodeName().equals(VALIDATORS_LIST_NODE)) {
            throw new IllegalArgumentException("Se ha encontrado un nodo distinto al listado de validadores. " + elementNode.getNodeName());
        }

        // Recorremos el listado de nodos recopilando la informacion de cada uno de ellos
        i = -1;
        childs = elementNode.getChildNodes();
        List<Validator> result = new ArrayList<>();
        while ((i = XmlUtils.nextNodeElementIndex(childs, ++i)) > -1) {

            Element userElement = (Element) childs.item(i);
            if (!userElement.getNodeName().equals(VALIDATOR_NODE)) {
                throw new IllegalArgumentException("Se ha encontrado un nodo no valido en el listado de usuarios: " + userElement.getNodeName());
            }

            result.add(parseValidator(userElement));
        }

        return result;
    }

    private static Validator parseValidator(Element authElement) {

        boolean forApps = Boolean.parseBoolean(authElement.getAttribute(VALIDATOR_FORAPPS_ATTRIBUTE));

        // Obtenemos la informacion del usuario emisor de la autorizacion
        NodeList childs = authElement.getChildNodes();
        int i = XmlUtils.nextNodeElementIndex(childs, 0);
        if (i == -1 || !childs.item(i).getNodeName().equals(USER_NODE)) {
            throw new IllegalArgumentException("No se ha encontrado el usuario validador");
        }
        GenericUser user = GenericUserParser.parse((Element) childs.item(i));

        // Componemos el objeto de autorizacion
        final Validator validator = new Validator();
        validator.setForApps(forApps);
        validator.setUser(user);

        return validator;
    }
}
