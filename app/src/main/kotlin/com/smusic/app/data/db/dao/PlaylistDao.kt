package com.smusic.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.smusic.app.data.db.entity.PlaylistEntity
import com.smusic.app.data.db.entity.PlaylistSongCrossRef
import com.smusic.app.data.db.entity.SongEntity
import kotlinx.coroutines.flow.Flow

data class PlaylistWithCount(
    val playlistId: Long,
    val name: String,
    val thumbnailUrl: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean,
    val sortOrder: Int,
    val songCount: Int,
)

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Query("SELECT * FROM playlists ORDER BY isPinned DESC, sortOrder ASC, createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("""
        SELECT 
            p.playlistId,
            p.name,
            COALESCE(
                NULLIF(p.thumbnailUrl, ''),
                (SELECT s.thumbnailUrl FROM songs s INNER JOIN playlist_song_cross_ref ref ON s.videoId = ref.videoId WHERE ref.playlistId = p.playlistId ORDER BY ref.addedAt DESC LIMIT 1),
                ''
            ) AS thumbnailUrl,
            p.createdAt,
            p.updatedAt,
            p.isPinned,
            p.sortOrder,
            (SELECT COUNT(*) FROM playlist_song_cross_ref ref WHERE ref.playlistId = p.playlistId) AS songCount
        FROM playlists p
        ORDER BY p.isPinned DESC, p.sortOrder ASC, p.createdAt DESC
    """)
    fun getAllPlaylistsWithCount(): Flow<List<PlaylistWithCount>>

    @Query("SELECT * FROM playlists WHERE playlistId = :playlistId")
    suspend fun getPlaylistById(playlistId: Long): PlaylistEntity?

    @Query("SELECT * FROM playlists WHERE playlistId = :playlistId")
    fun getPlaylistByIdFlow(playlistId: Long): Flow<PlaylistEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSongToPlaylist(crossRef: PlaylistSongCrossRef)

    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND videoId = :videoId")
    suspend fun removeSongFromPlaylist(playlistId: Long, videoId: String)

    @Transaction
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN playlist_song_cross_ref ref ON s.videoId = ref.videoId
        WHERE ref.playlistId = :playlistId
        ORDER BY ref.sortOrder ASC, ref.addedAt DESC
    """)
    fun getPlaylistSongs(playlistId: Long): Flow<List<SongEntity>>

    @Query("SELECT COUNT(*) FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    fun getPlaylistSongCount(playlistId: Long): Flow<Int>

    @Query("UPDATE playlists SET isPinned = :pinned WHERE playlistId = :playlistId")
    suspend fun updatePinnedStatus(playlistId: Long, pinned: Boolean)

    @Query("UPDATE playlists SET name = :name, updatedAt = :updatedAt WHERE playlistId = :playlistId")
    suspend fun renamePlaylist(playlistId: Long, name: String, updatedAt: Long = System.currentTimeMillis())
}
