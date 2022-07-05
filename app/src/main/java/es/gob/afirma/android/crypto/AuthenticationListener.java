package es.gob.afirma.android.crypto;

/**
 * Manejador de la operacion de autenticaci&oacute;n del usuario.
 */
public interface AuthenticationListener {

    /**
     * Procesa el resultado del proceso de autenticaci&oacute;n.
     * @param loginResult Resultado del proceso de autenticaci&oacute;n.
     */
    void processAuthenticationResult(AuthenticationResult loginResult);
}
