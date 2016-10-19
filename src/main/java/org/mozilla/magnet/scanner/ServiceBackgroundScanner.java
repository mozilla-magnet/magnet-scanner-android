package org.mozilla.magnet.scanner;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.mozilla.magnet.scanner.geolocation.ScannerGeolocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ServiceBackgroundScanner extends Service implements MagnetScannerListener, ScannerGeolocation.Listeners {
    private final static String TAG = "BackgroundScanner";
    private static final long MIN_SCAN_DURATION_MS = TimeUnit.SECONDS.toMillis(10);
    private static final long MAX_SCAN_DURATION_MS = TimeUnit.SECONDS.toMillis(30);
    ArrayList<MagnetScannerItem> mItems;
    private MagnetScanner mMagnetScanner;
    private Handler mHandler;
    private boolean mScanning;
    private long mTimeStarted;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "on start command");
        start();
        return super.onStartCommand(intent, flags, startId);
    }

    private void start() {
        if (mScanning) { return; }
        Log.d(TAG, "start");

        mItems = new ArrayList<MagnetScannerItem>();
        mTimeStarted = System.currentTimeMillis();

        mMagnetScanner = new MagnetScanner(this)
                .useBLE(null)
                .useMDNS(null)
                .useGeolocation(this)
                .start(this);

        addStopListener();

        // call the callback after time period elapsed
        setTimeout(MAX_SCAN_DURATION_MS);
        mScanning = true;
    }

    private void stop() {
        if (!mScanning) { return; }
        Log.d(TAG, "stop");
        mMagnetScanner.stop();
        clearTimeout();
        removeStopListener();
        mItems.clear();
        mScanning = false;
        stopSelf();
    }

    private void setTimeout(long ms) {
        clearTimeout();
        mHandler = new Handler();
        mHandler.postDelayed(onScanComplete, ms);
    }

    private void clearTimeout() {
        if (mHandler == null) { return; }
        mHandler.removeCallbacks(onScanComplete);
        mHandler = null;
    }

    private Runnable onScanComplete = new Runnable() {
        @Override
        public void run() {
            mHandler = null;
            broadcastFoundItems(mItems);
            stop();
        }
    };

    @Override
    public void onItemFound(MagnetScannerItem item) {
        Log.d(TAG, "scan item found: " + item.getUrl());
        mItems.add(item);
    }

    @Override
    public void onItemLost(MagnetScannerItem item) {
        Log.d(TAG, "scan item lost: " + item.getUrl());
        mItems.remove(item);
    }

    @Override
    public void onGeolocationScanComplete() {
        Log.d(TAG, "on geolocation scan complete");
        long elapsed = System.currentTimeMillis() - mTimeStarted;

        // if the geolocation scan finishes after the
        // minimum scan duration we can stop here
        if (elapsed >= MIN_SCAN_DURATION_MS) {
            Log.d(TAG, "stopping scan after: " + elapsed);
            stop();
            return;
        }

        // when the geolocation scan finishes before the
        // minimum scan duration, we set a new timer to
        // wait until the minimum time has been reached
        long remaining = MIN_SCAN_DURATION_MS - elapsed;
        setTimeout(remaining);
        Log.d(TAG, "will scan for " + remaining + " more");
    }

    /**
     * Called once, when the service is terminated.
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, "on destroy");
        stop();
    }

    private void addStopListener() {
        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(mReceiver, new IntentFilter(BackgroundScannerClient.ACTION_STOP_SCAN));
    }

    private void removeStopListener() {
        LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(mReceiver);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stop();
        }
    };

    private void broadcastFoundItems(ArrayList<MagnetScannerItem> items) {
        Log.d(TAG, "broadcast items");
        Intent intent = new Intent("org.mozilla.magnet.scanner.ITEMS_FOUND");
        intent.putExtra("items", serialize(items));
        sendBroadcast(intent);
    }

    private ArrayList<HashMap> serialize(ArrayList<MagnetScannerItem> items) {
        ArrayList<HashMap> result = new ArrayList<HashMap>();

        for (MagnetScannerItem item: items) {
            result.add(item.toHashMap());
        }

        return result;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
