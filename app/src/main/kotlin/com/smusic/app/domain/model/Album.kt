package com.smusic.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Album(
    val albumId: String,
    val title: String,
    val artist: String,
    val artistId: String = "",
    val thumbnailUrl: String = "",
    val year: String = "",
    val songCount: Int = 0,
    val songs: List<Song> = emptyList(),
)
