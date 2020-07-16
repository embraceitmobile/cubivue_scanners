package com.excubivue.cubivue_scanners.scanner.zxing

import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import com.excubivue.cubivue_scanners.R
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.ViewfinderView
import kotlinx.android.synthetic.main.activity_custom_scanner.*
import kotlinx.android.synthetic.main.custom_barcode_scanner.*

/**
 * Custom Scannner Activity extending from Activity to display a custom layout form scanner view.
 */
class CustomScannerActivity : Activity() {

    private var capture: CaptureManager? = null
    private var barcodeScannerView: DecoratedBarcodeView? = null
    private var viewfinderView: ViewfinderView? = null
    private val TAG = "CustomScannerActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_scanner)

        barcodeScannerView = zxing_barcode_scanner
        barcodeScannerView!!.setTorchOn()

        viewfinderView = zxing_viewfinder_view

        capture = CaptureManager(this, barcodeScannerView!!)
        capture!!.initializeFromIntent(intent, savedInstanceState)
        capture!!.decode()
    }

    override fun onResume() {
        super.onResume()
        capture!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture!!.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture!!.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture!!.onSaveInstanceState(outState)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return barcodeScannerView!!.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }
}
