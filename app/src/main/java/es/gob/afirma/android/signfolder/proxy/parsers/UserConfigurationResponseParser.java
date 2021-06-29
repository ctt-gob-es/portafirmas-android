package es.gob.afirma.android.signfolder.proxy.parsers;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import es.gob.afirma.android.signfolder.proxy.ServerControlledException;
import es.gob.afirma.android.signfolder.proxy.XmlUtils;
import es.gob.afirma.android.user.configuration.ApplicationFilter;
import es.gob.afirma.android.user.configuration.GenericFilter;
import es.gob.afirma.android.user.configuration.RoleInfo;
import es.gob.afirma.android.user.configuration.TagFilter;
import es.gob.afirma.android.user.configuration.UserConfig;
import es.gob.afirma.android.user.configuration.UserFilters;

/**
 * Analizador de XML para la generaci&oacute;n de la configuración de usuario.
 */
public final class UserConfigurationResponseParser {

    private static final String USER_CONFIG_RESPONSE_NODE = "rsgtsrcg"; //$NON-NLS-1$
    private static final String ERROR_NODE = "err"; //$NON-NLS-1$
    private static final String TAG_NAME_ROLES = "rls"; //$NON-NLS-1$
    private static final String TAG_NAME_SIM_PARAMS = "smcg"; //$NON-NLS-1$
    private static final String TAG_NAME_USER_WITH_VERIFIERS = "srvrf"; //$NON-NLS-1$
    private static final String TAG_NAME_PUSH_STATUS = "ntpsh"; //$NON-NLS-1$
    private static final String TAG_NAME_USER_FILTERS = "fltrs"; //$NON-NLS-1$
    private static final String TAG_NAME_FILTER_YEAR = "yrs"; //$NON-NLS-1$
    private static final String TAG_NAME_FILTER_MONTH = "mnths"; //$NON-NLS-1$
    private static final String TAG_NAME_FILTER_TAG = "tgs"; //$NON-NLS-1$
    private static final String TAG_NAME_FILTER_TYPE = "tps"; //$NON-NLS-1$
    private static final String TAG_NAME_FILTER_APPLICATION = "pps"; //$NON-NLS-1$

    /**
     * Método encargado de parsear la configuración de usuario.
     *
     * @param doc Documento recibido.
     * @return Objeto de tipo UserConfig que representa la configuración de usuario.
     * @throws ServerControlledException si la respuesta recibida es de tipo error.
     */
    public static UserConfig parseUserConfigReq(Document doc) throws ServerControlledException {
        if (doc == null) {
            throw new IllegalArgumentException("El documento proporcionado no puede ser nulo"); //$NON-NLS-1$
        }

        final Element docElement = doc.getDocumentElement();

        if (!USER_CONFIG_RESPONSE_NODE.equalsIgnoreCase(docElement.getNodeName())) {
            throw new IllegalArgumentException("El elemento raiz del XML debe ser '" + USER_CONFIG_RESPONSE_NODE + //$NON-NLS-1$
                    "' y aparece: " + doc.getDocumentElement().getNodeName()); //$NON-NLS-1$
        }

        if (docElement.getElementsByTagName(ERROR_NODE).item(0) != null) {
            final String errorCode = docElement.getElementsByTagName(ERROR_NODE).item(0).getTextContent();
            throw new ServerControlledException(errorCode, XmlUtils.getTextContent(docElement));
        }

        // Objetos necesarios para la creación de la configuración de usuario.
        List<RoleInfo> rolesList = new ArrayList<>();
        boolean simParams = false;
        boolean userWithVerifiers = false;
        boolean pushStatus = false;
        UserFilters filters = new UserFilters();

        // Roles
        final NodeList rolesNodeList = docElement.getElementsByTagName(TAG_NAME_ROLES);
        if (rolesNodeList != null && rolesNodeList.getLength() > 0) {

            Node roleNode = rolesNodeList.item(0);
            for (int e = 0; e < roleNode.getChildNodes().getLength(); e++) {
                NodeList roleItemNode = roleNode.getChildNodes().item(e).getChildNodes();
                RoleInfo role = new RoleInfo();
                for (int a = 0; a < roleItemNode.getLength(); a++) {
                    Node roleElem = roleItemNode.item(a);
                    switch (roleElem.getNodeName()) {
                        case "id": //$NON-NLS-1$
                            role.setRoleId(roleElem.getTextContent());
                            break;
                        case "roleName": //$NON-NLS-1$
                            role.setRoleName(roleElem.getTextContent());
                            break;
                        case "userName": //$NON-NLS-1$
                            role.setUserName(roleElem.getTextContent());
                            break;
                        case "dni": //$NON-NLS-1$
                            role.setOwnerDni(roleElem.getTextContent());
                            break;
                    }
                }
                rolesList.add(role);
            }
        }

        // Parámetros SIM
        NodeList simParamsNodeList = docElement.getElementsByTagName(TAG_NAME_SIM_PARAMS);
        if (simParamsNodeList != null) {
            String simParamsString = simParamsNodeList.item(0).getTextContent();
            simParams = simParamsString.equalsIgnoreCase("S");
        }

        // Estado notificaciones push
        NodeList pushStatusNodeList = docElement.getElementsByTagName(TAG_NAME_PUSH_STATUS);
        if (pushStatusNodeList != null) {
            String pushStatusString = pushStatusNodeList.item(0).getTextContent();
            pushStatus = pushStatusString.equalsIgnoreCase("S");
        }

        // Usuario con validadores
        NodeList userWithVerifiersNodeList = docElement.getElementsByTagName(TAG_NAME_USER_WITH_VERIFIERS);
        if (userWithVerifiersNodeList != null) {
            String userWithVerifiersString = userWithVerifiersNodeList.item(0).getTextContent();
            userWithVerifiers = userWithVerifiersString.equalsIgnoreCase("S");
        }

        // Filtros de usuario
        NodeList filtersNodeList = docElement.getElementsByTagName(TAG_NAME_USER_FILTERS);
        parseFilters(filtersNodeList, filters);

        return new UserConfig(rolesList, simParams, userWithVerifiers, pushStatus, filters);
    }

    /**
     * Método auxiliar encargado de parsear los filtros de usuario.
     *
     * @param filtersNodeList Nodo que contiene los filtros a parsear.
     * @param filters         Objeto donde se incluirá la lista de filtros resultante.
     */
    private static void parseFilters(NodeList filtersNodeList, UserFilters filters) {
        if (filtersNodeList != null) {
            List<GenericFilter> yearFilters = new ArrayList<>();
            List<GenericFilter> monthFilters = new ArrayList<>();
            List<GenericFilter> typeFilters = new ArrayList<>();
            List<TagFilter> tagFilters = new ArrayList<>();
            List<ApplicationFilter> applicationFilters = new ArrayList<>();

            NodeList filtersNodes = filtersNodeList.item(0).getChildNodes();
            for (int i = 0; i < filtersNodes.getLength(); i++) {

                Node filterNode = filtersNodes.item(i);
                switch (filterNode.getNodeName()) {
                    case TAG_NAME_FILTER_YEAR:
                        NodeList yearsNodeList = filterNode.getChildNodes();
                        if (yearsNodeList != null) {
                            for (int e = 0; e < yearsNodeList.getLength(); e++) {
                                e = XmlUtils.nextNodeElementIndex(yearsNodeList, e);
                                if (e == -1) {
                                    break;
                                }
                                Node yearNode = yearsNodeList.item(e);
                                if (yearNode != null) {
                                    GenericFilter yearFilter = new GenericFilter();
                                    for (int a = 0; a < yearNode.getChildNodes().getLength(); a++) {
                                        a = XmlUtils.nextNodeElementIndex(yearNode.getChildNodes(), a);
                                        if (a == -1) {
                                            break;
                                        }
                                        Node value = yearNode.getChildNodes().item(a);
                                        switch (value.getNodeName()) {
                                            case "YrId": //$NON-NLS-1$
                                                yearFilter.setId(value.getTextContent());
                                                break;
                                            case "YrDescription": //$NON-NLS-1$
                                                yearFilter.setDescription(value.getTextContent());
                                                break;
                                        }
                                    }
                                    yearFilters.add(yearFilter);
                                }
                            }
                        }
                        break;

                    case TAG_NAME_FILTER_MONTH:
                        NodeList monthNodeList = filterNode.getChildNodes();
                        if (monthNodeList != null) {
                            for (int e = 0; e < monthNodeList.getLength(); e++) {
                                e = XmlUtils.nextNodeElementIndex(monthNodeList, e);
                                if (e == -1) {
                                    break;
                                }
                                Node monthNode = monthNodeList.item(e);
                                if (monthNode != null) {
                                    GenericFilter monthFilter = new GenericFilter();
                                    for (int a = 0; a < monthNode.getChildNodes().getLength(); a++) {
                                        a = XmlUtils.nextNodeElementIndex(monthNode.getChildNodes(), a);
                                        if (a == -1) {
                                            break;
                                        }
                                        Node value = monthNode.getChildNodes().item(a);
                                        switch (value.getNodeName()) {
                                            case "mnthId": //$NON-NLS-1$
                                                monthFilter.setId(value.getTextContent());
                                                break;
                                            case "mnthDescription": //$NON-NLS-1$
                                                monthFilter.setDescription(value.getTextContent());
                                                break;
                                        }
                                    }
                                    monthFilters.add(monthFilter);
                                }
                            }
                        }
                        break;

                    case TAG_NAME_FILTER_TAG:
                        NodeList tagNodeList = filterNode.getChildNodes();
                        if (tagNodeList != null) {
                            for (int e = 0; e < tagNodeList.getLength(); e++) {
                                e = XmlUtils.nextNodeElementIndex(tagNodeList, e);
                                if (e == -1) {
                                    break;
                                }
                                Node tagNode = tagNodeList.item(e);
                                if (tagNode != null) {
                                    TagFilter tagFilter = new TagFilter();
                                    for (int a = 0; a < tagNode.getChildNodes().getLength(); a++) {
                                        a = XmlUtils.nextNodeElementIndex(tagNode.getChildNodes(), a);
                                        if (a == -1) {
                                            break;
                                        }
                                        Node value = tagNode.getChildNodes().item(a);
                                        switch (value.getNodeName()) {
                                            case "tgId": //$NON-NLS-1$
                                                tagFilter.setId(value.getTextContent());
                                                break;
                                            case "tgDescription": //$NON-NLS-1$
                                                tagFilter.setDescription(value.getTextContent());
                                                break;
                                            case "tgColor": //$NON-NLS-1$
                                                tagFilter.setColor(value.getTextContent());
                                                break;
                                        }
                                    }
                                    tagFilters.add(tagFilter);
                                }
                            }
                        }
                        break;

                    case TAG_NAME_FILTER_TYPE:
                        NodeList typeNodeList = filterNode.getChildNodes();
                        if (typeNodeList != null) {
                            for (int e = 0; e < typeNodeList.getLength(); e++) {
                                e = XmlUtils.nextNodeElementIndex(typeNodeList, e);
                                if (e == -1) {
                                    break;
                                }
                                Node typeNode = typeNodeList.item(e);
                                if (typeNode != null) {
                                    GenericFilter typeFilter = new GenericFilter();
                                    for (int a = 0; a < typeNode.getChildNodes().getLength(); a++) {
                                        a = XmlUtils.nextNodeElementIndex(typeNode.getChildNodes(), a);
                                        if (a == -1) {
                                            break;
                                        }
                                        Node value = typeNode.getChildNodes().item(a);
                                        switch (value.getNodeName()) {
                                            case "tpId":
                                                typeFilter.setId(value.getTextContent());
                                                break;
                                            case "tpDescription":
                                                typeFilter.setDescription(value.getTextContent());
                                                break;
                                        }
                                    }
                                    typeFilters.add(typeFilter);
                                }
                            }
                        }
                        break;

                    case TAG_NAME_FILTER_APPLICATION:
                        NodeList appNodeList = filterNode.getChildNodes();
                        if (appNodeList != null) {
                            for (int e = 0; e < appNodeList.getLength(); e++) {
                                e = XmlUtils.nextNodeElementIndex(appNodeList, e);
                                if (e == -1) {
                                    break;
                                }
                                Node appNode = appNodeList.item(e);
                                if (appNode != null) {
                                    ApplicationFilter appFilter = new ApplicationFilter();
                                    for (int a = 0; a < appNode.getChildNodes().getLength(); a++) {
                                        a = XmlUtils.nextNodeElementIndex(appNode.getChildNodes(), a);
                                        if (a == -1) {
                                            break;
                                        }
                                        Node value = appNode.getChildNodes().item(a);
                                        switch (value.getNodeName()) {
                                            case "ppId": //$NON-NLS-1$
                                                appFilter.setId(value.getTextContent());
                                                break;
                                            case "ppName": //$NON-NLS-1$
                                                appFilter.setName(value.getTextContent());
                                                break;
                                        }
                                    }
                                    applicationFilters.add(appFilter);
                                }
                            }
                        }
                        break;

                }
            }
            filters.setYearFilters(yearFilters);
            filters.setMonthFilters(monthFilters);
            filters.setTypeFilters(typeFilters);
            filters.setTagFilters(tagFilters);
            filters.setApplicationFilters(applicationFilters);
        }
    }

}
