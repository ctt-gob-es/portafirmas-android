package es.gob.afirma.android.signfolder.proxy;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Analizador de XML para la generaci&oacute;n de una petici&oacute;n de login con clave.
 * @author Sergio Mart&iacute;nez. */
class ClaveLoginRequestResponseParser {

	private static final String CLAVE_LOGIN_RESPONSE_NODE = "lgnrq"; //$NON-NLS-1$

	private static final String ERROR_NODE = "err"; //$NON-NLS-1$

	private static final String CD_ATTRIBUTE = "cd"; //$NON-NLS-1$

	private ClaveLoginRequestResponseParser() {
		// No instanciable
	}

	/**
	 * Analiza un documento XML y, en caso de tener el formato correcto, obtiene de &eacute;l
	 * un listado de objetos de tipo {@link TriphaseRequest}.
	 * @param doc Documento XML.
	 * @return Objeto con los datos del XML.
	 * @throws IllegalArgumentException Cuando el XML no tiene el formato esperado.
	 */
	static ClaveLoginResult parse(final Document doc) throws ServerControlledException {

		if (doc == null) {
			throw new IllegalArgumentException("El documento proporcionado no puede ser nulo");  //$NON-NLS-1$
		}

		final Element docElement = doc.getDocumentElement();

		if (ERROR_NODE.equalsIgnoreCase(docElement.getNodeName())) {
			final String errorCode = docElement.getAttribute(CD_ATTRIBUTE);
			throw new ServerControlledException(errorCode, XmlUtils.getTextContent(docElement));
		}

		if (!CLAVE_LOGIN_RESPONSE_NODE.equalsIgnoreCase(docElement.getNodeName())) {
			throw new IllegalArgumentException("El elemento raiz del XML debe ser '" + //$NON-NLS-1$
					CLAVE_LOGIN_RESPONSE_NODE + "' y aparece: " + //$NON-NLS-1$
					docElement.getNodeName());
		}
		return ResultParser.parse(docElement);
	}

	private static final class ResultParser {

		private static final String URL_NODE = "url"; //$NON-NLS-1$
		private static final String SESSION_ID_NODE = "sessionId"; //$NON-NLS-1$

		static ClaveLoginResult parse(final Node requestNode) {

			NodeList childNodes = requestNode.getChildNodes();

			// Obtenemos los distintos elementos del XML
			final ClaveLoginResult result = new ClaveLoginResult(true);
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node childNode = childNodes.item(i);
				if (childNode.getNodeType() == Node.ELEMENT_NODE) {
					if (childNode.getNodeName().equals(URL_NODE)) {
						result.setRedirectionUrl(childNode.getTextContent());
					} else if (childNode.getNodeName().equals(SESSION_ID_NODE)) {
						result.setTransactionId(childNode.getTextContent());
					}
				}
			}

			return result;
		}
	}
}
