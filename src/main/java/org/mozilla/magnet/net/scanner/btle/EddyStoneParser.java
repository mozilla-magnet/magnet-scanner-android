package org.mozilla.magnet.net.scanner.btle;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

/**
 * Created by arcturus on 10/05/2016.
 * Most of the code coming from:
 * https://github.com/google/physical-web/blob/master/android/PhysicalWeb/app/src/main/java/org/physical_web/physicalweb/ble/UriBeacon.java
 */
public class EddyStoneParser {

    private final static String TAG = EddyStoneParser.class.getName();

    private static final int DATA_TYPE_SERVICE_DATA = 0x16;
    private static final byte[] URI_SERVICE_16_BIT_UUID_BYTES = {(byte) 0xd8, (byte) 0xfe};
    private static final byte[] URIBEACON_SERVICE_16_BIT_UUID_BYTES = { (byte) 0xaa, (byte) 0xfe};
    private static final byte URIBEACON_URL_FRAME_TYPE = 0x10;

    private static final List<String> URI_SCHEMES = Arrays.asList(
            "http://www.",
            "https://www.",
            "http://",
            "https://"); // urn:uuid: not supported

    private static final List<String> URL_CODES = Arrays.asList(
            ".com/",
            ".org/",
            ".edu/",
            ".net/",
            ".info/",
            ".biz/",
            ".gov/",
            ".com",
            ".org",
            ".edu",
            ".net",
            ".info",
            ".biz",
            ".gov"
    );


    public static JSONObject parse(byte[] record) {
        JSONObject result = new JSONObject();

        byte[] serviceData = parseEddystoneRecord(record);
        int currentPos = 0;
        byte flags;
        byte txPowerLevel;
        String uri;
        if (serviceData != null && serviceData.length >= 2) {
            flags = serviceData[currentPos++];
            txPowerLevel = serviceData[currentPos++];
            uri = decodeUri(serviceData, currentPos);
        } else {
            serviceData = parseUribeaconRecord(record);
            if (serviceData == null || serviceData.length < 2) {
                return null;
            }
            currentPos = 0;
            txPowerLevel = serviceData[currentPos++];
            flags = (byte) (serviceData[currentPos] >> 4);
            serviceData[currentPos] = (byte) (serviceData[currentPos] & 0xFF);
            uri = decodeUri(serviceData, currentPos);
        }

        if (uri == null) {
            return null;
        }

        try {
            result.put("url", uri);
            result.put("flags", flags);
            result.put("txPower", txPowerLevel);
        } catch (JSONException e) {
            return null;
        }

        return result;
    }

    private static String decodeUri(byte[] serviceData, int offset) {
        if (serviceData.length <= offset) {
            return null;
        }

        byte schemeByte = serviceData[offset++];
        String scheme = URI_SCHEMES.get(schemeByte);
        if (schemeByte >= URI_SCHEMES.size()) {
            return null;
        }
        String url = new String(scheme);
        while(offset < serviceData.length) {
            byte b = serviceData[offset++];
            if (b >= URL_CODES.size()) {
                url += (char) b;
            } else {
                url += URL_CODES.get(b);
            }
        }
        return url;
    }

    private static byte[] parseEddystoneRecord(byte[] scanRecord) {
        int currentPos = 0;
        try {
            while (currentPos < scanRecord.length) {
                int fieldLength = scanRecord[currentPos++] & 0xff;
                if (fieldLength == 0) {
                    break;
                }
                int fieldType = scanRecord[currentPos] & 0xff;
                if (fieldType == DATA_TYPE_SERVICE_DATA) {
                    // The first two bytes of the service data are service data UUID.
                    if (scanRecord[currentPos + 1] == URI_SERVICE_16_BIT_UUID_BYTES[0]
                            && scanRecord[currentPos + 2] == URI_SERVICE_16_BIT_UUID_BYTES[1]) {
                        // jump to data
                        currentPos += 3;
                        // length includes the length of the field type and ID
                        byte[] bytes = new byte[fieldLength - 3];
                        System.arraycopy(scanRecord, currentPos, bytes, 0, fieldLength - 3);
                        return bytes;
                    }
                }
                // length includes the length of the field type
                currentPos += fieldLength;
            }
        } catch (Exception e) {
            Log.e(TAG, "unable to parse scan record: " + Arrays.toString(scanRecord), e);
        }
        return null;
    }

    private static byte[] parseUribeaconRecord(byte[] scanRecord) {
        int currentPos = 0;
        try {
            while (currentPos < scanRecord.length) {
                int fieldLength = scanRecord[currentPos++] & 0xff;
                if (fieldLength == 0) {
                    break;
                }
                int fieldType = scanRecord[currentPos] & 0xff;
                if (fieldType == DATA_TYPE_SERVICE_DATA) {
                    if (scanRecord[currentPos + 1] == URIBEACON_SERVICE_16_BIT_UUID_BYTES[0]
                            && scanRecord[currentPos + 2] == URIBEACON_SERVICE_16_BIT_UUID_BYTES[1]
                            && scanRecord[currentPos + 3] == URIBEACON_URL_FRAME_TYPE) {
                        // Jump to beginning of frame.
                        currentPos += 4;
                        // TODO: Add tests
                        // field length - field type - ID - frame type
                        byte[] bytes = new byte[fieldLength - 4];
                        System.arraycopy(scanRecord, currentPos, bytes, 0, fieldLength - 4);
                        return bytes;
                    }
                }
                // length includes the length of the field type.
                currentPos += fieldLength;
            }
        } catch (Exception e) {
            Log.e(TAG, "unable to parse scan record: " + Arrays.toString(scanRecord), e);
        }
        return null;
    }
}
