package org.mozilla.magnet.net.scanner;

import org.json.JSONObject;

/**
 * Interface to define the mechanism to get notified of a new web page
 * discovered by the scanner.
 *
 * @author Francisco Jordano
 */
public interface ScannerCallback {
    /**
     *
     * @param obj JSONObject with the information, will have a mandatory 'url' field and can have
     *            an optional 'metadata' field.
     */
    public void onItemFound(JSONObject obj);
}
