package es.gob.afirma.android.signfolder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import es.gob.afirma.android.signfolder.ClaveCheckLoginTask.ClaveCheckLoginListener;
import es.gob.afirma.android.signfolder.proxy.CommManager;

/** Actividad para entrada con usuario y contrase&ntilde;a en Cl@ve. */
public final class ClaveWebViewActivity extends Activity implements ClaveCheckLoginListener {

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


		WebSettings webSettings = webView.getSettings();
		webSettings.setBuiltInZoomControls(true);


		String url = super.getIntent().getExtras().getString("urlString");
		webView.loadUrl(url);
		webView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if(view != null && view.getHitTestResult() != null && view.getHitTestResult().getType() > 0){
					// Si el usuario hace click en algun enlace de la pagina
					ClaveCheckLoginTask cct = new ClaveCheckLoginTask(ClaveWebViewActivity.this, ClaveWebViewActivity.this);
					showProgressDialog(getString(R.string.dialog_msg_clave), cct);
					cct.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

					//En el post execute de la tarea obtendria el dni del usuario con una nueva tarea
					return true;
				} else {
					// Sin que el usuario interaccione
					return false;
				}
			}
			private int counter = 0;
			@Override
			public void onPageFinished(WebView view, String url) {
				if(counter == 0) {
					counter++;
				}
				else {
					// La segunda vez significa que se ha enviado un formulario de la pagina
					ClaveCheckLoginTask cct = new ClaveCheckLoginTask(ClaveWebViewActivity.this, ClaveWebViewActivity.this);
					showProgressDialog(getString(R.string.dialog_msg_clave), cct);
					cct.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
			}
		});
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

	@Override
	public void onBackPressed() {
		// Preguntamos si debe cerrarse la sesion
		showConfirmExitDialog();
	}

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
