package com.smusic.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artist_languages")
data class ArtistLanguageEntity(
    @PrimaryKey
    val artistId: String,
    val language: String,
    val playCount: Int = 1,
    val lastPlayedAt: Long = System.currentTimeMillis()
)
