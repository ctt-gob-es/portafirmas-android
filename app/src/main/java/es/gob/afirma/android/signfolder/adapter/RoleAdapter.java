package es.gob.afirma.android.signfolder.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.activities.PetitionListActivity;
import es.gob.afirma.android.user.configuration.ConfigurationConstants;
import es.gob.afirma.android.user.configuration.RoleInfo;
import es.gob.afirma.android.user.configuration.UserConfig;

/**
 * Clase que implementa el adaptador para la lista de roles.
 */
public class RoleAdapter extends RecyclerView.Adapter<RoleAdapter.RoleViewHolder> {

    /**
     * Configuración de usuario completa.
     */
    private UserConfig userconfig;

    /**
     * Rol seleccionado por el usuario.
     */
    private RoleInfo selectedRole;

    /**
     * Intent de la llamada anterior necesario para propagar los parámetros de usuario.
     */
    private Intent intent;

    /**
     * Bandera que indica si se debe limpiar la pila de actividades previas.
     */
    private boolean cleanStack;

    /**
     * Constructor de la clase.
     *
     * @param userconfig Configuración de usuario.
     */
    public RoleAdapter(UserConfig userconfig, Intent intent, boolean cleanStack) {
        this.userconfig = userconfig;
        this.intent = intent;
        this.cleanStack = cleanStack;
    }

    @NonNull
    @Override
    public RoleAdapter.RoleViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
        RelativeLayout l = (RelativeLayout) LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.role_item_view, viewGroup, false);
        return new RoleViewHolder(l);
    }

    @Override
    public void onBindViewHolder(@NonNull final RoleViewHolder roleViewHolder, int i) {
        selectedRole = userconfig.getRoles().get(i);

        if (selectedRole.getRoleId().equalsIgnoreCase("FIRMANTE")) {
            String mainText = "Acceder como firmante";
            ((TextView) roleViewHolder.layout.findViewById(R.id.roleNameId)).setText(mainText);
            ((TextView) roleViewHolder.layout.findViewById(R.id.dniId)).setText("");
            ((ImageView) roleViewHolder.layout.findViewById(R.id.roleTypeIconId)).setImageResource(R.drawable.icon_signline_firma);
        } else {
            ((TextView) roleViewHolder.layout.findViewById(R.id.roleNameId)).setText(selectedRole.getUserName());
            ((TextView) roleViewHolder.layout.findViewById(R.id.dniId)).setText(selectedRole.getRoleName());
            if (selectedRole.getRoleId().equalsIgnoreCase("VALIDADOR")) {
                ((ImageView) roleViewHolder.layout.findViewById(R.id.roleTypeIconId)).setImageResource(R.drawable.icon_signline_vistobueno);
            } else {
                ((ImageView) roleViewHolder.layout.findViewById(R.id.roleTypeIconId)).setImageResource(R.drawable.round_gesture_black_60);
            }
        }
        roleViewHolder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.setClass(v.getContext(), PetitionListActivity.class);
                intent.putExtra(ConfigurationConstants.EXTRA_RESOURCE_USER_CONFIG, userconfig);
                intent.putExtra(ConfigurationConstants.EXTRA_RESOURCE_ROLE_SELECTED, userconfig.getRoles().get(roleViewHolder.getLayoutPosition()));
                ((Activity) v.getContext()).startActivity(intent);
                if(cleanStack){
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                }
                ((Activity) v.getContext()).finish();

            }
        });
    }

    @Override
    public int getItemCount() {
        return userconfig.getRoles().size();
    }

    /**
     * Clase que representa el view holder de la lista de roles.
     */
    public static class RoleViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout layout;

        /**
         * Constructor con 1 parámetro.
         *
         * @param inputItem vista padre contenedora de la lista de roles.
         */
        public RoleViewHolder(RelativeLayout inputItem) {
            super(inputItem);
            layout = inputItem;
        }
    }
}
