package es.gob.afirma.android.signfolder.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.security.KeyChain;

import java.security.KeyStore.PrivateKeyEntry;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import es.gob.afirma.android.crypto.MobileKeyStoreManager.KeySelectedEvent;
import es.gob.afirma.android.crypto.MobileKeyStoreManager.PrivateKeySelectionListener;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.util.PfLog;

public final class LoadSelectedPrivateKeyTask extends AsyncTask<Void, Void, PrivateKey> {

	private final String selectedAlias;
	private final Context context;
	private final PrivateKeySelectionListener listener;
	private X509Certificate[] certChain;
	private Throwable t;

	public LoadSelectedPrivateKeyTask(final String certAlias, final PrivateKeySelectionListener listener, final Context context) {
		this.selectedAlias = certAlias;
		this.listener = listener;
		this.context = context;
	}

	@Override
	protected PrivateKey doInBackground(final Void... params) {

		final PrivateKey pk;
		try {
			pk = KeyChain.getPrivateKey(this.context, this.selectedAlias);
			this.certChain = KeyChain.getCertificateChain(this.context, this.selectedAlias);
		} catch (final Exception e) {
			PfLog.e(SFConstants.LOG_TAG, "No se pudo cargar la clave del certificado seleccionado", e);
			this.t = e;
			return null;
		}

		return pk;
	}

	@Override
	protected void onPostExecute(final PrivateKey privateKey) {

		final KeySelectedEvent ksEvent;
		if (privateKey != null) {
			ksEvent = new KeySelectedEvent(new PrivateKeyEntry(privateKey, this.certChain), this.selectedAlias);
		}
		else {
			ksEvent = new KeySelectedEvent(this.t);
		}

		this.listener.keySelected(ksEvent);
	}
}
