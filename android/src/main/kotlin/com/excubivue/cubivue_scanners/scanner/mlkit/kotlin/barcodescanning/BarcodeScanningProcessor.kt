package com.excubivue.cubivue_scanners.scanner.mlkit.kotlin.barcodescanning

import android.graphics.Bitmap
import android.util.Log
import com.excubivue.cubivue_scanners.scanner.mlkit.common.CameraImageGraphic
import com.excubivue.cubivue_scanners.scanner.mlkit.common.FrameMetadata
import com.excubivue.cubivue_scanners.scanner.mlkit.common.GraphicOverlay
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.excubivue.cubivue_scanners.scanner.mlkit.kotlin.VisionProcessorBase
import java.io.IOException

/** Barcode Detector Demo.  */
class BarcodeScanningProcessor(private val onBarCodesDetected: (barcodes: List<FirebaseVisionBarcode>) -> Unit) :
    VisionProcessorBase<List<FirebaseVisionBarcode>>() {

    // Note that if you know which format of barcode your app is dealing with, detection will be
    // faster to specify the supported barcode formats one by one, e.g.
    private val detector: FirebaseVisionBarcodeDetector by lazy {
        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_ALL_FORMATS)
            .build()
        FirebaseVision.getInstance().getVisionBarcodeDetector(options)
    }

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Barcode Detector: $e")
        }
    }

    override fun detectInImage(image: FirebaseVisionImage): Task<List<FirebaseVisionBarcode>> {
        return detector.detectInImage(image)
    }

    override fun onSuccess(
            originalCameraImage: Bitmap?,
            barcodes: List<FirebaseVisionBarcode>,
            frameMetadata: FrameMetadata,
            graphicOverlay: GraphicOverlay
    ) {
        onBarCodesDetected.invoke(barcodes)

        graphicOverlay.clear()

        originalCameraImage?.let {
            val imageGraphic = CameraImageGraphic(graphicOverlay, it)
            graphicOverlay.add(imageGraphic)
        }

        if (isOverlayOn){
            barcodes.forEach {
                val barcodeGraphic = BarcodeGraphic(graphicOverlay, it)
                graphicOverlay.add(barcodeGraphic)
            }
            graphicOverlay.postInvalidate()
        }
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "Barcode detection failed $e")
    }

    companion object {

        private const val TAG = "BarcodeScanProc"
        var isOverlayOn : Boolean = false
    }
}