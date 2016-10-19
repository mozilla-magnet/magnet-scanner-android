package org.mozilla.magnet.scanner;

import android.content.Context;
import android.util.Log;

import org.mozilla.magnet.scanner.ble.ScannerBLE;
import org.mozilla.magnet.scanner.geolocation.ScannerGeolocation;
import org.mozilla.magnet.scanner.mdns.ScannerMDNS;

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
     * Configures the scanner to use the Bluetooth Low Energy scan.
     * @param btleScanner ScannerBLE scanner object, used for testing
     * @return MagnetScanner self object to allow chaining.
     */
    public MagnetScanner useBLE(ScannerBLE btleScanner) {
        if (!mScanners.containsKey(ScannerBLE.class.getName())) {
            if (btleScanner == null) {
                btleScanner = new ScannerBLE(mContext);
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
                mdnsScanner = new ScannerMDNS(mContext);
            }
            mScanners.put(ScannerMDNS.class.getName(), mdnsScanner);
        }
        return this;
    }

    public MagnetScanner useGeolocation(ScannerGeolocation.Listeners listeners) {
        useGeolocation();
        String name = ScannerGeolocation.class.getName();
        ScannerGeolocation scannerGeolocation = (ScannerGeolocation) mScanners.get(name);
        scannerGeolocation.addListeners(listeners);
        return this;
    }

    /**
     * Configures the scanner to use geolocation scan.
     * @return MagnetScanner self object to allow chaining.
     */
    public MagnetScanner useGeolocation() {
        if (!mScanners.containsKey(ScannerGeolocation.class.getName())) {
            mScanners.put(ScannerGeolocation.class.getName(), new ScannerGeolocation(mContext));
        }

        return this;
    }

    /**
     * Once the object has been configure with the different scannig strategies, you need
     * to call `start` to properly trigger the scanning.
     */
    public MagnetScanner start(MagnetScannerListener listener) {
        Log.d(TAG, "start");

        for (BaseScanner scanner: mScanners.values()) {
            scanner.start(listener);
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

    public void startBackgroundScanning() {
        Log.d(TAG, "start background scanning");
        mBackgroundScannerClient.start();
    }

    public void stopBackgroundScanning() {
        Log.d(TAG, "stop background scanning");
        mBackgroundScannerClient.stop();
    }
}
