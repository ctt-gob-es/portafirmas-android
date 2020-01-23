package es.gob.afirma.android.signfolder.proxy;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import es.gob.afirma.android.network.AndroidUrlHttpManager;
import es.gob.afirma.android.network.ConnectionResponse;
import es.gob.afirma.android.signfolder.AppPreferences;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.util.Base64;
import es.gob.afirma.android.util.PfLog;

/** Gestor de comunicaciones con el servidor de portafirmas m&oacute;vil.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public class CommManagerOldVersion {

	private static final String HTTPS = "https"; //$NON-NLS-1$

	private static final String PARAMETER_NAME_OPERATION = "op"; //$NON-NLS-1$
	private static final String PARAMETER_NAME_DATA = "dat"; //$NON-NLS-1$

	private static final String OPERATION_PRESIGN = "0"; //$NON-NLS-1$
	private static final String OPERATION_REQUEST = "2"; //$NON-NLS-1$
	private static final String OPERATION_DETAIL = "4"; //$NON-NLS-1$
	private static final String OPERATION_APP_LIST = "6"; //$NON-NLS-1$

	DocumentBuilder db;

	final int timeout;

	String signFolderProxyUrl;

	private static CommManagerOldVersion instance = null;

	/** Obtiene una instancia de la clase.
	 * @return Gestor de comunicaciones con el Proxy. */
	public static CommManagerOldVersion getInstance() {
		if (instance == null) {
			instance = new CommManagerOldVersion();
		}
		return instance;
	}

	CommManagerOldVersion() {
		this.signFolderProxyUrl = AppPreferences.getInstance().getSelectedProxyUrl();
		this.timeout = AppPreferences.getInstance().getConnectionReadTimeout();

		try {
			this.db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (final ParserConfigurationException e) {
			PfLog.e(SFConstants.LOG_TAG,
					"No se ha podido cargar un manejador de XML: " + e.toString()); //$NON-NLS-1$
			e.printStackTrace();
			this.db = null;
		}
	}

	public void setProxyURL(String url) {
		this.signFolderProxyUrl = url;
	}

	private static String prepareParam(final String param) {
		return Base64.encode(param.getBytes(), true);
	}

	private String prepareUrl(final String operation, final String dataB64UrlSafe) {
		return this.signFolderProxyUrl + "?"
			+ PARAMETER_NAME_OPERATION + "=" + operation + "&"
			+ PARAMETER_NAME_DATA + "=" + dataB64UrlSafe;
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
	 * @param certEncodedB64 Certificado codificado en Base64.
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
	 * @param numPage N&uacute;mero de p&aacute;gina del listado que se debe mostrar.
	 * @param pageSize N&uacute;mero de peticiones que se incluyen en cada p&aacute;gina.
	 * @return Lista de peticiones de firma
	 * @throws SAXException
	 *             Si el XML obtenido del servidor no puede analizarse
	 * @throws IOException
	 *             Si ocurre un error de entrada / salida
	 */
	PartialSignRequestsList getSignRequests(final String certEncodedB64,
												   final String signRequestState, final String[] filters,
												   final int numPage, final int pageSize) throws SAXException,
			IOException, ServerControlledException {

		final String dataB64UrlSafe = prepareParam(XmlRequestsFactoryOldVersion
				.createRequestListRequest(certEncodedB64, signRequestState,
						AppPreferences.getInstance().getSupportedFormats(), filters, numPage,
						pageSize));

		return RequestListResponseParser.parse(getRemoteDocument(prepareUrl(
				OPERATION_REQUEST, dataB64UrlSafe)));
	}

	/** Inicia la pre-firma remota de las peticiones.
	 * @param request Petici&oacute;n de firma.
	 * @param cert Certificado del firmante.
	 * @return Prefirmas de las peticiones enviadas.
	 * @throws IOException Si ocurre algun error durante el tratamiento de datos.
	 * @throws CertificateEncodingException Si no se puede obtener la codificaci&oacute;n del certificado.
	 * @throws SAXException Si ocurren errores analizando el XML de respuesta. */
	TriphaseRequest[] preSignRequests(final SignRequest request,
											  final X509Certificate cert)
			throws IOException, CertificateEncodingException, SAXException {

		final String dataB64UrlSafe = prepareParam(
				XmlRequestsFactoryOldVersion.createPresignRequest(request, Base64.encode(cert.getEncoded()))
		);

		return PresignsResponseParser.parse(getRemoteDocument(prepareUrl(
				OPERATION_PRESIGN, dataB64UrlSafe)));
	}

	/**
	 * Obtiene los datos de un documento.
	 *
	 * @param certB64
	 *            Certificado codificado en base64.
	 * @param requestId
	 *            Identificador de la petici&oacute;n.
	 * @return Datos del documento.
	 * @throws SAXException
	 *             Cuando se encuentra un XML mal formado.
	 * @throws IOException
	 *             Cuando existe alg&uacute;n problema en la lectura/escritura
	 *             de XML o al recuperar la respuesta del servidor.
	 */
	RequestDetail getRequestDetail(final String certB64,
										  final String requestId) throws SAXException, IOException {

		final String dataB64UrlSafe = prepareParam(XmlRequestsFactoryOldVersion
				.createDetailRequest(certB64, requestId));

		return RequestDetailResponseParser.parse(getRemoteDocument(prepareUrl(
				OPERATION_DETAIL, dataB64UrlSafe)));
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

		final String dataB64UrlSafe = prepareParam(
				XmlRequestsFactoryOldVersion.createAppListRequest(certB64));

		return ApplicationListResponseParser.parse(
				getRemoteDocument(prepareUrl(OPERATION_APP_LIST, dataB64UrlSafe)));
	}

	/** Obtiene la previsualizaci&oacute;n de un documento.
	 * @param operation Identificador del tipo de documento (datos, firma o informe).
	 * @param documentId Identificador del documento.
	 * @param filename Nombre del fichero.
	 * @param mimetype MIME-Type del documento.
	 * @param certB64 Certificado codificado en base64.
	 * @return Datos del documento.
	 * @throws IOException Cuando existe alg&uacute;n problema en la lectura/escritura
	 *                     de XML o al recuperar la respuesta del servidor. */
	DocumentData getPreview(final String operation,
								   final String documentId, final String filename,
								   final String mimetype, final String certB64) throws IOException {

		final String dataB64UrlSafe = prepareParam(XmlRequestsFactoryOldVersion
				.createPreviewRequest(documentId, certB64));

		final DocumentData docData = new DocumentData(documentId, filename, mimetype);
		docData.setDataIs(getRemoteDocumentIs(prepareUrl(operation, dataB64UrlSafe)));

		return docData;
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

		if (url.startsWith(HTTPS)) {
			try {
				AndroidUrlHttpManager.disableSslChecks();
			}
			catch(final Exception e) {
				PfLog.w(SFConstants.LOG_TAG,
						"No se ha podido ajustar la confianza SSL, es posible que no se pueda completar la conexion: " + e //$NON-NLS-1$
				);
			}
		}

		final ConnectionResponse response = AndroidUrlHttpManager.getRemoteDataByPost(url, this.timeout);
		final InputStream is = response.getDataIs();
		final Document doc = this.db.parse(is);
		is.close();

		if (url.startsWith(HTTPS)) {
			AndroidUrlHttpManager.enableSslChecks();
		}
		return doc;
	}


	/**
	 * Obtiene el flujo de entrada de los datos a descargar.
	 * @param url URL de donde descargar los datos.
	 * @return Flujo de datos para la descarga.
	 * @throws IOException
	 *             Error en la lectura del documento.
	 */
	private InputStream getRemoteDocumentIs(final String url) throws IOException {

		if (url.startsWith(HTTPS)) {
			try {
				AndroidUrlHttpManager.disableSslChecks();
			}
			catch(final Exception e) {
				PfLog.w(SFConstants.LOG_TAG,
						"No se ha podido ajustar la confianza SSL, es posible que no se pueda completar la conexion: " + e //$NON-NLS-1$
				);
			}
		}

		final ConnectionResponse response = AndroidUrlHttpManager.getRemoteDataByPost(url, this.timeout);
		final InputStream is = response.getDataIs();

		if (url.startsWith(HTTPS)) {
			AndroidUrlHttpManager.enableSslChecks();
		}

		PfLog.d(SFConstants.LOG_TAG, "Se ha obtenido el flujo de entrada de los datos"); //$NON-NLS-1$

		return is;
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

	public void logoutRequest() throws Exception {
		instance = null;
	}
}