package com.smusic.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class QueueItem(
    val song: Song,
    val addedAt: Long = System.currentTimeMillis(),
    val source: String = "",
)
