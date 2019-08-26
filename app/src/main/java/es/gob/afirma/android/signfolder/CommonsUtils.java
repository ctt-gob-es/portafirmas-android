package es.gob.afirma.android.signfolder;

/**
 * Class that contains the commons set of utilities methods for the signfolder package.
 */
public final class CommonsUtils {

    /**
     * Attribute that represents the date subject attribute.
     */
    private static final String VALUE_ORDER_ATTR_SUBJECT = "dsubject"; //$NON-NLS-1$

    /**
     * Attribute that represents the date application attribute.
     */
    private static final String VALUE_ORDER_ATTR_APP = "application"; //$NON-NLS-1$

    /**
     * Default constructor.
     */
    private CommonsUtils() {
    }

    /**
     * Method that transforms the string representation of an order attribute into an intenger.
     *
     * @param orderAttr String representation of the order attribute.
     * @return the asigned integer value to the order attribute.
     */
    public static int getOrderAttrInteger(String orderAttr) {
        int res;
        switch (orderAttr) {
            case VALUE_ORDER_ATTR_SUBJECT:
                res = 1;
                break;
            case VALUE_ORDER_ATTR_APP:
                res = 2;
                break;
            default:
                res = 0;
        }
        return res;
    }

    /**
     * Method that transforms the string representation of the application selected into an integer.
     *
     * @param applicationAttr String representation of the application attribute.
     * @return the asigned intenger value to the application attribute or 0 if the values it not exists.
     */
    public static int getApplicationAttrInteger(String applicationAttr) {
        int res = 0;
        if (ConfigureFilterDialogBuilder.mApps.containsKey(applicationAttr)) {
            res = ConfigureFilterDialogBuilder.mApps.get(applicationAttr);
        }
        return res;
    }
}
