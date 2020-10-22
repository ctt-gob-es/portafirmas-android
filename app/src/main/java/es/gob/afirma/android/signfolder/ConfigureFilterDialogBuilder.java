package es.gob.afirma.android.signfolder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
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
    static final String FILTERS_SHOW_UNVERIFIED = "filters_show_unverified"; //$NON-NLS-1$
    static final String FILTERS_USER_ID = "filters_user_ID"; //$NON-NLS-1$
    static final String FILTERS_USER_ROLE = "filters_user_role"; //$NON-NLS-1$
    static final String FILTERS_OWNER_ID = "filter_owner_id"; //$NON-NLS-1$
    static final String FILTERS_APP_TYPE = "filters_app_type"; //$NON-NLS-1$
    static final String FILTERS_MONTH = "filters_month"; //$NON-NLS-1$
    static final String FILTERS_YEAR = "filters_year"; //$NON-NLS-1$

    private static final String KEY_ORDER = "orderAscDesc="; //$NON-NLS-1$
    private static final String VALUE_ORDER_DESC = "desc"; //$NON-NLS-1$
    private static final String VALUE_ORDER_ASC = "asc"; //$NON-NLS-1$

    private static final String KEY_ORDER_ATTR = "orderAttribute="; //$NON-NLS-1$
    private static final String VALUE_ORDER_ATTR_DATE = "fmodified"; //$NON-NLS-1$
    private static final String VALUE_ORDER_ATTR_SUBJECT = "dsubject"; //$NON-NLS-1$
    private static final String VALUE_ORDER_ATTR_APP = "application"; //$NON-NLS-1$

    private static final String DEFAULT_VALUE_ORDER_ATTR = VALUE_ORDER_ATTR_DATE;

    private static final String KEY_FILTER_APP_FILTER = "tipoFilter="; //$NON-NLS-1$
    private static final String VALUE_APP_TYPE_VIEW_ALL = "view_all"; //$NON-NLS-1$
    private static final String VALUE_APP_TYPE_VIEW_SIGN = "view_sign"; //$NON-NLS-1$
    private static final String VALUE_APP_TYPE_VIEW_PASS = "view_pass"; //$NON-NLS-1$
    private static final String VALUE_APP_TYPE_VIEW_VALIDATE = "view_validate"; //$NON-NLS-1$
    private static final String VALUE_APP_TYPE_VIEW_NO_VALIDATE = "view_no_validate"; //$NON-NLS-1$

    private static final String KEY_FILTER_MONTH = "mesFilter="; //$NON-NLS-1$
    private static final String VALUE_MONTH_ALL = "all"; //$NON-NLS-1$
    private static final String VALUE_MONTH_LAST_24_HOURS = "last24Hours"; //$NON-NLS-1$
    private static final String VALUE_MONTH_LAST_WEEK = "lastWeek"; //$NON-NLS-1$
    private static final String VALUE_MONTH_LAST_MONTH = "lastMonth"; //$NON-NLS-1$
    private static final String VALUE_MONTH_1 = "1"; //$NON-NLS-1$
    private static final String VALUE_MONTH_2 = "2"; //$NON-NLS-1$
    private static final String VALUE_MONTH_3 = "3"; //$NON-NLS-1$
    private static final String VALUE_MONTH_4 = "4"; //$NON-NLS-1$
    private static final String VALUE_MONTH_5 = "5"; //$NON-NLS-1$
    private static final String VALUE_MONTH_6 = "6"; //$NON-NLS-1$
    private static final String VALUE_MONTH_7 = "7"; //$NON-NLS-1$
    private static final String VALUE_MONTH_8 = "8"; //$NON-NLS-1$
    private static final String VALUE_MONTH_9 = "9"; //$NON-NLS-1$
    private static final String VALUE_MONTH_10 = "10"; //$NON-NLS-1$
    private static final String VALUE_MONTH_11 = "11"; //$NON-NLS-1$
    private static final String VALUE_MONTH_12 = "12"; //$NON-NLS-1$

    private static final String KEY_FILTER_YEAR = "anioFilter="; //$NON-NLS-1$
    private static final int VALUE_YEAR_BASE = 2010;

    private static final String KEY_FILTER_TEXT = "searchFilter="; //$NON-NLS-1$
    private static final String KEY_FILTER_APP = "applicationFilter="; //$NON-NLS-1$

    private static final String KEY_FILTER_VERIFIER_DNI = "dniValidadorFilter="; //$NON-NLS-1$
    private static final String KEY_FILTER_SHOW_UNVERIFIED = "showUnverified="; //$NON-NLS-1$
    private static final String KEY_FILTER_USER_ID = "userId="; //$NON-NLS-1$
    private static final String KEY_FILTER_USER_ROLE = "userRole="; //$NON-NLS-1$

    /**
     * Static attribute that represents the set of applications showed in the filter spiner of applications.
     */
    protected static Map<String, Integer> mApps = new HashMap<>();
    private final KeyValuePair[] months;
    private final AlertDialog.Builder builder;
    private final View v;
    private final Map<String, Integer> mAppsTypes = new HashMap<>();
    private final Map<String, Integer> mMonths = new HashMap<>();
    private final Map<String, Integer> mYears = new HashMap<>();
    private boolean avoidFirstCall;
    private final FilterConfig filterConfig;

    public ConfigureFilterDialogBuilder(final Bundle bundle, final String[] appIds, final String[] appNames, final ConfigurationRole role, final Activity activity) {

        this.filterConfig = new FilterConfig();
        avoidFirstCall = true;

        this.builder = new AlertDialog.Builder(activity);
        final LayoutInflater inflater = activity.getLayoutInflater();

        // Establecemos el layout del dialogo
        this.v = inflater.inflate(R.layout.activity_filters_configuration, null);

        // Inicializamos los listados de configuracion
        KeyValuePair[] orderAdapterItems = new KeyValuePair[]{
                new KeyValuePair(VALUE_ORDER_ATTR_DATE, activity.getString(R.string.filter_order_attribute_date)),
                new KeyValuePair(VALUE_ORDER_ATTR_SUBJECT, activity.getString(R.string.filter_order_attribute_subject)),
                new KeyValuePair(VALUE_ORDER_ATTR_APP, activity.getString(R.string.filter_order_attribute_app))
        };
        KeyValuePair[] appTypes = new KeyValuePair[]{
                new KeyValuePair(VALUE_APP_TYPE_VIEW_ALL, activity.getString(R.string.filter_app_type_view_all)),
                new KeyValuePair(VALUE_APP_TYPE_VIEW_SIGN, activity.getString(R.string.filter_app_type_view_sign)),
                new KeyValuePair(VALUE_APP_TYPE_VIEW_PASS, activity.getString(R.string.filter_app_type_view_pass)),
                new KeyValuePair(VALUE_APP_TYPE_VIEW_VALIDATE, activity.getString(R.string.filter_app_type_view_validate)),
                new KeyValuePair(VALUE_APP_TYPE_VIEW_NO_VALIDATE, activity.getString(R.string.filter_app_type_view_no_validate))
        };
        this.months = new KeyValuePair[]{
                new KeyValuePair(VALUE_MONTH_ALL, activity.getString(R.string.filter_month_all)),
                new KeyValuePair(VALUE_MONTH_LAST_24_HOURS, activity.getString(R.string.filter_month_last_hours)),
                new KeyValuePair(VALUE_MONTH_LAST_WEEK, activity.getString(R.string.filter_month_last_week)),
                new KeyValuePair(VALUE_MONTH_LAST_MONTH, activity.getString(R.string.filter_month_last_month)),
                new KeyValuePair(VALUE_MONTH_1, activity.getString(R.string.filter_month_1)),
                new KeyValuePair(VALUE_MONTH_2, activity.getString(R.string.filter_month_2)),
                new KeyValuePair(VALUE_MONTH_3, activity.getString(R.string.filter_month_3)),
                new KeyValuePair(VALUE_MONTH_4, activity.getString(R.string.filter_month_4)),
                new KeyValuePair(VALUE_MONTH_5, activity.getString(R.string.filter_month_5)),
                new KeyValuePair(VALUE_MONTH_6, activity.getString(R.string.filter_month_6)),
                new KeyValuePair(VALUE_MONTH_7, activity.getString(R.string.filter_month_7)),
                new KeyValuePair(VALUE_MONTH_8, activity.getString(R.string.filter_month_8)),
                new KeyValuePair(VALUE_MONTH_9, activity.getString(R.string.filter_month_9)),
                new KeyValuePair(VALUE_MONTH_10, activity.getString(R.string.filter_month_10)),
                new KeyValuePair(VALUE_MONTH_11, activity.getString(R.string.filter_month_11)),
                new KeyValuePair(VALUE_MONTH_12, activity.getString(R.string.filter_month_12))
        };
        int year = Calendar.getInstance().get(Calendar.YEAR);
        KeyValuePair[] years = new KeyValuePair[year - VALUE_YEAR_BASE + 1];
        for (int i = VALUE_YEAR_BASE; i <= year; i++) {
            years[i - VALUE_YEAR_BASE] = new KeyValuePair(String.valueOf(i), String.valueOf(i));
            mYears.put(String.valueOf(i),i - VALUE_YEAR_BASE );
        }

        //Inicializamos los maps que contienen los pares clave/valor de los spinners.
        mAppsTypes.put(VALUE_APP_TYPE_VIEW_ALL, 0);
        mAppsTypes.put(VALUE_APP_TYPE_VIEW_SIGN, 1);
        mAppsTypes.put(VALUE_APP_TYPE_VIEW_PASS, 2);
        mAppsTypes.put(VALUE_APP_TYPE_VIEW_VALIDATE, 3);
        mAppsTypes.put(VALUE_APP_TYPE_VIEW_NO_VALIDATE, 4);

        mMonths.put(activity.getString(R.string.filter_month_all), 0);
        mMonths.put(activity.getString(R.string.filter_month_last_hours), 1);
        mMonths.put(activity.getString(R.string.filter_month_last_week), 2);
        mMonths.put(activity.getString(R.string.filter_month_last_month), 3);
        mMonths.put(activity.getString(R.string.filter_month_1), 4);
        mMonths.put(activity.getString(R.string.filter_month_2), 5);
        mMonths.put(activity.getString(R.string.filter_month_3), 6);
        mMonths.put(activity.getString(R.string.filter_month_4), 7);
        mMonths.put(activity.getString(R.string.filter_month_5), 8);
        mMonths.put(activity.getString(R.string.filter_month_6), 9);
        mMonths.put(activity.getString(R.string.filter_month_7), 10);
        mMonths.put(activity.getString(R.string.filter_month_8), 11);
        mMonths.put(activity.getString(R.string.filter_month_9), 12);
        mMonths.put(activity.getString(R.string.filter_month_10), 13);
        mMonths.put(activity.getString(R.string.filter_month_11), 14);
        mMonths.put(activity.getString(R.string.filter_month_12), 15);

        // Si el usuario tiene el rol de validador, ocultamos el checkbox de mostrar peticiones no validadas
        // y recuperamos el dni del validador.
        if (role != null && role.equals(ConfigurationRole.VERIFIER)) {
            this.v.findViewById(R.id.textView5).setVisibility(View.GONE);
            CheckBox cb = this.v.findViewById(R.id.show_verifier_rq_filter);
            cb.setVisibility(View.GONE);
            cb.setEnabled(false);

            final String verifierId = bundle.getString(FILTERS_OWNER_ID) != null ? bundle.getString(FILTERS_OWNER_ID) : "";
            this.filterConfig.setOwnerId(verifierId);
        }

        ((Spinner) this.v.findViewById(R.id.spinner_order)).setAdapter(new KeyValueSpinnerAdapter(orderAdapterItems, activity));
        ((Spinner) this.v.findViewById(R.id.spinner_app)).setAdapter(new KeyValueSpinnerAdapter(appIds, appNames, activity));
        ((Spinner) this.v.findViewById(R.id.spinner_type)).setAdapter(new KeyValueSpinnerAdapter(appTypes, activity));
        ((Spinner) this.v.findViewById(R.id.spinner_month)).setAdapter(new KeyValueSpinnerAdapter(this.months, activity));
        ((Spinner) this.v.findViewById(R.id.spinner_year)).setAdapter(new KeyValueSpinnerAdapter(years, activity));
        ((Spinner) this.v.findViewById(R.id.spinner_month)).setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (avoidFirstCall) {
                    avoidFirstCall = false;
                } else if (months[position].getKey().equals(VALUE_MONTH_ALL) ||
                        months[position].getKey().equals(VALUE_MONTH_LAST_24_HOURS) ||
                        months[position].getKey().equals(VALUE_MONTH_LAST_WEEK) ||
                        months[position].getKey().equals(VALUE_MONTH_LAST_MONTH)) {
                    view.getRootView().findViewById(R.id.lb_filter_year).setVisibility(View.INVISIBLE);
                    view.getRootView().findViewById(R.id.spinner_year).setVisibility(View.INVISIBLE);
                    view.getRootView().findViewById(R.id.spinner_year).setEnabled(false);
                    filterConfig.setMonth(months[position].getKey());
                } else {
                    view.getRootView().findViewById(R.id.lb_filter_year).setVisibility(View.VISIBLE);
                    view.getRootView().findViewById(R.id.spinner_year).setVisibility(View.VISIBLE);
                    view.getRootView().findViewById(R.id.spinner_year).setEnabled(true);
                    filterConfig.setMonth(months[position].getKey());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hace nada
            }
        });

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
        configureField((Spinner) this.v.findViewById(R.id.spinner_type), bundle.getString(FILTERS_APP_TYPE), "setAppType"); //$NON-NLS-1$
        configureField((Spinner) this.v.findViewById(R.id.spinner_year), bundle.getString(FILTERS_YEAR), "setYear"); //$NON-NLS-1$
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
                savedInstanceState.getString(FILTERS_APP_TYPE, null),
                savedInstanceState.getString(FILTERS_MONTH, null),
                savedInstanceState.getString(FILTERS_YEAR, null),
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
            String ownerId = "";
            if (config.getUserId() != null) {
                userId = config.userId;
            }
            if (config.getUserRole() != null) {
                userRole = config.userRole;
            }
            if (config.getOwnerId() != null) {
                ownerId = config.ownerId;
            }
            filters.add(KEY_FILTER_USER_ID + userId);
            filters.add(KEY_FILTER_USER_ROLE + userRole);
            filters.add(KEY_FILTER_VERIFIER_DNI + ownerId);
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
                if (config.getAppType() != null && config.getAppType().length() > 0) {
                    filters.add(KEY_FILTER_APP_FILTER + config.getAppType());
                }
                if (config.getMonth() != null && config.getMonth().length() > 0) {
                    filters.add(KEY_FILTER_MONTH + config.getMonth());
                }
                if (config.getYear() != null && config.getYear().length() > 0) {
                    filters.add(KEY_FILTER_YEAR + config.getYear());
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
        ((Spinner) this.v.findViewById(R.id.spinner_type)).setSelection(0);
        ((Spinner) this.v.findViewById(R.id.spinner_month)).setSelection(0);
        ((Spinner) this.v.findViewById(R.id.spinner_year)).setSelection(0);
        ((CheckBox) this.v.findViewById(R.id.show_verifier_rq_filter)).setChecked(false);
    }

    /**+
     * Restablece los valores por los del objeto pasado como párametro del diálogo de filtros.
     */
    public void resetLayout(FilterConfig filterConfig) {
        if (filterConfig != null) {
            ((CheckBox) this.v.findViewById(R.id.cb_enable_filter)).setChecked(filterConfig.isEnabled());
            ((Spinner) this.v.findViewById(R.id.spinner_order)).setSelection(filterConfig.getOrderAttribute() != null ? CommonsUtils.getOrderAttrInteger(filterConfig.getOrderAttribute()) : 0);
            ((TextView) this.v.findViewById(R.id.et_filter_subject)).setText(filterConfig.getSubject() != null ? filterConfig.getSubject() : ""); //$NON-NLS-1$
            ((Spinner) this.v.findViewById(R.id.spinner_app)).setSelection(filterConfig.getApp() != null ? CommonsUtils.getApplicationAttrInteger(filterConfig.getApp()) : 0);
            ((Spinner) this.v.findViewById(R.id.spinner_type)).setSelection(filterConfig.getAppType() != null ? mAppsTypes.get(filterConfig.getAppType()) : 0);
            ((Spinner) this.v.findViewById(R.id.spinner_month)).setSelection(filterConfig.getMonth() != null ? mMonths.get(filterConfig.getMonth()) : 0);
            ((Spinner) this.v.findViewById(R.id.spinner_year)).setSelection(filterConfig.getYear() != null ? mYears.get(filterConfig.getYear()) : 0);
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
                    getFilterConfig().getClass().getDeclaredMethod(methodName, boolean.class).invoke(getFilterConfig(), isChecked);
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
        private String appType;
        private String month;
        private String year;
        private boolean showUnverified;
        private String userId;
        private String userRole;
        private String ownerId;

        public FilterConfig() {
            reset();
        }

        FilterConfig(final boolean enabled, final String orderAttribute, final String subject, final String app, final String appType, final String month, final String year, final boolean showUnverified) {
            this.enabled = enabled;
            this.orderAttribute = orderAttribute;
            this.subject = subject;
            this.app = app;
            this.appType = appType;
            this.month = month;
            this.year = year;
            this.showUnverified = showUnverified;
        }

        public static boolean isDefaultConfig(final FilterConfig config) {

            return config == null ||
                    !config.enabled &&
                            (config.orderAttribute == null || DEFAULT_VALUE_ORDER_ATTR.equals(config.orderAttribute)) &&
                            (config.subject == null || config.subject.length() == 0) &&
                            config.app == null && config.appType == null &&
                            config.month == null && config.year == null &&
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

        public String getAppType() {
            return appType;
        }

        public void setAppType(final String appType) {
            this.appType = appType;
        }

        public String getMonth() {
            return month;
        }

        public void setMonth(final String month) {
            this.month = month;
        }

        public String getYear() {
            return year;
        }

        public void setYear(final String year) {
            this.year = year;
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

        public String getOwnerId() {
            return ownerId;
        }

        public void setOwnerId(String ownerId) {
            this.ownerId = ownerId;
        }

        public FilterConfig reset() {
            this.enabled = false;
            this.orderAttribute = null;
            this.subject = null;
            this.app = null;
            this.appType = null;
            this.month = null;
            this.year = null;
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
            newBundle.putBoolean(FILTERS_ENABLED, this.enabled);
            newBundle.putString(FILTERS_ORDER_ATTRIBUTE, this.orderAttribute);
            newBundle.putString(FILTERS_SUBJECT, this.subject);
            newBundle.putString(FILTERS_APP, this.app);
            newBundle.putString(FILTERS_APP_TYPE, this.appType);
            newBundle.putString(FILTERS_MONTH, this.month);
            newBundle.putString(FILTERS_YEAR, this.year);
            newBundle.putBoolean(FILTERS_SHOW_UNVERIFIED, this.showUnverified);
            newBundle.putString(FILTERS_USER_ID, this.userId);
            newBundle.putString(FILTERS_USER_ROLE, this.userRole);
            newBundle.putString(FILTERS_OWNER_ID, this.ownerId);

            return newBundle;
        }
    }

    /**
     * Clase para activar y desactivar las opciones de configuraci&oacute;n de filtros en el
     * di&aacute;logo de filtrado.
     */
    final class FilterOptionCheckedListener implements CompoundButton.OnCheckedChangeListener {

        final View parentView;
        private final int[] DIALOG_ENABLED_RESOURCE_IDS = new int[]{
                R.id.lb_filter_subject,
                R.id.lb_filter_apps,
                R.id.lb_filter_type,
                R.id.lb_filter_month,
                R.id.lb_filter_year,
                R.id.et_filter_subject,
                R.id.spinner_app,
                R.id.spinner_type,
                R.id.spinner_month,
                R.id.spinner_year,
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
                        .invoke(getFilterConfig(), checked);
            } catch (final Exception e) {
                PfLog.w(SFConstants.LOG_TAG, "No se ha podido configurar el valor de la propiedad de activacion de filtros"); //$NON-NLS-1$
                e.printStackTrace();
            }
        }
    }

    /**
     * Adaptador para el <i>Spinner</i> de aplicaciones.
     */

    static final class KeyValueSpinnerAdapter extends ArrayAdapter<KeyValuePair> {

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

    private static final class KeyValuePair extends Pair<String, String> {

        KeyValuePair(final String key, final String value) {
            super(key, value);
        }

        String getKey() {
            return this.first;
        }

        @NonNull
        @Override
        public String toString() {
            return this.second;
        }
    }
}
