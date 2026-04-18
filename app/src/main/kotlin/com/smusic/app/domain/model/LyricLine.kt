package com.smusic.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LyricLine(
    val timeMs: Long,
    val text: String,
)
