package es.gob.afirma.android.user.configuration;

import android.os.Bundle;

import java.io.Serializable;

/**
 * Información básica de un usuario.
 */
public class GenericUser implements Serializable {

    private static final long serialVersionUID = -2905363334470829578L;

    /** Identificador de usuario. */
    private String id;

    /** DNI del usuario. */
    private String dni;

    /** Nombre del usuario. */
    private String name;

    /**
     * Recupera el identificador del usuario.
     * @return Identificador del usuario.
     */
    public String getId() {
        return id;
    }

    /**
     * Establece el identificador del usuario.
     * @param id Identificador del usuario.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Recupera el DNI del usuario.
     * @return DNI del usuario.
     */
    public String getDni() {
        return dni;
    }

    /**
     * Establece el DNI del usuario.
     * @param dni DNI del usuario.
     */
    public void setDni(String dni) {
        this.dni = dni;
    }

    /**
     * Recupera el nombre del usuario.
     * @return Nombre del usuario.
     */
    public String getName() {
        return name;
    }

    /**
     * Establece el nombre del usuario.
     * @param name Nombre del usuario.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Guarda el usuario en un Bundle.
     * @return Bundle con el usuario.
     */
    public Bundle toBundle() {
        final Bundle bundle = new Bundle();
        bundle.putSerializable("genericuser", this);

        return bundle;
    }

    /**
     * Carga un usuario de un Bundle.
     * @param bundle De donde cargar el usuario.
     * @return Usuario cargado o {@code null} si no se encontró.
     */
    public static GenericUser fromBundle(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        return (GenericUser) bundle.getSerializable("genericuser");
    }
}
