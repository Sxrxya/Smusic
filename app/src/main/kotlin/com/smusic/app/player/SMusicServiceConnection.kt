package com.smusic.app.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.smusic.app.data.repository.MusicRepository
import com.smusic.app.data.repository.SettingsRepository
import com.smusic.app.domain.model.PlayerState
import com.smusic.app.domain.model.RepeatMode
import com.smusic.app.domain.model.Song
import com.smusic.app.domain.usecase.GetStreamUrlUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SMusicServiceConnection @Inject constructor(
    @ApplicationContext private val context: Context,
    private val queueManager: QueueManager,
    private val getStreamUrlUseCase: GetStreamUrlUseCase,
    private val musicRepository: MusicRepository,
    private val settingsRepository: SettingsRepository,
    private val crossfadeHandler: CrossfadeHandler,
    private val equalizerManager: EqualizerManager,
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var positionUpdateJob: Job? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private var playingFrom = ""

    fun connect() {
        if (_isConnected.value) return
        val sessionToken = SessionToken(context, ComponentName(context, SMusicService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            controller = controllerFuture?.let { future ->
                if (future.isDone && !future.isCancelled) future.get() else null
            }
            controller?.let { ctrl ->
                _isConnected.value = true
                setupPlayerListener(ctrl)
                startPositionUpdates()

                // Init EQ
                scope.launch {
                    val prefs = settingsRepository.userPreferences.first()
                    crossfadeHandler.setCrossfadeDuration(prefs.crossfadeSeconds)
                    ctrl.playbackParameters = ctrl.playbackParameters.withSpeed(prefs.playbackSpeed)
                }
            }
        }, MoreExecutors.directExecutor())
    }

    fun disconnect() {
        positionUpdateJob?.cancel()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controller = null
        _isConnected.value = false
    }

    fun playSong(song: Song, songs: List<Song>? = null, source: String = "") {
        playingFrom = source
        scope.launch {
            val queue = songs ?: listOf(song)
            val index = queue.indexOfFirst { it.videoId == song.videoId }.coerceAtLeast(0)
            queueManager.setQueue(queue, index)
            playCurrentFromQueue()
        }
    }

    fun playFromQueue(index: Int) {
        queueManager.skipTo(index)
        scope.launch { playCurrentFromQueue() }
    }

    private suspend fun playCurrentFromQueue() {
        val song = queueManager.currentSong ?: return
        val streamResult = getStreamUrlUseCase(song.videoId)
        streamResult.onSuccess { (url, bitrate) ->
            val updatedSong = song.copy(streamUrl = url, bitrate = bitrate)
            playStream(updatedSong)
            musicRepository.recordPlay(updatedSong)
        }.onFailure {
            updateState { copy(isBuffering = false) }
        }
    }

    private fun playStream(song: Song) {
        val ctrl = controller ?: return
        updateState { copy(isBuffering = true, currentSong = song) }

        val metadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(song.streamUrl)
            .setMediaMetadata(metadata)
            .build()

        ctrl.setMediaItem(mediaItem)
        ctrl.prepare()
        ctrl.play()
    }

    fun playPause() {
        controller?.let { ctrl ->
            if (ctrl.isPlaying) ctrl.pause() else ctrl.play()
        }
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
    }

    fun next() {
        scope.launch {
            val nextSong = queueManager.next()
            if (nextSong != null) {
                playCurrentFromQueue()
            }
        }
    }

    fun previous() {
        val ctrl = controller ?: return
        if (ctrl.currentPosition > 3000) {
            ctrl.seekTo(0)
        } else {
            scope.launch {
                queueManager.previous()
                playCurrentFromQueue()
            }
        }
    }

    fun toggleShuffle() {
        queueManager.toggleShuffle()
        updateState {
            copy(
                shuffleEnabled = queueManager.shuffleEnabled.value,
                queue = queueManager.queue.value,
                currentIndex = queueManager.currentIndex.value,
            )
        }
    }

    fun cycleRepeatMode() {
        queueManager.cycleRepeatMode()
        updateState { copy(repeatMode = queueManager.repeatMode.value) }
    }

    fun setVolume(volume: Float) {
        controller?.volume = volume
        updateState { copy(volume = volume) }
    }

    fun setSpeed(speed: Float) {
        controller?.let { ctrl ->
            ctrl.playbackParameters = ctrl.playbackParameters.withSpeed(speed)
            updateState { copy(playbackSpeed = speed) }
        }
        scope.launch { settingsRepository.updatePlaybackSpeed(speed) }
    }

    fun addToQueue(song: Song) {
        queueManager.addToQueue(song)
        updateState { copy(queue = queueManager.queue.value) }
    }

    fun addNext(song: Song) {
        queueManager.addNext(song)
        updateState { copy(queue = queueManager.queue.value) }
    }

    fun removeFromQueue(index: Int) {
        queueManager.removeFromQueue(index)
        updateState {
            copy(
                queue = queueManager.queue.value,
                currentIndex = queueManager.currentIndex.value,
            )
        }
    }

    fun clearQueue() {
        controller?.stop()
        queueManager.clearQueue()
        updateState { PlayerState() }
    }

    fun moveQueueItem(from: Int, to: Int) {
        queueManager.moveItem(from, to)
        updateState {
            copy(
                queue = queueManager.queue.value,
                currentIndex = queueManager.currentIndex.value,
            )
        }
    }

    fun saveQueueState() {
        scope.launch {
            val queue = queueManager.queue.value
            val index = queueManager.currentIndex.value
            val positionMs = controller?.currentPosition?.toInt() ?: 0
            val queueJson = Json.encodeToString(queue)
            settingsRepository.saveQueueState(queueJson, index, positionMs)
        }
    }

    private fun setupPlayerListener(ctrl: MediaController) {
        ctrl.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateState { copy(isPlaying = isPlaying) }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> updateState { copy(isBuffering = true) }
                    Player.STATE_READY -> {
                        updateState {
                            copy(
                                isBuffering = false,
                                durationMs = ctrl.duration.coerceAtLeast(0),
                            )
                        }
                        // Init EQ with new session
                        equalizerManager.init(ctrl.audioSessionId)
                    }
                    Player.STATE_ENDED -> {
                        scope.launch {
                            val nextSong = queueManager.next()
                            if (nextSong != null) {
                                playCurrentFromQueue()
                            }
                        }
                    }
                    Player.STATE_IDLE -> {}
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                updateState { copy(isBuffering = false) }
            }
        })
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = scope.launch {
            while (true) {
                val ctrl = controller
                if (ctrl != null && ctrl.isPlaying) {
                    val pos = ctrl.currentPosition
                    val dur = ctrl.duration.coerceAtLeast(0)
                    val buf = ctrl.bufferedPosition

                    // Check crossfade
                    if (crossfadeHandler.isEnabled() &&
                        crossfadeHandler.shouldStartCrossfade(pos, dur)
                    ) {
                        crossfadeHandler.startCrossfade()
                    }

                    updateState {
                        copy(
                            positionMs = pos,
                            durationMs = dur,
                            bufferedPositionMs = buf,
                            queue = queueManager.queue.value,
                            currentIndex = queueManager.currentIndex.value,
                            playingFrom = this@SMusicServiceConnection.playingFrom,
                        )
                    }
                }
                delay(200)
            }
        }
    }

    private inline fun updateState(transform: PlayerState.() -> PlayerState) {
        _playerState.value = _playerState.value.transform()
    }
}
