package es.gob.afirma.android.signfolder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.gob.afirma.android.user.configuration.ConfigurationRole;
import es.gob.afirma.android.util.PfLog;

public final class ConfigureFilterDialogBuilder {

    static final String FILTERS_ENABLED = "filters_enabled"; //$NON-NLS-1$
    static final String FILTERS_ORDER_ATTRIBUTE = "filters_order"; //$NON-NLS-1$
    static final String FILTERS_SUBJECT = "filters_subject"; //$NON-NLS-1$
    static final String FILTERS_APP = "filters_app"; //$NON-NLS-1$
    static final String FILTERS_DATE_START = "filters_date_start"; //$NON-NLS-1$
    static final String FILTERS_DATE_END = "filters_date_end"; //$NON-NLS-1$
    static final String FILTERS_SHOW_UNVERIFIED = "filters_show_unverified"; //$NON-NLS-1$
    static final String FILTERS_USER_ID = "filters_user_ID"; //$NON-NLS-1$
    static final String FILTERS_USER_ROLE = "folters_user_role"; //$NON-NLS-1$

    private static final String KEY_ORDER = "orderAscDesc="; //$NON-NLS-1$
    private static final String VALUE_ORDER_DESC = "desc"; //$NON-NLS-1$
    private static final String VALUE_ORDER_ASC = "asc"; //$NON-NLS-1$

    private static final String KEY_ORDER_ATTR = "orderAttribute="; //$NON-NLS-1$
    private static final String VALUE_ORDER_ATTR_DATE = "fmodified"; //$NON-NLS-1$
    private static final String VALUE_ORDER_ATTR_SUBJECT = "dsubject"; //$NON-NLS-1$
    private static final String VALUE_ORDER_ATTR_APP = "application"; //$NON-NLS-1$

    private static final String DEFAULT_VALUE_ORDER_ATTR = VALUE_ORDER_ATTR_DATE;

    private static final String KEY_FILTER_TEXT = "searchFilter="; //$NON-NLS-1$
    private static final String KEY_FILTER_APP = "applicationFilter="; //$NON-NLS-1$
    private static final String KEY_FILTER_DATE_START = "initDateFilter="; //$NON-NLS-1$
    private static final String KEY_FILTER_DATE_END = "endDateFilter="; //$NON-NLS-1$
    private static final String KEY_FILTER_SHOW_UNVERIFIED = "showUnverified="; //$NON-NLS-1$
    private static final String KEY_FILTER_USER_ID = "userId="; //$NON-NLS-1$
    private static final String KEY_FILTER_USER_ROLE = "userRole="; //$NON-NLS-1$

    /**
     * Static attribute that represents the set of applications showed in the filter spiner of applications.
     */
    protected static Map<String, Integer> mApps = new HashMap<>();
    private final KeyValuePair[] orderAdapterItems;

    private final AlertDialog.Builder builder;
    private final View v;
    private FilterConfig filterConfig;

    public ConfigureFilterDialogBuilder(final Bundle bundle, final String[] appIds, final String[] appNames, final ConfigurationRole role, final Activity activity) {

        this.filterConfig = new FilterConfig();

        this.builder = new AlertDialog.Builder(activity);
        final LayoutInflater inflater = activity.getLayoutInflater();

        // Establecemos el layout del dialogo
        this.v = inflater.inflate(R.layout.activity_filters_configuration, null);

        // Inicializamos los listados de configuracion
        this.orderAdapterItems = new KeyValuePair[]{
                new KeyValuePair(VALUE_ORDER_ATTR_DATE, activity.getString(R.string.filter_order_attribute_date)),
                new KeyValuePair(VALUE_ORDER_ATTR_SUBJECT, activity.getString(R.string.filter_order_attribute_subject)),
                new KeyValuePair(VALUE_ORDER_ATTR_APP, activity.getString(R.string.filter_order_attribute_app))
        };

        // Si el usuario tiene el rol de validador, ocultamos el checkbox de mostrar peticiones no validadas.
        if (role != null && role.equals(ConfigurationRole.VERIFIER)) {
            this.v.findViewById(R.id.textView5).setVisibility(View.GONE);
            CheckBox cb = this.v.findViewById(R.id.show_verifier_rq_filter);
            cb.setVisibility(View.GONE);
            cb.setEnabled(false);
        }

        ((Spinner) this.v.findViewById(R.id.spinner_order)).setAdapter(new KeyValueSpinnerAdapter(this.orderAdapterItems, activity));
        ((Spinner) this.v.findViewById(R.id.spinner_app)).setAdapter(new KeyValueSpinnerAdapter(appIds, appNames, activity));

        // Configuramos los campos con sus valores y comportamientos
        final boolean checked = bundle.getBoolean(FILTERS_ENABLED, false);
        final CheckBox cb = this.v.findViewById(R.id.cb_enable_filter);
        cb.setChecked(checked);
        final OnCheckedChangeListener listener = new FilterOptionCheckedListener(this.v);
        listener.onCheckedChanged(cb, checked);
        cb.setOnCheckedChangeListener(listener);

        final boolean unverified = bundle.getBoolean(FILTERS_SHOW_UNVERIFIED, false);
        final CheckBox sv = this.v.findViewById(R.id.show_verifier_rq_filter);
        sv.setChecked(unverified);
        this.filterConfig.setShowUnverified(unverified);

        final String userId = bundle.getString(FILTERS_USER_ID) != null ? bundle.getString(FILTERS_USER_ID) : "";
        this.filterConfig.setUserId(userId);
        final String userRole = bundle.getString(FILTERS_USER_ROLE) != null ? bundle.getString(FILTERS_USER_ROLE) : "";
        this.filterConfig.setUserRole(userRole);

        configureField((Spinner) this.v.findViewById(R.id.spinner_order), bundle.getString(FILTERS_ORDER_ATTRIBUTE), "setOrderAttribute"); //$NON-NLS-1$
        configureField((TextView) this.v.findViewById(R.id.et_filter_subject), bundle.getString(FILTERS_SUBJECT), "setSubject"); //$NON-NLS-1$
        configureField((Spinner) this.v.findViewById(R.id.spinner_app), bundle.getString(FILTERS_APP), "setApp"); //$NON-NLS-1$
        configureDateField((TextView) this.v.findViewById(R.id.et_filter_date_start), bundle.getString(FILTERS_DATE_START), "setDateStart", activity); //$NON-NLS-1$
        configureClearDateButton((Button) this.v.findViewById(R.id.bt_filter_date_start_clear), (TextView) this.v.findViewById(R.id.et_filter_date_start), "setDateStart"); //$NON-NLS-1$
        configureDateField((TextView) this.v.findViewById(R.id.et_filter_date_end), bundle.getString(FILTERS_DATE_END), "setDateEnd", activity); //$NON-NLS-1$
        configureClearDateButton((Button) this.v.findViewById(R.id.bt_filter_date_end_clear), (TextView) this.v.findViewById(R.id.et_filter_date_end), "setDateEnd"); //$NON-NLS-1$
        configureCheckBoxField((CheckBox) this.v.findViewById(R.id.show_verifier_rq_filter), bundle.getBoolean(FILTERS_SHOW_UNVERIFIED), "setShowUnverified"); //$NON-NLS-1$

        this.builder.setView(this.v).setTitle(R.string.title_configure_filter);
    }

    /**
     * Selecciona un elemento de un <i>Spinner</i>.
     *
     * @param spinner <i>Spinner</i> del que queremos seleccionar el elemento.
     * @param item    Texto del elemento que queremos seleccionar.
     */
    private static void selectSpinnerItem(final Spinner spinner, final String item) {
        if (item != null) {
            for (int i = 0; i < spinner.getCount(); i++) {
                if (item.equals(((KeyValuePair) spinner.getItemAtPosition(i)).getKey())) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }

    public static FilterConfig loadFilter(final Bundle savedInstanceState) {

        return new ConfigureFilterDialogBuilder.FilterConfig(
                savedInstanceState.getBoolean(FILTERS_ENABLED, false),
                savedInstanceState.getString(FILTERS_ORDER_ATTRIBUTE, null),
                savedInstanceState.getString(FILTERS_SUBJECT, null),
                savedInstanceState.getString(FILTERS_APP, null),
                savedInstanceState.getString(FILTERS_DATE_START, null),
                savedInstanceState.getString(FILTERS_DATE_END, null),
                savedInstanceState.getBoolean(FILTERS_SHOW_UNVERIFIED, false));
    }

    public static List<String> generateFilters(final FilterConfig config) {

        final List<String> filters = new ArrayList<>();
        if (config == null) {
            filters.add(KEY_ORDER_ATTR + VALUE_ORDER_ATTR_DATE);
            filters.add(KEY_ORDER + VALUE_ORDER_DESC);
        } else {
            String userId = "";
            String userRole = "";
            if (config.getUserId() != null) {
                userId = config.userId;
            }
            if (config.getUserRole() != null) {
                userRole = config.userRole;
            }
            filters.add(KEY_FILTER_USER_ID + userId);
            filters.add(KEY_FILTER_USER_ROLE + userRole);
            if (config.getOrderAttribute() == null) {
                filters.add(KEY_ORDER_ATTR + VALUE_ORDER_ATTR_DATE);
                filters.add(KEY_ORDER + VALUE_ORDER_DESC);
            } else {
                final String orderAttr = config.getOrderAttribute();
                filters.add(KEY_ORDER_ATTR + orderAttr);
                filters.add(KEY_ORDER + (VALUE_ORDER_ATTR_DATE.equals(orderAttr) ? VALUE_ORDER_DESC : VALUE_ORDER_ASC));
            }
            if (config.isEnabled()) {
                if (config.getSubject() != null) {
                    filters.add(KEY_FILTER_TEXT + config.getSubject());
                }
                if (config.getApp() != null && config.getApp().length() > 0) {
                    filters.add(KEY_FILTER_APP + config.getApp());
                }
                if (config.getDateStart() != null) {
                    filters.add(KEY_FILTER_DATE_START + config.getDateStart());
                }
                if (config.getDateEnd() != null) {
                    filters.add(KEY_FILTER_DATE_END + config.getDateEnd());
                }
                filters.add(KEY_FILTER_SHOW_UNVERIFIED + config.showUnverified);
            }
        }

        return filters;
    }

    /**
     * Método que almacena la lista de aplicaciones y la asocia a un identificador.
     *
     * @param apps Lista de aplicaciones a almacenar.
     */
    public static void updateApps(ArrayList<String> apps) {
        // reseteamos la variable, en caso de que tenga algún valor.
        if (!mApps.isEmpty()) {
            mApps = new HashMap<>();
        }
        // Almacenamos las aplicaciones en el map.
        for (int i = 0; i < apps.size(); i++) {
            mApps.put(apps.get(i), i);
        }
    }

    /**
     * Restablece los valores por defecto del di&aacute;logo de filtros.
     */
    public void resetLayout() {
        ((CheckBox) this.v.findViewById(R.id.cb_enable_filter)).setChecked(false);
        ((Spinner) this.v.findViewById(R.id.spinner_order)).setSelection(0);
        ((TextView) this.v.findViewById(R.id.et_filter_subject)).setText(""); //$NON-NLS-1$
        ((Spinner) this.v.findViewById(R.id.spinner_app)).setSelection(0);
        ((TextView) this.v.findViewById(R.id.et_filter_date_start)).setText(""); //$NON-NLS-1$
        ((TextView) this.v.findViewById(R.id.et_filter_date_end)).setText(""); //$NON-NLS-1$
        ((CheckBox) this.v.findViewById(R.id.show_verifier_rq_filter)).setChecked(false);
    }

    /**
     * Restablece los valores por los del objeto pasado como párametro del diálogo de filtros.
     */
    public void resetLayout(FilterConfig filterConfig) {
        if (filterConfig != null) {
            ((CheckBox) this.v.findViewById(R.id.cb_enable_filter)).setChecked(filterConfig.isEnabled());
            ((Spinner) this.v.findViewById(R.id.spinner_order)).setSelection(filterConfig.getOrderAttribute() != null ? CommonsUtils.getOrderAttrInteger(filterConfig.getOrderAttribute()) : 0);
            ((TextView) this.v.findViewById(R.id.et_filter_subject)).setText(filterConfig.getSubject() != null ? filterConfig.getSubject() : ""); //$NON-NLS-1$
            ((Spinner) this.v.findViewById(R.id.spinner_app)).setSelection(filterConfig.getApp() != null ? CommonsUtils.getApplicationAttrInteger(filterConfig.getApp()) : 0);
            ((TextView) this.v.findViewById(R.id.et_filter_date_start)).setText(filterConfig.getDateStart() != null ? filterConfig.getDateStart() : ""); //$NON-NLS-1$
            ((TextView) this.v.findViewById(R.id.et_filter_date_end)).setText(filterConfig.getDateEnd() != null ? filterConfig.getDateEnd() : ""); //$NON-NLS-1$
            ((CheckBox) this.v.findViewById(R.id.show_verifier_rq_filter)).setChecked(filterConfig.isEnabled());
        }
    }

    private void configureField(final TextView textView, final String defaultValue, final String methodName) {
        if (defaultValue != null) {
            textView.setText(defaultValue);
        }
        textView.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(final Editable s) {

                String value = s.toString();
                if (value.length() == 0) {
                    value = null;
                }
                try {
                    getFilterConfig().setSubject(value);
                } catch (final Exception e) {
                    PfLog.w(SFConstants.LOG_TAG, "No se ha podido configurar el valor del filtro con el metodo: " + methodName); //$NON-NLS-1$
                    e.printStackTrace();
                }
            }

            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) { /* No hacemos nada */ }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) { /* No hacemos nada */ }
        });
    }

    private void configureDateField(final TextView textView, final String defaultValue,
                                    final String methodName, final Context context) {
        if (defaultValue != null) {
            textView.setText(defaultValue);
        }

        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View arg0) {

                final OnDateSetListener listener = new OnDateSetListener() {

                    @Override
                    public void onDateSet(final DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {

                        final StringBuilder dateText = new StringBuilder();
                        dateText.append(dayOfMonth).append("/").append(monthOfYear + 1).append("/").append(year); //$NON-NLS-1$ //$NON-NLS-2$
                        try {
                            getFilterConfig().getClass().getDeclaredMethod(methodName, String.class)
                                    .invoke(getFilterConfig(), dateText.toString());
                        } catch (final Exception e) {
                            PfLog.w(SFConstants.LOG_TAG, "No se ha podido configurar el valor del filtro con el metodo: " + methodName); //$NON-NLS-1$
                            e.printStackTrace();
                        }

                        textView.setText(dateText.toString());
                    }
                };

                final Calendar today = Calendar.getInstance();
                new DatePickerDialog(context, listener,
                        today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)
                ).show();
            }
        });
    }

    private void configureClearDateButton(final Button button, final TextView textView, final String methodName) {

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View arg0) {

                textView.setText(""); //$NON-NLS-1$
                try {
                    getFilterConfig().getClass().getDeclaredMethod(methodName, String.class)
                            .invoke(getFilterConfig(), (String) null);
                } catch (final Exception e) {
                    PfLog.w(SFConstants.LOG_TAG, "No se ha podido configurar el comportamiento del boton de borrado con el metodo: " + methodName); //$NON-NLS-1$
                    e.printStackTrace();
                }
            }
        });
    }

    private void configureField(final Spinner spinner, final String defaultValue, final String methodName) {

        selectSpinnerItem(spinner, defaultValue);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> spnr, final View arg1, final int position, final long arg3) {

                try {
                    getFilterConfig().getClass().getDeclaredMethod(methodName, String.class)
                            .invoke(getFilterConfig(), ((KeyValuePair) spnr.getItemAtPosition(position)).getKey());
                } catch (final Exception e) {
                    PfLog.w(SFConstants.LOG_TAG, "No se ha podido configurar el valor del filtro correspondiente al spinner: " + spnr.getId()); //$NON-NLS-1$
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(final AdapterView<?> arg0) {
                // No hacemos nada
            }
        });
    }

    private void configureCheckBoxField(final CheckBox checkBox, final boolean value, final String methodName) {
        checkBox.setEnabled(value);
        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    getFilterConfig().getClass().getDeclaredMethod(methodName, boolean.class).invoke(getFilterConfig(), Boolean.valueOf(isChecked));
                } catch (Exception e) {
                    PfLog.e(SFConstants.LOG_TAG, "No ha sido posible modificar el valor del filtro correspondiente al checkBox" + buttonView.getId());
                    e.printStackTrace();
                }
            }
        });
    }

    public Dialog create() {
        return this.builder.create();
    }

    public void setPositiveButton(final int id, final DialogInterface.OnClickListener listener) {
        this.builder.setPositiveButton(id, listener);
    }

    public void setNegativeButton(final int id, final DialogInterface.OnClickListener listener) {
        this.builder.setNegativeButton(id, listener);
    }

    public FilterConfig getFilterConfig() {
        return this.filterConfig;
    }

    public static final class FilterConfig {
        private boolean enabled;
        private String orderAttribute;
        private String subject;
        private String app;
        private String dateStart;
        private String dateEnd;
        private boolean showUnverified;
        private String userId;
        private String userRole;

        public FilterConfig() {
            reset();
        }

        FilterConfig(final boolean enabled, final String orderAttribute, final String subject, final String app, final String dateStart, final String dateEnd, final boolean showUnverified) {
            this.enabled = enabled;
            this.orderAttribute = orderAttribute;
            this.subject = subject;
            this.app = app;
            this.dateStart = dateStart;
            this.dateEnd = dateEnd;
            this.showUnverified = showUnverified;
        }

        public static boolean isDefaultConfig(final FilterConfig config) {

            return config == null ||
                    !config.enabled &&
                            (config.orderAttribute == null || DEFAULT_VALUE_ORDER_ATTR.equals(config.orderAttribute)) &&
                            (config.subject == null || config.subject.length() == 0) &&
                            config.app == null &&
                            (config.dateStart == null || config.dateStart.length() == 0) &&
                            (config.dateEnd == null || config.dateEnd.length() == 0) &&
                            !config.showUnverified;
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public String getOrderAttribute() {
            return this.orderAttribute;
        }

        public void setOrderAttribute(final String orderAttribute) {
            this.orderAttribute = orderAttribute;
        }

        public String getSubject() {
            return this.subject;
        }

        public void setSubject(final String subject) {
            this.subject = subject;
        }

        public String getApp() {
            return this.app;
        }

        public void setApp(final String app) {
            this.app = app;
        }

        public String getDateStart() {
            return this.dateStart;
        }

        public void setDateStart(final String dateStart) {
            this.dateStart = dateStart;
        }

        public String getDateEnd() {
            return this.dateEnd;
        }

        public void setDateEnd(final String dateEnd) {
            this.dateEnd = dateEnd;
        }

        public boolean isShowUnverified() {
            return showUnverified;
        }

        public void setShowUnverified(boolean showUnverified) {
            this.showUnverified = showUnverified;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserRole() {
            return userRole;
        }

        public void setUserRole(String userRole) {
            this.userRole = userRole;
        }

        public FilterConfig reset() {
            this.enabled = false;
            this.orderAttribute = null;
            this.subject = null;
            this.app = null;
            this.dateStart = null;
            this.dateEnd = null;
            this.showUnverified = false;

            return this;
        }

        /**
         * Agrega a un <i>Bundle</i> la configuraci&oacute;n del filtro. Si se pasa {@code null}, se crea un nuevo
         * <i>Bundle</i> con esta configuraci&oacute;n.
         *
         * @param bundle <i>Bundle</i> en donde insertar los datos o null.
         * @return <i>Bundle</i> actualizado.
         */
        public Bundle copyToBundle(final Bundle bundle) {

            Bundle newBundle = bundle;
            if (bundle == null) {
                newBundle = new Bundle();
            }
            newBundle.putBoolean(ConfigureFilterDialogBuilder.FILTERS_ENABLED, this.enabled);
            newBundle.putString(ConfigureFilterDialogBuilder.FILTERS_ORDER_ATTRIBUTE, this.orderAttribute);
            newBundle.putString(ConfigureFilterDialogBuilder.FILTERS_SUBJECT, this.subject);
            newBundle.putString(ConfigureFilterDialogBuilder.FILTERS_APP, this.app);
            newBundle.putString(ConfigureFilterDialogBuilder.FILTERS_DATE_START, this.dateStart);
            newBundle.putString(ConfigureFilterDialogBuilder.FILTERS_DATE_END, this.dateEnd);
            newBundle.putBoolean(ConfigureFilterDialogBuilder.FILTERS_SHOW_UNVERIFIED, this.showUnverified);
            newBundle.putString(ConfigureFilterDialogBuilder.FILTERS_USER_ID, this.userId);
            newBundle.putString(ConfigureFilterDialogBuilder.FILTERS_USER_ROLE, this.userRole);

            return newBundle;
        }
    }

    /**
     * Clase para activar y desactivar las opciones de configuraci&oacute;n de filtros en el
     * di&aacute;logo de filtrado.
     */

    final class FilterOptionCheckedListener implements OnCheckedChangeListener {

        final View parentView;
        private final int[] DIALOG_ENABLED_RESOURCE_IDS = new int[]{
                R.id.lb_filter_subject,
                R.id.lb_filter_apps,
                R.id.lb_filter_date_start,
                R.id.lb_filter_date_end,
                R.id.et_filter_subject,
                R.id.spinner_app,
                R.id.et_filter_date_start,
                R.id.et_filter_date_end,
                R.id.textView5,
                R.id.show_verifier_rq_filter
        };

        FilterOptionCheckedListener(final View parentView) {
            this.parentView = parentView;
        }

        @Override
        public void onCheckedChanged(final CompoundButton buttonView, final boolean checked) {

            for (final int id : this.DIALOG_ENABLED_RESOURCE_IDS) {
                final View view = this.parentView.findViewById(id);
                if (view != null) {
                    view.setEnabled(checked);
                }
            }

            try {
                getFilterConfig().getClass().getDeclaredMethod("setEnabled", Boolean.TYPE) //$NON-NLS-1$
                        .invoke(getFilterConfig(), Boolean.valueOf(checked));
            } catch (final Exception e) {
                PfLog.w(SFConstants.LOG_TAG, "No se ha podido configurar el valor de la propiedad de activacion de filtros"); //$NON-NLS-1$
                e.printStackTrace();
            }
        }
    }

    /**
     * Adaptador para el <i>Spinner</i> de aplicaciones.
     */

    final class KeyValueSpinnerAdapter extends ArrayAdapter<KeyValuePair> {

        KeyValueSpinnerAdapter(final KeyValuePair[] items, final Context context) {
            super(context, android.R.layout.simple_spinner_item, items);
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        KeyValueSpinnerAdapter(final String[] ids, final String[] names, final Context context) {
            super(context, android.R.layout.simple_spinner_item);
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            if (ids != null && names != null) {
                for (int i = 0; i < ids.length; i++) {
                    if (ids[i] != null && names[i] != null) {
                        super.add(new KeyValuePair(ids[i], names[i]));
                    }
                }
            }
        }
    }

    private final class KeyValuePair extends Pair<String, String> {

        KeyValuePair(final String key, final String value) {
            super(key, value);
        }

        String getKey() {
            return this.first;
        }

        @Override
        public String toString() {
            return this.second;
        }
    }
}
