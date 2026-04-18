package com.smusic.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey
    val albumId: String,
    val title: String,
    val artist: String,
    val artistId: String = "",
    val thumbnailUrl: String = "",
    val year: String = "",
    val songCount: Int = 0,
    val addedAt: Long = System.currentTimeMillis(),
)
