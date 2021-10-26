package es.gob.afirma.android.user.configuration;

import android.os.Bundle;

import java.io.Serializable;

/**
 * Autorizacion que un usuario ha concedido a otro para que gestione sus peticiones.
 */
public class Validator implements Serializable {

    private static final long serialVersionUID = -8311764968398531309L;

    private static final String BUNDLE_KEY = "validator";

    private boolean forApps;

    private GenericUser user;

    public boolean isForApps() {
        return this.forApps;
    }

    public void setForApps(boolean forApps) {
        this.forApps = forApps;
    }

    public GenericUser getUser() {
        return this.user;
    }

    public void setUser(GenericUser user) {
        this.user = user;
    }

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
    public static Validator fromBundle(Bundle auth) {
        if (auth == null) {
            return null;
        }
        return (Validator) auth.getSerializable(BUNDLE_KEY);
    }
}
