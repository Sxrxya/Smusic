package com.smusic.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Song(
    val videoId: String,
    val title: String,
    val artist: String,
    val artistId: String = "",
    val album: String = "",
    val albumId: String = "",
    val thumbnailUrl: String = "",
    val durationSeconds: Int = 0,
    val year: String = "",
    val language: String = "",
    val isLiked: Boolean = false,
    val isDownloaded: Boolean = false,
    val streamUrl: String = "",
    val bitrate: Int = 0,
) {
    val durationFormatted: String
        get() {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }

    val highResThumbnail: String
        get() = thumbnailUrl.replace("w120-h120", "w544-h544")
            .replace("w226-h226", "w544-h544")
}
