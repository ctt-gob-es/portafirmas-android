package es.gob.afirma.android.signfolder.proxy.parsers;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import es.gob.afirma.android.signfolder.proxy.GetRoleRequest;
import es.gob.afirma.android.signfolder.proxy.GetUserRequest;
import es.gob.afirma.android.signfolder.proxy.PartialResponseRolesList;
import es.gob.afirma.android.signfolder.proxy.PartialResponseUserList;
import es.gob.afirma.android.signfolder.proxy.PartialSignRequestsList;
import es.gob.afirma.android.signfolder.proxy.RequestDocument;
import es.gob.afirma.android.signfolder.proxy.ServerControlledException;
import es.gob.afirma.android.signfolder.proxy.SignRequest;
import es.gob.afirma.android.signfolder.proxy.SignRequest.RequestType;
import es.gob.afirma.android.signfolder.proxy.SignRequestDocument;
import es.gob.afirma.android.signfolder.proxy.XmlUtils;
import es.gob.afirma.android.user.configuration.AuthorizedType;
import es.gob.afirma.android.user.configuration.ContactData;
import es.gob.afirma.android.user.configuration.UserProfile;

/**
 * Analizador de XML para la generaci&oacute;n de listas de peticiones de firma.
 */
public final class RequestListResponseParser {

    /**
     * Constantes que representan nombres de nodos XML.
     */
    private static final String LIST_NODE = "list"; //$NON-NLS-1$
    private static final String ERROR_NODE = "err"; //$NON-NLS-1$
    private static final String NUM_REQUESTS_ATTRIBUTE = "n"; //$NON-NLS-1$
    private static final String CD_ATTRIBUTE = "cd"; //$NON-NLS-1$

    /**
     * Valor usado por el Portafirmas para indicar que una petici&oacute;n es de firma.
     */
    private static final String REQUEST_TYPE_SIGN = "FIRMA"; //$NON-NLS-1$

    /**
     * Valor usado por el Portafirmas para indicar que una petici&oacute;n es de visto bueno.
     */
    private static final String REQUEST_TYPE_APPROVE = "VISTOBUENO"; //$NON-NLS-1$

    /**
     * Analiza un documento XML y, en caso de tener el formato correcto, obtiene una lista de peticiones de firma.
     *
     * @param doc Documento XML.
     * @return Objeto con los datos del XML.
     * @throws IllegalArgumentException Cuando el XML no tiene el formato esperado.
     */
    public static PartialSignRequestsList parse(final Document doc) throws ServerControlledException {

        if (doc == null) {
            throw new IllegalArgumentException("El documento proporcionado no puede ser nulo");  //$NON-NLS-1$
        }

        final Element docElement = doc.getDocumentElement();

        if (ERROR_NODE.equalsIgnoreCase(docElement.getNodeName())) {
            final String errorCode = docElement.getAttribute(CD_ATTRIBUTE);
            throw new ServerControlledException(errorCode, XmlUtils.getTextContent(docElement));
        }

        if (!LIST_NODE.equalsIgnoreCase(docElement.getNodeName())) {
            throw new IllegalArgumentException("El elemento raiz del XML debe ser '" + LIST_NODE + //$NON-NLS-1$
                    "' y aparece: " + doc.getDocumentElement().getNodeName()); //$NON-NLS-1$
        }

        final String numRequestAttrValue = docElement.getAttribute(NUM_REQUESTS_ATTRIBUTE);
        int numRequests;
        try {
            numRequests = numRequestAttrValue == null ? 0 : Integer.parseInt(numRequestAttrValue);
        } catch (final Exception e) {
            numRequests = 0;
        }

        final Vector<SignRequest> listSignRequest = new Vector<>();
        final NodeList requestNodes = docElement.getChildNodes();
        for (int i = 0; i < requestNodes.getLength(); i++) {
            // Nos aseguramos de procesar solo nodos de tipo Element
            i = XmlUtils.nextNodeElementIndex(requestNodes, i);
            if (i == -1) {
                break;
            }
            listSignRequest.addElement(SignRequestParser.parse(requestNodes.item(i)));
        }

        return new PartialSignRequestsList(listSignRequest, numRequests);
    }

    /**
     * Analiza un documento XML y, en caso de tener el formato correcto, obtiene una lista de usuarios.
     *
     * @param doc Documento XML.
     * @return Objeto con los datos del XML.
     * @throws IllegalArgumentException Cuando el XML no tiene el formato esperado.
     */
    public static PartialResponseRolesList parseRolesReq(final Document doc) throws ServerControlledException {

        if (doc == null) {
            throw new IllegalArgumentException("El documento proporcionado no puede ser nulo");  //$NON-NLS-1$
        }

        final Element docElement = doc.getDocumentElement();

        if (ERROR_NODE.equalsIgnoreCase(docElement.getNodeName())) {
            final String errorCode = docElement.getAttribute(CD_ATTRIBUTE);
            throw new ServerControlledException(errorCode, XmlUtils.getTextContent(docElement));
        }

        if (!LIST_NODE.equalsIgnoreCase(docElement.getNodeName())) {
            throw new IllegalArgumentException("El elemento raiz del XML debe ser '" + LIST_NODE + //$NON-NLS-1$
                    "' y aparece: " + doc.getDocumentElement().getNodeName()); //$NON-NLS-1$
        }

        final String numRequestAttrValue = docElement.getAttribute(NUM_REQUESTS_ATTRIBUTE);
        int numRequests;
        try {
            numRequests = numRequestAttrValue == null ? 0 : Integer.parseInt(numRequestAttrValue);
        } catch (final Exception e) {
            numRequests = 0;
        }

        final Vector<GetRoleRequest> listRolesRequest = new Vector<>();
        final NodeList requestNodes = docElement.getChildNodes();
        for (int i = 0; i < requestNodes.getLength(); i++) {
            // Nos aseguramos de procesar solo nodos de tipo Element
            i = XmlUtils.nextNodeElementIndex(requestNodes, i);
            if (i == -1) {
                break;
            }
            listRolesRequest.addElement(GetRoleRequestParser.parse(requestNodes.item(i)));
        }

        return new PartialResponseRolesList(listRolesRequest, numRequests);
    }

    /**
     * Método que parsea la respuesta del servicio de obtención de usuarios.
     *
     * @param doc Documento a parsear.
     * @return un objeto de tipo PartialResponseUserList que representa la respuesta parseada.
     * @throws ServerControlledException si algo ha ido mal.
     */
    public static PartialResponseUserList parseUsersReq(final Document doc) throws ServerControlledException {
        if (doc == null) {
            throw new IllegalArgumentException("El documento proporcionado no puede ser nulo");  //$NON-NLS-1$
        }

        final Element docElement = doc.getDocumentElement();

        if (ERROR_NODE.equalsIgnoreCase(docElement.getNodeName())) {
            final String errorCode = docElement.getAttribute(CD_ATTRIBUTE);
            throw new ServerControlledException(errorCode, XmlUtils.getTextContent(docElement));
        }

        if (!LIST_NODE.equalsIgnoreCase(docElement.getNodeName())) {
            throw new IllegalArgumentException("El elemento raiz del XML debe ser '" + LIST_NODE + //$NON-NLS-1$
                    "' y aparece: " + doc.getDocumentElement().getNodeName()); //$NON-NLS-1$
        }

        final String numRequestAttrValue = docElement.getAttribute(NUM_REQUESTS_ATTRIBUTE);
        int numRequests;
        try {
            numRequests = numRequestAttrValue == null ? 0 : Integer.parseInt(numRequestAttrValue);
        } catch (final Exception e) {
            numRequests = 0;
        }

        final Vector<GetUserRequest> listUserRequest = new Vector<>();
        final NodeList requestNodes = docElement.getChildNodes();
        for (int i = 0; i < requestNodes.getLength(); i++) {
            // Nos aseguramos de procesar solo nodos de tipo Element
            i = XmlUtils.nextNodeElementIndex(requestNodes, i);
            if (i == -1) {
                break;
            }
            listUserRequest.addElement(GetUserRequestParser.parse(requestNodes.item(i)));
        }

        return new PartialResponseUserList(listUserRequest, numRequests);
    }

    /**
     * Deshace los cambios que hizo el proxy para asegurar que el XML est&aacute;ba bien formado.
     *
     * @param value Valor que normalizar.
     * @return Valor normalizado.
     */
    static String normalizeValue(final String value) {
        return value.trim().replace("&_lt;", "<").replace("&_gt;", ">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    private static class SignRequestParser {

        private static final String REQUEST_NODE = "rqt"; //$NON-NLS-1$
        private static final String ID_ATTRIBUTE = "id"; //$NON-NLS-1$
        private static final String PRIORITY_ATTRIBUTE = "priority"; //$NON-NLS-1$
        private static final String WORKFLOW_ATTRIBUTE = "workflow"; //$NON-NLS-1$
        private static final String FORWARD_ATTRIBUTE = "forward"; //$NON-NLS-1$
        private static final String TYPE_ATTRIBUTE = "type"; //$NON-NLS-1$
        private static final String SUBJECT_NODE = "subj"; //$NON-NLS-1$
        private static final String SENDER_NODE = "snder"; //$NON-NLS-1$
        private static final String VIEW_NODE = "view"; //$NON-NLS-1$
        private static final String DATE_NODE = "date"; //$NON-NLS-1$
        private static final String EXPIRATION_DATE_NODE = "expdate"; //$NON-NLS-1$
        private static final String DOCUMENTS_NODE = "docs"; //$NON-NLS-1$

        static SignRequest parse(final Node signRequestNode) {

            if (!REQUEST_NODE.equalsIgnoreCase(signRequestNode.getNodeName())) {
                throw new IllegalArgumentException("Se ha encontrado el elemento '" + signRequestNode.getNodeName() + //$NON-NLS-1$
                        "' en el listado de peticiones"); //$NON-NLS-1$
            }

            /* Atributos */
            String ref;
            int priority = 1;    // Valor por defecto
            boolean workflow = false; // Valor por defecto
            boolean forward = false; // Valor por defecto
            RequestType type = RequestType.SIGNATURE; // Valor por defecto

            /* Elementos */
            final String subject;
            final String sender;
            final String view;
            final String date;
            final String expirationDate;
            final Vector<SignRequestDocument> signRequestDocumentsList = new Vector<>();
            final Vector<RequestDocument> requestDocumentsList = new Vector<>();

            // Cargamos los atributos
            Node attributeNode;
            final NamedNodeMap attributes = signRequestNode.getAttributes();
            attributeNode = attributes.getNamedItem(ID_ATTRIBUTE);
            if (attributeNode == null) {
                throw new IllegalArgumentException("No se ha encontrado el atributo obligatorio '" + //$NON-NLS-1$
                        ID_ATTRIBUTE + "' en un peticion de firma"); //$NON-NLS-1$
            }
            ref = attributeNode.getNodeValue();

            attributeNode = attributes.getNamedItem(PRIORITY_ATTRIBUTE);
            if (attributeNode != null) {
                try {
                    priority = Integer.parseInt(attributeNode.getNodeValue());
                } catch (final Exception e) {
                    throw new IllegalArgumentException("La prioridad de la peticion con referencia '" + //$NON-NLS-1$
                            ref + "' no es valida. Debe ser un valor entero"); //$NON-NLS-1$
                }
            }

            attributeNode = attributes.getNamedItem(WORKFLOW_ATTRIBUTE);
            if (attributeNode != null) {
                try {
                    workflow = XmlUtils.parseBoolean(attributeNode.getNodeValue());
                } catch (final Exception e) {
                    throw new IllegalArgumentException("El valor del atributo " + WORKFLOW_ATTRIBUTE + //$NON-NLS-1$
                            "de la peticion con referencia '" + ref + "' no es valido. Debe ser 'true' o 'false'"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }

            attributeNode = attributes.getNamedItem(FORWARD_ATTRIBUTE);
            if (attributeNode != null) {
                try {
                    forward = XmlUtils.parseBoolean(attributeNode.getNodeValue());
                } catch (final Exception e) {
                    throw new IllegalArgumentException("El valor del atributo " + FORWARD_ATTRIBUTE + //$NON-NLS-1$
                            "de la peticion con referencia '" + ref + "' no es valido. Debe ser 'true' o 'false'"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }

            attributeNode = attributes.getNamedItem(TYPE_ATTRIBUTE);
            if (attributeNode != null && attributeNode.getNodeValue() != null) {
                if (REQUEST_TYPE_SIGN.equalsIgnoreCase(attributeNode.getNodeValue())) {
                    type = RequestType.SIGNATURE;
                } else if (REQUEST_TYPE_APPROVE.equalsIgnoreCase(attributeNode.getNodeValue())) {
                    type = RequestType.APPROVE;
                } else {
                    type = null;
                }
            }

            // Cargamos los elementos
            int elementIndex = 0;
            final NodeList childNodes = signRequestNode.getChildNodes();
            elementIndex = XmlUtils.nextNodeElementIndex(childNodes, elementIndex);
            if (elementIndex == -1 || !SUBJECT_NODE.equalsIgnoreCase(childNodes.item(elementIndex).getNodeName())) {
                throw new IllegalArgumentException("La peticion con referencia '" + ref + //$NON-NLS-1$
                        "' no contiene el elemento " + SUBJECT_NODE); //$NON-NLS-1$
            }
            subject = normalizeValue(XmlUtils.getTextContent(childNodes.item(elementIndex)));

            elementIndex = XmlUtils.nextNodeElementIndex(childNodes, ++elementIndex);
            if (elementIndex == -1 || !SENDER_NODE.equalsIgnoreCase(childNodes.item(elementIndex).getNodeName())) {
                throw new IllegalArgumentException("La peticion con referencia '" + ref + //$NON-NLS-1$
                        "' no contiene el elemento " + SENDER_NODE); //$NON-NLS-1$
            }
            sender = normalizeValue(XmlUtils.getTextContent(childNodes.item(elementIndex)));

            elementIndex = XmlUtils.nextNodeElementIndex(childNodes, ++elementIndex);
            if (elementIndex == -1 || !VIEW_NODE.equalsIgnoreCase(childNodes.item(elementIndex).getNodeName())) {
                throw new IllegalArgumentException("La peticion con referencia '" + ref + //$NON-NLS-1$
                        "' no contiene el elemento " + VIEW_NODE); //$NON-NLS-1$
            }
            view = XmlUtils.getTextContent(childNodes.item(elementIndex));

            elementIndex = XmlUtils.nextNodeElementIndex(childNodes, ++elementIndex);
            if (elementIndex == -1 || !DATE_NODE.equalsIgnoreCase(childNodes.item(elementIndex).getNodeName())) {
                throw new IllegalArgumentException("La peticion con referencia '" + ref + //$NON-NLS-1$
                        "' no contiene el elemento " + DATE_NODE); //$NON-NLS-1$
            }
            date = XmlUtils.getTextContent(childNodes.item(elementIndex));

            elementIndex = XmlUtils.nextNodeElementIndex(childNodes, ++elementIndex);
            if (elementIndex == -1 || !EXPIRATION_DATE_NODE.equalsIgnoreCase(childNodes.item(elementIndex).getNodeName())) {
                // No hay fecha de caducidad, es opcional
                expirationDate = null;
            } else {
                String expDate = XmlUtils.getTextContent(childNodes.item(elementIndex));
                expirationDate = expDate != null && !expDate.isEmpty() ? expDate : null;
                // Obtenemos el siguiente elemento
                elementIndex = XmlUtils.nextNodeElementIndex(childNodes, ++elementIndex);
            }

            if (elementIndex == -1 || !DOCUMENTS_NODE.equalsIgnoreCase(childNodes.item(elementIndex).getNodeName())) {
                throw new IllegalArgumentException("La peticion con referencia '" + ref + //$NON-NLS-1$
                        "' no contiene el elemento " + DOCUMENTS_NODE); //$NON-NLS-1$
            }
            final NodeList docsList = childNodes.item(elementIndex).getChildNodes();
            for (int i = 0; i < docsList.getLength(); i++) {
                // Nos aseguramos de procesar solo nodos de tipo Element
                i = XmlUtils.nextNodeElementIndex(docsList, i);
                if (i == -1) {
                    break;
                }
                signRequestDocumentsList.addElement(SignRequestDocumentParser.parse(docsList.item(i)));
            }


            final SignRequestDocument[] tmpRet = new SignRequestDocument[signRequestDocumentsList.size()];

            signRequestDocumentsList.copyInto(tmpRet);

            return new SignRequest(ref, subject, sender, view, date, expirationDate, priority, workflow, forward, type, tmpRet, null);
        }
    }

    /**
     * Clase encargada de gestionar el parseo de la petición para
     * la obtención de la lista de los roles de un usuario.
     */
    private static class GetRoleRequestParser {

        /**
         * Constantes que representan los nombres de las etiquetas XML.
         */
        private static final String REQUEST_NODE = "rsgtrl"; //$NON-NLS-1$
        private static final String ROL_NODE = "rol"; //$NON-NLS-1$
        private static final String ID_REQUEST_ELEMENT = "idReq";
        private static final String NAME_ELEMENT = "name"; //$NON-NLS-1$
        private static final String SURNAME_ELEMENT = "surname"; //$NON-NLS-1$
        private static final String SECOND_SURNAME_ELEMENT = "secondSurname"; //$NON-NLS-1$
        private static final String LDAP_USER_ELEMENT = "LDAPUser"; //$NON-NLS-1$
        private static final String ID_USER_ELEMENT = "ID"; //$NON-NLS-1$
        private static final String POSITION_ELEMENT = "position"; //$NON-NLS-1$
        private static final String HEADQUARTER_ELEMENT = "headquarter"; //$NON-NLS-1$
        private static final String PROFILES_ELEMENT = "profiles"; //$NON-NLS-1$
        private static final String PROFILE_ELEMENT = "profile"; //$NON-NLS-1$
        private static final String DATA_CONTACTS_ELEMENT = "dataContacts"; //$NON-NLS-1$
        private static final String EMAIL_ELEMENT = "email"; //$NON-NLS-1$
        private static final String NOTIFY_ELEMENT = "notify"; //$NON-NLS-1$
        private static final String ATTACH_SIGNATURE_ELEMENT = "attachSignature"; //$NON-NLS-1$
        private static final String ATTACH_REPORT_ELEMENT = "attachReport"; //$NON-NLS-1$
        private static final String PAGE_SIZE_ELEMENT = "pageSize"; //$NON-NLS-1$
        private static final String APPLY_APP_FILTER_ELEMENT = "applyAppFilter"; //$NON-NLS-1$
        private static final String SHOW_PREVIOUS_SIGNER_ELEMENT = "showPreviousSigner"; //$NON-NLS-1$
        private static final String VERIFIER_IDENTIFIER_ELEMENT = "verifierIdentifier"; //$NON-NLS-1$
        private static final String VERIFIER_NAME_ELEMENT = "verifierName"; //$NON-NLS-1$
        private static final String STATUS_ELEMENT = "status"; //$NON-NLS-1$
        private static final String SENT_RECEIVED_ELEMENT = "sentReceived"; //$NON-NLS-1$
        private static final String TYPE_ELEMENT = "type"; //$NON-NLS-1$
        private static final String SENDER_RECEIVER_ELEMENT = "senderReceiver"; //$NON-NLS-1$
        private static final String INIT_DATE_ELEMENT = "initDate"; //$NON-NLS-1$
        private static final String AUTHORIZATION_ELEMENT = "authorization"; //$NON-NLS-1$
        private static final String END_DATE_ELEMENT = "endDate"; //$NON-NLS-1$

        private static final String DATE_FORMAT = "dd/MM/yyyy";

        /**
         * Método que parsea la respuesta decibida por el proxy para el servicio 'GetUserByRole'.
         * La respuesta debe seguir la siguiente estructura:
         *
         * <rsgtrl>
         * <!-- 1 o más elementos -->
         * <rol>
         * <idReq>String</idReq>
         * <name>String</name>
         * <surname>String</surname>
         * <secondSurname>String</secondSurname>
         * <LDAPUser>String</LDAPUser>
         * <ID>String</ID>
         * <position>String</position>
         * <headquarter>String</headquarter>
         * <profiles>
         * <!-- 1 o más elementos -->
         * <profile>UserProfile</profile>
         * </profiles>
         * <dataContacts>
         * <!-- 1 o más elementos -->
         * <dataContact>
         * <email>String</email>
         * <notify>boolean</notify>
         * </dataContact>
         * </dataContacts>
         * <attachSignature>boolean</attachSignature>
         * <attachReport>boolean</attachReport>
         * <pageSize>int</pageSize>
         * <applyAppFilter>boolean</applyAppFilter>
         * <showPreviousSigner>boolean</showPreviousSigner>
         * <verifierIdentifier>String</verifierIdentifier>
         * <verifierName>String</verifierName>
         * <status>String</status>
         * <sentReceived>String</sentReceived>
         * <type>AuthorizedType</type>
         * <senderReceiver>String</senderReceiver>
         * <initDate>Date</initDate>
         * <authorization>Date</authorization>
         * <endDate>Date</endDate>
         * </rol>
         * </rsgtrl>
         *
         * @param getRoleReqNode Nodo principal de la respuesta.
         * @return la respuesta recibida como un objeto del tipo GetRoleRequest.
         */
        static GetRoleRequest parse(final Node getRoleReqNode) {

            if (!REQUEST_NODE.equalsIgnoreCase(getRoleReqNode.getNodeName())) {
                throw new IllegalArgumentException("Se ha encontrado el elemento '" + getRoleReqNode.getNodeName() + //$NON-NLS-1$
                        "' en el listado de roles"); //$NON-NLS-1$
            }

            /* Elementos */
            Node idReqNode = null;
            Node nameNode = null;
            Node surnameNode = null;
            Node secondSurnameNode = null;
            Node LDAPUserNode = null;
            Node IDNode = null;
            Node positionNode = null;
            Node headquarterNode = null;
            NodeList profilesNode = null;
            NodeList dataContactNode = null;
            Node attachSignatureNode = null;
            Node attachReportNode = null;
            Node pageSizeNode = null;
            Node applyAppFilterNode = null;
            Node showPreviousSignerNode = null;
            Node verifierIdentifierNode = null;
            Node verifierNameNode = null;
            Node statusNode = null;
            Node sentReceivedNode = null;
            Node typeNode = null;
            Node senderReceiverNode = null;
            Node initDateNode = null;
            Node authorizationNode = null;
            Node endDateNode = null;

            /* Valores */
            String idReq = null;
            String name = null;
            String surname = null;
            String secondSurname = null;
            String LDAPUser = null;
            String ID = null;
            String position = null;
            String headquarter = null;
            List<UserProfile> profiles = null;
            List<ContactData> dataContact = null;
            boolean attachSignature = false;
            boolean attachReport = false;
            int pageSize = -1;
            boolean applyAppFilter = false;
            boolean showPreviousSigner = false;
            String verifierIdentifier = null;
            String verifierName = null;
            String status = null;
            String sentReceived = null;
            AuthorizedType type = null;
            String senderReceiver = null;
            Date initDate = null;
            Date authorization = null;
            Date endDate = null;


            // Cargamos los elementos hijos.
            NodeList elementsNode;
            elementsNode = getRoleReqNode.getChildNodes();

            if (elementsNode == null || elementsNode.getLength() < 1) {
                throw new IllegalArgumentException("No se han encontrado elementos hijos '" + //$NON-NLS-1$
                        ROL_NODE + "' en la respuesta proxy"); //$NON-NLS-1$
            }

            // Recorremos los elementos hijos del nodo 'rsgtrl'.
            for (int i = 0; i < elementsNode.getLength(); i++) {

                // Recuperamos el elemento 'rol'.
                Node rolNode = elementsNode.item(i);
                if (rolNode == null || !rolNode.getNodeName().equalsIgnoreCase(ROL_NODE)) {
                    throw new IllegalArgumentException("No se ha encontrado el elemento '" //$NON-NLS-1$
                            + ROL_NODE + "' en la respuesta proxy."); //$NON-NLS-1$
                }
                if (!rolNode.hasChildNodes()) {
                    throw new IllegalArgumentException("No se ha encontrado ningún elemento " + //$NON-NLS-1$
                            "hijo del nodo '" + ROL_NODE + "' en la respuesta proxy"); //$NON-NLS-1$ //$NON-NLS-2$
                }

                // Recorremos los elementos hijos del nodo 'rol'.
                NodeList rolChildren = rolNode.getChildNodes();
                for (int u = 0; u < rolChildren.getLength(); u++) {
                    Node node = rolChildren.item(u);
                    // Puesto que cabe la posibilidad de que los elementos vengan desordenados,
                    // implementamos un método a parte para extraer la información al nodo correspondiente.
                    updateNodeValues(node, idReqNode, nameNode, surnameNode, secondSurnameNode,
                            LDAPUserNode, IDNode, positionNode, headquarterNode, profilesNode,
                            dataContactNode, attachSignatureNode, attachReportNode, pageSizeNode,
                            applyAppFilterNode, showPreviousSignerNode, verifierIdentifierNode,
                            verifierNameNode, statusNode, sentReceivedNode, typeNode,
                            senderReceiverNode, initDateNode, authorizationNode, endDateNode);
                }

                // recuperamos los valores de los nodos.
                if (idReqNode != null) {
                    idReq = normalizeValue(XmlUtils.getTextContent(idReqNode));
                }
                if (nameNode != null) {
                    name = normalizeValue(XmlUtils.getTextContent(nameNode));
                }
                if (surnameNode != null) {
                    surname = normalizeValue(XmlUtils.getTextContent(surnameNode));
                }
                if (secondSurnameNode != null) {
                    secondSurname = normalizeValue(XmlUtils.getTextContent(secondSurnameNode));
                }
                if (LDAPUserNode != null) {
                    LDAPUser = normalizeValue(XmlUtils.getTextContent(LDAPUserNode));
                }
                if (IDNode != null) {
                    ID = normalizeValue(XmlUtils.getTextContent(IDNode));
                }
                if (positionNode != null) {
                    position = normalizeValue(XmlUtils.getTextContent(positionNode));
                }
                if (headquarterNode != null) {
                    headquarter = normalizeValue(XmlUtils.getTextContent(headquarterNode));
                }
                if (profilesNode != null && profilesNode.getLength() > 0) {
                    profiles = new ArrayList<>();
                    for (int o = 0; o < profilesNode.getLength(); o++) {
                        Node profile = profilesNode.item(o);
                        if (profile != null && profile.getNodeName().equalsIgnoreCase(PROFILE_ELEMENT)) {
                            UserProfile up = UserProfile.getUserProfile(normalizeValue(XmlUtils.getTextContent(profile)));
                            profiles.add(up);
                        }
                    }
                }
                if (dataContactNode != null && dataContactNode.getLength() > 0) {
                    dataContact = new ArrayList<>();
                    for (int e = 0; e < dataContactNode.getLength(); e++) {
                        NodeList dataContactsNode = dataContactNode.item(e).getChildNodes();
                        if (dataContactsNode != null && dataContactsNode.getLength() > 0) {
                            ContactData contactData = new ContactData();
                            for (int o = 0; o < dataContactsNode.getLength(); o++) {
                                Node node = dataContactsNode.item(o);
                                if (node != null && node.getNodeName().equalsIgnoreCase(EMAIL_ELEMENT)) {
                                    contactData.setEmail(normalizeValue(XmlUtils.getTextContent(node)));
                                }
                                if (node != null && node.getNodeName().equalsIgnoreCase(NOTIFY_ELEMENT)) {
                                    contactData.setNotify(XmlUtils.parseBoolean(node.getNodeValue()));
                                }
                            }
                            dataContact.add(contactData);
                        }
                    }
                }
                if (attachSignatureNode != null) {
                    attachSignature = XmlUtils.parseBoolean(attachSignatureNode.getNodeValue());
                }
                if (attachReportNode != null) {
                    attachReport = XmlUtils.parseBoolean(attachReportNode.getNodeValue());
                }
                if (pageSizeNode != null) {
                    pageSize = Integer.valueOf(normalizeValue(XmlUtils.getTextContent(pageSizeNode)));
                }
                if (applyAppFilterNode != null) {
                    applyAppFilter = XmlUtils.parseBoolean(applyAppFilterNode.getNodeValue());
                }
                if (showPreviousSignerNode != null) {
                    showPreviousSigner = XmlUtils.parseBoolean(showPreviousSignerNode.getNodeValue());
                }
                if (verifierIdentifierNode != null) {
                    verifierIdentifier = normalizeValue(XmlUtils.getTextContent(verifierIdentifierNode));
                }
                if (verifierNameNode != null) {
                    verifierName = normalizeValue(XmlUtils.getTextContent(verifierNameNode));
                }
                if (statusNode != null) {
                    status = normalizeValue(XmlUtils.getTextContent(statusNode));
                }
                if (sentReceivedNode != null) {
                    sentReceived = normalizeValue(XmlUtils.getTextContent(sentReceivedNode));
                }
                if (typeNode != null) {
                    type = AuthorizedType.getAuthorizedType(normalizeValue(XmlUtils.getTextContent(typeNode)));
                }
                if (senderReceiverNode != null) {
                    senderReceiver = normalizeValue(XmlUtils.getTextContent(senderReceiverNode));
                }
                if (initDateNode != null) {
                    String date = normalizeValue(XmlUtils.getTextContent(initDateNode));
                    try {
                        initDate = new SimpleDateFormat(DATE_FORMAT).parse(date);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("La fecha indicada '" + date + "' no puede ser parseada al formato '" + DATE_FORMAT + "'."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                }
                if (authorizationNode != null) {
                    String date = normalizeValue(XmlUtils.getTextContent(authorizationNode));
                    try {
                        authorization = new SimpleDateFormat(DATE_FORMAT).parse(date);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("La fecha indicada '" + date + "' no puede ser parseada al formato '" + DATE_FORMAT + "'."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                }
                if (endDateNode != null) {
                    String date = normalizeValue(XmlUtils.getTextContent(endDateNode));
                    try {
                        endDate = new SimpleDateFormat(DATE_FORMAT).parse(date);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("La fecha indicada '" + date + "' no puede ser parseada al formato '" + DATE_FORMAT + "'."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                }


            }
            return new GetRoleRequest(idReq, name, surname, secondSurname, LDAPUser, ID, position,
                    headquarter, profiles, dataContact, attachSignature, attachReport, pageSize,
                    applyAppFilter, showPreviousSigner, verifierIdentifier, verifierName, status,
                    sentReceived, type, senderReceiver, initDate, authorization, endDate);
        }

        /**
         * Método que actualiza el valor de un determinado parámetro según el nombre del nodo proporcionado.
         *
         * @param nodeValue              Node que contienen el valor a copiar en el objeto.
         * @param idReqNode              Elemento <i>idReq</i>.
         * @param nameNode               Elemento <i>name</i>.
         * @param surnameNode            Elemento <i>surname</i>.
         * @param secondSurnameNode      Elemento <i>secondSurname</i>.
         * @param LDAPUserNode           Elemento <i>LDAPUser</i>.
         * @param IDNode                 Elemento <i>ID</i>.
         * @param positionNode           Elemento <i>position</i>.
         * @param headquarterNode        Elemento <i>headquarter</i>.
         * @param profilesNode           Elemento <i>profiles</i>.
         * @param dataContactNode        Elemento <i>dataContacts</i>.
         * @param attachSignatureNode    Elemento <i>attachSignature</i>.
         * @param attachReportNode       Elemento <i>attachReport</i>.
         * @param pageSizeNode           Elemento <i>pageSize</i>.
         * @param applyAppFilterNode     Elemento <i>applyAppFilter</i>.
         * @param showPreviousSignerNode Elemento <i>showPreviousSigner</i>.
         * @param verifierIdentifierNode Elemento <i>verifierIdentifier</i>.
         * @param verifierNameNode       Elemento <i>verifierName</i>.
         * @param statusNode             Elemento <i>status</i>.
         * @param sentReceivedNode       Elemento <i>sentReceived</i>.
         * @param typeNode               Elemento <i>type</i>.
         * @param senderReceiverNode     Elemento <i>senderReceiver</i>.
         * @param initDateNode           Elemento <i>initDate</i>.
         * @param authorizationNode      Elemento <i>authorization</i>.
         * @param endDateNode            Elemento <i>endDate</i>.
         */
        private static void updateNodeValues(Node nodeValue, Node idReqNode, Node nameNode,
                                             Node surnameNode, Node secondSurnameNode,
                                             Node LDAPUserNode, Node IDNode, Node positionNode,
                                             Node headquarterNode, NodeList profilesNode,
                                             NodeList dataContactNode, Node attachSignatureNode,
                                             Node attachReportNode, Node pageSizeNode,
                                             Node applyAppFilterNode, Node showPreviousSignerNode,
                                             Node verifierIdentifierNode, Node verifierNameNode,
                                             Node statusNode, Node sentReceivedNode, Node typeNode,
                                             Node senderReceiverNode, Node initDateNode,
                                             Node authorizationNode, Node endDateNode) {
            if (nodeValue == null) {
                return;
            }
            String nodeName = nodeValue.getNodeName();
            switch (nodeName) {
                case ID_REQUEST_ELEMENT:
                    idReqNode = nodeValue.cloneNode(true);
                    break;
                case NAME_ELEMENT:
                    nameNode = nodeValue.cloneNode(true);
                    break;
                case SURNAME_ELEMENT:
                    surnameNode = nodeValue.cloneNode(true);
                    break;
                case SECOND_SURNAME_ELEMENT:
                    secondSurnameNode = nodeValue.cloneNode(true);
                    break;
                case LDAP_USER_ELEMENT:
                    LDAPUserNode = nodeValue.cloneNode(true);
                    break;
                case ID_USER_ELEMENT:
                    IDNode = nodeValue.cloneNode(true);
                    break;
                case POSITION_ELEMENT:
                    positionNode = nodeValue.cloneNode(true);
                    break;
                case HEADQUARTER_ELEMENT:
                    headquarterNode = nodeValue.cloneNode(true);
                    break;
                case PROFILES_ELEMENT:
                    profilesNode = nodeValue.getChildNodes();
                    break;
                case DATA_CONTACTS_ELEMENT:
                    dataContactNode = nodeValue.getChildNodes();
                    break;
                case ATTACH_SIGNATURE_ELEMENT:
                    attachSignatureNode = nodeValue.cloneNode(true);
                    break;
                case ATTACH_REPORT_ELEMENT:
                    attachReportNode = nodeValue.cloneNode(true);
                    break;
                case PAGE_SIZE_ELEMENT:
                    pageSizeNode = nodeValue.cloneNode(true);
                    break;
                case APPLY_APP_FILTER_ELEMENT:
                    applyAppFilterNode = nodeValue.cloneNode(true);
                    break;
                case SHOW_PREVIOUS_SIGNER_ELEMENT:
                    showPreviousSignerNode = nodeValue.cloneNode(true);
                    break;
                case VERIFIER_IDENTIFIER_ELEMENT:
                    verifierIdentifierNode = nodeValue.cloneNode(true);
                    break;
                case VERIFIER_NAME_ELEMENT:
                    verifierNameNode = nodeValue.cloneNode(true);
                    break;
                case STATUS_ELEMENT:
                    statusNode = nodeValue.cloneNode(true);
                    break;
                case SENT_RECEIVED_ELEMENT:
                    sentReceivedNode = nodeValue.cloneNode(true);
                    break;
                case TYPE_ELEMENT:
                    typeNode = nodeValue.cloneNode(true);
                    break;
                case SENDER_RECEIVER_ELEMENT:
                    senderReceiverNode = nodeValue.cloneNode(true);
                    break;
                case INIT_DATE_ELEMENT:
                    initDateNode = nodeValue.cloneNode(true);
                    break;
                case AUTHORIZATION_ELEMENT:
                    authorizationNode = nodeValue.cloneNode(true);
                    break;
                case END_DATE_ELEMENT:
                    endDateNode = nodeValue.cloneNode(true);
                    break;
            }
        }
    }

    /**
     * Clase encargada de gestionar el parseo de peticiones del servicio de recuperación de roles.
     */
    private static class GetUserRequestParser {

        /**
         * Constante que representa el nombre de la etiqueta XML.
         */
        private static final String REQUEST_NODE = "rsgtus"; //$NON-NLS-1$
        private static final String USER_NODE = "user"; //$NON-NLS-1$
        private static final String ID_REQUEST_ELEMENT = "idReq";
        private static final String NAME_ELEMENT = "name"; //$NON-NLS-1$
        private static final String SURNAME_ELEMENT = "surname"; //$NON-NLS-1$
        private static final String SECOND_SURNAME_ELEMENT = "secondSurname"; //$NON-NLS-1$
        private static final String LDAP_USER_ELEMENT = "LDAPUser"; //$NON-NLS-1$
        private static final String ID_USER_ELEMENT = "ID"; //$NON-NLS-1$
        private static final String POSITION_ELEMENT = "position"; //$NON-NLS-1$
        private static final String HEADQUARTER_ELEMENT = "headquarter"; //$NON-NLS-1$
        private static final String PROFILES_ELEMENT = "profiles"; //$NON-NLS-1$
        private static final String PROFILE_ELEMENT = "profile"; //$NON-NLS-1$
        private static final String DATA_CONTACTS_ELEMENT = "dataContacts"; //$NON-NLS-1$
        private static final String EMAIL_ELEMENT = "email"; //$NON-NLS-1$
        private static final String NOTIFY_ELEMENT = "notify"; //$NON-NLS-1$
        private static final String ATTACH_SIGNATURE_ELEMENT = "attachSignature"; //$NON-NLS-1$
        private static final String ATTACH_REPORT_ELEMENT = "attachReport"; //$NON-NLS-1$
        private static final String PAGE_SIZE_ELEMENT = "pageSize"; //$NON-NLS-1$
        private static final String APPLY_APP_FILTER_ELEMENT = "applyAppFilter"; //$NON-NLS-1$
        private static final String SHOW_PREVIOUS_SIGNER_ELEMENT = "showPreviousSigner"; //$NON-NLS-1$

        /**
         * Método que parsea la respuesta recibida por el proxy para el servicio 'GetUser'.
         * La respuesta debe seguir la siguiente estructura:
         *
         * <rsgtus>
         * <!-- 1 o más elementos -->
         * <user>
         * <idReq>String</idReq>
         * <name>String</name>
         * <surname>String</surname>
         * <secondSurname>String</secondSurname>
         * <LDAPUser>String</LDAPUser>
         * <ID>String</ID>
         * <position>String</position>
         * <headquarter>String</headquarter>
         * <profiles>
         * <!-- 1 o más elementos -->
         * <profile>UserProfile</profile>
         * </profiles>
         * <dataContacts>
         * <!-- 1 o más elementos -->
         * <dataContact>
         * <email>String</email>
         * <notify>boolean</notify>
         * </dataContact>
         * </dataContacts>
         * <attachSignature>boolean</attachSignature>
         * <attachReport>boolean</attachReport>
         * <pageSize>int</pageSize>
         * <applyAppFilter>boolean</applyAppFilter>
         * <showPreviousSigner>boolean</showPreviousSigner>
         * </user>
         * </rsgtus>
         *
         * @param getUserReqNode Nodo principal de la respuesta.
         * @return la respuesta recibida en un objeto de tipo GetUserRequest.
         */
        static GetUserRequest parse(final Node getUserReqNode) {

            if (!REQUEST_NODE.equalsIgnoreCase(getUserReqNode.getNodeName())) {
                throw new IllegalArgumentException("Se ha encontrado el elemento '" + getUserReqNode.getNodeName() + //$NON-NLS-1$
                        "' en el listado de roles"); //$NON-NLS-1$
            }

            /* Elementos */
            Node idReqNode = null;
            Node nameNode = null;
            Node surnameNode = null;
            Node secondSurnameNode = null;
            Node LDAPUserNode = null;
            Node IDNode = null;
            Node positionNode = null;
            Node headquarterNode = null;
            NodeList profilesNode = null;
            NodeList dataContactNode = null;
            Node attachSignatureNode = null;
            Node attachReportNode = null;
            Node pageSizeNode = null;
            Node applyAppFilterNode = null;
            Node showPreviousSignerNode = null;

            /* Valores */
            String idReq = null;
            String name = null;
            String surname = null;
            String secondSurname = null;
            String LDAPUser = null;
            String ID = null;
            String position = null;
            String headquarter = null;
            List<UserProfile> profiles = null;
            List<ContactData> dataContact = null;
            boolean attachSignature = false;
            boolean attachReport = false;
            int pageSize = -1;
            boolean applyAppFilter = false;
            boolean showPreviousSigner = false;

            // Cargamos los elementos hijos.
            NodeList elementsNode;
            elementsNode = getUserReqNode.getChildNodes();

            if (elementsNode == null || elementsNode.getLength() < 1) {
                throw new IllegalArgumentException("No se han encontrado elementos hijos '" + //$NON-NLS-1$
                        USER_NODE + "' en la respuesta proxy"); //$NON-NLS-1$
            }

            // Recorremos los elementos hijos del nodo 'rsgtus'.
            for (int i = 0; i < elementsNode.getLength(); i++) {

                // Recuperamos el elemento 'user'.
                Node userNode = elementsNode.item(i);
                if (userNode == null || !userNode.getNodeName().equalsIgnoreCase(USER_NODE)) {
                    throw new IllegalArgumentException("No se ha encontrado el elemento '" //$NON-NLS-1$
                            + USER_NODE + "' en la respuesta proxy."); //$NON-NLS-1$
                }
                if (!userNode.hasChildNodes()) {
                    throw new IllegalArgumentException("No se ha encontrado ningún elemento " + //$NON-NLS-1$
                            "hijo del nodo '" + USER_NODE + "' en la respuesta proxy"); //$NON-NLS-1$ //$NON-NLS-2$
                }

                // Recorremos los elementos hijos del nodo 'user'.
                NodeList rolChildren = userNode.getChildNodes();
                for (int u = 0; u < rolChildren.getLength(); u++) {
                    Node node = rolChildren.item(u);
                    // Puesto que cabe la posibilidad de que los elementos vengan desordenados,
                    // implementamos un método a parte para extraer la información al nodo correspondiente.
                    updateNodeValues(node, idReqNode, nameNode, surnameNode, secondSurnameNode,
                            LDAPUserNode, IDNode, positionNode, headquarterNode, profilesNode,
                            dataContactNode, attachSignatureNode, attachReportNode, pageSizeNode,
                            applyAppFilterNode, showPreviousSignerNode);
                }

                // recuperamos los valores de los nodos.
                if (idReqNode != null) {
                    idReq = normalizeValue(XmlUtils.getTextContent(idReqNode));
                }
                if (nameNode != null) {
                    name = normalizeValue(XmlUtils.getTextContent(nameNode));
                }
                if (surnameNode != null) {
                    surname = normalizeValue(XmlUtils.getTextContent(surnameNode));
                }
                if (secondSurnameNode != null) {
                    secondSurname = normalizeValue(XmlUtils.getTextContent(secondSurnameNode));
                }
                if (LDAPUserNode != null) {
                    LDAPUser = normalizeValue(XmlUtils.getTextContent(LDAPUserNode));
                }
                if (IDNode != null) {
                    ID = normalizeValue(XmlUtils.getTextContent(IDNode));
                }
                if (positionNode != null) {
                    position = normalizeValue(XmlUtils.getTextContent(positionNode));
                }
                if (headquarterNode != null) {
                    headquarter = normalizeValue(XmlUtils.getTextContent(headquarterNode));
                }
                if (profilesNode != null && profilesNode.getLength() > 0) {
                    profiles = new ArrayList<>();
                    for (int o = 0; o < profilesNode.getLength(); o++) {
                        Node profile = profilesNode.item(o);
                        if (profile != null && profile.getNodeName().equalsIgnoreCase(PROFILE_ELEMENT)) {
                            UserProfile up = UserProfile.getUserProfile(normalizeValue(XmlUtils.getTextContent(profile)));
                            profiles.add(up);
                        }
                    }
                }
                if (dataContactNode != null && dataContactNode.getLength() > 0) {
                    dataContact = new ArrayList<>();
                    for (int e = 0; e < dataContactNode.getLength(); e++) {
                        NodeList dataContactsNode = dataContactNode.item(e).getChildNodes();
                        if (dataContactsNode != null && dataContactsNode.getLength() > 0) {
                            ContactData contactData = new ContactData();
                            for (int o = 0; o < dataContactsNode.getLength(); o++) {
                                Node node = dataContactsNode.item(o);
                                if (node != null && node.getNodeName().equalsIgnoreCase(EMAIL_ELEMENT)) {
                                    contactData.setEmail(normalizeValue(XmlUtils.getTextContent(node)));
                                }
                                if (node != null && node.getNodeName().equalsIgnoreCase(NOTIFY_ELEMENT)) {
                                    contactData.setNotify(XmlUtils.parseBoolean(node.getNodeValue()));
                                }
                            }
                            dataContact.add(contactData);
                        }
                    }
                }
                if (attachSignatureNode != null) {
                    attachSignature = XmlUtils.parseBoolean(attachSignatureNode.getNodeValue());
                }
                if (attachReportNode != null) {
                    attachReport = XmlUtils.parseBoolean(attachReportNode.getNodeValue());
                }
                if (pageSizeNode != null) {
                    pageSize = Integer.valueOf(normalizeValue(XmlUtils.getTextContent(pageSizeNode)));
                }
                if (applyAppFilterNode != null) {
                    applyAppFilter = XmlUtils.parseBoolean(applyAppFilterNode.getNodeValue());
                }
                if (showPreviousSignerNode != null) {
                    showPreviousSigner = XmlUtils.parseBoolean(showPreviousSignerNode.getNodeValue());
                }

            }
            return new GetUserRequest(idReq, name, surname, secondSurname, LDAPUser, ID, position,
                    headquarter, profiles, dataContact, attachSignature, attachReport, pageSize,
                    applyAppFilter, showPreviousSigner);
        }

        /**
         * Método que actualiza el valor de un parámetro según el nombre del nodo proporcionado.
         *
         * @param nodeValue              Nodo que contiene el valor a copiar en el objeto.
         * @param idReqNode              Elemento <i>idReq</i>.
         * @param nameNode               Elemento <i>name</i>.
         * @param surnameNode            Elemento <i>surname</i>.
         * @param secondSurnameNode      Elemento <i>secondSurname</i>.
         * @param LDAPUserNode           Elemento <i>LDAPUser</i>.
         * @param IDNode                 Elemento <i>ID</i>.
         * @param positionNode           Elemento <i>position</i>.
         * @param headquarterNode        Elemento <i>headquarter</i>.
         * @param profilesNode           Elemento <i>profiles</i>.
         * @param dataContactNode        Elemento <i>dataContacts</i>.
         * @param attachSignatureNode    Elemento <i>attachSignature</i>.
         * @param attachReportNode       Elemento <i>attachReport</i>.
         * @param pageSizeNode           Elemento <i>pageSize</i>.
         * @param applyAppFilterNode     Elemento <i>applyAppFilter</i>.
         * @param showPreviousSignerNode Elemento <i>showPreviousSigner</i>.
         */
        private static void updateNodeValues(Node nodeValue, Node idReqNode, Node nameNode,
                                             Node surnameNode, Node secondSurnameNode,
                                             Node LDAPUserNode, Node IDNode, Node positionNode,
                                             Node headquarterNode, NodeList profilesNode,
                                             NodeList dataContactNode, Node attachSignatureNode,
                                             Node attachReportNode, Node pageSizeNode,
                                             Node applyAppFilterNode, Node showPreviousSignerNode) {
            if (nodeValue == null) {
                return;
            }
            String nodeName = nodeValue.getNodeName();
            switch (nodeName) {
                case ID_REQUEST_ELEMENT:
                    idReqNode = nodeValue.cloneNode(true);
                    break;
                case NAME_ELEMENT:
                    nameNode = nodeValue.cloneNode(true);
                    break;
                case SURNAME_ELEMENT:
                    surnameNode = nodeValue.cloneNode(true);
                    break;
                case SECOND_SURNAME_ELEMENT:
                    secondSurnameNode = nodeValue.cloneNode(true);
                    break;
                case LDAP_USER_ELEMENT:
                    LDAPUserNode = nodeValue.cloneNode(true);
                    break;
                case ID_USER_ELEMENT:
                    IDNode = nodeValue.cloneNode(true);
                    break;
                case POSITION_ELEMENT:
                    positionNode = nodeValue.cloneNode(true);
                    break;
                case HEADQUARTER_ELEMENT:
                    headquarterNode = nodeValue.cloneNode(true);
                    break;
                case PROFILES_ELEMENT:
                    profilesNode = nodeValue.getChildNodes();
                    break;
                case DATA_CONTACTS_ELEMENT:
                    dataContactNode = nodeValue.getChildNodes();
                    break;
                case ATTACH_SIGNATURE_ELEMENT:
                    attachSignatureNode = nodeValue.cloneNode(true);
                    break;
                case ATTACH_REPORT_ELEMENT:
                    attachReportNode = nodeValue.cloneNode(true);
                    break;
                case PAGE_SIZE_ELEMENT:
                    pageSizeNode = nodeValue.cloneNode(true);
                    break;
                case APPLY_APP_FILTER_ELEMENT:
                    applyAppFilterNode = nodeValue.cloneNode(true);
                    break;
                case SHOW_PREVIOUS_SIGNER_ELEMENT:
                    showPreviousSignerNode = nodeValue.cloneNode(true);
                    break;
            }
        }
    }
}
