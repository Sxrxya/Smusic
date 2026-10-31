package com.smusic.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "play_events")
data class PlayEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val videoId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int,
    val listenTimeSeconds: Int,
    val wasSkipped: Boolean = false,
    val language: String,
    val genre: String,
    val artistId: String,
)
