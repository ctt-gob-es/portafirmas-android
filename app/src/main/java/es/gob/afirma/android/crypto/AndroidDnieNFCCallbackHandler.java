package es.gob.afirma.android.crypto;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import android.app.Activity;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import es.gob.jmulticard.android.callbacks.CachePasswordCallback;
import es.gob.jmulticard.android.callbacks.DialogDoneChecker;
import es.gob.jmulticard.android.callbacks.PinDialog;
import es.gob.jmulticard.android.callbacks.ShowPinDialogTask;
import es.gob.jmulticard.android.nfc.AndroidNfcConnection;
import es.gob.jmulticard.callback.CustomAuthorizeCallback;
import es.gob.jmulticard.callback.CustomTextInputCallback;

/** CallbackHandler que gestiona los Callbacks de petici&oacute;n de informaci&oacute;n al usuario.
 * @author Sergio Mart&iacute;nez Rico. */
public class AndroidDnieNFCCallbackHandler implements CallbackHandler {

    private static final String TAG = AndroidNfcConnection.class.getSimpleName();

	private final Activity activity;
	private final DialogDoneChecker dialogDone;
	private CachePasswordCallback canPasswordCallback;
	private CachePasswordCallback pinPasswordCallback;

	/** CallbackHandler que gestiona los Callbacks de petici&oacute;n de informaci&oacute;n al usuario.
	 * @param ac Handler de la actividad desde la que se llama.
	 * @param ddc Instancia de la clase utilizada para utilizar wait() y notify() al esperar el PIN.
	 * @param passwordCallback Instancia que contiene el CAN pedido antes a la lectura NFC.*/
	public AndroidDnieNFCCallbackHandler(final Activity ac, final DialogDoneChecker ddc, final CachePasswordCallback passwordCallback) {
		this.activity = ac;
		this.dialogDone = ddc;
		this.canPasswordCallback = passwordCallback;
		this.pinPasswordCallback = null;
	}

	@Override
	public void handle(final Callback[] callbacks) throws UnsupportedCallbackException {
		if (callbacks != null) {
			for (final Callback cb : callbacks) {

				if (cb instanceof PasswordCallback) {
					String input;
					if (this.pinPasswordCallback == null) {
						final PinDialog dialog = new PinDialog(
								false,
								this.activity,
								cb,
								this.dialogDone
								);
						final FragmentTransaction ft = ((FragmentActivity)this.activity).getSupportFragmentManager().beginTransaction();
						final ShowPinDialogTask spdt = new ShowPinDialogTask(dialog, ft, this.activity, this.dialogDone);
						input = spdt.getInput();

						this.pinPasswordCallback = new CachePasswordCallback(input.toCharArray());
					}
					else {
						input = new String(this.pinPasswordCallback.getPassword());
					}
					((PasswordCallback) cb).setPassword(input.toCharArray());

					return;
				}
				String input;
				if (cb instanceof CustomTextInputCallback) {
					if (this.canPasswordCallback == null) {
						final PinDialog dialog = new PinDialog(
							true,
							this.activity,
							cb,
							this.dialogDone
						);

						final FragmentTransaction ft = ((FragmentActivity)this.activity).getSupportFragmentManager().beginTransaction();
						final ShowPinDialogTask spdt = new ShowPinDialogTask(dialog, ft, this.activity, this.dialogDone);
						input = spdt.getInput();
					}
					else {
						input = new String(this.canPasswordCallback.getPassword());

						// En caso de fallar el primer CAN lo pedira de nuevo al ususario
						this.canPasswordCallback = null;
					}

					((CustomTextInputCallback) cb).setText(input);

					return;
				}

				if (cb instanceof CustomAuthorizeCallback) {
					return;
				}

				Log.e(TAG, "Se ha solicitado un tipo de entrada desconocido: " + cb.getClass().getName());
			}
		}
		else {
			Log.w(TAG, "Se ha recibido un array de Callbacks nulo"); //$NON-NLS-1$
			throw new UnsupportedCallbackException(null);
		}
	}

	public void clearPin() {
		if (this.pinPasswordCallback != null) {
			this.pinPasswordCallback.clearPassword();
			this.pinPasswordCallback = null;
		}
	}

	public void clearCan() {
		if (this.canPasswordCallback != null) {
			this.canPasswordCallback.clearPassword();
			this.canPasswordCallback = null;
		}
	}
}
