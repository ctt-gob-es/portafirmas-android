package es.gob.afirma.android.signfolder.proxy;

import java.util.List;


import es.gob.afirma.android.user.configuration.ContactData;
import es.gob.afirma.android.user.configuration.UserProfile;

/**
 * Clase que representa el objeto de la petición del servicio "GetUsers".
 */
public class GetUserRequest {

    /**
     * Identificador único de la petición.
     */
    private final String idReq;

    /**
     * Atributo que representa el nombre de usuario.
     */
    private String name;

    /**
     * Atributo que representa el primer apellido del usuario.
     */
    private String surname;

    /**
     * Atributo que representa el segundo apellido del usuario.
     */
    private String secondSurname;

    /**
     * Atributo que representa el LDAP del usuario.
     */
    private String LDAPUser;

    /**
     * Atributo que representa el identificador del usuario (DNI).
     */
    private String ID;

    /**
     * Atributo que representa la posición actual.
     */
    private String position;

    /**
     * Atributo que representa el departamento.
     */
    private String headquarter;

    /**
     * Atributo que representa la lista de perfiles activos.
     */
    private List<UserProfile> profiles;

    /**
     * Atributo que representa la lista de datos de contactos.
     */
    private List<ContactData> dataContact;

    /**
     * Atributo que indica si la firma debe ser adjunta.
     */
    private boolean attachSignature;

    /**
     * Atributo que indica si el reporte debe ser adjunto.
     */
    private boolean attachReport;

    /**
     * Atributo que representa el tamaño de página.
     */
    private int pageSize;

    /**
     * Atributo que indica si se deben aplicar los filtros en la aplicación.
     */
    private boolean applyAppFilter;

    /**
     * Atributo que indica si es necesario mostrar los firmantes anteriores.
     */
    private boolean showPreviousSigner;


    /**
     * Constructor con todos los atributos.
     * @param idReq Identificador único de la petición.
     * @param name Nombre de usuario.
     * @param surname Primer apellido.
     * @param secondSurname Segundo apellido.
     * @param LDAPUser  Autenticación LDAP.
     * @param ID Identificador de usuario (DNI).
     * @param position Posición en la empresa.
     * @param headquarter Departamento.
     * @param profiles Lista de perfiles activos.
     * @param dataContact Lista de datos de contacto.
     * @param attachSignature Bandera que indica si la firma debe ser adjunta.
     * @param attachReport Bandera que indica si el reporte debe ser adjunto.
     * @param pageSize Tamaño de página.
     * @param applyAppFilter Bandera que indica si el filtro debe ser aplicado.
     * @param showPreviousSigner Bandera que indica si los firmantes anteriores deben ser mostrados.
     */
    public GetUserRequest(String idReq, String name, String surname, String secondSurname,
                          String LDAPUser, String ID, String position, String headquarter,
                          List<UserProfile> profiles, List<ContactData> dataContact,
                          boolean attachSignature, boolean attachReport, int pageSize,
                          boolean applyAppFilter, boolean showPreviousSigner) {
        this.idReq = idReq;
        this.name = name;
        this.surname = surname;
        this.secondSurname = secondSurname;
        this.LDAPUser = LDAPUser;
        this.ID = ID;
        this.position = position;
        this.headquarter = headquarter;
        this.profiles = profiles;
        this.dataContact = dataContact;
        this.attachSignature = attachSignature;
        this.attachReport = attachReport;
        this.pageSize = pageSize;
        this.applyAppFilter = applyAppFilter;
        this.showPreviousSigner = showPreviousSigner;
    }

    /**
     * Método get para el atributo <i>idReq</i>.
     *
     * @return el valor del atributo.
     */
    public String getIdReq() {
        return idReq;
    }

    /**
     * Método get para el atributo <i>name</i>.
     *
     * @return el valor del atributo.
     */
    public String getName() {
        return name;
    }

    /**
     * Método set para el atributo <i>name</i>.
     *
     * @param name Nuevo valor del atributo.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Método get para el atributo <i>surname</i>.
     *
     * @return el valor del atributo.
     */
    public String getSurname() {
        return surname;
    }

    /**
     * Método set para el atributo <i>surname</i>.
     *
     * @param surname Nuevo valor del atributo.
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * Método get para el atributo <i>secondSurname</i>.
     *
     * @return el valor del atributo.
     */
    public String getSecondSurname() {
        return secondSurname;
    }

    /**
     * Método set para el atributo <i>secondSurname</i>.
     *
     * @param secondSurname Nuevo valor del atributo.
     */
    public void setSecondSurname(String secondSurname) {
        this.secondSurname = secondSurname;
    }

    /**
     * Método get para el atributo <i>LDAPUser</i>.
     *
     * @return el valor del atributo.
     */
    public String getLDAPUser() {
        return LDAPUser;
    }

    /**
     * Método set para el atributo <i>LDAPUser</i>.
     *
     * @param LDAPUser Nuevo valor del atributo.
     */
    public void setLDAPUser(String LDAPUser) {
        this.LDAPUser = LDAPUser;
    }

    /**
     * Método get para el atributo <i>ID</i>.
     *
     * @return el valor del atributo.
     */
    public String getID() {
        return ID;
    }

    /**
     * Método set para el atributo <i>ID</i>.
     *
     * @param ID Nuevo valor del atributo.
     */
    public void setID(String ID) {
        this.ID = ID;
    }

    /**
     * Método get para el atributo <i>position</i>.
     *
     * @return el valor del atributo.
     */
    public String getPosition() {
        return position;
    }

    /**
     * Método set para el atributo <i>position</i>.
     *
     * @param position Nuevo valor del atributo.
     */
    public void setPosition(String position) {
        this.position = position;
    }

    /**
     * Método get para el atributo <i>headquarter</i>.
     *
     * @return el valor del atributo.
     */
    public String getHeadquarter() {
        return headquarter;
    }

    /**
     * Método set para el atributo <i>headquarter</i>.
     *
     * @param headquarter Nuevo valor del atributo.
     */
    public void setHeadquarter(String headquarter) {
        this.headquarter = headquarter;
    }

    /**
     * Método get para el atributo <i>profiles</i>.
     *
     * @return el valor del atributo.
     */
    public List<UserProfile> getProfiles() {
        return profiles;
    }

    /**
     * Método set para el atributo <i>profiles</i>.
     *
     * @param profiles Nuevo valor del atributo.
     */
    public void setProfiles(List<UserProfile> profiles) {
        this.profiles = profiles;
    }

    /**
     * Método get para el atributo <i>dataContact</i>.
     *
     * @return el valor del atributo.
     */
    public List<ContactData> getDataContact() {
        return dataContact;
    }

    /**
     * Método set para el atributo <i>dataContact</i>.
     *
     * @param dataContact Nuevo valor del atributo.
     */
    public void setDataContact(List<ContactData> dataContact) {
        this.dataContact = dataContact;
    }

    /**
     * Método get para el atributo <i>attachSignature</i>.
     *
     * @return el valor del atributo.
     */
    public boolean isAttachSignature() {
        return attachSignature;
    }

    /**
     * Método set para el atributo <i>attachSignature</i>.
     *
     * @param attachSignature Nuevo valor del atributo.
     */
    public void setAttachSignature(boolean attachSignature) {
        this.attachSignature = attachSignature;
    }

    /**
     * Método get para el atributo <i>attachReport</i>.
     *
     * @return el valor del atributo.
     */
    public boolean isAttachReport() {
        return attachReport;
    }

    /**
     * Método set para el atributo <i>attachReport</i>.
     *
     * @param attachReport Nuevo valor del atributo.
     */
    public void setAttachReport(boolean attachReport) {
        this.attachReport = attachReport;
    }

    /**
     * Método get para el atributo <i>pageSize</i>.
     *
     * @return el valor del atributo.
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Método set para el atributo <i>pageSize</i>.
     *
     * @param pageSize Nuevo valor del atributo.
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Método get para el atributo <i>applyAppFilter</i>.
     *
     * @return el valor del atributo.
     */
    public boolean isApplyAppFilter() {
        return applyAppFilter;
    }

    /**
     * Método set para el atributo <i>applyAppFilter</i>.
     *
     * @param applyAppFilter Nuevo valor del atributo.
     */
    public void setApplyAppFilter(boolean applyAppFilter) {
        this.applyAppFilter = applyAppFilter;
    }

    /**
     * Método get para el atributo <i>showPreviousSigner</i>.
     *
     * @return el valor del atributo.
     */
    public boolean isShowPreviousSigner() {
        return showPreviousSigner;
    }

    /**
     * Método set para el atributo <i>showPreviousSigner</i>.
     *
     * @param showPreviousSigner Nuevo valor del atributo.
     */
    public void setShowPreviousSigner(boolean showPreviousSigner) {
        this.showPreviousSigner = showPreviousSigner;
    }
}
