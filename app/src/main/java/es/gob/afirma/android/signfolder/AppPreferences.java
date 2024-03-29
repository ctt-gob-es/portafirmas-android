package es.gob.afirma.android.signfolder;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import es.gob.afirma.android.util.PfLog;

/** Preferencias de la aplicaci&oacute;n. */
public final class AppPreferences {

	private static final String CONFIG_PROPERTIES = "config.properties"; //$NON-NLS-1$

	/** Tiempo maximo que se va a esperar por una respuesta del proxy. */
	private static final int DEFAULT_CONNECTION_READ_TIMEOUT = 30000;

	private static final String KEY_CONNECTION_READ_TIMEOUT = "connection.timeout"; //$NON-NLS-1$

	private static final String KEY_FORMATS_SUPPORTED = "supported.format"; //$NON-NLS-1$

	private static final String KEY_HELP_URL = "help.url"; //$NON-NLS-1$

	/** &Uacute;ltimo certificado utilizado. */
	private static final String LAST_CERT = "lastCert"; //$NON-NLS-1$

	/** Clave de preferencia del almac&eacute;n de claves. */
	private static final String PREFERENCES_KEY_KEYSTORE = "keystore";

	/** Almac&eacute;n de claves local. */
	public static final String KEYSTORE_LOCAL = "local";

	/** Almac&eacute;n de claves remoto. */
	public static final String KEYSTORE_CLOUD = "cloud";

	/** Almac&eacute;n de claves DNIe. */
	public static final String KEYSTORE_DNIE = "dnie";


	/** Clave con la que se almacena el indicador de que esta es la primera ejecuci&oacute;n. */
	private static final String FIRST_EXECUTION = "firstExecution"; //$NON-NLS-1$

	/** Clave de preferencia de la URL del Proxy. */
	private static final String PREFERENCES_KEY_SELECTED_PROXY_URL = "URL_PROXY"; //$NON-NLS-1$

	/** Clave de preferencia de alias del proxy seleccionado . */
	private static final String PREFERENCES_KEY_SELECTED_PROXY_ALIAS = "alias"; //$NON-NLS-1$

	/** Clave de preferencia del conjunto de las huellas de los certificados. */
	private static final String PREFERENCES_KEY_TRUSTED_CERTS = "trustedSslCerts";

	/** Prefijo de clave de preferencia para registrar cuando un usuario tiene activas las
	 * notificaciones para un proxy. */
	public static final String PREFERENCES_KEY_PREFIX_NOTIFICATION_ACTIVE = "not_act_"; //$NON-NLS-1$

	public static final String PREFERENCES_KEY_PREFIX_NOTIFICATION_TOKEN = "not_tok_"; //$NON-NLS-1$

	/** Prefijo clave de preferencias de servidores. */
	private static final String PREFERENCES_KEY_PREFIX_SERVER = "server"; //$NON-NLS-1$

	/** Token actual para el env&iacute;o de notificaciones. */
	static final String PREFERENCES_KEY_CURRENT_TOKEN = "currentToken";

	/** Clave de preferencia del tipo de vista de las peticiones. */
	private static final String PREFERENCES_IS_COMPACT_VIEW = "isCompactView"; //$NON-NLS-1$

	private static final String CONFIG_SEPARATOR = ";"; //$NON-NLS-1$

	/** Alias del portafirmas por defecto de la AGE. */
	public static final String DEFAULT_PROXY_GOB_ALIAS = "Portafirmas General AGE";
	/** URL del portafirmas por defecto de la AGE. */
	public static final String DEFAULT_PROXY_GOB_URL = "https://servicios.seap.minhap.es/pfmovil/signfolder";
	/** Alias del portafirmas por defecto de RedSARA. */
	public static final String DEFAULT_PROXY_REDSARA_ALIAS = "Portafirmas RedSARA";
	/** URL del portafirmas por defecto de RedSARA. */
	public static final String DEFAULT_PROXY_REDSARA_URL = "https://portafirmas.redsara.es/pfmovil/pf";

	private static Properties config;

	private static AppPreferences mInstance;

	private static SharedPreferences sharedPref;

	private AppPreferences() { }

	public static AppPreferences getInstance(){
		if (mInstance == null) {
			mInstance = new AppPreferences();
		}
		return mInstance;
	}

	private static SharedPreferences getSharedPreference() {
		if (sharedPref == null) {
			sharedPref = PreferenceManager.getDefaultSharedPreferences(SignfolderApp.getAppContext());
		}
		return sharedPref;
	}

	private static Properties getConfig () {
		if (config == null) {
			config = new Properties();
			try {
				config.load(SignfolderApp.getAppContext().getAssets().open(CONFIG_PROPERTIES));
			} catch (final Exception e) {
				// Esto no deberia ocurrir nunca
				throw new RuntimeException("No se encuentra el fichero de configuracion " + CONFIG_PROPERTIES, e); //$NON-NLS-1$
			}
		}
		return config;
	}

	/**
	 * Establece el almac&eacute;n de claves configurado.
	 * @param keystore Almac&eacute;n de claves.
	 */
	public void setCertKeyStore(String keystore) {
		setPreference(PREFERENCES_KEY_KEYSTORE, keystore);
	}

	/**
	 * Recupera el almac&eacute;n de claves configurado.
	 * @return Almac&eacute;n de claves configurado.
	 */
	public String getCertKeyStore() {
		return getPreference(PREFERENCES_KEY_KEYSTORE, KEYSTORE_LOCAL);
	}

	/**
	 * Define el que se trate la próxima ejecución de la aplicación como si fuese la primera.
	 * @param  firstExecution {@code true} para que se considere la próxima ejecución como la primera,
	 * {@code false} para que se considere que la aplicación se ha ejecutado previamente.
	 */
	public void setFirstExecution(boolean firstExecution) {
		setPreferenceBool(FIRST_EXECUTION, firstExecution);
	}

	/**
	 * Recupera si esta ejecución de la aplicación debe considerarse como la primera.
	 * @return {@code true} si se considera esta la primera ejecución de la aplicación,
	 * {@code false} en caso contrario.
	 */
	public boolean isFirstExecution() {
		return getPreferenceBool(FIRST_EXECUTION, true);
	}

	/**
	 * Define el ultimo certificado con el que se accedio.
	 * @param  certEncoded Certificado del usuario.
	 */
	public void setLastCertificate(String certEncoded) {
		setPreference(LAST_CERT, certEncoded);
	}

	/**
	 * Recupera el tiempo de TimeOut configurado para las conexiones de red.
	 * @return Milisegundos de espera.
	 */
	public int getConnectionReadTimeout() {

		if (getConfig().containsKey(KEY_CONNECTION_READ_TIMEOUT)) {
			try {
				return Integer.parseInt(getConfig().getProperty(KEY_CONNECTION_READ_TIMEOUT));
			}
			catch (final NumberFormatException e) {
				PfLog.w(SFConstants.LOG_TAG,
						"Error en el timeout configurado en el fichero de propiedades. Se usara el por defecto: " + DEFAULT_CONNECTION_READ_TIMEOUT); //$NON-NLS-1$
			}
		}
		return DEFAULT_CONNECTION_READ_TIMEOUT;
	}

	/** Recupera el listado de formatos soportados.
	 * @return Listado de formatos soportados. */
	public String[] getSupportedFormats() {
		return getConfig().getProperty(KEY_FORMATS_SUPPORTED).split(CONFIG_SEPARATOR);
	}

	/** Recupera la URL del documento de ayuda de la aplicaci&oacute;n.
	 * @return URL del documento. */
	public String getHelpUrl() {
		return getConfig().getProperty(KEY_HELP_URL);
	}

	private String getPreference(final String key) {
		return getPreference(key, ""); //$NON-NLS-1$
	}

	public String getPreference(final String key, final String defaultValue) {
		return getSharedPreference().getString(key.trim(), defaultValue);
	}

	public void setPreference(final String key, final String value) {
		final SharedPreferences.Editor editor = getSharedPreference().edit();
		editor.putString(key.trim(), value);
		editor.apply();
	}

	public void setPreference(final String key, final Set<String> value) {
		final SharedPreferences.Editor editor = getSharedPreference().edit();
		editor.putStringSet(key.trim(), value);
		editor.apply();
	}

	public int getPreferenceInt(final String key, final int defaultValue) {
		return getSharedPreference().getInt(key.trim(), defaultValue);
	}

	public void setPreferenceInt(final String key, final int value) {
		final SharedPreferences.Editor editor = getSharedPreference().edit();
		editor.putInt(key.trim(), value);
		editor.apply();
	}

	public boolean getPreferenceBool(final String key, final boolean defaultValue) {
		return getSharedPreference().getBoolean(key.trim(), defaultValue);
	}

	public void setPreferenceBool(final String key, final boolean value) {
		final SharedPreferences.Editor editor = getSharedPreference().edit();
		editor.putBoolean(key.trim(), value);
		editor.apply();
	}

	private void removePreference(final String key) {
		final SharedPreferences.Editor editor = getSharedPreference().edit();
		editor.remove(key.trim());
		editor.apply();
	}

	/** Establece un proxy como el seleccionado.
	 * @param alias Alias del proxy.
	 * @param urlProxy URL del proxy. */
	public void setSelectedProxy(final String alias, final String urlProxy) {
		setPreference(PREFERENCES_KEY_SELECTED_PROXY_ALIAS, alias);
		setPreference(PREFERENCES_KEY_SELECTED_PROXY_URL, urlProxy);
	}

	/** Recupera la direcci&oacute;n del servidor proxy para la conexi&oacute;n con el Portafirmas.
	 * @return URL del Portafirmas. */
	public String getSelectedProxyUrl() {
		return getPreference(PREFERENCES_KEY_SELECTED_PROXY_URL, "");
	}

	/** Recupera el alias del proxy actualmente seleccionado.
	 * @return Alias del proxy o cadena vaci&iacute;a si no hay ninguno. */
	String getSelectedProxyAlias() {
		return getPreference(PREFERENCES_KEY_SELECTED_PROXY_ALIAS, "");
	}
	
	void saveServer(final String alias, final String url) {
		setPreference(PREFERENCES_KEY_PREFIX_SERVER + alias, url);
	}

	/**
	 * Devuelve la URL asignada al servidor.
	 * @param alias Alias del servidor.
	 * @return URL del servidor o cadena vac&iacute;a si no exist&iacute;a.
	 */
	public String getServer(final String alias) {
		return getPreference(PREFERENCES_KEY_PREFIX_SERVER + alias, "");
	}

	public void setCurrentToken(final String token) {
		setPreference(PREFERENCES_KEY_CURRENT_TOKEN, token);
	}

	public String getCurrentToken() {
		return getPreference(PREFERENCES_KEY_CURRENT_TOKEN);
	}

	void removeServer(final String alias) {
		removePreference(PREFERENCES_KEY_PREFIX_SERVER + alias);
	}
	
	void removeProxyConfig() {
		removePreference(PREFERENCES_KEY_SELECTED_PROXY_ALIAS);
		removePreference(PREFERENCES_KEY_SELECTED_PROXY_URL);
	}
	
	@NonNull
	public List<String> getServersList() {
		ArrayList<String> servers = new ArrayList<>();
		Map<String, ?> allPrefs = getSharedPreference().getAll();
	    Set<String> set = allPrefs.keySet();
	    for(String s : set){
	    	if (s.startsWith(PREFERENCES_KEY_PREFIX_SERVER)) {
	    		servers.add(s.replaceFirst("^" + PREFERENCES_KEY_PREFIX_SERVER,""));
	    	}
	    }
	    return servers;
	}

	public void addTrustedCertificate(X509Certificate cert) {
		Set<String> trustedCertsSaved = getSharedPreference().getStringSet(PREFERENCES_KEY_TRUSTED_CERTS, null);
		Set<String> trustedCerts = new HashSet<>();
		if (trustedCertsSaved != null) {
			trustedCerts.addAll(trustedCertsSaved);
		}
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			trustedCerts.add(String.format("%040x", new BigInteger(1, md.digest(cert.getEncoded()))));
		}
		catch (Exception e) {
			PfLog.w(SFConstants.LOG_TAG, "No se ha podido codificar el certificado para su guardado", e);
			return;
		}
		setPreference(PREFERENCES_KEY_TRUSTED_CERTS, trustedCerts);
	}

	public boolean isTrustedCertificate(X509Certificate cert) {

		String certKey;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			certKey = String.format("%040x", new BigInteger(1, md.digest(cert.getEncoded())));
		}
		catch (Exception e) {
			PfLog.w(SFConstants.LOG_TAG, "No se ha podido codificar el certificado para evaluar si es de confianza", e);
			return false;
		}

		Set<String> trustedCerts = getSharedPreference().getStringSet(PREFERENCES_KEY_TRUSTED_CERTS, null);
		return trustedCerts != null && trustedCerts.contains(certKey);
	}

	/**
	 * Establece los servidores proxy por defecto de la aplicaci&oacute;n. El del Portafirmas
	 * General de la AGE y el de RedSara.
	 */
	public void setDefaultServers() {
		saveServer(DEFAULT_PROXY_GOB_ALIAS, DEFAULT_PROXY_GOB_URL);
		saveServer(DEFAULT_PROXY_REDSARA_ALIAS, DEFAULT_PROXY_REDSARA_URL);

		setSelectedProxy(DEFAULT_PROXY_GOB_ALIAS, DEFAULT_PROXY_GOB_URL);
	}

	/**
	 * Establece el tipo de vista utilizado en las peticiones.
	 * @param isCompactView si es true se utiliza la compacta, false si es la expandida.
	 */
	public void setIsCompactView(boolean isCompactView) {
		setPreferenceBool(PREFERENCES_IS_COMPACT_VIEW, isCompactView);
	}

	/**
	 * Recupera el tipo de vista utilizado en las peticiones.
	 * @return si es true se utiliza la compacta, false si es la expandida.
	 */
	public boolean getIsCompactView() {
		return getPreferenceBool(PREFERENCES_IS_COMPACT_VIEW, true);
	}

}
