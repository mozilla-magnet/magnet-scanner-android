package org.mozilla.magnet.scanner;

import android.content.Context;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mozilla.magnet.scanner.ble.ScannerBle;
import org.mozilla.magnet.scanner.mdns.ScannerMdns;

import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class ScannerTest extends TestCase {

    @Mock private Map<String, BaseScanner> scannersList;
    @InjectMocks private MagnetScanner magnetScanner;
    @Mock private Context context;
    @Mock private ScannerBle btleScanner;
    @Mock private ScannerMdns mdnsScanner;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        magnetScanner
                .useBle(btleScanner)
                .useMdns(mdnsScanner);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    @Ignore
    public void start() {
        MagnetScannerListener cb = new MagnetScannerListener() {
            @Override
            public void onItemFound(MagnetScannerItem obj) {

            }

            @Override
            public void onItemLost(MagnetScannerItem obj) {

            }
        };
        magnetScanner.start(cb);

        verify(btleScanner).start(cb);
        verify(mdnsScanner).start(cb);

        verify(scannersList, calls(2));
    }

    @Test
    @Ignore
    public void stop() {
        magnetScanner.stop();

        verify(btleScanner).stop();
        verify(mdnsScanner).stop();
    }

}
