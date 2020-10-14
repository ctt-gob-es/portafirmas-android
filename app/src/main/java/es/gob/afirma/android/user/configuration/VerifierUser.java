package es.gob.afirma.android.user.configuration;

/**
 * Clase que representa el objeto Validador.
 */
public class VerifierUser extends UserInfo {

    /**
     * Atributo que representa el identificador del validador.
     */
    private String identifier;

    /**
     * Atributo que representa el nombre del validador.
     */
    private String verifierName;

    /**
     * Método get para el atributo <i>status</i>.
     * @return el valor del atributo.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Método set del atributo <i>identifier</i>.
     * @param identifier Nuevo valor del atributo.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Método get para el atributo <i>verifierName</i>.
     * @return el valor del atributo.
     */
    public String getVerifierName() {
        return verifierName;
    }

    /**
     * Método set del atributo <i>verifierName</i>.
     * @param verifierName Nuevo valor del atributo.
     */
    public void setVerifierName(String verifierName) {
        this.verifierName = verifierName;
    }

    @Override
    public String toString() {
        return identifier + " - " + verifierName;
    }
}
