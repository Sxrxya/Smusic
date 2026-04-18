package com.smusic.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Playlist(
    val playlistId: Long = 0,
    val name: String,
    val thumbnailUrl: String = "",
    val songCount: Int = 0,
    val songs: List<Song> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
)
