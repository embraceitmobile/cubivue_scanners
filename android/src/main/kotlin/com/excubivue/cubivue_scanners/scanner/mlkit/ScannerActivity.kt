package com.excubivue.cubivue_scanners.scanner.mlkit

import android.os.Bundle
import com.excubivue.cubivue_scanners.R
import com.excubivue.cubivue_scanners.scanner.ScannerHelper
import com.excubivue.cubivue_scanners.scanner.mlkit.kotlin.barcodescanning.BarcodeScanningProcessor
import com.google.android.gms.common.annotation.KeepName
import kotlinx.android.synthetic.main.activity_scanner.*


@KeepName
class ScannerActivity : ScannerFactory() {

    companion object {
        private val TAG = "MainActivity"
        const val PERMISSION_REQUESTS = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        scannerHelper.setRegistry(this.activityResultRegistry)
        scannerHelper.let {
            lifecycle.addObserver(it)
        }

        flash_button.isSelected = true
        flash_button.isSelected = true
        BarcodeScanningProcessor.isOverlayOn = false

        setScannerOverlayOption()

        flash_button.setOnClickListener {
            btnLightClicked()
        }

        if (allPermissionsGranted()) {
            createCameraSource()
        } else {
            getRuntimePermissions()
        }
    }

    private fun setScannerOverlayOption() {
        if (intent != null && intent.hasExtra(ScannerHelper.KEY_OVERLAY_ENABLED)) {
            val isOverlayEnabled = intent?.getBooleanExtra(ScannerHelper.KEY_OVERLAY_ENABLED, false)
            isOverlayEnabled?.let {
                BarcodeScanningProcessor.isOverlayOn = isOverlayEnabled
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        if (allPermissionsGranted()) {
            startCamera()
        }
    }

    /** Stops the camera.  */
    override fun onPause() {
        super.onPause()
        firePreview?.stop()
    }

    public override fun onDestroy() {
        super.onDestroy()
        stopCamera()
    }
}
