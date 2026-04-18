package com.smusic.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "playlist_song_cross_ref",
    primaryKeys = ["playlistId", "videoId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["playlistId"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["videoId"])],
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val videoId: String,
    val addedAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0,
)
