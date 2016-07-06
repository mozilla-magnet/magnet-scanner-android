package org.mozilla.magnet.net.scanner;

import android.content.Context;

import org.mozilla.magnet.net.scanner.ble.ScannerBLE;
import org.mozilla.magnet.net.scanner.mdns.ScannerMDNS;

import java.util.HashMap;
import java.util.Map;

/**
 * This class offers the capabilities of discovering urls that are around you.
 * That is, is a library for the Physical Web.
 * It also allows to configure which kind of discovery mechanism you want to
 * use.
 * When starting the scan you will need to pass a callback object implementing
 * MagnetScannerCallback to receive the results discovered.
 * For example, for discovering web pages via Bluetooth Low Energy and mDNS
 * you can use:
 * <pre>
 * {@code
 * MagnetScanner scanner = new MagnetScanner(getApplicationContext());
 * scanner.useBLE().useMDNS();
 * scanner.start(...);
 *}
 * </pre>
 * The notified results are JSONObjects, which will have a mandatory field 'url'
 * with the recently discovered url, and a optional 'metadata' field, with extra
 * information provided by the different discovery mechanism.
 *
 * @author Francisco Jordano
 */
public class MagnetScanner {
    /**
     * List of different scanning strategies.
     */
    private final Map<String, BaseScanner> mScanners = new HashMap<String, BaseScanner>();
    private Context mContext = null;

    /**
     * Constructor with Context.
     * @param ctx Context needed to instantiate some of the scanners
     */
    public MagnetScanner(Context ctx) {
        mContext = ctx;
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

    /**
     * Once the object has been configure with the different scannig strategies, you need
     * to call `start` to properly trigger the scanning.
     * @param cb Callback object that will be invoked everytime any scanner finds a web around you.
     */
    public void start(MagnetScannerCallback cb) {
        for (BaseScanner scanner: mScanners.values()) {
            scanner.start(cb);
        }
    }

    /**
     * Stops the scanning strategies.
     */
    public void stop() {
        for (BaseScanner scanner: mScanners.values()) {
            scanner.stop();
        }
    }
}
