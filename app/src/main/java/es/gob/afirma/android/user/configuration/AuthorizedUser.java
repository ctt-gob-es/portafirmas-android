package es.gob.afirma.android.user.configuration;

import java.util.Date;

/**
 * Clase que representa el objeto de autorización.
 */
public class AuthorizedUser extends UserConfiguration {

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
     * Atributo que representa el nombre del emisor / receptor.
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
     * Atributo que representa la fecha de finalización de la autorización.
     */
    private Date endDate;

    /**
     * Atributo que representa las observaciones asociadas a la autorización.
     */
    private String observations;

    /**
     * Método get para el atributo <i>status</i>.
     * @return el valor del atributo.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Método set para el atributo <i>status</i>.
     * @param status nuevo valor del atributo.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Método get para el atributo <i>sentReceived</i>.
     * @return el valor del atributo.
     */
    public String getSentReceived() {
        return sentReceived;
    }

    /**
     * Método set para el atributo <i>sentReceived</i>.
     * @param sentReceived nuevo valor del atributo.
     */
    public void setSentReceived(String sentReceived) {
        this.sentReceived = sentReceived;
    }

    /**
     * Método get para el atributo <i>type</i>.
     * @return el valor del atributo.
     */
    public AuthorizedType getType() {
        return type;
    }

    /**
     * Método set para el atributo <i>type</i>.
     * @param type nuevo valor del atributo.
     */
    public void setType(AuthorizedType type) {
        this.type = type;
    }

    /**
     * Método get para el atributo <i>senderReceiver</i>.
     * @return el valor del atributo.
     */
    public String getSenderReceiver() {
        return senderReceiver;
    }

    /**
     * Método set para el atributo <i>senderReceiver</i>.
     * @param senderReceiver nuevo valor del atributo.
     */
    public void setSenderReceiver(String senderReceiver) {
        this.senderReceiver = senderReceiver;
    }

    /**
     * Método get para el atributo <i>initDate</i>.
     * @return el valor del atributo.
     */
    public Date getInitDate() {
        return initDate;
    }

    /**
     * Método set para el atributo <i>initDate</i>.
     * @param initDate nuevo valor del atributo.
     */
    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    /**
     * Método get para el atributo <i>authorization</i>.
     * @return el valor del atributo.
     */
    public Date getAuthorization() {
        return authorization;
    }

    /**
     * Método set para el atributo <i>authorization</i>.
     * @param authorization nuevo valor del atributo.
     */
    public void setAuthorization(Date authorization) {
        this.authorization = authorization;
    }

    /**
     * Método get para el atributo <i>endDate</i>.
     * @return el valor del atributo.
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Método set para el atributo <i>endDate</i>.
     * @param endDate nuevo valor del atributo.
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * Método get para el atributo <i>observations</i>.
     * @return el valor del atributo.
     */
    public String getObservations() {
        return observations;
    }

    /**
     * Método set para el atributo <i>observations</i>.
     * @param observations nuevo valor del atributo.
     */
    public void setObservations(String observations) {
        this.observations = observations;
    }

    @Override
    public String toString() {
        return type + " - " + getName() + " " + getSurname() + " " + getSecondSurname() + " - " + initDate + " - " + endDate;
    }
}
