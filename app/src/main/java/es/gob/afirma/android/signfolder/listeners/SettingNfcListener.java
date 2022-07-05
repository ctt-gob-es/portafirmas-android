package es.gob.afirma.android.signfolder.listeners;

/**
 * Manegador que define el comportamiento despu&eacute;s de acceder al panel de configuraci&oacute;n
 * de NFC y volver a la aplicaci&oacute;n.
 */
public interface SettingNfcListener {

    /**
     * Define el comportamiento despu&eacute;s de volver a la aplicaci&oacute;n principal desde la
     * pantalla de configuraci&oacute;n del NFC.
     * @param nfcEnabled Indica si el NFC ha quedado activado.
     */
    void detectNfcChanges(boolean nfcEnabled);
}
