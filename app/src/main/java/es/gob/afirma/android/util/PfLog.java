package es.gob.afirma.android.util;

import android.content.pm.ApplicationInfo;
import android.util.Log;

/**
 * Utilities class that manages the app logs messages.
 */
@SuppressWarnings("unused")
public class PfLog {

    /**
     * Attribute that indicates if the app is in production or not.
     */
    private static boolean isProduction;

    static {
        //noinspection ConstantConditions
        isProduction = ApplicationInfo.FLAG_DEBUGGABLE == 0;
    }

    /**
     * Private constructor.
     */
    private PfLog() {
    }

    /**
     * Method that write a log line if the app it is not in production.
     *
     * @param tag     Log tag.
     * @param message Message to write.
     */
    public static void w(String tag, String message) {
        if (!isProduction) {
            Log.w(tag, message);
        }
    }

    /**
     * Method that write a log line if the app it is not in production.
     *
     * @param tag     Log tag.
     * @param message Message to write.
     * @param t       Throwable object to include in the log message.
     */
    public static void w(String tag, String message, Throwable t) {
        if (!isProduction) {
            Log.w(tag, message, t);
        }
    }

    /**
     * Method that write a log line if the app it is not in production.
     *
     * @param tag     Log tag.
     * @param message Message to write.
     */
    public static void v(String tag, String message) {
        if (!isProduction) {
            Log.v(tag, message);
        }
    }

    /**
     * Method that write a log line if the app it is not in production.
     *
     * @param tag     Log tag.
     * @param message Message to write.
     * @param t       Throwable object to include in the log message.
     */
    public static void v(String tag, String message, Throwable t) {
        if (!isProduction) {
            Log.v(tag, message, t);
        }
    }

    /**
     * Method that write a log line if the app it is not in production.
     *
     * @param tag     Log tag.
     * @param message Message to write.
     */
    public static void i(String tag, String message) {
        if (!isProduction) {
            Log.i(tag, message);
        }
    }

    /**
     * Method that write a log line if the app it is not in production.
     *
     * @param tag     Log tag.
     * @param message Message to write.
     * @param t       Throwable object to include in the log message.
     */
    public static void i(String tag, String message, Throwable t) {
        if (!isProduction) {
            Log.i(tag, message, t);
        }
    }

    /**
     * Method that write a log line if the app it is not in production.
     *
     * @param tag     Log tag.
     * @param message Message to write.
     */
    public static void e(String tag, String message) {
        if (!isProduction) {
            Log.e(tag, message);
        }
    }

    /**
     * Method that write a log line if the app it is not in production.
     *
     * @param tag     Log tag.
     * @param message Message to write.
     * @param t       Throwable object to include in the log message.
     */
    public static void e(String tag, String message, Throwable t) {
        if (!isProduction) {
            Log.e(tag, message, t);
        }
    }

    /**
     * Method that write a log line if the app it is not in production.
     *
     * @param tag     Log tag.
     * @param message Message to write.
     */
    public static void d(String tag, String message) {
        if (!isProduction) {
            Log.d(tag, message);
        }
    }

    /**
     * Method that write a log line if the app it is not in production.
     *
     * @param tag     Log tag.
     * @param message Message to write.
     * @param t       Throwable object to include in the log message.
     */
    public static void d(String tag, String message, Throwable t) {
        if (!isProduction) {
            Log.d(tag, message, t);
        }
    }

    /**
     * Method that write a log line if the app it is not in production.
     *
     * @param tag     Log tag.
     * @param message Message to write.
     */
    public static void wtf(String tag, String message) {
        if (!isProduction) {
            Log.wtf(tag, message);
        }
    }

    /**
     * Method that write a log line if the app it is not in production.
     *
     * @param tag     Log tag.
     * @param message Message to write.
     * @param t       Throwable object to include in the log message.
     */
    public static void wtf(String tag, String message, Throwable t) {
        if (!isProduction) {
            Log.wtf(tag, message, t);
        }
    }

    /**
     * Method that write a log line if the app it is not in production.
     *
     * @param priority message priority.
     * @param tag      Log tag.
     * @param message  Message to write.
     */
    public static void println(int priority, String tag, String message) {
        if (!isProduction) {
            Log.println(priority, tag, message);
        }
    }

}
