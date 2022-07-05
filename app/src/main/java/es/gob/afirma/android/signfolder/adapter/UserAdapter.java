package es.gob.afirma.android.signfolder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.proxy.RequestAppConfiguration;
import es.gob.afirma.android.user.configuration.ConfigurationRole;
import es.gob.afirma.android.user.configuration.GenericUser;

/**
 * Clase que implementa el adaptador para la lista de usuarios.
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    /**
     * Conjunto de datos a mostrar.
     */
    private final List<GenericUser> dataSet;

    /**
     * Rol seleccionado.
     */
    private final ConfigurationRole selectedRole;

    /**
     * Lista de aplicaciones.
     */
    private final RequestAppConfiguration apps;

    /**
     * Objeto para el procesado de la selección de un usuario.
     */
    private final SelectUserListener listener;

    /**
     * Construye el adaptador.
     * @param dataSet Lista de usuarios.
     * @param selectedRole Rol seleccionado.
     * @param apps Lista de aplicaciones.
     */
    public UserAdapter(List<GenericUser> dataSet, ConfigurationRole selectedRole,
                       RequestAppConfiguration apps, SelectUserListener listener) {
        this.dataSet = dataSet;
        this.selectedRole = selectedRole;
        this.apps = apps;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
        RelativeLayout l = (RelativeLayout) LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.verifier_item_view, viewGroup, false);
        return new UserViewHolder(l);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserAdapter.UserViewHolder userViewHolder, int i) {
        GenericUser user = dataSet.get(i);
        ((TextView) userViewHolder.layout.findViewById(R.id.nameView)).setText(user.getName());

        userViewHolder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConfigurationRole.AUTHORIZED.equals(selectedRole)) {
//                    Intent intent = new Intent(v.getContext(), CreateNewAuthorizedActivity.class);
//                    intent.putExtra(ConfigurationConstants.EXTRA_RESOURCE_USER_INFO, dataSet.get(userViewHolder.getLayoutPosition()).toBundle());
//                    ((Activity) v.getContext()).startActivityForResult(intent, ConfigurationConstants.ACTIVITY_REQUEST_CODE_AUTH_CREATION);

                    UserAdapter.this.listener.selectNewAuthorizedUser(dataSet.get(userViewHolder.getLayoutPosition()));

                } else if (ConfigurationRole.VERIFIER.equals(selectedRole)) {
//                    Intent intent = new Intent(v.getContext(), CreateNewVerifierActivity.class);
//                    intent.putExtra(ConfigurationConstants.EXTRA_RESOURCE_USER_INFO, dataSet.get(userViewHolder.getLayoutPosition()).toBundle());
//                    if (apps.getAppIdsList() != null) {
//                        intent.putStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_IDS, apps.getAppIdsList());
//                    }
//                    intent.putStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_NAMES, apps.getAppNamesList());
//                    ((Activity) v.getContext()).startActivityForResult(intent, ConfigurationConstants.ACTIVITY_REQUEST_CODE_VERIFIER_CREATION);
                      UserAdapter.this.listener.selectNewValidatorUser(dataSet.get(userViewHolder.getLayoutPosition()));
                } else {
                    throw new IllegalArgumentException("El rol seleccionado [" + selectedRole + "] no tiene un valor permitido.");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    /**
     * Clase que representa el view holder de la lista de usuarios.
     */
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout layout;

        /**
         * Constructor con 1 parámetro.
         *
         * @param inputItem vista padre contenedora de la lista de usuarios.
         */
        public UserViewHolder(RelativeLayout inputItem) {
            super(inputItem);
            layout = inputItem;
        }
    }

    public static interface SelectUserListener {

        void selectNewAuthorizedUser(GenericUser user);

        void selectNewValidatorUser(GenericUser user);
    }
}
