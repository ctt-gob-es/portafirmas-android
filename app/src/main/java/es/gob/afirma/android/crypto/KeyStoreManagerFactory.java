package es.gob.afirma.android.crypto;

import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Provider;
import java.security.Security;

import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.activities.NFCDetectorActivity;
import es.gob.afirma.android.util.PfLog;
import es.gob.jmulticard.android.nfc.AndroidNfcConnection;
import es.gob.jmulticard.apdu.connection.ApduConnection;

/** Factor&iacute;a de gestores de contrase&ntilde;as y claves para Android. */
public final class KeyStoreManagerFactory {

	private KeyStoreManagerFactory() {
		// Se prohibe crear instancias
	}

	private static final String ES_GOB_AFIRMA = "es.gob.afirma"; //$NON-NLS-1$

	/** Obtiene el gestor de contrase&ntilde;as y claves m&aacute;s apropiado seg&uacute;n el entorno
	 * operativo y el hardware encontrado.
	 * @param activity Actividad padre
	 * @param ksml Clase a la que hay que notificar la finalizaci&oacute;n de la
	 *             carga e inicializaci&oacute;n del gestor de claves y certificados
	 * @param usbDevice Dispositivo USB en el caso de almacenes de claves externos
	 * @param usbManager Gestor de dispositivos USB en el caso de almacenes de claves externos */
	public static void initKeyStoreManager(final FragmentActivity activity,
											   final KeyStoreManagerListener ksml) {

		// Si no encontramos el almacen anterior, accedemos al almacen del sistema
		Log.i(ES_GOB_AFIRMA, "Estableciendo almacen del sistema"); //$NON-NLS-1$

		ksml.onLoadingKeyStoreResult(new LoadingKeyStoreResult(new Android4KeyStoreManager(activity)));
	}

	/** Obtiene el gestor de contrase&ntilde;as y claves m&aacute;s apropiado seg&uacute;n el entorno
	 * operativo y el hardware encontrado.
	 * @param ksml Clase a la que hay que notificar la finalizaci&oacute;n de la
	 *             carga e inicializaci&oacute;n del gestor de claves y certificados
	 */
	public static KeyStore initNfcKeyStoreManager(final KeyStoreManagerListener ksml)
			throws UnsupportedNfcCardException, InitializingNfcCardException {

		KeyStore ks = null;

		// En caso de no existir un lector conectado por USB, comprobamos que se haya detectado una tarjeta por NFC
		DnieConnectionManager dnieManager = DnieConnectionManager.getInstance();
		if (dnieManager.getDiscoveredTag() != null) {
			try {

				if (dnieManager.getNfcConnection() != null) {
					ks = KeyStore.getInstance("DNI"); //$NON-NLS-1$
				}
				else {
					final ApduConnection androidNfcConnectionObject =
							new AndroidNfcConnection(dnieManager.getDiscoveredTag());
					dnieManager.setNfcConnection(androidNfcConnectionObject);
					final Provider p = new es.gob.jmulticard.jse.provider.DnieProvider(androidNfcConnectionObject);

					Security.addProvider(p);

					// Obtenemos el almacen unicamente para ver si falla
					ks = KeyStore.getInstance("DNI", p); //$NON-NLS-1$
				}

			} catch (final KeyStoreException e) {
				Log.e(ES_GOB_AFIRMA, "Se ha encontrado una tarjeta por NFC, pero no es un DNIe: " + e); //$NON-NLS-1$ //$NON-NLS-2$
				throw new UnsupportedNfcCardException("Se ha encontrado una tarjeta por NFC distinta al DNIe", e);
			} catch (final Exception e) {
				Log.e(ES_GOB_AFIRMA, "No se ha podido instanciar el controlador del DNIe por NFC: " + e, e); //$NON-NLS-1$ //$NON-NLS-2$
				throw new InitializingNfcCardException("Error inicializando la tarjeta", e);
			}
		}
		return ks;
	}
}