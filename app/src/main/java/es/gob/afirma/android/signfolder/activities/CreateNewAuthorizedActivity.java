package es.gob.afirma.android.signfolder.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.Calendar;
import java.util.Date;

import es.gob.afirma.android.signfolder.DateTimeFormatter;
import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.proxy.GenericResponse;
import es.gob.afirma.android.signfolder.tasks.CreateAuthorizationTask;
import es.gob.afirma.android.user.configuration.Authorization;
import es.gob.afirma.android.user.configuration.AuthorizedType;
import es.gob.afirma.android.user.configuration.ConfigurationConstants;
import es.gob.afirma.android.user.configuration.GenericUser;
import es.gob.afirma.android.util.PfLog;

/**
 * Clase que gestiona la actividad asociada a la creación de nuevas autorizaciones.
 */
public class CreateNewAuthorizedActivity extends Activity implements CreateAuthorizationTask.SaveAuthorizationListener {

    /** Resultado de error por no haber indicado el usuario al que se le desea la autorización. */
    public static final int RESULT_NO_USER = 1;

    /** C&oacute;digo de referencia del resultado de la operacion de creaci&oacute;n. */
    public static final String EXTRA_RESPONSE = "resp";

    /** Usuario al que enviar la autorización. */
    private GenericUser authorizedUser;

    /** Fecha inicial seleccionada. */
    private Calendar initDateTime = null;

    /** Fecha final seleccionada. */
    private Calendar endDateTime = null;

    /** Campo de texto para la fecha inicial (dia, mes y año). */
    private TextView initDateTextView;

    /** Campo de texto para la fecha final (dia, mes y año). */
    private TextView endDateTextView;

    /** Campo de texto para la hora inicial (horas y minutos). */
    private TextView initTimeTextView;

    /** Campo de texto para la hora final (horas y minutos). */
    private TextView endTimeTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Recuperamos los parámetros del usuario seleccionado.
        Bundle userBundle = getIntent().getBundleExtra(ConfigurationConstants.EXTRA_RESOURCE_USER_INFO);
        this.authorizedUser = GenericUser.fromBundle(userBundle);

        // Si no se ha proporcionado el usuario la que dar de alta, se cancela la operacion
        if (this.authorizedUser == null) {
            setResult(RESULT_NO_USER);
            finish();
            return;
        }

        // Restauramos el valor por defecto del resultado a devolver por la actividad.
        setResult(ConfigurationConstants.ACTIVITY_RESULT_CODE_NONE);

        // Mostramos la vista.
        setContentView(R.layout.activity_create_new_authorized);

        // Mostramos el usuario seleccionado.
        String name = this.authorizedUser.getName() != null ? this.authorizedUser.getName() : "-";
        ((TextView) findViewById(R.id.nameFieldValueId)).setText(name);

        // Configuramos los pickers asociados a los campos de fechas.
        setupDateFields();

        // Configuramos el comportamiento de los botones de la actividad.
        setupButtons();
    }

    /**
     * Método que configura los pickets de fechas de la vista.
     */
    private void setupDateFields() {
        initDateTextView = findViewById(R.id.initDateText);
        initTimeTextView = findViewById(R.id.initTimeText);
        ImageButton initDateBtn = findViewById(R.id.imageInitDateButton);
        ImageButton initTimeBtn = findViewById(R.id.imageInitTimeButton);
        endDateTextView = findViewById(R.id.endDateText);
        endTimeTextView = findViewById(R.id.endTimeText);
        ImageButton endDateBtn = findViewById(R.id.imageEndDateButton);
        ImageButton endTimeBtn = findViewById(R.id.imageEndTimeButton);

        initDateTextView.setOnClickListener(initDateListener);
        initDateBtn.setOnClickListener(initDateListener);

        initTimeTextView.setOnClickListener(initTimeListener);
        initTimeBtn.setOnClickListener(initTimeListener);

        endDateTextView.setOnClickListener(endDateListener);
        endDateBtn.setOnClickListener(endDateListener);

        endTimeTextView.setOnClickListener(endTimeListener);
        endTimeBtn.setOnClickListener(endTimeListener);
    }

    /**
     * Método que configura el comportamiento de los botones de la vista.
     */
    private void setupButtons() {

        this.findViewById(R.id.finishButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValues()) {
                    try {
                        final Authorization auth = getAuthorizationFieldsValues();
                        final CreateAuthorizationTask task = new CreateAuthorizationTask(auth, CreateNewAuthorizedActivity.this);
                        task.execute();
                    } catch (Exception e) {
                        PfLog.e(SFConstants.LOG_TAG, "Se ha producido un error durante la creación de la autorización", e);
                        errorSavingAuthorization(null);
                    }
                } else {
                    Toast toast = Toast.makeText(CreateNewAuthorizedActivity.this,
                            R.string.error_input_params_creation_role, Toast.LENGTH_LONG);
                    toast.getView().setBackgroundColor(getResources().getColor(R.color.lightRed));
                    toast.show();
                }
            }
        });
    }

    /**
     * Método auxiliar que almacena los valores de los campos del formulario en el objeto de tipo AuthorizedUser.
     *
     * @return un nuevo objeto AuthorizedUser con los nuevos valores.
     */
    private Authorization getAuthorizationFieldsValues() {
        Authorization auth = new Authorization();
        auth.setAuthoricedUser(this.authorizedUser);
        if (initDateTime != null) {
            auth.setStartDate(initDateTime.getTime());
        }
        if (endDateTime != null) {
            auth.setRevDate(endDateTime.getTime());
        }
        RadioGroup rg = this.findViewById(R.id.radioGroupId);
        if (R.id.radioButtonDelegate == rg.getCheckedRadioButtonId()) {
            auth.setType(AuthorizedType.DELEGATE);
        } else if (R.id.radioButtonSubstitute == rg.getCheckedRadioButtonId()) {
            auth.setType(AuthorizedType.SUBSTITUTE);
        }
        EditText et = this.findViewById(R.id.editTextTextMultiLine);
        String observations = et.getText() != null ? et.getText().toString() : null;
        auth.setObservations(observations);
        return auth;
    }

    /**
     * Método que actualiza la fecha (dia, mes y año) inicial mostrada.
     */
    private void updateInitDateText() {
        initDateTextView.setText(DateTimeFormatter.getDateFormatterInstance().format(initDateTime.getTime()));
    }

    /**
     * Método que actualiza la fecha (horas y minutos) inicial mostrada.
     */
    private void updateInitTimeText() {
        String time = DateTimeFormatter.getTimeFormatterInstance().format(initDateTime.getTime());
        initTimeTextView.setText(time);
    }

    /**
     * Método que actualiza la fecha (dia, mes y año) final mostrada.
     */
    private void updateEndDateText() {
        endDateTextView.setText(DateTimeFormatter.getDateFormatterInstance().format(endDateTime.getTime()));
    }

    /**
     * Método que actualiza la fecha (horas y minutos) final mostrada.
     */
    private void updateEndTimeText() {
        String time = DateTimeFormatter.getTimeFormatterInstance().format(endDateTime.getTime());
        endTimeTextView.setText(time);
    }

    /**
     * Método que comprueba que los valores del formulario asociado a las fechas son válidos.
     * @return <i>True</i> si los valores son correctos y <i>False</i> en caso contrario.
     */
    private boolean checkValues() {
        boolean res = true;
        if (endDateTime != null) {
            Date currentDate = new Date();
            res = endDateTime.getTime().after(currentDate);
            if (initDateTime != null) {
                res = res && initDateTime.before(endDateTime);
            }
        }
        return res;
    }

    @Override
    public void authorizationSaved() {
        setResult(ConfigurationConstants.ACTIVITY_RESULT_CODE_AUTH_ROLE_OK);
        finish();
    }

    @Override
    public void errorSavingAuthorization(GenericResponse errorResponse) {
        Intent data = null;
        if (errorResponse != null) {
            data = new Intent();
            data.putExtra(EXTRA_RESPONSE, errorResponse);
        }
        setResult(ConfigurationConstants.ACTIVITY_RESULT_CODE_AUTH_ROLE_KO, data);
        finish();
    }

    private final View.OnClickListener initDateListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Calendar calendar = initDateTime != null ? initDateTime : Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(CreateNewAuthorizedActivity.this,
                    initDatePickerListener, calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            dpd.getDatePicker().setMinDate(new Date().getTime() - 10000);
            dpd.show();
        }
    };

    private final DatePickerDialog.OnDateSetListener initDatePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            if (initDateTime == null) {
                initDateTime = Calendar.getInstance();
            }
            initDateTime.set(Calendar.YEAR, year);
            initDateTime.set(Calendar.MONTH, monthOfYear);
            initDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateInitDateText();
        }
    };

    private final View.OnClickListener initTimeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Calendar calendar = initDateTime != null ? initDateTime : Calendar.getInstance();
            new TimePickerDialog(CreateNewAuthorizedActivity.this, initTimePickerListener,
                    calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
                    true).show();
        }
    };

    private final TimePickerDialog.OnTimeSetListener initTimePickerListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            if (initDateTime == null) {
                initDateTime = Calendar.getInstance();
            }
            initDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            initDateTime.set(Calendar.MINUTE, minute);
            updateInitTimeText();
        }
    };

    private final View.OnClickListener endDateListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Calendar calendar = endDateTime != null ? endDateTime : Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(CreateNewAuthorizedActivity.this,
                    endDatePickerListener,
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            dpd.getDatePicker().setMinDate(new Date().getTime() - 10000);
            dpd.show();
        }
    };

    private final DatePickerDialog.OnDateSetListener endDatePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            if (endDateTime == null) {
                endDateTime = Calendar.getInstance();
            }
            endDateTime.set(Calendar.YEAR, year);
            endDateTime.set(Calendar.MONTH, monthOfYear);
            endDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateEndDateText();
        }
    };

    private final View.OnClickListener endTimeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Calendar calendar = endDateTime != null ? endDateTime : Calendar.getInstance();
            new TimePickerDialog(CreateNewAuthorizedActivity.this,
                    endTimePickerListener, calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE), true).show();
        }
    };

    private final TimePickerDialog.OnTimeSetListener endTimePickerListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            if (endDateTime == null) {
                endDateTime = Calendar.getInstance();
            }
            endDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            endDateTime.set(Calendar.MINUTE, minute);
            updateEndTimeText();
        }
    };
}
