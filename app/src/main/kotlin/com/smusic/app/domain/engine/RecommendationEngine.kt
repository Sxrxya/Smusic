package com.smusic.app.domain.engine

import com.smusic.app.data.db.dao.RecommendationDao
import com.smusic.app.domain.model.LanguageContext
import com.smusic.app.domain.model.Song
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.exp

@Singleton
class RecommendationEngine @Inject constructor(
    private val recommendationDao: RecommendationDao,
    private val languageResolver: LanguageResolver
) {

    data class Weights(
        val longTermAffinity: Float = 0.3f,
        val contentSimilarity: Float = 0.45f,
        val recency: Float = 0.15f,
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
        val seedGenre = if (seed.genre.isNotBlank()) seed.genre else context.genre
        
        return candidates
            .filter { canPassFilter(it, context, history) }
            .distinctBy { it.videoId }
            .map { candidate ->
                candidate to calculateScore(seed, seedGenre, context, candidate, topLanguage)
            }
            .sortedByDescending { it.second }
            .map { it.first }
    }

    private fun canPassFilter(
        candidate: Song,
        context: LanguageContext,
        history: List<String>
    ): Boolean {
        // Blacklist / History check
        if (history.contains(candidate.videoId)) return false

        // Strict Language Lock
        if (context.language != "unknown" && context.language.isNotBlank()) {
            val candidateContext = languageResolver.resolveFromSong(candidate)
            if (candidateContext.language != "unknown" && candidateContext.language.isNotBlank()) {
                if (!candidateContext.language.equals(context.language, ignoreCase = true)) {
                    return false
                }
            }
        }
        
        return true
    }

    private fun calculateScore(
        seed: Song,
        seedGenre: String,
        context: LanguageContext,
        candidate: Song,
        topLanguage: String?
    ): Float {
        var score = 0f
        val candidateContext = languageResolver.resolveFromSong(candidate)

        // 1. Language Match (Core Spotify-style coherence)
        if (context.language != "unknown" && candidateContext.language.equals(context.language, ignoreCase = true)) {
            score += 0.45f
        } else if (topLanguage != null && candidateContext.language.equals(topLanguage, ignoreCase = true)) {
            score += 0.2f
        }

        // 2. Genre Match
        val candidateGenre = if (candidate.genre.isNotBlank()) candidate.genre else candidateContext.genre
        if (seedGenre.isNotBlank() && candidateGenre.equals(seedGenre, ignoreCase = true)) {
            score += 0.35f * weights.contentSimilarity
        }

        // 3. Artist Similarity / Match
        if (seed.artistId.isNotBlank() && seed.artistId == candidate.artistId) {
            score += 0.3f * weights.contentSimilarity
        } else if (seed.artist.isNotBlank() && candidate.artist.isNotBlank()) {
            if (seed.artist.contains(candidate.artist, ignoreCase = true) || 
                candidate.artist.contains(seed.artist, ignoreCase = true)) {
                score += 0.25f * weights.contentSimilarity
            }
        }

        if (seed.albumId.isNotBlank() && seed.albumId == candidate.albumId) {
            score += 0.2f * weights.contentSimilarity
        }

        // 4. Duplicate / Remix penalty: penalize exact same track re-uploads
        if (seed.title.contains(candidate.title, ignoreCase = true) && 
            seed.artist.contains(candidate.artist, ignoreCase = true)) {
            score -= 0.6f
        }

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
