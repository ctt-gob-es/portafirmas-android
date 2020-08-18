package es.gob.afirma.android.signfolder.proxy.parsers;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Vector;

import es.gob.afirma.android.signfolder.proxy.RequestDetail;
import es.gob.afirma.android.signfolder.proxy.RequestDocument;
import es.gob.afirma.android.signfolder.proxy.SignLine;
import es.gob.afirma.android.signfolder.proxy.SignLineElement;
import es.gob.afirma.android.signfolder.proxy.SignRequest.RequestType;
import es.gob.afirma.android.signfolder.proxy.SignRequestDocument;
import es.gob.afirma.android.signfolder.proxy.XmlUtils;

/** Analizador de XML de respuesta del detalle de una petici&oacute;n de firma. */
public final class RequestDetailResponseParser {

	private static final String DETAIL_RESPONSE_NODE = "dtl"; //$NON-NLS-1$
	private static final String ID_ATTRIBUTE = "id"; //$NON-NLS-1$
	private static final String PRIORITY_ATTRIBUTE = "priority"; //$NON-NLS-1$
	private static final String WORKFLOW_ATTRIBUTE = "workflow"; //$NON-NLS-1$
	private static final String FORWARD_ATTRIBUTE = "forward"; //$NON-NLS-1$
	private static final String TYPE_ATTRIBUTE = "type"; //$NON-NLS-1$


	private static final String SUBJECT_NODE = "subj"; //$NON-NLS-1$
	private static final String MESSAGE_NODE = "msg"; //$NON-NLS-1$
	private static final String SENDERS_NODE = "snders"; //$NON-NLS-1$
	private static final String SENDER_NODE = "snder"; //$NON-NLS-1$
	private static final String DATE_NODE = "date"; //$NON-NLS-1$
	private static final String EXPIRATION_DATE_NODE = "expdate"; //$NON-NLS-1$
	private static final String APPLICATION_NODE = "app"; //$NON-NLS-1$
	private static final String REJECT_TEXT_NODE = "rejt"; //$NON-NLS-1$
	private static final String REFERENCE_NODE = "ref"; //$NON-NLS-1$
	private static final String SIGN_LINES_NODE = "sgnlines"; //$NON-NLS-1$
	private static final String SIGN_LINE_NODE = "sgnline"; //$NON-NLS-1$
	private static final String RECEIVER_NODE = "rcvr"; //$NON-NLS-1$
	private static final String SIGN_STATE_ATTRIBUTE = 	"st";
	private static final String DOCUMENTS_NODE = "docs"; //$NON-NLS-1$
	private static final String ATTACHED_NODE = "attachedList"; //$NON-NLS-1$
	private static final String SIGNLINETYPE_NODE = "signlinestype"; //$NON-NLS-1$

	private static final int DEFAULT_REQUEST_PRIORITY_VALUE = 1;
	private static final boolean DEFAULT_REQUEST_WORKFLOW_VALUE = false;
	private static final boolean DEFAULT_REQUEST_FORWARD_VALUE = false;

	/** Valor usado por el Portafirmas para indicar que una petici&oacute;n es de firma. */
	private static final String REQUEST_TYPE_SIGN = "FIRMA"; //$NON-NLS-1$

	/** Valor usado por el Portafirmas para indicar que una petici&oacute;n es de visto bueno. */
	private static final String REQUEST_TYPE_APPROVE = "VISTOBUENO"; //$NON-NLS-1$

	private RequestDetailResponseParser() {
		// No instanciable
	}

	/**
	 * Analiza un documento XML y, en caso de tener el formato correcto, obtiene de &eacute;l
	 * el detalle de una petici&oacute;n de firma.
	 * @param doc Documento XML.
	 * @return Objeto con los datos del XML.
	 * @throws IllegalArgumentException Cuando el XML no tiene el formato esperado.
	 */
	public static RequestDetail parse(final Document doc) {

		if (doc == null) {
			throw new IllegalArgumentException("El documento proporcionado no puede ser nulo");  //$NON-NLS-1$
		}

		if (!DETAIL_RESPONSE_NODE.equalsIgnoreCase(doc.getDocumentElement().getNodeName())) {
			throw new IllegalArgumentException("El elemento raiz del XML debe ser '" + //$NON-NLS-1$
					DETAIL_RESPONSE_NODE + "' y aparece: " + //$NON-NLS-1$
					doc.getDocumentElement().getNodeName() + ". Detalle: " + doc.getDocumentElement().getAttribute("cd"));
		}

		final NamedNodeMap attributes = doc.getDocumentElement().getAttributes();
		final Node attNode = attributes.getNamedItem(ID_ATTRIBUTE);
		if (attNode == null || attNode.getNodeValue() == null || attNode.getNodeValue().trim().length() == 0) {
			throw new IllegalArgumentException("El detalle de la peticion carece del atributo '" + //$NON-NLS-1$
					ID_ATTRIBUTE + "' con el identificador de la peticion"); //$NON-NLS-1$
		}

		final RequestDetail reqDetail = new RequestDetail(attNode.getNodeValue());

		// Establecemos los atributos opcionales de la peticion
		setOptionalAttributes(attributes, reqDetail);

		final NodeList paramsNodes = doc.getDocumentElement().getChildNodes();

		// Configuramos el asunto
		int index = XmlUtils.nextNodeElementIndex(paramsNodes, 0);
		if (index == -1 || !SUBJECT_NODE.equalsIgnoreCase(paramsNodes.item(index).getNodeName())) {
			throw new IllegalArgumentException("No se encontro el nodo '" + //$NON-NLS-1$
					SUBJECT_NODE + "' en la peticion con identificador " + reqDetail.getId()); //$NON-NLS-1$
		}
		reqDetail.setSubject(normalizeValue(XmlUtils.getTextContent(paramsNodes.item(index))));

		// Configuramos el mensaje de la peticion (no obligatorio)
		index = XmlUtils.nextNodeElementIndex(paramsNodes, ++index);
		if (index != -1 && MESSAGE_NODE.equalsIgnoreCase(paramsNodes.item(index).getNodeName())) {
			reqDetail.setMessage(XmlUtils.getTextContent(paramsNodes.item(index)));
			index = XmlUtils.nextNodeElementIndex(paramsNodes, ++index);
		}

		// Configuramos los remitentes
		if (index == -1 || !SENDERS_NODE.equalsIgnoreCase(paramsNodes.item(index).getNodeName())) {
			throw new IllegalArgumentException("No se encontro el nodo '" + //$NON-NLS-1$
					SENDERS_NODE + "' en la peticion con identificador " + reqDetail.getId()); //$NON-NLS-1$
		}
		reqDetail.setSenders(getSenders(paramsNodes.item(index).getChildNodes()));

		// Configuramos la fecha
		index = XmlUtils.nextNodeElementIndex(paramsNodes, ++index);
		if (index == -1 || !DATE_NODE.equalsIgnoreCase(paramsNodes.item(index).getNodeName())) {
			throw new IllegalArgumentException("No se encontro el nodo '" + //$NON-NLS-1$
					DATE_NODE + "' en la peticion con identificador " + reqDetail.getId()); //$NON-NLS-1$
		}
		reqDetail.setDate(XmlUtils.getTextContent(paramsNodes.item(index)));

		// Configuramos la fecha de caducidad (no obligatoria)
		index = XmlUtils.nextNodeElementIndex(paramsNodes, ++index);
		if (index == -1 || !EXPIRATION_DATE_NODE.equalsIgnoreCase(paramsNodes.item(index).getNodeName())) {
			// No hay fecha de caducidad definida
		}
		else {
			String expDate = XmlUtils.getTextContent(paramsNodes.item(index));
			if (expDate != null && !expDate.isEmpty()) {
				reqDetail.setExpDate(expDate);
			}
			index = XmlUtils.nextNodeElementIndex(paramsNodes, ++index);
		}

		// Configuramos la aplicacion que solicito la firma
		if (index == -1 || !APPLICATION_NODE.equalsIgnoreCase(paramsNodes.item(index).getNodeName())) {
			throw new IllegalArgumentException("No se encontro el nodo '" + //$NON-NLS-1$
					APPLICATION_NODE + "' en la peticion con identificador " + reqDetail.getId()); //$NON-NLS-1$
		}
		reqDetail.setApp(normalizeValue(XmlUtils.getTextContent(paramsNodes.item(index))));

		// Configuramos el motivo de rechazo (no obligatorio)
		index = XmlUtils.nextNodeElementIndex(paramsNodes, ++index);
		if (index != -1 && REJECT_TEXT_NODE.equalsIgnoreCase(paramsNodes.item(index).getNodeName())) {
			reqDetail.setRejectReason(XmlUtils.getTextContent(paramsNodes.item(index)));
			index = XmlUtils.nextNodeElementIndex(paramsNodes, ++index);
		}

		// Configuramos la referencia de la solicitud
		if (index == -1 || !REFERENCE_NODE.equalsIgnoreCase(paramsNodes.item(index).getNodeName())) {
			throw new IllegalArgumentException("No se encontro el nodo '" + //$NON-NLS-1$
					REFERENCE_NODE + "' en la peticion con identificador " + reqDetail.getId()); //$NON-NLS-1$
		}
		reqDetail.setRef(normalizeValue(XmlUtils.getTextContent(paramsNodes.item(index))));

		// Configuramos el tipo de linea de firma (paralelo o en cascada). Si no esta definido es en cascada por defecto
		index = XmlUtils.nextNodeElementIndex(paramsNodes, ++index);
		if (index == -1 || !SIGNLINETYPE_NODE.equalsIgnoreCase(paramsNodes.item(index).getNodeName())) {
			reqDetail.setSignLinesType("cascada");
		}
		else {
			reqDetail.setSignLinesType(normalizeValue(XmlUtils.getTextContent(paramsNodes.item(index))));
			index = XmlUtils.nextNodeElementIndex(paramsNodes, ++index);
		}
		// Configuramos las linea de firma de la aplicacion

		if (index == -1 || !SIGN_LINES_NODE.equalsIgnoreCase(paramsNodes.item(index).getNodeName())) {
			throw new IllegalArgumentException("No se encontro el nodo '" + //$NON-NLS-1$
					SIGN_LINES_NODE + "' en la peticion con identificador " + reqDetail.getId()); //$NON-NLS-1$
		}
		reqDetail.setSignLines(getSignLines(paramsNodes.item(index).getChildNodes()));

		// Configuramos los documentos de la solicitud
		index = XmlUtils.nextNodeElementIndex(paramsNodes, ++index);
		if (index == -1 || !DOCUMENTS_NODE.equalsIgnoreCase(paramsNodes.item(index).getNodeName())) {
			throw new IllegalArgumentException("No se encontro el nodo '" + //$NON-NLS-1$
					DOCUMENTS_NODE + "' en la peticion con identificador " + reqDetail.getId()); //$NON-NLS-1$
		}
		reqDetail.setDocs(getDocuments(paramsNodes.item(index).getChildNodes()));

		// Configuramos los anexos de la solicitud, no es obligatorio que existan
		index = XmlUtils.nextNodeElementIndex(paramsNodes, ++index);
		if (index != -1 && ATTACHED_NODE.equalsIgnoreCase(paramsNodes.item(index).getNodeName())) {
			reqDetail.setAttached(getAttachments(paramsNodes.item(index).getChildNodes()));
		}

		return reqDetail;
	}

	/**
	 * Recoge los valores de los atributos opcionales de la solicitud: prioridad, flujo de trabajo y
	 * reenv&iacute;o.
	 * @param attributes Listado de atributos.
	 * @param reqDetail Detalle de la petici&oacute;n a la que se refieren los datos.
	 */
	private static void setOptionalAttributes(final NamedNodeMap attributes, final RequestDetail reqDetail) {
		// Establecemos la prioridad de la peticion
		Node attNode = attributes.getNamedItem(PRIORITY_ATTRIBUTE);
		if (attNode != null) {
			try {
				reqDetail.setPriority(Integer.parseInt(attNode.getNodeValue()));
			} catch (final Exception e) {
				throw new IllegalArgumentException("Se ha establecido un valor no valido en el atributo '" + //$NON-NLS-1$
						PRIORITY_ATTRIBUTE + "' en el detalle de la peticion " + reqDetail.getId()); //$NON-NLS-1$
			}
		}
		else {
			reqDetail.setPriority(DEFAULT_REQUEST_PRIORITY_VALUE);
		}

		// Establecemos el valor indicativo de Workflow de la peticion
		attNode = attributes.getNamedItem(WORKFLOW_ATTRIBUTE);
		if (attNode != null) {
			try {
				reqDetail.setWorkflow(Boolean.parseBoolean(attNode.getNodeValue()));
			} catch (final Exception e) {
				throw new IllegalArgumentException("Se ha establecido un valor no valido en el atributo'" + //$NON-NLS-1$
						WORKFLOW_ATTRIBUTE + "' en el detalle de la peticion " + reqDetail.getId()); //$NON-NLS-1$
			}
		}
		else {
			reqDetail.setWorkflow(DEFAULT_REQUEST_WORKFLOW_VALUE);
		}

		// Establecemos si la peticion fue reenviada por otro usuario
		attNode = attributes.getNamedItem(FORWARD_ATTRIBUTE);
		if (attNode != null) {
			try {
				reqDetail.setForward(Boolean.parseBoolean(attNode.getNodeValue()));
			} catch (final Exception e) {
				throw new IllegalArgumentException("Se ha establecido un valor no valido para el atributo'" + //$NON-NLS-1$
						FORWARD_ATTRIBUTE + "' en el detalle de la peticion " + reqDetail.getId()); //$NON-NLS-1$
			}
		}
		else {
			reqDetail.setForward(DEFAULT_REQUEST_FORWARD_VALUE);
		}

		// Establecemos el tipo de peticion
		attNode = attributes.getNamedItem(TYPE_ATTRIBUTE);
		if (attNode != null && attNode.getNodeValue() != null) {
			if (REQUEST_TYPE_SIGN.equalsIgnoreCase(attNode.getNodeValue())) {
				reqDetail.setType(RequestType.SIGNATURE);
			} else if (REQUEST_TYPE_APPROVE.equalsIgnoreCase(attNode.getNodeValue())) {
				reqDetail.setType(RequestType.APPROVE);
			} else {
				reqDetail.setType(null);
			}
		}
	}

	/**
	 * Obtiene el listado de remitentes de un nodo de remitentes.
	 * @param senderNodes Nodos con los remitentes.
	 * @return Listado de remitentes.
	 */
	private static String[] getSenders(final NodeList senderNodes) {
		final Vector<String> sendersList = new Vector<String>();
		for (int i = 0; i < senderNodes.getLength(); i++) {
			// Nos aseguramos de procesar solo nodos de tipo Element
			i = XmlUtils.nextNodeElementIndex(senderNodes, i);
			if (i == -1) {
				break;
			}
			if (!SENDER_NODE.equalsIgnoreCase(senderNodes.item(i).getNodeName())) {
				throw new IllegalArgumentException("Se ha encontrado el nodo " + senderNodes.item(i).getNodeName() //$NON-NLS-1$
						+ " en el listado de remitentes de la solicitud de firma"); //$NON-NLS-1$
			}
			sendersList.addElement(normalizeValue(XmlUtils.getTextContent(senderNodes.item(i))));
		}

		final String[] senders = new String[sendersList.size()];
		sendersList.copyInto(senders);
		return senders;
	}

	/**
	 * Obtiene el listado de lineas de firmante. Cada linea de firmante puede poseer un
	 * indeterminado n&uacute;mero de firmantes.
	 * @param signLinesNode Nodos con la informaci&oacute;n de los documentos..
	 * @return Listado de documentos.
	 */
	private static Vector<SignLine> getSignLines(final NodeList signLinesNode) {

		final Vector<SignLine> signLinesList = new Vector<SignLine>();
		boolean done = false;
		String type = REQUEST_TYPE_SIGN;
		for (int i = 0; i < signLinesNode.getLength(); i++) {
			// Nos aseguramos de procesar solo nodos de tipo Element
			i = XmlUtils.nextNodeElementIndex(signLinesNode, i);
			if (i == -1) {
				break;
			}
			if (!SIGN_LINE_NODE.equalsIgnoreCase(signLinesNode.item(i).getNodeName())) {
				throw new IllegalArgumentException("Se ha encontrado el nodo " + //$NON-NLS-1$
						signLinesNode.item(i).getNodeName()  + " en el listado de lineas de firma"); //$NON-NLS-1$
			}

			if (signLinesNode.item(i).hasAttributes()) {
				final NamedNodeMap attrs = signLinesNode.item(i).getAttributes();
				// Comprobamos si se indica el estado de la firma en cuestion
				if (attrs.getNamedItem(TYPE_ATTRIBUTE) != null) {
					type = attrs.getNamedItem(TYPE_ATTRIBUTE).getNodeValue();
				}
				else {
				    type = REQUEST_TYPE_SIGN;
                }
			}
            else {
                type = REQUEST_TYPE_SIGN;
            }

			final SignLine receiverList = new SignLine(type);
			final NodeList recivers = signLinesNode.item(i).getChildNodes();
			for (int j = 0; j < recivers.getLength(); j++) {
				done = false;
				j = XmlUtils.nextNodeElementIndex(recivers, j);
				if (j == -1) {
					break;
				}
				if (!RECEIVER_NODE.equalsIgnoreCase(recivers.item(j).getNodeName())) {
					throw new IllegalArgumentException("Se ha encontrado el nodo " + //$NON-NLS-1$
							recivers.item(j).getNodeName()  + " en el listado de lineas de firma"); //$NON-NLS-1$
				}

				// Comprobamos si se indica el nombre del firmante en cuestion, es obligatorio

				if (recivers.item(j).hasAttributes()) {
					final NamedNodeMap attrs = recivers.item(j).getAttributes();
					// Comprobamos si se indica el estado de la firma en cuestion
					if (attrs.getNamedItem(SIGN_STATE_ATTRIBUTE) != null) {
						done = Boolean.parseBoolean(attrs.getNamedItem(SIGN_STATE_ATTRIBUTE).getNodeValue());
					}
				}

				receiverList.addElement(
							new SignLineElement(
									normalizeValue(XmlUtils.getTextContent(recivers.item(j))),
									done
							)
				);
			}
			signLinesList.addElement(receiverList);
		}

		return signLinesList;

	}

	/**
	 * Obtiene el listado con la informacion necesaria de los documentos a firmar.
	 * @param documentNodes Nodos con la informaci&oacute;n de los documentos..
	 * @return Listado de documentos.
	 */
	private static SignRequestDocument[] getDocuments(final NodeList documentNodes) {

		final Vector<SignRequestDocument> docsList = new Vector<SignRequestDocument>();
		for (int i = 0; i < documentNodes.getLength(); i++) {
			i = XmlUtils.nextNodeElementIndex(documentNodes, i);
			if (i == -1) {
				break;
			}
			docsList.addElement(SignRequestDocumentParser.parse(documentNodes.item(i)));
		}

		final SignRequestDocument[] docs = new SignRequestDocument[docsList.size()];
		docsList.copyInto(docs);
		return docs;

	}

	private static RequestDocument[] getAttachments(final NodeList documentNodes) {

		final Vector<RequestDocument> docsList = new Vector<RequestDocument>();
		for (int i = 0; i < documentNodes.getLength(); i++) {
			i = XmlUtils.nextNodeElementIndex(documentNodes, i);
			if (i == -1) {
				break;
			}
			docsList.addElement(RequestDocumentParser.parse(documentNodes.item(i)));
		}

		final RequestDocument[] docs = new RequestDocument[docsList.size()];
		docsList.copyInto(docs);
		return docs;

	}

	/**
	 * Deshace los cambios que hizo el proxy para asegurar que el XML est&aacute;ba bien formado.
	 * @param value Valor que normalizar.
	 * @return Valor normalizado.
	 */
	static String normalizeValue(final String value) {
		return value.trim().replace("&_lt;", "<").replace("&_gt;", ">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
}
