package es.gob.afirma.android.signfolder.proxy;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;

import es.gob.afirma.android.network.AndroidUrlHttpManager;
import es.gob.afirma.android.network.ConnectionResponse;
import es.gob.afirma.android.signfolder.AppPreferences;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.proxy.parsers.ApplicationListResponseParser;
import es.gob.afirma.android.signfolder.proxy.parsers.ApproveResponseParser;
import es.gob.afirma.android.signfolder.proxy.parsers.ClaveLoginRequestResponseParser;
import es.gob.afirma.android.signfolder.proxy.parsers.FindUserResponseParser;
import es.gob.afirma.android.signfolder.proxy.parsers.FireLoadDataResponseParser;
import es.gob.afirma.android.signfolder.proxy.parsers.FireSignResponseParser;
import es.gob.afirma.android.signfolder.proxy.parsers.GenericResponseParser;
import es.gob.afirma.android.signfolder.proxy.parsers.ListAuthorizationsResponseParser;
import es.gob.afirma.android.signfolder.proxy.parsers.ListValidatorsResponseParser;
import es.gob.afirma.android.signfolder.proxy.parsers.LoginTokenResponseParser;
import es.gob.afirma.android.signfolder.proxy.parsers.LoginValidationResponseParser;
import es.gob.afirma.android.signfolder.proxy.parsers.LogoutResponseParser;
import es.gob.afirma.android.signfolder.proxy.parsers.PostsignsResponseParser;
import es.gob.afirma.android.signfolder.proxy.parsers.PresignsResponseParser;
import es.gob.afirma.android.signfolder.proxy.parsers.RegisterOnNotificationParser;
import es.gob.afirma.android.signfolder.proxy.parsers.RejectsResponseParser;
import es.gob.afirma.android.signfolder.proxy.parsers.RequestDetailResponseParser;
import es.gob.afirma.android.signfolder.proxy.parsers.RequestListResponseParser;
import es.gob.afirma.android.signfolder.proxy.parsers.UpdatePushNotsResponseParser;
import es.gob.afirma.android.signfolder.proxy.parsers.UserConfigurationResponseParser;
import es.gob.afirma.android.signfolder.proxy.parsers.VerifyResponseParser;
import es.gob.afirma.android.user.configuration.Authorization;
import es.gob.afirma.android.user.configuration.ConfigurationRole;
import es.gob.afirma.android.user.configuration.GenericUser;
import es.gob.afirma.android.user.configuration.UserConfig;
import es.gob.afirma.android.user.configuration.Validator;
import es.gob.afirma.android.util.AOUtil;
import es.gob.afirma.android.util.Base64;
import es.gob.afirma.android.util.PfLog;

/**
 * Gestor de comunicaciones con el servidor de portafirmas m&oacute;vil.
 *
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s.
 */
public final class CommManager extends CommManagerOldVersion {

    private static final String HTTPS = "https"; //$NON-NLS-1$

    private static final String PARAMETER_NAME_OPERATION_FIRST = "?op="; //$NON-NLS-1$
    private static final String PARAMETER_NAME_DATA = "&dat="; //$NON-NLS-1$
    private static final String PARAMETER_NAME_SHARED_SESSION_ID = "&ssid="; //$NON-NLS-1$

    /**
     * Operation identifiers.
     */
    private static final String OPERATION_PRESIGN = "0"; //$NON-NLS-1$
    private static final String OPERATION_POSTSIGN = "1"; //$NON-NLS-1$
    private static final String OPERATION_REQUEST = "2"; //$NON-NLS-1$
    private static final String OPERATION_REJECT = "3"; //$NON-NLS-1$
    private static final String OPERATION_DETAIL = "4"; //$NON-NLS-1$
    private static final String OPERATION_PREVIEW_DOCUMENT = "5"; //$NON-NLS-1$
    private static final String OPERATION_APP_LIST = "6"; //$NON-NLS-1$
    private static final String OPERATION_APPROVE = "7"; //$NON-NLS-1$
    private static final String OPERATION_PREVIEW_SIGN = "8"; //$NON-NLS-1$
    private static final String OPERATION_PREVIEW_REPORT = "9"; //$NON-NLS-1$
    private static final String OPERATION_LOGIN_REQUEST = "10"; //$NON-NLS-1$
    private static final String OPERATION_LOGIN_VALIDATION = "11"; //$NON-NLS-1$
    private static final String OPERATION_LOGOUT_REQUEST = "12"; //$NON-NLS-1$
    private static final String OPERATION_NOTIFICATION_SERVICE_REGISTER = "13"; //$NON-NLS-1$
    private static final String OPERATION_CLAVE_LOGIN_REQUEST = "14"; //$NON-NLS-1$
    private static final String OPERATION_PRESIGN_CLAVE_FIRMA = "16"; //$NON-NLS-1$
    private static final String OPERATION_POSTSIGN_CLAVE_FIRMA = "17"; //$NON-NLS-1$
    private static final String OPERATION_GET_USER_CONFIG = "18"; //$NON-NLS-1$

    // Servicio de busqueda de usuario
    private static final String OPERATION_FIND_USERS = "19"; //$NON-NLS-1$

    private static final String OPERATION_VERIFY = "20"; //$NON-NLS-1$

    private static final String OPERATION_UPDATE_PUSH_NOTIFICATIONS = "23";

    // Servicio de listado de autorizaciones
    private static final String OPERATION_LIST_AUTHORIZATIONS = "24"; //$NON-NLS-1$
    // Servicio de guardado de una autorizacion
    private static final String OPERATION_SAVE_AUTHORIZATION = "25"; //$NON-NLS-1$
    // Servicio de cancelacion de una autorizacion
    private static final String OPERATION_REVOKE_AUTHORIZATION = "26"; //$NON-NLS-1$
    // Servicio de aceptacion de una autorizacion
    private static final String OPERATION_ACCEPT_AUTHORIZATION = "27"; //$NON-NLS-1$
    // Servicio de listado de validadores
    private static final String OPERATION_LIST_VALIDATORS = "28"; //$NON-NLS-1$
    // Servicio de guardado de un validador
    private static final String OPERATION_SAVE_VALIDATOR = "29"; //$NON-NLS-1$
    // Servicio de cancelacion de un validador
    private static final String OPERATION_REVOKE_VALIDATOR = "30"; //$NON-NLS-1$

    private static final int BUFFER_SIZE = 1024;

    // Variables utilizadas en la autenticacion con certificado local
    private static CommManager instance = null;

    // Variables utilizadas en la autenticacion con certificado en la nube
    /**
     * Certificado de autenticaci&oacute;n en base 64.
     */
    private String certb64;
    /**
     * Identificador de sesi&oacute;n remoto.
     */
    private String remoteId = null;
    private boolean oldProxy = false;

    private boolean logged = false;

    private CommManager() {
        super();
    }

    /**
     * Obtiene una instancia de la clase.
     *
     * @return Gestor de comunicaciones con el Proxy.
     */
    public static CommManager getInstance() {
        if (instance == null) {
            instance = new CommManager();
        }
        return instance;
    }

    /**
     * Reinicia la confifuraci&oacute;n del gestor para
     * permitir que luego se inicie una nueva conexi&oacute; con el servicio proxy.
     */
    public static void resetConfig() {
        instance = null;
    }

    /**
     * Lee el contenido completo de un flujo de entrada de datos.
     *
     * @param is Flujo de entrada de datos.
     * @return Datos contenidos en el flujo.
     * @throws IOException Cuando no se pueden leer los datos.
     */
    private static byte[] readDataFromInpuStream(InputStream is) throws IOException {
        int n;
        byte[] buffer = new byte[BUFFER_SIZE];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((n = is.read(buffer)) > 0) {
            baos.write(buffer, 0, n);
        }
        return baos.toByteArray();
    }

    /**
     * Elimina restos de las sesiones de la instancia actual.
     */
    private void reset() {
        this.certb64 = null;
        this.remoteId = null;
        this.oldProxy = false;
    }

    /**
     * Devuelve true si se conecta con el proxy antiguo.
     */
    public boolean isOldProxy() {
        return oldProxy;
    }

    public ClaveLoginResult claveLoginRequest() throws ServerControlledException, IOException, SAXException {

        String xml = "<lgnrq />"; //$NON-NLS-1$

        String url = this.signFolderProxyUrl + createUrlParams(OPERATION_CLAVE_LOGIN_REQUEST, xml); //$NON-NLS-1$

        ConnectionResponse response = getRemoteData(url);

        InputStream is = response.getDataIs();
        if (!PfLog.isProduction) {
            byte[] data = readDataFromInpuStream(is);
            PfLog.i(SFConstants.LOG_TAG, "Respuesta de la peticion: " + new String(data));
            is = new ByteArrayInputStream(data);
        }

        Document doc = this.db.parse(is);

        final ClaveLoginResult result = ClaveLoginRequestResponseParser.parse(doc);

        result.setCookieId(response.getCookieId());

        PfLog.i(SFConstants.LOG_TAG, "Cookie Id envidada desde el servidor: " + response.getCookieId());

        // Almacenamos el identificador remoto y lo eliminamos del resultado
        this.remoteId = result.getTransactionId();
        result.setTransactionId(null);

        this.logged = true;

        return result;
    }

    public RequestResult loginRequest() throws OldProxyException, IOException, SAXException {

        // Limpiamos restos de sesiones anteriores
        reset();

        // --------------------------
        // Llamada al metodo de login
        // --------------------------
        String xml = "<lgnrq />"; //$NON-NLS-1$
        String url = this.signFolderProxyUrl + createUrlParams(OPERATION_LOGIN_REQUEST, xml); //$NON-NLS-1$

        Document doc;
        try {
            doc = getRemoteDocument(url);
        } catch (UnknownHostException e) {
            throw new OldProxyException("El proxy no soporta la nueva operacion de login", e);
        }

        RequestResult result = LoginTokenResponseParser.parse(doc);

        // Almacenamos el identificador remoto y lo eliminamos del resultado
        this.remoteId = result.getSsid();
        result.setSsid(null);

        this.logged = true;

        return result;
    }

    public ValidationLoginResult tokenValidation(final byte[] pkcs1, String cert) throws Exception {

        String xml = XmlRequestsFactory.createValidateLogin(cert, pkcs1);
        String url = this.signFolderProxyUrl + createUrlParams(OPERATION_LOGIN_VALIDATION, xml); //$NON-NLS-1$
        return LoginValidationResponseParser.parse(getRemoteDocument(url));
    }

    private String createUrlParams(final String op, final String data) {
        String url = PARAMETER_NAME_OPERATION_FIRST + op + PARAMETER_NAME_DATA +
                Base64.encode(data.getBytes(), true);

        if (this.remoteId != null) {
            url += PARAMETER_NAME_SHARED_SESSION_ID + remoteId;
        }
        return url;
    }

    public void logoutRequest() throws Exception {

        // --------------------------
        // Llamada al metodo de logout
        // --------------------------
        if (!oldProxy) {
            String xml = "<lgorq />"; //$NON-NLS-1$
            String url = this.signFolderProxyUrl + createUrlParams(OPERATION_LOGOUT_REQUEST, xml); //$NON-NLS-1$

            Document doc;
            try {
                doc = getRemoteDocument(url);
            } catch (UnknownHostException e) {
                throw new OldProxyException("El proxy no soporta la nueva operacion de login", e);
            }

            LogoutResponseParser.parse(doc);
        } else {
            CommManagerOldVersion.getInstance().logoutRequest();
        }

        this.logged = false;

        instance = null;
    }

    /**
     * Obtiene la peticiones de firma. Las peticiones devueltas deben cumplir
     * las siguientes condiciones:
     * <ul>
     * <li>Estar en el estado se&ntilde;alado (unresolved, signed o rejected).</li>
     * <li>Que todos los documentos que contiene se tengan que firmar con los
     * formatos de firma indicados (s&oacute;lo si se indica alguno)</li>
     * <li>Que las solicitudes cumplan con los filtros establecidos. Estos
     * filtros tendran la forma: key=value</li>
     * </ul>
     *
     * @param signRequestState Estado de las peticiones que se desean obtener.
     * @param filters          Listado de filtros que deben cumplir las peticiones
     *                         recuperadas. Los filtros soportados son:
     *                         <ul>
     *                         <li><b>orderAscDesc:</b> con valor "asc" para que sea orden
     *                         ascendente en la consulta, en cualquier otro caso ser&aacute;
     *                         descendente</li>
     *                         <li><b>initDateFilter:</b> fecha de inicio de las peticiones</li>
     *                         <li><b>endDateFilter:</b> fecha de fin de las peticiones</li>
     *                         <li><b>orderAttribute:</b> par&aacute;metro para ordenar por
     *                         una columna de la petici&oacute;n</li>
     *                         <li><b>searchFilter:</b> busca la cadena introducida en
     *                         cualquier texto de la petici&oacute;n (asunto, referencia,
     *                         etc)</li>
     *                         <li><b>labelFilter:</b> texto con el nombre de una etiqueta.
     *                         Filtra las peticiones en base a esa etiqueta, ej: "IMPORTANTE"
     *                         </li>
     *                         <li><b>applicationFilter:</b> texto con el identificador de
     *                         una aplicaci&oacute;n. Filtra las peticiones en base a la
     *                         aplicaci&oacute;n, ej: "SANCIONES"</li>
     *                         </ul>
     * @param numPage          N&uacute;mero de p&aacute;gina del listado.
     * @param pageSize         N&uacute;mero de peticiones por p&aacute;gina.
     * @return Lista de peticiones de firma
     * @throws SAXException Si el XML obtenido del servidor no puede analizarse
     * @throws IOException  Si ocurre un error de entrada / salida
     */
    public PartialSignRequestsList getSignRequests(
            final String signRequestState, final String[] filters,
            final int numPage, final int pageSize) throws ServerControlledException, SAXException,
            IOException {
        PartialSignRequestsList rsl;
        if (!oldProxy) {
            String[] signFormats = AppPreferences.getInstance().getSupportedFormats();
            String xml = XmlRequestsFactory.createRequestListRequest(
                    signRequestState, signFormats, filters, numPage, pageSize);
            String url = this.signFolderProxyUrl + createUrlParams(OPERATION_REQUEST, xml); //$NON-NLS-1$
            rsl = RequestListResponseParser.parse(getRemoteDocument(url));
        } else {
            return CommManagerOldVersion.getInstance().getSignRequests(this.certb64, signRequestState, filters, numPage, pageSize);
        }
        return rsl;
    }

    /**
     * Inicia la pre-firma remota de las peticiones.
     *
     * @param request Petici&oacute;n de firma.
     * @return Prefirmas de las peticiones enviadas.
     * @throws IOException                  Si ocurre algun error durante el tratamiento de datos.
     * @throws CertificateEncodingException Si no se puede obtener la codificaci&oacute;n del certificado.
     * @throws SAXException                 Si ocurren errores analizando el XML de respuesta.
     */
    public TriphaseRequest[] preSignRequests(final SignRequest request) throws IOException,
            CertificateException,
            SAXException {
        if (!oldProxy) {
            String xml = XmlRequestsFactory.createPresignRequest(request);
            String url = this.signFolderProxyUrl + createUrlParams(OPERATION_PRESIGN, xml);

            // Agregamos la cabecera Expect: 100-Continue para que el servidor no se queje de
            // peticiones grandes
            final Properties headers = new Properties();
            headers.setProperty("Expect", "100-Continue"); //$NON-NLS-1$ //$NON-NLS-2$

            return PresignsResponseParser.parse(getRemoteDocument(url, headers));
        } else {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(Base64.decode(this.certb64));
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(in);
            return CommManagerOldVersion.getInstance().preSignRequests(request, cert);
        }
    }


    /**
     * Inicia la post-firma remota de las peticiones.
     *
     * @param requests Peticiones a post-firmar
     * @return Listado con el resultado de la operaci&oacute;n de firma de cada
     * petici&oacute;n.
     * @throws IOException  Si ocurre algun error durante el proceso
     * @throws SAXException Si ocurren errores analizando el XML de respuesta
     */
    public RequestResult postSignRequests(final TriphaseRequest[] requests)
            throws IOException, SAXException {
        String xml = oldProxy ?
                XmlRequestsFactoryOldVersion.createPostsignRequest(requests, this.certb64) :
                XmlRequestsFactory.createPostsignRequest(requests);
        String url = this.signFolderProxyUrl + createUrlParams(OPERATION_POSTSIGN, xml);

        // Agregamos la cabecera Expect: 100-Continue para que el servidor no se queje de
        // peticiones grandes
        final Properties headers = new Properties();
        headers.setProperty("Expect", "100-Continue"); //$NON-NLS-1$ //$NON-NLS-2$

        return PostsignsResponseParser.parse(getRemoteDocument(url, headers));
    }

    /**
     * Obtiene los datos de un documento.
     *
     * @param requestId Identificador de la petici&oacute;n.
     * @param ownerId   DNI del usuario propietario de la petici&oacute;n.
     * @return Datos del documento.
     * @throws SAXException Cuando se encuentra un XML mal formado.
     * @throws IOException  Cuando existe alg&uacute;n problema en la lectura/escritura
     *                      de XML o al recuperar la respuesta del servidor.
     */
    public RequestDetail getRequestDetail(final String requestId, final String ownerId) throws SAXException, IOException {
        if (!oldProxy) {
            String xml = XmlRequestsFactory.createDetailRequest(requestId, ownerId);
            String url = this.signFolderProxyUrl + createUrlParams(OPERATION_DETAIL, xml);
            return RequestDetailResponseParser.parse(getRemoteDocument(url));
        } else {
            return CommManagerOldVersion.getInstance().getRequestDetail(this.certb64, requestId);
        }

    }

    /**
     * Obtiene el listado de aplicaciones para las que hay peticiones de firma.
     *
     * @param certB64 Certificado codificado en base64.
     * @return Configuracion de aplicaci&oacute;n.
     * @throws SAXException Cuando se encuentra un XML mal formado.
     * @throws IOException  Cuando existe alg&uacute;n problema en la lectura/escritura
     *                      de XML o al recuperar la respuesta del servidor.
     */
    public RequestAppConfiguration getApplicationList(final String certB64)
            throws SAXException, IOException {
        this.certb64 = certB64;

        // Preparamos la peticion. En caso de usarse el proxy nuevo, no se necesita el certificado
        String xml = XmlRequestsFactory.createAppListRequest(this.oldProxy ? certB64 : null);
        String url = this.signFolderProxyUrl + createUrlParams(OPERATION_APP_LIST, xml);

        return ApplicationListResponseParser.parse(getRemoteDocument(url));
    }

    /**
     * Rechaza las peticiones de firma indicadas.
     *
     * @param requestIds Identificadores de las peticiones de firma que se quieren
     *                   rechazar.
     * @return Resultado de la operacion para cada una de las peticiones de
     * firma.
     * @throws SAXException Si el XML obtenido del servidor no puede analizarse
     * @throws IOException  Si ocurre un error de entrada / salida
     */
    public RequestResult[] rejectRequests(final String[] requestIds,
                                          final String reason) throws SAXException, IOException {
        String xml = oldProxy ?
                XmlRequestsFactoryOldVersion.createRejectRequest(requestIds, this.certb64, reason) :
                XmlRequestsFactory.createRejectRequest(requestIds, reason);
        String url = this.signFolderProxyUrl + createUrlParams(OPERATION_REJECT, xml);
        return RejectsResponseParser.parse(getRemoteDocument(url));
    }

    /**
     * Obtiene la previsualizaci&oacute;n de un documento.
     *
     * @param documentId Identificador del documento.
     * @param filename   Nombre del fichero.
     * @param mimetype   MIME-Type del documento.
     * @return Datos del documento.
     * @throws IOException Cuando existe alg&uacute;n problema en la lectura/escritura
     *                     de XML o al recuperar la respuesta del servidor.
     */
    public DocumentData getPreviewDocument(final String documentId,
                                           final String filename, final String mimetype) throws IOException {

        if (!oldProxy) {
            return getPreview(OPERATION_PREVIEW_DOCUMENT, documentId, filename, mimetype);
        } else {
            return getPreview(OPERATION_PREVIEW_DOCUMENT, documentId, filename, mimetype, this.certb64);
        }
    }

    /**
     * Obtiene la previsualizaci&oacute;n de una firma.
     *
     * @param documentId Identificador del documento.
     * @param filename   Nombre del fichero.
     * @return Datos del documento.
     * @throws IOException Cuando existe alg&uacute;n problema en la lectura/escritura
     *                     de XML o al recuperar la respuesta del servidor.
     */
    public DocumentData getPreviewSign(final String documentId,
                                       final String filename) throws IOException {

        if (!oldProxy) {
            return getPreview(OPERATION_PREVIEW_SIGN, documentId,
                    filename, null);
        } else {
            return getPreview(OPERATION_PREVIEW_SIGN, documentId,
                    filename, null, this.certb64);
        }
    }

    /**
     * Obtiene la previsualizaci&oacute;n de un informe de firma.
     *
     * @param documentId Identificador del documento.
     * @param filename   Nombre del fichero.
     * @param mimetype   MIME-Type del documento.
     * @return Datos del documento.
     * @throws IOException Cuando existe alg&uacute;n problema en la lectura/escritura
     *                     de XML o al recuperar la respuesta del servidor.
     */
    public DocumentData getPreviewReport(final String documentId,
                                         final String filename, final String mimetype) throws IOException {

        if (!oldProxy) {
            return getPreview(OPERATION_PREVIEW_REPORT, documentId,
                    filename, mimetype);
        } else {
            return getPreview(OPERATION_PREVIEW_REPORT, documentId,
                    filename, mimetype, this.certb64);
        }
    }

    /**
     * Obtiene la previsualizaci&oacute;n de un documento.
     *
     * @param operation  Identificador del tipo de documento (datos, firma o informe).
     * @param documentId Identificador del documento.
     * @param filename   Nombre del fichero.
     * @param mimetype   MIME-Type del documento.
     * @return Datos del documento.
     * @throws IOException Cuando existe alg&uacute;n problema en la lectura/escritura
     *                     de XML o al recuperar la respuesta del servidor.
     */
    private DocumentData getPreview(final String operation,
                                    final String documentId, final String filename,
                                    final String mimetype) throws IOException {

        String xml = oldProxy ?
                XmlRequestsFactoryOldVersion.createPreviewRequest(documentId, this.certb64) :
                XmlRequestsFactory.createPreviewRequest(documentId);
        String url = this.signFolderProxyUrl + createUrlParams(operation, xml);

        final DocumentData docData = new DocumentData(documentId, filename, mimetype);
        docData.setDataIs(getRemoteDocumentIs(url));

        return docData;
    }

    /**
     * Aprueba peticiones de firma (les da el visto bueno).
     *
     * @param requestIds Identificador de las peticiones.
     * @return Resultado de la operaci&oacute;n.
     * @throws SAXException Cuando se encuentra un XML mal formado.
     * @throws IOException  Cuando existe alg&uacute;n problema en la lectura/escritura
     *                      de XML o al recuperar la respuesta del servidor.
     */
    public RequestResult[] approveRequests(final String[] requestIds) throws SAXException, IOException {
        String xml = oldProxy ?
                XmlRequestsFactoryOldVersion.createApproveRequest(requestIds, this.certb64) :
                XmlRequestsFactory.createApproveRequest(requestIds);
        String url = this.signFolderProxyUrl + createUrlParams(OPERATION_APPROVE, xml);
        return ApproveResponseParser.parse(getRemoteDocument(url));
    }

    public FireLoadDataResult firePrepareSigns(final SignRequest[] requests) throws IOException, SAXException {
        String xml = XmlRequestsFactory.createFireLoadDataRequest(requests);
        String url = this.signFolderProxyUrl + createUrlParams(OPERATION_PRESIGN_CLAVE_FIRMA, xml);
        return FireLoadDataResponseParser.parse(getRemoteDocument(url));
    }

    public FireSignResult fireSignRequests() throws IOException, SAXException {
        String xml = XmlRequestsFactory.createFireSignRequest();
        String url = this.signFolderProxyUrl + createUrlParams(OPERATION_POSTSIGN_CLAVE_FIRMA, xml);
        return FireSignResponseParser.parse(getRemoteDocument(url));
    }

    /**
     * Método que valida peticiones para su posterior firma.
     *
     * @param requestIds Conjunto de identificadores de peticiones a validar.
     * @return Resultado de la operación.
     */
    public RequestResult[] verifyRequests(final String[] requestIds) throws IOException, SAXException {
        String xml = oldProxy ?
                XmlRequestsFactoryOldVersion.createVerifyRequest(requestIds, this.certb64) :
                XmlRequestsFactory.createVerifyRequest(requestIds);
        String url = this.signFolderProxyUrl + createUrlParams(OPERATION_VERIFY, xml);
        return VerifyResponseParser.parse(getRemoteDocument(url));
    }

    /**
     * Da de alta en el sistema de notificaciones.
     *
     * @param token  Token de registro en GCM.
     * @param device Identificador de dispositivo.
     * @param dni    DNI del usuario.
     * @return Resultado del proceso de alta en el sistema de notificaciones.
     * Indica
     * @throws SAXException Cuando se encuentra un XML mal formado.
     * @throws IOException  Cuando existe alg&uacute;n problema en la lectura/escritura
     *                      de XML o al recuperar la respuesta del servidor.
     */
    public NotificationRegistryResult registerOnNotificationService(final String token, final String device, final String dni) throws SAXException, IOException {

        // El antiguo proxy no admite la operacion de registro en el sistema de notificaciones
        if (oldProxy) {
            return null;
        }

        PfLog.i(SFConstants.LOG_TAG, "Notificamos el registro en el sistema de notificaciones");

        // Realizamos la peticion
        String xml = XmlRequestsFactory.createRegisterNotificationRequest(token, device, dni);
        String url = this.signFolderProxyUrl + createUrlParams(OPERATION_NOTIFICATION_SERVICE_REGISTER, xml);
        return RegisterOnNotificationParser.parse(getRemoteDocument(url));
    }

    /**
     * Descarga un XML remoto de una URL dada.
     *
     * @param url URL de donde descargar el XML.
     * @return &Aacute;rbol XML descargado.
     * @throws IOException  Error en la lectura del documento.
     * @throws SAXException Error al parsear el XML.
     */
    private Document getRemoteDocument(final String url) throws SAXException, IOException {
        return getRemoteDocument(url, null);
    }

    /**
     * Descarga un XML remoto de una URL dada.
     *
     * @param url URL de donde descargar el XML.
     * @param headers Cabeceras que agregar a la petici&oacute;n.
     * @return &Aacute;rbol XML descargado.
     * @throws IOException  Error en la lectura del documento.
     * @throws SAXException Error al parsear el XML.
     */
    private Document getRemoteDocument(final String url, Properties headers) throws SAXException, IOException {

        InputStream is = getRemoteDocumentIs(url, headers);

        // En entornos de depuracion hacemos una precarga de los datos para mostrarlos en el log
        if (!PfLog.isProduction) {
            byte[] data = AOUtil.getDataFromInputStream(is);
            is.close();
            is = new ByteArrayInputStream(data);
            PfLog.i(SFConstants.LOG_TAG, "XML recibido: " + new String(data));
        }
        final Document doc = this.db.parse(is);
        is.close();

        return doc;
    }

    /**
     * Obtiene el flujo de entrada de los datos a descargar.
     *
     * @param url URL de donde descargar los datos.
     * @return Flujo de datos para la descarga.
     * @throws IOException Error en la lectura del documento.
     */
    public InputStream getRemoteDocumentIs(final String url) throws IOException {
        return getRemoteDocumentIs(url, null);
    }

    /**
     * Obtiene el flujo de entrada de los datos a descargar.
     *
     * @param url URL de donde descargar los datos.
     * @param headers Cabeceras que agregar a la petici&oacute;n.
     * @return Flujo de datos para la descarga.
     * @throws IOException Error en la lectura del documento.
     */
    public InputStream getRemoteDocumentIs(final String url, final Properties headers) throws IOException {
        final ConnectionResponse response = getRemoteData(url, headers);
        return response.getDataIs();
    }


    /**
     * Obtiene el flujo de entrada de los datos a descargar.
     *
     * @param url URL de donde descargar los datos.
     * @return Flujo de datos para la descarga.
     * @throws IOException Error en la lectura del documento.
     */
    private ConnectionResponse getRemoteData(final String url) throws IOException {
        return getRemoteData(url, null);
    }

    /**
     * Obtiene el flujo de entrada de los datos a descargar.
     *
     * @param url URL de donde descargar los datos.
     * @param headers Cabeceras que agregar a la petici&oacute;n.
     * @return Flujo de datos para la descarga.
     * @throws IOException Error en la lectura del documento.
     */
    private ConnectionResponse getRemoteData(final String url, final Properties headers) throws IOException {

        if (!PfLog.isProduction) {
            PfLog.d(SFConstants.LOG_TAG, "PETICION AL PROXY NUEVO");
            PfLog.i(SFConstants.LOG_TAG, "URL de peticion: " + url);
        }

        if (url.startsWith(HTTPS)) {
            try {
                AndroidUrlHttpManager.disableSslChecks();
            } catch (final Exception e) {
                PfLog.w(SFConstants.LOG_TAG,
                        "No se ha podido ajustar la confianza SSL, es posible que no se pueda completar la conexion: " + e //$NON-NLS-1$
                );
            }
        }

        final ConnectionResponse response = AndroidUrlHttpManager.getRemoteDataByPost(url, this.timeout, headers);

        if (url.startsWith(HTTPS)) {
            AndroidUrlHttpManager.enableSslChecks();
        }

        PfLog.d(SFConstants.LOG_TAG, "Se ha obtenido el flujo de entrada de los datos"); //$NON-NLS-1$

        return response;
    }

    /**
     * Verifica si la URL de proxy configurada es correcta.
     *
     * @return <code>true</code> si es correcta, <code>false</code> si no lo es.
     */
    public boolean verifyProxyUrl() {

        boolean correctUrl = true;
        if (this.signFolderProxyUrl == null || this.signFolderProxyUrl.trim().length() == 0) {
            correctUrl = false;
        } else {
            try {
                new URL(this.signFolderProxyUrl);
            } catch (final Exception e) {
                correctUrl = false;
            }
        }
        return correctUrl;
    }

    public void setNewProxy() {
        oldProxy = false;
    }

    public void setOldProxy() {
        oldProxy = true;
    }

    /**
     * Obtiene el listado de autorizaciones emitidas o recibidas por el usuario.
     * @return Lista de autorizaciones.
     * @throws IOException               Si algo falla en el proceso.
     * @throws SAXException              Si algo falla en el proceso.
     * @throws ServerControlledException Si hay algún error en la comunicación con el proxy.
     */
    public List<Authorization> getAuthorizations()
            throws IOException, SAXException, ServerControlledException {
        String xml = XmlRequestsFactory.createListUsersRequest();
        String url = this.signFolderProxyUrl + createUrlParams(OPERATION_LIST_AUTHORIZATIONS, xml);

        return ListAuthorizationsResponseParser.parse(getRemoteDocument(url));
    }

    /**
     * Guarda una autorización.
     * @param auth Autorización.
     * @return Resultado de la operacion.
     * @throws IOException               Si algo falla en el proceso.
     * @throws SAXException              Si algo falla en el proceso.
     */
    public GenericResponse saveAuthorization(Authorization auth)
            throws IOException, SAXException {

        String xml = XmlRequestsFactory.createSaveAuthorizationRequest(auth);
        String url = this.signFolderProxyUrl + createUrlParams(OPERATION_SAVE_AUTHORIZATION, xml);

        return GenericResponseParser.parse(getRemoteDocument(url));
    }


    /**
     * Da de alta un validador.
     * @param user Usuario validador.
     * @return Resultado de la operacion.
     * @throws IOException     Si algo falla en el proceso.
     * @throws SAXException    Si algo falla en el proceso.
     */
    public GenericResponse saveValidator(GenericUser user)
            throws IOException, SAXException {

        String xml = XmlRequestsFactory.createSaveValidatorRequest(user);
        String url = this.signFolderProxyUrl + createUrlParams(OPERATION_SAVE_VALIDATOR, xml);

        return GenericResponseParser.parse(getRemoteDocument(url));
    }

    /**
     * Aprueba una autorización.
     * @return Resultado de la operacion.
     * @throws IOException               Si algo falla en el proceso.
     * @throws SAXException              Si algo falla en el proceso.
     */
    public GenericResponse approveAuthorization(Authorization auth)
            throws IOException, SAXException {

        String xml = XmlRequestsFactory.createChangeStateAuthorizationRequest(auth);
        String url = this.signFolderProxyUrl + createUrlParams(OPERATION_ACCEPT_AUTHORIZATION, xml);

        return GenericResponseParser.parse(getRemoteDocument(url));
    }

    /**
     * Cancela o rechaza una autorización.
     * @return Resultado de la operacion.
     * @throws IOException               Si algo falla en el proceso.
     * @throws SAXException              Si algo falla en el proceso.
     */
    public GenericResponse revokeAuthorization(Authorization auth)
            throws IOException, SAXException {

        String xml = XmlRequestsFactory.createChangeStateAuthorizationRequest(auth);
        String url = this.signFolderProxyUrl + createUrlParams(OPERATION_REVOKE_AUTHORIZATION, xml);

        return GenericResponseParser.parse(getRemoteDocument(url));
    }


    public GenericResponse removeValidator(Validator validator) throws IOException, SAXException {

        String xml = XmlRequestsFactory.createRemoveValidatorRequest(validator);
        String url = this.signFolderProxyUrl + createUrlParams(OPERATION_REVOKE_VALIDATOR, xml);

        return GenericResponseParser.parse(getRemoteDocument(url));
    }

    /**
     * Obtiene el listado de validadores del usuario.
     * @return Lista de autorizaciones.
     * @throws IOException               Si algo falla en el proceso.
     * @throws SAXException              Si algo falla en el proceso.
     * @throws ServerControlledException Si hay algún error en la comunicación con el proxy.
     */
    public List<Validator> getValidators()
            throws IOException, SAXException, ServerControlledException {

        String xml = XmlRequestsFactory.createListUsersRequest();
        String url = this.signFolderProxyUrl + createUrlParams(OPERATION_LIST_VALIDATORS, xml);

        return ListValidatorsResponseParser.parse(getRemoteDocument(url));
    }

    /**
     * Método que obtiene la lista de usuarios con un determinado rol del portafirmas-proxy.
     * @param role     Rol a usar en el filtro.
     * @param text Texto de búsqueda.
     * @return una lista de usuarios del tipo AuthorizedUser (si el rol proporcionado es Authorized)
     * o VerifierUser (si el rol proporcionado es Verifier).
     * @throws IOException               Si algo falla en el proceso.
     * @throws SAXException              Si algo falla en el proceso.
     * @throws ServerControlledException Si hay algún error en la comunicación con el proxy.
     */
    public List<GenericUser> findUsers(final ConfigurationRole role, final String text)
            throws IOException, SAXException, ServerControlledException {
        String xml = XmlRequestsFactory.createFindUsersRequest(role, text);
        String url = this.signFolderProxyUrl + createUrlParams(OPERATION_FIND_USERS, xml);
        return FindUserResponseParser.parse(getRemoteDocument(url));
    }

    public UserConfig getUserConfig() throws IOException, SAXException, ServerControlledException {
        String xml = XmlRequestsFactory.createRequestGetUserConfig();
        String url = this.signFolderProxyUrl + createUrlParams(OPERATION_GET_USER_CONFIG, xml);
        return UserConfigurationResponseParser.parseUserConfigReq(getRemoteDocument(url));
    }

    public boolean isUserLogged() {
        return this.logged;
    }

    /**
     * Método que actualiza el estado de las notificaciones push.
     *
     * @param activePushNots Nuevo estado de las notificaciones.
     * @return la respuesta generada por portafirmas-web.
     * @throws IOException  Si algo falla en el proceso.
     * @throws SAXException Si algo falla en el proceso.
     */
    public boolean updatePushNotifications(boolean activePushNots) throws IOException, SAXException {

        PfLog.i(SFConstants.LOG_TAG, "Se van a activar las notificaciones: " + activePushNots);

        String xml = XmlRequestsFactory.createUpdatePushNotsRequest(activePushNots);
        String url = this.signFolderProxyUrl + createUrlParams(OPERATION_UPDATE_PUSH_NOTIFICATIONS, xml);
        return UpdatePushNotsResponseParser.parse(getRemoteDocument(url));
    }
}