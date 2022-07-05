package es.gob.afirma.android.crypto;

public class LoadingKeyStoreResult {

    private MobileKeyStoreManager msm;
    private boolean error;
    private String errorMessage;
    private Throwable cause;
    private boolean cancelled;

    public LoadingKeyStoreResult(MobileKeyStoreManager msm) {
        this.msm = msm;
        this.error = false;
        this.cause = null;
        this.errorMessage = null;
        this.cancelled = false;
    }

    public LoadingKeyStoreResult(String errorMessage, Throwable cause) {
        this.msm = null;
        this.error = true;
        this.cause = cause;
        this.errorMessage = errorMessage;
        this.cancelled = false;
    }

    public MobileKeyStoreManager getMsm() {
        return this.msm;
    }

    public boolean isError() {
        return this.error;
    }

    public Throwable getCause() {
        return this.cause;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
