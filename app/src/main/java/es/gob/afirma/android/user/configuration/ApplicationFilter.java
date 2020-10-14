package es.gob.afirma.android.user.configuration;

import java.io.Serializable;

/**
 * Clase que representa el filtro de usuario del tipo 'aplicación'.
 */
public class ApplicationFilter implements Serializable {

    /**
     * Identificador del filtro.
     */
    private String id;

    /**
     * Nombre del filtro.
     */
    private String name;

    /**
     * Constructor de la clase.
     *
     * @param id   Identificador del filtro.
     * @param name Nombre del filtro.
     */
    public ApplicationFilter(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Constructor sin parámetros de la clase.
     */
    public ApplicationFilter() {
    }

    /**
     * Método get del atributo <i>id</i>.
     *
     * @return el valor del atributo.
     */
    public String getId() {
        return id;
    }

    /**
     * Método set del atributo <i>id</i>.
     *
     * @param id Nuevo valor del atributo.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Método get del atributo <i>name</i>.
     *
     * @return el valor del atributo.
     */
    public String getName() {
        return name;
    }

    /**
     * Método set del atributo <i>name</i>.
     *
     * @param name Nuevo valor del atributo.
     */
    public void setName(String name) {
        this.name = name;
    }
}
