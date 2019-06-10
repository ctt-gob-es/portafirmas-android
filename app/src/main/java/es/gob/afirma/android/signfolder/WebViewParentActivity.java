package es.gob.afirma.android.signfolder;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;

/** Actividad para entrada con usuario y contrase&ntilde;a al servicio de Portafirmas. */
public class WebViewParentActivity extends FragmentActivity {

    static final int WEBVIEW_REQUEST_CODE = 20;

    /**
     * Abre una actividad con un WebView en el que se carga una URL.
     * @param url URL que se desea cargar en el WebView.
     */
	public void openWebViewActivity(String url, String cookieId) {

        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setClass(this, ClaveWebViewActivity.class);

        Log.i(SFConstants.LOG_TAG, " ========= Cargamos el WebView con la URL: " + url);
        Log.i(SFConstants.LOG_TAG, " ========= Cargamos el WebView con el id de sesion: " + cookieId);

        intent.putExtra(ClaveWebViewActivity.EXTRA_RESOURCE_URL, url);
        intent.putExtra(ClaveWebViewActivity.EXTRA_RESOURCE_COOKIE_ID, cookieId);
        startActivityForResult(intent, WEBVIEW_REQUEST_CODE);
	}
}
