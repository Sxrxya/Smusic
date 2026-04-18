package com.smusic.app.presentation.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smusic.app.domain.model.Song
import com.smusic.app.domain.usecase.DownloadSongUseCase
import com.smusic.app.player.SMusicServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DownloadsUiState(
    val songs: List<Song> = emptyList(),
    val totalSize: Long = 0,
)

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val downloadSongUseCase: DownloadSongUseCase,
    val serviceConnection: SMusicServiceConnection,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadsUiState())
    val uiState: StateFlow<DownloadsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            downloadSongUseCase.getDownloadedSongs().collect { songs ->
                _uiState.value = _uiState.value.copy(songs = songs)
            }
        }
        viewModelScope.launch {
            downloadSongUseCase.getTotalSize().collect { size ->
                _uiState.value = _uiState.value.copy(totalSize = size)
            }
        }
    }

    fun deleteDownload(videoId: String) {
        viewModelScope.launch { downloadSongUseCase.deleteDownload(videoId) }
    }

    fun deleteAll() {
        viewModelScope.launch { downloadSongUseCase.deleteAllDownloads() }
    }

    fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024))
            else -> "%.2f GB".format(bytes / (1024.0 * 1024 * 1024))
        }
    }
}
