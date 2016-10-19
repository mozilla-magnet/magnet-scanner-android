package org.mozilla.magnet.scanner;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.concurrent.TimeUnit;

/**
 * Created by wilsonpage on 19/10/2016.
 */

public class BackgroundScannerClient implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private final static String TAG = "BackgroundScannerClient";
    public static final String ACTION_START_SCAN = "org.mozilla.magnet.scanner.START_SCAN";
    public static final String ACTION_STOP_SCAN = "org.mozilla.magnet.scanner.STOP_SCAN";
    public static final long BACKGROUND_SCAN_INTERVAL = TimeUnit.MINUTES.toMillis(10);
    public static final long BACKGROUND_SCAN_INTERVAL_FASTEST = TimeUnit.MINUTES.toMillis(1);
    GoogleApiClient mGoogleApiClient;
    boolean mStarting = false;
    boolean mStopping = false;
    Context mContext;

    public BackgroundScannerClient(Context context) {
        mContext = context;
    }

    public void start() {
        Log.d(TAG, "start");
        mStarting = true;

        GoogleApiClient googleApiClient = getGoogleApiClient();

        // proceed if client is already connected
        if (googleApiClient.isConnected()) {
            finishStart();
            return;
        }

        // connect and wait for callback
        googleApiClient.connect();
    }

    /**
     * This location request is responsible for the waking
     * of `ServiceBackgroundScanner` which performs a full
     * scan and broadcasts the results to the host app.
     *
     * `LocationRequest` configuration can get complex,
     * [this article](https://goo.gl/0DqShd) helps
     * to explain common strategies.
     *
     * Our approach is to use a low-power/low-accuracy
     * scan to wake the background service, which then
     * performs a short high-accuracy/high-power scan
     * to fetch location based results.
     */
    private void finishStart() {
        Log.d(TAG, "finish start");
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(BACKGROUND_SCAN_INTERVAL)
                .setSmallestDisplacement(10)
                .setFastestInterval(BACKGROUND_SCAN_INTERVAL_FASTEST);

        PendingIntent pendingIntent = getPendingIntent();
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, pendingIntent);
        mStarting = false;
    }

    public void stop() {
        Log.d(TAG, "stop");
        mStopping = true;
        GoogleApiClient googleApiClient = getGoogleApiClient();

        // proceed if client is already connected
        if (googleApiClient.isConnected()) {
            finishStop();
            return;
        }

        // connect and wait for callback
        googleApiClient.connect();
    }

    private void finishStop() {
        Log.d(TAG, "finish stop");
        PendingIntent pendingIntent = getPendingIntent();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, pendingIntent);
        pendingIntent.cancel();

        // send a broadcast to tell background scanner
        // to stop any currently active scanning
        Intent intent = new Intent(ACTION_STOP_SCAN);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

        mGoogleApiClient.disconnect();
        mStopping = false;
    }

    private GoogleApiClient getGoogleApiClient() {
        if (mGoogleApiClient != null) { return mGoogleApiClient; }

        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        return mGoogleApiClient;
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(ACTION_START_SCAN);
        intent.putExtra("timestamp", System.currentTimeMillis());
        return PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "on connected");
        if (mStarting) finishStart();
        else if (mStopping) finishStop();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "connection failed");
    }
}
