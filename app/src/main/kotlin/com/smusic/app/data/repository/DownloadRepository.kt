package com.smusic.app.data.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.smusic.app.data.db.dao.DownloadDao
import com.smusic.app.data.db.entity.DownloadEntity
import com.smusic.app.data.worker.DownloadWorker
import com.smusic.app.domain.model.DownloadState
import com.smusic.app.domain.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadDao: DownloadDao,
    private val settingsRepository: SettingsRepository,
) {
    private val workManager = WorkManager.getInstance(context)

    fun getDownloadedSongs(): Flow<List<Song>> {
        return downloadDao.getDownloadedSongs().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getPendingDownloads(): Flow<List<DownloadEntity>> {
        return downloadDao.getPendingDownloads()
    }

    fun getTotalDownloadSize(): Flow<Long> {
        return downloadDao.getTotalDownloadSize().map { it ?: 0L }
    }

    suspend fun queueDownload(song: Song) = withContext(Dispatchers.IO) {
        val entity = DownloadEntity(
            videoId = song.videoId,
            title = song.title,
            artist = song.artist,
            thumbnailUrl = song.thumbnailUrl,
            durationSeconds = song.durationSeconds,
            state = DownloadState.QUEUED.name,
        )
        downloadDao.insertDownload(entity)

        val prefs = settingsRepository.userPreferences.first()
        val networkType = if (prefs.downloadWifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(networkType)
            .build()

        val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(constraints)
            .setInputData(
                workDataOf(
                    "videoId" to song.videoId,
                    "title" to song.title,
                    "artist" to song.artist
                )
            )
            .addTag("download_${song.videoId}")
            .build()

        workManager.enqueue(downloadRequest)
    }

    suspend fun updateDownloadProgress(videoId: String, state: DownloadState, progress: Int) = withContext(Dispatchers.IO) {
        downloadDao.updateDownloadProgress(videoId, state.name, progress)
    }

    suspend fun completeDownload(videoId: String, filePath: String, fileSize: Long, bitrate: Int) = withContext(Dispatchers.IO) {
        val entity = downloadDao.getDownloadById(videoId) ?: return@withContext
        downloadDao.updateDownload(
            entity.copy(
                state = DownloadState.DOWNLOADED.name,
                filePath = filePath,
                fileSize = fileSize,
                bitrate = bitrate,
                progress = 100,
                downloadedAt = System.currentTimeMillis(),
            )
        )
    }

    suspend fun deleteDownload(videoId: String) = withContext(Dispatchers.IO) {
        downloadDao.deleteDownload(videoId)
    }

    suspend fun deleteAllDownloads() = withContext(Dispatchers.IO) {
        downloadDao.deleteAllDownloads()
    }

    suspend fun getDownloadState(videoId: String): DownloadState = withContext(Dispatchers.IO) {
        val entity = downloadDao.getDownloadById(videoId)
        if (entity != null) {
            try {
                DownloadState.valueOf(entity.state)
            } catch (e: Exception) {
                DownloadState.NOT_DOWNLOADED
            }
        } else {
            DownloadState.NOT_DOWNLOADED
        }
    }

    fun getDownloadByIdFlow(videoId: String): Flow<DownloadEntity?> {
        return downloadDao.getDownloadByIdFlow(videoId)
    }

    private fun DownloadEntity.toDomain() = Song(
        videoId = videoId,
        title = title,
        artist = artist,
        thumbnailUrl = thumbnailUrl,
        durationSeconds = durationSeconds,
        isDownloaded = state == DownloadState.DOWNLOADED.name,
        bitrate = bitrate,
    )
}
