package es.gob.afirma.android.network;

import android.util.Log;

import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

import es.gob.afirma.android.signfolder.SFConstants;

public class CookieStoreWrapper implements CookieStore {

    private CookieStore store;

    public CookieStoreWrapper() {
        this.store = new CookieManager().getCookieStore();
    }

    @Override
    public void add(URI uri, HttpCookie cookie) {
        Log.w(SFConstants.LOG_TAG," ====== Agregamos al CookieStore la Uri: " + uri + "\nCookie: " + cookie);
        this.store.add(uri, cookie);
    }

    @Override
    public List<HttpCookie> get(URI uri) {
        Log.w(SFConstants.LOG_TAG," ====== Recuperamos del CookieStore la Uri: " + uri);
        return this.store.get(uri);
    }

    @Override
    public List<HttpCookie> getCookies() {
        return this.store.getCookies();
    }

    @Override
    public List<URI> getURIs() {
        return this.store.getURIs();
    }

    @Override
    public boolean remove(URI uri, HttpCookie cookie) {
        return this.store.remove(uri, cookie);
    }

    @Override
    public boolean removeAll() {
        return this.store.removeAll();
    }
}
