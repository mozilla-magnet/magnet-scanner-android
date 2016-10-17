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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.magnet.scanner.BaseScanner;
import org.mozilla.magnet.scanner.MagnetScannerItem;

public class ScannerGeolocation extends BaseScanner implements ConnectionCallbacks, OnConnectionFailedListener {
    private final static String TAG = "ScannerGeolocation";
    private final static String SCANNER_TYPE = "geolocation";
    private final static String SLUG_BASE_URL = "https://tengam.org/";
    private final static String API_SEARCH_URL = "https://tengam.org/content/v1/search/beacons/";
    private final static int SCAN_RADIUS_METERS = 10;
    private GoogleApiClient mGoogleApiClient;
    private RequestQueue mQueue;

    public ScannerGeolocation(Context context) {
        mQueue = Volley.newRequestQueue(context);

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    @Override
    public String scannerType() {
        return SCANNER_TYPE;
    }

    @Override
    protected void doStart() {
        mGoogleApiClient.connect();
    }

    @Override
    public void stop() {
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Log.d(TAG, "lat: " + location.getLatitude());
        Log.d(TAG, "lon: " + location.getLongitude());
        Log.d(TAG, "accuracy: " + location.getAccuracy());
        scan(location);
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

        JsonArrayRequest request = new JsonArrayRequest(method, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                onScanResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "request error: " + error);
            }
        });

        mQueue.add(request);
    }

    private void onScanResponse(JSONArray json) {
        Log.d(TAG, "scan response: " + json);

        for (int i = 0 ; i < json.length(); i++) {
            try {
                JSONObject item = json.getJSONObject(i);
                MagnetScannerItem scannerItem = new MagnetScannerItem();
                scannerItem.setType(SCANNER_TYPE);
                scannerItem.setUrl(SLUG_BASE_URL + item.getString("slug"));
                notify(scannerItem);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
