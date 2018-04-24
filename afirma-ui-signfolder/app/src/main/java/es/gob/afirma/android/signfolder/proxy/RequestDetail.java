package es.gob.afirma.android.signfolder.proxy;

import java.util.Vector;

/** Datos identificados de una petici&oacute; para visualizar su detalle. */
public final class RequestDetail extends SignRequest {

	private String app;

	private String ref = null;

	private String signlinestype = null;

	private Vector<SignLine> signLines;

	private String rejectReason = null;

	RequestDetail(final String id) {
		super(id);
	}

	/** Recupera el nombre de la aplicaci&oacute;n que solicit&oacute; la firma.
	 * @return Nombre de la aplicaci&oacute;n. */
	public String getApp() {
		return this.app;
	}

	/** Establece el nombre de la aplicaci&oacute;n que solicit&oacute; la firma.
	 * @param app Nombre de la aplicaci&oacute;n. */
	public void setApp(final String app) {
		this.app = app;
	}

	/** Recupera la referencia de la petici&oacute;n.
	 * @return Referencia de la aplicaci&oacute;n. */
	public String getRef() {
		return this.ref;
	}

	/** Establece la referencia de la petici&oacute;n.
	 * @param ref Referencia de la aplicaci&oacute;n. */
	public void setRef(final String ref) {
		this.ref = ref;
	}

	/** Recupera el listado de l&iacute;neas de firma de la petici&oacute;n. Las l&iacute;neas de firma
	 * se componen de un listado de nombre de usuarios por los que debe pasar o ha pasado la firma.
	 * @return Listado de l&iacute;neas de firma. */
	public Vector<SignLine> getSignLines() {
		return this.signLines;
	}

	/** Establece el listado de l&iacute;neas de firma de la petici&oacute;n. Las l&iacute;neas de firma
	 * se componen de un listado de nombre de usuarios por los que debe pasar o ha pasado la firma.
	 * @param signLines Listado de l&iacute;neas de firma. */
	public void setSignLines(final Vector<SignLine> signLines) {
		this.signLines = signLines;
	}

	/** Recupera el tipo de l&iacute;neas de firma.
	 * @return Referencia del tipo de l&iacute;neas de firma. */
	public String getSignLinesType() {
		return this.signlinestype;
	}

	/** Establece el tipo de l&iacute;neas de firma.
	 * @param signlinestype Referencia del tipo de l&iacute;neas de firma. */
	public void setSignLinesType(final String signlinestype) {
		this.signlinestype = signlinestype;
	}

	/** Recupera el motivo de rechazo de la petici&oacute;n.
	 * @return Motivo del rechazo o {@code null} si la peticion no esta rechazada o no se especific&oacute;. */
	public String getRejectReason() {
		return this.rejectReason;
	}

	/** Establece la referencia de la petici&oacute;n.
	 * @param ref Referencia de la aplicaci&oacute;n. */
	public void setRejectReason(final String rejectReason) {
		this.rejectReason = rejectReason;
	}
}
