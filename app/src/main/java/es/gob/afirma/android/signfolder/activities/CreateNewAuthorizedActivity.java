package es.gob.afirma.android.signfolder.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.user.configuration.AuthorizedType;
import es.gob.afirma.android.user.configuration.AuthorizedUser;
import es.gob.afirma.android.user.configuration.ConfigurationConstants;
import es.gob.afirma.android.user.configuration.ConfigurationRole;
import es.gob.afirma.android.user.configuration.UserConfiguration;

/**
 * Clase que gestiona la actividad asociada a la creación de nuevas autorizaciones.
 */
public class CreateNewAuthorizedActivity extends Activity {

    /**
     * Atributo que representa la fecha inicial seleccionada.
     */
    private final Calendar initDateTime = Calendar.getInstance();

    /**
     * Atributo que representa la fecha final seleccionada.
     */
    private final Calendar endDateTime = Calendar.getInstance();

    /**
     * Atributo que representa el usuario seleccionado para la creación del rol.
     */
    private UserConfiguration user;

    /**
     * Atributo que representa el texto mostrado para la fecha (dia, mes y año) inicial.
     */
    private TextView initDateTextView;

    /**
     * Atributo que representa el texto mostrado para la fecha (dia, mes y año) final.
     */
    private TextView endDateTextView;

    /**
     * Atributo que representa el texto mostrado para la fecha (horas y minutos) inicial.
     */
    private TextView initTimeTextView;

    /**
     * Atributo que representa el texto mostrado para la fecha (horas y minutos) final.
     */
    private TextView endTimeTextView;

    /**
     * Método auxiliar que construye la representación del usuario dado para mostrarlo en la vista.
     *
     * @param user Usuario a mostrar.
     * @return la representación del usuario: nombre + apellidos.
     */
    public static String buildUserRepresentation(UserConfiguration user) {
        String name = user.getName();
        String surname = user.getSurname();
        String secondSurname = user.getSecondSurname();

        if (name == null && surname == null && secondSurname == null) {
            return "-";
        }

        StringBuilder sb = new StringBuilder();
        if (name != null) {
            sb.append(name);
        }
        if (surname != null) {
            sb.append(" ");
            sb.append(surname);
        }
        if (secondSurname != null) {
            sb.append(" ");
            sb.append(secondSurname);
        }
        return sb.toString();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Recuperamos los parámetros del usuario seleccionado.
        String[] userParams = getIntent().getStringArrayExtra(ConfigurationConstants.EXTRA_RESOURCE_USER_INFO);
        if (userParams == null || userParams.length != 4) {
            throw new IllegalArgumentException("No ha sido posible recuperar el usuario seleccionado previamente.");
        }
        user = new UserConfiguration();
        user.setID(userParams[0]);
        user.setName(userParams[1]);
        user.setSurname(userParams[2]);
        user.setSecondSurname(userParams[3]);

        // Restauramos el valor por defecto del resultado a devolver por la actividad.
        setResult(ConfigurationConstants.ACTIVITY_RESULT_CODE_NONE);

        // Mostramos la vista.
        setContentView(R.layout.activity_create_new_authorized);

        // Mostramos el usuario seleccionado.
        String userRepresentation = buildUserRepresentation(user);
        ((TextView) findViewById(R.id.nameFieldValueId)).setText(userRepresentation);
        if (user.getID() != null) {
            ((TextView) findViewById(R.id.identifierFieldValueId)).setText(user.getID());
        } else {
            ((TextView) findViewById(R.id.identifierFieldValueId)).setText(R.string.empty_field_value);
        }


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


        final DatePickerDialog.OnDateSetListener initDatePicker = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                initDateTime.set(Calendar.YEAR, year);
                initDateTime.set(Calendar.MONTH, monthOfYear);
                initDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateInitDateText();
            }
        };

        initDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dpd = new DatePickerDialog(CreateNewAuthorizedActivity.this, initDatePicker,
                        initDateTime.get(Calendar.YEAR), initDateTime.get(Calendar.MONTH),
                        initDateTime.get(Calendar.DAY_OF_MONTH));
                dpd.getDatePicker().setMinDate(new Date().getTime() - 10000);
                dpd.show();
            }
        });

        final TimePickerDialog.OnTimeSetListener initTimePicker = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                initDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                initDateTime.set(Calendar.MINUTE, minute);
                updateInitTimeText();
            }
        };

        initTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(CreateNewAuthorizedActivity.this, initTimePicker,
                        initDateTime.get(Calendar.HOUR_OF_DAY), initDateTime.get(Calendar.MINUTE),
                        true).show();
            }
        });

        final DatePickerDialog.OnDateSetListener endDatePicker = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                endDateTime.set(Calendar.YEAR, year);
                endDateTime.set(Calendar.MONTH, monthOfYear);
                endDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateEndDateText();
            }
        };

        endDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dpd = new DatePickerDialog(CreateNewAuthorizedActivity.this, endDatePicker,
                        endDateTime.get(Calendar.YEAR), endDateTime.get(Calendar.MONTH),
                        endDateTime.get(Calendar.DAY_OF_MONTH));
                dpd.getDatePicker().setMinDate(new Date().getTime() - 10000);
                dpd.show();
            }
        });

        final TimePickerDialog.OnTimeSetListener endTimePicker = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                endDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                endDateTime.set(Calendar.MINUTE, minute);
                updateEndTimeText();
            }
        };

        endTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(CreateNewAuthorizedActivity.this, endTimePicker,
                        endDateTime.get(Calendar.HOUR_OF_DAY), endDateTime.get(Calendar.MINUTE),
                        true).show();
            }
        });
    }

    /**
     * Método que configura el comportamiento de los botones de la vista.
     */
    private void setupButtons() {
        this.findViewById(R.id.finishButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValues()) {
                    boolean opResult;
                    try {
                        AuthorizedUser authUser = getAuthorizationFieldsValues();
                        opResult = CommManager.getInstance().createNewRole(user, ConfigurationRole.AUTHORIZED, authUser,  null);
                    } catch (Exception e) {
                        Log.e("CreateRoleError", "Se ha producido un error durante la creación de la autorización", e);
                        opResult = false;
                    }
                    if (opResult) {
                        setResult(ConfigurationConstants.ACTIVITY_RESULT_CODE_AUTH_ROLE_OK);
                    } else {
                        setResult(ConfigurationConstants.ACTIVITY_RESULT_CODE_AUTH_ROLE_KO);
                    }
                    finish();
                } else {
                    Toast.makeText(CreateNewAuthorizedActivity.this,
                            R.string.error_input_params_creation_role, Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        // Configuramos el listener del botón de información.
        this.findViewById(R.id.infoDetailsDateId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CreateNewAuthorizedActivity.this);
                alertDialogBuilder.setTitle(null);

                alertDialogBuilder.setTitle(R.string.dialog_msg_info_date_auth);
                alertDialogBuilder.setPositiveButton(CreateNewAuthorizedActivity.this.getString(R.string.close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.dismiss();
                    }
                });
                alertDialogBuilder.create();
                alertDialogBuilder.show();
            }
        });
    }

    /**
     * Método auxiliar que almacena los valores de los campos del formulario en el objeto de tipo AuthorizedUser.
     *
     * @return un nuevo objeto AuthorizedUser con los nuevos valores.
     */
    private AuthorizedUser getAuthorizationFieldsValues() {
        AuthorizedUser authUser = new AuthorizedUser();
        authUser.setInitDate(initDateTime.getTime());
        authUser.setEndDate(endDateTime.getTime());
        RadioGroup rg = this.findViewById(R.id.radioGroupId);
        if (R.id.radioButtonDelegate == rg.getCheckedRadioButtonId()) {
            authUser.setType(AuthorizedType.DELEGATE);
        } else if (R.id.radioButtonSubstitute == rg.getCheckedRadioButtonId()) {
            authUser.setType(AuthorizedType.SUBSTITUTE);
        }
        EditText et = this.findViewById(R.id.editTextTextMultiLine);
        String observations = et.getText() != null ? et.getText().toString() : null;
        authUser.setObservations(observations);
        return authUser;
    }

    /**
     * Método que actualiza la fecha (dia, mes y año) inicial mostrada.
     */
    private void updateInitDateText() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, new Locale("es"));
        initDateTextView.setText(sdf.format(initDateTime.getTime()));
    }

    /**
     * Método que actualiza la fecha (dia, mes y año) final mostrada.
     */
    private void updateEndDateText() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, new Locale("es"));
        endDateTextView.setText(sdf.format(endDateTime.getTime()));
    }

    /**
     * Método que actualiza la fecha (horas y minutos) inicial mostrada.
     */
    private void updateInitTimeText() {
        String time = formatHour(initDateTime.get(Calendar.HOUR_OF_DAY)) + ":" + formatHour(initDateTime.get(Calendar.MINUTE));
        initTimeTextView.setText(time);
    }

    /**
     * Método que actualiza la fecha (horas y minutos) final mostrada.
     */
    private void updateEndTimeText() {
        String time = formatHour(endDateTime.get(Calendar.HOUR_OF_DAY)) + ":" + formatHour(endDateTime.get(Calendar.MINUTE));
        endTimeTextView.setText(time);
    }

    /**
     * Método auxiliar que formatea la fecha (horas y minutos) mostrada.
     *
     * @param value Horas o minutos a formatear.
     * @return un nuevo string que representa el valor formateado.
     */
    private String formatHour(Integer value) {
        if (value.toString().length() < 2) {
            return "0" + value.toString();
        } else {
            return value.toString();
        }
    }

    /**
     * Método que comprueba que los valores del formulario asociado a las fechas son válidos.
     *
     * @return <i>True</i> si los valores son correctos y <i>False</i> en caso contrario.
     */
    private boolean checkValues() {
        Date currentDate = new Date();
        boolean res = endDateTime.getTime().after(currentDate);
        res = res && initDateTime.before(endDateTime);
        return res;
    }
}
