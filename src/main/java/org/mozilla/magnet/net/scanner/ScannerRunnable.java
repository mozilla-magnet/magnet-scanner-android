package org.mozilla.magnet.net.scanner;

import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by arcturus on 11/05/2016.
 */
public class ScannerRunnable {
    private Scanner mScanner;
    private final ExecutorService mThreadPoolExecutor = Executors.newSingleThreadExecutor();
    private Future<?> mTask;
    private ScanRunner mRunner;
    private ScannerCallback mCallback;

    public ScannerRunnable(Context ctx, ScannerCallback cb) {
        mCallback = cb;
        mScanner = new Scanner(ctx);
        mScanner.useBTLE().usemDNS();
        mRunner = new ScanRunner();
    }

    public void start() {
        if (!mRunner.isRunning) {
            mTask = mThreadPoolExecutor.submit(mRunner);
        }
    }

    public void stop() {
        mRunner.stop();
        mTask.cancel(true);
    }

    private class ScanRunner implements Runnable {

        private boolean isRunning = false;

        @Override
        public void run() {
            try {
                isRunning = true;
                mScanner.start(mCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void stop() {
            isRunning = false;
            mScanner.stop();
        }

        public boolean isRunning() {
            return isRunning;
        }
    }
}
