package es.gob.afirma.android.signfolder.proxy.parsers;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import es.gob.afirma.android.signfolder.proxy.FireSignResult;
import es.gob.afirma.android.signfolder.proxy.TriphaseRequest;

/** Analizador de XML para la generaci&oacute;n de un listado de objetos
 * de tipo {@link TriphaseRequest} a partir
 * de un XML de respuesta de prefirma. */
public final class FireSignResponseParser {

	private static final String ROOT_RESPONSE_NODE = "cfsig"; //$NON-NLS-1$
	private static final String RESPONSE_STATUS_ATTR = "ok"; //$NON-NLS-1$
	private static final String ERROR_ATTR = "er"; //$NON-NLS-1$

	private FireSignResponseParser() {
		// No instanciable
	}

	/**
	 * Analiza un documento XML y, en caso de tener el formato correcto, obtiene el resultado de la
	 * firma de peticiones con FIRe.
	 * @param doc Documento XML.
	 * @return Objeto con los datos del XML.
	 * @throws IllegalArgumentException Cuando el XML no tiene el formato esperado.
	 */
	public static FireSignResult parse(final Document doc) {

		if (doc == null) {
			throw new IllegalArgumentException("El documento proporcionado no puede ser nulo");  //$NON-NLS-1$
		}

		final Node requestNode = doc.getDocumentElement();

		if (!ROOT_RESPONSE_NODE.equalsIgnoreCase(requestNode.getNodeName())) {
			throw new IllegalArgumentException("El elemento raiz del XML debe ser '" + //$NON-NLS-1$
					ROOT_RESPONSE_NODE + "' y aparece: " + //$NON-NLS-1$
					doc.getDocumentElement().getNodeName());
		}

		// Cargamos los atributos
		final NamedNodeMap attributes = requestNode.getAttributes();
		Node attributeNode = attributes.getNamedItem(RESPONSE_STATUS_ATTR);
		if (attributeNode == null) {
			throw new IllegalArgumentException("No se ha encontrado el atributo obligatorio '" + //$NON-NLS-1$
					RESPONSE_STATUS_ATTR + "' en un peticion de carga de datos con Cl@ve Firma"); //$NON-NLS-1$
		}
		final boolean statusOk = Boolean.parseBoolean(attributeNode.getNodeValue());

		int errorType = 0;
		attributeNode = attributes.getNamedItem(ERROR_ATTR);
		if (attributeNode != null) {
			errorType = Integer.parseInt(attributeNode.getNodeValue());
		}

		return new FireSignResult(statusOk, errorType);
	}
}
