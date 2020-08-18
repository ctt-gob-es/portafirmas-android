package es.gob.afirma.android.signfolder.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.activities.CreateNewAuthorizedActivity;
import es.gob.afirma.android.signfolder.activities.CreateNewVerifierActivity;
import es.gob.afirma.android.signfolder.activities.PetitionListActivity;
import es.gob.afirma.android.signfolder.proxy.RequestAppConfiguration;
import es.gob.afirma.android.user.configuration.ConfigurationConstants;
import es.gob.afirma.android.user.configuration.ConfigurationRole;
import es.gob.afirma.android.user.configuration.UserConfiguration;

/**
 * Clase que implementa el adaptador para la lista de usuarios.
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    /**
     * Conjunto de datos a mostrar.
     */
    private List<UserConfiguration> dataSet;

    /**
     * Rol seleccionado.
     */
    private ConfigurationRole selectedRole;

    /**
     * Lista de aplicaciones.
     */
    private RequestAppConfiguration apps;


    /**
     * Constructor por defecto.
     * @param dataSetParam Lista de usuarios.
     * @param selectedRoleParam Rol seleccionado.
     * @param appsParam Lista de aplicaciones.
     */
    public UserAdapter(List<UserConfiguration> dataSetParam, ConfigurationRole selectedRoleParam, RequestAppConfiguration appsParam) {
        dataSet = dataSetParam;
        selectedRole = selectedRoleParam;
        apps = appsParam;
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
        UserConfiguration user = dataSet.get(i);
        ((TextView) userViewHolder.linearLayout.findViewById(R.id.idView)).setText(user.getID());
        ((TextView) userViewHolder.linearLayout.findViewById(R.id.nameView)).setText(generateFullName(user));

        userViewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConfigurationRole.AUTHORIZED.equals(selectedRole)) {
                    Intent intent = new Intent(v.getContext(), CreateNewAuthorizedActivity.class);
                    intent.putExtra(ConfigurationConstants.EXTRA_RESOURCE_USER_INFO, fromUserToArrayString(dataSet.get(userViewHolder.getLayoutPosition())));
                    ((Activity) v.getContext()).startActivityForResult(intent, ConfigurationConstants.ACTIVITY_REQUEST_CODE_AUTH_CREATION);
                } else if (ConfigurationRole.VERIFIER.equals(selectedRole)) {
                    Intent intent = new Intent(v.getContext(), CreateNewVerifierActivity.class);
                    intent.putExtra(ConfigurationConstants.EXTRA_RESOURCE_USER_INFO, fromUserToArrayString(dataSet.get(userViewHolder.getLayoutPosition())));
                    intent.putStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_IDS, apps.getAppIdsList());
                    intent.putStringArrayListExtra(PetitionListActivity.EXTRA_RESOURCE_APP_NAMES, apps.getAppNamesList());
                    ((Activity) v.getContext()).startActivityForResult(intent, ConfigurationConstants.ACTIVITY_REQUEST_CODE_VERIFIER_CREATION);
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
     * Método auxiliar que genera el nombre completo del usuario.
     *
     * @param user Objeto que representa el usuario.
     * @return el nombre completo del usuario o "-" en caso de no poder generarlo.
     */
    private String generateFullName(UserConfiguration user) {
        if (user == null ||
                user.getName() == null && user.getSurname() == null && user.getSecondSurname() == null) {
            return "-";
        } else {
            String space = " ";
            StringBuilder sb = new StringBuilder();
            if (user.getName() != null) {
                sb.append(user.getName());
                sb.append(space);
            }
            if (user.getSurname() != null) {
                sb.append(user.getSurname());
                sb.append(space);
            }
            if (user.getSecondSurname() != null) {
                sb.append(user.getSecondSurname());
                sb.append(space);
            }
            return sb.toString();
        }
    }

    /**
     * Método que construye un array de string a partir de los parámetros de tipo "UserConfiguration".
     *
     * @param user Objeto a parsear.
     * @return un array de string con los siguientes elementos: [ID, name, surname, secondSurname].
     */
    private String[] fromUserToArrayString(UserConfiguration user) {
        String[] res = new String[4];

        res[0] = user.getID();
        res[1] = user.getName();
        res[2] = user.getSurname();
        res[3] = user.getSecondSurname();

        return res;
    }

    /**
     * Clase que representa el view holder de la lista de usuarios.
     */
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout linearLayout;

        /**
         * Constructor con 1 parámetro.
         *
         * @param inputItem vista padre contenedora de la lista de usuarios.
         */
        public UserViewHolder(RelativeLayout inputItem) {
            super(inputItem);
            linearLayout = inputItem;
        }
    }
}
