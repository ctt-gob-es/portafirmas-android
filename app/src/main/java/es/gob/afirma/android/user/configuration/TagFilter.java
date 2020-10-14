package es.gob.afirma.android.user.configuration;

import java.io.Serializable;

/**
 * Clase que representa un filtro de usuario del tipo 'etiqueta'.
 */
public class TagFilter extends GenericFilter implements Serializable {

    /**
     * Color del filtro.
     */
    private String color;

    /**
     * Constructor de la clase.
     *
     * @param id          Identificador del filtro.
     * @param description Descripción del filtro.
     * @param color       Color del filtro.
     */
    public TagFilter(String id, String description, String color) {
        super(id, description);
        this.color = color;
    }

    /**
     * Constructor sin parámetros de la clase.
     */
    public TagFilter() {
        super();
    }

    /**
     * Método get del atributo <i>color</i>.
     *
     * @return el valor del atributo.
     */
    public String getColor() {
        return color;
    }

    /**
     * Método set del atributo <i>color</i>.
     *
     * @param color Nuevo valor del atributo.
     */
    public void setColor(String color) {
        this.color = color;
    }
}
