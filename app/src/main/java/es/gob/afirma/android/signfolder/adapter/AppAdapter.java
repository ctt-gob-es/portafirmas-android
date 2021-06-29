package es.gob.afirma.android.signfolder.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.proxy.RequestAppConfiguration;

/**
 * Clase que representa el adaptador para la vista recyclerView que se encarga de mostrar
 * la lista de aplicaciones en la pantalla de creación de validadores.
 */
public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> {

    /**
     * Conjunto de datos a mostrar.
     */
    private RequestAppConfiguration dataSet;

    /**
     * Atributo que representa el listener de los checkbox de la lista de aplicaciones.
     */
    @NonNull
    private OnItemCheckListener onItemClick;

    /**
     * Atributo que representa la lista de aplicaciones seleccionadas.
     */
    private List<String> appsSelected = new ArrayList<>();

    /**
     * Constructor por defecto.
     * @param myDataset Conjunto de datos a cargar en la lista.
     * @param appsSelectedParam Lista de aplicaciones seleccionadas.
     * @param listener Listener de checkboxs de aplicaciones.
     */
    public AppAdapter(RequestAppConfiguration myDataset, List<String> appsSelectedParam, @NonNull OnItemCheckListener listener) {
        dataSet = myDataset;
        onItemClick = listener;
        appsSelected.addAll(appsSelectedParam);
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LinearLayout l = (LinearLayout) LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.app_item_view, viewGroup, false);

        return new AppAdapter.AppViewHolder(l);
    }

    @Override
    public void onBindViewHolder(@NonNull final AppViewHolder appViewHolder, int i) {
        // Recuperamos el nombre de la aplicación en la posición 'i'.
        String appName = dataSet.getAppNamesList().get(i);

        // Cargamos la vista a actualizar.
        CheckBox cb = appViewHolder.linearLayout.findViewById(R.id.cbAppId);

        // Modificamos el nombre a mostrar.
        cb.setText(appName);

        // Modificamos si debe estar marcada o no.
        if (appsSelected.contains(dataSet.getAppIdsList().get(i))) {
            cb.setChecked(true);
        } else {
            cb.setChecked(false);
        }

        // Modificamos el comportamiento del checkbox cuando es marcado/desmarcado.
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String appId = dataSet.getAppIdsList().get(appViewHolder.getLayoutPosition());
                String appName = dataSet.getAppNamesList().get(appViewHolder.getLayoutPosition());
                if (isChecked) {
                    onItemClick.onItemCheck(appId);
                    appsSelected.add(appId);
                } else {
                    onItemClick.onItemUncheck(appId);
                    appsSelected.remove(appId);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.getAppIdsList().size();
    }

    /**
     * Interfaz que define las operaciones del listener asociado al checkbox de cada aplicación.
     */
    public interface OnItemCheckListener {
        void onItemCheck(String appId);
        void onItemUncheck(String appId);
    }

    /**
     * Clase que representa el viewHolder de la recycler view.
     */
    public static class AppViewHolder extends RecyclerView.ViewHolder {

        /**
         * Attributo que representa el layout del dato.
         */
        public LinearLayout linearLayout;

        /**
         * Constructor por defecto.
         *
         * @param inputItem Layout del dato.
         */
        public AppViewHolder(LinearLayout inputItem) {
            super(inputItem);
            linearLayout = inputItem;
        }
    }
}
