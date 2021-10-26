package es.gob.afirma.android.signfolder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.user.configuration.Authorization;
import es.gob.afirma.android.user.configuration.AuthorizationState;

/**
 * Clase que implementa el adaptador para la lista de usuarios con rol de autorizado.
 */
public class AuthorizerAdapter extends RecyclerView.Adapter<AuthorizerAdapter.AuthorizedViewHolder> {

    /** Conjunto de datos a mostrar. */
    private final List<Authorization> dataSet;

    /** Objeto para la escucha de las pulsaciones sobre los elementos del listado. */
    private final SelectAutorizationListener listener;

    /**
     * Constructor por defecto.
     * @param dataSet Conjunto de datos a cargar en la lista.
     */
    public AuthorizerAdapter(List<Authorization> dataSet, SelectAutorizationListener listener) {
        this.dataSet = dataSet;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AuthorizerAdapter.AuthorizedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RelativeLayout l = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.auth_item_view, parent, false);

        return new AuthorizerAdapter.AuthorizedViewHolder(l);
    }

    @Override
    public void onBindViewHolder(final AuthorizerAdapter.AuthorizedViewHolder holder, int position) {
        Authorization authorization = this.dataSet.get(position);

        // Estado
        AuthorizationState state = authorization.getState();
        ImageView statusIcon = holder.linearLayout.findViewById(R.id.statusIconId);
        switch (state) {
            case ACTIVE:
                statusIcon.setImageResource(R.drawable.icon_check_16);
                break;
            case PENDING:
                statusIcon.setImageResource(R.drawable.icon_enespera_16);
                break;
            case REVOKED:
            default:
                statusIcon.setImageResource(R.drawable.icon_error_16);
                break;
        }

        // Entrada/Salida
        boolean sended = authorization.isSended();
        ImageView inOutIcon = holder.linearLayout.findViewById(R.id.inputOutputIconId);
        if (sended) {
            inOutIcon.setImageResource(R.drawable.icon_authorized_out_16);
        }
        else {
            inOutIcon.setImageResource(R.drawable.icon_authorized_in_16);
        }

        // Nombre remitente
        ((TextView) holder.linearLayout.findViewById(R.id.roleNameId)).setText(
                sended ? authorization.getAuthoricedUser().getName() : authorization.getUser().getName());

        // Fecha de finalización.
        String dateAsString = "-";
        Date date = authorization.getRevDate();
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("es"));
            dateAsString = sdf.format(date);
        }

        ((TextView) holder.linearLayout.findViewById(R.id.dniId)).setText(dateAsString);

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Authorization auth = dataSet.get(holder.getLayoutPosition());
                AuthorizerAdapter.this.listener.itemSelected(auth);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public static class AuthorizedViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout linearLayout;

        public AuthorizedViewHolder(RelativeLayout inputItem) {
            super(inputItem);
            linearLayout = inputItem;
        }
    }


    public static interface SelectAutorizationListener {
        void itemSelected(Authorization auth);
    }
}
