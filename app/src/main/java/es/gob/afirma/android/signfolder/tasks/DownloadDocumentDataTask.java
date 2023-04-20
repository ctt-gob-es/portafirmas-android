package es.gob.afirma.android.signfolder.tasks;

import android.content.Context;
import android.os.AsyncTask;


import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.DocumentData;
import es.gob.afirma.android.util.PfLog;

/** Tarea as&iacute;ncrona para compartir documentos. */
public final class DownloadDocumentDataTask extends AsyncTask<Void, Void, DocumentData> {

	private static final String PDF_MIMETYPE = "application/pdf"; //$NON-NLS-1$

	private final String documentId;
	private final int type;
	private final String proposedName;
	private final String mimetype;
	private final CommManager commManager;
	private final Context context;

	/** Documento de firma. */
	public static final int DOCUMENT_TYPE_SIGN = 2;

	/** Informe de firma. */
	public static final int DOCUMENT_TYPE_REPORT = 3;

	/**
	 * Crea una tarea as&iacute;ncrona para compartir un documento. 	 * @param documentId Identificador del documento que se desea previsualizar.
	 * @param type Tipo de documento (datos, firma o informe).
	 * @param proposedName Nombre propuesto para el fichero.
	 * @param commManager Manejador de los servicios de comunicaci&oacute;n con el portafirmas.
	 * @param context Contexto sobre el que mostrar las notificaciones.
	 */
	public DownloadDocumentDataTask(final String documentId, final int type, final String proposedName,
									final String mimetype, final CommManager commManager, final Context context) {
		this.documentId = documentId;
		this.type = type;
		this.proposedName = proposedName;
		this.mimetype = mimetype;
		this.commManager = commManager;
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

		if (isCancelled() || documentData == null) {
			return;
		}
	}

}
