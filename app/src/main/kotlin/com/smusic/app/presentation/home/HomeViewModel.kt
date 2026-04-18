package com.smusic.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smusic.app.data.repository.MusicRepository
import com.smusic.app.domain.model.Artist
import com.smusic.app.domain.model.Song
import com.smusic.app.domain.usecase.GetRecentlyPlayedUseCase
import com.smusic.app.domain.usecase.GetTrendingUseCase
import com.smusic.app.player.SMusicServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val trendingSongs: List<Song> = emptyList(),
    val recentlyPlayed: List<Song> = emptyList(),
    val topArtists: List<Artist> = emptyList(),
    val basedOnTaste: List<Song> = emptyList(),
    val selectedLanguage: String = "All",
    val selectedMood: String? = null,
    val moodSongs: List<Song> = emptyList(),
    val greeting: String = "Good Morning, Music Fan 🎵",
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTrendingUseCase: GetTrendingUseCase,
    private val getRecentlyPlayedUseCase: GetRecentlyPlayedUseCase,
    private val musicRepository: MusicRepository,
    val serviceConnection: SMusicServiceConnection,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        updateGreeting()
        loadHome()
        observeRecentlyPlayed()
    }

    private fun updateGreeting() {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Good Morning, Music Fan 🎵"
            hour < 17 -> "Good Afternoon, Music Fan 🎵"
            else -> "Good Evening, Music Fan 🎵"
        }
        _uiState.value = _uiState.value.copy(greeting = greeting)
    }

    fun loadHome() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            getTrendingUseCase().onSuccess { songs ->
                // Extract unique artists from trending
                val artists = songs
                    .filter { it.artistId.isNotBlank() }
                    .distinctBy { it.artistId }
                    .take(10)
                    .map { Artist(artistId = it.artistId, name = it.artist, thumbnailUrl = it.thumbnailUrl) }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    trendingSongs = songs.take(20),
                    topArtists = artists,
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Failed to load trending",
                )
            }

            // Load personalized based on recent artists
            loadBasedOnTaste()
        }
    }

    private fun observeRecentlyPlayed() {
        viewModelScope.launch {
            getRecentlyPlayedUseCase(20).collect { songs ->
                _uiState.value = _uiState.value.copy(recentlyPlayed = songs)
            }
        }
    }

    private suspend fun loadBasedOnTaste() {
        val recentArtists = musicRepository.getRecentArtistNames(3)
        if (recentArtists.isNotEmpty()) {
            val query = recentArtists.joinToString(" ") + " songs"
            musicRepository.searchSongs(query).onSuccess { songs ->
                _uiState.value = _uiState.value.copy(basedOnTaste = songs.take(10))
            }
        }
    }

    fun onLanguageSelected(language: String) {
        _uiState.value = _uiState.value.copy(selectedLanguage = language)
        if (language != "All") {
            viewModelScope.launch {
                musicRepository.getSongsByLanguage(language).onSuccess { songs ->
                    _uiState.value = _uiState.value.copy(trendingSongs = songs.take(20))
                }
            }
        } else {
            loadHome()
        }
    }

    fun onMoodSelected(mood: String) {
        _uiState.value = _uiState.value.copy(selectedMood = mood)
        viewModelScope.launch {
            musicRepository.getSongsByMood(mood).onSuccess { songs ->
                _uiState.value = _uiState.value.copy(moodSongs = songs.take(15))
            }
        }
    }

    fun playSong(song: Song, songs: List<Song>, source: String) {
        serviceConnection.playSong(song, songs, source)
    }
}
