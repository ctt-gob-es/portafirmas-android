package es.gob.afirma.android.signfolder.proxy;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import es.gob.afirma.android.signfolder.SFConstants;

/** Analizador de XML para la generaci&oacute;n de una petici&oacute;n de login o logout.
 * @author Sergio Mart&iacute;nez. */
public class LoginTokenResponseParser {

	private static final String LOGIN_TOKEN_RESPONSE_NODE = "lgnrq"; //$NON-NLS-1$

	private LoginTokenResponseParser() {
		// No instanciable
	}

	/**
	 * Analiza un documento XML y, en caso de tener el formato correcto, obtiene de &eacute;l
	 * un listado de objetos de tipo {@link TriphaseRequest}.
	 * @param doc Documento XML.
	 * @return Objeto con los datos del XML.
	 * @throws IllegalArgumentException Cuando el XML no tiene el formato esperado.
	 */
	static RequestResult parse(final Document doc) {

		if (doc == null) {
			throw new IllegalArgumentException("El documento proporcionado no puede ser nulo");  //$NON-NLS-1$
		}

		if (!LOGIN_TOKEN_RESPONSE_NODE.equalsIgnoreCase(doc.getDocumentElement().getNodeName())) {
			throw new IllegalArgumentException("El elemento raiz del XML debe ser '" + //$NON-NLS-1$
					LOGIN_TOKEN_RESPONSE_NODE + "' y aparece: " + //$NON-NLS-1$
					doc.getDocumentElement().getNodeName());
		}

		final NodeList requestNodes = doc.getDocumentElement().getChildNodes();
		final int nextIndex = XmlUtils.nextNodeElementIndex(requestNodes, 0);
		final Node requestNode;
		if(nextIndex == -1) {
			requestNode = doc.getDocumentElement();
		}
		else {
			requestNode = requestNodes.item(nextIndex);
		}
		return RequestResultParser.parse(requestNode);
	}

	private static final class RequestResultParser {

		private static final String LOGIN_NODE = "lgnrq"; //$NON-NLS-1$
		private static final String ID_ATTRIBUTE = "id"; //$NON-NLS-1$
		private static final String ERROR_ATTRIBUTE = "err"; //$NON-NLS-1$

		static RequestResult parse(final Node requestNode) {

			if (!LOGIN_NODE.equalsIgnoreCase(requestNode.getNodeName())) {
				throw new IllegalArgumentException("Se encontro un elemento '" + //$NON-NLS-1$
						requestNode.getNodeName() + "' en el listado de peticiones"); //$NON-NLS-1$
			}

			// Datos de la peticion
			String ref = requestNode.getTextContent();
			boolean statusOk = true;

			// Cargamos los atributos
			Node attributeNode = null;
			final NamedNodeMap attributes = requestNode.getAttributes();
			attributeNode = attributes.getNamedItem(ID_ATTRIBUTE);
			if (attributeNode == null) {
				if (attributeNode == null) {
					throw new IllegalArgumentException("No se ha encontrado el atributo obligatorio '" + //$NON-NLS-1$
							ID_ATTRIBUTE + "' en un peticion de prefirma"); //$NON-NLS-1$
				}
				else {
					attributeNode = attributes.getNamedItem(ERROR_ATTRIBUTE);
					// Si existe el atributo de error significa que se ha producido un error
					if (attributeNode == null) {
						statusOk = false;
					}
				}
			}

			attributeNode = attributes.getNamedItem(ERROR_ATTRIBUTE);
			// Si existe el atributo de error significa que se ha producido un error
			if (attributeNode != null) {
				statusOk = false;
			}

			Log.i(SFConstants.LOG_TAG, "Id=" + ref + "; status=" + statusOk); //$NON-NLS-1$ //$NON-NLS-2$

			return new RequestResult(ref, statusOk);
		}
	}
}
