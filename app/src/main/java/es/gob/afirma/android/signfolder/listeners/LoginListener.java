package es.gob.afirma.android.signfolder.listeners;

import es.gob.afirma.android.signfolder.proxy.ValidationLoginResult;

/**
 * Interfaz que recibe el resultado de la operaci&oacute;n de login en el Portafirmas.
 */
public interface LoginListener {

    /**
     * M&eacute;todo que recibe el resultado de la operaci&oacute;n de autenticaci&oacute;n
     * del usuario en el Portafirmas.
     * @param loginResult Resultado de la operaci&oacute;n junto con el DNI del usuario si
     *                    pudo autenticarse o el mensaje de error si no.
     */
    void loginResult(ValidationLoginResult loginResult);
}
