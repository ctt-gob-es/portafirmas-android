package es.gob.afirma.android.signfolder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.util.PfLog;

/** Tarea as&iacute;ncrona para la descarga y apertura del documento de ayuda de la
 * aplicaci&oacuten. Si el documento ya se descargo previamente, se abrir&aacute; directamente. */
final class OpenHelpDocumentTask extends AsyncTask<Void, Void, File> {

	private static final String PDF_MIMETYPE = "application/pdf"; //$NON-NLS-1$

	private final FragmentActivity activity;

	/**
	 * Crea una tarea as&iacute;ncrona para la descarga y apertura del documento de ayuda. En caso
	 * de que el documento estuviese ya descargado, lo abrir&iacute;a directamente.
	 * @param activity Actividad sobre la que mostrar las notificaciones.
	 */
	OpenHelpDocumentTask(final FragmentActivity activity) {
		this.activity = activity;
	}

	FragmentActivity getActivity() {
		return this.activity;
	}

	@Override
	protected File doInBackground(final Void... args) {

		// Cargamos la URL externa del documento de ayuda
		String helpUrl = AppPreferences.getInstance().getHelpUrl();
		String helpFilename;
		try {
			helpFilename = Uri.parse(helpUrl).getLastPathSegment();
		}
		catch (Exception e) {
			PfLog.e(SFConstants.LOG_TAG, "La URL de ayuda no es valida: " + helpUrl, e);
			return null;
		}

		// Calculamos la ruta de guardado del documento de ayuda
		File helpFile = new File(
				Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
				helpFilename);

		boolean exist = helpFile.exists();

		// Si no esta descargado, lo descargamos
		try {
			if (!exist) {

				PfLog.i(SFConstants.LOG_TAG, "Descargamos el fichero de ayuda");

				CommManager comm = CommManager.getInstance();
				InputStream docIs = comm.getRemoteDocumentIs(helpUrl);
				FileOutputStream fos = new FileOutputStream(helpFile);

				int n;
				byte[] data = new byte[128 * 1024];
				while ((n = docIs.read(data)) > 0) {
					fos.write(data, 0, n);
				}
				docIs.close();
				fos.flush();
				fos.close();
			}
		}
		catch (SecurityException e) {
			PfLog.e(SFConstants.LOG_TAG, "No se pudo acceder al fichero o comprobar su existencia: " + helpFile.toString(), e);
			helpFile = null;
		}
		catch (IOException e) {
			PfLog.e(SFConstants.LOG_TAG, "No se pudo descargar el fichero: " + helpFile.toString(), e);
			helpFile = null;
		}

		// Abrimos el documento
		return helpFile;
	}

	@Override
	protected void onPostExecute(final File helpFile) {

		if (helpFile == null) {
			Toast.makeText(getActivity(), "No se ha podido cargar el fichero de ayuda", Toast.LENGTH_SHORT).show();
			return;
		}

		viewPdf(helpFile, getActivity());
	}

	private void viewPdf (final File file, final FragmentActivity activity) {
		final String adobePackage = "com.adobe.reader"; //$NON-NLS-1$
		final String gdrivePackage = "com.google.android.apps.viewer"; //$NON-NLS-1$
		boolean isGdriveInstalled = false;

		final Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		/*final Uri fileUri = FileProvider.getUriForFile(
				this.activity,
				this.activity.getPackageName() + ".fileprovider",
				file);*/
		final Uri fileUri = FileProvider.getUriForFile(
				this.activity,
				this.activity.getPackageName() + ".fileprovider",
				file);
		this.activity.grantUriPermission(this.activity.getPackageName(), fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
		intent.setDataAndType(fileUri, PDF_MIMETYPE);

		final PackageManager pm = activity.getPackageManager();
		final List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
		if (list.isEmpty()) {
			PfLog.w(SFConstants.LOG_TAG, "No hay visor pdf instalado"); //$NON-NLS-1$
			new AlertDialog.Builder(activity)
					.setTitle(R.string.error)
					.setMessage(R.string.no_pdf_viewer_msg)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialog, final int which) {
							// Cerramos la ventana
						}
					})
					.create().show();
		}
		else {

			for (final ResolveInfo resolveInfo : list) {
				if (resolveInfo.activityInfo.name.startsWith(adobePackage)) {
					intent.setPackage(resolveInfo.activityInfo.packageName);
					activity.startActivity(intent);
					return;
				}
				else if (resolveInfo.activityInfo.name.startsWith(gdrivePackage)) {
					intent.setPackage(resolveInfo.activityInfo.packageName);
					isGdriveInstalled = true;
				}
			}

			if (isGdriveInstalled) {
				activity.startActivity(intent);
				return;
			}

			PfLog.i(SFConstants.LOG_TAG, "Ni Adobe ni Gdrive instalado"); //$NON-NLS-1$
			new AlertDialog.Builder(activity)
					.setTitle(R.string.aviso)
					.setMessage(R.string.no_adobe_reader_msg)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialog, final int which) {
							activity.startActivity(intent);
						}
					})
					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialog, final int which) {
							// Cerramos la ventana
						}
					})
					.create().show();
		}
	}
}
