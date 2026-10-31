package com.smusic.app.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    indices = [
        Index(value = ["genre"]),
        Index(value = ["language"])
    ]
)
data class SongEntity(
    @PrimaryKey
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
    val genre: String = "",
    val mood: String = "",
    val energy: Float = 0.5f,
    val vocalType: String = "vocal",
    val isLiked: Boolean = false,
    val likedAt: Long? = null,
    val streamUrl: String = "",
    val bitrate: Int = 0,
    val addedAt: Long = System.currentTimeMillis(),
)
