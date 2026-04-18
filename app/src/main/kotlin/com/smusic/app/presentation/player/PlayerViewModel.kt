package com.smusic.app.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smusic.app.domain.model.LyricLine
import com.smusic.app.domain.model.PlayerState
import com.smusic.app.domain.model.Song
import com.smusic.app.domain.usecase.GetLyricsUseCase
import com.smusic.app.domain.usecase.LikeSongUseCase
import com.smusic.app.player.SMusicServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val lyrics: List<LyricLine> = emptyList(),
    val isLoadingLyrics: Boolean = false,
    val isLiked: Boolean = false,
    val showLyrics: Boolean = false,
    val showQueue: Boolean = false,
    val showSleepTimer: Boolean = false,
    val sleepTimerActive: Boolean = false,
    val sleepTimerRemaining: Int = 0,
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    val serviceConnection: SMusicServiceConnection,
    private val getLyricsUseCase: GetLyricsUseCase,
    private val likeSongUseCase: LikeSongUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    val playerState: StateFlow<PlayerState> = serviceConnection.playerState

    private var currentLyricsSongId: String? = null

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

    fun toggleLyricsSheet() {
        _uiState.value = _uiState.value.copy(showLyrics = !_uiState.value.showLyrics)
    }

    fun toggleQueueSheet() {
        _uiState.value = _uiState.value.copy(showQueue = !_uiState.value.showQueue)
    }

    fun toggleSleepTimer() {
        _uiState.value = _uiState.value.copy(showSleepTimer = !_uiState.value.showSleepTimer)
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
