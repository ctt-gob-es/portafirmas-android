package es.gob.afirma.android.signfolder.adapter;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import es.gob.afirma.android.signfolder.MessageDialog;
import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.activities.AuthorizationInfoActivity;
import es.gob.afirma.android.signfolder.activities.FindUserActivity;
import es.gob.afirma.android.user.configuration.ConfigurationConstants;
import es.gob.afirma.android.user.configuration.ConfigurationRole;
import es.gob.afirma.android.user.configuration.GenericUser;
import es.gob.afirma.android.user.configuration.Validator;
import es.gob.afirma.android.user.configuration.VerifierUser;
import es.gob.afirma.android.util.PfLog;

/**
 * Clase que implementa el adaptador para la lista de usuarios con rol de validador.
 */
public class VerifierAdapter extends RecyclerView.Adapter<VerifierAdapter.VerifierViewHolder> {

    /** Conjunto de datos a mostrar. */
    private List<Validator> dataSet;

    /** Objeto para la escucha de la selección de un elemento del listado. */
    private SelectValidatorListener listener;

    /**
     * Constructor por defecto.
     *
     * @param dataSet Conjunto de datos a cargar en la lista.
     */
    public VerifierAdapter(List<Validator> dataSet, SelectValidatorListener listener) {
        this.dataSet = dataSet;
        this.listener = listener;
    }

    @Override
    public VerifierAdapter.VerifierViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout l = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.verifier_item_view, parent, false);
        return new VerifierViewHolder(l);
    }

    @Override
    public void onBindViewHolder(final VerifierViewHolder holder, final int position) {
        Validator validator = dataSet.get(position);
        ((TextView) holder.linearLayout.findViewById(R.id.nameView)).setText(validator.getUser().getName());

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Validator validator = dataSet.get(holder.getLayoutPosition());
                VerifierAdapter.this.listener.itemSelected(validator);
            }
        });
    }


    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public static class VerifierViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout linearLayout;

        public VerifierViewHolder(RelativeLayout inputItem) {
            super(inputItem);
            linearLayout = inputItem;
        }
    }


    public static interface SelectValidatorListener {
        void itemSelected(Validator validator);
    }
}
