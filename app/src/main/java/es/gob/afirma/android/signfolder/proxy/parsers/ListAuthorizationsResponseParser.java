package es.gob.afirma.android.signfolder.proxy.parsers;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import es.gob.afirma.android.signfolder.DateTimeFormatter;
import es.gob.afirma.android.signfolder.proxy.ServerControlledException;
import es.gob.afirma.android.signfolder.proxy.XmlUtils;
import es.gob.afirma.android.user.configuration.Authorization;
import es.gob.afirma.android.user.configuration.AuthorizationState;
import es.gob.afirma.android.user.configuration.AuthorizedType;
import es.gob.afirma.android.user.configuration.GenericUser;

public class ListAuthorizationsResponseParser {

    private static final String ROOT_RESPONSE_NODE = "rsauthlist"; //$NON-NLS-1$
    private static final String ERROR_NODE = "err"; //$NON-NLS-1$
    private static final String AUTHORIZATIONS_LIST_NODE = "authlist"; //$NON-NLS-1$
    private static final String AUTHORIZATION_NODE = "auth"; //$NON-NLS-1$
    private static final String AUTHORIZATION_ID_ATTRIBUTE = "id"; //$NON-NLS-1$
    private static final String AUTHORIZATION_TYPE_ATTRIBUTE = "type"; //$NON-NLS-1$
    private static final String AUTHORIZATION_STATE_ATTRIBUTE = "state"; //$NON-NLS-1$
    private static final String AUTHORIZATION_START_DATE_ATTRIBUTE = "startdate"; //$NON-NLS-1$
    private static final String AUTHORIZATION_REVOCATION_DATE_ATTRIBUTE = "revdate"; //$NON-NLS-1$
    private static final String AUTHORIZATION_SENDED_ATTRIBUTE = "sended"; //$NON-NLS-1$

    private static final String USER_NODE = "user"; //$NON-NLS-1$
    private static final String AUTHORIZED_USER_NODE = "authuser"; //$NON-NLS-1$
    private static final String OBSERVATIONS_NODE = "observations"; //$NON-NLS-1$

    private ListAuthorizationsResponseParser() {
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
    public static List<Authorization> parse(final Document doc) throws ServerControlledException {

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

        // Si no encontramos el listado de autorizaciones devolvemos un error de XML invalido
        if (!elementNode.getNodeName().equals(AUTHORIZATIONS_LIST_NODE)) {
            throw new IllegalArgumentException("Se ha encontrado un nodo distinto al listado de autorizaciones " + elementNode.getNodeName());
        }

        // Recorremos el listado de nodos recopilando la informacion de cada uno de ellos
        i = -1;
        childs = elementNode.getChildNodes();
        List<Authorization> result = new ArrayList<>();
        while ((i = XmlUtils.nextNodeElementIndex(childs, ++i)) > -1) {

            Element authElement = (Element) childs.item(i);
            if (!authElement.getNodeName().equals(AUTHORIZATION_NODE)) {
                throw new IllegalArgumentException("Se ha encontrado un nodo no valido en el listado de usuarios: " + authElement.getNodeName());
            }

            result.add(parseAuthorization(authElement));
        }

        return result;
    }

    private static Authorization parseAuthorization(Element authElement) {

        String id = authElement.getAttribute(AUTHORIZATION_ID_ATTRIBUTE);
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("No se ha proporcionado identificador de la autorizacion");
        }

        String typeValue = authElement.getAttribute(AUTHORIZATION_TYPE_ATTRIBUTE);
        if (typeValue == null || typeValue.isEmpty()) {
            throw new IllegalArgumentException("No se ha proporcionado el tipo de la autorizacion");
        }
        AuthorizedType type = AuthorizedType.parse(typeValue);
        if (type == null) {
            throw new IllegalArgumentException("No se ha proporcionado un tipo de autorizacion valido");
        }

        String stateParam = authElement.getAttribute(AUTHORIZATION_STATE_ATTRIBUTE);
        if (stateParam == null || stateParam.isEmpty()) {
            throw new IllegalArgumentException("No se ha proporcionado el estado de la autorizacion");
        }
        AuthorizationState state = AuthorizationState.parse(stateParam);
        if (state == null) {
            throw new IllegalArgumentException("No se ha proporcionado un estado de autorizacion valido");
        }

        Date startDate;
        String startDateParam = authElement.getAttribute(AUTHORIZATION_START_DATE_ATTRIBUTE);
        if (startDateParam == null || startDateParam.isEmpty()) {
            startDate = new Date();
        } else {
            try {
                startDate = DateTimeFormatter.getAppFormatterInstance().parse(startDateParam);
            }
            catch (Exception e) {
                throw new IllegalArgumentException("No se ha proporcionado una fecha valida de inicio de la autorizacion: " + startDateParam);
            }
        }

        Date revDate = null;
        String revDateParam = authElement.getAttribute(AUTHORIZATION_REVOCATION_DATE_ATTRIBUTE);
        if (revDateParam != null && !revDateParam.isEmpty()) {
            try {
                revDate = DateTimeFormatter.getAppFormatterInstance().parse(revDateParam);
            }
            catch (Exception e) {
                throw new IllegalArgumentException("No se ha proporcionado una fecha valida de fin de la autorizacion: " + revDateParam);
            }
        }

        boolean sended = false;
        String sendedParam = authElement.getAttribute(AUTHORIZATION_SENDED_ATTRIBUTE);
        if (sendedParam != null && !sendedParam.isEmpty()) {
            sended = Boolean.parseBoolean(sendedParam);
        }

        // Obtenemos la informacion del usuario emisor de la autorizacion
        NodeList childs = authElement.getChildNodes();
        int i = XmlUtils.nextNodeElementIndex(childs, 0);
        if (i == -1 || !childs.item(i).getNodeName().equals(USER_NODE)) {
            throw new IllegalArgumentException("No se ha encontrado el usuario emisor de la autorizacion");
        }
        GenericUser user = GenericUserParser.parse((Element) childs.item(i));

        // Obtenemos la informacion del usuario autorizado
        i = XmlUtils.nextNodeElementIndex(childs, ++i);
        if (i == -1 || !childs.item(i).getNodeName().equals(AUTHORIZED_USER_NODE)) {
            throw new IllegalArgumentException("No se ha encontrado el usuario autorizado");
        }
        GenericUser authorizedUser = GenericUserParser.parse((Element) childs.item(i));

        // Obtenemos las observaciones (que son opcionales)
        i = XmlUtils.nextNodeElementIndex(childs, ++i);

        String observations = null;
        if (i != -1 && childs.item(i).getNodeName().equals(OBSERVATIONS_NODE)) {
            observations = childs.item(i).getTextContent();
        }

        // Componemos el objeto de autorizacion
        final Authorization authorization = new Authorization();
        authorization.setId(id);
        authorization.setType(type);
        authorization.setState(state);
        authorization.setStartDate(startDate);
        authorization.setRevDate(revDate);
        authorization.setSended(sended);
        authorization.setUser(user);
        authorization.setAuthoricedUser(authorizedUser);
        authorization.setObservations(observations);

        return authorization;
    }
}
