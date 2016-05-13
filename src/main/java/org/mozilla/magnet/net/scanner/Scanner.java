package org.mozilla.magnet.net.scanner;

import android.content.Context;

import org.json.JSONObject;
import org.mozilla.magnet.net.scanner.btle.BTLEScanner;
import org.mozilla.magnet.net.scanner.mdns.MDNSScanner;

import java.util.HashMap;
import java.util.Map;

/**
 * This class offers the capabilities of discovering urls that are around you.
 * That is, is a library for the Physical Web.
 * It also allows to configure which kind of discovery mechanism you want to
 * use.
 * When starting the scan you will need to pass a callback object implementing
 * ScannerCallback to receive the results discovered.
 * For example, for discovering web pages via Bluetooth Low Energy and mDNS
 * you can use:
 * Scanner scanner = new Scanner(getApplicationContext());
 * scanner.useBTLE().usemDNS();
 * scanner.start(...)
 *
 * The notified results are JSONObjects, which will have a mandatory field 'url'
 * with the recently discovered url, and a optional 'metadata' field, with extra
 * information provided by the different discovery mechanism.
 *
 * @author Francisco Jordano
 */
public class Scanner {
    /**
     * List of different scanning strategies.
     */
    private final Map<String, PWScanner> mScanners = new HashMap<String, PWScanner>();
    private Context mContext = null;

    /**
     * Constructor with Context.
     * @param ctx Context needed to instantiate some of the scanners
     */
    public Scanner(Context ctx) {
        mContext = ctx;
    }

    /**
     * Configures the scanner to use the Bluetooth Low Energy scan.
     * @return Scanner self object to allow chaining.
     */
    public Scanner useBTLE() {
        if (!mScanners.containsKey(BTLEScanner.class.getName())) {
            mScanners.put(BTLEScanner.class.getName(), new BTLEScanner(mContext));
        }
        return this;
    }

    /**
     * Configures the scanner to use mDNS scan.
     * @return Scanner self object to allow chaining.
     */
    public Scanner usemDNS() {
        if (!mScanners.containsKey(MDNSScanner.class.getName())) {
            mScanners.put(MDNSScanner.class.getName(), new MDNSScanner(mContext));
        }
        return this;
    }

    /**
     * Once the object has been configure with the different scannig strategies, you need
     * to call `start` to properly trigger the scanning.
     * @param cb Callback object that will be invoked everytime any scanner finds a web around you.
     */
    public void start(ScannerCallback cb) {
        for (PWScanner scanner: mScanners.values()) {
            scanner.start(cb);
        }
    }

    /**
     * Stops the scanning strategies.
     */
    public void stop() {
        for (PWScanner scanner: mScanners.values()) {
            scanner.stop();
        }
    }


}
