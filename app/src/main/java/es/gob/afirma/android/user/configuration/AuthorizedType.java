package es.gob.afirma.android.user.configuration;

import java.io.Serializable;

/**
 * Enumerado que define los tipos de autorizaciones.
 */
public enum AuthorizedType implements Serializable {
    DELEGATE("DELEGADO", "1"),
    SUBSTITUTE("SUSTITUTO", "2");

    /**
     * Atributo que representa el valor del enumerado.
     */
    private String value;

    private String id;

    /**
     * Constructor por defecto.
     * @param value Texto descriptivo del tipo.
     * @param id Identificador del tipo.
     */
    AuthorizedType(String value, String id) {
        this.value = value;
        this.id = id;
    }

    public String getValue() {
        return this.value;
    }

    public String getId() {
        return this.id;
    }

    /**
     * Obtiene el tipo de autorizaci&oacute;n a partir de su identificador.
     * @param type Tipo de autorizaci&oacute;n.
     * @return un objeto de tipo AuthorizedType que representa el tipo pasado como parámetro o
     * nulo si no es posible parsear el valor.
     */
    public static AuthorizedType parse(String type) {
        if (type == null) {
            return null;
        }

        for (AuthorizedType authType : AuthorizedType.values()) {
            if (authType.value.equals(type)) {
                return authType;
            }
        }
        return null;
    }
}
