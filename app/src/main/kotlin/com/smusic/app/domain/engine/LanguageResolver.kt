package com.smusic.app.domain.engine

import com.smusic.app.domain.model.LanguageContext
import com.smusic.app.domain.model.Song
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguageResolver @Inject constructor() {

    private val languagePatterns = mapOf(
        "Tamil" to Pattern.compile("[\\u0B80-\\u0BFF]+"),
        "Hindi" to Pattern.compile("[\\u0900-\\u097F]+"),
        "Telugu" to Pattern.compile("[\\u0C00-\\u0C7F]+"),
        "Malayalam" to Pattern.compile("[\\u0D00-\\u0D7F]+"),
        "Kannada" to Pattern.compile("[\\u0C80-\\u0CFF]+"),
        "Bengali" to Pattern.compile("[\\u0980-\\u09FF]+"),
        "Punjabi" to Pattern.compile("[\\u0A00-\\u0A7F]+"),
        "Marathi" to Pattern.compile("[\\u0900-\\u097F]+")
    )

    private val genreKeywords = mapOf(
        "Melody" to listOf("melody", "romantic", "love", "duet", "slow"),
        "Mass" to listOf("mass", "kuthu", "fast", "dance", "beat"),
        "Devotional" to listOf("devotional", "bhajan", "god", "spiritual", "mantra"),
        "Classical" to listOf("classical", "carnatic", "hindustani"),
        "Folk" to listOf("folk", "gaana", "village")
    )

    fun resolveFromQuery(query: String): LanguageContext {
        val q = query.lowercase()
        var resolvedLang = "unknown"
        var resolvedGenre = ""
        var confidence = 0.0f

        // 1. Language keyword match
        val languages = listOf("tamil", "hindi", "telugu", "malayalam", "kannada", "punjabi", "bengali", "marathi", "english")
        for (lang in languages) {
            if (q.contains(lang)) {
                resolvedLang = lang.replaceFirstChar { it.uppercase() }
                confidence = 0.9f
                break
            }
        }

        // 2. Genre keyword match
        for ((genre, keywords) in genreKeywords) {
            if (keywords.any { q.contains(it) }) {
                resolvedGenre = genre
                if (confidence < 0.5f) confidence = 0.7f
                break
            }
        }

        return if (resolvedLang != "unknown") {
            LanguageContext(resolvedLang, resolvedGenre, confidence = confidence, source = "query")
        } else {
            LanguageContext.UNKNOWN
        }
    }

    fun resolveFromSong(song: Song): LanguageContext {
        if (song.language.isNotBlank() && song.language != "unknown") {
            return LanguageContext(song.language, song.genre, confidence = 1.0f, source = "metadata")
        }

        // Script detection
        val combinedText = "${song.title} ${song.artist}"
        for ((lang, pattern) in languagePatterns) {
            if (pattern.matcher(combinedText).find()) {
                return LanguageContext(lang, song.genre, confidence = 0.95f, source = "script")
            }
        }

        // Keyword hints for Romanized titles (basic)
        val lowerText = combinedText.lowercase()
        if (lowerText.contains("song") || lowerText.contains("music")) {
             // weak signal, maybe check common words like 'kaadhal' (Tamil), 'pyaar' (Hindi)
        }

        return LanguageContext.UNKNOWN
    }
}
