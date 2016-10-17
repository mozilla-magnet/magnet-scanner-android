package org.mozilla.magnet.scanner;

import org.json.JSONObject;

/**
 * Interface to define the mechanism to get notified of a new web page
 * discovered by the scanner.
 *
 * @author Francisco Jordano
 */
public interface MagnetScannerCallback {
    public void onItemFound(MagnetScannerItem item);
}
