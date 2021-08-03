package es.gob.afirma.android.signfolder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Locale;

import javax.security.auth.x500.X500Principal;

public class PermissionRequestorDialogFragment extends DialogFragment {

    public static final String ARG_SSL_CERTIFICATE = "cert";

    private static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f' };

    private X509Certificate sslCertificate;
    private WebViewAuthorizable webView = null;

    @Override
    public void setArguments(@Nullable Bundle args) {
        super.setArguments(args);

        if (args == null) {
            throw new IllegalArgumentException("No se han proporcionado parametros de entrada al dialogo");
        }

        this.sslCertificate = (X509Certificate) args.getSerializable(ARG_SSL_CERTIFICATE);
        if (this.sslCertificate == null) {
            throw new IllegalArgumentException("No se ha proporcionado el certificado SSL");
        }
    }

    public void setWebView(WebViewAuthorizable webView) {
        this.webView = webView;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.trust_in_cert)
                .setView(inflateCertificateView(this.sslCertificate))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        PermissionRequestorDialogFragment.this.webView.allowPermission(
                                PermissionRequestorDialogFragment.this.sslCertificate
                        );
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        PermissionRequestorDialogFragment.this.webView.denyPermission(
                                PermissionRequestorDialogFragment.this.sslCertificate
                        );
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    /**
     * Inflates the SSL certificate view (helper method).
     * @return The resultant certificate view with issued-to, issued-by,
     * issued-on, expires-on, and possibly other fields set.
     */
    public View inflateCertificateView(X509Certificate cert) {

        Context context = getActivity();
        LayoutInflater factory = LayoutInflater.from(context);

        View certificateView = factory.inflate(R.layout.ssl_certificate, null);

        // issued to:
        X500Principal issuedTo = cert.getSubjectX500Principal();
        if (issuedTo != null) {
            String issuedToText = issuedTo.getName(X500Principal.CANONICAL);
            ((TextView) certificateView.findViewById(R.id.to_common))
                    .setText(getRdn(issuedToText, "CN"));
            ((TextView) certificateView.findViewById(R.id.to_org))
                    .setText(getRdn(issuedToText, "O"));
            ((TextView) certificateView.findViewById(R.id.to_org_unit))
                    .setText(getRdn(issuedToText, "OU"));
        }
        // serial number:
        ((TextView) certificateView.findViewById(R.id.serial_number))
                .setText(fingerprint(cert.getSerialNumber().toByteArray()));

        // issued by:
        X500Principal issuedBy = cert.getIssuerX500Principal();
        if (issuedBy != null) {
            String issuedByText = issuedBy.getName(X500Principal.CANONICAL);
            ((TextView) certificateView.findViewById(R.id.by_common))
                    .setText(getRdn(issuedByText, "CN"));
            ((TextView) certificateView.findViewById(R.id.by_org))
                    .setText(getRdn(issuedByText, "O"));
            ((TextView) certificateView.findViewById(R.id.by_org_unit))
                    .setText(getRdn(issuedByText, "OU"));
        }

        // issued on:
        String issuedOn = formatCertificateDate(context, cert.getNotBefore());
        ((TextView) certificateView.findViewById(R.id.issued_on))
                .setText(issuedOn);

        // expires on:
        String expiresOn = formatCertificateDate(context, cert.getNotAfter());
        ((TextView) certificateView.findViewById(R.id.expires_on))
                .setText(expiresOn);

        // fingerprints:
        ((TextView) certificateView.findViewById(R.id.sha256_fingerprint))
                .setText(getDigest(cert, "SHA256"));
        ((TextView) certificateView.findViewById(R.id.sha1_fingerprint))
                .setText(getDigest(cert, "SHA1"));

        return certificateView;
    }

    /** Recupera el valor de un RDN (<i>Relative Distinguished Name</i>) de un principal. El valor de retorno no incluye
     * el nombre del RDN, el igual, ni las posibles comillas que envuelvan el valor.
     * La funci&oacute;n no es sensible a la capitalizaci&oacute;n del RDN. Si no se
     * encuentra, se devuelve {@code null}.
     * @param rdn RDN que deseamos encontrar.
     * @param principal Principal del que extraer el RDN (seg&uacute;n la <a href="http://www.ietf.org/rfc/rfc4514.txt">RFC 4514</a>).
     * @return Valor del RDN indicado o {@code null} si no se encuentra. */
    public static String getRdn(final String principal, final String rdn) {

        int offset1 = 0;
        while ((offset1 = principal.toLowerCase(Locale.US).indexOf(rdn.toLowerCase(), offset1)) != -1) {

            if (offset1 > 0 && principal.charAt(offset1-1) != ',' && principal.charAt(offset1-1) != ' ') {
                offset1++;
                continue;
            }

            offset1 += rdn.length();
            while (offset1 < principal.length() && principal.charAt(offset1) == ' ') {
                offset1++;
            }

            if (offset1 >= principal.length()) {
                return null;
            }

            if (principal.charAt(offset1) != '=') {
                continue;
            }

            offset1++;
            while (offset1 < principal.length() && principal.charAt(offset1) == ' ') {
                offset1++;
            }

            if (offset1 >= principal.length()) {
                return ""; //$NON-NLS-1$
            }

            int offset2;
            if (principal.charAt(offset1) == ',') {
                return ""; //$NON-NLS-1$
            }
            else if (principal.charAt(offset1) == '"') {
                offset1++;
                if (offset1 >= principal.length()) {
                    return ""; //$NON-NLS-1$
                }

                offset2 = principal.indexOf('"', offset1);
                if (offset2 == offset1) {
                    return ""; //$NON-NLS-1$
                }
                else if (offset2 != -1) {
                    return principal.substring(offset1, offset2);
                }
                else {
                    return principal.substring(offset1);
                }
            }
            else {
                offset2 = principal.indexOf(',', offset1);
                if (offset2 != -1) {
                    return principal.substring(offset1, offset2).trim();
                }
                return principal.substring(offset1).trim();
            }
        }

        return null;
    }

    /**
     * Formats the certificate date to a properly localized date string.
     * @return Properly localized version of the certificate date string and
     * the "" if it fails to localize.
     */
    private String formatCertificateDate(Context context, Date certificateDate) {
        if (certificateDate == null) {
            return "";
        }
        return DateFormat.getMediumDateFormat(context).format(certificateDate);
    }

    /**
     * Convenience for UI presentation, not intended as public API.
     */
    private static String getDigest(X509Certificate x509Certificate, String algorithm) {
        if (x509Certificate == null) {
            return "";
        }
        try {
            byte[] bytes = x509Certificate.getEncoded();
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] digest = md.digest(bytes);
            return fingerprint(digest);
        } catch (CertificateEncodingException ignored) {
            return "";
        } catch (NoSuchAlgorithmException ignored) {
            return "";
        }
    }

    private static String fingerprint(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            sb.append(HEX[(0xF0 & b) >>> 4]);   // Caracter con los 4 primeros bits
            sb.append(HEX[0x0F & b]);           // Caracter con los 4 ultimos bits
            if (i+1 != bytes.length) {
                sb.append(':');
            }
        }
        return sb.toString();
    }
}
