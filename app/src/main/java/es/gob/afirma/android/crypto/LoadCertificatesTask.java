package es.gob.afirma.android.crypto;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.security.auth.callback.CallbackHandler;

import es.gob.afirma.android.gui.CertificateInfoForAliasSelect;
import es.gob.afirma.android.gui.SelectAliasDialog;
import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.core.misc.AOUtil;
import es.gob.jmulticard.android.callbacks.CachePasswordCallback;
import es.gob.jmulticard.android.callbacks.DialogDoneChecker;

/**
 * Created by a621914 on 09/06/2016.
 */
public class LoadCertificatesTask extends AsyncTask<Void, Void, Exception> {

    private static final String ES_GOB_AFIRMA = "es.gob.afirma";

    private final KeyStore ks;
    private final CachePasswordCallback ksPasswordCallback;
    private final Activity activity;
    private KeyStoreManagerListener ksmListener;

    public LoadCertificatesTask(KeyStore ks, KeyStoreManagerListener ksmListener, Activity ac) {
        this.ks = ks;
        this.ksPasswordCallback = null;
        this.activity = ac;
        this.ksmListener = ksmListener;
    }

    public LoadCertificatesTask(KeyStore ks, CachePasswordCallback pc, KeyStoreManagerListener ksmListener, Activity ac) {
        this.ks = ks;
        this.ksPasswordCallback = pc;
        this.activity = ac;
        this.ksmListener = ksmListener;
    }

    @Override
    protected Exception doInBackground(Void... params) {

        try {
            loadCertificatesFromKeyStore();
        }
        catch (NullPointerException e) {
            // Esto ocurrira cuando no se haya definido un KeyStore especifico, por lo que se usara
            // el almacen del sistema
        }
        catch (Exception e) {
            Log.e(ES_GOB_AFIRMA, "No se pudieron cargar los certificados del almacen: " + e);
            return e;
        }

        return null;
    }

    private void loadCertificatesFromKeyStore() throws Exception {

        DnieConnectionManager dnieManager = DnieConnectionManager.getInstance();
        try {
            // Si no se ha inicializado el gestor para las solicitudes de claves del DNIe,
            // lo inicializamos ahora
            AndroidDnieNFCCallbackHandler dnieCallbackHandler = dnieManager.getCallbackHandler();
            if (dnieCallbackHandler == null) {
                final DialogDoneChecker ddc = new DialogDoneChecker();
                dnieCallbackHandler = new AndroidDnieNFCCallbackHandler(activity, ddc, LoadCertificatesTask.this.ksPasswordCallback);
                dnieManager.setCallbackHandler(dnieCallbackHandler);
            }

            final CallbackHandler callbackHandler = dnieCallbackHandler;

            this.ks.load(
                    new KeyStore.LoadStoreParameter() {
                        @Override
                        public KeyStore.ProtectionParameter getProtectionParameter() {
                            return new KeyStore.CallbackHandlerProtection(
                                    callbackHandler
                            );
                        }
                    }
            );
        }
        catch (final NullPointerException e) {
            // Se dara esta excepcion cuando no haya un KeyStore definido, lo que ocurrira cuando
            // se deba cargar el almacen del sistema
            Log.e(ES_GOB_AFIRMA, "Error al cargar el almacen de claves"); //$NON-NLS-1$
            dnieManager.setCallbackHandler(null);
            throw e;
        }
        catch (final Exception e) {
            // Estamos en una conexion NFC y encapsulamos
            // las excepciones para que se procesen adecuadamente
            Log.e(ES_GOB_AFIRMA, "Error al cargar el almacen de claves del dispositivo. Es posible que CAN introducido fuese incorrecto: " + e); //$NON-NLS-1$
            dnieManager.setCallbackHandler(null);
            throw encapsuleException(e);
        }
        // Obtenemos los elementos para el dialogo de seleccion
        final Enumeration<String> aliases;
        try {
            aliases = this.ks.aliases();
        } catch (final Exception e) {
            Log.e(ES_GOB_AFIRMA, "Error extrayendo los alias de los certificados del almacen: " + e); //$NON-NLS-1$
            throw encapsuleException(e);
        }

        final ArrayList<CertificateInfoForAliasSelect> arrayListCertificate = new ArrayList();

        while (aliases.hasMoreElements()) {
            final String alias = aliases.nextElement();
            X509Certificate cert;
            try {
                cert = (X509Certificate) this.ks.getCertificate(alias);
            } catch (final KeyStoreException e) {
                Log.w(ES_GOB_AFIRMA, "No se ha podido extraer el certificado '" + alias + "': " + e);  //$NON-NLS-1$//$NON-NLS-2$
                throw encapsuleException(e);
            } catch (final Exception e) {
                // Gestion a medida de un DNIe bloqueado (usando JMultiCard)
                if ("es.gob.jmulticard.card.AuthenticationModeLockedException".equals(e.getClass().getName())) { //$NON-NLS-1$
                    manageLockedDnie(e, this.activity, this.ksmListener);
                    return;
                }
                Log.e(ES_GOB_AFIRMA, "Error obteniendo el certificado con alias '" + alias + "': " + e, e); //$NON-NLS-1$ //$NON-NLS-2$
                throw encapsuleException(e);
            }
            arrayListCertificate.add(
                    new CertificateInfoForAliasSelect(
                            AOUtil.getCN(cert),
                            cert.getNotBefore(), cert.getNotAfter(),
                            alias,
                            AOUtil.getCN(cert.getIssuerX500Principal().toString())
                    )
            );

        }

//        if (KeyStoreManagerFactory.ksflStatic == null) {
//            Log.e(ES_GOB_AFIRMA, "No se ha establecido la tarea para la obtencion del almacen de certificados con setLoadKeyStoreManagerTask()");  //$NON-NLS-1$
//            if (KeyStoreManagerFactory.ksmlStatic != null) {
//                KeyStoreManagerFactory.ksmlStatic.onLoadingKeyStoreError(
//                        "No se ha establecido la tarea para la obtencion del almacen de certificados con setLoadKeyStoreManagerTask()", null
//                );
//            }
//            return;
//        }

        if (isCancelled()) {
            LoadingKeyStoreResult result = new LoadingKeyStoreResult("Operacion cancelada", null);
            result.setCancelled(true);
            this.ksmListener.onLoadingKeyStoreResult(result);
            return;
        }

        final SelectAliasDialog selectAlias = SelectAliasDialog.newInstance(
                arrayListCertificate,
                this.ksmListener
        );
        selectAlias.setKeyStore(ks);

        if (isCancelled()) {
            LoadingKeyStoreResult result = new LoadingKeyStoreResult("Operacion cancelada", null);
            result.setCancelled(true);
            this.ksmListener.onLoadingKeyStoreResult(result);
            return;
        }

        // No queremos que muestre todos los certificados del DNIe, sino que firme con el certificado de firma
        // selectAlias.show(this.activity.getFragmentManager(), "SelectAliasDialog"); //$NON-NLS-1$
        selectAlias.signWithSignCertificate();
    }

    private void manageLockedDnie(final Throwable e, final Activity activity, final KeyStoreManagerListener ksListener) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Log.e(ES_GOB_AFIRMA, "El DNIe esta bloqueado: " + e); //$NON-NLS-1$

                final AlertDialog.Builder dniBloqueado = new AlertDialog.Builder(activity);

                dniBloqueado.setTitle(activity.getString(R.string.error_title_dni_blocked));
                dniBloqueado.setMessage(activity.getString(R.string.error_dni_blocked_dlg));
                dniBloqueado.setPositiveButton(
                        activity.getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface d, final int id) {
                                d.dismiss();
                            }
                        }
                );
                dniBloqueado.create();
                dniBloqueado.show();

                if (ksListener != null) {
                    ksListener.onLoadingKeyStoreResult(new LoadingKeyStoreResult(
                            activity.getString(R.string.error_dni_blocked), e
                    ));
                }
            }
        });
    }
    
    /**
     *Elimina el di&acute;logo de carga cuando termina la tarea en segundo plano.
     */
    @Override
    protected void onPostExecute(Exception e) {

        if (isCancelled()) {
            LoadingKeyStoreResult result = new LoadingKeyStoreResult("Operacion cancelada por el usuario", null);
            result.setCancelled(true);
            this.ksmListener.onLoadingKeyStoreResult(result);
            return;
        }

        //Si se pierde la conexion reininciamos el proceso
        if (e != null) {
            this.ksmListener.onLoadingKeyStoreResult(new LoadingKeyStoreResult("Error cargando los certificados. Se reintentara la conexion", e));
        }
    }

    /**
     * Encapsula una excepci&oacute;n para indicar el tipo de error general durante la carga.
     * @param e Excepci&oacute;n a encapsular.
     * @return Excepci&oacute;n general.
     */
    private Exception encapsuleException(final Exception e) {
        Exception ex;
        if (this.ksPasswordCallback != null) {
            ex = new InitializingNfcCardException("Error cargando los certificados del almacen", e);
        }
        else {
            ex = new LoadingCertificateException("Error cargando los certificados del almacen", e);
        }
        return ex;
    }

}
