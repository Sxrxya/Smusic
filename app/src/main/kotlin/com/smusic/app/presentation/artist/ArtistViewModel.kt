package com.smusic.app.presentation.artist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smusic.app.data.repository.MusicRepository
import com.smusic.app.domain.model.Artist
import com.smusic.app.domain.model.Song
import com.smusic.app.player.SMusicServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArtistUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val artist: Artist? = null,
    val songs: List<Song> = emptyList(),
)

@HiltViewModel
class ArtistViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    val serviceConnection: SMusicServiceConnection,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArtistUiState())
    val uiState: StateFlow<ArtistUiState> = _uiState.asStateFlow()

    fun loadArtist(artistId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            musicRepository.getArtistDetails(artistId).onSuccess { artist ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    artist = artist,
                    songs = artist.topSongs,
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message,
                )
            }
        }
    }

    fun playSong(song: Song, source: String) {
        serviceConnection.playSong(song, _uiState.value.songs, source)
    }
}
