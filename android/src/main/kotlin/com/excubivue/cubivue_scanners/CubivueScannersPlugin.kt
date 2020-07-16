package com.excubivue.cubivue_scanners

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.excubivue.cubivue_scanners.scanner.ScannerHelper
import com.excubivue.cubivue_scanners.scanner.models.ScanResult
import com.excubivue.cubivue_scanners.scanner.utils.ScannerType
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry


class CubivueScannersPlugin : FlutterPlugin, ActivityAware, PluginRegistry.RequestPermissionsResultListener {

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        Log.i(TAG, "onAttachedToEngine")
        setUpPluginMethods(flutterPluginBinding.applicationContext, flutterPluginBinding.binaryMessenger)
    }

    companion object {
        private val TAG = "CubivueScannersPlugin"
        private var REQUEST_PERMISSION = 1512
        private var channel: MethodChannel? = null
        private var event_channel: EventChannel? = null
        var eventSink: EventChannel.EventSink? = null
        private var currentActivity: Activity? = null
        private var isStarted = false
        private val scannerHelper = ScannerHelper

        @JvmStatic
        var binaryMessenger: BinaryMessenger? = null

        @JvmStatic
        fun registerWith(registrar: PluginRegistry.Registrar) {
            Log.i(TAG, "registerWith: CubivueScannersPlugin")
            val instance = CubivueScannersPlugin()
            registrar.addRequestPermissionsResultListener(instance)
            requestPermissions()
            setUpPluginMethods(registrar.activity(), registrar.messenger())
        }

        @JvmStatic
        fun registerWith(messenger: BinaryMessenger, context: Context) {
            Log.i(TAG, "registerWith: CubivueScannersPlugin")
            val instance = CubivueScannersPlugin()
            requestPermissions()
            setUpPluginMethods(context, messenger)
        }

        @JvmStatic
        fun isMyServiceRunning(serviceClass: Class<*>, context: Context): Boolean {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                if (serviceClass.name == service.service.className) {
                    return true
                }
            }
            return false
        }

        @JvmStatic
        private fun setUpPluginMethods(context: Context, messenger: BinaryMessenger) {
            Log.i(TAG, "setUpPluginMethods")

            channel = MethodChannel(messenger, "cubivue_scanners")
            notifyIfPermissionsGranted(context)

            channel?.setMethodCallHandler { call, result ->
                when (call.method) {
                    "startMLKitScanner" -> {
                        currentActivity.let { activity ->
                            isStarted = true
                            val helper = scannerHelper.apply {
                                setListener(object : ScannerHelper.BarcodeScanListener {
                                    override fun onScanned(value: String, scannerType: ScannerType) {
                                        Log.i("startMLKitScanner", "scanned: $value")
                                        eventSink?.success(ScanResult(value, scannerType.value).toString())
                                    }
                                })
                            }
                            helper.openScanner(activity, ScannerType.MLKIT)
                            result.success("MLKit scanner started.")
                        }
                    }
                    "startVisionScanner" -> {
                        currentActivity.let { activity ->
                            isStarted = true
                            val helper = scannerHelper.apply {
                                setListener(object : ScannerHelper.BarcodeScanListener {
                                    override fun onScanned(value: String, scannerType: ScannerType) {
                                        Log.i("startVisionScanner", "scanned: $value")
                                        eventSink?.success(ScanResult(value, scannerType.value).toString())
                                    }
                                })
                            }
                            helper.openScanner(activity, ScannerType.VISION)
                            result.success("Vision scanner started.")
                        }
                    }
                    "startZXingScanner" -> {
                        currentActivity?.let { activity ->
                            isStarted = true
                            scannerHelper.openScanner(activity, ScannerType.ZXING)
                            result.success("ZXing scanner started.")
                        }
                    }
                    else -> result.notImplemented()
                }
            }

            event_channel = EventChannel(messenger, "cubivue_scanners_stream")
            event_channel?.setStreamHandler(object : EventChannel.StreamHandler {
                override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                    eventSink = events
                }

                override fun onCancel(arguments: Any?) {

                }
            })
        }

        @JvmStatic
        private fun notifyIfPermissionsGranted(context: Context) {
            if (permissionsGranted(context)) {
                doIfPermissionsGranted()
            }
        }

        @JvmStatic
        fun permissionsGranted(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        }

        @JvmStatic
        private fun doIfPermissionsGranted() {
            channel?.let {
                Log.i(TAG, "doIfPermissionsGranted: Send event.")
                it.invokeMethod("recordPermissionsGranted", "")
            }
        }

        @JvmStatic
        private fun requestPermissions() {
            if (!arePermissionsGranted()) {
                Log.i(TAG, "requestRecordPermission: Requesting record audio permissions..")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    currentActivity?.let {
                        ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.CAMERA), REQUEST_PERMISSION)
                    }
                            ?: Log.e(TAG, "requestRecordPermission: Unable to request storage permissions.")
                } else {
                    doIfPermissionsGranted()
                }
            } else {
                doIfPermissionsGranted()
            }
        }

        @JvmStatic
        private fun arePermissionsGranted(): Boolean {
            currentActivity?.let {
                return ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            }
            return false
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        currentActivity = null
        Log.i(TAG, "onDetachedFromEngine")
        channel?.setMethodCallHandler(null)
        event_channel?.setStreamHandler(null)
    }

    override fun onAttachedToActivity(activityPluginBinding: ActivityPluginBinding) {
        Log.i(TAG, "onAttachedToActivity")
        currentActivity = activityPluginBinding.activity
        activityPluginBinding.addRequestPermissionsResultListener(this)
        requestPermissions()
    }

    override fun onDetachedFromActivityForConfigChanges() {
        Log.i(TAG, "onDetachedFromActivityForConfigChanges")
    }

    override fun onReattachedToActivityForConfigChanges(activityPluginBinding: ActivityPluginBinding) {
        Log.i(TAG, "onReattachedToActivityForConfigChanges")
        currentActivity = activityPluginBinding.activity
        activityPluginBinding.addRequestPermissionsResultListener(this)
    }

    override fun onDetachedFromActivity() {
        Log.i(TAG, "onDetachedFromActivity")
        currentActivity = null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?): Boolean {
        if (requestCode == REQUEST_PERMISSION && grantResults?.isNotEmpty()!! && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            doIfPermissionsGranted()
            return true
        }
        return false
    }
}

