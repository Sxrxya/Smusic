package com.smusic.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LanguageContext(
    val language: String,
    val genre: String = "",
    val mood: String = "",
    val confidence: Float = 1.0f,
    val source: String = "unknown"
) {
    companion object {
        val UNKNOWN = LanguageContext(language = "unknown", confidence = 0.0f)
    }
}
