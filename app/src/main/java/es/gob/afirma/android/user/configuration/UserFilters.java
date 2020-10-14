package es.gob.afirma.android.user.configuration;

import java.io.Serializable;
import java.util.List;

/**
 * Clase que representa la lista de filtros de usuarios de la configuración de usuario.
 */
public class UserFilters implements Serializable {

    /**
     * Filtros de años.
     */
    private List<GenericFilter> yearFilters;

    /**
     * Filtros de meses.
     */
    private List<GenericFilter> monthFilters;

    /**
     * Filtros de tipo.
     */
    private List<GenericFilter> typeFilters;

    /**
     * Filtros de etiquetas.
     */
    private List<TagFilter> tagFilters;

    /**
     * Filtros de aplicaciones.
     */
    private List<ApplicationFilter> applicationFilters;

    /**
     * Constructor de la clase.
     *
     * @param yearFilters        Filtros de años.
     * @param monthFilters       Filtros de meses.
     * @param typeFilters        Filtros de tipos.
     * @param tagFilters         Filtros de etiquetas.
     * @param applicationFilters Filtros de aplicaciones.
     */
    public UserFilters(List<GenericFilter> yearFilters, List<GenericFilter> monthFilters, List<GenericFilter> typeFilters, List<TagFilter> tagFilters, List<ApplicationFilter> applicationFilters) {
        this.yearFilters = yearFilters;
        this.monthFilters = monthFilters;
        this.typeFilters = typeFilters;
        this.tagFilters = tagFilters;
        this.applicationFilters = applicationFilters;
    }

    /**
     * Constructor sin parámetros de la clase.
     */
    public UserFilters(){
    }

    /**
     * Método get del atributo <i>yearFilters</i>.
     *
     * @return el valor del atributo.
     */
    public List<GenericFilter> getYearFilters() {
        return yearFilters;
    }

    /**
     * Método set del atributo <i>yearFilters</i>.
     *
     * @param yearFilters Nuevo valor del atributo.
     */
    public void setYearFilters(List<GenericFilter> yearFilters) {
        this.yearFilters = yearFilters;
    }

    /**
     * Método get del atributo <i>monthFilters</i>.
     *
     * @return el valor del atributo.
     */
    public List<GenericFilter> getMonthFilters() {
        return monthFilters;
    }

    /**
     * Método set del atributo <i>monthFilters</i>.
     *
     * @param monthFilters Nuevo valor del atributo.
     */
    public void setMonthFilters(List<GenericFilter> monthFilters) {
        this.monthFilters = monthFilters;
    }

    /**
     * Método get del atributo <i>typeFilters</i>.
     *
     * @return el valor del atributo.
     */
    public List<GenericFilter> getTypeFilters() {
        return typeFilters;
    }

    /**
     * Método set del atributo <i>typeFilters</i>.
     *
     * @param typeFilters Nuevo valor del atributo.
     */
    public void setTypeFilters(List<GenericFilter> typeFilters) {
        this.typeFilters = typeFilters;
    }

    /**
     * Método get del atributo <i>tagFilters</i>.
     *
     * @return el valor del atributo.
     */
    public List<TagFilter> getTagFilters() {
        return tagFilters;
    }

    /**
     * Método set del atributo <i>tagFilters</i>.
     *
     * @param tagFilters Nuevo valor del atributo.
     */
    public void setTagFilters(List<TagFilter> tagFilters) {
        this.tagFilters = tagFilters;
    }

    /**
     * Método get del atributo <i>applicationFilters</i>.
     *
     * @return el valor del atributo.
     */
    public List<ApplicationFilter> getApplicationFilters() {
        return applicationFilters;
    }

    /**
     * Método set del atributo <i>applicationFilters</i>.
     *
     * @param applicationFilters Nuevo valor del atributo.
     */
    public void setApplicationFilters(List<ApplicationFilter> applicationFilters) {
        this.applicationFilters = applicationFilters;
    }
}
