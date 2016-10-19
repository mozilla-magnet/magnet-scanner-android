package org.mozilla.magnet.scanner;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.mozilla.magnet.scanner.ble.ScannerBLE;
import org.mozilla.magnet.scanner.geolocation.ScannerGeolocation;
import org.mozilla.magnet.scanner.mdns.ScannerMDNS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MagnetScanner implements MagnetScannerListener {
    private static final String TAG = "MagnetScanner";
    private ArrayList<MagnetScannerListener> mListeners = new ArrayList<>();
    private BackgroundScannerClient mBackgroundScannerClient;
    private GoogleApiClient mGoogleApiClient;

    /**
     * List of installed scanners.
     */
    private final Map<String, BaseScanner> mScanners = new HashMap<>();
    private Context mContext = null;

    /**
     * Constructor with Context.
     * @param context Context needed to instantiate some of the scanners
     */
    public MagnetScanner(Context context) {
        mContext = context;
        mBackgroundScannerClient = new BackgroundScannerClient(mContext);
    }

    /**
     * Configures the scanner to use the Bluetooth Low Energy scan.
     * @param btleScanner ScannerBLE scanner object, used for testing
     * @return MagnetScanner self object to allow chaining.
     */
    public MagnetScanner useBLE(ScannerBLE btleScanner) {
        if (!mScanners.containsKey(ScannerBLE.class.getName())) {
            if (btleScanner == null) {
                btleScanner = new ScannerBLE(mContext, this);
            }
            mScanners.put(ScannerBLE.class.getName(), btleScanner);
        }
        return this;
    }

    /**
     * Configures the scanner to use mDNS scan.
     * @param mdnsScanner MagnetScanner already build, used for testing.
     * @return MagnetScanner self object to allow chaining.
     */
    public MagnetScanner useMDNS(ScannerMDNS mdnsScanner) {
        if (!mScanners.containsKey(ScannerMDNS.class.getName())) {
            if (mdnsScanner == null) {
                mdnsScanner = new ScannerMDNS(mContext, this);
            }
            mScanners.put(ScannerMDNS.class.getName(), mdnsScanner);
        }
        return this;
    }

    /**
     * Configures the scanner to use geolocation scan.
     * @return MagnetScanner self object to allow chaining.
     */
    public MagnetScanner useGeolocation(ScannerGeolocation scanner) {
        if (!mScanners.containsKey(ScannerGeolocation.class.getName())) {
            if (scanner == null) {
                scanner = new ScannerGeolocation(mContext, this);
            }
            mScanners.put(ScannerGeolocation.class.getName(), scanner);
        }
        return this;
    }

    /**
     * Once the object has been configure with the different scannig strategies, you need
     * to call `start` to properly trigger the scanning.
     */
    public MagnetScanner start() {
        Log.d(TAG, "start");

        for (BaseScanner scanner: mScanners.values()) {
            scanner.start();
        }

        return this;
    }

    /**
     * Stops the scanning strategies.
     */
    public MagnetScanner stop() {
        Log.d(TAG, "stop");

        for (BaseScanner scanner: mScanners.values()) {
            scanner.stop();
        }

        return this;
    }

    public MagnetScanner addListener(MagnetScannerListener listener) {
        if (!mListeners.contains(listener)) { mListeners.add(listener); }
        return this;
    }

    public MagnetScanner removeListener(MagnetScannerListener listener) {
        mListeners.remove(listener);
        return this;
    }

    @Override
    public void onItemFound(MagnetScannerItem item) {
        for (MagnetScannerListener listener: mListeners) {
          listener.onItemFound(item);
        }
    }

    @Override
    public void onItemLost(MagnetScannerItem item) {
        for (MagnetScannerListener listener: mListeners) {
            listener.onItemLost(item);
        }
    }

    public void startBackgroundScanning() {
        Log.d(TAG, "start background scanning");
        mBackgroundScannerClient.start();
    }

    public void stopBackgroundScanning() {
        Log.d(TAG, "stop background scanning");
        mBackgroundScannerClient.stop();
    }
}
