package org.mozilla.magnet.net.scanner.mdns;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;
import android.webkit.URLUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.magnet.net.scanner.PWScanner;

/**
 * Scanner that discovers web pages via mDNS protocol.
 *
 * @author Francisco Jordano
 */
public class MDNSScanner extends PWScanner {

    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager mNsdManager;
    private final static String TAG = MDNSScanner.class.getName();
    private final static String MDNS_SERVICE_TYPE = "_http._tcp.";
    private Context mContext = null;
    private final static String SCAN_TYPE = "mdns";

    /**
     * Constructor with Context, needed to start the mDNS service.
     * @param ctx Context object.
     */
    public MDNSScanner(Context ctx) {
        mContext = ctx;
        mNsdManager = (NsdManager) mContext.getSystemService(Context.NSD_SERVICE);
        mDiscoveryListener = createListener();
    }

    /**
     * Returns an object that listens to all the discovery lifecycle on mDNS protocol.
     * @return DiscoveryListener object to handle the different states.
     */
    private NsdManager.DiscoveryListener createListener() {
        return new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it
                Log.d(TAG, "Service discovery success" + service);
                String name = service.getServiceName();
                if (URLUtil.isNetworkUrl(name)) {
                    JSONObject result = new JSONObject();
                    try {
                        result.put("url", name);
                        JSONObject metadata = new JSONObject();
                        metadata.put("type", service.getServiceType());
                        result.put("metadata", metadata);

                        MDNSScanner.this.notify(result);
                    } catch (JSONException e) {
                        Log.e(TAG, "Could not create response object for " + name);
                    }
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost" + service);
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
        };
    }

    /**
     * Starts the mDNS discovery.
     */
    @Override
    protected void doStart() {
        mNsdManager.discoverServices(MDNS_SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    /**
     * Stops the mDNS discovery.
     */
    @Override
    public void stop() {
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }

    /**
     * Kind of scanner type.
     * @return String name of the scanner.
     */
    @Override
    public String scannerType() {
        return SCAN_TYPE;
    }
}
