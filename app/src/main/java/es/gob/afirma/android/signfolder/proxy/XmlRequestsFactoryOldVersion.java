package es.gob.afirma.android.signfolder.proxy;

import java.io.IOException;

import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.util.Base64;
import es.gob.afirma.android.util.PfLog;

/** Factor&iacute;a para la creaci&oacute;n de solitidudes XML hacia el servidor de firmas multi-fase.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s */
final class XmlRequestsFactoryOldVersion {

	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"; //$NON-NLS-1$

	private static final String XML_CERT_OPEN ="<cert>"; //$NON-NLS-1$
	private static final String XML_CERT_CLOSE ="</cert>"; //$NON-NLS-1$

	private static final String XML_TRISIGN_OPEN = "<rqttri>"; //$NON-NLS-1$
	private static final String XML_TRISIGN_CLOSE = "</rqttri>"; //$NON-NLS-1$

	private static final String XML_REQUESTS_OPEN = "<reqs>"; //$NON-NLS-1$
	private static final String XML_REQUESTS_CLOSE = "</reqs>"; //$NON-NLS-1$

	private static final String XML_REJECTS_OPEN = "<reqrjcts>"; //$NON-NLS-1$
	private static final String XML_REJECTS_CLOSE = "</reqrjcts>"; //$NON-NLS-1$

	private static final String XML_APPROVE_OPEN = "<apprv>"; //$NON-NLS-1$
	private static final String XML_APPROVE_CLOSE = "</apprv>"; //$NON-NLS-1$

	private static final String XML_PARAMS_OPEN = "<params>"; //$NON-NLS-1$
	private static final String XML_PARAMS_CLOSE = "</params>"; //$NON-NLS-1$

	private static final String XML_RESULT_OPEN = "<result>"; //$NON-NLS-1$
	private static final String XML_RESULT_CLOSE = "</result>"; //$NON-NLS-1$
	
	private static final String XML_RSN_OPEN ="<rsn>"; //$NON-NLS-1$
	private static final String XML_RSN_CLOSE ="</rsn>"; //$NON-NLS-1$


	private XmlRequestsFactoryOldVersion() {
		// No permitimos la instanciacion
	}

	/**
	 * Crea un XML para la solicitud al proxy de un listado de peticiones.
	 * @param certEncoded Certificado codificado en Base64 con el que validar la petici&oacute;n.
	 * @param state Estado de la petici&oacute;n.
	 * @param signFormats Listado de formatos de firma soportados.
	 * @param filters Filtros para limitar las peticiones devueltas.
	 * @return XML para la solicitud de las peticiones de firma.
	 */
	static String createRequestListRequest(final String certEncoded, final String state, final String[] signFormats, final String[] filters, final int numPage, final int pageSize) {

		final StringBuffer sb = new StringBuffer(XML_HEADER);
		sb.append("<rqtlst state=\""); //$NON-NLS-1$
		sb.append(state);
		sb.append("\" pg=\""); //$NON-NLS-1$
		sb.append(numPage);
		sb.append("\" sz=\""); //$NON-NLS-1$
		sb.append(pageSize);
		sb.append("\">"); //$NON-NLS-1$

		sb.append(XML_CERT_OPEN);
		sb.append(certEncoded);
		sb.append(XML_CERT_CLOSE);

		if (signFormats != null && signFormats.length > 0) {
			sb.append("<fmts>"); //$NON-NLS-1$
			for (final String signFormat : signFormats) {
				sb.append("<fmt>"); //$NON-NLS-1$
				sb.append(signFormat);
				sb.append("</fmt>"); //$NON-NLS-1$
			}
			sb.append("</fmts>"); //$NON-NLS-1$
		}

		if (filters != null && filters.length > 0) {
			sb.append("<fltrs>"); //$NON-NLS-1$
			for (final String filter : filters) {
				sb.append("<fltr>"); //$NON-NLS-1$
				final int equalPos = filter.indexOf('=') != -1 ? filter.indexOf('=') : filter.length();
				sb.append("<key>"); //$NON-NLS-1$
				if (equalPos > 0) {
					sb.append(filter.substring(0, equalPos));
				}
				sb.append("</key>"); //$NON-NLS-1$
				sb.append("<value>"); //$NON-NLS-1$
				if (equalPos < filter.length() - 1) {
					sb.append(filter.substring(equalPos + 1));
				}
				sb.append("</value>"); //$NON-NLS-1$
				sb.append("</fltr>"); //$NON-NLS-1$
			}
			sb.append("</fltrs>"); //$NON-NLS-1$
		}

		sb.append("</rqtlst>"); //$NON-NLS-1$

	    return sb.toString();
	}

	/** Crea una solicitud de prefirma a partir de una lista de peticiones de firma.
	 * @param request Petici&oacute;n de firma
	 * @param requesterCert Certificado del solicitante codificado en Base64
	 * @return Solicitud de prefirma a partir de una lista de peticiones de firma
	 * @throws IOException Si ocurre alg&uacute;n problema durante la construcci&oacute;n de la solicitud */
	static String createPresignRequest(final SignRequest request, final String requesterCert) throws IOException {
		if (request == null) {
			throw new IllegalArgumentException("La lista de peticiones no puede ser nula"); //$NON-NLS-1$
		}
		if (requesterCert == null) {
			throw new IllegalArgumentException("El certificado del solicitante no puede ser nulo"); //$NON-NLS-1$
		}
		final StringBuffer sb = new StringBuffer(XML_HEADER);
		sb.append(XML_TRISIGN_OPEN);

		// Certificado
		sb.append(XML_CERT_OPEN);
		sb.append(requesterCert);
		sb.append(XML_CERT_CLOSE);

		// Listado de peticiones
		sb.append(XML_REQUESTS_OPEN);

		// Peticion
		SignRequestDocument[] documents;

		sb.append("<req id=\""); //$NON-NLS-1$
		sb.append(request.getId());
		sb.append("\">"); //$NON-NLS-1$
		documents = request.getDocs();
		for (final SignRequestDocument document : documents) {

			PfLog.i(SFConstants.LOG_TAG, "Parametros que se agregan:\n" + document.getParams()); //$NON-NLS-1$ //$NON-NLS-2$

			sb.append("<doc docid=\"") //$NON-NLS-1$
			.append(document.getId())
			.append("\" cop=\"") //$NON-NLS-1$
			.append(document.getCryptoOperation())
			.append("\" sigfrmt=\"") //$NON-NLS-1$
			.append(document.getSignFormat())
			.append("\" mdalgo=\"") //$NON-NLS-1$
			.append(document.getMessageDigestAlgorithm())
			.append("\">") //$NON-NLS-1$
			.append(XML_PARAMS_OPEN)
			.append(document.getParams() == null ? "" : document.getParams()) //$NON-NLS-1$
			.append(XML_PARAMS_CLOSE)
			.append("</doc>"); //$NON-NLS-1$
		}
		sb.append("</req>"); //$NON-NLS-1$

		sb.append(XML_REQUESTS_CLOSE); // Cierre del listado de peticiones

		sb.append(XML_TRISIGN_CLOSE); // Cierre del XML

		return sb.toString();
	}

	/** Crea una solicitud de postfirma a partir de una lista de peticiones de firma.
	 * @param requests Lista de peticiones de firma
	 * @param requesterCert Certificado del solicitante codificado en Base64
	 * @return Solicitud de postfirma a partir de una lista de peticiones de firma
	 * @throws IOException Si ocurre alg&uacute;n problema durante la construcci&oacute;n de la solicitud */
	static String createPostsignRequest(final TriphaseRequest[] requests, final String requesterCert) throws IOException {
		if (requests == null) {
			throw new IllegalArgumentException("La lista de peticiones no puede ser nula"); //$NON-NLS-1$
		}
		if (requesterCert == null) {
			throw new IllegalArgumentException("El certificado del solicitante no puede ser nulo"); //$NON-NLS-1$
		}
		final StringBuffer sb = new StringBuffer(XML_HEADER);
		sb.append(XML_TRISIGN_OPEN);

		// Certificado
		sb.append(XML_CERT_OPEN);
		sb.append(requesterCert);
		sb.append(XML_CERT_CLOSE);

		// Peticiones
		sb.append(XML_REQUESTS_OPEN);
	    TriphaseSignDocumentRequest[] documents;
	    for (final TriphaseRequest request : requests) {
	    	sb.append("<req id=\""); //$NON-NLS-1$
	    	sb.append(request.getRef());
	    	sb.append("\" status=\""); //$NON-NLS-1$
	    	sb.append(request.isStatusOk() ? "OK" : "KO"); //$NON-NLS-1$ //$NON-NLS-2$
	    	sb.append("\">"); //$NON-NLS-1$
	    	// Solo procesamos los documentos si la peticion es buena
	    	if (request.isStatusOk()) {
		    	documents = request.getDocumentsRequests();
		    	for (final TriphaseSignDocumentRequest document : documents) {

		    		PfLog.i(SFConstants.LOG_TAG, "Parametros que se agregan:\n" + document.getParams()); //$NON-NLS-1$ //$NON-NLS-2$

		    		sb.append("<doc docid=\"") //$NON-NLS-1$
		    		.append(document.getId())
		    		.append("\" cop=\"") //$NON-NLS-1$
		    		.append(document.getCryptoOperation())
		    		.append("\" sigfrmt=\"") //$NON-NLS-1$
		    		.append(document.getSignatureFormat())
		    		.append("\" mdalgo=\"") //$NON-NLS-1$
		    		.append(document.getMessageDigestAlgorithm())
		    		.append("\">") //$NON-NLS-1$
		    		.append(XML_PARAMS_OPEN)
		    		.append(document.getParams() == null ? "" : document.getParams()) //$NON-NLS-1$
		    		.append(XML_PARAMS_CLOSE)
		    		.append(XML_RESULT_OPEN)
		    		.append(document.getPartialResult().toXMLParamList())
		    		.append(XML_RESULT_CLOSE)
		    		.append("</doc>"); //$NON-NLS-1$
		    	}
	    	}
	    	sb.append("</req>"); //$NON-NLS-1$
	    }
		sb.append(XML_REQUESTS_CLOSE);

		sb.append(XML_TRISIGN_CLOSE);

		// Imprimimos la peticion en el log
		PfLog.i(SFConstants.LOG_TAG, "Peticion postfirma:"); //$NON-NLS-1$ //$NON-NLS-2$
		final int BUFFER_LENGTH = 1000;
		final String urlString = sb.toString();
		for (int i = 0; i < urlString.length() / BUFFER_LENGTH + 1; i++) {
			PfLog.i(SFConstants.LOG_TAG, urlString.substring(i * BUFFER_LENGTH, Math.min((i + 1) * BUFFER_LENGTH, urlString.length()))); //$NON-NLS-1$
		}

		return sb.toString();
	}

	static String createRejectRequest(final String[] requestIds, final String certB64, final String reason) {
		if (requestIds == null || requestIds.length == 0) {
			throw new IllegalArgumentException("La lista de peticiones no puede ser nula"); //$NON-NLS-1$
		}

		final StringBuffer sb = new StringBuffer(XML_HEADER);
		sb.append(XML_REJECTS_OPEN);
		sb.append(XML_CERT_OPEN);
		sb.append(certB64);
		sb.append(XML_CERT_CLOSE);
	    if (reason != null && !reason.trim().isEmpty()) {
		    sb.append(XML_RSN_OPEN);
		    sb.append(Base64.encode(reason.getBytes()));
		    sb.append(XML_RSN_CLOSE);
	    }
		sb.append("<rjcts>"); //$NON-NLS-1$
		// Peticiones que se rechazan
	    for (final String requestId : requestIds) {
	    	sb.append("<rjct id=\""); //$NON-NLS-1$
	    	sb.append(requestId);
	    	sb.append("\"/>"); //$NON-NLS-1$
	    }
	    sb.append("</rjcts>"); //$NON-NLS-1$
	    sb.append(XML_REJECTS_CLOSE);

	    return sb.toString();
	}


	static String createAppListRequest(final String certEncodedB64) {

		final StringBuffer sb = new StringBuffer(XML_HEADER);
		sb.append("<rqtconf>"); //$NON-NLS-1$
		sb.append(XML_CERT_OPEN);
		sb.append(certEncodedB64);
		sb.append(XML_CERT_CLOSE);
		sb.append("</rqtconf>"); //$NON-NLS-1$

	    return sb.toString();
	}

	static String createDetailRequest(final String certEncodedB64, final String requestId) {
		if (requestId == null || requestId.trim().length() == 0) {
			throw new IllegalArgumentException("El identificador de la solicitud de firma no puede ser nulo"); //$NON-NLS-1$
		}

		final StringBuffer sb = new StringBuffer(XML_HEADER);
		sb.append("<rqtdtl id=\""); //$NON-NLS-1$
		sb.append(requestId);
		sb.append("\">"); //$NON-NLS-1$
		sb.append(XML_CERT_OPEN);
		sb.append(certEncodedB64);
		sb.append(XML_CERT_CLOSE);
		sb.append("</rqtdtl>"); //$NON-NLS-1$

	    return sb.toString();
	}

	static String createPreviewRequest(final String documentId, final String certB64) {
		if (documentId == null || documentId.trim().length() == 0) {
			throw new IllegalArgumentException("El identificador de documento no puede ser nulo"); //$NON-NLS-1$
		}

		final StringBuffer sb = new StringBuffer(XML_HEADER);
		sb.append("<rqtprw docid=\""); //$NON-NLS-1$
		sb.append(documentId);
		sb.append("\">"); //$NON-NLS-1$
		sb.append(XML_CERT_OPEN);
		sb.append(certB64);
		sb.append(XML_CERT_CLOSE);
		sb.append("</rqtprw>"); //$NON-NLS-1$

	    return sb.toString();
	}

	static String createApproveRequest(final String[] requestIds, final String certB64) {
		if (requestIds == null || requestIds.length == 0) {
			throw new IllegalArgumentException("La lista de peticiones no puede ser nula"); //$NON-NLS-1$
		}

		final StringBuffer sb = new StringBuffer(XML_HEADER);
		sb.append(XML_APPROVE_OPEN);
		sb.append(XML_CERT_OPEN);
		sb.append(certB64);
		sb.append(XML_CERT_CLOSE);
		sb.append("<reqs>"); //$NON-NLS-1$
		// Peticiones que se rechazan
	    for (final String requestId : requestIds) {
	    	sb.append("<r id=\""); //$NON-NLS-1$
	    	sb.append(requestId);
	    	sb.append("\"/>"); //$NON-NLS-1$
	    }
	    sb.append("</reqs>"); //$NON-NLS-1$
	    sb.append(XML_APPROVE_CLOSE);

	    return sb.toString();
	}
}
