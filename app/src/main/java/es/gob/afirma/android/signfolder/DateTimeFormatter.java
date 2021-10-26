package es.gob.afirma.android.signfolder;

import java.text.SimpleDateFormat;

/**
 * Instanciador para obtener el objeto con el que procesar el formato de las fechas en texto
 * enviadas desde y hacia el Portafirmas.
 */
public class DateTimeFormatter {

    private static final String APP_DATETIME_FORMAT = "dd/MM/yyyy HH:mm";

    private static final String APP_DATE_FORMAT = "dd/MM/yyyy";

    private static final String APP_TIME_FORMAT = "HH:mm";

    private static SimpleDateFormat appFormatter = null;

    private static SimpleDateFormat dateFormatter = null;

    private static SimpleDateFormat timeFormatter = null;

    public static SimpleDateFormat getAppFormatterInstance() {
        if (appFormatter == null) {
            appFormatter = new SimpleDateFormat(APP_DATETIME_FORMAT);
        }
        return appFormatter;
    }

    public static SimpleDateFormat getDateFormatterInstance() {
        if (dateFormatter == null) {
            dateFormatter = new SimpleDateFormat(APP_DATE_FORMAT);
        }
        return dateFormatter;
    }

    public static SimpleDateFormat getTimeFormatterInstance() {
        if (timeFormatter == null) {
            timeFormatter = new SimpleDateFormat(APP_TIME_FORMAT);
        }
        return timeFormatter;
    }
}
