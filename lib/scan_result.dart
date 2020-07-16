class ScanResult {
  String result;
  String scannerType;

  ScanResult({this.result, this.scannerType});

  ScanResult.fromJson(Map<String, dynamic> json) {
    result = json['result'];
    scannerType = json['scannerType'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['result'] = this.result;
    data['scannerType'] = this.scannerType;
    return data;
  }

  @override
  String toString() {
    return 'ScanResult{result: $result, scannerType: $scannerType}';
  }
}
