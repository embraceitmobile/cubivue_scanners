package com.excubivue.cubivue_scanners.scanner.mlkit

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.os.CountDownTimer
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleObserver
import com.excubivue.cubivue_scanners.R
import com.excubivue.cubivue_scanners.scanner.ScannerHelper
import com.google.firebase.ml.common.FirebaseMLException
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.excubivue.cubivue_scanners.scanner.mlkit.common.CameraSource
import com.excubivue.cubivue_scanners.scanner.mlkit.kotlin.barcodescanning.BarcodeScanningProcessor
import kotlinx.android.synthetic.main.activity_scanner.*
import java.io.IOException


@SuppressLint("Registered")
open class ScannerFactory() : AppCompatActivity(), LifecycleObserver {

    private val TAG = "ScannerFactory"

    private var cameraSource: CameraSource? = null

    private var mSoundPoolPlayer: MLSoundPoolPlayer? = null

    var lightOn = true

    private val listOfScannedItems = arrayListOf<ScannedItem>()
    private val listOfScannedBarcode = arrayListOf<ScannedItem>()

    var scannerHelper = ScannerHelper

    private var delayValue = 3

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        mSoundPoolPlayer = MLSoundPoolPlayer(this)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
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
    fun btnLightClicked() {

        if (!lightOn) {
            cameraSource?.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH)
            lightOn = true
            flash_button.isSelected = true

        } else {
            cameraSource?.setFlashMode(Camera.Parameters.FLASH_MODE_OFF)

            lightOn = false
            flash_button.isSelected = false
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

    fun allPermissionsGranted(): Boolean {
        if (!isPermissionGranted(this, Manifest.permission.CAMERA)) {
            return false
        }
        return true
    }

    fun getRuntimePermissions() {
        val allNeededPermissions = arrayListOf<String>()
        if (!isPermissionGranted(this, Manifest.permission.CAMERA)) {
            allNeededPermissions.add(Manifest.permission.CAMERA)
        }

        if (allNeededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toTypedArray(), ScannerActivity.PERMISSION_REQUESTS
            )
        }
    }

    fun createCameraSource() {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = CameraSource(this, fireFaceOverlay)
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
                    it?.let {
                        doOnDetection(it.trim())
                    }
                }
            }
        }
    }

    private fun doOnDetection(value: String) {
        if (isNotAlreadyScanned(value)) {
            listOfScannedBarcode.filter {
                it.item == value
            }.let {
                if (it.isEmpty()) {
                    addToList(value)
                } else {
                    updateList(value)
                    listOfScannedBarcode
                }
            }

            mSoundPoolPlayer?.playShortResource(R.raw.success_01)
            //vibrate()
            //Log.i(TAG, value)

            //Send Result
            scannerHelper.let {
                it.scannedResult(value)
                cv_text.visibility = View.VISIBLE
                detected_bar_code.text = value
                hideAfterDelay()
                finish()
            }

        } /*else {
            doIfAlreadyScanned()
        }*/
    }

    private fun doIfAlreadyScanned() {
        try {
            detected_bar_code.visibility = View.VISIBLE
            //detected_bar_code.text = "Already Scanned."
            hideAfterDelay()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun hideAfterDelay() {
        object : CountDownTimer(2000, 1000) {
            override fun onFinish() {
                detected_bar_code.visibility = View.GONE
            }

            override fun onTick(millisUntilFinished: Long) {

            }

        }.start()
    }

    private fun addToList(value: String) {
        listOfScannedBarcode.add(
                ScannedItem(
                        value,
                        System.currentTimeMillis()
                )
        )

        listOfScannedItems.add(
                ScannedItem(
                        value,
                        System.currentTimeMillis()
                )
        )
    }

    private fun updateList(value: String) {
        listOfScannedBarcode.map {
            it.time = System.currentTimeMillis()
        }
        listOfScannedItems.add(
                ScannedItem(
                        value,
                        System.currentTimeMillis()
                )
        )
    }

    private fun isNotAlreadyScanned(value: String): Boolean {
        listOfScannedBarcode.filter { item ->
            item.item == value
        }.let {
            if (it.isEmpty()) {
                return true
            } else {
                if (hasXSecondsPassed(delayValue, it.first().time)) {
                    return true
                }
            }
        }
        return false
    }

    private fun showHideCamera(show: Boolean) {
        if (show) {

            if (mSoundPoolPlayer == null) {
                mSoundPoolPlayer = MLSoundPoolPlayer(this)
            }

            firePreview?.visibility = View.VISIBLE

            //Set Camera Flash Mode
            cameraSource?.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH)

        } else {
            firePreview?.visibility = View.GONE
            //Set Camera Flash Mode
            cameraSource?.setFlashMode(Camera.Parameters.FLASH_MODE_OFF)

            mSoundPoolPlayer?.release()
            mSoundPoolPlayer = null

        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        if (allPermissionsGranted()) {
            createCameraSource()
            startCamera()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(
                        context,
                        permission
                ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    fun stopCamera() {
        cameraSource?.release()
    }

    fun startCamera() {
        startCameraSource()
        showHideCamera(true)
    }
}