package com.smusic.app.player

import android.content.Intent
import android.net.wifi.WifiManager
import android.os.PowerManager
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class SMusicService : MediaSessionService() {

    @Inject lateinit var equalizerManager: EqualizerManager

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        // Build OkHttp client with proper timeouts and YouTube-compatible headers
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()

        // Use OkHttp DataSource with proper User-Agent for YouTube streams
        val dataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
            .setUserAgent(
                "com.google.android.apps.youtube.music/6.42.52 " +
                "(Linux; U; Android 13; en_IN; Pixel 7 Build/TQ3A.230901.001)"
            )

        val mediaSourceFactory = DefaultMediaSourceFactory(this)
            .setDataSourceFactory(dataSourceFactory)

        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()
            .also { exo ->
                exo.playWhenReady = true
                exo.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_READY) {
                            equalizerManager.init(exo.audioSessionId)
                        }
                    }
                })
            }

        mediaSession = MediaSession.Builder(this, player!!)
            .build()

        acquireWakeLock()
        acquireWifiLock()

        Log.d("SMusicService", "Service created with OkHttp DataSource")
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player ?: return
        if (!player.playWhenReady || player.mediaItemCount == 0 ||
            player.playbackState == Player.STATE_ENDED
        ) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        player = null
        equalizerManager.release()
        releaseWakeLock()
        releaseWifiLock()
        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    private fun acquireWakeLock() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "smusic:playback")
        wakeLock?.acquire(24 * 60 * 60 * 1000L) // 24 hours max
    }

    private fun releaseWakeLock() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    @Suppress("DEPRECATION")
    private fun acquireWifiLock() {
        val wm = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "smusic:wifi")
        wifiLock?.acquire()
    }

    private fun releaseWifiLock() {
        wifiLock?.let { if (it.isHeld) it.release() }
        wifiLock = null
    }
}
