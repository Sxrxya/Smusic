package com.smusic.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smusic.app.data.datastore.UserPreferences
import com.smusic.app.data.repository.DownloadRepository
import com.smusic.app.data.repository.SettingsRepository
import com.smusic.app.player.EqualizerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val equalizerManager: EqualizerManager,
    private val downloadRepository: DownloadRepository,
) : ViewModel() {

    private val _prefs = MutableStateFlow(UserPreferences())
    val prefs: StateFlow<UserPreferences> = _prefs.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.userPreferences.collect { userPrefs ->
                _prefs.value = userPrefs
            }
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
    fun updateStartupAnimation(enabled: Boolean) = viewModelScope.launch { settingsRepository.updateStartupAnimation(enabled) }
    fun updateStartupSound(enabled: Boolean) = viewModelScope.launch { settingsRepository.updateStartupSound(enabled) }

    // Equalizer operations
    fun applyEqualizerPreset(preset: String) = viewModelScope.launch {
        val presetBands = equalizerManager.getPresetBands(preset)
        equalizerManager.applyPreset(presetBands, _prefs.value.bassBoost, _prefs.value.virtualizer, _prefs.value.loudnessEnhancer)
        settingsRepository.updateEqualizerPreset(preset)
        settingsRepository.updateEqualizerBands(presetBands)
    }

    fun updateBandLevel(band: Int, level: Int) = viewModelScope.launch {
        equalizerManager.setBandLevel(band, level)
        val currentBands = _prefs.value.equalizerBands.toMutableList()
        if (band in currentBands.indices) {
            currentBands[band] = level
            settingsRepository.updateEqualizerBands(currentBands)
        }
    }

    fun updateBassBoost(level: Int) = viewModelScope.launch {
        equalizerManager.setBassBoostStrength(level)
        settingsRepository.updateBassBoost(level)
    }

    fun updateVirtualizer(level: Int) = viewModelScope.launch {
        equalizerManager.setVirtualizerStrength(level)
        settingsRepository.updateVirtualizer(level)
    }

    fun updateLoudnessEnhancer(level: Int) = viewModelScope.launch {
        equalizerManager.setLoudnessGain(level)
        settingsRepository.updateLoudnessEnhancer(level)
    }

    fun resetEqualizer() = viewModelScope.launch {
        equalizerManager.resetToFlat()
        val flatBands = List(10) { 0 }
        settingsRepository.updateEqualizerPreset("Flat")
        settingsRepository.updateEqualizerBands(flatBands)
        settingsRepository.updateBassBoost(0)
        settingsRepository.updateVirtualizer(0)
        settingsRepository.updateLoudnessEnhancer(0)
    }

    // Downloads cache
    fun clearAllDownloads() = viewModelScope.launch {
        downloadRepository.deleteAllDownloads()
    }
}
