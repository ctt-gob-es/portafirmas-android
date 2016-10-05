package es.gob.afirma.android.crypto;

import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;

import android.app.Activity;
import android.security.KeyChain;
import android.security.KeyChainAliasCallback;
import es.gob.afirma.android.signfolder.CryptoConfiguration;

/** Gestor simple de claves y certificados para dispositivos Android 4.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s */
public final class Android4KeyStoreManager implements MobileKeyStoreManager {

    private final Activity activity;

    Activity getActivity() {
        return this.activity;
    }

    /** Construye un gestor simple de claves y certificados para dispositivos Android 4.
     * @param act Actividad padre de la aplicaci&oacute;n padre */
    public Android4KeyStoreManager(final Activity act) {
        if (act == null) {
            throw new IllegalArgumentException(
        		"Es necesaria una actividad padre para mostrar los dialogos de seleccion de certificado" //$NON-NLS-1$
            );
        }
        this.activity = act;
    }

    /** {@inheritDoc} */
    @Override
    public void getPrivateKeyEntryAsynchronously(final PrivateKeySelectionListener pksl) {
        if (pksl == null) {
            throw new IllegalArgumentException("La clase a notificar la seleccion de clave no puede ser nula"); //$NON-NLS-1$
        }
        KeyChain.choosePrivateKeyAlias(
    		this.activity, new KeyChainAliasCallback() {
		        /** {@inheritDoc} */
		        @Override
		        public void alias(final String alias) {
		        	if (alias != null) {
			            try {
			            	final PrivateKeyEntry pke = new PrivateKeyEntry(
			            			KeyChain.getPrivateKey(
			            					Android4KeyStoreManager.this.getActivity(),
			            					alias),
			            			KeyChain.getCertificateChain(
			            					Android4KeyStoreManager.this.getActivity(),
			            					alias)
			            			);

			            	CryptoConfiguration.setCertificatePrivateKeyEntry(pke);
			            	CryptoConfiguration.setCertificateAlias(alias);

			                pksl.keySelected(
		                		new KeySelectedEvent(
	                				pke,
	                                alias
	            				)
	                		);
			            }
			            catch (final Throwable e) {
			                pksl.keySelected(new KeySelectedEvent(e));
			            }
		        	}
		        	else {
		        		pksl.keySelected(new KeySelectedEvent(new KeyStoreException("El usuario no selecciono un certificado"))); //$NON-NLS-1$
		        	}
		        }
    		},
            new String[] { "RSA" }, // KeyTypes //$NON-NLS-1$
            null, // Issuers
            null, // Host
            -1, // Port
            null // Alias
        );
    }

}