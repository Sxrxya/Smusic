package com.smusic.app.presentation.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smusic.app.data.repository.MusicRepository
import com.smusic.app.domain.model.Song
import com.smusic.app.player.SMusicServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlbumUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val albumTitle: String = "",
    val albumArtist: String = "",
    val thumbnailUrl: String = "",
    val songs: List<Song> = emptyList(),
)

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    val serviceConnection: SMusicServiceConnection,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlbumUiState())
    val uiState: StateFlow<AlbumUiState> = _uiState.asStateFlow()

    fun loadAlbum(albumId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            musicRepository.getAlbumSongs(albumId).onSuccess { songs ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    songs = songs,
                    albumTitle = songs.firstOrNull()?.album ?: "",
                    albumArtist = songs.firstOrNull()?.artist ?: "",
                    thumbnailUrl = songs.firstOrNull()?.thumbnailUrl ?: "",
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
            }
        }
    }

    fun playSong(song: Song, source: String) {
        serviceConnection.playSong(song, _uiState.value.songs, source)
    }
}
