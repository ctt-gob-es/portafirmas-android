package es.gob.afirma.android.user.configuration;

/**
 * Enumerado que define los tipos de roles disponibles.
 */
public enum ConfigurationRole {
    AUTHORIZED("AUTORIZADO"),
    VERIFIER("VALIDADOR");

    /**
     * Atributo que representa el valor del enumerado.
     */
    public final String name;

    /**
     * Construye el rol con su nombre.
     * @param name Nombre del rol.
     */
    ConfigurationRole(String name) {
        this.name = name;
    }

    /**
     * Obtiene el nombre del rol.
     * @return Nombre del rol.
     */
    public String getName() {
        return this.name;
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
        for (ConfigurationRole role : values()) {
            if (value.equalsIgnoreCase(role.name()) || value.equalsIgnoreCase(role.getName())) {
                return role;
            }
        }
        return null;
    }
}

