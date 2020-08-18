package es.gob.afirma.android.signfolder.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import es.gob.afirma.android.signfolder.R;
import es.gob.afirma.android.user.configuration.AuthorizedType;
import es.gob.afirma.android.user.configuration.ConfigurationConstants;

/**
 * Clase que gestiona la actividad asociada a la información del rol "autorizado".
 */
public class AuthRoleInfoActivity extends Activity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] authUserValues = getIntent().getStringArrayExtra(ConfigurationConstants.EXTRA_RESOURCE_AUTH_ROLE_INFO);
        setContentView(R.layout.activity_auth_role_info);

        String status = authUserValues[0];
        if (status != null) {
            ImageView statusIcon = this.findViewById(R.id.statusIconId);
            switch (status.toUpperCase()) {
                case "VERIFIED":
                    ((TextView) this.findViewById(R.id.statusValueId)).setText(R.string.verified);
                    statusIcon.setImageResource(R.drawable.icon_check_16);
                    break;
                case "PENDING":
                    ((TextView) this.findViewById(R.id.statusValueId)).setText(R.string.pending);
                    statusIcon.setImageResource(R.drawable.icon_enespera_16);
                    break;
                case "REJECTED":
                    ((TextView) this.findViewById(R.id.statusValueId)).setText(R.string.rejected);
                    statusIcon.setImageResource(R.drawable.icon_error_16);
                    break;
                default:
                    throw new IllegalArgumentException("El estado de la autorización [" + status + "] no está reconocido.");
            }
        } else {
            this.findViewById(R.id.statusIconId).setVisibility(View.GONE);
        }

        String sentReceived = authUserValues[1];
        if (sentReceived != null) {
            ImageView inOutIcon = this.findViewById(R.id.inputOutputIconId);
            switch (sentReceived.toUpperCase()) {
                case "IN":
                    ((TextView) this.findViewById(R.id.inputOutputValueId)).setText(R.string.auth_type_in);
                    inOutIcon.setImageResource(R.drawable.icon_authorized_in_16);
                    break;
                case "OUT":
                    ((TextView) this.findViewById(R.id.inputOutputValueId)).setText(R.string.auth_type_out);
                    inOutIcon.setImageResource(R.drawable.icon_authorized_out_16);
                    break;
                default:
                    throw new IllegalArgumentException("La autorización no ha sido identificada ni " +
                            "como entrada ni como salida. El parámetro recibido ha sido: [" + sentReceived + "].");
            }
        }

        String type = authUserValues[2];
        if (type != null) {
            ((TextView) this.findViewById(R.id.TypeValueId)).setText(translateType(type));
        }

        String name = authUserValues[3];
        if (name != null) {
            ((TextView) this.findViewById(R.id.nameValueId)).setText(name);
        }

        String initDate = authUserValues[4];
        if (initDate != null) {
            ((TextView) this.findViewById(R.id.startDateValueId)).setText(initDate);
        }

        String endDate = authUserValues[5];
        if (endDate != null) {
            ((TextView) this.findViewById(R.id.endDateValueId)).setText(endDate);
        }
    }

    /**
     * Método que traduce el tipo de la autorización.
     * @param type Tipo a traducir.
     * @return el tipo de autorización traducido.
     */
    private String translateType(String type) {
        if (type != null && AuthorizedType.SUBSTITUTE.name().toUpperCase().equals(type.toUpperCase())) {
            return this.getString(R.string.substitute_type);
        } else if (type != null && AuthorizedType.DELEGATE.name().toUpperCase().equals(type.toUpperCase())) {
            return this.getString(R.string.delegate_type);
        }
        return this.getString(R.string.empty_field_value);
    }

}
