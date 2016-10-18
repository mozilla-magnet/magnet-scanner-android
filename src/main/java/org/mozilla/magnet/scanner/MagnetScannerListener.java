package org.mozilla.magnet.scanner;

/**
 * Created by wilsonpage on 18/10/2016.
 */

public interface MagnetScannerListener {
    public void onItemFound(MagnetScannerItem item);
    public void onItemLost(MagnetScannerItem item);
}

