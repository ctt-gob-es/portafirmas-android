package es.gob.afirma.android.signfolder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.webkit.WebView;

import java.util.Timer;
import java.util.TimerTask;

import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.RequestDetail;
import es.gob.afirma.android.signfolder.proxy.RequestResult;

/** Tarea de conexi&oacute;n con clave en Android. */
public final class ClaveCheckLoginTask extends AsyncTask<Void, Void, String> {

	static public Timer timer;
	static private ClaveCheckLoginListener listener;
	static private Context context;

	/**
	 * Comprueba que el login con clave se ha realizado con &eacute;xito.
	 */
	public ClaveCheckLoginTask(ClaveCheckLoginListener listener, Context context) {
		this.listener = listener;
		this.context = context;
	}

	class TaskKiller extends TimerTask {
		private AsyncTask<?, ?, ?> mTask;
		public TaskKiller(AsyncTask<?, ?, ?> task) {
			this.mTask = task;
		}

		public void run() {
			mTask.cancel(true);
			ClaveCheckLoginTask.listener.errorClaveLogin(ClaveCheckLoginTask.this);
		}
	}

	@Override
	protected String doInBackground(final Void... params) {
		timer = new Timer();
		timer.schedule(new TaskKiller(this), 20000);

		CommManager com = CommManager.getInstance();
		RequestResult rr;
		try {
			rr = com.claveConnectValidateLogin();
			timer.cancel();
			return rr.getId();
		} catch (Exception e) {
			e.printStackTrace();
		}
		timer.cancel();
		return null;
	}

	@Override
	protected void onPostExecute(final String valid) {
		if(!"ok".equalsIgnoreCase(valid)) {
			this.listener.errorClaveLogin(ClaveCheckLoginTask.this);
		}
		else {
			// Se cargan los datos
			this.listener.closeActivity();
			final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setClass(context, PetitionListActivity.class);
			intent.putExtra(PetitionListActivity.EXTRA_RESOURCE_CERT_B64, "");
			intent.putExtra(PetitionListActivity.EXTRA_RESOURCE_CERT_ALIAS, "");
			intent.putExtra(PetitionListActivity.EXTRA_RESOURCE_APP_IDS, new String[]{"prueba1", "prueba2"});
			intent.putExtra(PetitionListActivity.EXTRA_RESOURCE_APP_NAMES, new String[]{"prueba1", "prueba2"});
			context.startActivity(intent);
			this.listener.dismissDialog();
		}
	}

	/** Interfaz con los metodos para gestionar los resultados de la peticion del detalle
	 * de una solicitud de firma. */
	interface ClaveCheckLoginListener {
		void showProgressDialog(String message, ClaveCheckLoginTask cclt);
		void errorClaveLogin(final ClaveCheckLoginTask cclt);
		void closeActivity();
		void dismissDialog();
	}
}
