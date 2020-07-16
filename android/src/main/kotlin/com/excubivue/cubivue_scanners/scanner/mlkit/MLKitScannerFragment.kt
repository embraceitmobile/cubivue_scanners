package com.excubivue.cubivue_scanners.scanner.mlkit

import android.Manifest.permission.CAMERA
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.excubivue.cubivue_scanners.R
import com.excubivue.cubivue_scanners.scanner.mlkit.common.CameraSource
import com.excubivue.cubivue_scanners.scanner.mlkit.kotlin.barcodescanning.BarcodeScanningProcessor
import com.google.android.gms.common.annotation.KeepName
import com.google.firebase.ml.common.FirebaseMLException
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import kotlinx.android.synthetic.main.mlkit_scanner.*
import java.io.IOException


@KeepName
class MLKitScannerFragment : Fragment() {

    var onScanned: MLKitScannerCallback? = null

    private var cameraSource: CameraSource? = null

    var lightOn = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.mlkit_scanner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_flash.setOnClickListener {
            btnLightClicked()
        }

        if (allPermissionsGranted()) {
            createCameraSource()
        } else {
            getRuntimePermissions()
        }
    }

    /**
     * Gets the current flash mode setting.
     *
     * @return current flash mode. null if flash mode setting is not
     * supported or the camera is not yet created.
     * @see Camera.Parameters#FLASH_MODE_OFF
     * @see Camera.Parameters#FLASH_MODE_AUTO
     * @see Camera.Parameters#FLASH_MODE_ON
     * @see Camera.Parameters#FLASH_MODE_RED_EYE
     * @see Camera.Parameters#FLASH_MODE_TORCH
     */
    private fun btnLightClicked() {

        if (!lightOn) {
            cameraSource?.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH)
            btn_flash?.text = "Turn Flash Off"
            lightOn = true

        } else {
            cameraSource?.setFlashMode(Camera.Parameters.FLASH_MODE_OFF)
            btn_flash?.text = "Turn Flash On"
            lightOn = false
        }

    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private fun startCameraSource() {
        cameraSource?.let {
            try {
                if (firePreview == null) {
                    Log.d(TAG, "resume: Preview is null")
                }
                if (fireFaceOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null")
                }
                firePreview?.start(cameraSource, fireFaceOverlay)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start camera source.", e)
                cameraSource?.release()
                cameraSource = null
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        startCameraSource()
        cameraSource?.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH)
    }

    /** Stops the camera.  */
    override fun onPause() {
        super.onPause()
        firePreview?.stop()
        cameraSource?.setFlashMode(Camera.Parameters.FLASH_MODE_OFF)
    }

    public override fun onDestroy() {
        super.onDestroy()
        cameraSource?.release()
    }

    private fun allPermissionsGranted(): Boolean {
        activity?.let {
            if (!isPermissionGranted(it, CAMERA)) {
                return false
            }
        }
        return true
    }

    private fun getRuntimePermissions() {
        val allNeededPermissions = arrayListOf<String>()
        activity?.let {
            if (!isPermissionGranted(it, CAMERA)) {
                allNeededPermissions.add(CAMERA)
            }
        }

        if (allNeededPermissions.isNotEmpty()) {
            activity?.let {
                ActivityCompat.requestPermissions(
                        it, allNeededPermissions.toTypedArray(), PERMISSION_REQUESTS
                )
            }
        }
    }

    private fun createCameraSource() {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            activity?.let {
                cameraSource = CameraSource(it, fireFaceOverlay)
            }
        }

        try {
            Log.i(TAG, "Using Barcode Detector Processor")
            cameraSource?.setMachineLearningFrameProcessor(BarcodeScanningProcessor(::onBarCodesDetected))
        } catch (e: FirebaseMLException) {
            e.printStackTrace()
        }
    }

    private fun onBarCodesDetected(barcodes: List<FirebaseVisionBarcode>) {
        if (barcodes.isNotEmpty()) {
            barcodes.forEach { it ->
                it.displayValue.let {
                    val list = barcodes.map { it.displayValue!! }
                    onScanned?.onScanned(list)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        Log.i(TAG, "Permission granted!")
        if (allPermissionsGranted()) {
            createCameraSource()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        private val TAG = "MLKitScannerFragment"
        private const val PERMISSION_REQUESTS = 1


        private fun isPermissionGranted(context: Context, permission: String): Boolean {
            if (ContextCompat.checkSelfPermission(
                            context,
                            permission
                    ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.i(TAG, "Permission granted: $permission")
                return true
            }
            Log.i(TAG, "Permission NOT granted: $permission")
            return false
        }
    }
}