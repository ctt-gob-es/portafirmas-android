package es.gob.afirma.android.signfolder.proxy;

import android.util.Log;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import es.gob.afirma.android.signfolder.SFConstants;

/**
 * Analizador de XML con la informaci&oacute;n requerida para la firma de documentos.
 */
public class RequestDocumentParser {

	private static final String DOC_NODE = "doc"; //$NON-NLS-1$
	private static final String ATTACHED_NODE = "attached"; //$NON-NLS-1$
	private static final String ID_ATTRIBUTE = "docid"; //$NON-NLS-1$
	private static final String NAME_NODE = "nm"; //$NON-NLS-1$
	private static final String SIZE_NODE = "sz"; //$NON-NLS-1$
	private static final String MIMETYPE_NODE = "mmtp"; //$NON-NLS-1$

	static RequestDocument parse(final Node signRequestDocumentNode) {

		if (!DOC_NODE.equalsIgnoreCase(signRequestDocumentNode.getNodeName()) && !ATTACHED_NODE.equalsIgnoreCase(signRequestDocumentNode.getNodeName())) {
			throw new IllegalArgumentException("Se encontro un elemento '" + //$NON-NLS-1$
					signRequestDocumentNode.getNodeName() + "' en el listado de documentos"); //$NON-NLS-1$
		}

		// Elementos del documento
		final String docId;
		final String name;
		int size = -1;
		final String mimeType;
		final String signatureFormat;
		final String messageDigestAlgorithm;
		String params = null;

		// Atributos del nodo
		final Node idNode = signRequestDocumentNode.getAttributes().getNamedItem(ID_ATTRIBUTE);
		if (idNode == null) {
			throw new IllegalArgumentException("Existe un documento sin el atributo '" + ID_ATTRIBUTE + //$NON-NLS-1$
					"'"); //$NON-NLS-1$
		}
		docId = idNode.getNodeValue();

		// Cargamos los elementos
		int elementIndex = 0;
		final NodeList childNodes = signRequestDocumentNode.getChildNodes();
		elementIndex = XmlUtils.nextNodeElementIndex(childNodes, 0);
		if (elementIndex == -1 || !NAME_NODE.equalsIgnoreCase(childNodes.item(elementIndex).getNodeName())) {
			throw new IllegalArgumentException("Existe un documento sin el elemento '" + NAME_NODE + //$NON-NLS-1$
					"'"); //$NON-NLS-1$
		}
		name = XmlUtils.getTextContent(childNodes.item(elementIndex));

		elementIndex = XmlUtils.nextNodeElementIndex(childNodes, ++elementIndex);
		if (elementIndex != -1 && SIZE_NODE.equalsIgnoreCase(childNodes.item(elementIndex).getNodeName())) {
			try {
				size = Integer.parseInt(XmlUtils.getTextContent(childNodes.item(elementIndex)));
			} catch (NumberFormatException e) {
				Log.w(SFConstants.LOG_TAG, "No se ha indicado un tamano de documento valido: " + XmlUtils.getTextContent(childNodes.item(elementIndex))); //$NON-NLS-1$
			}
			elementIndex = XmlUtils.nextNodeElementIndex(childNodes, ++elementIndex);
		}

		if (elementIndex == -1 || !MIMETYPE_NODE.equalsIgnoreCase(childNodes.item(elementIndex).getNodeName())) {
			throw new IllegalArgumentException(
					"Existe un documento sin el elemento '" + MIMETYPE_NODE + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		mimeType = XmlUtils.getTextContent(childNodes.item(elementIndex));

		return new RequestDocument(docId, name, size, mimeType);
	}
}
