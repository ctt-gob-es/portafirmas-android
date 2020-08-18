package es.gob.afirma.android.user.configuration;

import java.io.Serializable;

/**
 * Enumerado que define los tipos de autorizaciones.
 */
public enum AuthorizedType implements Serializable {
    DELEGATE("Delegado"),
    SUBSTITUTE("Sustituto");

    /**
     * Atributo que representa el valor del enumerado.
     */
    private String value;

    /**
     * Constructor por defecto.
     * @param value Nuevo valor del atributo.
     */
    AuthorizedType(String value){
        this.value = value;
    }

    /**
     * Método que transforma un string en un objeto del tipo AuthorizedType.
     * @param value String a transformar.
     * @return un objeto de tipo AuthorizedType que representa el tipo pasado como parámetro o
     * nulo si no es posible parsear el valor.
     */
    public static AuthorizedType getAuthorizedType(String value){
        if(value == null){
            return null;
        }
        String val = value.toUpperCase();
        switch (val){
            case "Delegado":
                return AuthorizedType.DELEGATE;
            case "Sustituto":
                return AuthorizedType.SUBSTITUTE;
            default:
                return null;
        }
    }
}
