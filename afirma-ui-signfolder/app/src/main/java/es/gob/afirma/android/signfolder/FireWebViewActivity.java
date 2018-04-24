package es.gob.afirma.android.signfolder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import es.gob.afirma.android.signfolder.ClaveCheckLoginTask.ClaveCheckLoginListener;
import es.gob.afirma.android.signfolder.proxy.CommManager;

/** Actividad para entrada con usuario y contrase&ntilde;a en Cl@ve. */
public final class FireWebViewActivity extends Activity implements ClaveCheckLoginListener {

	public static final int REQUEST_CODE = 7;

	public static final String EXTRA_PARAM_URL = "url";

	public static final String EXTRA_PARAM_TRANSACTION_ID = "trId";

	/** Dialogo para mostrar mensajes al usuario */
	private MessageDialog messageDialog = null;

	/** Tag para la presentaci&oacute;n de di&aacute;logos */
	private final static String DIALOG_TAG = "dialog"; //$NON-NLS-1$

	/** Di&aacute;logo para confirmar el cierre de la sesi&oacute;n. */
	private final static int DIALOG_CONFIRM_EXIT = 12;

	MessageDialog getMessageDialog() {
		return this.messageDialog;
	}

	public void onCreate(Bundle savedInstanceState) {
		final Context context = this;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webview);

		WebView webView = (WebView) findViewById(R.id.webView);

//		WebSettings webSettings = webView.getSettings();
//		webSettings.setBuiltInZoomControls(true);

		Bundle extras = super.getIntent().getExtras();
		String url = extras.getString(EXTRA_PARAM_URL);
		String trId = extras.getString(EXTRA_PARAM_TRANSACTION_ID);

		Log.w(SFConstants.LOG_TAG, "------------ URL cargada en el WebView: " + url);
		Log.w(SFConstants.LOG_TAG, "------------ Transaccion: " + trId);

		webView.setWebViewClient(new WebViewClient(){

			/* Comportamiento al cargar una nueva URL. */
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {

				Log.w(SFConstants.LOG_TAG, "---- Redireccion a: " + url);

				if (url.endsWith("/ok")) {
					FireWebViewActivity.this.setResult(Activity.RESULT_OK);
					FireWebViewActivity.this.finish();
					return true;
				}
				else if (url.endsWith("/error")) {
					Intent intentData = new Intent();
					intentData.putExtra("msg", "FIRe devolvi\u00F3 un error durante la autorizaci\u00F3n del usuario");
					FireWebViewActivity.this.setResult(Activity.RESULT_FIRST_USER, intentData);
					FireWebViewActivity.this.finish();
					return true;
				}
				return false;
			}

			/*
			// TODO: ESTO SIRVE PARA PERMITIR MOSTRAR PAGINAS WEB INSEGURAS Y DEBERIA ELIMINARSE
			// EN LA VERSION DE PRODUCCION
			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
				handler.proceed();
			}

			*/
		});


		webView.loadUrl(url);
	}

	private ProgressDialog progressDialog = null;
	ProgressDialog getProgressDialog() {
		return this.progressDialog;
	}
	void setProgressDialog(final ProgressDialog pd) {
		this.progressDialog = pd;
	}

	/** Muestra un di&aacute;logo de espera con un mensaje. */
	@Override
	public void showProgressDialog(final String message, final ClaveCheckLoginTask cclt) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					setProgressDialog(ProgressDialog.show(FireWebViewActivity.this, null, message, true));
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
	@Override
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

	@Override
	public void errorClaveLogin(final ClaveCheckLoginTask cclt) {
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

	@Override
	public void dismissDialog() {
		dismissProgressDialog();
	}

/*
	@Override
	public void onBackPressed() {
		// Preguntamos si debe cerrarse la sesion
		showConfirmExitDialog();
	}
*/
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
