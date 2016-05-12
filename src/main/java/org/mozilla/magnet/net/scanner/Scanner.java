package org.mozilla.magnet.net.scanner;

import android.content.Context;

import org.json.JSONObject;
import org.mozilla.magnet.net.scanner.btle.BTLEScanner;

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
            CallbackListener btleListener = new CallbackListener("BTLE");
            mScanners.put(BTLEScanner.class.getName(), new BTLEScanner(mContext, btleListener));
        }
        return this;
    }

    public Scanner usemDNS() {
        System.out.println("mDNS not implemented");
        return this;
    }

    public void start() throws Exception {
        for (PWScanner scanner: mScanners.values()) {
            scanner.start();
        }
    }

    public void stop() {
        for (PWScanner scanner: mScanners.values()) {
            scanner.stop();
        }
    }

    private class CallbackListener implements ScannerCallback {
        private String mType;
        public CallbackListener(String type) {
            mType = type;
        }

        @Override
        public void onItemFound(JSONObject obj) {
            System.out.println(mType + " found something!! --> " + obj.toString());
        }
    }


}
