package es.gob.afirma.android.signfolder.proxy.parsers;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import es.gob.afirma.android.signfolder.proxy.NotificationRegistryResult;


public class RegisterOnNotificationParser {

    private static final String NOTIFICATION_RESPONSE_NODE = "reg";
    private static final String OK_ATTRIBUTE = "ok";
    private static final String ERROR_ATTRIBUTE = "err";


    public static NotificationRegistryResult parse(final Document doc) {

        if (doc == null) {
            throw new IllegalArgumentException("El documento proporcionado no puede ser nulo");  //$NON-NLS-1$
        }

        if (!NOTIFICATION_RESPONSE_NODE.equalsIgnoreCase(doc.getDocumentElement().getNodeName())) {
            if (ERROR_ATTRIBUTE.equalsIgnoreCase(doc.getDocumentElement().getNodeName())) {
                return new NotificationRegistryResult(false, doc.getDocumentElement().getTextContent());
            }
            throw new IllegalArgumentException("El elemento raiz del XML debe ser '" + //$NON-NLS-1$
                    NOTIFICATION_RESPONSE_NODE + "' y aparece: " + //$NON-NLS-1$
                    doc.getDocumentElement().getNodeName());
        }

        final NamedNodeMap attributes = doc.getDocumentElement().getAttributes();
        Node attNode = attributes.getNamedItem(OK_ATTRIBUTE);
        if (attNode == null || attNode.getNodeValue() == null || attNode.getNodeValue().trim().length() == 0) {
            throw new IllegalArgumentException("El resultado de la notificacion carece del atributo '" + //$NON-NLS-1$
                    OK_ATTRIBUTE + "' si el registro finalizo correctamente"); //$NON-NLS-1$
        }

        boolean ok = Boolean.parseBoolean(attNode.getNodeValue());
        if (ok) {
            return new NotificationRegistryResult(ok);
        }

        String err = null;
        attNode = attributes.getNamedItem(ERROR_ATTRIBUTE);
        if (attNode != null && attNode.getNodeValue() != null) {
            err = attNode.getNodeValue();
        }
        return new NotificationRegistryResult(ok, err);
    }
}
