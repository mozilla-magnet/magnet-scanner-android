package org.mozilla.magnet.net.scanner.btle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.magnet.net.scanner.PWScanner;
import org.mozilla.magnet.net.scanner.ScannerCallback;

/**
 * Created by arcturus on 10/05/2016.
 */
public class BTLEScanner extends PWScanner {
    private final String TAG = BTLEScanner.class.getName();
    private BluetoothAdapter mBTAdapter;
    private Context mContext = null;
    private ScannerCallback mCallback = null;
    private BluetoothAdapter.LeScanCallback mScanCallback = null;

    public BTLEScanner(Context ctx, ScannerCallback cb) {
        mContext = ctx;
        mCallback = cb;
        final BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBTAdapter = bluetoothManager.getAdapter();
        mScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                Log.d(TAG, "rssi: " + rssi);
                JSONObject json = new JSONObject();
                JSONArray values = toJSON(scanRecord);
                try {
                    json.put("values", values);
                    json.put("rssi", rssi);
                    json.put("bytes", scanRecord.toString());
                } catch (JSONException e) {
                    Log.d(TAG, "Error transforming json: " + e.getMessage());
                }
                mCallback.onItemFound(json);
            }
        };
    }

    @Override
    protected void doStart() {
        Log.d(TAG, "Starting scan");
        mBTAdapter.startLeScan(mScanCallback);
    }

    @Override
    public void stop() {
        Log.d(TAG, "Stopping scan");
        mBTAdapter.stopLeScan(mScanCallback);
    }

    @Override
    public String scannerType() {
        return TAG;
    }

    private JSONArray toJSON(byte[] bytes) {
        JSONArray array = new JSONArray();
        for (byte item:bytes) array.put(item);
        return array;
    }
}
