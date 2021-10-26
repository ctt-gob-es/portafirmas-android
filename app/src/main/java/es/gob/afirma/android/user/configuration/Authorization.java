package es.gob.afirma.android.user.configuration;

import android.os.Bundle;

import java.io.Serializable;
import java.util.Date;

/**
 * Autorizacion que un usuario ha concedido a otro para que gestione sus peticiones.
 */
public class Authorization implements Serializable {

    private static final long serialVersionUID = 5047686399464371116L;

    private String id;

    private AuthorizedType type;

    private AuthorizationState state;

    private Date startDate;

    private Date revDate;

    private GenericUser user;

    private GenericUser authoricedUser;

    private String observations;

    private boolean sended;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AuthorizedType getType() {
        return type;
    }

    public void setType(AuthorizedType type) {
        this.type = type;
    }

    public AuthorizationState getState() {
        return state;
    }

    public void setState(AuthorizationState state) {
        this.state = state;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getRevDate() {
        return revDate;
    }

    public void setRevDate(Date revDate) {
        this.revDate = revDate;
    }

    public GenericUser getUser() {
        return user;
    }

    public void setUser(GenericUser user) {
        this.user = user;
    }

    public GenericUser getAuthoricedUser() {
        return authoricedUser;
    }

    public String getObservations() {
        return this.observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public boolean isSended() { return this.sended; }

    public void setSended(boolean sended) { this.sended = sended; }

    public void setAuthoricedUser(GenericUser authoricedUser) {
        this.authoricedUser = authoricedUser;
    }

    private static final String BUNDLE_KEY = "auth";

    public Bundle toBundle() {
        final Bundle bundle = new Bundle();
        bundle.putSerializable(BUNDLE_KEY, this);
        return bundle;
    }

    /**
     * Obtiene una instancia de la clase con los datos proporcionados en el array de cadenas.
     * @param userData Array de cadenas con los datos.
     * @return Objeto con los datos.
     */
    public static Authorization fromBundle(Bundle auth) {
        if (auth == null) {
            return null;
        }
        return (Authorization) auth.getSerializable(BUNDLE_KEY);
    }
}
