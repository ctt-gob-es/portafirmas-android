package es.gob.afirma.android.signfolder.proxy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

import es.gob.afirma.android.network.AndroidUrlHttpManager;
import es.gob.afirma.android.network.ConnectionResponse;
import es.gob.afirma.android.signfolder.AppPreferences;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.util.AOUtil;
import es.gob.afirma.android.util.Base64;
import es.gob.afirma.core.misc.http.UrlHttpManager;
import es.gob.afirma.core.misc.http.UrlHttpManagerFactory;
import es.gob.afirma.core.misc.http.UrlHttpMethod;

/** Gestor de comunicaciones con el servidor de portafirmas m&oacute;vil.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class CommManager  extends CommManagerOldVersion{

	private static final String HTTPS = "https"; //$NON-NLS-1$

	private static final String PARAMETER_NAME_OPERATION = "op"; //$NON-NLS-1$
	private static final String PARAMETER_NAME_DATA = "dat"; //$NON-NLS-1$

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

	private static final String OPERATION_CLAVE_LOGIN_VALIDATION = "15"; //$NON-NLS-1$

	private static final String OPERATION_PRESIGN_CLAVE_FIRMA = "16"; //$NON-NLS-1$

	private static final String OPERATION_POSTSIGN_CLAVE_FIRMA = "17"; //$NON-NLS-1$

	private String certb64;

	private boolean oldProxy = false;

	private static CommManager instance = null;

	/** Obtiene una instancia de la clase.
	 * @return Gestor de comunicaciones con el Proxy. */
	public static CommManager getInstance() {
		if (instance == null) {
			instance = new CommManager();
		}
		return instance;
	}

	private CommManager() {
		super();
	}

	/** Reinicia la confifuraci&oacute;n del gestor para
	 * permitir que luego se inicie una nueva conexi&oacute; con el servicio proxy. */
	public static void resetConfig() {
		instance = null;
	}

	private static String prepareParam(final String param) {
		return Base64.encode(param.getBytes(), true);
	}

    /** Devuelve true si se conecta con el proxy antiguo. */
	public boolean isOldProxy() {
        return oldProxy;
    }

	private String prepareUrl(final String operation, final String dataB64UrlSafe) {
		return this.signFolderProxyUrl + "?"
				+ PARAMETER_NAME_OPERATION + "=" + operation + "&"
				+ PARAMETER_NAME_DATA + "=" + dataB64UrlSafe;
	}

	public FirePreSignResult firePreSignRequests(final SignRequest[] requests) throws IOException, SAXException {
		final String dataB64UrlSafe = prepareParam(
				XmlRequestsFactory.createClaveFirmaPreSignRequest(requests)
		);

		return PresignsClaveFirmaResponseParser.parse(getRemoteDocument(prepareUrl(
				OPERATION_PRESIGN_CLAVE_FIRMA, dataB64UrlSafe)));
	}

	public RequestResult[] firePostSignRequests(final FirePreSignResult firePreSignResult) throws IOException, SAXException {
		final String dataB64UrlSafe = prepareParam(
				XmlRequestsFactory.createClaveFirmaPostSignRequest(firePreSignResult)
		);

		return PostsignsClaveFirmaResponseParser.parse(getRemoteDocument(prepareUrl(
				OPERATION_POSTSIGN_CLAVE_FIRMA, dataB64UrlSafe)));
	}

	public ClaveLoginResult claveLoginRequest() throws Exception {

		String xml = "<lgnrq />"; //$NON-NLS-1$

		String url = this.signFolderProxyUrl + createUrlParams(OPERATION_CLAVE_LOGIN_REQUEST, xml); //$NON-NLS-1$

		ConnectionResponse response = getRemoteData(url);

        InputStream is = response.getDataIs();
		String xmlResponse = new String(AOUtil.getDataFromInputStream(is));
		is.close();

		Log.i(SFConstants.LOG_TAG," ===== Respuesta a la peticion de login a clave:\n" + xmlResponse); //$NON-NLS-1$

		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse(new InputSource(new StringReader(xmlResponse)));

        final ClaveLoginResult result = ClaveLoginRequestResponseParser.parse(doc);
        result.setSessionId(response.getCookieId());

        Log.i(SFConstants.LOG_TAG, "Session Id envidada desde el servidor: " + response.getCookieId());

		return result;
	}

	public ValidationLoginResult claveLoginValidation(String tokenSaml) throws Exception {
		final UrlHttpManager urlManager = UrlHttpManagerFactory.getInstalledManager();

		String xml = "<claveLoginValidation samltkn=\"" + tokenSaml + "\"/>"; //$NON-NLS-1$

		String url = this.signFolderProxyUrl + createUrlParams(OPERATION_CLAVE_LOGIN_VALIDATION, xml); //$NON-NLS-1$

		String xmlResponse = new String(urlManager.readUrl(url, UrlHttpMethod.POST));

		System.out.println("Respuesta a la verificacion de login con clave:\n" + xmlResponse); //$NON-NLS-1$

		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse(new InputSource(new StringReader(xmlResponse)));

		return ClaveLoginValidationResponseParser.parse(doc);
	}

	public RequestResult loginRequest() throws OldProxyException, IOException, SAXException {

		// --------------------------
		// Llamada al metodo de login
		// --------------------------
		String xml = "<lgnrq />"; //$NON-NLS-1$
		String url = this.signFolderProxyUrl + createUrlParams(OPERATION_LOGIN_REQUEST, xml); //$NON-NLS-1$

		Document doc;
		try {
			doc = getRemoteDocument(url);
		}
		catch (UnknownHostException e) {
			throw new OldProxyException("El proxy no soporta la nueva operacion de login");
		}

		return LoginTokenResponseParser.parse(doc);
	}

	public ValidationLoginResult tokenValidation(final byte[] pkcs1, String cert) throws Exception {

		// Enviamos el token firmado a validar
		String xml = "<rqtvl><cert>" + cert + "</cert><pkcs1>" + Base64.encode(pkcs1) + "</pkcs1></rqtvl>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String url = this.signFolderProxyUrl + createUrlParams(OPERATION_LOGIN_VALIDATION, xml); //$NON-NLS-1$
		Document doc = getRemoteDocument(url);

		return LoginValidationResponseParser.parse(doc);
	}

	private static String createUrlParams(final String op, final String data) {
		return "?op=" + op + "&dat=" + Base64.encode(data.getBytes(), true); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void logoutRequest() throws Exception {

		// --------------------------
		// Llamada al metodo de logout
		// --------------------------
		if(!oldProxy) {
			final UrlHttpManager urlManager = UrlHttpManagerFactory.getInstalledManager();

			String xml = "<lgorq />"; //$NON-NLS-1$

			String url = this.signFolderProxyUrl + createUrlParams(OPERATION_LOGOUT_REQUEST, xml); //$NON-NLS-1$

			byte[] data = urlManager.readUrl(url, UrlHttpMethod.POST);

			String xmlResponse = new String(data);

			System.out.println("Respuesta a la peticion de logout:\n" + xmlResponse); //$NON-NLS-1$

			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(new InputSource(new StringReader(xmlResponse)));

			LogoutResponseParser.parse(doc);
		}
		else {
			CommManagerOldVersion.getInstance().logoutRequest();
		}
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
	 * @param signRequestState Estado de las peticiones que se desean obtener.
	 * @param filters
	 *            Listado de filtros que deben cumplir las peticiones
	 *            recuperadas. Los filtros soportados son:
	 *            <ul>
	 *            <li><b>orderAscDesc:</b> con valor "asc" para que sea orden
	 *            ascendente en la consulta, en cualquier otro caso ser&aacute;
	 *            descendente</li>
	 *            <li><b>initDateFilter:</b> fecha de inicio de las peticiones</li>
	 *            <li><b>endDateFilter:</b> fecha de fin de las peticiones</li>
	 *            <li><b>orderAttribute:</b> par&aacute;metro para ordenar por
	 *            una columna de la petici&oacute;n</li>
	 *            <li><b>searchFilter:</b> busca la cadena introducida en
	 *            cualquier texto de la petici&oacute;n (asunto, referencia,
	 *            etc)</li>
	 *            <li><b>labelFilter:</b> texto con el nombre de una etiqueta.
	 *            Filtra las peticiones en base a esa etiqueta, ej: "IMPORTANTE"
	 *            </li>
	 *            <li><b>applicationFilter:</b> texto con el identificador de
	 *            una aplicaci&oacute;n. Filtra las peticiones en base a la
	 *            aplicaci&oacute;n, ej: "SANCIONES"</li>
	 *            </ul>
	 * @param numPage N&uacute;mero de p&aacute;gina del listado.
	 * @param pageSize N&uacute;mero de peticiones por p&aacute;gina.
	 * @return Lista de peticiones de firma
	 * @throws SAXException
	 *             Si el XML obtenido del servidor no puede analizarse
	 * @throws IOException
	 *             Si ocurre un error de entrada / salida
	 */
	public PartialSignRequestsList getSignRequests(
			final String signRequestState, final String[] filters,
			final int numPage, final int pageSize) throws SAXException,
			IOException {
		PartialSignRequestsList rsl;
		if (!oldProxy) {
			final String dataB64UrlSafe = prepareParam(XmlRequestsFactory
					.createRequestListRequest(signRequestState,
							AppPreferences.getInstance().getSupportedFormats(), filters, numPage,
							pageSize));

			rsl = RequestListResponseParser.parse(getRemoteDocument(prepareUrl(
					OPERATION_REQUEST, dataB64UrlSafe)));
		}
		else {
			return CommManagerOldVersion.getInstance().getSignRequests(this.certb64, signRequestState, filters, numPage, pageSize);
		}
		return rsl;
	}

	/** Inicia la pre-firma remota de las peticiones.
	 * @param request Petici&oacute;n de firma.
	 * @return Prefirmas de las peticiones enviadas.
	 * @throws IOException Si ocurre algun error durante el tratamiento de datos.
	 * @throws CertificateEncodingException Si no se puede obtener la codificaci&oacute;n del certificado.
	 * @throws SAXException Si ocurren errores analizando el XML de respuesta. */
	public TriphaseRequest[] preSignRequests(final SignRequest request) throws IOException,
			CertificateException,
			SAXException {
		if(!oldProxy) {
			final String dataB64UrlSafe = prepareParam(
					XmlRequestsFactory.createPresignRequest(request)
			);

			return PresignsResponseParser.parse(getRemoteDocument(prepareUrl(
					OPERATION_PRESIGN, dataB64UrlSafe)));
		}
		else {
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			InputStream in = new ByteArrayInputStream(Base64.decode(this.certb64));
			X509Certificate cert = (X509Certificate) certFactory.generateCertificate(in);
			return CommManagerOldVersion.getInstance().preSignRequests(request, cert);
		}
	}

	/**
	 * Inicia la post-firma remota de las peticiones.
	 *
	 * @param requests
	 *            Peticiones a post-firmar
	 * @return Listado con el resultado de la operaci&oacute;n de firma de cada
	 *         petici&oacute;n.
	 * @throws IOException
	 *             Si ocurre algun error durante el proceso
	 * @throws SAXException
	 *             Si ocurren errores analizando el XML de respuesta
	 */
	public RequestResult postSignRequests(final TriphaseRequest[] requests) throws IOException,
			SAXException {
		final String dataB64UrlSafe;
		if(!oldProxy) {
			dataB64UrlSafe = prepareParam(XmlRequestsFactory
					.createPostsignRequest(requests));
		}
		else {
			dataB64UrlSafe = prepareParam(XmlRequestsFactoryOldVersion
					.createPostsignRequest(requests, this.certb64));
		}

		return PostsignsResponseParser.parse(getRemoteDocument(prepareUrl(
				OPERATION_POSTSIGN, dataB64UrlSafe)));
	}

	/**
	 * Obtiene los datos de un documento.
	 *
	 * @param requestId
	 *            Identificador de la petici&oacute;n.
	 * @return Datos del documento.
	 * @throws SAXException
	 *             Cuando se encuentra un XML mal formado.
	 * @throws IOException
	 *             Cuando existe alg&uacute;n problema en la lectura/escritura
	 *             de XML o al recuperar la respuesta del servidor.
	 */
	public RequestDetail getRequestDetail(final String requestId) throws SAXException, IOException {
		String dataB64UrlSafe;
		if(!oldProxy) {
			dataB64UrlSafe = prepareParam(XmlRequestsFactory
					.createDetailRequest(requestId));
			return RequestDetailResponseParser.parse(getRemoteDocument(prepareUrl(
					OPERATION_DETAIL, dataB64UrlSafe)));
		}
		else {
			return CommManagerOldVersion.getInstance().getRequestDetail(this.certb64, requestId);
		}

	}

	/**
	 * Obtiene el listado de aplicaciones para las que hay peticiones de firma.
	 *
	 * @param certB64
	 *            Certificado codificado en base64.
	 * @return Configuracion de aplicaci&oacute;n.
	 * @throws SAXException
	 *             Cuando se encuentra un XML mal formado.
	 * @throws IOException
	 *             Cuando existe alg&uacute;n problema en la lectura/escritura
	 *             de XML o al recuperar la respuesta del servidor.
	 */
	public RequestAppConfiguration getApplicationList(final String certB64)
			throws SAXException, IOException {
		this.certb64 = certB64;

		// Preparamos a peticion. En caso de usarse el proxy nuevo, no se necesita el certificado
		final String dataB64UrlSafe = prepareParam(
				XmlRequestsFactory.createAppListRequest(this.oldProxy ? certB64 : null));

		return ApplicationListResponseParser.parse(
				getRemoteDocument(prepareUrl(OPERATION_APP_LIST, dataB64UrlSafe)));
	}

	/**
	 * Rechaza las peticiones de firma indicadas.
	 *
	 * @param requestIds
	 *            Identificadores de las peticiones de firma que se quieren
	 *            rechazar.
	 * @return Resultado de la operacion para cada una de las peticiones de
	 *         firma.
	 * @throws SAXException
	 *             Si el XML obtenido del servidor no puede analizarse
	 * @throws IOException
	 *             Si ocurre un error de entrada / salida
	 */
	public RequestResult[] rejectRequests(final String[] requestIds,
			final String reason) throws SAXException, IOException {
		final String dataB64UrlSafe;
		if(!oldProxy) {
			dataB64UrlSafe = prepareParam(XmlRequestsFactory
					.createRejectRequest(requestIds, reason));
		}
		else {
			dataB64UrlSafe = prepareParam(XmlRequestsFactoryOldVersion
					.createRejectRequest(requestIds, this.certb64, reason));
		}

		return RejectsResponseParser.parse(getRemoteDocument(prepareUrl(
				OPERATION_REJECT, dataB64UrlSafe)));
	}

	/** Obtiene la previsualizaci&oacute;n de un documento.
	 * @param documentId Identificador del documento.
	 * @param filename Nombre del fichero.
	 * @param mimetype MIME-Type del documento.
	 * @return Datos del documento.
	 * @throws IOException Cuando existe alg&uacute;n problema en la lectura/escritura
	 *                     de XML o al recuperar la respuesta del servidor. */
	public DocumentData getPreviewDocument(final String documentId,
			final String filename, final String mimetype) throws IOException {

		if(!oldProxy) {
			return getPreview(OPERATION_PREVIEW_DOCUMENT, documentId, filename, mimetype);
		}
		else {
			return getPreview(OPERATION_PREVIEW_DOCUMENT, documentId, filename, mimetype, this.certb64);
		}
	}

	/** Obtiene la previsualizaci&oacute;n de una firma.
	 * @param documentId Identificador del documento.
	 * @param filename Nombre del fichero.
	 * @return Datos del documento.
	 * @throws IOException Cuando existe alg&uacute;n problema en la lectura/escritura
	 *                     de XML o al recuperar la respuesta del servidor. */
	public DocumentData getPreviewSign(final String documentId,
			final String filename) throws IOException {

		if(!oldProxy) {
			return getPreview(OPERATION_PREVIEW_SIGN, documentId,
					filename, null);
		}
		else {
			return getPreview(OPERATION_PREVIEW_SIGN, documentId,
					filename, null, this.certb64);
		}
	}

	/** Obtiene la previsualizaci&oacute;n de un informe de firma.
	 * @param documentId Identificador del documento.
	 * @param filename Nombre del fichero.
	 * @param mimetype MIME-Type del documento.
	 * @return Datos del documento.
	 * @throws IOException Cuando existe alg&uacute;n problema en la lectura/escritura
	 *                     de XML o al recuperar la respuesta del servidor. */
	public DocumentData getPreviewReport(final String documentId,
			final String filename, final String mimetype) throws IOException {

		if(!oldProxy) {
			return getPreview(OPERATION_PREVIEW_REPORT, documentId,
					filename, mimetype);
		}
		else {
			return getPreview(OPERATION_PREVIEW_REPORT, documentId,
					filename, mimetype, this.certb64);
		}
	}

	/** Obtiene la previsualizaci&oacute;n de un documento.
	 * @param operation Identificador del tipo de documento (datos, firma o informe).
	 * @param documentId Identificador del documento.
	 * @param filename Nombre del fichero.
	 * @param mimetype MIME-Type del documento.
	 * @return Datos del documento.
	 * @throws IOException Cuando existe alg&uacute;n problema en la lectura/escritura
	 *                     de XML o al recuperar la respuesta del servidor. */
	private DocumentData getPreview(final String operation,
			final String documentId, final String filename,
			final String mimetype) throws IOException {
		final String dataB64UrlSafe;
		if (!oldProxy) {
			dataB64UrlSafe = prepareParam(XmlRequestsFactory
					.createPreviewRequest(documentId));
		}
		else {
			dataB64UrlSafe = prepareParam(XmlRequestsFactoryOldVersion
					.createPreviewRequest(documentId, this.certb64));
		}

		final DocumentData docData = new DocumentData(documentId, filename, mimetype);
		docData.setDataIs(getRemoteDocumentIs(prepareUrl(operation, dataB64UrlSafe)));

		return docData;
	}

	/**
	 * Aprueba peticiones de firma (les da el visto bueno).
	 *
	 * @param requestIds
	 *            Identificador de las peticiones.
	 * @return Resultado de la operaci&oacute;n.
	 * @throws SAXException
	 *             Cuando se encuentra un XML mal formado.
	 * @throws IOException
	 *             Cuando existe alg&uacute;n problema en la lectura/escritura
	 *             de XML o al recuperar la respuesta del servidor.
	 */
	public RequestResult[] approveRequests(final String[] requestIds) throws SAXException, IOException {

		final String dataB64UrlSafe;
		if(!oldProxy) {
			dataB64UrlSafe = prepareParam(XmlRequestsFactory
					.createApproveRequest(requestIds));
		}
		else {
			dataB64UrlSafe = prepareParam(XmlRequestsFactoryOldVersion
					.createApproveRequest(requestIds, this.certb64));
		}

		return ApproveResponseParser.parse(getRemoteDocument(prepareUrl(
				OPERATION_APPROVE, dataB64UrlSafe)));
	}

	/**
	 * Da de alta en el sistema de notificaciones.
	 *
	 * @param token
	 * 			Token de registro en GCM.
	 * @param device
	 * 			Identificador de dispositivo.
	 * @param certB64
	 * 			Certificado en base 64 del usuario.
	 * @return Resultado del proceso de alta en el sistema de notificaciones.
	 * 			Indica
	 * @throws SAXException
	 *             Cuando se encuentra un XML mal formado.
	 * @throws IOException
	 *             Cuando existe alg&uacute;n problema en la lectura/escritura
	 *             de XML o al recuperar la respuesta del servidor.
	 */
	public NotificationRegistryResult registerOnNotificationService(final String token, final String device, final String certB64) throws SAXException, IOException {

		// El antiguo proxy no admite la operacion de registro en el sistema de notificaciones
		if (oldProxy) {
			return null;
		}

		// Componentemos el XML con la peticion
		final String dataB64UrlSafe = prepareParam(XmlRequestsFactory
					.createRegisterNotificationRequest(token, device, certB64));

		// Realizamos la peticion
		return RegisterOnNotificationParser.parse(
				getRemoteDocument(prepareUrl(OPERATION_NOTIFICATION_SERVICE_REGISTER, dataB64UrlSafe)));
	}

	/**
	 * Descarga un XML remoto de una URL dada.
	 *
	 * @param url
	 *            URL de donde descargar el XML.
	 * @return &Aacute;rbol XML descargado.
	 * @throws IOException
	 *             Error en la lectura del documento.
	 * @throws SAXException
	 *             Error al parsear el XML.
	 */
	private Document getRemoteDocument(final String url) throws SAXException, IOException {

		final InputStream is = getRemoteDocumentIs(url);
		byte[] data = AOUtil.getDataFromInputStream(is);
		is.close();

		Log.w(SFConstants.LOG_TAG, "XML recibido: " + new String(data));

		return this.db.parse(new ByteArrayInputStream(data));
	}


	/**
	 * Obtiene el flujo de entrada de los datos a descargar.
	 * @param url URL de donde descargar los datos.
	 * @return Flujo de datos para la descarga.
	 * @throws IOException
	 *             Error en la lectura del documento.
	 */
	public InputStream getRemoteDocumentIs(final String url) throws IOException {
		final ConnectionResponse response = getRemoteData(url);
		return response.getDataIs();
	}

    /**
     * Obtiene el flujo de entrada de los datos a descargar.
     * @param url URL de donde descargar los datos.
     * @return Flujo de datos para la descarga.
     * @throws IOException
     *             Error en la lectura del documento.
     */
    private ConnectionResponse getRemoteData(final String url) throws IOException {

        if (url.startsWith(HTTPS)) {
            try {
                AndroidUrlHttpManager.disableSslChecks();
            }
            catch(final Exception e) {
                Log.w(SFConstants.LOG_TAG,
                        "No se ha podido ajustar la confianza SSL, es posible que no se pueda completar la conexion: " + e //$NON-NLS-1$
                );
            }
        }

        final ConnectionResponse response = AndroidUrlHttpManager.getRemoteDataByPost(url, this.timeout);

        if (url.startsWith(HTTPS)) {
            AndroidUrlHttpManager.enableSslChecks();
        }

        Log.d(SFConstants.LOG_TAG, "Se ha obtenido el flujo de entrada de los datos"); //$NON-NLS-1$

        return response;
    }

	/** Verifica si la URL de proxy configurada es correcta.
	 * @return <code>true</code> si es correcta, <code>false</code> si no lo es. */
	public boolean verifyProxyUrl() {

		boolean correctUrl = true;
		if (this.signFolderProxyUrl == null || this.signFolderProxyUrl.trim().length() == 0) {
			correctUrl = false;
		}
		else {
			try {
				new URL(this.signFolderProxyUrl);
			}
			catch (final Exception e) {
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


	private static void printText(String text) {

		if (text == null || text.isEmpty()) {
			return;
		}

		Log.i(SFConstants.LOG_TAG, "===========");
		if (text.length() <= 4000) {
			Log.i(SFConstants.LOG_TAG, text);
		}
		else {
			int idx = 0;
			while (text.length() - idx > 4000) {
				Log.i(SFConstants.LOG_TAG, text.substring(idx, idx + 4000));
				idx += 4000;
			}
			Log.i(SFConstants.LOG_TAG, text.substring(idx));
		}
		Log.i(SFConstants.LOG_TAG, "===========");
	}
}