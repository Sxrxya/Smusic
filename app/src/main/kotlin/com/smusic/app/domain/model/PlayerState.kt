package com.smusic.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val bufferedPositionMs: Long = 0,
    val volume: Float = 1f,
    val playbackSpeed: Float = 1f,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val playingFrom: String = "",
) {
    val progress: Float
        get() = if (durationMs > 0) (positionMs.toFloat() / durationMs) else 0f

    val currentTimeFormatted: String
        get() = formatMs(positionMs)

    val totalTimeFormatted: String
        get() = formatMs(durationMs)

    private fun formatMs(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }
}

@Serializable
enum class RepeatMode {
    OFF,
    REPEAT_ALL,
    REPEAT_ONE,
}

@Serializable
enum class DownloadState {
    NOT_DOWNLOADED,
    QUEUED,
    DOWNLOADING,
    DOWNLOADED,
    FAILED,
}
