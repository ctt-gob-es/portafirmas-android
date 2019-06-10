package es.gob.afirma.android.network;

import java.io.InputStream;

public class ConnectionResponse {

    private InputStream dataIs;

    private String cookieId;

    public InputStream getDataIs() {
        return this.dataIs;
    }

    public void setDataIs(InputStream dataIs) {
        this.dataIs = dataIs;
    }

    public String getCookieId() {
        return this.cookieId;
    }

    public void setCookieId(String cookieId) {
        this.cookieId = cookieId;
    }
}
