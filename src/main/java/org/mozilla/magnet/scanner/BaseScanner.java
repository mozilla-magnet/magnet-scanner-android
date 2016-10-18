package org.mozilla.magnet.scanner;

import android.support.annotation.CallSuper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Parent class for all scanner strategies. Defines some common methos for notification and extra
 * metadata appending.
 * @author Francisco Jordano
 */
public abstract class BaseScanner {
    private final static String TAG = "BaseScanner";
    private MagnetScannerListener mListener;
    private final HashMap<String,MagnetScannerItem> mItems = new HashMap<String,MagnetScannerItem>();

    public BaseScanner(MagnetScannerListener listener) {
        mListener = listener;
    }

    /**
     * Returns a string that defines the name of the scanner strategy implemented
     * @return String name for the strategy implemented.
     */
    public abstract String scannerType();

    /**
     * Performs the real scanning process, once the scanner object has been configured.
     */
    protected abstract void start();

    /**
     * Stops the scanning
     */
    @CallSuper
    public void stop() {
        mItems.clear();
    }

    public HashMap<String, MagnetScannerItem> getItems() {
        return mItems;
    }

    public MagnetScannerItem getItem(String id) {
        return mItems.get(id);
    }

    public void addItem(MagnetScannerItem item) {
        Log.d(TAG, "add item: " + item.getUrl());
        mItems.put(item.getUrl(), item);
        mListener.onItemFound(item);
    }

    public void removeItem(String id) {
        Log.d(TAG, "remove item: " + id);
        MagnetScannerItem item = getItem(id);
        mItems.remove(id);
        mListener.onItemLost(item);
    }

    /**
     * Method that is called when the scanner discover an url. It also appends more metadata
     * information, like the type of scanner.
     */
    protected void onItemFound(MagnetScannerItem item) {
        mListener.onItemFound(item);
    }
    protected void onItemLost(MagnetScannerItem item) {
        mListener.onItemLost(item);
    }

}
