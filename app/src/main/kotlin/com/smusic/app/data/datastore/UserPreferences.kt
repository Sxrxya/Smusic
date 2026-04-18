package com.smusic.app.data.datastore

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val streamQuality: String = "HIGH",
    val downloadQuality: String = "HIGH",
    val crossfadeSeconds: Int = 0,
    val gaplessPlayback: Boolean = true,
    val audioNormalization: Boolean = false,
    val monoAudio: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val autoPlay: Boolean = true,
    val shakeToSkip: Boolean = false,
    val headphonePause: Boolean = true,
    val bluetoothAutoPlay: Boolean = false,
    val downloadWifiOnly: Boolean = true,
    val autoDownloadLiked: Boolean = false,
    val theme: String = "DARK",
    val lyricsLockscreen: Boolean = true,
    val selectedLanguages: List<String> = listOf("All"),
    val equalizerPreset: String = "Flat",
    val equalizerBands: List<Int> = List(10) { 0 },
    val bassBoost: Int = 0,
    val virtualizer: Int = 0,
    val loudnessEnhancer: Int = 0,
    val carMode: Boolean = false,
    val sleepTimerMinutes: Int = 0,
    val recentSearches: List<String> = emptyList(),
    val blacklistedVideoIds: List<String> = emptyList(),
)
