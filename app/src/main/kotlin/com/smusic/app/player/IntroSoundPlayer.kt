package com.smusic.app.player

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.SoundPool
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

@Singleton
class IntroSoundPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var soundPool: SoundPool? = null
    private var soundId: Int = -1
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(attributes)
            .build()
            
        // Attempt to load from raw if it exists, otherwise we'll use fallback
        val resId = context.resources.getIdentifier("smusic_intro", "raw", context.packageName)
        if (resId != 0) {
            soundId = soundPool?.load(context, resId, 1) ?: -1
        }
    }

    fun playIntroSound(isMusicPlaying: Boolean) {
        if (isMusicPlaying) return
        if (audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL) return
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) return

        if (soundId != -1) {
            soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
        } else {
            playFallbackSynth()
        }
    }

    private fun playFallbackSynth() {
        scope.launch {
            try {
                val sampleRate = 44100
                val durationS = 2.4
                val numSamples = (durationS * sampleRate).toInt()
                val samples = ShortArray(numSamples)
                
                val hitFrame = (1.2 * sampleRate).toInt()
                
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    var value = 0.0
                    
                    // 0 - 1.1s: Sub-bass swell (55Hz)
                    if (t < 1.1) {
                        val amp = t / 1.1
                        value += 0.3 * amp * sin(2.0 * PI * 55.0 * t)
                    }
                    
                    // 1.0 - 1.2s: Rising shimmer
                    if (t in 1.0..1.2) {
                        val amp = (t - 1.0) / 0.2
                        value += 0.1 * amp * Math.random() // White noise shimmer
                    }
                    
                    // 1.2s: HIT (C Major chord: C5=523.25, E5=659.25, G5=783.99)
                    if (t >= 1.2) {
                        val elapsed = t - 1.2
                        val decay = exp(-4.0 * elapsed)
                        value += 0.2 * decay * sin(2.0 * PI * 523.25 * elapsed)
                        value += 0.2 * decay * sin(2.0 * PI * 659.25 * elapsed)
                        value += 0.2 * decay * sin(2.0 * PI * 783.99 * elapsed)
                    }
                    
                    samples[i] = (value * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                    .setAudioFormat(AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(samples, 0, samples.size)
                audioTrack.play()
                
                // Release after play
                delay(2500)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                // Fail silently
            }
        }
    }

    private suspend fun delay(ms: Long) = kotlinx.coroutines.delay(ms)

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}
