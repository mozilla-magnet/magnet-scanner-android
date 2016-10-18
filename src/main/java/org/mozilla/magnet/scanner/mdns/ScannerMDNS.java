package org.mozilla.magnet.scanner.mdns;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;
import android.webkit.URLUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.magnet.scanner.BaseScanner;
import org.mozilla.magnet.scanner.MagnetScannerItem;
import org.mozilla.magnet.scanner.MagnetScannerListener;

/**
 * MagnetScanner that discovers web pages via mDNS protocol.
 *
 * @author Francisco Jordano
 */
public class ScannerMDNS extends BaseScanner implements NsdManager.DiscoveryListener {
    private final static String TAG = ScannerMDNS.class.getName();
    private final static String MDNS_SERVICE_TYPE = "_http._tcp.";
    private final static String SCAN_TYPE = "mdns";
    private Context mContext = null;
    private NsdManager mNsdManager;

    /**
     * Constructor with Context, needed to start the mDNS service.
     * @param ctx Context object.
     */
    public ScannerMDNS(Context ctx, MagnetScannerListener listener) {
        super(listener);
        mContext = ctx;
        mNsdManager = (NsdManager) mContext.getSystemService(Context.NSD_SERVICE);
    }

    /**
     * Kind of scanner type.
     * @return String name of the scanner.
     */
    @Override
    public String scannerType() {
        return SCAN_TYPE;
    }

    //  Called as soon as service discovery begins.
    @Override
    public void onDiscoveryStarted(String regType) {
        Log.d(TAG, "Service discovery started");
    }

    @Override
    public void onServiceFound(NsdServiceInfo service) {
        Log.d(TAG, "service found: " + service);
        String url = getUrl(service);

        if (url == null) { return; }

        MagnetScannerItem item = new MagnetScannerItem();
        item.setUrl(url);
        item.setType(scannerType());
        addItem(item);
    }

    @Override
    public void onServiceLost(NsdServiceInfo service) {
        // When the network service is no longer available.
        // Internal bookkeeping code goes here.
        Log.e(TAG, "service lost" + service);

        String url = getUrl(service);
        if (url == null) { return; }

        removeItem(url);
    }

    @Override
    public void onDiscoveryStopped(String serviceType) {
        Log.i(TAG, "Discovery stopped: " + serviceType);
    }

    @Override
    public void onStartDiscoveryFailed(String serviceType, int errorCode) {
        Log.e(TAG, "Discovery failed: Error code:" + errorCode);
        mNsdManager.stopServiceDiscovery(this);
    }

    @Override
    public void onStopDiscoveryFailed(String serviceType, int errorCode) {
        Log.e(TAG, "Discovery failed: Error code:" + errorCode);
        mNsdManager.stopServiceDiscovery(this);
    }

    private String getUrl(NsdServiceInfo service) {
        String name = service.getServiceName();

        // not all mdns advertisements are urls
        if (!URLUtil.isNetworkUrl(name)) { return null; }

        return name;
    }

    /**
     * Starts the mDNS discovery.
     */
    @Override
    protected void start() {
        mNsdManager.discoverServices(MDNS_SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, this);
    }

    /**
     * Stops the mDNS discovery.
     */
    @Override
    public void stop() {
        super.stop();
        mNsdManager.stopServiceDiscovery(this);
    }
}
