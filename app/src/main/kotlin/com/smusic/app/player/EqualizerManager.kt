package com.smusic.app.player

import android.media.AudioManager
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EqualizerManager @Inject constructor() {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var audioSessionId: Int = 0

    val bandCount: Int get() = equalizer?.numberOfBands?.toInt() ?: 10
    val minBandLevel: Int get() = equalizer?.bandLevelRange?.get(0)?.toInt() ?: -1500
    val maxBandLevel: Int get() = equalizer?.bandLevelRange?.get(1)?.toInt() ?: 1500

    fun getBandFrequency(band: Int): Int {
        return equalizer?.getCenterFreq(band.toShort())?.div(1000) ?: 0
    }

    fun init(sessionId: Int) {
        if (sessionId == audioSessionId && equalizer != null) return
        release()
        audioSessionId = sessionId
        try {
            equalizer = Equalizer(0, sessionId).apply { enabled = true }
            bassBoost = BassBoost(0, sessionId).apply { enabled = true }
            virtualizer = Virtualizer(0, sessionId).apply { enabled = true }
            loudnessEnhancer = LoudnessEnhancer(sessionId).apply { enabled = true }
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Failed to init audio effects", e)
        }
    }

    fun setBandLevel(band: Int, level: Int) {
        try {
            equalizer?.setBandLevel(band.toShort(), level.toShort())
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Failed to set band level", e)
        }
    }

    fun getBandLevel(band: Int): Int {
        return try {
            equalizer?.getBandLevel(band.toShort())?.toInt() ?: 0
        } catch (e: Exception) {
            0
        }
    }

    fun setBassBoostStrength(strength: Int) {
        try {
            bassBoost?.setStrength(strength.toShort())
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Failed to set bass boost", e)
        }
    }

    fun setVirtualizerStrength(strength: Int) {
        try {
            virtualizer?.setStrength(strength.toShort())
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Failed to set virtualizer", e)
        }
    }

    fun setLoudnessGain(gainMb: Int) {
        try {
            loudnessEnhancer?.setTargetGain(gainMb)
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Failed to set loudness", e)
        }
    }

    fun applyPreset(bands: List<Int>, bass: Int, virt: Int, loud: Int) {
        bands.forEachIndexed { index, level ->
            setBandLevel(index, level)
        }
        setBassBoostStrength(bass)
        setVirtualizerStrength(virt)
        setLoudnessGain(loud)
    }

    fun resetToFlat() {
        val count = bandCount
        for (i in 0 until count) {
            setBandLevel(i, 0)
        }
        setBassBoostStrength(0)
        setVirtualizerStrength(0)
        setLoudnessGain(0)
    }

    fun getPresetBands(presetName: String): List<Int> {
        return when (presetName) {
            "Bass Boost" -> listOf(600, 500, 300, 100, 0, 0, 0, 0, 0, 0)
            "Pop" -> listOf(-100, 200, 400, 500, 300, 0, -100, -100, 200, 300)
            "Rock" -> listOf(400, 300, 100, 0, -100, 0, 200, 400, 500, 500)
            "Jazz" -> listOf(300, 200, 0, 100, -100, -100, 0, 200, 300, 400)
            "Classical" -> listOf(400, 300, 200, 100, -100, -100, 0, 200, 300, 400)
            "Electronic" -> listOf(500, 400, 200, 0, -200, 0, 100, 300, 400, 500)
            "Hip-Hop" -> listOf(500, 400, 200, 100, -100, -100, 100, 200, 300, 400)
            "R&B" -> listOf(300, 600, 300, -100, -200, -100, 200, 300, 300, 400)
            "Vocal" -> listOf(-200, -100, 0, 200, 500, 500, 300, 100, 0, -100)
            else -> List(10) { 0 } // Flat
        }
    }

    fun release() {
        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
            loudnessEnhancer?.release()
        } catch (e: Exception) {
            Log.e("EqualizerManager", "Failed to release", e)
        }
        equalizer = null
        bassBoost = null
        virtualizer = null
        loudnessEnhancer = null
        audioSessionId = 0
    }
}
