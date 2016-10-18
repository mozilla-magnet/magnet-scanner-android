package org.mozilla.magnet.scanner;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ServiceBackgroundScanner extends Service implements MagnetScannerListener {
    private final static String TAG = "BackgroundScanner";
    private static final long SCAN_DURATION_MS = TimeUnit.SECONDS.toMillis(10);
    ArrayList<MagnetScannerItem> mItems;
    private MagnetScanner mMagnetScanner;
    private Handler mHandler;
    private boolean mScanning;

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

        mMagnetScanner = new MagnetScanner(this)
                .useBLE(null)
                .useMDNS(null)
                .useGeolocation(null)
                .addListener(this)
                .start();

        addStopListener();

        // call the callback after time period elapsed
        mHandler = new Handler();
        mHandler.postDelayed(onScanComplete, SCAN_DURATION_MS);

        mScanning = true;
    }

    private void stop() {
        if (!mScanning) { return; }
        Log.d(TAG, "stop");

        mMagnetScanner
                .removeListener(this)
                .stop();

        if (mHandler != null) {
            mHandler.removeCallbacks(onScanComplete);
        }

        removeStopListener();
        mItems.clear();
        mScanning = false;
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

    /**
     * Called once, when the service is terminated.
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, "on destroy");
        stop();
    }

    public void addStopListener() {
        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(mReceiver, new IntentFilter(MagnetScanner.ACTION_STOP_SCAN));
    }

    public void removeStopListener() {
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

    public void broadcastFoundItems(ArrayList<MagnetScannerItem> items) {
        Log.d(TAG, "broadcast items");
        Intent intent = new Intent("org.mozilla.magnet.scanner.ITEMS_FOUND");
        intent.putExtra("items", serialize(items));
        sendBroadcast(intent);
    }

    public ArrayList<HashMap> serialize(ArrayList<MagnetScannerItem> items) {
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
