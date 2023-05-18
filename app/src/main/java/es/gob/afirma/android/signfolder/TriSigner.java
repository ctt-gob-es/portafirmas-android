package es.gob.afirma.android.signfolder;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

import es.gob.afirma.android.crypto.AOPkcs1Signer;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.RequestResult;
import es.gob.afirma.android.signfolder.proxy.SignRequest;
import es.gob.afirma.android.signfolder.proxy.SignRequestDocument;
import es.gob.afirma.android.signfolder.proxy.SignaturePermission;
import es.gob.afirma.android.signfolder.proxy.TriphaseRequest;
import es.gob.afirma.android.signfolder.proxy.TriphaseSignDocumentRequest;
import es.gob.afirma.android.signfolder.proxy.TriphaseSignDocumentRequest.TriphaseConfigData;
import es.gob.afirma.android.util.AOException;
import es.gob.afirma.android.util.PfLog;

/** Firmador trif&aacute;sico.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s */
public final class TriSigner {

	/** Firma de forma trif&aacute;sica una petici&oacute;n de firma.
	 * @param request Petici&oacute;n de firma.
	 * @param pk Clave privada para al firma.
	 * @param certificateChain Cadena de certificaci&oacute;n del certificado de firma.
	 * @param commMgr Gestor de las llamadas a servicios remotos.
	 * @return Resultado de la operaci&oacute;n.
	 * @throws CertificateEncodingException Cuando hay un error en la codificaci&oacute;n del certificado.
	 * @throws IOException Cuando hay un error de lectura/escritura de datos.
	 * @throws SAXException 0Cuando el XML est&aacute; mal formado. */
	public static RequestResult sign(final SignRequest request, final PrivateKey pk,
			final X509Certificate[] certificateChain, final CommManager commMgr)
			throws CertificateException, IOException, SAXException {

		// *****************************************************************************************************
		// **************************** PREFIRMA ***************************************************************
		//******************************************************************************************************

		PfLog.i(SFConstants.LOG_TAG, "TriSigner - sign: == PREFIRMA =="); //$NON-NLS-1$

		// Mandamos a prefirmar y obtenemos los resultados
		final TriphaseRequest[] triphaseRequests = signPhase1(request, commMgr);

		// Comprobamos que todas hayan terminado correctamente o esten pendientes de
		// confirmacion
		Set<SignaturePermission> permissionsNeeded = new HashSet<>();
		for (TriphaseRequest triphaseRequest : triphaseRequests) {
			if (!triphaseRequest.isStatusOk()) {
				PfLog.w(SFConstants.LOG_TAG, "Se encontro prefirma erronea, se aborta el proceso de firma. La traza de la excepcion es: " + triphaseRequest.getException()); //$NON-NLS-1$
				return new RequestResult(request.getId(), false);
			}
			// Si para firmar todos los documentos se necesita alguna confirmacion, se agrega
			// al conjunto de permisos para despues notificarlos todos
			if (triphaseRequest.isNeedConfirmation()) {

				// Si la peticion requiere confirmaciones comprobamos que documentos exactamente
				// son los que las requieren y establecemos que estos requirieron confirmacion
				// y pueden no ser validos, con lo cual se debe omitir la validacion de la firma
				// generada.
				//TODO: Hacemos esto mutando la peticion de entrada, lo que deberia ser incorrecto
				for (TriphaseSignDocumentRequest docRequest : triphaseRequest.getDocumentsRequests()) {
					if (docRequest.isNeedConfirmation()) {
						for (SignRequestDocument doc : request.getDocs()) {
							if (doc.getId().equals(docRequest.getId())) {
								doc.setNeedConfirmation(true);
							}
						}
					}
				}

				permissionsNeeded.addAll(triphaseRequest.getPermissions());
			}
//			for (TriphaseSignDocumentRequest docReq : triphaseRequest.getDocumentsRequests()) {
//				if (docReq.isNeedConfirmation()) {
//					for (String requestorText : docReq.getConfirmationRequestorTexts().split(";")) {
//						SignaturePermission permission = SignaturePermission.parse(requestorText);
//						if (permission == null) {
//							PfLog.w(SFConstants.LOG_TAG, "El servicio de firma requirio un permiso no reconocido. Se cancelara la firma de la peticion: " + triphaseRequest.getConfirmationCode()); //$NON-NLS-1$
//							return new RequestResult(request.getId(), false);
//						}
//						permissionsNeeded.add(permission);
//					}
//				}
//			}
		}

		// Si son necesario permisos, devolvemos un resultado parcial pendiente de la confirmacion
		if (!permissionsNeeded.isEmpty()) {
			return new RequestResult(request.getId(), true, null, permissionsNeeded);
		}

		// *****************************************************************************************************
		// ******************************* FIRMA ***************************************************************
		//******************************************************************************************************

		PfLog.i(SFConstants.LOG_TAG, "TriSigner - sign: == FIRMA =="); //$NON-NLS-1$

		// Recorremos los documentos de la peticion de firma
		for (TriphaseRequest triphaseRequest : triphaseRequests) {

			// Recorremos cada una de las firmas requeridas por el documento
			for (final TriphaseSignDocumentRequest docRequests : triphaseRequest.getDocumentsRequests()) {

				// Firmamos las prefirmas y actualizamos los parciales de cada documento de cada peticion
				try {
					signPhase2(docRequests, pk, certificateChain);
				}
				catch(final Exception e) {
					PfLog.w(SFConstants.LOG_TAG, "Error en la fase de FIRMA", e); //$NON-NLS-1$

					// Si un documento falla en firma toda la peticion se da por fallida
					return new RequestResult(request.getId(), false);
				}

			} // Firmas del documento
		} // Documentos de peticion

		// *****************************************************************************************************
		// **************************** POSTFIRMA **************************************************************
		//******************************************************************************************************

		PfLog.i(SFConstants.LOG_TAG, "TriSigner - sign: == POSTFIRMA =="); //$NON-NLS-1$


		// Comprobamos si entre los documentos originales habia alguno que requiriese
		// confirmacion del usuario, en cuyo caso, actualizamos la informacion aqui
		// para que este en la postfirma
		for (SignRequestDocument originalDoc : request.getDocs()) {
			if (originalDoc.isNeedConfirmation()) {
				for (TriphaseRequest triphaseRequest : triphaseRequests) {
					for (final TriphaseSignDocumentRequest docRequest : triphaseRequest.getDocumentsRequests()) {
						if (docRequest.getId().equals(originalDoc.getId())) {
							docRequest.setNeedConfirmation(true);
						}
					}
				}
			}
		}

		return signPhase3(triphaseRequests, commMgr);
	}

	/**
	 * Obtiene el nombre de un algoritmo de firma que usa un algoritmo de huella espec&iacute;fico.
	 * @param mdAlgorithm Algoritmo de huella digital.
	 * @return Algoritmo de firma que utiliza el algoritmo de huella digital indicado.
	 */
	private static String getSignatureAlgorithm(final String mdAlgorithm) {
		return mdAlgorithm.replace("-", "") + "withRSA"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Genera la prefirma de una petici&oacute;n de firma.
	 * @param request Petici&oacute;n de firma.
	 * @param commMgr Gestor de las llamadas a servicios remotos.
	 * @throws CertificateEncodingException Cuando hay un error en la codificaci&oacute;n del certificado.
	 * @throws IOException Cuando hay un error de lectura/escritura de datos.
	 * @throws SAXException Cuando el XML est&aacute; mal formado.
	 */
	private static TriphaseRequest[] signPhase1(final SignRequest request, final CommManager commMgr)
			throws CertificateException, SAXException, IOException {
		return commMgr.preSignRequests(request);
	}

	/**
	 * Genera la firma PKCS#1 (segunda fase del proceso de firma trif&aacute;sica) y muta el objeto
	 * de petici&oacute;n de firma de un documento para almacenar el mismo el resultado.
	 * @param docRequest Petici&oacute;n de firma de un documento.
	 * @param key Clave privada de firma.
	 * @param certChain Cadena de certificaci&oacute;n del certificado de firma.
	 * @throws AOException Cuando ocurre alg&uacute;n error al generar el PKCS#1 o falta alg&uacute;n
	 * par&aacute;metro obligatorio dentro del resultado de prefirma de alguno de los documentos.
	 */
	private static void signPhase2(final TriphaseSignDocumentRequest docRequest, final PrivateKey key, final Certificate[] certChain) throws AOException {

		final String signatureAlgorithm = getSignatureAlgorithm(docRequest.getMessageDigestAlgorithm());

		final TriphaseConfigData config = docRequest.getPartialResult();

		// TODO: Es posible que se ejecute mas de una firma como resultado de haber proporcionado varios
		// identificadores de datos o en una operacion de contrafirma.

		byte[] preSign;
		try {
			preSign = config.getPreSign();
		}
		catch (final IOException e) {
			// Cuando la respuesta no indica el numero de firmas y no se ha devuelto ninguna
			PfLog.e(SFConstants.LOG_TAG, "No se ha devuelto ningun resultado de firma"); //$NON-NLS-1$
			preSign = null;
		}

		if (preSign == null) {
			throw new AOException("El servidor no ha devuelto la prefirma del documento"); //$NON-NLS-1$
		}

		final byte[] pkcs1sign = new AOPkcs1Signer().sign(
				preSign,
				signatureAlgorithm,
				key,
				certChain,
				null // No hay parametros en PKCS#1
				);

		// Configuramos la peticion de postfirma indicando las firmas PKCS#1 generadas
		config.setPk1(pkcs1sign);

		if (!config.isNeedPreSign()) {
			config.removePreSign();
		}
	}

	/**
	 * Genera la postfirma de un listado de prefirmas.
	 * @param request Petici&oacute;n de firma.
	 * @param commMgr Gestor de las llamadas a servicios remotos.
	 * @throws IOException Cuando hay un error de lectura/escritura de datos.
	 * @throws SAXException Cuando el XML est&aacute; mal formado.
	 */
	private static RequestResult signPhase3(final TriphaseRequest[] request, final CommManager commMgr)
			throws SAXException, IOException {
		return commMgr.postSignRequests(request);
	}
}
