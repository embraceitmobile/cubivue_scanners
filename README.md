# cubivue_scanners

A flutter plugin for scanning Barcodes & QR codes with different scanners.

#### Scanners Included:

1. MLKit Scanner
2. ZXing Scanner
3. Vision Scanner

## How to use it?

#### Android Setup
___________________

#### Step 1: (Only required for MLKit scanner)
Add google play services dependency on app's gradle file.

```groovy
dependencies {
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"
}
apply plugin: "com.google.gms.google-services"
```

#### Step 2: (Only required for MLKit scanner)
Download and add 'google-services.json' from Firebase to your project.

#### Step 3: (Only required for MLKit scanner)
Init Firebase.

```kotlin
    class MainActivity : FlutterActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            //Initialize Scanner
            ScannerHelper.init(this)
        }
    }
```

#### Step 4: (Only required for ZXing scanner)
Setup listener for ZXing scanner.

```kotlin
    class MainActivity : FlutterActivity() {

        public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

            ScannerHelper.parseZXingResult(resultCode, data)?.let {
                Log.i("parseZXingResult", "onActivityResult: $it")
                CubivueScannersPlugin.eventSink?.success(ScanResult(it, ScannerType.ZXING.value).toString())
            }
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
```

#### Flutter Setup
___________________

## Install
In your pubspec.yaml

```yaml
dependencies:
  cubivue_scanners: [LATEST_VERSION]
```

```dart
    import 'package:cubivue_scanners/cubivue_scanners.dart';
```


#### Start Scanner Service
__________________________________


```dart
 var _service = CubivueScanners();

    _service.startScannerService();

    _service.getScanResults().onData((data) {
      print("getScanResults: ${data.result} , ${data.scannerType}");
    });
```


## For MLKit Scanner

```dart
     await _service.startMLKitScanner;
```

## For ZXing Scanner

```dart
    await _service.startZXingScanner;
```

## For Vision Scanner

```dart
    await _service.startVisionScanner;
```

# Author

cubivue_scanners plugin is developed by CubiVue Developers. You can email us at <embraceitcubivue@gmail.com> for any queries.