package com.excubivue.cubivue_scanners.scanner.mlkit

interface MLKitScannerCallback {
    fun onScanned(scannedItem: List<String>)
}