package com.smusic.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smusic.app.data.db.entity.DownloadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)

    @Update
    suspend fun updateDownload(download: DownloadEntity)

    @Query("SELECT * FROM downloads WHERE videoId = :videoId")
    suspend fun getDownloadById(videoId: String): DownloadEntity?

    @Query("SELECT * FROM downloads WHERE videoId = :videoId")
    fun getDownloadByIdFlow(videoId: String): Flow<DownloadEntity?>

    @Query("SELECT * FROM downloads WHERE state = 'DOWNLOADED' ORDER BY downloadedAt DESC")
    fun getDownloadedSongs(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE state = 'QUEUED' OR state = 'DOWNLOADING'")
    fun getPendingDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT SUM(fileSize) FROM downloads WHERE state = 'DOWNLOADED'")
    fun getTotalDownloadSize(): Flow<Long?>

    @Query("DELETE FROM downloads WHERE videoId = :videoId")
    suspend fun deleteDownload(videoId: String)

    @Query("DELETE FROM downloads WHERE state = 'DOWNLOADED'")
    suspend fun deleteAllDownloads()

    @Query("UPDATE downloads SET state = :state, progress = :progress WHERE videoId = :videoId")
    suspend fun updateDownloadProgress(videoId: String, state: String, progress: Int)
}
