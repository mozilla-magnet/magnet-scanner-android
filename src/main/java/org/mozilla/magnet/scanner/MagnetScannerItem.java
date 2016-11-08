package org.mozilla.magnet.scanner;

import java.util.HashMap;

public class MagnetScannerItem {
    private double mDistance = -1;
    private Double mLatitude;
    private Double mLongitude;
    private String mChannelId;
    private String mImageUri;
    private String mTitle;
    private String mDevice;
    private long mLastSeen;
    private String mIconUri;
    private String mType;
    private String mUrl;

    public MagnetScannerItem(String url) {
        mUrl = url;
        touch();
    }

    public String getUrl() {
        return mUrl;
    }

    public MagnetScannerItem setDevice(String device) {
        mDevice = device;
        return this;
    }

    public String getDevice() {
        return mDevice;
    }

    public MagnetScannerItem setDistance(double distance) {
        mDistance = distance;
        return this;
    }

    public double getDistance() {
        return mDistance;
    }

    public MagnetScannerItem setLatitude(double latitude) {
        mLatitude = latitude;
        return this;
    }

    public Double getLatitude() {
        return mLatitude;
    }

    public MagnetScannerItem setLongitude(double longitude) {
        mLongitude = longitude;
        return this;
    }

    public Double getLongitude() {
        return mLongitude;
    }

    public MagnetScannerItem setType(String type) {
        mType = type;
        return this;
    }

    public String getType() {
        return mType;
    }

    public MagnetScannerItem setChannelId(String channelId) {
        mChannelId = channelId;
        return this;
    }

    public String getChannelId() {
        return mChannelId;
    }

    public MagnetScannerItem setImage(String imageUri) {
        mImageUri = imageUri;
        return this;
    }

    public String getImage() {
        return mImageUri;
    }

    public MagnetScannerItem setIcon(String iconUri) {
        mIconUri = iconUri;
        return this;
    }

    public String getIcon() {
        return mIconUri;
    }

    public MagnetScannerItem setTitle(String title) {
        mTitle = title;
        return this;
    }

    public String getTitle() {
        return mTitle;
    }

    public long getLastSeen() {
        return mLastSeen;
    }

    public void touch() {
        mLastSeen = System.currentTimeMillis();
    }

    public HashMap serialize() {
        HashMap<String, Object> hash = new HashMap<>();
        hash.put("type", getType());
        hash.put("url", getUrl());
        hash.put("distance", getDistance());
        hash.put("channel_id", getChannelId());
        if (getLatitude() != null && getLongitude() != null) {
            hash.put("latitude", getLatitude());
            hash.put("longitude", getLongitude());
        }
        return hash;
    }
}
