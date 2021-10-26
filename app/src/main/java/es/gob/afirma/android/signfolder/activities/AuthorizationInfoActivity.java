package es.gob.afirma.android.signfolder.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

import es.gob.afirma.android.signfolder.DateTimeFormatter;
import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.tasks.ChangeAuthorizationStateTask;
import es.gob.afirma.android.user.configuration.Authorization;
import es.gob.afirma.android.user.configuration.AuthorizationState;
import es.gob.afirma.android.user.configuration.AuthorizedType;
import es.gob.afirma.android.user.configuration.ConfigurationConstants;
import es.gob.afirma.android.util.PfLog;

/**
 * Clase que gestiona la actividad asociada a la información del rol "autorizado".
 */
public class AuthorizationInfoActivity extends Activity {

    /** Código para señalar la llamada a la actividad. */
    public static final int REQUEST_CODE_INFO = 41;

    /** Código para señalar un resultado errónea debido al cambio de estado. */
    public static final int RESULT_CODE_CHANGE_STATE_ERROR = RESULT_FIRST_USER;

    private Authorization auth = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle authUserBaundle = getIntent().getBundleExtra(ConfigurationConstants.EXTRA_RESOURCE_AUTHORIZATION);
        this.auth = Authorization.fromBundle(authUserBaundle);
        setContentView(R.layout.activity_auth_role_info);

        AuthorizationState state = this.auth.getState();
        if (state != null) {
            ImageView statusIcon = this.findViewById(R.id.statusIconId);
            switch (state) {
                case ACTIVE:
                    ((TextView) this.findViewById(R.id.statusValueId)).setText(R.string.verified);
                    statusIcon.setImageResource(R.drawable.icon_check_16);
                    break;
                case PENDING:
                    ((TextView) this.findViewById(R.id.statusValueId)).setText(R.string.pending);
                    statusIcon.setImageResource(R.drawable.icon_enespera_16);
                    break;
                case REVOKED:
                    ((TextView) this.findViewById(R.id.statusValueId)).setText(R.string.rejected);
                    statusIcon.setImageResource(R.drawable.icon_error_16);
                    break;
                default:
                    throw new IllegalArgumentException("El estado de la autorización [" + state + "] no está reconocido.");
            }
        } else {
            this.findViewById(R.id.statusIconId).setVisibility(View.GONE);
        }

        ImageView inOutIcon = this.findViewById(R.id.inputOutputIconId);
        if (this.auth.isSended()) {
            ((TextView) this.findViewById(R.id.inputOutputValueId)).setText(R.string.auth_type_out);
            inOutIcon.setImageResource(R.drawable.icon_authorized_out_16);
        }
        else {
            ((TextView) this.findViewById(R.id.inputOutputValueId)).setText(R.string.auth_type_in);
            inOutIcon.setImageResource(R.drawable.icon_authorized_in_16);
        }

        AuthorizedType type = this.auth.getType();
        if (type != null) {
            ((TextView) this.findViewById(R.id.TypeValueId)).setText(translateType(type));
        }

        String name = this.auth.getUser().getName();
        if (this.auth.getUser().getName() != null) {
            ((TextView) this.findViewById(R.id.nameValueId)).setText(name);
        }

        Date initDate = this.auth.getStartDate();
        if (initDate != null) {
            ((TextView) this.findViewById(R.id.startDateValueId)).setText(
                    DateTimeFormatter.getAppFormatterInstance().format(initDate));
        }

        Date endDate = this.auth.getRevDate();
        if (endDate != null) {
            ((TextView) this.findViewById(R.id.endDateValueId)).setText(
                    DateTimeFormatter.getAppFormatterInstance().format(endDate));
        }

        String observations = this.auth.getObservations();
        if (observations != null) {
            ((TextView) this.findViewById(R.id.observationsValueId)).setText(observations);
        }

        if (state == AuthorizationState.ACTIVE) {
            this.findViewById(R.id.authInfoButtonsView).setVisibility(View.VISIBLE);
            this.findViewById(R.id.cancelAuthtButton).setVisibility(View.VISIBLE);
        }
        else if (state == AuthorizationState.PENDING) {
            if (this.auth.isSended()) {
                this.findViewById(R.id.authInfoButtonsView).setVisibility(View.VISIBLE);
                this.findViewById(R.id.cancelAuthtButton).setVisibility(View.VISIBLE);
            }
            else {
                this.findViewById(R.id.authInfoButtonsView).setVisibility(View.VISIBLE);
                this.findViewById(R.id.rejectAuthtButton).setVisibility(View.VISIBLE);
                this.findViewById(R.id.acceptAuthtButton).setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Método que traduce el tipo de la autorización.
     * @param type Tipo a traducir.
     * @return el tipo de autorización traducido.
     */
    private String translateType(AuthorizedType type) {
        if (AuthorizedType.SUBSTITUTE == type) {
            return this.getString(R.string.substitute_type);
        } else if (AuthorizedType.DELEGATE == type) {
            return this.getString(R.string.delegate_type);
        }
        return this.getString(R.string.empty_field_value);
    }

    /**
     * Ejecuta la operación de cancelación o rechazo de la autorización.
     * @param v Vista sobre la que se hace clic para ejecutar esta operación.
     */
    public void onClickCancel(final View v) {
        changeAuthorizationState(AuthorizationState.REVOKED);
    }

    /**
     * Ejecuta la operación de aceptación de la autorización.
     * @param v Vista sobre la que se hace clic para ejecutar esta operación.
     */
    public void onClickAccept(final View v) {
        changeAuthorizationState(AuthorizationState.ACTIVE);
    }

    /**
     * Cambia el estado de una autorizaci&oacute;n.
     * @param state
     */
    private void changeAuthorizationState(AuthorizationState state) {
        try {
            ChangeAuthorizationStateTask task = new ChangeAuthorizationStateTask(
                    this.auth,
                    state,
                    new ChangeAuthorizationStateTask.ChangeAuthorizationStateListener() {
                        @Override
                        public void onSuccess() {
                            setResult(RESULT_OK);
                            finish();
                        }
                        @Override
                        public void onError(String message) {
                            PfLog.e(SFConstants.LOG_TAG, "Error al cambiar el estado de la autorizacion: " + message);
                            setResult(RESULT_CODE_CHANGE_STATE_ERROR);
                            finish();
                        }
                    });
            task.execute();
        }
        catch (Exception e) {
            PfLog.e(SFConstants.LOG_TAG, "Error en la ejecucion de la tarea de cambio de estado", e);
            setResult(RESULT_CODE_CHANGE_STATE_ERROR);
            finish();
        }
    }
}
