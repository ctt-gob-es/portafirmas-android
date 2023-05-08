package es.gob.afirma.android.signfolder.proxy;

import es.gob.afirma.android.signfolder.R;

/**
 * Permisos que pueden ser necesarios para completar una firma.
 */
public enum SignaturePermission {
    /** Permiso para firmar documentos PDF certificados. */
    CERTIFIED_PDF("signingCertifiedPdf", "allowSigningCertifiedPdfs", R.string.request_sign_certified_pdf_permission),
    /** Permiso para firmar documentos PDF con firmas no registradas. */
    UNREGISTERED_PDF_SIGNATURES("signingPdfWithUnregisteredSigns", "allowCosigningUnregisteredSignatures", R.string.request_sign_pdf_with_unregistered_sign_permission),
    /** Permiso para firmar documentos PDF sospechosos de haber sufrido un PDF Shadow Attack. */
    PDF_SHADOW_ATTACK("pdfShadowAttackSuspect", "allowShadowAttack", R.string.request_sign_suspected_psa_pdf_permission),
    /** Permiso para firmar documentos PDF con formularios modificados tras la firma. */
    MODIFIED_PDF_FORM("signingModifiedPdfForm", "allowModifiedForm", R.string.request_sign_pdf_with_modified_form_permission),
    /** Permiso para cofirmar/contrafirmas documentos con firmas de archivo. */
    SIGNING_LONG_TERM_SIGNATURE("signingLts", "allowSignLTSignature", R.string.request_sign_lts_signature_permission);

    /** Identificador del permiso. */
    private String permission;
    /** Propiedad de configuraci&oacute;n a establecer seg&uacute;n la respuesta del usuario. */
    private String extraParam;
    /** Texto de solicitud del permiso. */
    private int requestorText;

    /**
     * Crea el permiso.
     * @param permission Identificador del permiso.
     * @param requestorText Texto para la solicitud del permiso.
     */
    private SignaturePermission(String permission, String extraParam, int requestorText) {
        this.permission = permission;
        this.extraParam = extraParam;
        this.requestorText = requestorText;
    }

    /**
     * Recupera el identificador del permiso.
     * @return Identificador del permiso.
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Recupera el identificador del texto para la solicitud del permiso.
     * @return Identificador del texto para la solicitud del permiso.
     */
    public int getRequestorText() {
        return requestorText;
    }

    /**
     * Obtiene el permiso asociado a un identificador.
     * @param requestedPermission Identificador del permiso deseado.
     * @return Permiso o {@code null} si no se identific&oacute;.
     */
    public static SignaturePermission parse(String requestedPermission) {
        for (SignaturePermission value : values()) {
            if (value.getPermission().equals(requestedPermission)) {
                return value;
            }
        }
        return null;
    }

    public String getExtraParam() {
        return this.extraParam;
    }
}
