package es.gob.afirma.android.signfolder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.gob.afirma.android.network.AndroidUrlHttpManager;
import es.gob.afirma.android.signfolder.proxy.CommManager;

/** Actividad para entrada con usuario y contrase&ntilde;a en Cl@ve. */
public final class ClaveWebViewActivity extends Activity {

	/** Dialogo para mostrar mensajes al usuario */
	private MessageDialog messageDialog = null;

	/** Tag para la presentaci&oacute;n de di&aacute;logos */
	private static final String DIALOG_TAG = "dialog"; //$NON-NLS-1$

	/** Di&aacute;logo para confirmar el cierre de la sesi&oacute;n. */
	private final static int DIALOG_CONFIRM_EXIT = 12;

	static final String EXTRA_RESOURCE_URL = "url";
    static final String EXTRA_RESOURCE_COOKIE_ID = "cookieId";

	static final String RESULT_BOOLEAN_AUTHORIZED = "authorized";

	static final String RESULT_STRING_TOKEN_SAML = "tokensaml";

	MessageDialog getMessageDialog() {
		return this.messageDialog;
	}

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webview);

		WebView webView = (WebView) findViewById(R.id.webView);

		webView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				Log.i(SFConstants.LOG_TAG, "---- Inicio de pagina: " + url);
				int pathPos = url.lastIndexOf('/') + 1;
				if (pathPos == 0) {
					return;
				}

				int endPos = url.indexOf('?', pathPos);
				String path = endPos != -1 ? url.substring(pathPos, endPos) : url.substring(pathPos);

				// Cerramos el webview si somos redirigidos a la pagina "ok" o "error"
                // devolviendo el resultado pertinente en cada caso

				if (path.endsWith("/ok")) {
					setResult(Activity.RESULT_OK);
					closeActivity();
				}
				else if (path.endsWith("/error")) {
					Intent result = new Intent();
					result.putExtra("errorParams", url.substring(endPos + 1));
					setResult(Activity.RESULT_FIRST_USER, result);
					closeActivity();
				}
			}
		});

		// Configuramos el WebView para que cargue la misma sesion que la aplicacion Java
        final CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, true);
        }

        String url = super.getIntent().getStringExtra(EXTRA_RESOURCE_URL);
        String cookieId = super.getIntent().getStringExtra(EXTRA_RESOURCE_COOKIE_ID);
        if (cookieId != null) {
            cookieManager.setCookie(url, cookieId);
        }

        // Insertamos la referencia a la Cookie en la cabecera de la peticion
        final String cookie = cookieManager.getCookie(url);
        Log.w(SFConstants.LOG_TAG, "---- Cookie del WebView: " + cookie);

        final Map<String, String> headers = new HashMap<>();
        if (cookie != null) {
            headers.put("Cookie", cookie);
        }

        // Habilitamos los permisos del WebView
        final WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.loadUrl(url, headers);
	}

	private ProgressDialog progressDialog = null;
	ProgressDialog getProgressDialog() {
		return this.progressDialog;
	}
	void setProgressDialog(final ProgressDialog pd) {
		this.progressDialog = pd;
	}

	/** Muestra un di&aacute;logo de espera con un mensaje. */

	public void showProgressDialog(final String message, final ClaveLoginTask cclt) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					setProgressDialog(ProgressDialog.show(ClaveWebViewActivity.this, null, message, true));
					if(cclt != null){
						getProgressDialog().setOnKeyListener(new OnKeyListener() {
							@Override
							public boolean onKey(final DialogInterface dlg, final int keyCode, final KeyEvent event) {
								if (keyCode == KeyEvent.KEYCODE_BACK) {
									cclt.cancel(true);
									dismissProgressDialog();
									closeActivity();
									return true;
								}
								return false;
							}
						});
					}
				} catch (final Exception e) {
					Log.e(SFConstants.LOG_TAG, "No se ha podido mostrar el dialogo de progreso: " + e); //$NON-NLS-1$
				}
			}
		});
	}

	/** Cierra el activity liberando recursos. */
	public void closeActivity() {
		finish();
	}

	/** Cierra el di&aacute;logo de espera en caso de estar abierto. */
	void dismissProgressDialog() {
		if (getProgressDialog() != null) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					getProgressDialog().dismiss();
				}
			});
		}
	}

	public void errorClaveLogin(final ClaveLoginTask cclt) {
		showErrorDialog(getString(R.string.dialog_msg_clave_login_fail));
	}

	/**
	 * Muestra un mensaje de advertencia al usuario de que se ha producido un error en el login.
	 * @param message Mensaje que se desea mostrar.
	 */
	private void showErrorDialog(final String message) {
		dismissProgressDialog();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.aviso));
		builder.setMessage(message);
		builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public void dismissDialog() {
		dismissProgressDialog();
	}


//	@Override
//	public void onBackPressed() {
//		// Preguntamos si debe cerrarse la sesion
//		showConfirmExitDialog();
//	}

	/**
	 * Muestra un mensaje al usuario pidiendo confirmacion para cerrar la
	 * sesi&oacute;n del usuario.
	 */
	private void showConfirmExitDialog() {

		dismissProgressDialog();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.dialog_title_close_session));
		builder.setMessage(R.string.dialog_msg_close_session);
		builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				try {
					LogoutRequestTask lrt = new LogoutRequestTask(CommManager.getInstance());
					lrt.execute();
				} catch (Exception e) {
					Log.e(SFConstants.LOG_TAG,
							"No se ha podido cerrar sesion: " + e); //$NON-NLS-1$
				}
				closeActivity();
			}
		});
		builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

}
