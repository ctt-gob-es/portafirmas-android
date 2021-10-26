package es.gob.afirma.android.signfolder.proxy.parsers;

import org.w3c.dom.Element;

import es.gob.afirma.android.signfolder.proxy.XmlUtils;
import es.gob.afirma.android.user.configuration.GenericUser;

/**
 *  Parser para la obtención de los datos de un usuario de un nodo XML.
 */
public class GenericUserParser {

    private static final String ID_ATTRIBUTE = "id";
    private static final String DNI_ATTRIBUTE = "dni"; //$NON-NLS-1$

    /**
     * Obtiene los datos b&aacute;sicos de un usuario de un nodo XML.
     * @param userElement Element XML del que obtener los datos del usuario.
     * @return Informaci&oacute;n del usuario.
     */
    public static GenericUser parse(Element userElement) {
        String id = userElement.getAttribute(ID_ATTRIBUTE);
        if (id == null) {
            throw new IllegalArgumentException("El nodo de usuario no incluye el atributo " + ID_ATTRIBUTE); //$NON-NLS-1$
        }
        String dni = userElement.getAttribute(DNI_ATTRIBUTE);
        if (dni == null) {
            throw new IllegalArgumentException("El nodo de usuario no incluye el atributo " + DNI_ATTRIBUTE); //$NON-NLS-1$
        }
        String name = XmlUtils.getTextContent(userElement);
        if (name == null) {
            throw new IllegalArgumentException("El nodo de usuario no incluye el nombre de usuario"); //$NON-NLS-1$
        }

        // Construimos el objeto con los datos del usuario
        final GenericUser user = new GenericUser();
        user.setId(id);
        user.setDni(dni);
        user.setName(name);

        return user;
    }
}
