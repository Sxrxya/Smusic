package com.smusic.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smusic.app.data.db.entity.AlbumEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: AlbumEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<AlbumEntity>)

    @Query("SELECT * FROM albums WHERE albumId = :albumId")
    suspend fun getAlbumById(albumId: String): AlbumEntity?

    @Query("SELECT * FROM albums WHERE albumId = :albumId")
    fun getAlbumByIdFlow(albumId: String): Flow<AlbumEntity?>

    @Query("SELECT * FROM albums ORDER BY addedAt DESC")
    fun getAllAlbums(): Flow<List<AlbumEntity>>

    @Query("DELETE FROM albums WHERE albumId = :albumId")
    suspend fun deleteAlbum(albumId: String)
}
