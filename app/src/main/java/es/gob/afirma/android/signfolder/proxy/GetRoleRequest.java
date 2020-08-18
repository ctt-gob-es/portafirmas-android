package es.gob.afirma.android.signfolder.proxy;

import java.util.Date;
import java.util.List;

import es.gob.afirma.android.user.configuration.AuthorizedType;
import es.gob.afirma.android.user.configuration.ContactData;
import es.gob.afirma.android.user.configuration.UserProfile;

/**
 * Clase que representa el objeto de petición para el servicio "GetUsersByRole".
 */
public class GetRoleRequest {

    /**
     * Identificador único para la petición.
     */
    private final String idReq;

    /**
     * Atributo que representa el nombre del usuario.
     */
    private String name;

    /**
     * Atributo que representa  el primer apellido del usuario.
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
     * Atributo que representa el identificador del usuario.
     */
    private String ID;

    /**
     * Atributo que representa la posición actual del usuario en la empresa.
     */
    private String position;

    /**
     * Atributo que representa  el departamento al que pertenece el usuario.
     */
    private String headquarter;

    /**
     * Atributo que representa la lista de perfiles activos.
     */
    private List<UserProfile> profiles;

    /**
     * Atributo que representa la lista de contactos.
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
     * Atributo que indica si los filtros deben ser aplicados en la aplicación.
     */
    private boolean applyAppFilter;

    /**
     * AAtributo que indica si es neesario mostrar los firmantes anteriores.
     */
    private boolean showPreviousSigner;

    /**
     * Atributo que representa el identificador del validador.
     */
    private String verifierIdentifier;

    /**
     * Atributo que representa  el nombre del validador.
     */
    private String verifierName;

    /**
     * Atributo que representa el estado de la autorización.
     */
    private String status;

    /**
     * Atributo que representa si la autorización ha sido enviada o recibida.
     */
    private String sentReceived;

    /**
     * Atributo que representa el tipo de autorización.
     */
    private AuthorizedType type;

    /**
     * Atributo que representa el nombre del emisor/destinatario de la autorización.
     */
    private String senderReceiver;

    /**
     * Atributo que representa la fecha de inicio de la autorización.
     */
    private Date initDate;

    /**
     * Atributo que representa la fecha de creación de la autorización.
     */
    private Date authorization;

    /**
     * Atributo que representa la fecha de revocación de la autorización.
     */
    private Date endDate;

    /**
     * Constructor con todos los atributos.
     * @param idReq Identificador único de la petición.
     * @param name Nombre de usuario.
     * @param surname Primer apellido del usuario.
     * @param secondSurname Segundo apellido del usuario.
     * @param LDAPUser  Autenticación LDAP del usuario.
     * @param ID Identificador de usuario (DNI).
     * @param position Posición en la empresa.
     * @param headquarter departamento.
     * @param profiles Lista de perfiles activos.
     * @param dataContact Lista de datos de contacto.
     * @param attachSignature Bandera que indica si la firma debe ser adjunta.
     * @param attachReport Bandera que indica si el reporte debe ser adjunto.
     * @param pageSize Tamaño de página.
     * @param applyAppFilter Bandera que indica si el filtro debe ser aplicado.
     * @param showPreviousSigner Bandera que indica si los firmantes previos deben ser mostrados.
     * @param verifierIdentifier identificador para el rol de validador.
     * @param verifierName Nombre del validador.
     * @param status Estado de la autorización.
     * @param sentReceived Bandera que indica si la autorización ha sido enviada o recibida.
     * @param type Tipo de autorización.
     * @param senderReceiver Emisor/destinatario de la autorización.
     * @param initDate Fecha de inicio de la autorización.
     * @param authorization fecha de creación de la autorización.
     * @param endDate Fecha de finalización de la autorización.
     */
    public GetRoleRequest(String idReq, String name, String surname, String secondSurname,
                          String LDAPUser, String ID, String position, String headquarter,
                          List<UserProfile> profiles, List<ContactData> dataContact,
                          boolean attachSignature, boolean attachReport, int pageSize,
                          boolean applyAppFilter, boolean showPreviousSigner,
                          String verifierIdentifier, String verifierName, String status,
                          String sentReceived, AuthorizedType type, String senderReceiver,
                          Date initDate, Date authorization, Date endDate) {
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
        this.verifierIdentifier = verifierIdentifier;
        this.verifierName = verifierName;
        this.status = status;
        this.sentReceived = sentReceived;
        this.type = type;
        this.senderReceiver = senderReceiver;
        this.initDate = initDate;
        this.authorization = authorization;
        this.endDate = endDate;
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

    /**
     * Método get para el atributo <i>verifierIdentifier</i>.
     *
     * @return el valor del atributo.
     */
    public String getVerifierIdentifier() {
        return verifierIdentifier;
    }

    /**
     * Método set para el atributo <i>verifierIdentifier</i>.
     *
     * @param verifierIdentifier Nuevo valor del atributo.
     */
    public void setVerifierIdentifier(String verifierIdentifier) {
        this.verifierIdentifier = verifierIdentifier;
    }

    /**
     * Método get para el atributo <i>verifierName</i>.
     *
     * @return el valor del atributo.
     */
    public String getVerifierName() {
        return verifierName;
    }

    /**
     * Método set para el atributo <i>verifierName</i>.
     *
     * @param verifierName Nuevo valor del atributo.
     */
    public void setVerifierName(String verifierName) {
        this.verifierName = verifierName;
    }

    /**
     * Método get para el atributo <i>status</i>.
     *
     * @return el valor del atributo.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Método set para el atributo <i>status</i>.
     *
     * @param status Nuevo valor del atributo.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Método get para el atributo <i>sentReceived</i>.
     *
     * @return el valor del atributo.
     */
    public String getSentReceived() {
        return sentReceived;
    }

    /**
     * Método set para el atributo <i>sentReceived</i>.
     *
     * @param sentReceived Nuevo valor del atributo.
     */
    public void setSentReceived(String sentReceived) {
        this.sentReceived = sentReceived;
    }

    /**
     * Método get para el atributo <i>type</i>.
     *
     * @return el valor del atributo.
     */
    public AuthorizedType getType() {
        return type;
    }

    /**
     * Método set para el atributo <i>type</i>.
     *
     * @param type Nuevo valor del atributo.
     */
    public void setType(AuthorizedType type) {
        this.type = type;
    }

    /**
     * Método get para el atributo <i>senderReceiver</i>.
     *
     * @return el valor del atributo.
     */
    public String getSenderReceiver() {
        return senderReceiver;
    }

    /**
     * Método set para el atributo <i>senderReceiver</i>.
     *
     * @param senderReceiver Nuevo valor del atributo.
     */
    public void setSenderReceiver(String senderReceiver) {
        this.senderReceiver = senderReceiver;
    }

    /**
     * Método get para el atributo <i>initDate</i>.
     *
     * @return el valor del atributo.
     */
    public Date getInitDate() {
        return initDate;
    }

    /**
     * Método set para el atributo <i>initDate</i>.
     *
     * @param initDate Nuevo valor del atributo.
     */
    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    /**
     * Método get para el atributo <i>authorization</i>.
     *
     * @return el valor del atributo.
     */
    public Date getAuthorization() {
        return authorization;
    }

    /**
     * Método set para el atributo <i>authorization</i>.
     *
     * @param authorization Nuevo valor del atributo.
     */
    public void setAuthorization(Date authorization) {
        this.authorization = authorization;
    }

    /**
     * Método get para el atributo <i>endDate</i>.
     *
     * @return el valor del atributo.
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Método set para el atributo <i>endDate</i>.
     *
     * @param endDate Nuevo valor del atributo.
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
