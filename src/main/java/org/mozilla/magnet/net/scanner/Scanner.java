package org.mozilla.magnet.net.scanner;

import android.content.Context;

import org.json.JSONObject;
import org.mozilla.magnet.net.scanner.btle.BTLEScanner;
import org.mozilla.magnet.net.scanner.mdns.MDNSScanner;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by arcturus on 10/05/2016.
 */
public class Scanner {
    private final Map<String, PWScanner> mScanners = new HashMap<String, PWScanner>();
    private Context mContext = null;

    public Scanner(Context ctx) {
        mContext = ctx;
    }

    public Scanner useBTLE() {
        if (!mScanners.containsKey(BTLEScanner.class.getName())) {
            mScanners.put(BTLEScanner.class.getName(), new BTLEScanner(mContext));
        }
        return this;
    }

    public Scanner usemDNS() {
        if (!mScanners.containsKey(MDNSScanner.class.getName())) {
            mScanners.put(MDNSScanner.class.getName(), new MDNSScanner(mContext));
        }
        return this;
    }

    public void start(ScannerCallback cb) throws Exception {
        for (PWScanner scanner: mScanners.values()) {
            scanner.start(cb);
        }
    }

    public void stop() {
        for (PWScanner scanner: mScanners.values()) {
            scanner.stop();
        }
    }


}
