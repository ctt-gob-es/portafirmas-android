package es.gob.afirma.android.signfolder.listeners;

public interface DialogFragmentListener {

	void onDialogPositiveClick(int dialogId, String reason);

	void onDialogNegativeClick(int dialogId);
}
