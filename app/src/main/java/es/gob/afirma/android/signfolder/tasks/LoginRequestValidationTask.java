package es.gob.afirma.android.signfolder.tasks;

import android.os.AsyncTask;

import java.io.IOException;
import java.security.KeyStore;
import java.util.Timer;
import java.util.TimerTask;

import es.gob.afirma.android.crypto.AOPkcs1Signer;
import es.gob.afirma.android.signfolder.SFConstants;
import es.gob.afirma.android.signfolder.listeners.LoginListener;
import es.gob.afirma.android.signfolder.proxy.CommManager;
import es.gob.afirma.android.signfolder.proxy.OldProxyException;
import es.gob.afirma.android.signfolder.proxy.RequestResult;
import es.gob.afirma.android.signfolder.proxy.ValidationLoginResult;
import es.gob.afirma.android.util.Base64;
import es.gob.afirma.android.util.PfLog;

/** Carga los datos remotos necesarios para la configuraci&oacute;n de la aplicaci&oacute;n. */
public final class LoginRequestValidationTask extends AsyncTask<Void, Void, ValidationLoginResult> {

	private final String certB64;
	private final String certAlias;
	private final CommManager commManager;
	private LoginListener loginListener;
	private final KeyStore.PrivateKeyEntry privateKeyEntry;
	private Timer timer = null;

	class TaskKiller extends TimerTask {
		private AsyncTask<?, ?, ?> mTask;
		private LoginListener listener;
		TaskKiller(AsyncTask<?, ?, ?> task, LoginListener listener) {
			this.mTask = task;
			this.listener = listener;
		}

		public void run() {
			this.mTask.cancel(true);
			ValidationLoginResult loginResult = new ValidationLoginResult(false);
			loginResult.setErrorMsg("La operacion tardo demasiado tiempo");
			this.listener.loginResult(loginResult);
		}
	}

	/**
	 * Crea la tarea para la carga de la configuraci&oacute;n de la aplicaci&oacute;n
	 * necesaria para su correcto funcionamiento.
	 * @param certB64 Certificado para la autenticaci&oacute;n de la petici&oacute;n.
	 * @param commManager Manejador de los servicios de comunicaci&oacute;n con el portafirmas.
	 * @param loginListener Manejador del resultado de la operaci&oacute;n.
	 * @param pke Clave privada del almac&eacute;n de claves.
	 */
	public LoginRequestValidationTask(final String certB64, final String certAlias,
							   final CommManager commManager,
							   final LoginListener loginListener,
							   final KeyStore.PrivateKeyEntry pke) {
		this.certB64 = certB64;
		this.certAlias = certAlias;
		this.commManager = commManager;
		this.loginListener = loginListener;
		this.privateKeyEntry = pke;
	}

	@Override
	protected ValidationLoginResult doInBackground(final Void... args) {

		this.timer = new Timer();
        this.timer.schedule(new TaskKiller(this, this.loginListener), 10000);

        // Se realiza la peticion para realizar el login
		ValidationLoginResult result = new ValidationLoginResult(false);
		try {
			RequestResult token = this.commManager.loginRequest();

			// Si no se ha lanzado un OldProxyException es que estamos ante un proxy seguro
			CommManager.getInstance().setNewProxy();
            PfLog.i(SFConstants.LOG_TAG, "Se ha encontrado una version segura del proxy"); //$NON-NLS-1$

			if (!token.isStatusOk()) {
				result.setErrorMsg("Error al solicitar el token de login.");
			}
			// Si el proceso no ha fallado, continuamos
			else {
                // Firma del token
                final AOPkcs1Signer signer = new AOPkcs1Signer();
                final byte[] pkcs1 = signer.sign(Base64.decode(token.getId()), "SHA256withRSA",
                        privateKeyEntry.getPrivateKey(), privateKeyEntry.getCertificateChain(), null); //$NON-NLS-1$

                // Se solicita la validacion del acceso
				result = this.commManager.tokenValidation(pkcs1, this.certB64);
            }
        } catch (final OldProxyException e) {
            // Proxy antiguo sin validacion
            PfLog.w(SFConstants.LOG_TAG, "Login no necesario: Se trabaja con una version antigua del portafirmas"); //$NON-NLS-1$
            CommManager.getInstance().setOldProxy();
            result.setStatusOk(true);
		} catch (final IOException e) {
			PfLog.w(SFConstants.LOG_TAG, "No se pudo conectar con el servidor", e); //$NON-NLS-1$
			result.setErrorMsg("No se pudo conectar con el servidor.");
		} catch (final Exception e) {
			PfLog.w(SFConstants.LOG_TAG, "No se pudo realizar el login", e); //$NON-NLS-1$
            result.setErrorMsg("No se pudo completar la autenticacion del usuario.");
		}
		timer.cancel();

		if (result.isStatusOk()) {
		    result.setCertAlias(this.certAlias);
		    result.setCertificateB64(this.certB64);
        }

		return result;
	}

	@Override
	protected void onPostExecute(final ValidationLoginResult result) {
		this.loginListener.loginResult(result);
	}

	@Override
	protected void onCancelled(ValidationLoginResult result) {
		super.onCancelled(result);
		if (this.timer != null) {
			this.timer.cancel();
		}
	}
}
