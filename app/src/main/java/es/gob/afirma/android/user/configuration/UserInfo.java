package es.gob.afirma.android.user.configuration;

import java.io.Serializable;
import java.util.List;

/**
 * Clase que representa la información de usuario.
 */
public class UserInfo implements Serializable {

    /**
     * Atributo que representa el nombre de usuario.
     */
    private String name;

    /**
     * Atributo que representa el primer apellido.
     */
    private String surname;

    /**
     * Atributo que representa el segundo apellido.
     */
    private String secondSurname;

    /**
     * Atributo que representa el LDAP del usuario.
     */
    private String LDAPUser;

    /**
     * Atributo que representa el identificador (DNI).
     */
    private String ID;

    /**
     * Atributo que representa la posición actual en la empresa.
     */
    private String position;

    /**
     * Atributo que representa el departamento.
     */
    private String headquarter;

    /**
     * Atributo que representa  la lista de perfiles activos.
     */
    private List<UserProfile> profiles;

    /**
     * Atributo que representa la lista de datos de contacto.
     */
    private List<ContactData> dataContact;

    /**
     * Atributo que indica si se debe adjuntar la firma.
     */
    private boolean attachSignature;

    /**
     * Atributo que indica si se debe adjuntar el reporte.
     */
    private boolean attachReport;

    /**
     * Atributo que representa el tamaño de página.
     */
    private int pageSize;

    /**
     * Atributo que indica si los filtros deben aplicarse a la plicación.
     */
    private boolean applyAppFilter;

    /**
     * Atributo que indica si es necesario mostrar las firmas anteriores.
     */
    private boolean showPreviousSigner;

    /**
     * Método get para el atributo <i>name</i>.
     * @return el valor del atributo.
     */
    public String getName() {
        return name;
    }

    /**
     * Método set del atributo <i>name</i>.
     * @param name Nuevo valor del atributo.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Método get para el atributo <i>surname</i>.
     * @return el valor del atributo.
     */
    public String getSurname() {
        return surname;
    }

    /**
     * Método set del atributo <i>surname</i>.
     * @param surname Nuevo valor del atributo.
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * Método get para el atributo <i>secondSurname</i>.
     * @return el valor del atributo.
     */
    public String getSecondSurname() {
        return secondSurname;
    }

    /**
     * Método set del atributo <i>secondSurname</i>.
     * @param secondSurname Nuevo valor del atributo.
     */
    public void setSecondSurname(String secondSurname) {
        this.secondSurname = secondSurname;
    }

    /**
     * Método get para el atributo <i>LDAPUser</i>.
     * @return el valor del atributo.
     */
    public String getLDAPUser() {
        return LDAPUser;
    }

    /**
     * Método set del atributo <i>LDAPUser</i>.
     * @param LDAPUser Nuevo valor del atributo.
     */
    public void setLDAPUser(String LDAPUser) {
        this.LDAPUser = LDAPUser;
    }

    /**
     * Método get para el atributo <i>ID</i>.
     * @return el valor del atributo.
     */
    public String getID() {
        return ID;
    }

    /**
     * Método set del atributo <i>ID</i>.
     * @param ID Nuevo valor del atributo.
     */
    public void setID(String ID) {
        this.ID = ID;
    }

    /**
     * Método get para el atributo <i>position</i>.
     * @return el valor del atributo.
     */
    public String getPosition() {
        return position;
    }

    /**
     * Método set del atributo <i>position</i>.
     * @param position Nuevo valor del atributo.
     */
    public void setPosition(String position) {
        this.position = position;
    }

    /**
     * Método get para el atributo <i>headquarter</i>.
     * @return el valor del atributo.
     */
    public String getHeadquarter() {
        return headquarter;
    }

    /**
     * Método set del atributo <i>headquarter</i>.
     * @param headquarter Nuevo valor del atributo.
     */
    public void setHeadquarter(String headquarter) {
        this.headquarter = headquarter;
    }

    /**
     * Método get para el atributo <i>profiles</i>.
     * @return el valor del atributo.
     */
    public List<UserProfile> getProfiles() {
        return profiles;
    }

    /**
     * Método set del atributo <i>profiles</i>.
     * @param profiles Nuevo valor del atributo.
     */
    public void setProfiles(List<UserProfile> profiles) {
        this.profiles = profiles;
    }

    /**
     * Método get para el atributo <i>dataContact</i>.
     * @return el valor del atributo.
     */
    public List<ContactData> getDataContact() {
        return dataContact;
    }

    /**
     * Método set del atributo <i>dataContact</i>.
     * @param dataContact Nuevo valor del atributo.
     */
    public void setDataContact(List<ContactData> dataContact) {
        this.dataContact = dataContact;
    }

    /**
     * Método get para el atributo <i>attachSignature</i>.
     * @return el valor del atributo.
     */
    public boolean isAttachSignature() {
        return attachSignature;
    }

    /**
     * Método set del atributo <i>attachSignature</i>.
     * @param attachSignature Nuevo valor del atributo.
     */
    public void setAttachSignature(boolean attachSignature) {
        this.attachSignature = attachSignature;
    }

    /**
     * Método get para el atributo <i>attachReport</i>.
     * @return el valor del atributo.
     */
    public boolean isAttachReport() {
        return attachReport;
    }

    /**
     * Método set del atributo <i>attachReport</i>.
     * @param attachReport Nuevo valor del atributo.
     */
    public void setAttachReport(boolean attachReport) {
        this.attachReport = attachReport;
    }

    /**
     * Método get para el atributo <i>pageSize</i>.
     * @return el valor del atributo.
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Método set del atributo <i>pageSize</i>.
     * @param pageSize Nuevo valor del atributo.
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Método get para el atributo <i>applyAppFilter</i>.
     * @return el valor del atributo.
     */
    public boolean isApplyAppFilter() {
        return applyAppFilter;
    }

    /**
     * Método set del atributo <i>applyAppFilter</i>.
     * @param applyAppFilter Nuevo valor del atributo.
     */
    public void setApplyAppFilter(boolean applyAppFilter) {
        this.applyAppFilter = applyAppFilter;
    }

    /**
     * Método get para el atributo <i>showPreviousSigner</i>.
     * @return el valor del atributo.
     */
    public boolean isShowPreviousSigner() {
        return showPreviousSigner;
    }

    /**
     * Método set del atributo <i>showPreviousSigner</i>.
     * @param showPreviousSigner Nuevo valor del atributo.
     */
    public void setShowPreviousSigner(boolean showPreviousSigner) {
        this.showPreviousSigner = showPreviousSigner;
    }

    @Override
    public String toString() {
        return name + " " + surname + " " + secondSurname + " - " + ID;
    }
}
