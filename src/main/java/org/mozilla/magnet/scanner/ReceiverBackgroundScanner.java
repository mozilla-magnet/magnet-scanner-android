package org.mozilla.magnet.scanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.mozilla.magnet.scanner.geolocation.ScannerGeolocation;

/**
 * Created by wilsonpage on 17/10/2016.
 */

public class ReceiverBackgroundScanner extends BroadcastReceiver {
    private final String TAG = "ReceiverBackgroundScan";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "on receive: " + intent.getAction());
        Intent serviceIntent = new Intent(context, ServiceBackgroundScanner.class);

        Bundle bundle = intent.getExtras();
        long timeStarted = bundle.getLong("timestamp");
        long elapsed = System.currentTimeMillis() - timeStarted;

        // the background requestLocationUpdates fires as soon as
        // it's scheduled; we ignore any broadcasts that happen
        // 'too soon' after the app haes gone to the background
        if (elapsed < BackgroundScannerClient.BACKGROUND_SCAN_INTERVAL_FASTEST) {
            Log.d(TAG, "too soon to scan: " + elapsed);
            return;
        }

        context.startService(serviceIntent);
    }
}
