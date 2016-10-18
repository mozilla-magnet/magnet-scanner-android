package org.mozilla.magnet.scanner;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.mozilla.magnet.scanner.ble.ScannerBLE;
import org.mozilla.magnet.scanner.geolocation.ScannerGeolocation;
import org.mozilla.magnet.scanner.mdns.ScannerMDNS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This class offers the capabilities of discovering urls that are around you.
 * That is, is a library for the Physical Web.
 * It also allows to configure which kind of discovery mechanism you want to
 * use.
 * When starting the scan you will need to pass a callback object implementing
 * MagnetScannerCallback to receive the results discovered.
 * For example, for discovering web pages via Bluetooth Low Energy and mDNS
 * you can use:
 * <pre>
 * {@code
 * MagnetScanner scanner = new MagnetScanner(getApplicationContext());
 * scanner.useBLE().useMDNS();
 * scanner.start(...);
 *}
 * </pre>
 * The notified results are JSONObjects, which will have a mandatory field 'url'
 * with the recently discovered url, and a optional 'metadata' field, with extra
 * information provided by the different discovery mechanism.
 *
 * @author Francisco Jordano
 */
public class MagnetScanner implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, MagnetScannerListener {
    private static final String TAG = "MagnetScanner";
    public static final String ACTION_START_SCAN = "org.mozilla.magnet.scanner.START_SCAN";
    public static final String ACTION_STOP_SCAN = "org.mozilla.magnet.scanner.STOP_SCAN";
    public static final long BACKGROUND_SCAN_INTERVAL = TimeUnit.MINUTES.toMillis(5);
    public static final long BACKGROUND_SCAN_INTERVAL_FASTEST = BACKGROUND_SCAN_INTERVAL / 6;
    private ArrayList<MagnetScannerListener> mListeners = new ArrayList<>();
    private PendingIntent mPendingIntentLocation;
    private boolean mBackgroundScanning = false;
    private GoogleApiClient mGoogleApiClient;
    private Runnable mScanComplete;
    private Handler mHandler;

    /**
     * List of installed scanners.
     */
    private final Map<String, BaseScanner> mScanners = new HashMap<>();
    private Context mContext = null;

    /**
     * Constructor with Context.
     * @param ctx Context needed to instantiate some of the scanners
     */
    public MagnetScanner(Context ctx) {
        mContext = ctx;
    }

    /**
     * Configures the scanner to use the Bluetooth Low Energy scan.
     * @param btleScanner ScannerBLE scanner object, used for testing
     * @return MagnetScanner self object to allow chaining.
     */
    public MagnetScanner useBLE(ScannerBLE btleScanner) {
        if (!mScanners.containsKey(ScannerBLE.class.getName())) {
            if (btleScanner == null) {
                btleScanner = new ScannerBLE(mContext, this);
            }
            mScanners.put(ScannerBLE.class.getName(), btleScanner);
        }
        return this;
    }

    /**
     * Configures the scanner to use mDNS scan.
     * @param mdnsScanner MagnetScanner already build, used for testing.
     * @return MagnetScanner self object to allow chaining.
     */
    public MagnetScanner useMDNS(ScannerMDNS mdnsScanner) {
        if (!mScanners.containsKey(ScannerMDNS.class.getName())) {
            if (mdnsScanner == null) {
                mdnsScanner = new ScannerMDNS(mContext, this);
            }
            mScanners.put(ScannerMDNS.class.getName(), mdnsScanner);
        }
        return this;
    }

    /**
     * Configures the scanner to use geolocation scan.
     * @return MagnetScanner self object to allow chaining.
     */
    public MagnetScanner useGeolocation(ScannerGeolocation scanner) {
        if (!mScanners.containsKey(ScannerGeolocation.class.getName())) {
            if (scanner == null) {
                scanner = new ScannerGeolocation(mContext, this);
            }
            mScanners.put(ScannerGeolocation.class.getName(), scanner);
        }
        return this;
    }

    /**
     * Once the object has been configure with the different scannig strategies, you need
     * to call `start` to properly trigger the scanning.
     */
    public MagnetScanner start() {
        Log.d(TAG, "start");

        for (BaseScanner scanner: mScanners.values()) {
            scanner.start();
        }

        return this;
    }

    /**
     * Stops the scanning strategies.
     */
    public MagnetScanner stop() {
        Log.d(TAG, "stop");

        for (BaseScanner scanner: mScanners.values()) {
            scanner.stop();
        }

        return this;
    }

    public MagnetScanner addListener(MagnetScannerListener listener) {
        if (!mListeners.contains(listener)) { mListeners.add(listener); }
        return this;
    }

    public MagnetScanner removeListener(MagnetScannerListener listener) {
        mListeners.remove(listener);
        return this;
    }

    @Override
    public void onItemFound(MagnetScannerItem item) {
        for (MagnetScannerListener listener: mListeners) {
          listener.onItemFound(item);
        }
    }

    @Override
    public void onItemLost(MagnetScannerItem item) {
        for (MagnetScannerListener listener: mListeners) {
            listener.onItemLost(item);
        }
    }

    public void startBackgroundScanning() {
        if (mBackgroundScanning) return;
        Log.d(TAG, "start background scanning");

        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();
    }

    public void stopBackgroundScanning() {
        if (!mBackgroundScanning) return;
        Log.d(TAG, "stop background scanning");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mPendingIntentLocation);

        // send a broadcast to tell background scanner to stop
        Intent intent = new Intent(MagnetScanner.ACTION_STOP_SCAN);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

        mGoogleApiClient.disconnect();
        mBackgroundScanning = false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "on connected");
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(BACKGROUND_SCAN_INTERVAL)
                .setSmallestDisplacement(10)
                .setFastestInterval(BACKGROUND_SCAN_INTERVAL_FASTEST);

        Intent intent = new Intent(ACTION_START_SCAN);
        intent.putExtra("started", System.currentTimeMillis());
        mPendingIntentLocation = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, mPendingIntentLocation);
        mBackgroundScanning = true;
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "connection failed");
    }

    public interface ScanCallback {
        public void onScanComplete(ArrayList<MagnetScannerItem> items);
    }

}
