package com.smusic.app.presentation.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smusic.app.domain.model.Playlist
import com.smusic.app.domain.model.Song
import com.smusic.app.domain.usecase.ManagePlaylistUseCase
import com.smusic.app.player.SMusicServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaylistUiState(
    val playlist: Playlist? = null,
    val songs: List<Song> = emptyList(),
    val songCount: Int = 0,
)

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val managePlaylistUseCase: ManagePlaylistUseCase,
    val serviceConnection: SMusicServiceConnection,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistUiState())
    val uiState: StateFlow<PlaylistUiState> = _uiState.asStateFlow()

    fun loadPlaylist(playlistId: Long) {
        viewModelScope.launch {
            managePlaylistUseCase.getPlaylistWithCount(playlistId).collect { (playlist, count) ->
                _uiState.value = _uiState.value.copy(playlist = playlist, songCount = count)
            }
        }
        viewModelScope.launch {
            managePlaylistUseCase.getPlaylistSongs(playlistId).collect { songs ->
                _uiState.value = _uiState.value.copy(songs = songs)
            }
        }
    }

    fun playSong(song: Song, source: String) {
        serviceConnection.playSong(song, _uiState.value.songs, source)
    }

    fun removeSong(playlistId: Long, videoId: String) {
        viewModelScope.launch { managePlaylistUseCase.removeSong(playlistId, videoId) }
    }
}
