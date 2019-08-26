package es.gob.afirma.android.signfolder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashMap;
import java.util.Map;

import es.gob.afirma.android.util.PfLog;

/** Actividad para entrada con usuario y contrase&ntilde;a en Cl@ve. */
public final class ClaveWebViewActivity extends Activity {

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		int titleStringId = getIntent().getIntExtra(WebViewParentActivity.EXTRA_RESOURCE_TITLE, 0);
		if (titleStringId != 0) {
			setTitle(titleStringId);
		}
		setContentView(R.layout.activity_webview);

		WebView webView = findViewById(R.id.webView);

		// Definimos el comportamiento del WebView
		webView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				PfLog.i(SFConstants.LOG_TAG, "---- Inicio de pagina: " + url);

				int paramsPos = url.indexOf('?');

				String cleanUrl = paramsPos != -1 ?  url.substring(0, paramsPos) : url;
				int pathPos = cleanUrl.lastIndexOf('/');
				if (pathPos == -1) {
					return;
				}

				String path = cleanUrl.substring(pathPos); // La '/' forma parte del path
				String params = paramsPos != -1 ?  url.substring(paramsPos + 1) : null;

				PfLog.i(SFConstants.LOG_TAG, "---- Particula final: " + path);

				// Cerramos el webview si somos redirigidos a la pagina "ok" o "error"
                // devolviendo el resultado pertinente en cada caso

				if (path.startsWith("/ok")) {
					final Intent result = saveParamsInDataIntent(params);
					setResult(Activity.RESULT_OK, result);
					closeActivity();
				}
				else if (path.startsWith("/error")) {
					final Intent result = saveParamsInDataIntent(params);
					setResult(Activity.RESULT_FIRST_USER, result);
					closeActivity();
				}
			}

			@Override
			public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
				super.onReceivedError(view, request, error);
				PfLog.e(SFConstants.LOG_TAG,"Error recibido en el WebView: " + error);
			}

			@Override
			public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
				super.onReceivedHttpError(view, request, errorResponse);
				PfLog.w(SFConstants.LOG_TAG,"No se ha podido cargar en el el recurso: " + request);
			}

			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
				super.onReceivedSslError(view, handler, error);
				PfLog.e(SFConstants.LOG_TAG,"Error de SSL recibido en el WebView con la URL: " + error.getUrl());
			}
		});

		// Habilitamos los permisos del WebView
		boolean needJavaScript = getIntent().getBooleanExtra(
				WebViewParentActivity.EXTRA_RESOURCE_NEED_JAVASCRIPT, false);
		final WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(needJavaScript);

		// Recuperamos la configuracion
		String url = getIntent().getStringExtra(WebViewParentActivity.EXTRA_RESOURCE_URL);
		String cookieId = getIntent().getStringExtra(WebViewParentActivity.EXTRA_RESOURCE_COOKIE_ID);

		// Si se ha indicado un id de cookie de sesi&oacute;n, lo asociamos a la URL y configuramos
		// unas cabeceras de petici&oacute;n para el uso de esa cookie
		Map<String, String> headers = null;
		if (cookieId != null) {
			headers = configureCookies(webView, url, cookieId);
		}
		webView.loadUrl(url, headers);
	}

	/**
	 * Configura la cookies de sesi&oacute;n en un WebView.
	 * @param webView WebView en el que se van a cargar la URL con las coIdentificador de la cookie
	 *                   de sesi&oacute;n a utilizar.
	 * @param url URL para la que se desean utilizar las cookies.
	 * @param cookieId Identificador de la cookie de sesi&oacute;n a utilizar.
	 * @return Cabeceras que proporcionar al WebView para realizar la carga de la URL.
	 */
	private Map<String, String> configureCookies(WebView webView, String url, String cookieId) {
		final CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			cookieManager.setAcceptThirdPartyCookies(webView, true);
		}

		if (cookieId != null) {
			cookieManager.setCookie(url, cookieId);
		}

		// Insertamos la referencia a la Cookie en la cabecera de la peticion
		final String cookie = cookieManager.getCookie(url);
		PfLog.w(SFConstants.LOG_TAG, "---- Cookie del WebView: " + cookie);

		final Map<String, String> headers = new HashMap<>();
		if (cookie != null) {
			headers.put("Cookie", cookie);
		}

		return headers;
	}


	private static Intent saveParamsInDataIntent(String urlParams) {
		Intent result = new Intent();
		if (urlParams != null && !urlParams.trim().isEmpty()) {
			String[] params = urlParams.split("&");
			for (String param : params) {
				int sep = param.indexOf('=');
				if (sep > 0) {
					result.putExtra(param.substring(0, sep), param.substring(sep + 1));
				}
			}
		}
		return result;
	}

	/** Cierra la actividad liberando recursos. */
	public void closeActivity() {
		finish();
	}



//	private ProgressDialog progressDialog = null;
//	ProgressDialog getProgressDialog() {
//		return this.progressDialog;
//	}
//	void setProgressDialog(final ProgressDialog pd) {
//		this.progressDialog = pd;
//	}

//	/** Muestra un di&aacute;logo de espera con un mensaje. */
//	public void showProgressDialog(final String message, final ClaveLoginTask cclt) {
//		runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					setProgressDialog(ProgressDialog.show(ClaveWebViewActivity.this, null, message, true));
//					if(cclt != null){
//						getProgressDialog().setOnKeyListener(new OnKeyListener() {
//							@Override
//							public boolean onKey(final DialogInterface dlg, final int keyCode, final KeyEvent event) {
//								if (keyCode == KeyEvent.KEYCODE_BACK) {
//									cclt.cancel(true);
//									dismissProgressDialog();
//									closeActivity();
//									return true;
//								}
//								return false;
//							}
//						});
//					}
//				} catch (final Exception e) {
//					PfLog.e(SFConstants.LOG_TAG, "No se ha podido mostrar el dialogo de progreso: " + e); //$NON-NLS-1$
//				}
//			}
//		});
//	}


//	/** Cierra el di&aacute;logo de espera en caso de estar abierto. */
//	void dismissProgressDialog() {
//		if (getProgressDialog() != null) {
//			runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
//					getProgressDialog().dismiss();
//				}
//			});
//		}
//	}

//	/**
//	 * Muestra un mensaje de advertencia al usuario de que se ha producido un error en el login.
//	 * @param message Mensaje que se desea mostrar.
//	 */
//	private void showErrorDialog(final String message) {
//		dismissProgressDialog();
//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setTitle(getString(R.string.aviso));
//		builder.setMessage(message);
//		builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int id) {
//			}
//		});
//		AlertDialog alert = builder.create();
//		alert.show();
//	}

	@Override
	public void onBackPressed() {
		// Preguntamos si debe cerrarse la sesion
		//showConfirmExitDialog();

		setResult(Activity.RESULT_CANCELED);
		super.onBackPressed();
	}
//
//	/**
//	 * Muestra un mensaje al usuario pidiendo confirmacion para cerrar la
//	 * sesi&oacute;n del usuario.
//	 */
//	private void showConfirmExitDialog() {
//
//		dismissProgressDialog();
//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setTitle(getString(R.string.dialog_title_close_session));
//		builder.setMessage(R.string.dialog_msg_close_session);
//		builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int id) {
//				try {
//					LogoutRequestTask lrt = new LogoutRequestTask(CommManager.getInstance());
//					lrt.execute();
//				} catch (Exception e) {
//					PfLog.e(SFConstants.LOG_TAG,
//							"No se ha podido cerrar sesion: " + e); //$NON-NLS-1$
//				}
//				closeActivity();
//			}
//		});
//		builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int id) {
//			}
//		});
//		AlertDialog alert = builder.create();
//		alert.show();
//	}
}
