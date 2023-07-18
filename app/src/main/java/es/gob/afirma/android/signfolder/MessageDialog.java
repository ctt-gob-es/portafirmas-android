package es.gob.afirma.android.signfolder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import es.gob.afirma.android.util.PfLog;

/** Di&aacute;logo modal con el que mostrar al usuario un mensaje y un bot&oacute;n para ocultar el
 * di&aacute;logo y, opcionalmente, realizar una acci&oacute;n. */
final public class MessageDialog extends DialogFragment {

	private String title = null;
	private String message = null;

	private DialogInterface.OnClickListener positiveListener = null;
	private DialogInterface.OnClickListener negativeListener = null;

	private AlertDialog.Builder dialogBuilder;

	private boolean needShowNegativeButton;

	public MessageDialog() {
		//Default constructor
		this.needShowNegativeButton = false;
		if (getActivity() != null) {
			this.dialogBuilder = new AlertDialog.Builder(getActivity());
		}
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	public void setTitle(final String title) {
		this.title = title;
	}


	public String getMessage() {
		return this.message;
	}

	public String getTitle() {
		return this.title;
	}

	public void setListeners(final DialogInterface.OnClickListener positiveListener, final DialogInterface.OnClickListener negativeListener) {
		this.positiveListener = positiveListener;
		this.negativeListener = negativeListener;
	}

	public void setNeedShowNegativeButton(final boolean needShowNegativeButton) { this.needShowNegativeButton = needShowNegativeButton; }

	public void setContext(final Context context) { this.dialogBuilder = new AlertDialog.Builder(context); }

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {

		if (this.dialogBuilder == null) {
			PfLog.w("es.gob.afirma",
					"Se ha tratado de mostrar un dialogo sin definir antes el contexto");
			return null;
		}

		if (this.title != null) {
			this.dialogBuilder.setTitle(this.title);
		}
		this.dialogBuilder.setMessage(this.message);
		this.dialogBuilder.setPositiveButton(android.R.string.ok, this.positiveListener);

		if (this.needShowNegativeButton) {
			this.dialogBuilder.setNegativeButton(android.R.string.cancel, this.negativeListener);
		}
		return this.dialogBuilder.create();
	}
}
