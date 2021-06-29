package es.gob.afirma.android.user.configuration;

/**
 * Interfaz que contiene el conjunto de constantes asociadas al módulo de configuración de la
 * aplicación.
 */
public interface ConfigurationConstants {

    /**
     * Constante que representa el código de petición de actividad para mostrar la pantalla de
     * configuración de roles.
     */
    int ACTIVITY_REQUEST_CODE_ROLE_VIEW = 0;

    /**
     * Constante que representa el código de petición para la actividad usada en la creación de una
     * nueva autorización.
     */
    int ACTIVITY_REQUEST_CODE_AUTH_CREATION = 1;

    /**
     * Constante que representa el código de petición para la actividad usada en la creación de un
     * nuevo validador.
     */
    int ACTIVITY_REQUEST_CODE_VERIFIER_CREATION = 2;

    /**
     * Constante que representa el código de respuesta "sin respuesta".
     */
    int ACTIVITY_RESULT_CODE_NONE = -1;

    /**
     * Constante que representa el código de respuesta de actividad para indicar que el acceso ha
     * sido denegado.
     */
    int ACTIVITY_RESULT_CODE_ACCESS_DENEGATED = -777;

    /**
     * Constante que representa el código de respuesta para la creación satisfactoria de una nueva
     * autorización.
     */
    int ACTIVITY_RESULT_CODE_AUTH_ROLE_OK = 0;

    /**
     * Constante que representa  que representa el código de respuesta para la creación fallida de
     * una nueva autorización.
     */
    int ACTIVITY_RESULT_CODE_AUTH_ROLE_KO = 1;

    /**
     * Constante que representa el código de respuesta para la creación satisfactoria de un nuevo
     * validador.
     */
    int ACTIVITY_RESULT_CODE_VERIFIER_ROLE_OK = 2;

    /**
     * Constante que representa el código de respuesta para la creación fallida de un nuevo
     * validador.
     */
    int ACTIVITY_RESULT_CODE_VERIFIER_ROLE_KO = 3;

    /**
     * Constante que representa la clave para almacenar rol seleccionado durante el proceso de
     * creación.
     */
    String EXTRA_RESOURCE_ROLE_SELECTED = "ROLE_SELECTED";

    String EXTRA_RESOURCE_FORCE_REFRESH = "FORCE_REFRESH";

    /**
     * Constante que representa la clave para almacenar la configuración de usuario durante el login.
     */
    String EXTRA_RESOURCE_USER_CONFIG = "userConfig";

    /**
     * Constante que representa la clave usada para almacenar el mensaje de error entre actividades.
     */
    String EXTRA_RESOURCE_ERROR_MSG = "errorMsg";

    /**
     * Constante que representa el identificador del parámetro del Intent
     * asociado a mostrar la información del rol "autorizado".
     */
    String EXTRA_RESOURCE_AUTH_ROLE_INFO = "authRoleInfo";

    /**
     * Constante que representa el identificador del parámetro del Intent
     * asociado a crear un nuevo validador o autorización.
     */
    String EXTRA_RESOURCE_USER_INFO = "userInfo";

    /**
     * Constante que representa la bandera que indica si se debe limpiar la pila de actividades.
     */
    String EXTRA_RESOURCE_CLEAN_STACK = "cleanActivitiesStack";


}
