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
import com.smusic.app.data.db.dao.DownloadDao
import com.smusic.app.data.repository.MusicRepository
import com.smusic.app.data.repository.SettingsRepository
import com.smusic.app.domain.engine.LanguageResolver
import com.smusic.app.domain.engine.RecommendationEngine
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
    private val downloadDao: DownloadDao,
    private val recommendationEngine: RecommendationEngine,
    private val languageResolver: LanguageResolver,
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var positionUpdateJob: Job? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    var playingFrom = ""
    private var consecutiveFailures = 0
    private var activeListeningSong: Song? = null
    private var activeListenMs: Long = 0L
    private var activeCountedPlay: Boolean = false
    private var lastPlaybackPos: Long = 0L

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
            // Ensure controller is connected
            if (controller == null) {
                connect()
                var retryCount = 0
                while (controller == null && retryCount < 10) {
                    delay(500)
                    retryCount++
                }
            }

            val queue = songs ?: listOf(song)
            val index = queue.indexOfFirst { it.videoId == song.videoId }.coerceAtLeast(0)
            queueManager.setQueue(queue, index)
            playCurrentFromQueue()
            
            // Spotify-like recommendation: load related songs if queue is small
            if (queue.size <= 1) {
                loadRelatedSongs(song.videoId)
            }
        }
    }

    private suspend fun loadRelatedSongs(videoId: String) {
        if (playingFrom == "playlist" || playingFrom == "custom_playlist") {
            return
        }
        val currentSong = queueManager.currentSong ?: return
        val context = languageResolver.resolveFromSong(currentSong)
        musicRepository.getRelatedSongs(videoId).onSuccess { related ->
            val history = queueManager.queue.value.map { it.videoId }
            val ranked = recommendationEngine.rankCandidates(
                seed = currentSong,
                context = context,
                candidates = related,
                history = history
            )
            ranked.forEach { song ->
                if (queueManager.queue.value.none { it.videoId == song.videoId }) {
                    queueManager.addToQueue(song)
                }
            }
            updateState { copy(queue = queueManager.queue.value) }
        }
    }

    fun playFromQueue(index: Int) {
        queueManager.skipTo(index)
        scope.launch { playCurrentFromQueue() }
    }

    private var playJob: Job? = null

    private suspend fun playCurrentFromQueue() {
        val song = queueManager.currentSong ?: return
        
        // Cancel any pending play request to avoid race conditions
        playJob?.cancel()
        
        playJob = scope.launch {
            updateState { copy(isBuffering = true, currentSong = song) }

            // 0. Offline Check: Play local downloaded file immediately if available
            try {
                if (song.streamUrl.isNotBlank()) {
                    val file = if (song.streamUrl.startsWith("file://")) {
                        java.io.File(android.net.Uri.parse(song.streamUrl).path ?: "")
                    } else {
                        java.io.File(song.streamUrl)
                    }
                    if (file.exists() && file.length() > 0) {
                        val fileUri = android.net.Uri.fromFile(file).toString()
                        val offlineSong = song.copy(streamUrl = fileUri)
                        playStream(offlineSong)
                        return@launch
                    }
                }
                val localDownload = downloadDao.getDownloadById(song.videoId)
                if (localDownload != null && localDownload.state == "DOWNLOADED" && localDownload.filePath.isNotBlank()) {
                    val file = java.io.File(localDownload.filePath)
                    if (file.exists() && file.length() > 0) {
                        val fileUri = android.net.Uri.fromFile(file).toString()
                        val offlineSong = song.copy(streamUrl = fileUri, bitrate = localDownload.bitrate)
                        playStream(offlineSong)
                        musicRepository.recordSongStarted(offlineSong)
                        return@launch
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w("SMusicPlay", "Local playback check fallback: ${e.message}")
            }
            
            val streamResult = getStreamUrlUseCase(song.videoId, song.title, song.artist)
            streamResult.onSuccess { (url, bitrate) ->
                consecutiveFailures = 0
                // Final check to ensure we are still supposed to play this song
                if (queueManager.currentSong?.videoId == song.videoId) {
                    val validDuration = if (song.durationSeconds > 0) {
                        song.durationSeconds
                    } else {
                        val exoDuration = (controller?.duration ?: 0L) / 1000L
                        if (exoDuration > 0) exoDuration.toInt() else 180
                    }
                    val updatedSong = song.copy(streamUrl = url, bitrate = bitrate, durationSeconds = validDuration)
                    playStream(updatedSong)
                    musicRepository.recordSongStarted(updatedSong)
                    
                    if (queueManager.currentIndex.value >= queueManager.queue.value.size - 2) {
                        loadRelatedSongs(song.videoId)
                    }
                }
            }.onFailure { error ->
                if (queueManager.currentSong?.videoId == song.videoId) {
                    consecutiveFailures++
                    updateState { copy(isBuffering = false) }
                    android.util.Log.e("SMusicPlay", "Failed to play ${song.title} (${song.videoId}): ${error.message}")
                    if (consecutiveFailures < 3 && queueManager.hasNext()) {
                        delay(1000)
                        next()
                    } else {
                        consecutiveFailures = 0
                        android.util.Log.w("SMusicPlay", "Playback auto-skip stopped after consecutive failures")
                    }
                }
            }
        }
    }

    private fun playStream(song: Song) {
        val ctrl = controller
        if (ctrl == null) {
            android.util.Log.e("SMusicPlay", "Cannot playStream: MediaController is null! Retrying connection...")
            connect()
            scope.launch {
                var retry = 0
                while (controller == null && retry < 10) {
                    delay(300)
                    retry++
                }
                controller?.let { playStream(song) }
            }
            return
        }

        updateState { copy(isBuffering = true, currentSong = song) }

        val metadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setArtworkUri(android.net.Uri.parse(song.thumbnailUrl))
            .build()

        val uri = if (song.streamUrl.startsWith("http://") || song.streamUrl.startsWith("https://") || song.streamUrl.startsWith("file://") || song.streamUrl.startsWith("content://")) {
            android.net.Uri.parse(song.streamUrl)
        } else {
            android.net.Uri.fromFile(java.io.File(song.streamUrl))
        }

        val mediaItemBuilder = MediaItem.Builder()
            .setMediaId(song.videoId)
            .setUri(uri)
            .setMediaMetadata(metadata)

        if (song.streamUrl.contains(".m3u8") || song.streamUrl.contains("manifest/hls")) {
            mediaItemBuilder.setMimeType(androidx.media3.common.MimeTypes.APPLICATION_M3U8)
        }

        val mediaItem = mediaItemBuilder.build()

        ctrl.setMediaItem(mediaItem)
        ctrl.prepare()
        ctrl.play()
        android.util.Log.d("SMusicPlay", "Started playback for: ${song.title}")
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

    fun addToQueue(songs: List<Song>) {
        queueManager.addToQueue(songs)
        updateState { copy(queue = queueManager.queue.value) }
    }

    fun addNext(song: Song) {
        queueManager.addNext(song)
        updateState { copy(queue = queueManager.queue.value) }
    }

    fun addNext(songs: List<Song>) {
        queueManager.addNext(songs)
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
                                isPlaying = ctrl.isPlaying,
                                durationMs = ctrl.duration.coerceAtLeast(0),
                            )
                        }
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
                android.util.Log.e("SMusicPlay", "ExoPlayer error: ${error.errorCodeName} (${error.errorCode}) - ${error.message}", error)
                updateState { copy(isBuffering = false, isPlaying = false) }
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

                    // Track accurate listen duration and play counts
                    val curSong = queueManager.currentSong
                    if (curSong?.videoId != activeListeningSong?.videoId) {
                        val prevSong = activeListeningSong
                        val prevMs = activeListenMs
                        if (prevSong != null && prevMs > 1000L) {
                            scope.launch(Dispatchers.IO) {
                                musicRepository.recordListenDuration(prevSong, prevMs)
                            }
                        }
                        activeListeningSong = curSong
                        activeListenMs = 0L
                        activeCountedPlay = false
                        lastPlaybackPos = pos
                    } else if (curSong != null) {
                        if (pos > lastPlaybackPos && pos - lastPlaybackPos < 2000L) {
                            val delta = pos - lastPlaybackPos
                            activeListenMs += delta
                        }
                        lastPlaybackPos = pos

                        if (!activeCountedPlay && activeListenMs >= 15_000L) {
                            activeCountedPlay = true
                            scope.launch(Dispatchers.IO) {
                                musicRepository.recordPlayCount(curSong)
                            }
                        }
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

    fun getCurrentMediaItem(): MediaItem? {
        return controller?.currentMediaItem
    }

    private inline fun updateState(transform: PlayerState.() -> PlayerState) {
        _playerState.value = _playerState.value.transform()
    }
}
