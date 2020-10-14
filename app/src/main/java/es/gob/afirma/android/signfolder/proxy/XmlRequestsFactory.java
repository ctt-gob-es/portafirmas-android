package es.gob.afirma.android.signfolder.proxy;

import java.io.IOException;
import java.util.List;

import es.gob.afirma.android.user.configuration.AuthorizedUser;
import es.gob.afirma.android.user.configuration.ConfigurationRole;
import es.gob.afirma.android.user.configuration.UserInfo;
import es.gob.afirma.android.util.Base64;
import es.gob.afirma.android.util.PfLog;

/**
 * Factor&iacute;a para la creaci&oacute;n de solitidudes XML hacia el servidor de firmas multi-fase.
 *
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s
 */
final class XmlRequestsFactory {

    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"; //$NON-NLS-1$

    private static final String XML_CERT_OPEN = "<cert>"; //$NON-NLS-1$
    private static final String XML_CERT_CLOSE = "</cert>"; //$NON-NLS-1$

    private static final String XML_TRISIGN_OPEN = "<rqttri>"; //$NON-NLS-1$
    private static final String XML_TRISIGN_CLOSE = "</rqttri>"; //$NON-NLS-1$

    private static final String XML_REQUESTS_OPEN = "<reqs>"; //$NON-NLS-1$
    private static final String XML_REQUESTS_CLOSE = "</reqs>"; //$NON-NLS-1$

    private static final String XML_REJECTS_OPEN = "<reqrjcts>"; //$NON-NLS-1$
    private static final String XML_REJECTS_CLOSE = "</reqrjcts>"; //$NON-NLS-1$

    private static final String XML_APPROVE_OPEN = "<apprv>"; //$NON-NLS-1$
    private static final String XML_APPROVE_CLOSE = "</apprv>"; //$NON-NLS-1$

    private static final String XML_VERIFY_OPEN = "<verfreq>"; //$NON-NLS-1$
    private static final String XML_VERIFY_CLOSE = "</verfreq>"; //$NON-NLS-1$

    private static final String XML_PARAMS_OPEN = "<params>"; //$NON-NLS-1$
    private static final String XML_PARAMS_CLOSE = "</params>"; //$NON-NLS-1$

    private static final String XML_RESULT_OPEN = "<result>"; //$NON-NLS-1$
    private static final String XML_RESULT_CLOSE = "</result>"; //$NON-NLS-1$

    private static final String XML_RSN_OPEN = "<rsn>"; //$NON-NLS-1$
    private static final String XML_RSN_CLOSE = "</rsn>"; //$NON-NLS-1$

    private XmlRequestsFactory() {
        // No permitimos la instanciacion
    }

    static String createValidateLogin(String cert, byte[] pkcs1) {
        final StringBuffer sb = new StringBuffer(XML_HEADER);
        sb.append("<rqtvl><cert>"); //$NON-NLS-1$
        sb.append(cert);
        sb.append("</cert><pkcs1>"); //$NON-NLS-1$
        sb.append(Base64.encode(pkcs1));
        sb.append("</pkcs1></rqtvl>"); //$NON-NLS-1$

        return sb.toString();
    }


    /**
     * Crea un XML para la solicitud al proxy de un listado de peticiones.
     *
     * @param state       Estado de la petici&oacute;n.
     * @param signFormats Listado de formatos de firma soportados.
     * @param filters     Filtros para limitar las peticiones devueltas.
     * @return XML para la solicitud de las peticiones de firma.
     */
    static String createRequestListRequest(final String state, final String[] signFormats, final String[] filters, final int numPage, final int pageSize) {

        final StringBuffer sb = new StringBuffer(XML_HEADER);
        sb.append("<rqtlst state=\""); //$NON-NLS-1$
        sb.append(state);
        sb.append("\" pg=\""); //$NON-NLS-1$
        sb.append(numPage);
        sb.append("\" sz=\""); //$NON-NLS-1$
        sb.append(pageSize);
        sb.append("\">"); //$NON-NLS-1$

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

    /**
     * Crea una solicitud de prefirma a partir de una lista de peticiones de firma.
     *
     * @param request Petici&oacute;n de firma
     * @return Solicitud de prefirma a partir de una lista de peticiones de firma
     * @throws IOException Si ocurre alg&uacute;n problema durante la construcci&oacute;n de la solicitud
     */
    static String createPresignRequest(SignRequest request) throws IOException {
        if (request == null) {
            throw new IllegalArgumentException("La lista de peticiones no puede ser nula"); //$NON-NLS-1$
        }
        final StringBuffer sb = new StringBuffer(XML_HEADER);
        sb.append(XML_TRISIGN_OPEN);

        // Listado de peticiones
        sb.append(XML_REQUESTS_OPEN);

        // Peticion
        SignRequestDocument[] documents;

        sb.append("<req id=\""); //$NON-NLS-1$
        sb.append(request.getId());
        sb.append("\">"); //$NON-NLS-1$
        documents = request.getDocs();
        for (final SignRequestDocument document : documents) {

            PfLog.i("es.gob.afirma", "Parametros que se agregan:\n" + document.getParams()); //$NON-NLS-1$ //$NON-NLS-2$

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

    /**
     * Crea una solicitud de postfirma a partir de una lista de peticiones de firma.
     *
     * @param requests Lista de peticiones de firma
     * @return Solicitud de postfirma a partir de una lista de peticiones de firma
     * @throws IOException Si ocurre alg&uacute;n problema durante la construcci&oacute;n de la solicitud
     */
    static String createPostsignRequest(final TriphaseRequest[] requests) throws IOException {
        if (requests == null) {
            throw new IllegalArgumentException("La lista de peticiones no puede ser nula"); //$NON-NLS-1$
        }
        final StringBuffer sb = new StringBuffer(XML_HEADER);
        sb.append(XML_TRISIGN_OPEN);

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

                    PfLog.i("es.gob.afirma", "Parametros que se agregan:\n" + document.getParams()); //$NON-NLS-1$ //$NON-NLS-2$

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
        PfLog.i("es.gob.afirma", "Peticion postfirma:"); //$NON-NLS-1$ //$NON-NLS-2$
        final int BUFFER_LENGTH = 1000;
        final String urlString = sb.toString();
        for (int i = 0; i < urlString.length() / BUFFER_LENGTH + 1; i++) {
            PfLog.i("es.gob.afirma", urlString.substring(i * BUFFER_LENGTH, Math.min((i + 1) * BUFFER_LENGTH, urlString.length()))); //$NON-NLS-1$
        }

        return sb.toString();
    }

    static String createRejectRequest(final String[] requestIds, final String reason) {
        if (requestIds == null || requestIds.length == 0) {
            throw new IllegalArgumentException("La lista de peticiones no puede ser nula"); //$NON-NLS-1$
        }

        final StringBuffer sb = new StringBuffer(XML_HEADER);
        sb.append(XML_REJECTS_OPEN);
		/*sb.append(XML_CERT_OPEN);
		sb.append(certB64);
		sb.append(XML_CERT_CLOSE);*/
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
        if (certEncodedB64 != null) {
            sb.append(XML_CERT_OPEN);
            sb.append(certEncodedB64);
            sb.append(XML_CERT_CLOSE);
        }
        sb.append("</rqtconf>"); //$NON-NLS-1$

        return sb.toString();
    }

    static String createDetailRequest(final String requestId, String ownerId) {
        if (requestId == null || requestId.trim().length() == 0) {
            throw new IllegalArgumentException("El identificador de la solicitud de firma no puede ser nulo"); //$NON-NLS-1$
        }

        final StringBuffer sb = new StringBuffer(XML_HEADER);
        sb.append("<rqtdtl id=\""); //$NON-NLS-1$
        sb.append(requestId);
        if (ownerId != null && ownerId.trim().length() != 0) {
            sb.append("\" ownerId=\""); //$NON-NLS-1$
            sb.append(ownerId);
        }
        sb.append("\">"); //$NON-NLS-1$
        //sb.append(XML_CERT_OPEN);
        //sb.append(certEncodedB64);
        //sb.append(XML_CERT_CLOSE);
        sb.append("</rqtdtl>"); //$NON-NLS-1$

        return sb.toString();
    }

    static String createPreviewRequest(final String documentId) {
        if (documentId == null || documentId.trim().length() == 0) {
            throw new IllegalArgumentException("El identificador de documento no puede ser nulo"); //$NON-NLS-1$
        }

        final StringBuffer sb = new StringBuffer(XML_HEADER);
        sb.append("<rqtprw docid=\""); //$NON-NLS-1$
        sb.append(documentId);
        sb.append("\">"); //$NON-NLS-1$
        //sb.append(XML_CERT_OPEN);
        //sb.append(certB64);
        //sb.append(XML_CERT_CLOSE);
        sb.append("</rqtprw>"); //$NON-NLS-1$

        return sb.toString();
    }

    static String createApproveRequest(final String[] requestIds) {
        if (requestIds == null || requestIds.length == 0) {
            throw new IllegalArgumentException("La lista de peticiones no puede ser nula"); //$NON-NLS-1$
        }

        final StringBuffer sb = new StringBuffer(XML_HEADER);
        sb.append(XML_APPROVE_OPEN);
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

    static String createVerifyRequest(final String[] requestIds) {
        if (requestIds == null || requestIds.length == 0) {
            throw new IllegalArgumentException("La lista de peticiones no puede ser nula"); //$NON-NLS-1$
        }

        final StringBuffer sb = new StringBuffer(XML_HEADER);
        sb.append(XML_VERIFY_OPEN);
        sb.append("<reqs>"); //$NON-NLS-1$
        // Peticiones que se validan.
        for (final String requestId : requestIds) {
            sb.append("<r id=\""); //$NON-NLS-1$
            sb.append(requestId);
            sb.append("\"/>"); //$NON-NLS-1$
        }
        sb.append("</reqs>"); //$NON-NLS-1$
        sb.append(XML_VERIFY_CLOSE);

        return sb.toString();
    }

    static String createRegisterNotificationRequest(final String token, final String device, final String idUsuario) {

        final StringBuffer sb = new StringBuffer(XML_HEADER);
        sb.append("<rqtreg ") //$NON-NLS-1$
                .append("plt='1' ") //$NON-NLS-1$
                .append("tkn='").append(token).append("' ") //$NON-NLS-1$ //$NON-NLS-2$
                .append("dvc='").append(device).append("'>") //$NON-NLS-1$ //$NON-NLS-2$
                .append("<cert>").append(idUsuario).append("</cert>") //$NON-NLS-1$ //$NON-NLS-2$
                .append("</rqtreg>"); //$NON-NLS-1$

        return sb.toString();
    }


    static String createFireLoadDataRequest(SignRequest[] requests) throws IOException {
        if (requests == null) {
            throw new IllegalArgumentException("La lista de peticiones no puede ser nula"); //$NON-NLS-1$
        }
        final StringBuffer sb = new StringBuffer(XML_HEADER);
        sb.append(XML_TRISIGN_OPEN);

        // Listado de peticiones
        sb.append(XML_REQUESTS_OPEN);

        for (SignRequest request : requests) {

            // Peticion
            sb.append("<req id=\""); //$NON-NLS-1$
            sb.append(request.getId());
            sb.append("\"/>"); //$NON-NLS-1$

            // No es necesario que indiquemos los documentos porque el Portafirmas ya los conoce
        }
        sb.append(XML_REQUESTS_CLOSE); // Cierre del listado de peticiones

        sb.append(XML_TRISIGN_CLOSE); // Cierre del XML

        return sb.toString();
    }

    /**
     * Crea una solicitud de firma de peticiones con FIRe.
     *
     * @return XML para la solicitud de firma con FIRe.
     */
    static String createFireSignRequest() {

        return XML_HEADER +
                "<cfrq />";
    }

    /**
     * Método que crea una petición para obtener la lista de autorizaciones o de validadores.
     *
     * @param role     Filtro a usar en la búsqueda. Puede tener 2 valores: "authorized" o "verifier".
     * @param numPage  Número de la página requerída.
     * @param pageSize Tamaño de cada página.
     * @return una lista de usuarios que coinciden con el rol proporcionado.
     */
    public static String createRequestListRoles(final ConfigurationRole role, final int numPage, final int pageSize) {
        final StringBuffer sb = new StringBuffer(XML_HEADER);
        sb.append("<rqrolels"); //$NON-NLS-1$
        sb.append(" pg=\""); //$NON-NLS-1$
        sb.append(numPage);
        sb.append("\""); //$NON-NLS-1$
        sb.append(" sz=\""); //$NON-NLS-1$
        sb.append(pageSize);
        sb.append("\">"); //$NON-NLS-1$
        sb.append("<role>"); //$NON-NLS-1$
        sb.append(role.value);
        sb.append("</role>"); //$NON-NLS-1$
        sb.append("</rqrolels>"); //$NON-NLS-1$
        return sb.toString();
    }

    /**
     * Método que crea una nueva petición para obtener la lista de usuarios a partir de filtros.
     *
     * @param numPage  Número de la página solicitada.
     * @param pageSize Tamaño de cada página.
     * @param filter   Filtro del usuario.
     * @return la petición construida.
     */
    public static String createRequestListUsers(final int numPage, final int pageSize, String filter) {
        final StringBuffer sb = new StringBuffer(XML_HEADER);
        sb.append("<rquserls"); //$NON-NLS-1$
        sb.append(" pg=\""); //$NON-NLS-1$
        sb.append(numPage);
        sb.append("\""); //$NON-NLS-1$
        sb.append(" sz=\""); //$NON-NLS-1$
        sb.append(pageSize);
        sb.append("\">"); //$NON-NLS-1$
        sb.append("<filter>"); //$NON-NLS-1$
        sb.append(filter);
        sb.append("</filter>"); //$NON-NLS-1$
        sb.append("</rquserls>"); //$NON-NLS-1$
        return sb.toString();
    }

    /**
     * Método que create una nueva petición para la creación de un nuevo rol.
     *
     * @param user   Usuario seleccionado para proporcionarle el rol.
     * @param role   Tipo de rol seleccionado.
     * @param appIds Lista de identificadores de aplicación.
     * @return la petición construida.
     */
    public static String createRequestCreateRole(final UserInfo user, final ConfigurationRole role, final AuthorizedUser authUser, final List<String> appIds) {
        final StringBuffer sb = new StringBuffer(XML_HEADER);
        sb.append("<rqCrtRole>"); //$NON-NLS-1$
        sb.append("<userId>"); //$NON-NLS-1$
        sb.append(user.getID());
        sb.append("</userId>"); //$NON-NLS-1$
        sb.append("<role>"); //$NON-NLS-1$
        sb.append(role.name());
        sb.append("</role>"); //$NON-NLS-1$
        if (appIds != null && !appIds.isEmpty()) {
            sb.append("<apps>"); //$NON-NLS-1$
            for (String appId : appIds) {
                sb.append("<appId>"); //$NON-NLS-1$
                sb.append(appId);
                sb.append("</appId>"); //$NON-NLS-1$
            }
            sb.append("</apps>"); //$NON-NLS-1$
        }
        if (authUser != null) {
            sb.append("<authParams>"); //$NON-NLS-1$
            if (authUser.getInitDate() != null) {
                sb.append("<initDate>"); //$NON-NLS-1$
                sb.append(authUser.getInitDate().getTime());
                sb.append("</initDate>"); //$NON-NLS-1$
            }
            if (authUser.getEndDate() != null) {
                sb.append("<endDate>"); //$NON-NLS-1$
                sb.append(authUser.getEndDate().getTime());
                sb.append("</endDate>"); //$NON-NLS-1$
            }
            if (authUser.getType() != null) {
                sb.append("<authType>"); //$NON-NLS-1$
                sb.append(authUser.getType());
                sb.append("</authType>"); //$NON-NLS-1$
            }
            if (authUser.getObservations() != null && !authUser.getObservations().isEmpty()) {
                sb.append("<obs>"); //$NON-NLS-1$
                sb.append(authUser.getObservations());
                sb.append("</obs>"); //$NON-NLS-1$
            }
            sb.append("</authParams>"); //$NON-NLS-1$
        }
        sb.append("</rqCrtRole>"); //$NON-NLS-1$
        return sb.toString();
    }

    /**
     * Método que crea una petición de consulta de la configuración de usuario.
     *
     * @return la configuración de usuario.
     */
    public static String createRequestGetUserConfig() {
        StringBuilder sb = new StringBuilder();
        sb.append("<rqsrcnfg />");
        return sb.toString();
    }
}
