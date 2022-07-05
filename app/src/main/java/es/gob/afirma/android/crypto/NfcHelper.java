package es.gob.afirma.android.crypto;

import android.content.Context;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;

/**
 * Clase para la detecci&oacute;n de NFC.
 * Created by carlos on 08/07/2016.
 */
public class NfcHelper {

    /**
     * Comprueba si el dispositivo cuenta con NFC.
     * @param context Contexto de la aplicaci&oacute;n.
     * @return {@code true} si el dispositivo tiene NFC, {@code false} en caso contrario.
     */
    public static boolean isNfcServiceAvailable(final Context context) {
        final NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
        return manager != null && manager.getDefaultAdapter() != null;
    }

    /**
     * Comprueba si el dispositivo tiene habilitado NFC.
     * @param context Contexto de la aplicaci&oacute;n.
     * @return {@code true} si el dispositivo tiene habilitado NFC, {@code false} en caso contrario.
     */
    public static boolean isNfcServiceEnabled(final Context context) {
        final NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
        if (manager == null) {
            return false;
        }

        final NfcAdapter adapter = manager.getDefaultAdapter();
        return adapter != null && adapter.isEnabled();
    }
}
