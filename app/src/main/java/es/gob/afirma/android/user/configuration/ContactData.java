package es.gob.afirma.android.user.configuration;

import java.io.Serializable;

/**
 * Clase que representa los datos de contacto de usuario.
 */
public class ContactData implements Serializable {

    /**
     * Atributo que representa el correo electrónico de contacto.
     */
    private String email;

    /**
     * Atributo que indica si la notificación será enviada al email proporcionado.
     */
    private boolean notify;

    /**
     * Método get para el atributo <i>email</i>.
     * @return el valor del atributo.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Método set para el atributo <i>email</i>.
     * @param email Nuevo valor del atributo.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Método get para el atributo <i>notify</i>.
     * @return el valor del atributo.
     */
    public boolean isNotify() {
        return notify;
    }

    /**
     * Método set para el atributo <i>notify</i>.
     * @param notify Nuevo valor del atributo.
     */
    public void setNotify(boolean notify) {
        this.notify = notify;
    }
}
