package com.smusic.app.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smusic.app.data.repository.LibraryRepository
import com.smusic.app.domain.model.Album
import com.smusic.app.domain.model.Artist
import com.smusic.app.domain.model.Playlist
import com.smusic.app.domain.model.Song
import com.smusic.app.player.SMusicServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val selectedTab: Int = 0,
    val likedSongs: List<Song> = emptyList(),
    val likedSongsCount: Int = 0,
    val playlists: List<Playlist> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val history: List<Song> = emptyList(),
    val showCreatePlaylistDialog: Boolean = false,
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    val serviceConnection: SMusicServiceConnection,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        observeLibrary()
    }

    private fun observeLibrary() {
        viewModelScope.launch {
            libraryRepository.getLikedSongs().collect { songs ->
                _uiState.value = _uiState.value.copy(likedSongs = songs, likedSongsCount = songs.size)
            }
        }
        viewModelScope.launch {
            libraryRepository.getAllPlaylists().collect { playlists ->
                _uiState.value = _uiState.value.copy(playlists = playlists)
            }
        }
        viewModelScope.launch {
            libraryRepository.getFollowedArtists().collect { artists ->
                _uiState.value = _uiState.value.copy(artists = artists)
            }
        }
        viewModelScope.launch {
            libraryRepository.getSavedAlbums().collect { albums ->
                _uiState.value = _uiState.value.copy(albums = albums)
            }
        }
        viewModelScope.launch {
            libraryRepository.getHistory().collect { history ->
                _uiState.value = _uiState.value.copy(history = history)
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }

    fun toggleCreatePlaylist() {
        _uiState.value = _uiState.value.copy(
            showCreatePlaylistDialog = !_uiState.value.showCreatePlaylistDialog
        )
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            libraryRepository.createPlaylist(name)
            toggleCreatePlaylist()
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch { libraryRepository.deletePlaylist(playlistId) }
    }

    fun clearHistory() {
        viewModelScope.launch { libraryRepository.clearHistory() }
    }

    fun playSong(song: Song, songs: List<Song>, source: String) {
        serviceConnection.playSong(song, songs, source)
    }

    fun playAll(songs: List<Song>, source: String) {
        if (songs.isNotEmpty()) {
            serviceConnection.playSong(songs.first(), songs, source)
        }
    }

    fun shuffleAll(songs: List<Song>, source: String) {
        if (songs.isNotEmpty()) {
            val shuffled = songs.shuffled()
            serviceConnection.playSong(shuffled.first(), shuffled, source)
        }
    }
}
