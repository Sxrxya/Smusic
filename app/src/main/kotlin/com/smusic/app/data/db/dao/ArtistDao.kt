package com.smusic.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smusic.app.data.db.entity.ArtistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtist(artist: ArtistEntity)

    @Update
    suspend fun updateArtist(artist: ArtistEntity)

    @Query("SELECT * FROM artists WHERE artistId = :artistId")
    suspend fun getArtistById(artistId: String): ArtistEntity?

    @Query("SELECT * FROM artists WHERE artistId = :artistId")
    fun getArtistByIdFlow(artistId: String): Flow<ArtistEntity?>

    @Query("SELECT * FROM artists WHERE isFollowed = 1 ORDER BY followedAt DESC")
    fun getFollowedArtists(): Flow<List<ArtistEntity>>

    @Query("UPDATE artists SET isFollowed = :followed, followedAt = :followedAt WHERE artistId = :artistId")
    suspend fun updateFollowStatus(artistId: String, followed: Boolean, followedAt: Long?)

    @Query("SELECT * FROM artists ORDER BY name ASC")
    fun getAllArtists(): Flow<List<ArtistEntity>>
}
