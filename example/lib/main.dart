import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:cubivue_scanners/cubivue_scanners.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  var _service = CubivueScanners();
  var result = "";

  @override
  void initState() {
    _service.startScannerService();
    _service.getScanResults().onData((data) {
      print("getScanResults: ${data.result} , ${data.scannerType}");
    });

    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Scanners Demo'),
        ),
        body: Center(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.center,
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              RaisedButton(
                child: Text("ML Kit Scanner"),
                onPressed: () async {
                  await _service.startMLKitScanner;
                },
              ),
              RaisedButton(
                child: Text("Vision Scanner"),
                onPressed: () async {
                  await _service.startVisionScanner;
                },
              ),
              RaisedButton(
                child: Text("ZXing Scanner"),
                onPressed: () async {
                  await _service.startZXingScanner;
                },
              )
            ],
          ),
        ),
      ),
    );
  }
}
