package org.mozilla.magnet.net.scanner;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Parent class for all scanner strategies. Defines some common methos for notification and extra
 * metadata appending.
 * @author Francisco Jordano
 */
public abstract class BaseScanner {
    private MagnetScannerCallback mCallback;

    /**
     * Start the scanner mechanism.
     * @param cb Callback object to be invoked when something has been discovered.
     */
    public final void start(MagnetScannerCallback cb) {
        mCallback = cb;
        this.doStart();
    }

    /**
     * Performs the real scanning process, once the scanner object has been configured.
     */
    protected abstract void doStart();

    /**
     * Stops the scanning
     */
    public abstract void stop();

    /**
     * Returns a string that defines the name of the scanner strategy implemented
     * @return String name for the strategy implemented.
     */
    public abstract String scannerType();

    /**
     * Method that is called when the scanner discover an url. It also appends more metadata
     * information, like the type of scanner.
     * @param obj JSONObject containing the information about the url discovered.
     */
    protected void notify(JSONObject obj) {
        if (obj.has("metadata")) {
            try {
                obj.getJSONObject("metadata").put("scanner", scannerType());
            } catch (JSONException e) {
            }
        }
        mCallback.onItemFound(obj);
    }
}
