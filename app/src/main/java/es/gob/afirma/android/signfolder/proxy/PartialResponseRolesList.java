package es.gob.afirma.android.signfolder.proxy;

import java.util.ArrayList;
import java.util.List;

import es.gob.afirma.android.user.configuration.AuthorizedUser;
import es.gob.afirma.android.user.configuration.VerifierUser;

/**
 * Clase que representa el resultado parcial para el servicio "GetUsersByRole".
 */
public class PartialResponseRolesList {

    /**
     * Atributo que representa la lista parcial actual.
     */
    private final List<GetRoleRequest> currentGetRoleRequest;

    /**
     * Atributo que representa el numero total de peticiones del servicio.
     */
    private final int totalGetRoleRequests;

    /**
     * Constructor por defecto.
     *
     * @param currentGetRoleRequest Petición parcial actual.
     * @param totalGetRoleRequests  Número total de peticiones del servicio.
     */
    public PartialResponseRolesList(final List<GetRoleRequest> currentGetRoleRequest, final int totalGetRoleRequests) {
        this.currentGetRoleRequest = currentGetRoleRequest;
        this.totalGetRoleRequests = totalGetRoleRequests;
    }

    /**
     * Método get para el atributo <i>currentGetRoleRequest</i>
     *
     * @return el valor del atributo.
     */
    public List<GetRoleRequest> getCurrentGetRoleRequest() {
        return currentGetRoleRequest;
    }

    /**
     * Método get para el atributo <i>totalGetRoleRequests</i>
     *
     * @return el valor del atributo.
     */
    public int getTotalGetRoleRequests() {
        return totalGetRoleRequests;
    }

    /**
     * Método que construyo una lista de autorizaciones a partir de la lista de usuarios recibida del proxy.
     *
     * @return una lista de autorizaciones.
     */
    public List<AuthorizedUser> getAuthorizedList() {
        List<AuthorizedUser> res = new ArrayList<>();
        for (GetRoleRequest req : this.currentGetRoleRequest) {
            if (req != null) {
                // Attributos de usuario.
                AuthorizedUser authUser = new AuthorizedUser();
                authUser.setName(req.getName());
                authUser.setSurname(req.getSurname());
                authUser.setSecondSurname(req.getSecondSurname());
                authUser.setLDAPUser(req.getLDAPUser());
                authUser.setID(req.getID());
                authUser.setPosition(req.getPosition());
                authUser.setHeadquarter(req.getHeadquarter());
                authUser.setProfiles(req.getProfiles());
                authUser.setDataContact(req.getDataContact());
                authUser.setAttachSignature(req.isAttachSignature());
                authUser.setAttachReport(req.isAttachReport());
                authUser.setPageSize(req.getPageSize());
                authUser.setApplyAppFilter(req.isApplyAppFilter());
                authUser.setShowPreviousSigner(req.isShowPreviousSigner());
                // Attributos del rol 'autorizados'.
                authUser.setStatus(req.getStatus());
                authUser.setSentReceived(req.getSentReceived());
                authUser.setType(req.getType());
                authUser.setSenderReceiver(req.getSenderReceiver());
                authUser.setInitDate(req.getInitDate());
                authUser.setAuthorization(req.getAuthorization());
                authUser.setEndDate(req.getEndDate());
                // Añadimos el objeto a la lista.
                res.add(authUser);
            }
        }
        return res;
    }

    /**
     * Método que construye una lista de validadores a partir de la lista de usuarios recibida del proxy.
     * @return una lista de validadores.
     */
    public List<VerifierUser> getVerifierList() {
        List<VerifierUser> res = new ArrayList<>();
        for (GetRoleRequest req : this.currentGetRoleRequest) {
            if (req != null) {
                // Attributos de usuario.
                VerifierUser verifierUser = new VerifierUser();
                verifierUser.setName(req.getName());
                verifierUser.setSurname(req.getSurname());
                verifierUser.setSecondSurname(req.getSecondSurname());
                verifierUser.setLDAPUser(req.getLDAPUser());
                verifierUser.setID(req.getID());
                verifierUser.setPosition(req.getPosition());
                verifierUser.setHeadquarter(req.getHeadquarter());
                verifierUser.setProfiles(req.getProfiles());
                verifierUser.setDataContact(req.getDataContact());
                verifierUser.setAttachSignature(req.isAttachSignature());
                verifierUser.setAttachReport(req.isAttachReport());
                verifierUser.setPageSize(req.getPageSize());
                verifierUser.setApplyAppFilter(req.isApplyAppFilter());
                verifierUser.setShowPreviousSigner(req.isShowPreviousSigner());
                // Attributos del rol 'validador'
                verifierUser.setIdentifier(req.getVerifierIdentifier());
                verifierUser.setVerifierName(req.getVerifierName());
                // Añadimos el objeto a la lista.
                res.add(verifierUser);
            }
        }
        return res;
    }
}
