package es.gob.afirma.android.signfolder.adapter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.activities.AuthRoleInfoActivity;
import es.gob.afirma.android.user.configuration.AuthorizedUser;
import es.gob.afirma.android.user.configuration.ConfigurationConstants;

/**
 * Clase que implementa el adaptador para la lista de usuarios con rol de autorizado.
 */
public class AuthorizerAdapter extends RecyclerView.Adapter<AuthorizerAdapter.AuthorizedViewHolder> {

    /**
     * Conjunto de datos a mostrar.
     */
    private List<AuthorizedUser> dataSet;

    /**
     * Constructor por defecto.
     *
     * @param myDataset Conjunto de datos a cargar en la lista.
     */
    public AuthorizerAdapter(List<AuthorizedUser> myDataset) {
        dataSet = myDataset;
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
        AuthorizedUser user = dataSet.get(position);

        // Estado
        String status = user.getStatus();
        ImageView statusIcon = holder.linearLayout.findViewById(R.id.statusIconId);
        switch (status.toUpperCase()) {
            case "VERIFIED":
                statusIcon.setImageResource(R.drawable.icon_check_16);
                break;
            case "PENDING":
                statusIcon.setImageResource(R.drawable.icon_enespera_16);
                break;
            case "REJECTED":
                statusIcon.setImageResource(R.drawable.icon_error_16);
                break;
            default:
                throw new IllegalArgumentException("El estado de la autorización [" + status + "] no está reconocido.");
        }

        // Entrada/Salida
        String inputOutput = user.getSentReceived();
        ImageView inOutIcon = holder.linearLayout.findViewById(R.id.inputOutputIconId);
        switch (inputOutput.toUpperCase()) {
            case "IN":
                inOutIcon.setImageResource(R.drawable.icon_authorized_in_16);
                break;
            case "OUT":
                inOutIcon.setImageResource(R.drawable.icon_authorized_out_16);
                break;
            default:
                throw new IllegalArgumentException("La autorización no ha sido identificada ni " +
                        "como entrada ni como salida. El parámetro recibido ha sido: [" + inputOutput + "].");
        }

        // Nombre remitente
        ((TextView) holder.linearLayout.findViewById(R.id.authNameId)).setText(user.getSenderReceiver());

        // Fecha de finalización.
        String dateAsString = "-";
        Date date = user.getEndDate();
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("es"));
            dateAsString = sdf.format(date);
        }

        ((TextView) holder.linearLayout.findViewById(R.id.authEndDateId)).setText(dateAsString);

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), AuthRoleInfoActivity.class);
                intent.putExtra(ConfigurationConstants.EXTRA_RESOURCE_AUTH_ROLE_INFO, fromAuthUserToArrayString(dataSet.get(holder.getLayoutPosition())));
                v.getContext().startActivity(intent);
            }
        });
    }

    /**
     * Método que transforma un objeto de tipo <i>AuthorizedUser</i> en un array de String.
     * @param authUser Objeto uthorizedUser a parsear.
     * @return un array de string con los valores de los campos del objeto parseado en el siguiente orden:
     * [status, sentReceived, type, senderReceiver, initDate, endDate].
     * Las fechas son parseadas al formato: dd/MM/yyyy.
     */
    private String[] fromAuthUserToArrayString(AuthorizedUser authUser) {
        String[] res = new String[6];

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("es"));
        String initDateAsString = authUser.getInitDate() == null ? null : sdf.format(authUser.getInitDate());
        String endDateAsString = authUser.getEndDate() == null ? null : sdf.format(authUser.getEndDate());

        res[0] = authUser.getStatus();
        res[1] = authUser.getSentReceived();
        res[2] = authUser.getType() == null ? null : authUser.getType().name();
        res[3] = authUser.getSenderReceiver();
        res[4] = authUser.getInitDate() == null ? null : initDateAsString;
        res[5] = authUser.getInitDate() == null ? null : endDateAsString;

        return res;
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
}
