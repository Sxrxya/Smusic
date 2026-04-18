package com.smusic.app.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smusic.app.data.repository.MusicRepository
import com.smusic.app.data.repository.SettingsRepository
import com.smusic.app.domain.model.Song
import com.smusic.app.domain.usecase.SearchSongsUseCase
import com.smusic.app.player.SMusicServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val error: String? = null,
    val results: List<Song> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val categorySongs: List<Song> = emptyList(),
    val categoryName: String = "",
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchSongsUseCase: SearchSongsUseCase,
    private val musicRepository: MusicRepository,
    private val settingsRepository: SettingsRepository,
    val serviceConnection: SMusicServiceConnection,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadRecentSearches()
    }

    private fun loadRecentSearches() {
        viewModelScope.launch {
            val prefs = settingsRepository.userPreferences.first()
            _uiState.value = _uiState.value.copy(recentSearches = prefs.recentSearches)
        }
    }

    fun onQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        searchJob?.cancel()
        if (query.length >= 2) {
            searchJob = viewModelScope.launch {
                delay(300) // Debounce
                search(query)
            }
        } else {
            _uiState.value = _uiState.value.copy(results = emptyList(), isSearching = false)
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, error = null)
            settingsRepository.addRecentSearch(query)

            searchSongsUseCase(query).onSuccess { songs ->
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    results = songs,
                )
                loadRecentSearches()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    error = error.message ?: "Search failed",
                )
            }
        }
    }

    fun loadCategory(category: String) {
        _uiState.value = _uiState.value.copy(categoryName = category)
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true)
            musicRepository.getSongsByCategory(category).onSuccess { songs ->
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    categorySongs = songs,
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(isSearching = false)
            }
        }
    }

    fun removeRecentSearch(query: String) {
        viewModelScope.launch {
            settingsRepository.removeRecentSearch(query)
            loadRecentSearches()
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            settingsRepository.updateRecentSearches(emptyList())
            _uiState.value = _uiState.value.copy(recentSearches = emptyList())
        }
    }

    fun playSong(song: Song, songs: List<Song>, source: String) {
        serviceConnection.playSong(song, songs, source)
    }
}
