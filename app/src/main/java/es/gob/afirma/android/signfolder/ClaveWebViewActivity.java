package es.gob.afirma.android.signfolder;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import es.gob.afirma.android.util.PfLog;

/** Actividad para entrada con usuario y contrase&ntilde;a en Cl@ve. */
public final class ClaveWebViewActivity extends Activity {

	private static final boolean DEBUG = ApplicationInfo.FLAG_DEBUGGABLE != 0;

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

			@TargetApi(23)
			@Override
			public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
				String errorMsg = null;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					PfLog.e(SFConstants.LOG_TAG, String.format(Locale.ENGLISH,
							"Error %1d recibido en el WebView con la descripcion: %2s",
							error.getErrorCode(), error.getDescription()));
					errorMsg = error.getDescription() != null ?
							error.getDescription().toString() : "Error en el WebView";
				}
				else {
					PfLog.e(SFConstants.LOG_TAG, "Error recibido en el WebView");
				}
				Intent result = createErrorIntent("claveerror", errorMsg);
				setResult(Activity.RESULT_FIRST_USER, result);
				closeActivity();
			}

			@Override
			public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
				PfLog.w(SFConstants.LOG_TAG,"No se ha podido cargar la URL requerida");
				closeByStatusError(errorResponse);
			}

			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
				if (DEBUG) {
					PfLog.i(SFConstants.LOG_TAG, "Se ignora error SSL en la pagina cargada");
					handler.proceed();
					return;
				}
				PfLog.e(SFConstants.LOG_TAG, String.format(Locale.ENGLISH,
						"Error de SSL (%1d) recibido en el WebView con la URL: %2s",
						error.getPrimaryError(), error.getUrl()));
				Intent result = createErrorIntent("claveerror", "Error SSL en la conexion con Cl@ve");
				setResult(Activity.RESULT_FIRST_USER, result);
				closeActivity();
			}

			@Override
			public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {

				PfLog.e(SFConstants.LOG_TAG,"Error al autenticarse en la Web cargada");
				Intent result = createErrorIntent("claveerror", "Error al autenticar al usuario en la pagina");
				setResult(Activity.RESULT_FIRST_USER, result);
				closeActivity();
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

		if (url == null) {
			PfLog.e(SFConstants.LOG_TAG, "No se ha proporcionado la URL que cargar en el WebView");
			Intent result = createErrorIntent("arguments", "No se encuentra la URL a cargar");
			setResult(Activity.RESULT_FIRST_USER, result);
			closeActivity();
			return;
		}

		PfLog.i(SFConstants.LOG_TAG, "---- Cookie con el que se inicio la sesion: " + cookieId);

		// Si se ha indicado un id de cookie de sesi&oacute;n, lo asociamos a la URL y configuramos
		// unas cabeceras de petici&oacute;n para el uso de esa cookie

		Map<String, String> headers = null;
		if (cookieId != null) {
			PfLog.w(SFConstants.LOG_TAG, "---- Id de cookie cargada: " + cookieId);
			headers = configureCookies(webView, url, cookieId);
		}
		webView.loadUrl(url, headers);
	}

	@TargetApi(21)
	private void closeByStatusError(WebResourceResponse errorResponse) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			PfLog.w(SFConstants.LOG_TAG, "Status Error: " + errorResponse.getStatusCode());
		}
		Intent result = createErrorIntent("claveerror",
				"Error recibido del servicio en la nube");
		setResult(Activity.RESULT_FIRST_USER, result);
		closeActivity();
	}

	/**
	 * Configura la cookies de sesi&oacute;n en un WebView.
	 * @param webView WebView en el que se van a cargar la URL con las coIdentificador de la cookie
	 *                   de sesi&oacute;n a utilizar.
	 * @param url URL para la que se desean utilizar las cookies.
	 * @param cookieId Identificador de la cookie de sesi&oacute;n a utilizar.
	 * @return Cabeceras que proporcionar al WebView para realizar la carga de la URL.
	 */
	@TargetApi(21)
	private Map<String, String> configureCookies(WebView webView, String url, String cookieId) {
		final CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			cookieManager.setAcceptThirdPartyCookies(webView, true);
		}

		PfLog.w(SFConstants.LOG_TAG, "---- Cookie del WebView antes de establecerla: " + cookieManager.getCookie(url));

		if (cookieId != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				cookieManager.removeSessionCookies(new RemoveCookiesCallback());
			}
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

	private static Intent createErrorIntent(String type, String message) {
		Intent result = new Intent();
		result.putExtra("type", type);
		if (message != null) {
			result.putExtra("msg", message);
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

	/**
	 * Clase para conocer el resultado de la eliminaci&oacute;n de las cockies de sesi&oacute;n.
	 */
	private class RemoveCookiesCallback implements ValueCallback<Boolean> {
		@Override
		public void onReceiveValue(Boolean value) {
			if (!value.equals(Boolean.TRUE)) {
				PfLog.w(SFConstants.LOG_TAG, "Error al eliminar una de las cookies de sesion");
			}
		}
	}
}
