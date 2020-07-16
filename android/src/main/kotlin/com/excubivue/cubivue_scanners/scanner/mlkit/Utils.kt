package com.excubivue.cubivue_scanners.scanner.mlkit

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import java.util.*

fun Context.vibrate(milliseconds: Long = 500) {
    val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    // Check whether device/hardware has a vibrator
    val canVibrate: Boolean = vibrator.hasVibrator()

    if (canVibrate) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // void vibrate (VibrationEffect vibe)
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    milliseconds,
                    // The default vibration strength of the device.
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            // This method was deprecated in API level 26
            vibrator.vibrate(milliseconds)
        }
    }
}


/**
 * Has X Seconds Passed
 *
 * Sends True, if an x Seconds are passed from time to compare.
 */
fun hasXSecondsPassed(seconds: Int, timeToCompare: Long): Boolean {

    try {
        val different = Date().time - timeToCompare
        val secondsInMilli: Long = 1000
        val ellapsedSeconds = different / secondsInMilli

        if (Math.abs(ellapsedSeconds) >= seconds) {
            return true
        }
    } catch (e: Exception) {
        //e.printStackTrace();
    }
    return false
}
