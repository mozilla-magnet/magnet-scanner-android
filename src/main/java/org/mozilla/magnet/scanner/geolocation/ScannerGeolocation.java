package org.mozilla.magnet.scanner.geolocation;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.magnet.scanner.BaseScanner;
import org.mozilla.magnet.scanner.MagnetScannerItem;
import org.mozilla.magnet.scanner.MagnetScannerListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ScannerGeolocation extends BaseScanner implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
    private final static String TAG = "ScannerGeolocation";
    private final static String SCANNER_TYPE = "geolocation";
    private final static String API_SEARCH_URL = "https://tengam.org/content/v1/search/beacons/";
    private final static int MIN_DISTANCE_CHANGE_METERS = 10;
    private final static int LOCATION_INTERVAL = 3000;
    private final static int MIN_ACCURACY_METERS = 20;
    private final static int SCAN_RADIUS_METERS = 100;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private RequestQueue mQueue;
    private Listeners mListeners;

    public ScannerGeolocation(Context context) {
        Log.d(TAG, "create");
        mQueue = Volley.newRequestQueue(context);
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public String scannerType() {
        return SCANNER_TYPE;
    }

    @Override
    public void start(MagnetScannerListener listener) {
        super.start(listener);
        Log.d(TAG, "start");

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void stop() {
        super.stop();
        Log.d(TAG, "stop");

        // there's a chance that the GoogleApiClient might not
        // be connected yet, in which case we can't disconnect
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

        // clear the last location so that a
        // scan will be performed next `.start()`
        mLastLocation = null;
    }

    public void addListeners(Listeners listeners) {
        mListeners = listeners;
    }

    /**
     * Called when the GoogleApiClient is connected
     * and we're ready to register for location updates.
     *
     * There is a chance that the scanner could have been
     * stopped before the GoogleApiClient has connected.
     * To prevent errors we check that the scanner isn't
     * 'stopped'.
     *
     * @param connectionHint
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        if (!isStarted()) return;

        LocationRequest locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(LOCATION_INTERVAL)
            .setFastestInterval(LOCATION_INTERVAL);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        float accuracy = location.getAccuracy();
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        Log.d(TAG, "lat: " + lon);
        Log.d(TAG, "lon: " + lat);
        Log.d(TAG, "accuracy: " + accuracy);

        if (accuracy > MIN_ACCURACY_METERS) {
            Log.d(TAG, "not accurate enough");
            return;
        }

        // quality-check the location before use
        if (!isValidNextLocation(location)) {
            Log.d(TAG, "location didn't change");
            return;
        }

        mLastLocation = location;
        scan(location);
    }

    /**
     * In order for a location to be 'valid' there must
     * either not be an existing location or the location
     * just be far enough away from the last scanned location
     * to warrant us scanning again.
     *
     * @param location
     * @return boolean
     */
    private boolean isValidNextLocation(Location location) {
        return mLastLocation == null || location.distanceTo(mLastLocation) > MIN_DISTANCE_CHANGE_METERS;
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(TAG, "connection failed: " + result);
    }

    @Override
    public void onConnectionSuspended(int result) {
        Log.e(TAG, "connection suspended: " + result);
    }

    private void scan(Location location) {
        String lat = String.valueOf(location.getLatitude());
        String lon = String.valueOf(location.getLongitude());
        String url = API_SEARCH_URL + lat + "," + lon + "," + SCAN_RADIUS_METERS;
        int method = Request.Method.GET;
        Log.d(TAG, "scanning: " + url);

        JsonArrayRequest request = new JsonArrayRequest(method, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                onScanResponse(response);
                onScanComplete();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "request error: " + error);
                onScanComplete();
            }
        });

        mQueue.add(request);
    }

    private void onScanResponse(JSONArray json) {
        Log.d(TAG, "scan response: " + json);

        HashMap<String,MagnetScannerItem> oldItems = getItems();
        HashMap<String,MagnetScannerItem> newItems = inflateJsonItems(json);
        ArrayList<String> toRemove = new ArrayList<>();

        // remove old items
        Iterator iterator = oldItems.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            String id = (String) pair.getKey();

            // remove old items not found in the latest scan
            if (!newItems.containsKey(id)) {
                toRemove.add(id);
            }
        }

        // remove items in separate loop to avoid
        // `ConcurrentModificationException`
        for (String id: toRemove) {
            removeItem(id);
        }

        // add new items
        iterator = newItems.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            String id = (String) pair.getKey();
            MagnetScannerItem item = (MagnetScannerItem) pair.getValue();

            // add new items found in the scan
            if (!oldItems.containsKey(id)) {
                addItem(item);
            }
        }
    }

    private void onScanComplete() {
        Log.d(TAG, "on scan complete");
        if (mListeners == null) { return; }
        mListeners.onGeolocationScanComplete();
    }

    /**
     * Transform JSONArray into a HashMap.
     * @param jsonArray
     * @return
     */
    private HashMap<String,MagnetScannerItem> inflateJsonItems(JSONArray jsonArray) {
        HashMap<String,MagnetScannerItem> result = new HashMap<>();

        for (int i = 0 ; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonItem = jsonArray.getJSONObject(i);
                String url = jsonItem.getString("url");
                MagnetScannerItem scannerItem = new MagnetScannerItem(url);
                scannerItem.setType(SCANNER_TYPE);
                scannerItem.setChannelId(jsonItem.getString("channel_id"));
                JSONObject jsonLocation = jsonItem.getJSONObject("location");
                scannerItem.setLatitude(jsonLocation.getDouble("latitude"));
                scannerItem.setLongitude(jsonLocation.getDouble("longitude"));
                result.put(url, scannerItem);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public interface Listeners {
        public void onGeolocationScanComplete();
    }
}
