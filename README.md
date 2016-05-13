Magne Scanner (java)
====================
This is an android library that you can use in your projects to discover physical web objects around you.

Currently discovers urls based on the following protocols:

* Bluetooth Low Energy: Discovering both Eddystone and UriBeacon beacons.
* mDNS: urls advertised via this protocol.

Usage
-----
Drop the library into your dependencies, and start using it like:

```java
Scanner scanner = new Scanner(getApplicationContext());
// Configure your scanner to use both btle and mdns.
scanner.useBTLE().usemDNS();
scanner.start(new ScannerCallback() {
                @Override
                public void onItemFound(JSONObject jsonObject) {
                    System.out.println("====>> " + jsonObject.toString());
                };
            });
...
scanner.stop();
```

Results of the discovery
------------------------
When starting the scanner you will need pass a callback object that implements the `ScannerCallback` interface.

This interface has a method, `onItemFound`, that receives a `JSONObject`. This `JSONObject` has a mandatory field, `url`, that contains the url discovered, and could have a optional field `metadata`, filled with different information depending on how that url has been discovered (in the case of BTLE, you will have distance, transmision power, etc).
