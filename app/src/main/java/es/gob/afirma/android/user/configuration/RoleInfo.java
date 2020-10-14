package es.gob.afirma.android.user.configuration;

import java.io.Serializable;

/**
 * Clase que representa la información asociada a un rol.
 */
public class RoleInfo implements Serializable {

    /**
     * Identificador del tipo de rol.
     */
    private String roleId;

    /**
     * Nombre del rol.
     */
    private String roleName;

    /**
     * Nombre de la persona sobre la que se es rol, esto es, el emisor del rol.
     */
    private String userName;

    /**
     * DNI de la persona propietaria de las peticiones que podrá gestionar el rol.
     */
    private String ownerDni;

    /**
     * Constructor de la clase.
     *
     * @param roleId   Identificador del tipo de rol.
     * @param roleName Nombre del rol.
     * @param userName Nombre de la persona emisora del rol.
     * @param ownerDni DNI de la persona emisora del rol.
     */
    public RoleInfo(String roleId, String roleName, String userName, String ownerDni) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.userName = userName;
        this.ownerDni = ownerDni;
    }

    /**
     * Constructor sin parámetros de la clase.
     */
    public RoleInfo() {
    }

    /**
     * Método get del atributo <i>roleId</i>.
     *
     * @return el valor del atributo.
     */
    public String getRoleId() {
        return roleId;
    }

    /**
     * Método set del atributo <i>roleId</i>.
     *
     * @param roleId Nuevo valor del atributo.
     */
    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    /**
     * Método get del atributo <i>roleName</i>.
     *
     * @return el valor del atributo.
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * Método set del atributo <i>roleName</i>.
     *
     * @param roleName Nuevo valor del atributo.
     */
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    /**
     * Método get del atributo <i>userName</i>.
     *
     * @return el valor del atributo.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Método set del atributo <i>userName</i>.
     *
     * @param userName Nuevo valor del atributo.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Método get del atributo <i>ownerDni</i>.
     *
     * @return el valor del atributo.
     */
    public String getOwnerDni() {
        return ownerDni;
    }

    /**
     * Método set del atributo <i>ownerDni</i>.
     *
     * @param ownerDni Nuevo valor del atributo.
     */
    public void setOwnerDni(String ownerDni) {
        this.ownerDni = ownerDni;
    }
}
