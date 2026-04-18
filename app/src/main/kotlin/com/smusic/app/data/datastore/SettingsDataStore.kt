package com.smusic.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "smusic_preferences")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private object Keys {
        val STREAM_QUALITY = stringPreferencesKey("stream_quality")
        val DOWNLOAD_QUALITY = stringPreferencesKey("download_quality")
        val CROSSFADE_SECONDS = intPreferencesKey("crossfade_seconds")
        val GAPLESS_PLAYBACK = booleanPreferencesKey("gapless_playback")
        val AUDIO_NORMALIZATION = booleanPreferencesKey("audio_normalization")
        val MONO_AUDIO = booleanPreferencesKey("mono_audio")
        val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        val AUTO_PLAY = booleanPreferencesKey("auto_play")
        val SHAKE_TO_SKIP = booleanPreferencesKey("shake_to_skip")
        val HEADPHONE_PAUSE = booleanPreferencesKey("headphone_pause")
        val BLUETOOTH_AUTOPLAY = booleanPreferencesKey("bluetooth_autoplay")
        val DOWNLOAD_WIFI_ONLY = booleanPreferencesKey("download_wifi_only")
        val AUTO_DOWNLOAD_LIKED = booleanPreferencesKey("auto_download_liked")
        val THEME = stringPreferencesKey("theme")
        val LYRICS_LOCKSCREEN = booleanPreferencesKey("lyrics_lockscreen")
        val SELECTED_LANGUAGES = stringPreferencesKey("selected_languages")
        val EQUALIZER_PRESET = stringPreferencesKey("equalizer_preset")
        val EQUALIZER_BANDS = stringPreferencesKey("equalizer_bands")
        val BASS_BOOST = intPreferencesKey("bass_boost")
        val VIRTUALIZER = intPreferencesKey("virtualizer")
        val LOUDNESS_ENHANCER = intPreferencesKey("loudness_enhancer")
        val CAR_MODE = booleanPreferencesKey("car_mode")
        val RECENT_SEARCHES = stringPreferencesKey("recent_searches")
        val BLACKLISTED_IDS = stringPreferencesKey("blacklisted_ids")
        val QUEUE_JSON = stringPreferencesKey("queue_json")
        val QUEUE_INDEX = intPreferencesKey("queue_index")
        val QUEUE_POSITION = intPreferencesKey("queue_position_ms")
    }

    val userPreferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            streamQuality = prefs[Keys.STREAM_QUALITY] ?: "HIGH",
            downloadQuality = prefs[Keys.DOWNLOAD_QUALITY] ?: "HIGH",
            crossfadeSeconds = prefs[Keys.CROSSFADE_SECONDS] ?: 0,
            gaplessPlayback = prefs[Keys.GAPLESS_PLAYBACK] ?: true,
            audioNormalization = prefs[Keys.AUDIO_NORMALIZATION] ?: false,
            monoAudio = prefs[Keys.MONO_AUDIO] ?: false,
            playbackSpeed = prefs[Keys.PLAYBACK_SPEED] ?: 1.0f,
            autoPlay = prefs[Keys.AUTO_PLAY] ?: true,
            shakeToSkip = prefs[Keys.SHAKE_TO_SKIP] ?: false,
            headphonePause = prefs[Keys.HEADPHONE_PAUSE] ?: true,
            bluetoothAutoPlay = prefs[Keys.BLUETOOTH_AUTOPLAY] ?: false,
            downloadWifiOnly = prefs[Keys.DOWNLOAD_WIFI_ONLY] ?: true,
            autoDownloadLiked = prefs[Keys.AUTO_DOWNLOAD_LIKED] ?: false,
            theme = prefs[Keys.THEME] ?: "DARK",
            lyricsLockscreen = prefs[Keys.LYRICS_LOCKSCREEN] ?: true,
            selectedLanguages = prefs[Keys.SELECTED_LANGUAGES]?.let {
                json.decodeFromString<List<String>>(it)
            } ?: listOf("All"),
            equalizerPreset = prefs[Keys.EQUALIZER_PRESET] ?: "Flat",
            equalizerBands = prefs[Keys.EQUALIZER_BANDS]?.let {
                json.decodeFromString<List<Int>>(it)
            } ?: List(10) { 0 },
            bassBoost = prefs[Keys.BASS_BOOST] ?: 0,
            virtualizer = prefs[Keys.VIRTUALIZER] ?: 0,
            loudnessEnhancer = prefs[Keys.LOUDNESS_ENHANCER] ?: 0,
            carMode = prefs[Keys.CAR_MODE] ?: false,
            recentSearches = prefs[Keys.RECENT_SEARCHES]?.let {
                json.decodeFromString<List<String>>(it)
            } ?: emptyList(),
            blacklistedVideoIds = prefs[Keys.BLACKLISTED_IDS]?.let {
                json.decodeFromString<List<String>>(it)
            } ?: emptyList(),
        )
    }

    suspend fun updateStreamQuality(quality: String) {
        context.dataStore.edit { it[Keys.STREAM_QUALITY] = quality }
    }

    suspend fun updateDownloadQuality(quality: String) {
        context.dataStore.edit { it[Keys.DOWNLOAD_QUALITY] = quality }
    }

    suspend fun updateCrossfade(seconds: Int) {
        context.dataStore.edit { it[Keys.CROSSFADE_SECONDS] = seconds }
    }

    suspend fun updateGaplessPlayback(enabled: Boolean) {
        context.dataStore.edit { it[Keys.GAPLESS_PLAYBACK] = enabled }
    }

    suspend fun updateAudioNormalization(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUDIO_NORMALIZATION] = enabled }
    }

    suspend fun updateMonoAudio(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MONO_AUDIO] = enabled }
    }

    suspend fun updatePlaybackSpeed(speed: Float) {
        context.dataStore.edit { it[Keys.PLAYBACK_SPEED] = speed }
    }

    suspend fun updateAutoPlay(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_PLAY] = enabled }
    }

    suspend fun updateShakeToSkip(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SHAKE_TO_SKIP] = enabled }
    }

    suspend fun updateHeadphonePause(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HEADPHONE_PAUSE] = enabled }
    }

    suspend fun updateBluetoothAutoPlay(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BLUETOOTH_AUTOPLAY] = enabled }
    }

    suspend fun updateDownloadWifiOnly(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DOWNLOAD_WIFI_ONLY] = enabled }
    }

    suspend fun updateAutoDownloadLiked(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_DOWNLOAD_LIKED] = enabled }
    }

    suspend fun updateTheme(theme: String) {
        context.dataStore.edit { it[Keys.THEME] = theme }
    }

    suspend fun updateLyricsLockscreen(enabled: Boolean) {
        context.dataStore.edit { it[Keys.LYRICS_LOCKSCREEN] = enabled }
    }

    suspend fun updateSelectedLanguages(languages: List<String>) {
        context.dataStore.edit { it[Keys.SELECTED_LANGUAGES] = json.encodeToString(languages) }
    }

    suspend fun updateEqualizerPreset(preset: String) {
        context.dataStore.edit { it[Keys.EQUALIZER_PRESET] = preset }
    }

    suspend fun updateEqualizerBands(bands: List<Int>) {
        context.dataStore.edit { it[Keys.EQUALIZER_BANDS] = json.encodeToString(bands) }
    }

    suspend fun updateBassBoost(level: Int) {
        context.dataStore.edit { it[Keys.BASS_BOOST] = level }
    }

    suspend fun updateVirtualizer(level: Int) {
        context.dataStore.edit { it[Keys.VIRTUALIZER] = level }
    }

    suspend fun updateLoudnessEnhancer(level: Int) {
        context.dataStore.edit { it[Keys.LOUDNESS_ENHANCER] = level }
    }

    suspend fun updateCarMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.CAR_MODE] = enabled }
    }

    suspend fun updateRecentSearches(searches: List<String>) {
        context.dataStore.edit { it[Keys.RECENT_SEARCHES] = json.encodeToString(searches) }
    }

    suspend fun addRecentSearch(query: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.RECENT_SEARCHES]?.let {
                json.decodeFromString<List<String>>(it)
            } ?: emptyList()
            val updated = (listOf(query) + current.filter { it != query }).take(20)
            prefs[Keys.RECENT_SEARCHES] = json.encodeToString(updated)
        }
    }

    suspend fun removeRecentSearch(query: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.RECENT_SEARCHES]?.let {
                json.decodeFromString<List<String>>(it)
            } ?: emptyList()
            prefs[Keys.RECENT_SEARCHES] = json.encodeToString(current.filter { it != query })
        }
    }

    suspend fun addBlacklistedId(videoId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.BLACKLISTED_IDS]?.let {
                json.decodeFromString<List<String>>(it)
            } ?: emptyList()
            prefs[Keys.BLACKLISTED_IDS] = json.encodeToString(current + videoId)
        }
    }

    suspend fun removeBlacklistedId(videoId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.BLACKLISTED_IDS]?.let {
                json.decodeFromString<List<String>>(it)
            } ?: emptyList()
            prefs[Keys.BLACKLISTED_IDS] = json.encodeToString(current.filter { it != videoId })
        }
    }

    suspend fun saveQueueState(queueJson: String, index: Int, positionMs: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.QUEUE_JSON] = queueJson
            prefs[Keys.QUEUE_INDEX] = index
            prefs[Keys.QUEUE_POSITION] = positionMs
        }
    }

    val savedQueueState: Flow<Triple<String, Int, Int>> = context.dataStore.data.map { prefs ->
        Triple(
            prefs[Keys.QUEUE_JSON] ?: "",
            prefs[Keys.QUEUE_INDEX] ?: 0,
            prefs[Keys.QUEUE_POSITION] ?: 0,
        )
    }
}
