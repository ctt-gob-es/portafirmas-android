package es.gob.afirma.android.signfolder.proxy;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.Vector;

import es.gob.afirma.android.util.Base64;

/** Analizador de XML para la generaci&oacute;n de un listado de objetos
 * de tipo {@link TriphaseRequest} a partir
 * de un XML de respuesta de prefirma.
 * @author Carlos Gamuci */
public final class PresignsClaveFirmaResponseParser {

	private static final String PRESIGN_RESPONSE_NODE = "cfpre"; //$NON-NLS-1$
	private static final String TRANSACTION_ID_PARAM = "tr"; //$NON-NLS-1$
	private static final String URL_PARAM = "url"; //$NON-NLS-1$

	private PresignsClaveFirmaResponseParser() {
		// No instanciable
	}

	/**
	 * Analiza un documento XML y, en caso de tener el formato correcto, obtiene de &eacute;l
	 * un listado de objetos {@link TriphaseRequest}.
	 * @param doc Documento XML.
	 * @return Objeto con los datos del XML.
	 * @throws IllegalArgumentException Cuando el XML no tiene el formato esperado.
	 */
	static FirePreSignResult parse(final Document doc) {

		if (doc == null) {
			throw new IllegalArgumentException("El documento proporcionado no puede ser nulo");  //$NON-NLS-1$
		}

		final Node requestNode = doc.getDocumentElement();

		if (!PRESIGN_RESPONSE_NODE.equalsIgnoreCase(requestNode.getNodeName())) {
			throw new IllegalArgumentException("El elemento raiz del XML debe ser '" + //$NON-NLS-1$
					PRESIGN_RESPONSE_NODE + "' y aparece: " + //$NON-NLS-1$
					doc.getDocumentElement().getNodeName());
		}

		// Datos de la peticion
		String url;
		final String idTrans;
		final Vector<TriphaseSignDocumentRequest> listDocumentRequests = new Vector<TriphaseSignDocumentRequest>();

		// Cargamos los atributos
		final NamedNodeMap attributes = requestNode.getAttributes();
		Node attributeNode = attributes.getNamedItem(TRANSACTION_ID_PARAM);
		if (attributeNode == null) {
			throw new IllegalArgumentException("No se ha encontrado el atributo obligatorio '" + //$NON-NLS-1$
					TRANSACTION_ID_PARAM + "' en un peticion de prefirma con Cl@ve Firma"); //$NON-NLS-1$
		}
		idTrans = attributeNode.getNodeValue();

		attributeNode = attributes.getNamedItem(URL_PARAM);
		if (attributeNode == null) {
			throw new IllegalArgumentException("No se ha encontrado el atributo obligatorio '" + //$NON-NLS-1$
					URL_PARAM + "' en un peticion de prefirma con Cl@ve Firma"); //$NON-NLS-1$
		}
		try {
			url = new String(Base64.decode(attributeNode.getNodeValue()));
		}
		catch (Exception e) {
			url = attributeNode.getNodeValue();
		}

		final TriphaseRequest[] triphaseRequests = PresignsResponseParser.parse((Element) requestNode.getFirstChild());

		return new FirePreSignResult(idTrans, url, triphaseRequests);
	}
}
