package com.excubivue.cubivue_scanners.scanner

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.excubivue.cubivue_scanners.scanner.mlkit.ScannerActivity
import com.excubivue.cubivue_scanners.scanner.utils.ScannerType
import com.excubivue.cubivue_scanners.scanner.vision.MaterialBarcodeScannerBuilder
import com.excubivue.cubivue_scanners.scanner.zxing.CustomScannerActivity
import com.google.firebase.FirebaseApp
import com.google.zxing.integration.android.IntentIntegrator

/**
 * Created by umair on 2019-05-17 12:19
 * for bk-android
 */
object ScannerHelper : LifecycleObserver {

    private val TAG = "ScannerHelper"
    private var registry: ActivityResultRegistry? = null


    private var isQRScannerOverlayEnabled: Boolean? = false
    const val KEY_OVERLAY_ENABLED: String = "overlayEnabled"


    fun setRegistry(registry: ActivityResultRegistry?) {
        ScannerHelper.registry = registry
    }

    fun setQRScannerOverlaySettings(overlayEnabled: Boolean) {
        isQRScannerOverlayEnabled = overlayEnabled
    }

    interface BarcodeScanListener {
        fun onScanned(value: String, scannerType: ScannerType)
    }

    private var scanListener: BarcodeScanListener? = null

    fun setListener(listener: BarcodeScanListener) {
        scanListener = listener
    }

    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null

    fun onCreate(owner: LifecycleOwner) {

        activityResultLauncher = registry?.registerActivityResultCallback("key", owner, ActivityResultContracts.StartActivityForResult(), ActivityResultCallback<ActivityResult> {
            Log.i(TAG, "onCreate: "+ it.data.toString())
        })
    }

    fun scannedResult(result: String) {
        scanListener?.onScanned(result, ScannerType.MLKIT)
    }

    /**
     * Note: For ZXing scanner, it must always be called from inside an Activity class,
     * otherwise result will not be delivered to 'onActivityResult' in some cases.
     */
    fun openScanner(activity: Activity?, scannerType: ScannerType) {

        activity?.let {

            when (scannerType) {
                ScannerType.ZXING -> {
                    val integrator = IntentIntegrator(activity)
                    integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES)
                    integrator.setCameraId(0)
                    integrator.setCaptureActivity(CustomScannerActivity::class.java)
                    integrator.setBeepEnabled(true)
                    integrator.setOrientationLocked(true)
                    integrator.initiateScan()
                }
                ScannerType.VISION -> {

                    val materialBarcodeScanner = MaterialBarcodeScannerBuilder()
                            .withActivity(activity)
                            .withEnableAutoFocus(true)
                            .withBleepEnabled(false)
                            .withBackfacingCamera()
                            .withCenterTracker()
                            .withFlashLightEnabledByDefault()
                            .withResultListener { barcode ->
                                try {
                                    Log.i(TAG, "scanResult: Value: ${barcode.rawValue} ---- ${barcode.displayValue}")
                                    scanListener?.onScanned(barcode.rawValue, scannerType)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Log.i(TAG, "startScan", e)
                                }
                            }
                            .build()
                    materialBarcodeScanner.startScan()
                }
                ScannerType.MLKIT -> {
                    /*val startForResult = activity.prepareCall(ActivityResultContracts.StartActivityForResult()) {

                    }*/
                    val scannerActivityIntent = Intent(activity, ScannerActivity::class.java)
                    scannerActivityIntent.putExtra(KEY_OVERLAY_ENABLED, isQRScannerOverlayEnabled)
                    it.startActivity(scannerActivityIntent)
                    //startForResult.launch(scannerActivityIntent)
                }
            }
        }
    }

    fun parseZXingResult(resultCode: Int, data: Intent?): String? {
        IntentIntegrator.parseActivityResult(resultCode, data)?.let {
            if (it.contents != null) {
                return it.contents
            }
        }

        return null
    }

    fun init(context: Context) {
        FirebaseApp.initializeApp(context)
    }
}