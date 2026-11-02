package com.smusic.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smusic.app.data.repository.MusicRepository
import com.smusic.app.domain.engine.LanguageResolver
import com.smusic.app.domain.engine.RecommendationEngine
import com.smusic.app.domain.model.Artist
import com.smusic.app.domain.model.Song
import com.smusic.app.domain.usecase.GetRecentlyPlayedUseCase
import com.smusic.app.domain.usecase.GetTrendingUseCase
import com.smusic.app.player.SMusicServiceConnection
import com.smusic.app.presentation.components.formatNetworkError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class YouTubePlaylistItem(
    val title: String,
    val subtitle: String,
    val query: String,
    val gradientColors: List<Long>,
)

val defaultFeaturedPlaylists = listOf(
    YouTubePlaylistItem("Trending Tamil", "Top hits in Kollywood", "latest trending tamil songs 2024", listOf(0xFFFA233B, 0xFFFF5E3A)),
    YouTubePlaylistItem("Top Hits Hindi", "Bollywood chartbusters", "top trending hindi bollywood songs 2024", listOf(0xFF8E2DE2, 0xFF4A00E0)),
    YouTubePlaylistItem("Global Top 50", "Most played worldwide", "global top 50 hits 2024", listOf(0xFF00C9FF, 0xFF92FE9D)),
    YouTubePlaylistItem("Punjabi 101", "Bhangra & Hip-Hop", "trending punjabi hits 2024", listOf(0xFFFF416C, 0xFFFF4B2B)),
    YouTubePlaylistItem("Telugu Superhits", "Tollywood blockbusters", "latest trending telugu songs 2024", listOf(0xFFF7971E, 0xFFFFD200))
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val trendingSongs: List<Song> = emptyList(),
    val recentlyPlayed: List<Song> = emptyList(),
    val topArtists: List<Artist> = emptyList(),
    val basedOnTaste: List<Song> = emptyList(),
    val featuredPlaylists: List<YouTubePlaylistItem> = defaultFeaturedPlaylists,
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
    private val recommendationEngine: RecommendationEngine,
    private val languageResolver: LanguageResolver,
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
                    error = formatNetworkError(error.message),
                )
            }

            // Load personalized based on recent history & taste
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
        val recentlyPlayed = _uiState.value.recentlyPlayed
        if (recentlyPlayed.isNotEmpty()) {
            val lastSong = recentlyPlayed.first()
            val context = languageResolver.resolveFromSong(lastSong)
            musicRepository.getRelatedSongs(lastSong.videoId).onSuccess { related ->
                if (related.isNotEmpty()) {
                    val ranked = recommendationEngine.rankCandidates(
                        seed = lastSong,
                        context = context,
                        candidates = related,
                        history = recentlyPlayed.map { it.videoId }
                    )
                    _uiState.value = _uiState.value.copy(basedOnTaste = ranked.take(15))
                    return
                }
            }
        }

        // Fallback to artist-based search
        val recentArtists = musicRepository.getRecentArtistNames(3)
        if (recentArtists.isNotEmpty()) {
            val query = recentArtists.joinToString(" ") + " songs"
            musicRepository.searchSongs(query).onSuccess { songs ->
                _uiState.value = _uiState.value.copy(basedOnTaste = songs.take(15))
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

    fun playYouTubePlaylist(item: YouTubePlaylistItem) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            musicRepository.getYouTubePlaylist(item.query).onSuccess { songs ->
                _uiState.value = _uiState.value.copy(isLoading = false)
                if (songs.isNotEmpty()) {
                    serviceConnection.playSong(songs.first(), songs, "yt_playlist")
                }
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = formatNetworkError(err.message)
                )
            }
        }
    }

    fun playSong(song: Song, songs: List<Song>, source: String) {
        serviceConnection.playSong(song, songs, source)
    }
}
