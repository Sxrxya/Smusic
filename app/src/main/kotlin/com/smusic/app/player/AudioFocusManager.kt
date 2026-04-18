package com.smusic.app.player

import android.content.Context
import android.media.AudioManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioFocusManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var focusChangeListener: ((Int) -> Unit)? = null
    private var hasFocus = false

    private val afChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        focusChangeListener?.invoke(focusChange)
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> hasFocus = true
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> hasFocus = false
        }
    }

    fun setOnFocusChangeListener(listener: (Int) -> Unit) {
        focusChangeListener = listener
    }

    @Suppress("DEPRECATION")
    fun requestFocus(): Boolean {
        val result = audioManager.requestAudioFocus(
            afChangeListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN,
        )
        hasFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        return hasFocus
    }

    @Suppress("DEPRECATION")
    fun abandonFocus() {
        audioManager.abandonAudioFocus(afChangeListener)
        hasFocus = false
    }

    fun hasFocus(): Boolean = hasFocus
}
