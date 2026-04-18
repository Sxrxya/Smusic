package com.smusic.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smusic.app.data.db.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Update
    suspend fun updateSong(song: SongEntity)

    @Query("SELECT * FROM songs WHERE videoId = :videoId")
    suspend fun getSongById(videoId: String): SongEntity?

    @Query("SELECT * FROM songs WHERE videoId = :videoId")
    fun getSongByIdFlow(videoId: String): Flow<SongEntity?>

    @Query("SELECT * FROM songs WHERE isLiked = 1 ORDER BY likedAt DESC")
    fun getLikedSongs(): Flow<List<SongEntity>>

    @Query("SELECT COUNT(*) FROM songs WHERE isLiked = 1")
    fun getLikedSongsCount(): Flow<Int>

    @Query("UPDATE songs SET isLiked = :liked, likedAt = :likedAt WHERE videoId = :videoId")
    suspend fun updateLikeStatus(videoId: String, liked: Boolean, likedAt: Long?)

    @Query("SELECT isLiked FROM songs WHERE videoId = :videoId")
    suspend fun isLiked(videoId: String): Boolean?

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%'")
    fun searchSongs(query: String): Flow<List<SongEntity>>

    @Query("DELETE FROM songs WHERE videoId = :videoId")
    suspend fun deleteSong(videoId: String)

    @Query("SELECT * FROM songs ORDER BY addedAt DESC LIMIT :limit")
    fun getRecentSongs(limit: Int = 50): Flow<List<SongEntity>>
}
