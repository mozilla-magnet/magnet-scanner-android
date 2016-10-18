package org.mozilla.magnet.scanner;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MagnetScannerItem {
    private double mDistance = -1;
    private String mDevice;
    private long mLastSeen;
    private String mType;
    private String mUrl;

    public MagnetScannerItem() {
        touch();
    }

    public void setUrl(String url) { mUrl = url; }
    public String getUrl() { return mUrl; }

    public void setDevice(String device) { mDevice = device; }
    public String getDevice() { return mDevice; }

    public void setDistance(double distance) { mDistance = distance; }
    public double getDistance() { return mDistance; }

    public void setType(String type) { mType = type; }
    public String getType() { return mType; }

    public long getLastSeen() { return mLastSeen; }

    public void touch() {
        mLastSeen = System.currentTimeMillis();
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();

        try {
            json.put("url", mUrl);
            json.put("distance", mDistance);
            json.put("type", mType);
        } catch (JSONException e) {
            e.printStackTrace();
            return json;
        }

        return json;
    }

    public HashMap toHashMap() {
        HashMap<String, Object> hash = new HashMap<>();
        hash.put("url", mUrl);
        hash.put("distance", mDistance);
        hash.put("type", mType);
        return hash;
    }
}
