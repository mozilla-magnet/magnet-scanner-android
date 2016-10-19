package org.mozilla.magnet.scanner;

import android.content.Context;
import android.util.Log;

import org.mozilla.magnet.scanner.ble.ScannerBle;
import org.mozilla.magnet.scanner.geolocation.ScannerGeolocation;
import org.mozilla.magnet.scanner.mdns.ScannerMdns;

import java.util.HashMap;
import java.util.Map;

public class MagnetScanner {
    private static final String TAG = "MagnetScanner";
    private BackgroundScannerClient mBackgroundScannerClient;

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
     * Install the BLE scanner.
     * @return MagnetScanner
     */
    public MagnetScanner useBle() {
        return useBle(new ScannerBle(mContext));
    }

    public MagnetScanner useBle(ScannerBle scanner) {
        if (!mScanners.containsKey(ScannerBle.class.getName())) {
            mScanners.put(ScannerBle.class.getName(), scanner);
        }

        return this;
    }

    /**
     * Install the mDNS scanner.
     * @return MagnetScanner
     */
    public MagnetScanner useMdns() {
        return useMdns(new ScannerMdns(mContext));
    }

    public MagnetScanner useMdns(ScannerMdns scanner) {
        if (!mScanners.containsKey(ScannerMdns.class.getName())) {
            mScanners.put(ScannerMdns.class.getName(), scanner);
        }

        return this;
    }

    /**
     * Install the geolocation scanner.
     * @return MagnetScanner
     */
    public MagnetScanner useGeolocation() {
        if (!mScanners.containsKey(ScannerGeolocation.class.getName())) {
            mScanners.put(ScannerGeolocation.class.getName(), new ScannerGeolocation(mContext));
        }

        return this;
    }

    /**
     * Install the GeolocationScanner with optional listeners.
     * @param listeners
     * @return
     */
    public MagnetScanner useGeolocation(ScannerGeolocation.Listeners listeners) {
        useGeolocation();
        String name = ScannerGeolocation.class.getName();
        ScannerGeolocation scannerGeolocation = (ScannerGeolocation) mScanners.get(name);
        scannerGeolocation.addListeners(listeners);
        return this;
    }

    /**
     * Start all scanners scanning.
     * @param listener
     * @return
     */
    public MagnetScanner start(MagnetScannerListener listener) {
        Log.d(TAG, "start");

        for (BaseScanner scanner: mScanners.values()) {
            scanner.start(listener);
        }

        return this;
    }

    /**
     * Stop all scanners scanning.
     * @return
     */
    public MagnetScanner stop() {
        Log.d(TAG, "stop");

        for (BaseScanner scanner: mScanners.values()) {
            scanner.stop();
        }

        return this;
    }

    /**
     * Starts periodic background scans.
     * Should be called when app goes into the background.
     */
    public void startBackgroundScanning() {
        Log.d(TAG, "start background scanning");
        mBackgroundScannerClient.start();
    }

    /**
     * Stops periodic background scans.
     * Should be called when app comes back into foreground.
     */
    public void stopBackgroundScanning() {
        Log.d(TAG, "stop background scanning");
        mBackgroundScannerClient.stop();
    }
}
