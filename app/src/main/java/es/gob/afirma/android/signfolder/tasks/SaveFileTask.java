package es.gob.afirma.android.signfolder.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.util.PfLog;

/** Tarea para descarga de fichero en segundo plano. */
public class SaveFileTask extends AsyncTask<Void, Void, File> {

	private final InputStream dataIs;
	private final String filename;
	private final SaveFileListener listener;
	private final Activity activity;

	/** Crea una tarea para descarga de fichero en segundo plano.
	 * @param dataIs Flujo de lectura de los datos del fichero.
	 * @param filename Nombre del fichero a guardar.
	 * @param listener Clase a la que notificar el sesultado de la tarea.
	 * @param activity Actividad que invoca a la tarea.
	 */
	public SaveFileTask(final InputStream dataIs, final String filename, final SaveFileListener listener, final Activity activity) {
		this.dataIs = dataIs;
		this.filename = filename;
		this.listener = listener;
		this.activity = activity;
	}

	@Override
	protected File doInBackground(final Void... arg0) {

		if (!isExternalStorageWritable()) {
            PfLog.e(SFConstants.LOG_TAG, "No se encuentra disponible el almacenamiento externo para guardar del fichero"); //$NON-NLS-1$
		    return null;
        }

        File outFile = null;
        int i = 0;
        do {
            outFile = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    generateFileName(this.filename, i++)
            );
        } while (outFile.exists());

        PfLog.i(SFConstants.LOG_TAG, "Se intenta guardar en disco el fichero: " + outFile.getAbsolutePath()); //$NON-NLS-1$

        try {
            final FileOutputStream fos = new FileOutputStream(outFile);
            writeData(this.dataIs, fos);
            fos.close();
            this.dataIs.close();
        }
        catch (final Exception e) {
            PfLog.e(SFConstants.LOG_TAG, "Error al guardar el fichero en un directorio externo: " + e); //$NON-NLS-1$
            return null;
        }

		PfLog.i(SFConstants.LOG_TAG, "Fichero guardado con exito: " + outFile.exists());

		return outFile;
	}

	/**
	 * Comprueba si el almac&eacute;n externo esta disponible para lectura y escritura.
	 * @return {@code true} si est&aacute; disponible, {@code false} en caso contrario.
	 */
	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	/**
	 * Escribe los datos de un flujo de entrada en uno de salida.
	 * @param is Flujo de entrada.
	 * @param os Flujo de salida.
	 * @throws IOException Cuando ocurre un error.
	 */
	private static void writeData(final InputStream is, final OutputStream os) throws IOException {
		int n = -1;
		final byte[] buffer = new byte[1024];
		while ((n = is.read(buffer)) > 0) {
			os.write(buffer, 0, n);
		}
	}

	@Override
	protected void onPostExecute(final File result) {

		if (result == null) {
			this.listener.saveFileError(this.filename);
		}
		else {
			this.listener.saveFileSuccess(result);
		}
	}

	/**
	 * Genera un nombre de fichero agregando un indice al final del nombre propuesto. Si el
	 * &iacute;ndice es menor o igual a 0, se devuelve el nombre indicado.
	 * @param docName Nombre inicial del fichero.
	 * @param index &Iacute;ndice que agregar.
	 * @return Nombre generado.
	 */
	private static String generateFileName(final String docName, final int index) {
		if (index <= 0) {
			return docName;
		}

		final int lastDocPos = docName.lastIndexOf('.');
		if (lastDocPos == -1) {
			return docName + '(' + index + ')';
		}

		return docName.substring(0, lastDocPos) + '(' + index + ')' + docName.substring(lastDocPos);
	}

	public interface SaveFileListener {

		void saveFileSuccess(File outputFile);

		void saveFileError(String filename);
	}
}