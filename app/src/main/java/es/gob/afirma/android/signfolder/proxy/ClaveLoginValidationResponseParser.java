package es.gob.afirma.android.signfolder.proxy;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/** Analizador de XML para la validaci&acute;n de una autenticaci&oacute;n con Cl@ve. */
class ClaveLoginValidationResponseParser {

    private static final String CLAVE_LOGIN_RESPONSE_NODE = "claveLogin"; //$NON-NLS-1$
    private static final String STATUS_ATTRIBUTE = "status"; //$NON-NLS-1$
    private static final String DNI_NODE = "dni"; //$NON-NLS-1$
    private static final String ERROR_NODE = "err"; //$NON-NLS-1$

	private ClaveLoginValidationResponseParser() {
		// No instanciable
	}

	/**
	 * Analiza un documento XML y obtiene el DNI del usuario autenticado.
	 * @param doc Documento XML.
	 * @return Objeto con los datos del XML.
	 * @throws IllegalArgumentException Cuando el XML no tiene el formato esperado.
	 */
	static ValidationLoginResult parse(final Document doc) {

		if (doc == null) {
			throw new IllegalArgumentException("El documento proporcionado no puede ser nulo");  //$NON-NLS-1$
		}

        // Comprobamos el nombre del nodo
        //Node requestNode = doc.getOwnerDocument();
        if (!CLAVE_LOGIN_RESPONSE_NODE.equalsIgnoreCase(doc.getNodeName())) {
            throw new IllegalArgumentException("No se encontro el elemento '" + //$NON-NLS-1$
                    CLAVE_LOGIN_RESPONSE_NODE + "' como nodo raiz del XML de respuesta"); //$NON-NLS-1$
        }

        // Comprobamos el atributo de estado
        final NamedNodeMap attributes = doc.getAttributes();
        Node attributeNode = attributes.getNamedItem(STATUS_ATTRIBUTE);
        if (attributeNode == null) {
            throw new IllegalArgumentException("No se ha encontrado el atributo obligatorio '" + //$NON-NLS-1$
                    STATUS_ATTRIBUTE + "' en un peticion de prefirma"); //$NON-NLS-1$
        }
        boolean statusOk = Boolean.parseBoolean(attributeNode.getNodeValue());

        // Segun el estado sea correcto o no, cargamos la URL o el mensaje de error
        String dni = null;
        String error = null;
        if (statusOk) {
            Node dniNode = doc.getFirstChild();
            if (dniNode == null || !dniNode.getNodeName().equals(DNI_NODE)) {
                throw new IllegalArgumentException("No se encuentra el nodo '" + DNI_NODE + "' con la URL de redireccion"); //$NON-NLS-1$
            }
            dni = dniNode.getTextContent();
        }
        else {
            Node errorNode = doc.getFirstChild();
            if (errorNode == null || !errorNode.getNodeName().equals(ERROR_NODE)) {
                throw new IllegalArgumentException("No se encuentra el nodo '" + ERROR_NODE + "' con el mensaje de error"); //$NON-NLS-1$
            }
            error = errorNode.getTextContent();
        }

        // Componemos el resultado
        final ValidationLoginResult result = new ValidationLoginResult(statusOk);
        result.setDni(dni);
        result.setErrorMsg(error);
        return result;
	}
}
