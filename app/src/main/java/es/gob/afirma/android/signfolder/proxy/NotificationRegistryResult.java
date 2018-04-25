package es.gob.afirma.android.signfolder.proxy;

public class NotificationRegistryResult {

    private boolean ok;

    private String error;

    public NotificationRegistryResult(boolean ok) {
        this(ok, null);
    }

    public NotificationRegistryResult(boolean ok, String error) {
        this.ok = ok;
        this.error = error;
    }

    public boolean isOk() {
        return this.ok;
    }

    public String getError() {
        return this.error;
    }
}
