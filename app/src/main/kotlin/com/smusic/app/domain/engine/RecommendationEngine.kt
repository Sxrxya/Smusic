package com.smusic.app.domain.engine

import com.smusic.app.data.db.dao.RecommendationDao
import com.smusic.app.domain.model.LanguageContext
import com.smusic.app.domain.model.Song
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.exp

@Singleton
class RecommendationEngine @Inject constructor(
    private val recommendationDao: RecommendationDao
) {

    data class Weights(
        val longTermAffinity: Float = 0.3f,
        val contentSimilarity: Float = 0.4f,
        val recency: Float = 0.2f,
        val diversity: Float = 0.1f
    )

    private val weights = Weights()

    suspend fun rankCandidates(
        seed: Song,
        context: LanguageContext,
        candidates: List<Song>,
        history: List<String> = emptyList()
    ): List<Song> {
        val topLanguage = recommendationDao.getTopLanguage() ?: context.language
        
        return candidates
            .filter { canPassFilter(it, context, history) }
            .distinctBy { it.videoId }
            .map { candidate ->
                candidate to calculateScore(seed, context, candidate, topLanguage)
            }
            .sortedByDescending { it.second }
            .map { it.first }
    }

    private fun canPassFilter(
        candidate: Song,
        context: LanguageContext,
        history: List<String>
    ): Boolean {
        // Language Lock (Strict)
        if (context.language != "unknown" && candidate.language.isNotBlank()) {
            if (candidate.language.lowercase() != context.language.lowercase()) return false
        }
        
        // Blacklist / History check
        if (history.contains(candidate.videoId)) return false
        
        return true
    }

    private fun calculateScore(
        seed: Song,
        context: LanguageContext,
        candidate: Song,
        topLanguage: String?
    ): Float {
        var score = 0f

        // 1. Content Similarity
        if (seed.genre.isNotBlank() && seed.genre == candidate.genre) score += 0.5f * weights.contentSimilarity
        if (seed.artistId.isNotBlank() && seed.artistId == candidate.artistId) score += 0.3f * weights.contentSimilarity
        if (seed.albumId.isNotBlank() && seed.albumId == candidate.albumId) score += 0.4f * weights.contentSimilarity
        
        // 2. Language Match (Bonus)
        if (candidate.language == context.language) score += 0.2f
        if (candidate.language == topLanguage) score += 0.1f

        // 3. Metadata Match
        if (seed.title.contains(candidate.title, ignoreCase = true)) score -= 0.5f // Penalize near duplicates

        return score
    }

    /**
     * Exponential time decay for affinity
     */
    private fun calculateTimeDecay(timestamp: Long): Float {
        val now = System.currentTimeMillis()
        val days = (now - timestamp) / (1000 * 60 * 60 * 24)
        val halfLife = 30.0
        return exp(-days * 0.693 / halfLife).toFloat()
    }
}
