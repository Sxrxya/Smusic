package com.smusic.app.data.repository

import com.smusic.app.data.datastore.SettingsDataStore
import com.smusic.app.data.datastore.UserPreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
) {

    val userPreferences: Flow<UserPreferences> = settingsDataStore.userPreferences

    val savedQueueState = settingsDataStore.savedQueueState

    suspend fun updateStreamQuality(quality: String) = settingsDataStore.updateStreamQuality(quality)
    suspend fun updateDownloadQuality(quality: String) = settingsDataStore.updateDownloadQuality(quality)
    suspend fun updateCrossfade(seconds: Int) = settingsDataStore.updateCrossfade(seconds)
    suspend fun updateGaplessPlayback(enabled: Boolean) = settingsDataStore.updateGaplessPlayback(enabled)
    suspend fun updateAudioNormalization(enabled: Boolean) = settingsDataStore.updateAudioNormalization(enabled)
    suspend fun updateMonoAudio(enabled: Boolean) = settingsDataStore.updateMonoAudio(enabled)
    suspend fun updatePlaybackSpeed(speed: Float) = settingsDataStore.updatePlaybackSpeed(speed)
    suspend fun updateAutoPlay(enabled: Boolean) = settingsDataStore.updateAutoPlay(enabled)
    suspend fun updateShakeToSkip(enabled: Boolean) = settingsDataStore.updateShakeToSkip(enabled)
    suspend fun updateHeadphonePause(enabled: Boolean) = settingsDataStore.updateHeadphonePause(enabled)
    suspend fun updateBluetoothAutoPlay(enabled: Boolean) = settingsDataStore.updateBluetoothAutoPlay(enabled)
    suspend fun updateDownloadWifiOnly(enabled: Boolean) = settingsDataStore.updateDownloadWifiOnly(enabled)
    suspend fun updateAutoDownloadLiked(enabled: Boolean) = settingsDataStore.updateAutoDownloadLiked(enabled)
    suspend fun updateTheme(theme: String) = settingsDataStore.updateTheme(theme)
    suspend fun updateLyricsLockscreen(enabled: Boolean) = settingsDataStore.updateLyricsLockscreen(enabled)
    suspend fun updateSelectedLanguages(languages: List<String>) = settingsDataStore.updateSelectedLanguages(languages)
    suspend fun updateEqualizerPreset(preset: String) = settingsDataStore.updateEqualizerPreset(preset)
    suspend fun updateEqualizerBands(bands: List<Int>) = settingsDataStore.updateEqualizerBands(bands)
    suspend fun updateBassBoost(level: Int) = settingsDataStore.updateBassBoost(level)
    suspend fun updateVirtualizer(level: Int) = settingsDataStore.updateVirtualizer(level)
    suspend fun updateLoudnessEnhancer(level: Int) = settingsDataStore.updateLoudnessEnhancer(level)
    suspend fun updateCarMode(enabled: Boolean) = settingsDataStore.updateCarMode(enabled)
    suspend fun addRecentSearch(query: String) = settingsDataStore.addRecentSearch(query)
    suspend fun removeRecentSearch(query: String) = settingsDataStore.removeRecentSearch(query)
    suspend fun updateRecentSearches(searches: List<String>) = settingsDataStore.updateRecentSearches(searches)
    suspend fun addBlacklistedId(videoId: String) = settingsDataStore.addBlacklistedId(videoId)
    suspend fun removeBlacklistedId(videoId: String) = settingsDataStore.removeBlacklistedId(videoId)
    suspend fun saveQueueState(queueJson: String, index: Int, positionMs: Int) =
        settingsDataStore.saveQueueState(queueJson, index, positionMs)
}
