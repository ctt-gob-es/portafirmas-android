package es.gob.afirma.android.signfolder.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.user.configuration.VerifierUser;

/**
 * Clase que implementa el adaptador para la lista de usuarios con rol de validador.
 */
public class VerifierAdapter extends RecyclerView.Adapter<VerifierAdapter.VerifierViewHolder> {

    /**
     * Conjunto de datos a mostrar.
     */
    private List<VerifierUser> dataSet;

    /**
     * Constructor por defecto.
     *
     * @param myDataset Conjunto de datos a cargar en la lista.
     */
    public VerifierAdapter(List<VerifierUser> myDataset) {
        dataSet = myDataset;
    }

    @Override
    public VerifierAdapter.VerifierViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout l = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.verifier_item_view, parent, false);
        return new VerifierViewHolder(l);
    }

    @Override
    public void onBindViewHolder(final VerifierViewHolder holder, final int position) {
        VerifierUser user = dataSet.get(position);
        ((TextView) holder.linearLayout.findViewById(R.id.idView)).setText(user.getIdentifier());
        ((TextView) holder.linearLayout.findViewById(R.id.nameView)).setText(user.getVerifierName());
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

}
