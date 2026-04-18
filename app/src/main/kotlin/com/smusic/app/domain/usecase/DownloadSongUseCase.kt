package com.smusic.app.domain.usecase

import com.smusic.app.data.repository.DownloadRepository
import com.smusic.app.domain.model.DownloadState
import com.smusic.app.domain.model.Song
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DownloadSongUseCase @Inject constructor(
    private val downloadRepository: DownloadRepository,
) {
    suspend fun queueDownload(song: Song) = downloadRepository.queueDownload(song)

    fun getDownloadedSongs(): Flow<List<Song>> = downloadRepository.getDownloadedSongs()

    fun getTotalSize(): Flow<Long> = downloadRepository.getTotalDownloadSize()

    suspend fun getState(videoId: String): DownloadState = downloadRepository.getDownloadState(videoId)

    suspend fun deleteDownload(videoId: String) = downloadRepository.deleteDownload(videoId)

    suspend fun deleteAllDownloads() = downloadRepository.deleteAllDownloads()
}
