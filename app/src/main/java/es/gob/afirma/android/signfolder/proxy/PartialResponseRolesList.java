package es.gob.afirma.android.signfolder.proxy;

import java.util.List;

import es.gob.afirma.android.user.configuration.GenericUser;

/**
 * Clase que representa el resultado parcial para el servicio "GetUsersByRole".
 */
public class PartialResponseRolesList {

    /**
     * Atributo que representa la lista parcial actual.
     */
    private final List<GetRoleRequest> usersList;

    /**
     * Constructor por defecto.
     *
     * @param currentGetRoleRequest Petición parcial actual.
     * @param totalGetRoleRequests  Número total de peticiones del servicio.
     */
    public PartialResponseRolesList(final List<GetRoleRequest> currentGetRoleRequest) {
        this.usersList = currentGetRoleRequest;
    }

    /**
     * Método get para el atributo <i>currentGetRoleRequest</i>
     *
     * @return el valor del atributo.
     */
    public List<GetRoleRequest> getUsersList() {
        return usersList;
    }
}
