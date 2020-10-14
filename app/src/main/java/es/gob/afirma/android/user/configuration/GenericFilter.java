package es.gob.afirma.android.user.configuration;

import java.io.Serializable;

/**
 * Clase que representa un filtro de usuario genérico.
 */
public class GenericFilter implements Serializable {

    /**
     * Identificador del filtro.
     */
    private String id;

    /**
     * Descripción del filtro.
     */
    private String description;

    /**
     * Constructor de la clase.
     *
     * @param id          Identificador del filtro.
     * @param description Descripción del filtro.
     */
    public GenericFilter(String id, String description) {
        this.id = id;
        this.description = description;
    }

    /**
     * Constructor sin parámetros de la clase.
     */
    public GenericFilter(){

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
     * Método get del atributo <i>description</i>.
     *
     * @return el valor del atributo.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Método set del atributo <i>description</i>.
     *
     * @param description Nuevo valor del atributo.
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
