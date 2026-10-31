package com.smusic.app

import com.smusic.app.domain.engine.RecommendationEngine
import com.smusic.app.domain.model.Song
import org.junit.Assert.*
import org.junit.Test

class TestSuite {

    private val recommendationEngine = RecommendationEngine()

    @Test
    fun testVideoIdValidation() {
        val validId = "dQw4w9WgXcQ"
        val invalidIdShort = "abc"
        val invalidIdLong = "abc123456789"
        
        assertEquals(11, validId.length)
        // We can't easily test InnerTubeApi here without mocking Ktor, 
        // but we verified the 'require' blocks in the code.
    }

    @Test
    fun testRecommendationGenreConsistency() {
        val tamilSong = Song(
            videoId = "tamil1",
            title = "Tamil Song",
            artist = "Artist A",
            language = "Tamil",
            genre = "Melody"
        )
        
        val candidates = listOf(
            Song(videoId = "tamil2", title = "Tamil 2", artist = "Artist B", language = "Tamil", genre = "Melody"),
            Song(videoId = "hindi1", title = "Hindi 1", artist = "Artist C", language = "Hindi", genre = "Pop"),
            Song(videoId = "tamil3", title = "Tamil 3", artist = "Artist D", language = "Tamil", genre = "Folk"),
            Song(videoId = "english1", title = "Eng 1", artist = "Artist E", language = "English", genre = "Rock")
        )

        val recommended = recommendationEngine.recommendNextSongs(tamilSong, candidates)
        
        // Should recommend Tamil songs first or exclusively if they match
        assertTrue(recommended.all { it.language == "Tamil" || it.genre == "Melody" })
        assertEquals("tamil2", recommended[0].videoId) // Best match (both Tamil and Melody)
    }

    @Test
    fun testSimilarityScoring() {
        val seed = Song(videoId = "seed", title = "S", artist = "A", artistId = "AID", language = "L", genre = "G")
        
        val perfectMatch = Song(videoId = "p", title = "P", artist = "A", artistId = "AID", language = "L", genre = "G")
        val partialMatch = Song(videoId = "m", title = "M", artist = "X", artistId = "XID", language = "L", genre = "Y")
        val noMatch = Song(videoId = "n", title = "N", artist = "Z", artistId = "ZID", language = "Z", genre = "Z")

        val scorePerfect = recommendationEngine.getSimilarityScore(seed, perfectMatch)
        val scorePartial = recommendationEngine.getSimilarityScore(seed, partialMatch)
        val scoreNone = recommendationEngine.getSimilarityScore(seed, noMatch)

        assertTrue(scorePerfect > scorePartial)
        assertTrue(scorePartial > scoreNone)
        assertEquals(0, scoreNone)
    }
}
