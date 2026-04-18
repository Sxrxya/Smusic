package com.smusic.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey
    val videoId: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String = "",
    val durationSeconds: Int = 0,
    val filePath: String = "",
    val fileSize: Long = 0,
    val bitrate: Int = 0,
    val state: String = "NOT_DOWNLOADED",
    val progress: Int = 0,
    val downloadedAt: Long? = null,
    val queuedAt: Long = System.currentTimeMillis(),
)
