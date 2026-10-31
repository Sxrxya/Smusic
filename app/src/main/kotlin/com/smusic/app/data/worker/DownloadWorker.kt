package com.smusic.app.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.smusic.app.data.repository.DownloadRepository
import com.smusic.app.domain.model.DownloadState
import com.smusic.app.domain.usecase.GetStreamUrlUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import java.io.File
import androidx.core.app.NotificationCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import com.smusic.app.R

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val getStreamUrlUseCase: GetStreamUrlUseCase,
    private val downloadRepository: DownloadRepository,
    private val httpClient: HttpClient,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val videoId = inputData.getString("videoId") ?: return Result.failure()
        val title = inputData.getString("title") ?: "Unknown"

        setForeground(createForegroundInfo(title))

        downloadRepository.updateDownloadProgress(videoId, DownloadState.DOWNLOADING, 0)

        return try {
            val artist = inputData.getString("artist")
            val streamResult = getStreamUrlUseCase(videoId, title, artist)
            if (streamResult.isFailure) return Result.retry()

            val (url, bitrate) = streamResult.getOrThrow()
            val file = File(context.filesDir, "downloads/$videoId.m4a")
            file.parentFile?.mkdirs()

            val response = httpClient.get(url)
            val channel = response.bodyAsChannel()
            
            // Note: Simplistic download for now, Ktor 2.x doesn't have direct progress in copyAndClose easily without wrapping
            channel.copyAndClose(file.writeChannel())

            downloadRepository.completeDownload(
                videoId = videoId,
                filePath = file.absolutePath,
                fileSize = file.length(),
                bitrate = bitrate
            )

            Result.success()
        } catch (e: Exception) {
            downloadRepository.updateDownloadProgress(videoId, DownloadState.FAILED, 0)
            Result.failure()
        }
    }

    private fun createForegroundInfo(title: String): ForegroundInfo {
        val id = "download_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(id, "Downloads", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, id)
            .setContentTitle("Downloading $title")
            .setSmallIcon(R.drawable.ic_download) // Ensure this exists or use a system icon
            .setOngoing(true)
            .build()

        return ForegroundInfo(1001, notification)
    }
}
