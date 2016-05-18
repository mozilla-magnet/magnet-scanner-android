package org.mozilla.magnet.net.scanner;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.TestCase;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mozilla.magnet.net.scanner.btle.BTLEScanner;
import org.mozilla.magnet.net.scanner.mdns.MDNSScanner;

import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class ScannerTest extends TestCase {

    @Mock private Map<String, PWScanner> scannersList;
    @InjectMocks private Scanner scanner;
    @Mock Context context;
    @Mock BTLEScanner btleScanner;
    @Mock MDNSScanner mdnsScanner;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        scanner = new Scanner(context);
        scanner.useBTLE(btleScanner).usemDNS(mdnsScanner);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void start() {
        ScannerCallback cb = new ScannerCallback() {
            @Override
            public void onItemFound(JSONObject obj) {

            }
        };
        scanner.start(cb);

        verify(btleScanner).start(cb);
        verify(mdnsScanner).start(cb);
    }

    @Test
    public void stop() {
        scanner.stop();

        verify(btleScanner).stop();
        verify(mdnsScanner).stop();
    }
}
