package org.mozilla.magnet.net.scanner;

import android.os.Looper;

/**
 * Created by arcturus on 10/05/2016.
 */
public abstract class PWScanner {
    public final void start() throws Exception {
        ensureOffMainThread();
        this.doStart();
    }
    protected abstract void doStart();
    public abstract void stop();
    public abstract String scannerType();

    protected void ensureOffMainThread() throws Exception {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            throw new Exception("Never run the scanner in the UI thread");
        }
    }
}
