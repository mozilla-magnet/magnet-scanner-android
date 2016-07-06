package org.mozilla.magnet.net.scanner.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.mozilla.magnet.net.scanner.BaseScanner;
import org.mozilla.magnet.net.scanner.MagnetScannerItem;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MagnetScanner to discover web pages via Bluetooth Low Energy beacons using
 * the Eddystone (https://github.com/google/eddystone) protocol.
 *
 * @author Francisco Jordano
 */
public class ScannerBLE extends BaseScanner {
    private final String TAG = ScannerBLE.class.getName();
    private BluetoothAdapter mBTAdapter;
    private Context mContext = null;
    private BluetoothAdapter.LeScanCallback mScanCallback = null;
    private final static String TYPE = "btle";
    private MaxSizeHashMap<String, Long> mNotifyHistory = new MaxSizeHashMap<String, Long>(20);
    private final int MIN_NOTIFY_INVERVAL_MS = 5000;

    /**
     * Constructor with context needed to launch the BTLE scanner.
     * @param ctx Context
     */
    public ScannerBLE(Context ctx) {
        super();
        mContext = ctx;
        final BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBTAdapter = bluetoothManager.getAdapter();

        mScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                MagnetScannerItem item = EddyStoneParser.parse(scanRecord, rssi);

                if (item == null) {
                    return;
                }

                if (!shouldNotify(item)) {
                    return;
                }

                item.setDevice(device.toString());
                ScannerBLE.this.notify(item);
                logNotify(item);
            }
        };
    }

    private boolean shouldNotify(MagnetScannerItem item) {
        String url = item.getUrl();
        Long now = System.currentTimeMillis();
        Long last = mNotifyHistory.get(url);
        return last == null || now - last > MIN_NOTIFY_INVERVAL_MS;
    }

    private void logNotify(MagnetScannerItem item) {
        String url = item.getUrl();
        Long now = System.currentTimeMillis();
        mNotifyHistory.put(url, now);
    }

    /**
     * Starts the scanning.
     */
    @Override
    protected void doStart() {
        Log.d(TAG, "Starting scan");
        mBTAdapter.startLeScan(mScanCallback);
    }

    /**
     * Stops the scanning.
     */
    @Override
    public void stop() {
        Log.d(TAG, "Stopping scan");
        mBTAdapter.stopLeScan(mScanCallback);
    }

    /**
     * Returns the kind of scanner, btle in this case.
     * @return String btle.
     */
    @Override
    public String scannerType() {
        return "btle";
    }

    /**
     * Transforms an array of bytes into a JSONArray to be appended as part of the metadata.
     * @param bytes Payload discovered in the beacon.
     * @return JSONArray to be appended to the metadata section of the discovered object.
     */
    private JSONArray toJSON(byte[] bytes) {
        JSONArray array = new JSONArray();
        for (byte item:bytes) array.put(item);
        return array;
    }

    private static class MaxSizeHashMap<K, V> extends LinkedHashMap<K, V> {
        private final int maxSize;

        public MaxSizeHashMap(int maxSize) {
            this.maxSize = maxSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > maxSize;
        }
    }
}
