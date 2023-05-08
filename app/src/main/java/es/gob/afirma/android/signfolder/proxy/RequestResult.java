package es.gob.afirma.android.signfolder.proxy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Resultado de una operaci&oacute;n particular sobre una petici&oacute;n.
 */
public class RequestResult {

	private final String id;

	private final boolean statusOk;

	/** Identificador de sesi&oacute;n compartida. */
	private String ssid;

	/** Listado de permisos que debe dar el usuario para completar las firmas. */
	private final Set<SignaturePermission> permissionsNeeded;

	/** Prefirmas de la petici&oacute;n. */
	private TriphaseRequest[] triphaseRequests;

	/**
	 * Resultado de una petici&oacute;n particular.
	 * @param id Identificador de la petici&oacute;n.
	 * @param ok Resultado de la petici&oacute;n.
	 */
	public RequestResult(final String id, final boolean ok) {
		this(id, ok, null, null);
	}

	/**
	 * Resultado de una petici&oacute;n particular.
	 * @param id Identificador de la petici&oacute;n.
	 * @param ok Resultado de la petici&oacute;n.
	 * @param ssid Identificador de sesi&oacute;n compartida.
	 */
	public RequestResult(final String id, final boolean ok, final String ssid) {
		this(id, ok, ssid, null);
	}

	/**
	 * Resultado de una petici&oacute;n particular.
	 * @param id Identificador de la petici&oacute;n.
	 * @param ok Resultado de la petici&oacute;n.
	 * @param ssid Identificador de sesi&oacute;n compartida.
	 * @param permissionsNeeded Listado de permisos que el usuario debera solicitar para que
	 *                          completar la firma de una petici&oacute;n.
	 */
	public RequestResult(final String id, final boolean ok, final String ssid,
			 Set<SignaturePermission> permissionsNeeded) {
		this.id = id;
		this.statusOk = ok;
		this.ssid = ssid;
		this.permissionsNeeded = permissionsNeeded != null ? new HashSet<>(permissionsNeeded) : null;
	}

	/**
	 * Recupera el identificador de la petici&oacute;n.
	 * @return Identificador.
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Recupera el resultado de la operaci&oacute;n sobre la petici&oacute;n.
	 * @return {@code true} si la operaci&oacute;n finaliz&oacute; correctamente, {@code false}
	 * en caso contrario.
	 */
	public boolean isStatusOk() {
		return this.statusOk;
	}

	/**
	 * Recupera el identificador de sesi&oacute;n compartida.
	 * @return Identificador de sesi&oacute;n compartida.
	 */
	public String getSsid() { return this.ssid; }

	/**
	 * Establece el identificador de sesi&oacute;n compartida.
	 * @param ssid Identificador de sesi&oacute;n compartida.
	 */
	public void setSsid(String ssid) {
		this.ssid = ssid;
	}

	/**
	 * Recupera el listado de permisos que se le deben pedir al usuario para completar la firma
	 * de la petici&oacute;n.
	 * @return Listado de permisos.
	 */
	public Set<SignaturePermission> getPermissionsNeeded() {
		return this.permissionsNeeded;
	}
}
