package es.gob.afirma.android.gui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.Iterator;

import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.proxy.SignaturePermission;

/**
 * Di&aacute;logo con el que el usuario puede decidir si continuar con las firmas que requieren
 * confirmaci&oacute;n por presentar alg&uacute;n problema (multifirma de firmas no v&aacute;lidas,
 * firma de PDF certificados, etc.)
 */
public class ConfirmSignatureDialog extends DialogFragment {

    private Iterator<SignaturePermission> permissionIt = null;
    private SignaturePermission permission = null;
    private ConfirmSignatureDialogListener listener;

    public interface ConfirmSignatureDialogListener {
        void onConfirmSignatureDialogPositiveButton(SignaturePermission permission, Iterator<SignaturePermission> permissionIt);
        void onConfirmSignatureDialogNegativeButton(SignaturePermission permission, Iterator<SignaturePermission> permissionIt);
    }

    /**
     * Establece el mensaje de solicitud de confirmacion para el usuario y el permiso asociado.
     * @param permissionIt Permiso que se desea obtener.
     */
    public void setPermissionIterator(Iterator<SignaturePermission> permissionIt) {
        this.permission = permissionIt.next();
        this.permissionIt = permissionIt;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(this.permission.getRequestorText()));
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                listener.onConfirmSignatureDialogPositiveButton(permission, permissionIt);
            }
        })
        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                listener.onConfirmSignatureDialogNegativeButton(permission, permissionIt);
            }
        });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.listener = (ConfirmSignatureDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("La actividad " + context.toString()
                    + " debe implementar ConfirmSignatureDialogListener");
        }
    }
}
