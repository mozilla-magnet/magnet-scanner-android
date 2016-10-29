package org.mozilla.magnet.scanner.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import org.mozilla.magnet.scanner.BaseScanner;
import org.mozilla.magnet.scanner.MagnetScannerItem;
import org.mozilla.magnet.scanner.MagnetScannerListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * MagnetScanner to discover web pages via Bluetooth Low Energy beacons using
 * the Eddystone (https://github.com/google/eddystone) protocol.
 *
 * @author Francisco Jordano
 */
public class ScannerBle extends BaseScanner implements BluetoothAdapter.LeScanCallback {
    private final String TAG = ScannerBle.class.getName();
    private final int EXPIRE_CHECK_INTERVAL_MS = 6000;
    private final int ITEM_MAX_AGE_MS = 5000;
    private final static String TYPE = "ble";
    private BluetoothAdapter mBTAdapter;
    private Context mContext = null;
    private Handler mHandler;

    /**
     * Constructor with context needed to launch the BTLE scanner.
     * @param context Context
     */
    public ScannerBle(Context context) {
        mContext = context;
        final BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBTAdapter = bluetoothManager.getAdapter();
    }

    /**
     * Returns the kind of scanner, btle in this case.
     * @return String btle.
     */
    @Override
    public String scannerType() {
        return TYPE;
    }

    /**
     * Starts the scanning.
     */
    @Override
    public void start(MagnetScannerListener listener) {
        if (isStarted()) return;
        super.start(listener);
        Log.d(TAG, "starting scan");
        mHandler = new Handler();
        mBTAdapter.startLeScan(this);
        startExpireCheck();
    }

    /**
     * Stops the scanning.
     */
    @Override
    public void stop() {
        if (isStopped()) return;
        super.stop();
        Log.d(TAG, "stopping scan");
        mBTAdapter.stopLeScan(this);
        stopExpireCheck();
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        MagnetScannerItem item = EddyStoneParser.parse(scanRecord, rssi);

        // not all ble advertisements are eddystone-url
        if (item == null) { return; }

        String id = item.getUrl();
        MagnetScannerItem existingItem = getItem(id);

        // if item already in cache, touch and exit
        if (existingItem != null) {
            existingItem.touch();
            return;
        }

        item.setDevice(device.toString());
        item.setType(scannerType());
        addItem(item);
    }

    private void startExpireCheck() {
        scheduleExpireCheck();
    }

    private void stopExpireCheck() {
        mHandler.removeCallbacks(expireCheck);
    }

    private void scheduleExpireCheck() {
        mHandler.postDelayed(expireCheck, EXPIRE_CHECK_INTERVAL_MS);
    }

    private Runnable expireCheck = new Runnable() {
        public void run() {
            Map items = getItems();
            Iterator it = items.entrySet().iterator();
            long now = System.currentTimeMillis();
            ArrayList<String> toRemove = new ArrayList<>();

            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                String id = (String) pair.getKey();
                MagnetScannerItem item = (MagnetScannerItem) pair.getValue();
                long lastSeen = item.getLastSeen();
                long age = now - lastSeen;

                // remove expired mItems
                if (age > ITEM_MAX_AGE_MS) {
                    toRemove.add(id);
                }
            }

            // remove items in separate loop to avoid
            // `ConcurrentModificationException`
            for (String id: toRemove) {
                removeItem(id);
            }

            // loop
            scheduleExpireCheck();
        }
    };
}
