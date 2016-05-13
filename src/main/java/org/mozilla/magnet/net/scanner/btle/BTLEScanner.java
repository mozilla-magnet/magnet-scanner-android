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
 * Scanner to discover web pages via Bluetooth Low Energy beacons using
 * the Eddystone (https://github.com/google/eddystone) protocol.
 *
 * @author Francisco Jordano
 */
public class BTLEScanner extends PWScanner {
    private final String TAG = BTLEScanner.class.getName();
    private BluetoothAdapter mBTAdapter;
    private Context mContext = null;
    private BluetoothAdapter.LeScanCallback mScanCallback = null;
    private final static String TYPE = "btle";

    /**
     * Constructo with context needed to launch the BTLE scanner.
     * @param ctx Context
     */
    public BTLEScanner(Context ctx) {
        super();
        mContext = ctx;
        final BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBTAdapter = bluetoothManager.getAdapter();
        mScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                JSONObject parsed = EddyStoneParser.parse(scanRecord);
                if (parsed == null) {
                    return;
                }
                JSONObject json = new JSONObject();
                JSONObject metadata = new JSONObject();
                JSONArray values = toJSON(scanRecord);
                try {
                    json.put("url", parsed.getString("url"));
                    metadata.put("values", values);
                    metadata.put("rssi", rssi);
                    metadata.put("device", device.toString());
                    if (parsed.has("txPower")) {
                        metadata.put("distance", calculateDistance(parsed.getInt("txPower"), rssi));
                        metadata.put("txPower", parsed.getInt("txPower"));
                    }
                    metadata.put("flags", parsed.getInt("flags"));
                    json.put("metadata", metadata);
                } catch(JSONException e) {
                    return;
                }
                BTLEScanner.this.notify(json);

            }
        };
    }

    /**
     * Calculates the distance to the beacon based on transmission power configured in the beacon
     * and rssi detected with our sensor.
     * https://github.com/google/physical-web/blob/master/web-service/helpers.py#L124
     * @param txPower int Transmission power, configured in the beacon.
     * @param rssi int Received signal strength.
     * @return
     */
    private double calculateDistance(int txPower, int rssi) {
        return Math.pow(10, ((txPower - rssi) - 41) / 20);
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
}
