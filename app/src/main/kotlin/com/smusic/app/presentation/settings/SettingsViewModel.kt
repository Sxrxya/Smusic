package com.smusic.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smusic.app.data.datastore.UserPreferences
import com.smusic.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _prefs = MutableStateFlow(UserPreferences())
    val prefs: StateFlow<UserPreferences> = _prefs.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.userPreferences.collect { _prefs.value = it }
        }
    }

    fun updateStreamQuality(quality: String) = viewModelScope.launch { settingsRepository.updateStreamQuality(quality) }
    fun updateDownloadQuality(quality: String) = viewModelScope.launch { settingsRepository.updateDownloadQuality(quality) }
    fun updateCrossfade(seconds: Int) = viewModelScope.launch { settingsRepository.updateCrossfade(seconds) }
    fun updateGapless(enabled: Boolean) = viewModelScope.launch { settingsRepository.updateGaplessPlayback(enabled) }
    fun updateNormalization(enabled: Boolean) = viewModelScope.launch { settingsRepository.updateAudioNormalization(enabled) }
    fun updateMonoAudio(enabled: Boolean) = viewModelScope.launch { settingsRepository.updateMonoAudio(enabled) }
    fun updateAutoPlay(enabled: Boolean) = viewModelScope.launch { settingsRepository.updateAutoPlay(enabled) }
    fun updateShakeToSkip(enabled: Boolean) = viewModelScope.launch { settingsRepository.updateShakeToSkip(enabled) }
    fun updateHeadphonePause(enabled: Boolean) = viewModelScope.launch { settingsRepository.updateHeadphonePause(enabled) }
    fun updateBluetoothAutoPlay(enabled: Boolean) = viewModelScope.launch { settingsRepository.updateBluetoothAutoPlay(enabled) }
    fun updateDownloadWifiOnly(enabled: Boolean) = viewModelScope.launch { settingsRepository.updateDownloadWifiOnly(enabled) }
    fun updateAutoDownloadLiked(enabled: Boolean) = viewModelScope.launch { settingsRepository.updateAutoDownloadLiked(enabled) }
    fun updateTheme(theme: String) = viewModelScope.launch { settingsRepository.updateTheme(theme) }
    fun updateLyricsLockscreen(enabled: Boolean) = viewModelScope.launch { settingsRepository.updateLyricsLockscreen(enabled) }
}
