# magnet-scanner-android

> Discover URLs around you.

Supports:

- Bluetooth Low Energy: Discovering both Eddystone and UriBeacon beacons.
- mDNS: URLs advertised via this protocol.
- geolocation: Nearby URLs found in the magnet-service.

### Usage

Drop the library into your dependencies, and start using it like:

```java
Scanner scanner = new Scanner(getApplicationContext());
  .useBle()
  .useMdns()
  .useGeolocation()
  .addListener(new MagnetScannerListener() {
    @Override
    public void onItemFound(MagnetScannerItem item) { ... }

    @Override
    public void onItemLost(MagnetScannerItem item) { ... };
  });

scanner.start();

// later on
scanner.stop();
scanner.removeListener(this);
```
