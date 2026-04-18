package com.smusic.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smusic.app.data.db.entity.SongStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongStats(stats: SongStatsEntity)

    @Query("SELECT * FROM song_stats WHERE videoId = :videoId")
    suspend fun getStatsForSong(videoId: String): SongStatsEntity?

    @Query("""
        UPDATE song_stats
        SET playCount = playCount + 1,
            totalListenTimeMs = totalListenTimeMs + :listenTimeMs,
            lastPlayedAt = :playedAt
        WHERE videoId = :videoId
    """)
    suspend fun incrementPlayCount(videoId: String, listenTimeMs: Long, playedAt: Long)

    @Query("SELECT * FROM song_stats ORDER BY playCount DESC LIMIT :limit")
    fun getMostPlayedSongs(limit: Int = 10): Flow<List<SongStatsEntity>>

    @Query("SELECT SUM(totalListenTimeMs) FROM song_stats")
    fun getTotalListeningTime(): Flow<Long?>

    @Query("SELECT artist, SUM(playCount) as totalPlays FROM song_stats GROUP BY artist ORDER BY totalPlays DESC LIMIT 1")
    suspend fun getMostPlayedArtist(): ArtistPlayCount?

    @Query("SELECT * FROM song_stats ORDER BY playCount DESC LIMIT 1")
    suspend fun getMostPlayedSong(): SongStatsEntity?

    @Query("SELECT COUNT(DISTINCT date(lastPlayedAt / 1000, 'unixepoch', 'localtime')) FROM song_stats WHERE lastPlayedAt >= :sinceMs")
    suspend fun getListeningDaysCount(sinceMs: Long): Int

    @Query("SELECT * FROM song_stats WHERE lastPlayedAt >= :sinceMs ORDER BY playCount DESC LIMIT :limit")
    fun getTopSongsSince(sinceMs: Long, limit: Int = 10): Flow<List<SongStatsEntity>>
}

data class ArtistPlayCount(
    val artist: String,
    val totalPlays: Int,
)
