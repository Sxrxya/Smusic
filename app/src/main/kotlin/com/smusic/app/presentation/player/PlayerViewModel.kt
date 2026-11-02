package com.smusic.app.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smusic.app.data.repository.DownloadRepository
import com.smusic.app.data.repository.LibraryRepository
import com.smusic.app.data.repository.MusicRepository
import com.smusic.app.domain.engine.LanguageResolver
import com.smusic.app.domain.engine.RecommendationEngine
import com.smusic.app.domain.model.DownloadState
import com.smusic.app.domain.model.LanguageContext
import com.smusic.app.domain.model.LyricLine
import com.smusic.app.domain.model.PlayerState
import com.smusic.app.domain.model.Playlist
import com.smusic.app.domain.model.Song
import com.smusic.app.domain.usecase.GetLyricsUseCase
import com.smusic.app.domain.usecase.LikeSongUseCase
import com.smusic.app.player.SMusicServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val lyrics: List<LyricLine> = emptyList(),
    val isLoadingLyrics: Boolean = false,
    val isLiked: Boolean = false,
    val isDownloaded: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Int = 0,
    val showLyrics: Boolean = false,
    val showQueue: Boolean = false,
    val showAddToPlaylist: Boolean = false,
    val playlists: List<Playlist> = emptyList(),
    val showSleepTimer: Boolean = false,
    val sleepTimerActive: Boolean = false,
    val sleepTimerRemaining: Int = 0,
    val toastMessage: String? = null,
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    val serviceConnection: SMusicServiceConnection,
    private val getLyricsUseCase: GetLyricsUseCase,
    private val likeSongUseCase: LikeSongUseCase,
    private val recommendationEngine: RecommendationEngine,
    private val musicRepository: MusicRepository,
    private val languageResolver: LanguageResolver,
    private val downloadRepository: DownloadRepository,
    private val libraryRepository: LibraryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    val playerState: StateFlow<PlayerState> = serviceConnection.playerState

    private var currentLyricsSongId: String? = null
    private var downloadCheckJob: Job? = null
    private var sleepTimerJob: Job? = null

    init {
        observePlayerState()
        observePlaylists()
    }

    private fun observePlaylists() {
        viewModelScope.launch {
            libraryRepository.getAllPlaylists().collectLatest { list ->
                _uiState.value = _uiState.value.copy(playlists = list)
            }
        }
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            playerState.collectLatest { state ->
                state.currentSong?.let { song ->
                    // 1. Load lyrics for the new song
                    loadLyrics(song)

                    // 2. Check if song is downloaded
                    observeDownloadStatus(song.videoId)

                    // 3. Handle recommendations if queue is running low
                    if (state.queue.size - state.currentIndex <= 3) {
                        fetchAndRefillQueue(song, state.queue)
                    }
                    
                    verifySongIntegrity(song)
                }
            }
        }
    }

    private fun observeDownloadStatus(videoId: String) {
        downloadCheckJob?.cancel()
        downloadCheckJob = viewModelScope.launch {
            downloadRepository.getDownloadByIdFlow(videoId).collectLatest { entity ->
                val isDownloaded = entity?.state == DownloadState.DOWNLOADED.name
                val isDownloading = entity?.state == DownloadState.DOWNLOADING.name || entity?.state == DownloadState.QUEUED.name
                val progress = entity?.progress ?: 0
                _uiState.value = _uiState.value.copy(
                    isDownloaded = isDownloaded,
                    isDownloading = isDownloading,
                    downloadProgress = progress
                )
            }
        }
    }

    private fun fetchAndRefillQueue(currentSong: Song, currentQueue: List<Song>) {
        if (serviceConnection.playingFrom == "playlist" || serviceConnection.playingFrom == "custom_playlist") {
            return
        }
        viewModelScope.launch {
            val context = languageResolver.resolveFromSong(currentSong)
            
            musicRepository.getRelatedSongs(currentSong.videoId).onSuccess { candidates ->
                val history = currentQueue.map { it.videoId }
                val ranked = recommendationEngine.rankCandidates(
                    seed = currentSong,
                    context = context,
                    candidates = candidates,
                    history = history
                )
                
                if (ranked.isNotEmpty()) {
                    serviceConnection.addToQueue(ranked)
                }
            }
        }
    }

    private fun verifySongIntegrity(song: Song) {
        if (song.videoId.length != 11) {
            // Validation placeholder
        }
    }

    fun loadLyrics(song: Song) {
        if (song.videoId == currentLyricsSongId) return
        currentLyricsSongId = song.videoId

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingLyrics = true)
            getLyricsUseCase(song.artist, song.title).onSuccess { lyrics ->
                _uiState.value = _uiState.value.copy(lyrics = lyrics, isLoadingLyrics = false)
            }.onFailure {
                _uiState.value = _uiState.value.copy(lyrics = emptyList(), isLoadingLyrics = false)
            }
        }
    }

    fun toggleLike(song: Song) {
        viewModelScope.launch {
            val liked = likeSongUseCase(song)
            _uiState.value = _uiState.value.copy(isLiked = liked)
        }
    }

    fun downloadSong(song: Song) {
        viewModelScope.launch {
            if (_uiState.value.isDownloading) {
                downloadRepository.cancelDownload(song.videoId)
                _uiState.value = _uiState.value.copy(
                    isDownloading = false,
                    downloadProgress = 0,
                    toastMessage = "Download paused"
                )
            } else if (!_uiState.value.isDownloaded) {
                _uiState.value = _uiState.value.copy(
                    isDownloading = true,
                    downloadProgress = 0,
                    toastMessage = "Downloading ${song.title} in 320 kbps..."
                )
                downloadRepository.queueDownload(song)
            }
        }
    }

    fun toggleAddToPlaylistSheet() {
        _uiState.value = _uiState.value.copy(showAddToPlaylist = !_uiState.value.showAddToPlaylist)
    }

    fun addSongToPlaylist(playlistId: Long, song: Song) {
        viewModelScope.launch {
            libraryRepository.addSongToPlaylist(playlistId, song)
            _uiState.value = _uiState.value.copy(
                showAddToPlaylist = false,
                toastMessage = "Added to playlist!"
            )
        }
    }

    fun createPlaylistAndAddSong(name: String, song: Song) {
        viewModelScope.launch {
            val playlistId = libraryRepository.createPlaylist(name)
            libraryRepository.addSongToPlaylist(playlistId, song)
            _uiState.value = _uiState.value.copy(
                showAddToPlaylist = false,
                toastMessage = "Created and added to playlist!"
            )
        }
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    fun toggleLyricsSheet() {
        _uiState.value = _uiState.value.copy(showLyrics = !_uiState.value.showLyrics)
    }

    fun toggleQueueSheet() {
        _uiState.value = _uiState.value.copy(showQueue = !_uiState.value.showQueue)
    }

    fun toggleSleepTimer() {
        _uiState.value = _uiState.value.copy(showSleepTimer = !_uiState.value.showSleepTimer)
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes == -1) {
            _uiState.value = _uiState.value.copy(
                sleepTimerActive = true,
                sleepTimerRemaining = -1,
                toastMessage = "Sleep timer set: End of current song"
            )
            val currentId = playerState.value.currentSong?.videoId
            sleepTimerJob = viewModelScope.launch {
                playerState.collectLatest { state ->
                    if (state.currentSong?.videoId != currentId || (!state.isPlaying && state.positionMs >= state.durationMs - 1000 && state.durationMs > 0)) {
                        serviceConnection.playPause()
                        cancelSleepTimer()
                    }
                }
            }
        } else if (minutes > 0) {
            _uiState.value = _uiState.value.copy(
                sleepTimerActive = true,
                sleepTimerRemaining = minutes,
                toastMessage = "Sleep timer set for $minutes minutes"
            )
            sleepTimerJob = viewModelScope.launch {
                var remaining = minutes
                while (remaining > 0) {
                    kotlinx.coroutines.delay(60_000L)
                    remaining--
                    _uiState.value = _uiState.value.copy(sleepTimerRemaining = remaining)
                }
                serviceConnection.playPause()
                cancelSleepTimer()
            }
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _uiState.value = _uiState.value.copy(
            sleepTimerActive = false,
            sleepTimerRemaining = 0,
            toastMessage = "Sleep timer cancelled"
        )
    }

    fun playPause() = serviceConnection.playPause()
    fun seekTo(positionMs: Long) = serviceConnection.seekTo(positionMs)
    fun next() = serviceConnection.next()
    fun previous() = serviceConnection.previous()
    fun toggleShuffle() = serviceConnection.toggleShuffle()
    fun cycleRepeatMode() = serviceConnection.cycleRepeatMode()
    fun setVolume(volume: Float) = serviceConnection.setVolume(volume)
    fun setSpeed(speed: Float) = serviceConnection.setSpeed(speed)
}
