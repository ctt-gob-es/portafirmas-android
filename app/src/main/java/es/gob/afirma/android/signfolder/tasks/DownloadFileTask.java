package es.gob.afirma.android.signfolder.tasks;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.util.Locale;

import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.DocumentData;
import es.gob.afirma.android.signfolder.tasks.SaveFileTask.SaveFileListener;
import es.gob.afirma.android.util.PfLog;

/** Tarea as&iacute;ncrona para la previsualizaci&oacute;n de documentos. */
public final class DownloadFileTask extends AsyncTask<Void, Void, DocumentData> implements SaveFileListener {

	private static final String DEFAULT_TEMP_DOCUMENT_PREFIX = "temp";  //$NON-NLS-1$

	private static final String PDF_MIMETYPE = "application/pdf"; //$NON-NLS-1$
	private static final String PDF_FILE_EXTS = ".pdf"; //$NON-NLS-1$

	private final String documentId;
	private final int type;
	private final boolean extDir;
	private final String proposedName;
	private final String mimetype;
	private final CommManager commManager;
	private final DownloadDocumentListener listener;
	private final Context context;

	/** Documento de datos. */
	public static final int DOCUMENT_TYPE_DATA = 1;

	/** Documento de firma. */
	public static final int DOCUMENT_TYPE_SIGN = 2;

	/** Informe de firma. */
	public static final int DOCUMENT_TYPE_REPORT = 3;

	/**
	 * Listener utilizado para detectar el resultado de una peticion de descarga de fichero para
	 * visualizaci&oacute;n.
	 */
	public interface DownloadDocumentListener {

		/** Cuando el documento se ha descargado correctamente.
		 * @param documentFile Documento que hay que visualizar.
		 * @param filename Nombre del documento.
		 * @param mimetype MimeType del documento.
		 * @param docType Tipo de documento (datos, firma o informe).
		 * @param  externalDir Indica si se almacena el fichero en un directorio externo o en
		 *                     un directorio interno de la aplicaci&oacute;n. */
		void downloadDocumentSuccess(File documentFile, String filename, String mimetype, int docType, boolean externalDir);

		/** Cuando ocurri&oacute; un error al descargar el documento. */
		void downloadDocumentError();
	}

	/**
	 * Crea una tarea as&iacute;ncrona para la descarga de un documento. Al construir la tarea
	 * se indica si queremos almacenar el documento en el almacenamiento interno de la
	 * aplicaci&oacute;n o en el externo (directorio de descargas del dispositivo). El nombre
	 * propuesto del documento solo se atender&aacute;a cuando se almacene en el directorio externo.
	 * @param documentId Identificador del documento que se desea previsualizar.
	 * @param type Tipo de documento (datos, firma o informe).
	 * @param proposedName Nombre propuesto para el fichero.
	 * @param extDir Descargar en directorio de descargas externo ({@code true}) o en el directorio de datos interno ({@code false}).
	 * @param commManager Manejador de los servicios de comunicaci&oacute;n con el portafirmas.
	 * @param listener Listener que procesa las notificaciones con el resultado de la operaci&oacute;n.
	 * @param context Contexto sobre el que mostrar las notificaciones.
	 */
	public DownloadFileTask(final String documentId, final int type, final String proposedName,
							final String mimetype, final boolean extDir, final CommManager commManager,
							final DownloadDocumentListener listener, final Context context) {
		this.documentId = documentId;
		this.type = type;
		this.proposedName = proposedName;
		this.mimetype = mimetype;
		this.extDir = extDir;
		this.commManager = commManager;
		this.listener = listener;
		this.context = context;
	}

	@Override
	protected DocumentData doInBackground(final Void... args) {

		DocumentData documentData;
		try {
			switch (this.type) {
			case DOCUMENT_TYPE_SIGN:
				documentData = this.commManager.getPreviewSign(this.documentId,
						this.proposedName);
				break;
			case DOCUMENT_TYPE_REPORT:
				documentData = this.commManager.getPreviewReport(this.documentId,
						this.proposedName, PDF_MIMETYPE);
				break;
			default:
				documentData = this.commManager.getPreviewDocument(this.documentId,
						this.proposedName, this.mimetype);
			}

		} catch (final Exception e) {
    		PfLog.w(SFConstants.LOG_TAG, "No se pudo descargar el documento para su previsualizacion: " + e); //$NON-NLS-1$
    		return null;
    	}

		return documentData;
	}

	@Override
	protected void onPostExecute(final DocumentData documentData) {

		if (isCancelled()) {
			return;
		}

		if (documentData == null) {
			this.listener.downloadDocumentError();
			return;
		}

		// Una vez tenemos la respuesta del servicio, guardamos el fichero
		String suffix = null;
		if (documentData.getFilename() != null && documentData.getFilename().indexOf('.') != -1) {
			suffix = documentData.getFilename().substring(documentData.getFilename().lastIndexOf('.'));
		}

		String filename = this.proposedName;
		if (filename == null) {
			filename = DEFAULT_TEMP_DOCUMENT_PREFIX;
			if (suffix != null) {
				filename += suffix;
			}
		}

		// Adobe Reader parece tener problemas para abrir documentos PDF que no tienen
		// extension .pdf, asi que, cuando se vaya a guardar un documento para abrirlo despues,
		// nos asegiraremos de que tiene esa extension
		if (!this.extDir
				&& PDF_MIMETYPE.equalsIgnoreCase(this.mimetype)
				&& !filename.toLowerCase(Locale.ROOT).endsWith(PDF_FILE_EXTS)) {
			filename += PDF_FILE_EXTS;
		}

		// Guardamos el documento. En caso de guardarse en un directorio interno, se guardara como
		// temporal, ya que sera un documento que unicamente se va a visualizar para consulta
		new SaveFileTask(documentData.getDataIs(), filename, this.extDir, this).execute();
	}

	@Override
	public void saveFileSuccess(File outputFile) {
		this.listener.downloadDocumentSuccess(outputFile, outputFile.getName(), this.mimetype, this.type, this.extDir);
	}

	@Override
	public void saveFileError(String filename) {
		this.listener.downloadDocumentError();
	}
}
