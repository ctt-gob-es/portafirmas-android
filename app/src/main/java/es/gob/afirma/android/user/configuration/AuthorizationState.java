package es.gob.afirma.android.user.configuration;

/**
 * Estados en el que puede encontrarse una autorización.
 */
public enum AuthorizationState {
    PENDING,
    REVOKED,
    REJECTED,
    ACTIVE;

    public static AuthorizationState parse(String state) {
        if (state == null) {
            return null;
        }

        AuthorizationState authState;
        switch (state) {
            case "pending":
                authState = PENDING;
                break;
            case "revoked":
                authState = REVOKED;
                break;
            case "accepted":
                authState = ACTIVE;
                break;
            case "rejected":
                authState = REJECTED;
                break;
            default:
                authState = null;
        }
        return authState;
    }
}
