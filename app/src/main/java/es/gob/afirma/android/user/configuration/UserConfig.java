package es.gob.afirma.android.user.configuration;

import java.io.Serializable;
import java.util.List;

/**
 * Clase que representa la configuración de usuario para los servicios con el portafirmas-web.
 */
public class UserConfig implements Serializable {

    /**
     * Lista de roles asociados al usuario logeado.
     */
    private List<RoleInfo> roles;

    /**
     * Bandera que indica si los parámetros SIM del portafirmas-web están configurados.
     */
    private boolean simConfig;

    /**
     * Bandera que indica si el usuario logeado tiene validadores.
     */
    private boolean userWithVerifiers;

    /**
     * Bandera que indica el estado de las notificaciones push.
     */
    private boolean pushStatus;

    /**
     * Lista de filtros asociada al usuario.
     */
    private UserFilters userFilers;

    /**
     * Constructor de la clase.
     * @param roles Lista de roles del usuario.
     * @param simConfig Bandera de la configuración de los parámetros SIM.
     * @param userWithVerifiers Bandera que indica si el usuario tiene validadores.
     * @param pushStatus Bandera del estado de las notificaciones push.
     * @param userFilers Lista de filtros del usuario.
     */
    public UserConfig(List<RoleInfo> roles, boolean simConfig, boolean userWithVerifiers, boolean pushStatus, UserFilters userFilers){
        this.roles = roles;
        this.simConfig = simConfig;
        this.userWithVerifiers = userWithVerifiers;
        this.pushStatus = pushStatus;
        this.userFilers = userFilers;
    }

    /**
     * Método get del atributo <i>roles</i>.
     * @return el valor del atributo.
     */
    public List<RoleInfo> getRoles() {
        return roles;
    }

    /**
     * Método set del atributo <i>roles</i>.
     * @param roles Nuevo valor del atributo.
     */
    public void setRoles(List<RoleInfo> roles) {
        this.roles = roles;
    }

    /**
     * Método get del atributo <i>simConfig</i>.
     * @return el valor del atributo.
     */
    public boolean isSimConfig() {
        return simConfig;
    }

    /**
     * Método set del atributo <i>simConfig</i>.
     * @param simConfig Nuevo valor del atributo.
     */
    public void setSimConfig(boolean simConfig) {
        this.simConfig = simConfig;
    }

    /**
     * Método get del atributo <i>userWithVerifiers</i>.
     * @return el valor del atributo.
     */
    public boolean isUserWithVerifiers() {
        return userWithVerifiers;
    }

    /**
     * Método set del atributo <i>userWithVerifiers</i>.
     * @param userWithVerifiers Nuevo valor del atributo.
     */
    public void setUserWithVerifiers(boolean userWithVerifiers) {
        this.userWithVerifiers = userWithVerifiers;
    }

    /**
     * Método get del atributo <i>pushStatus</i>.
     * @return el valor del atributo.
     */
    public boolean isPushStatus() {
        return pushStatus;
    }

    /**
     * Método set del atributo <i>pushStatus</i>.
     * @param pushStatus Nuevo valor del atributo.
     */
    public void setPushStatus(boolean pushStatus) {
        this.pushStatus = pushStatus;
    }

    /**
     * Método get del atributo <i>userFilers</i>.
     * @return el valor del atributo.
     */
    public UserFilters getUserFilers() {
        return userFilers;
    }

    /**
     * Método set del atributo <i>userFilers</i>.
     * @param userFilers Nuevo valor del atributo.
     */
    public void setUserFilers(UserFilters userFilers) {
        this.userFilers = userFilers;
    }
}
