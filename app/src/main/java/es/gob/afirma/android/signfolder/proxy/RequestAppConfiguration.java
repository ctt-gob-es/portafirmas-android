package es.gob.afirma.android.signfolder.proxy;

import java.util.ArrayList;

/** Configuracion de la aplicaci&oacute;n. */
public final class RequestAppConfiguration {

	private ArrayList<String> appIdsList;
	private ArrayList<String> appNamesList;

	RequestAppConfiguration() {
		this.appIdsList = new ArrayList<>();
		this.appNamesList = new ArrayList<>();
	}

	/** Obtiene la lista de identificadores de la aplicaci&oacute;n.
	 * @return Lista de identificadores de la aplicaci&oacute;n. */
	public ArrayList<String> getAppIdsList() {
		return this.appIdsList;
	}

	void setAppIdsList(final ArrayList<String> appIdsList) {
		this.appIdsList = appIdsList;
	}

	/** Obtiene la lista de nombres de la aplicaci&oacute;n.
	 * @return Lista de nombres de la aplicaci&oacute;n. */
	public ArrayList<String> getAppNamesList() {
		return this.appNamesList;
	}

	void setAppNamesList(final ArrayList<String> appNamesList) {
		this.appNamesList = appNamesList;
	}
}
