/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 11/01/11
 * You may contact the copyright holder at: soporte.afirma5@mpt.es
 */

package es.gob.afirma.android.network;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.util.AOUtil;
import es.gob.afirma.android.util.PfLog;

/** Implementacion de una clase para la lectura del contenido de una URL. */
public final class AndroidUrlHttpManager {


	static {
		final CookieManager cookieManager = new CookieManager();
		cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(cookieManager);
	}

	private AndroidUrlHttpManager() {
		// No permitimos la instanciacion
	}

	private static final HostnameVerifier DEFAULT_HOSTNAME_VERIFIER = HttpsURLConnection.getDefaultHostnameVerifier();
	private static final SSLSocketFactory DEFAULT_SSL_SOCKET_FACTORY = HttpsURLConnection.getDefaultSSLSocketFactory();

	private static final TrustManager[] DUMMY_TRUST_MANAGER = new TrustManager[] {
		new X509TrustManager() {
			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
			@Override
			public void checkClientTrusted(final X509Certificate[] certs, final String authType) { /* No hacemos nada */ }
			@Override
			public void checkServerTrusted(final X509Certificate[] certs, final String authType) {  /* No hacemos nada */  }
		}
	};

	/** Lee una URL HTTP o HTTPS por POST si se indican par&aacute;metros en la URL y por GET en caso contrario.
	 * En HTTPS no se hacen comprobaciones del certificado servidor.
	 * @param url URL a leer
	 * @param timeout Tiempo m&aacute;ximo en milisegundos que se debe esperar por la respuesta. Un timeout de 0
	 * se interpreta como un timeout infinito. Si se indica -1, se usar&aacute; el por defecto de Java.
	 * @return Contenido de la URL
	 * @throws IOException Si no se puede leer la URL */
	public static ConnectionResponse getRemoteDataByPost(final String url, final int timeout) throws IOException {
		return getRemoteDataByPost(url, timeout, null);
	}

	/** Lee una URL HTTP o HTTPS por POST si se indican par&aacute;metros en la URL y por GET en caso contrario.
	 * En HTTPS no se hacen comprobaciones del certificado servidor.
	 * @param url URL a leer
	 * @param timeout Tiempo m&aacute;ximo en milisegundos que se debe esperar por la respuesta. Un timeout de 0
	 * se interpreta como un timeout infinito. Si se indica -1, se usar&aacute; el por defecto de Java.
	 * @param headers Cabeceras que agregar a la petici&oacute;n.
	 * @return Contenido de la URL
	 * @throws IOException Si no se puede leer la URL */
	public static ConnectionResponse getRemoteDataByPost(final String url, final int timeout, Properties headers) throws IOException {
		if (url == null) {
			throw new IllegalArgumentException("La URL a leer no puede ser nula"); //$NON-NLS-1$
		}

		// Si la URL no tiene parametros la leemos por GET
		if (!url.contains("?")) { //$NON-NLS-1$
			return getRemoteDataByGet(url, timeout);
		}

		final StringTokenizer st = new StringTokenizer(url, "?"); //$NON-NLS-1$
		final String request = st.nextToken();
		final String urlParameters = st.nextToken();

		ConnectionResponse response;

		final URL uri = new URL(request);
		final HttpURLConnection conn = (HttpURLConnection) uri.openConnection(Proxy.NO_PROXY);
		try {
			conn.setConnectTimeout(timeout);
			conn.setReadTimeout(timeout);
			conn.setRequestMethod("POST"); //$NON-NLS-1$

			if (headers != null) {
				for (String key : headers.keySet().toArray(new String[0])) {
					conn.setRequestProperty(key, headers.getProperty(key));
				}
			}

			conn.setDoOutput(true);

			final OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

			writer.write(urlParameters);
			writer.flush();

			// Leemos los datos
			byte[] data = AOUtil.getDataFromInputStream(conn.getInputStream());

			// Componemos la respuesta
			response = new ConnectionResponse();
			response.setDataIs(new ByteArrayInputStream(data));
			response.setCookieId(extractCookieId(conn));
		}
		catch (SocketTimeoutException e) {
			PfLog.e(SFConstants.LOG_TAG, "Timeout", e);
			throw e;
		}
		finally {
			conn.disconnect();
		}

		return response;
	}

	/**
	 * Extrae el identificador de sesi&oacute;n declarado por una conexi&oacute;n.
	 * @param conn Conexi&oacute;n en base a la se puede haber generado una sesi&oacute;n.
	 * @return Identificado de sesi&oacute;n o {@code null} si no se declar&oacute;.
	 */
	private static String extractCookieId(HttpURLConnection conn) {

	    String cookieId = null;

		String cookieField = conn.getHeaderField("Set-Cookie");
		if (cookieField == null) {
			cookieField = conn.getHeaderField("Set-Cookie2");
		}

		PfLog.i(SFConstants.LOG_TAG, "Cookie: " + cookieField);

		if (cookieField != null) {
			String[] params = cookieField.split(";");
			for (String param : params) {
				if (param.trim().startsWith("JSESSIONID=")) {
					cookieId = param.trim();
					break;
				}
			}
		}
		return cookieId;
	}

	/** Lee una URL HTTP o HTTPS por GET. En HTTPS no se hacen comprobaciones del certificado servidor.
	 * @param url URL a leer
	 * @param timeout Tiempo m&aacute;ximo en milisegundos que se debe esperar por la respuesta. Un timeout de 0
	 * se interpreta como un timeout infinito. Si se indica -1, se usar&aacute; el por defecto de Java.
	 * @return Contenido de la URL
	 * @throws IOException Si no se puede leer la URL */
	static ConnectionResponse getRemoteDataByGet(final String url, final int timeout) throws IOException {
        if (url == null) {
            throw new IllegalArgumentException("La URL a leer no puede ser nula"); //$NON-NLS-1$
        }

        final URL uri = new URL(url);
        final HttpURLConnection conn = (HttpURLConnection) uri.openConnection(Proxy.NO_PROXY);
        conn.setConnectTimeout(timeout);
        conn.setReadTimeout(timeout);
        conn.setRequestMethod("GET"); //$NON-NLS-1$

		// Leemos los datos
		byte[] data = AOUtil.getDataFromInputStream(conn.getInputStream());

        // Componemos la respuesta
        final ConnectionResponse response = new ConnectionResponse();
        response.setDataIs(new ByteArrayInputStream(data));
        response.setCookieId(extractCookieId(conn));

        return response;
	}

	/** Habilita las comprobaciones por defecto de las conexiones SSL. */
	public static void enableSslChecks() {
		HttpsURLConnection.setDefaultSSLSocketFactory(DEFAULT_SSL_SOCKET_FACTORY);
		HttpsURLConnection.setDefaultHostnameVerifier(DEFAULT_HOSTNAME_VERIFIER);
	}

	/** Deshabilita las comprobaciones por defecto de las conexiones SSL.
	 * @throws KeyManagementException Si hay problemas gestionando las claves SSL.
	 * @throws NoSuchAlgorithmException Si no se soporta un algoritmo necesario. */
	public static void disableSslChecks() throws KeyManagementException, NoSuchAlgorithmException {
		final SSLContext sc = SSLContext.getInstance("SSL"); //$NON-NLS-1$
		sc.init(null, DUMMY_TRUST_MANAGER, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
			@Override
			public boolean verify(final String hostname, final SSLSession session) {
				return true;
			}
		});
	}
}
