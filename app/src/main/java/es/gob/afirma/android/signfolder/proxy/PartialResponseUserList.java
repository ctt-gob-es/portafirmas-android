package es.gob.afirma.android.signfolder.proxy;

import java.util.ArrayList;
import java.util.List;

import es.gob.afirma.android.user.configuration.UserInfo;

/**
 * Clase que representa el resultado parcial del servicio "GetUsers".
 */
public class PartialResponseUserList {
    /**
     * Atributo que representa el listado parcial actual.
     */
    private final List<GetUserRequest> currentGetUserRequest;

    /**
     * Atributo que representa el número total de peticiones del servicio.
     */
    private final int totalGetRoleRequests;

    /**
     * Construye la lista parcial.
     *
     * @param currentGetRoleRequest Lista parcial actual.
     * @param totalGetRoleRequests  Número total de peticiones del servicio.
     */
    public PartialResponseUserList(final List<GetUserRequest> currentGetRoleRequest, final int totalGetRoleRequests) {
        this.currentGetUserRequest = currentGetRoleRequest;
        this.totalGetRoleRequests = totalGetRoleRequests;
    }

    /**
     * Método get del atributo <i>currentGetRoleRequest</i>
     *
     * @return el valor del atributo.
     */
    public List<GetUserRequest> getCurrentGetUserRequest() {
        return currentGetUserRequest;
    }

    /**
     * Método get del atributo <i>totalGetRoleRequests</i>
     *
     * @return el valor del atributo.
     */
    public int getTotalGetRoleRequests() {
        return totalGetRoleRequests;
    }

    /**
     * Método que construye una lista de usuarios a partir de la lista recibida del proxy.
     * @return una lista de usuarios.
     */
    public List<UserInfo> getUsersList() {
        List<UserInfo> res = new ArrayList<>();
        for (GetUserRequest req : this.currentGetUserRequest) {
            if (req != null) {
                // Attributos de usuario.
                UserInfo user = new UserInfo();
                user.setName(req.getName());
                user.setSurname(req.getSurname());
                user.setSecondSurname(req.getSecondSurname());
                user.setLDAPUser(req.getLDAPUser());
                user.setID(req.getID());
                user.setPosition(req.getPosition());
                user.setHeadquarter(req.getHeadquarter());
                user.setProfiles(req.getProfiles());
                user.setDataContact(req.getDataContact());
                user.setAttachSignature(req.isAttachSignature());
                user.setAttachReport(req.isAttachReport());
                user.setPageSize(req.getPageSize());
                user.setApplyAppFilter(req.isApplyAppFilter());
                user.setShowPreviousSigner(req.isShowPreviousSigner());
                // Añadimos el objeto a la lista.
                res.add(user);
            }
        }
        return res;
    }

}
