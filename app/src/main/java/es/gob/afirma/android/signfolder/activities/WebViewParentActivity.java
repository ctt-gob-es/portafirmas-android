package es.gob.afirma.android.signfolder.activities;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.util.PfLog;

/** Actividad de la que deberán heredar aquellas actividades que deseen utilizar un formulario web
 * a trav&eacute;s de un WebViewpara para la autenticaci&oacute;n/autorizaci&oacute;n del usuario. */
public class WebViewParentActivity extends FragmentActivity {

    public static final int WEBVIEW_REQUEST_CODE = 20;

    public static final String EXTRA_RESOURCE_URL = "url";
    public static final String EXTRA_RESOURCE_COOKIE_ID = "cookieId";
    public static final String EXTRA_RESOURCE_TITLE = "title";
    public static final String EXTRA_RESOURCE_NEED_JAVASCRIPT = "javascript";

    /**
     * Abre una actividad con un WebView en el que se carga una URL.
     * @param webViewClass Clase de la actividad con el WebView.
     * @param url URL que se desea cargar en el WebView.
     * @param cookieId Identificador de la cookie de sesion de la aplicaci&oacute;n.
     * @param titleStringId Identificador de la cadena con el t&iacute;tulo del WebView.
     * @param needJavaScript Indica si se debe habilitar el uso de JavaScript en el Webview.
     */
	public void openWebViewActivity(Class<?> webViewClass, String url, String cookieId,
                                    int titleStringId, boolean needJavaScript) {

        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setClass(this, webViewClass);

        PfLog.i(SFConstants.LOG_TAG, "Cargamos el WebView con la URL: " + url);
        PfLog.i(SFConstants.LOG_TAG, "Cargamos el WebView con el id de sesion: " + cookieId);

        intent.putExtra(EXTRA_RESOURCE_URL, url);
        intent.putExtra(EXTRA_RESOURCE_NEED_JAVASCRIPT, needJavaScript);
        if (cookieId != null) {
            intent.putExtra(EXTRA_RESOURCE_COOKIE_ID, cookieId);
        }
        if (titleStringId != 0) {
            intent.putExtra(EXTRA_RESOURCE_TITLE, titleStringId);
        }

        startActivityForResult(intent, WEBVIEW_REQUEST_CODE);
	}
}
