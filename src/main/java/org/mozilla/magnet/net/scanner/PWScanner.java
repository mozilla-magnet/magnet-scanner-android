package org.mozilla.magnet.net.scanner;

import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by arcturus on 10/05/2016.
 */
public abstract class PWScanner {
    private ScannerCallback mCallback;
    private final static boolean ENSURE_OFF_UI_THREAD = false;
    public final void start(ScannerCallback cb) throws Exception {
        if (ENSURE_OFF_UI_THREAD) {
            ensureOffUIThread();
        }
        mCallback = cb;
        this.doStart();
    }
    protected abstract void doStart();
    public abstract void stop();
    public abstract String scannerType();

    protected void notify(JSONObject obj) {
        mCallback.onItemFound(obj);
    }

    protected void ensureOffUIThread() throws Exception {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            throw new Exception("Never run the scanner in the UI thread");
        }
    }
}
