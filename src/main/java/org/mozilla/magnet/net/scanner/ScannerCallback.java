package org.mozilla.magnet.net.scanner;

import org.json.JSONObject;

/**
 * Created by arcturus on 10/05/2016.
 */
public interface ScannerCallback {
    public void onItemFound(JSONObject obj);
}
