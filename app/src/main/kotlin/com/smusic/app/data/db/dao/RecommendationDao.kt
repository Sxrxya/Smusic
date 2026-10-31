package com.smusic.app.data.db.dao

import androidx.room.*
import com.smusic.app.data.db.entity.ArtistLanguageEntity
import com.smusic.app.data.db.entity.PlayEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecommendationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayEvent(event: PlayEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtistLanguage(artistLanguage: ArtistLanguageEntity)

    @Query("SELECT * FROM artist_languages WHERE artistId = :artistId")
    suspend fun getArtistLanguage(artistId: String): ArtistLanguageEntity?

    @Query("UPDATE artist_languages SET playCount = playCount + 1, lastPlayedAt = :timestamp WHERE artistId = :artistId")
    suspend fun incrementArtistPlayCount(artistId: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT language FROM artist_languages GROUP BY language ORDER BY SUM(playCount) DESC LIMIT 1")
    suspend fun getTopLanguage(): String?

    @Query("SELECT videoId FROM play_events WHERE timestamp > :since ORDER BY timestamp DESC")
    suspend fun getRecentVideoIds(since: Long): List<String>
    
    @Query("SELECT * FROM play_events ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentPlayEvents(limit: Int): List<PlayEventEntity>
}
