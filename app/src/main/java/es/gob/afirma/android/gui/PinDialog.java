package es.gob.afirma.android.gui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;

import es.gob.afirma.android.crypto.KeyStoreManagerListener;
import es.gob.afirma.android.crypto.LoadKeyStoreManagerTask;
import es.gob.afirma.android.crypto.LoadingKeyStoreResult;
import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.util.AOUtil;
import es.gob.afirma.android.util.PfLog;

/** Di&acute;logo para introducir el PIN.
 * Se usa en almacenes distintos al del propio sistema operativo Android.
 * @author Astrid Idoate */

public class PinDialog extends DialogFragment {

	private String provider;
	String getProviderName() {
		return this.provider;
	}

	private String keyStoreName;
	String getKeyStoreName() {
		return this.keyStoreName;
	}

	private KeyStoreManagerListener ksmListener;
	KeyStoreManagerListener getKsmListener() {
		return this.ksmListener;
	}

	private LoadKeyStoreManagerTask ksmTask = null;
	LoadKeyStoreManagerTask getKsmTask() {
		return this.ksmTask;
	}

	/** Construye un di&acute;logo para introducir el PIN. */
	public PinDialog() {
		this.ksmListener = null;
	}

	/** Obtiene una nueva instancia de un di&acute;logo para introducir el PIN.
	 * @param provider proveedor Proveedor de seguridad para obtener el almac&eacute;n de claves
	 * @param keyStoreName Nombre del almac&eacute;n de claves
	 * @param ksml Clase a la que se establece el gestor de almacenes de claves y certificados
	 * @return Di&acute;logo creado.
	 */
	public static PinDialog newInstance(final String provider, final String keyStoreName, final KeyStoreManagerListener ksml) {

		final PinDialog pinDialog = new PinDialog();
		pinDialog.setKeyStoreManagerListener(ksml);
		final Bundle args = new Bundle();
		args.putString("provider", provider); //$NON-NLS-1$
		args.putString("keyStoreName", keyStoreName); //$NON-NLS-1$
		pinDialog.setArguments(args);
		return pinDialog;

	}

	/** Establece la clase que manejara el resultado de la carga del almacen de claves del dispositivo.
	 * @param ksml Manejador de la carga. */
	public void setKeyStoreManagerListener(final KeyStoreManagerListener ksml) {
		this.ksmListener = ksml;
	}

	/** Establece la clase a la que hay que establecer el almac&eacute;n sobre el cual se pide el PIN.
	 * @param lksmt Clase en la que hay que establecer el almac&eacute;n */
	public void setLoadKeyStoreManagerTask(final LoadKeyStoreManagerTask lksmt) {
		this.ksmTask = lksmt;
	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState){

		this.provider = getArguments().getString("provider"); //$NON-NLS-1$
		this.keyStoreName = getArguments().getString("keyStoreName"); //$NON-NLS-1$

		PfLog.i(SFConstants.LOG_TAG,"PinDialog recibe los argumentos provider: " + this.provider + " y keyStoreName: " + this.keyStoreName);   //$NON-NLS-1$//$NON-NLS-2$

		final Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
		alertDialogBuilder.setTitle(getString(R.string.security_code) + " " + this.keyStoreName); //$NON-NLS-1$

		final LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
		final View view = layoutInflater.inflate(R.layout.dialog_pin, null);

		final EditText editTextPin = (EditText) view.findViewById(R.id.etPin);
		alertDialogBuilder.setView(view);
		alertDialogBuilder.setNegativeButton(getActivity().getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int id) {
				dialog.dismiss();
				//Cancelamos el proceso
				if (PinDialog.this.getKsmListener() != null) {
					LoadingKeyStoreResult result = new LoadingKeyStoreResult("Operacion cancelada por el usuario", null);
					result.setCancelled(true);
					PinDialog.this.getKsmListener().onLoadingKeyStoreResult(result);
				}
			}
		});
		alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {

				//TODO: El PIN no puede ser cadena vacia?
				if(editTextPin.getText() != null && !"".equals(editTextPin.getText().toString())) { //$NON-NLS-1$
					// Aqui tenemos el PIN, empezamos con la inicializacion del almacen
					final KeyStore ks;
					try {
						ks = KeyStore.getInstance(PinDialog.this.getKeyStoreName(), PinDialog.this.getProviderName());
						ks.load(null, editTextPin.getText().toString().toCharArray());
					}
					catch(final Exception e) {
						PfLog.e(SFConstants.LOG_TAG, "Error al cargar el almacen de claves: " + e); //$NON-NLS-1$
						dialog.dismiss();
						if (PinDialog.this.getKsmListener() != null) {
							PinDialog.this.getKsmListener().onLoadingKeyStoreResult(
									new LoadingKeyStoreResult(
											getActivity().getString(R.string.error_loading_keystore), e
									));
						}
						return;
					}

					// Obtenemos los elementos para el dialogo de seleccion
					final Enumeration<String> aliases;
					try {
						aliases = ks.aliases();
					}
					catch(final Exception e) {
						PfLog.e(SFConstants.LOG_TAG, "Error extrayendo los alias de los certificados del almacen: " + e); //$NON-NLS-1$
						dialog.dismiss();
						if (PinDialog.this.getKsmListener() != null) {
							PinDialog.this.getKsmListener().onLoadingKeyStoreResult(
									new LoadingKeyStoreResult(
											getActivity().getString(R.string.error_loading_certificate_alias), e
									));
						}
						return;
					}

					final ArrayList<CertificateInfoForAliasSelect> arrayListCertificate = new ArrayList<>();
					while(aliases.hasMoreElements()) {
						final String alias = aliases.nextElement();
						final X509Certificate cert;
						try {
							cert = (X509Certificate) ks.getCertificate(alias);
						}
						catch (final KeyStoreException e) {
							PfLog.w(SFConstants.LOG_TAG, "No se ha podido extraer el certificado '" + alias + "': " + e);  //$NON-NLS-1$//$NON-NLS-2$
							continue;
						}

						// Comprobamos si tiene clave privada o no
						try {
							ks.getEntry(alias, null);
						}
						catch(final Exception e) {
							PfLog.w(SFConstants.LOG_TAG, "Se omite el certificado '" + AOUtil.getCN(cert) + "' por no tener clave privada: " + e); //$NON-NLS-1$ //$NON-NLS-2$
							continue;
						}
						arrayListCertificate.add(
								new CertificateInfoForAliasSelect(
										AOUtil.getCN(cert),
										cert.getNotBefore(), cert.getNotAfter(),
										alias,
										AOUtil.getCN(cert.getIssuerX500Principal().toString()))
								);

					}

					if(PinDialog.this.getKsmTask() == null){
						PfLog.e(SFConstants.LOG_TAG, "No se ha establecido la tarea para la obtencion del almacen de certificados con setLoadKeyStoreManagerTask()");  //$NON-NLS-1$
						dialog.dismiss();
						if (PinDialog.this.getKsmListener() != null) {
							PinDialog.this.getKsmListener().onLoadingKeyStoreResult(
									new LoadingKeyStoreResult(
											getActivity().getString(R.string.error_loading_keystore), null
									));
						}
					}

					final SelectAliasDialog selectAlias = SelectAliasDialog.newInstance(
						arrayListCertificate,
						PinDialog.this.getKsmListener()
					);
					selectAlias.setKeyStore(ks);
					selectAlias.setPin(editTextPin.getText().toString().toCharArray());

					selectAlias.show(getActivity().getFragmentManager(), "SelectAliasDialog"); //$NON-NLS-1$
					dialog.dismiss();
				}
			}
		});
		alertDialogBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(final DialogInterface dialog, final int keyCode, final KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					dialog.dismiss();
					// Cancelamos el proceso
					if (PinDialog.this.getKsmListener() != null) {
						LoadingKeyStoreResult result = new LoadingKeyStoreResult("Operacion cancelada por el usuario", null);
						result.setCancelled(true);
						PinDialog.this.getKsmListener().onLoadingKeyStoreResult(result);
					}
					return true;
				}
				return false;
			}
		});

		return alertDialogBuilder.create();
	}
}