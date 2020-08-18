package es.gob.afirma.android.user.configuration;

/**
 * Enumerado que define los tipos de roles disponibles.
 */
public enum ConfigurationRole {
    AUTHORIZED("Authorized"),
    VERIFIER("Verifier");

    /**
     * Atributo que representa el valor del enumerado.
     */
    public final String value;

    /**
     * Constructor por defecto.
     *
     * @param value Valor del rol.
     */
    ConfigurationRole(String value) {
        this.value = value;
    }

    /**
     * Método que transforma el valor de un determinado rol en un objeto de tipo ConfigurationRole.
     * @param value Valor a parsear.
     * @return un nuevo objeto de tipo ConfigurationRole o null si el parámetro no tiene un valor
     * válido.
     */
    public static ConfigurationRole getValue(String value) {
        if (value == null) {
            return null;
        }
        switch (value.toUpperCase()) {
            case "AUTHORIZED":
                return ConfigurationRole.AUTHORIZED;
            case "VERIFIER":
                return ConfigurationRole.VERIFIER;
            default:
                return null;

        }
    }
}

