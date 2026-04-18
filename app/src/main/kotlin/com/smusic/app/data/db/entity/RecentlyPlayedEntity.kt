package com.smusic.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recently_played")
data class RecentlyPlayedEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val videoId: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String = "",
    val durationSeconds: Int = 0,
    val playedAt: Long = System.currentTimeMillis(),
)
