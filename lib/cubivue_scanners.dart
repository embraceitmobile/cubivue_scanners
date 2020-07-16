import 'dart:async';
import 'dart:convert';

import 'package:cubivue_scanners/scan_result.dart';
import 'package:flutter/services.dart';

class CubivueScanners {
  var _tag = "CubivueScanners";
  static const _channel = const MethodChannel('cubivue_scanners');
  static const _eventChannel = EventChannel('cubivue_scanners_stream');

  static ScanResult _scanResultSaved = ScanResult();
 
  static Stream<ScanResult> get scanResult =>
      _scanListenerController.stream;

  static StreamController<ScanResult> _scanListenerController =
  StreamController<ScanResult>();
  
  StreamSubscription<ScanResult> scanSubscription =
  _scanListenerController.stream.asBroadcastStream().listen(
          (data) {
        print("DataReceived: " + data.result);
      },
      onDone: () {},
      onError: (error) {
        print("Some Error");
      });

  Future<String> get startMLKitScanner async {
    final String result = await _channel.invokeMethod('startMLKitScanner');
    print('[$_tag] Received: $result');
    return result;
  }

  Future<String> get startVisionScanner async {
    final String result = await _channel.invokeMethod('startVisionScanner');
    print('[$_tag] Received: $result');
    return result;
  }

  Future<String> get startZXingScanner async {
    final String result = await _channel.invokeMethod('startZXingScanner');
    print('[$_tag] Received: $result');
    return result;
  }
  
  void startScannerService() {
    _eventChannel.receiveBroadcastStream().listen((dynamic event) {
        Map result = jsonDecode(event);
        _scanResultSaved = ScanResult.fromJson(result);
        _scanListenerController.add(_scanResultSaved);
    }, onError: (dynamic error) {});
  }

  StreamSubscription<ScanResult> getScanResults() {
    return scanSubscription;
  }

  void _stopScanListener() {
    _scanListenerController.close();
    scanSubscription.cancel();
  }
}
