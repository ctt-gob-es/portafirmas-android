package es.gob.afirma.android.signfolder.proxy;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Analizador de XML para la generaci&oacute;n de una petici&oacute;n de login con clave.
 * @author Sergio Mart&iacute;nez. */
class ClaveLoginRequestResponseParser {

	private static final String CLAVE_LOGIN_RESPONSE_NODE = "lgnrq"; //$NON-NLS-1$

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
	static ClaveLoginResult parse(final Document doc) {

		if (doc == null) {
			throw new IllegalArgumentException("El documento proporcionado no puede ser nulo");  //$NON-NLS-1$
		}

		if (!CLAVE_LOGIN_RESPONSE_NODE.equalsIgnoreCase(doc.getDocumentElement().getNodeName())) {
			throw new IllegalArgumentException("El elemento raiz del XML debe ser '" + //$NON-NLS-1$
					CLAVE_LOGIN_RESPONSE_NODE + "' y aparece: " + //$NON-NLS-1$
					doc.getDocumentElement().getNodeName());
		}

		final NodeList requestNodes = doc.getDocumentElement().getChildNodes();
		final int nextIndex = XmlUtils.nextNodeElementIndex(requestNodes, 0);
		final Node requestNode;
		if (nextIndex == -1) {
			requestNode = doc.getDocumentElement();
		}
		else {
			requestNode = requestNodes.item(nextIndex);
		}
		return ResultParser.parse(requestNode);
	}

	private static final class ResultParser {

		private static final String CLAVE_LOGIN_RESPONSE_NODE = "lgnrq"; //$NON-NLS-1$
		private static final String ID_ATTRIBUTE = "id"; //$NON-NLS-1$

		static ClaveLoginResult parse(final Node requestNode) {

			if (!CLAVE_LOGIN_RESPONSE_NODE.equalsIgnoreCase(requestNode.getNodeName())) {
				throw new IllegalArgumentException("Se encontro un elemento '" + //$NON-NLS-1$
						requestNode.getNodeName() + "' en el listado de peticiones"); //$NON-NLS-1$
			}

			// Datos de la peticion
			String url = requestNode.getTextContent();

			final ClaveLoginResult result = new ClaveLoginResult(true);
			result.setRedirectionUrl(url);

			return result;
		}
	}
}
