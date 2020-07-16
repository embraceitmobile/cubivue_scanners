package com.excubivue.cubivue_scanners.scanner.mlkit

import android.content.Context
import android.media.SoundPool
import android.os.CountDownTimer
import com.excubivue.cubivue_scanners.R

import java.util.*

class MLSoundPoolPlayer(pContext: Context?) {

    private var previousPlayed = -1
    private var mShortPlayer: SoundPool? = null
    private val mSounds: HashMap<Int, Int?> = HashMap()
    private var isResetRequested = false
    private var isSuccessPlayed = false
    private var timeout = 1500L

    fun playShortResource(piResource: Int, forcePlay: Boolean = false) {
        if (forcePlay) {
            playSound(piResource)
        } else {
            if (piResource == previousPlayed) {
                return
            }
            if (!isSuccessPlayed)
                playSound(piResource)
        }
    }

    private fun playSound(piResource: Int) {
        val iSoundId = mSounds[piResource] as Int
        mShortPlayer?.play(iSoundId, 0.4f, 0.4f, 0, 0, 1f)
        previousPlayed = piResource
        resetFlags()
        isSuccessPlayed = piResource == SUCCESS_SOUND
    }

    private fun resetFlags() {
        if (!isResetRequested) {

            timeout = if (isSuccessPlayed) {
                1500L
            } else {
                2500L
            }

            object : CountDownTimer(timeout, 1000) {
                override fun onFinish() {
                    previousPlayed = -1
                    isResetRequested = false
                    isSuccessPlayed = false
                }

                override fun onTick(millisUntilFinished: Long) {

                }

            }.start()
            isResetRequested = true
        }
    }

    fun release() {
        mShortPlayer?.release()
    }

    init {

        mShortPlayer = SoundPool.Builder()
                .setMaxStreams(1)
                .build()

        mShortPlayer?.load(pContext, FAIL_SOUND, 1).let {
            mSounds[FAIL_SOUND] = it
        }
        mShortPlayer?.load(pContext, SUCCESS_SOUND, 5).let {
            mSounds[SUCCESS_SOUND] = it
        }
    }

    companion object {
        val SUCCESS_SOUND = R.raw.success_01
        val FAIL_SOUND = R.raw.error_01
    }
}