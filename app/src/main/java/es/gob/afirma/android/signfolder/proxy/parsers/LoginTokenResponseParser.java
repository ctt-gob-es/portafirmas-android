package es.gob.afirma.android.signfolder.proxy.parsers;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import es.gob.afirma.android.signfolder.ErrorManager;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.proxy.OldProxyException;
import es.gob.afirma.android.signfolder.proxy.RequestResult;
import es.gob.afirma.android.signfolder.proxy.TriphaseRequest;
import es.gob.afirma.android.signfolder.proxy.XmlUtils;
import es.gob.afirma.android.user.configuration.ConfigurationRole;
import es.gob.afirma.android.util.PfLog;

/** Analizador de XML para la generaci&oacute;n de una petici&oacute;n de login o logout.
 * @author Sergio Mart&iacute;nez. */
public class LoginTokenResponseParser {

	private static final String LOGIN_TOKEN_RESPONSE_NODE = "lgnrq"; //$NON-NLS-1$

    private static final String ERROR_NODE = "err"; //$NON-NLS-1$

    private static final String ERROR_CODE_ATTR = "cd"; //$NON-NLS-1$

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
	public static RequestResult parse(final Document doc) throws OldProxyException {

		if (doc == null) {
			throw new IllegalArgumentException("El documento proporcionado no puede ser nulo");  //$NON-NLS-1$
		}

		if (!LOGIN_TOKEN_RESPONSE_NODE.equalsIgnoreCase(doc.getDocumentElement().getNodeName())) {

			if (ERROR_NODE.equalsIgnoreCase(doc.getDocumentElement().getNodeName())) {
				String errorCode = doc.getDocumentElement().getAttribute(ERROR_CODE_ATTR);
				if (ErrorManager.ERROR_UNSUPPORTED_OPERATION_NAME.equals(errorCode)) {
					throw new OldProxyException("La operacion de login no esta soportada");
				}
			}

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
		private static final String SSID_ATTRIBUTE = "ssid"; //$NON-NLS-1$
		private static final String ERROR_ATTRIBUTE = "err"; //$NON-NLS-1$
		private static final String ROLES_ATTRIBUTE = "roles"; //$NON-NLS-1$

		static RequestResult parse(final Node requestNode) {

			if (!LOGIN_NODE.equalsIgnoreCase(requestNode.getNodeName())) {
				throw new IllegalArgumentException("Se encontro un elemento '" + //$NON-NLS-1$
						requestNode.getNodeName() + "' en la solicitud de token de inicio de sesion"); //$NON-NLS-1$
			}

			// Datos de la peticion
			String ref = requestNode.getTextContent();
			boolean statusOk = true;

			// Cargamos los atributos
			final NamedNodeMap attributes = requestNode.getAttributes();
			Node attributeNode = attributes.getNamedItem(ID_ATTRIBUTE);
			if (attributeNode == null) {
				throw new IllegalArgumentException("No se ha encontrado el atributo obligatorio '" + //$NON-NLS-1$
						ID_ATTRIBUTE + "' en un peticion de login"); //$NON-NLS-1$
			}

			String ssid = null;
			attributeNode = attributes.getNamedItem(SSID_ATTRIBUTE);
			if (attributeNode != null) {
				ssid = attributeNode.getNodeValue();
			}

			attributeNode = attributes.getNamedItem(ERROR_ATTRIBUTE);
			// Si existe el atributo de error significa que se ha producido un error
			if (attributeNode != null) {
				statusOk = false;
			}

			PfLog.i(SFConstants.LOG_TAG, "Id=" + ref + "; status=" + statusOk); //$NON-NLS-1$ //$NON-NLS-2$

			return new RequestResult(ref, statusOk, ssid);
		}
	}
}
