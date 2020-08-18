package es.gob.afirma.android.user.configuration;

import java.io.Serializable;

/**
 * Enumerado que define los tipos de perfiles de usuario.
 */
public enum UserProfile implements Serializable {
    ACCESO("ACCESO"),
    ADMIN("ADMIN"),
    ADMINCAID("ADMINCAID"),
    ADMIN_ORG("ADMIN_ORG"),
    ADMINPROV("ADMINPROV"),
    FIRMA("FIRMA"),
    REDACCION("REDACCION");

    /**
     * Atributo que representa el valor del tipo de perfil.
     */
    private String value;

    /**
     * Constructor por defecto.
     * @param value Valor del tipo de perfil.
     */
    UserProfile(String value) {
        this.value = value;
    }

    /**
     * Método que parsea una cadena en un objeto de tipo UserProfile.
     * @param value Valor a parsear.
     * @return un objeto de tipo UserProfile o null si el valor del parámetro no es válido.
     */
    public static UserProfile getUserProfile(String value){
        String val = value.toUpperCase();
        switch (val){
            case "ACCESO":
                return UserProfile.ACCESO;
            case "ADMIN":
                return UserProfile.ADMIN;
            case "ADMINCAID":
                return UserProfile.ADMINCAID;
            case "ADMIN_ORG":
                return UserProfile.ADMIN_ORG;
            case "ADMINPROV":
                return UserProfile.ADMINPROV;
            case "FIRMA":
                return UserProfile.FIRMA;
            case "REDACCION":
                return UserProfile.REDACCION;
            default:
                return null;

        }
    }
}
