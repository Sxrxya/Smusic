package com.smusic.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artists")
data class ArtistEntity(
    @PrimaryKey
    val artistId: String,
    val name: String,
    val thumbnailUrl: String = "",
    val subscriberCount: String = "",
    val description: String = "",
    val isFollowed: Boolean = false,
    val followedAt: Long? = null,
)
