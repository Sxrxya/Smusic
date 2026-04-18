package com.smusic.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smusic.app.data.db.entity.RecentlyPlayedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentlyPlayedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentlyPlayed(entry: RecentlyPlayedEntity)

    @Query("SELECT * FROM recently_played ORDER BY playedAt DESC LIMIT :limit")
    fun getRecentlyPlayed(limit: Int = 50): Flow<List<RecentlyPlayedEntity>>

    @Query("""
        SELECT * FROM recently_played
        WHERE playedAt >= :startOfDay AND playedAt < :endOfDay
        ORDER BY playedAt DESC
    """)
    fun getRecentlyPlayedForDay(startOfDay: Long, endOfDay: Long): Flow<List<RecentlyPlayedEntity>>

    @Query("DELETE FROM recently_played")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM recently_played")
    fun getCount(): Flow<Int>

    @Query("SELECT DISTINCT artist FROM recently_played ORDER BY playedAt DESC LIMIT :limit")
    suspend fun getRecentArtists(limit: Int = 10): List<String>
}
