package com.smusic.app.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smusic.app.data.db.entity.SongStatsEntity
import com.smusic.app.domain.usecase.SongStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatsUiState(
    val totalListenTimeMs: Long = 0,
    val mostPlayedSong: SongStatsEntity? = null,
    val mostPlayedArtist: String? = null,
    val listeningStreak: Int = 0,
    val topSongs: List<SongStatsEntity> = emptyList(),
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val songStatsUseCase: SongStatsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            songStatsUseCase.getTotalListeningTime().collect { time ->
                _uiState.value = _uiState.value.copy(totalListenTimeMs = time ?: 0)
            }
        }
        viewModelScope.launch {
            songStatsUseCase.getTopSongsThisMonth().collect { songs ->
                _uiState.value = _uiState.value.copy(topSongs = songs)
            }
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                mostPlayedSong = songStatsUseCase.getMostPlayedSong(),
                mostPlayedArtist = songStatsUseCase.getMostPlayedArtist(),
                listeningStreak = songStatsUseCase.getListeningStreak(),
            )
        }
    }
}
