package com.smusic.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Artist(
    val artistId: String,
    val name: String,
    val thumbnailUrl: String = "",
    val subscriberCount: String = "",
    val description: String = "",
    val isFollowed: Boolean = false,
    val topSongs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
)
