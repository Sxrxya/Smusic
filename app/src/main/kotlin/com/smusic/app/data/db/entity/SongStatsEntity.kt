package com.smusic.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "song_stats")
data class SongStatsEntity(
    @PrimaryKey
    val videoId: String,
    val title: String = "",
    val artist: String = "",
    val thumbnailUrl: String = "",
    val playCount: Int = 0,
    val totalListenTimeMs: Long = 0,
    val lastPlayedAt: Long = 0,
    val firstPlayedAt: Long = System.currentTimeMillis(),
)
