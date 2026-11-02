package com.smusic.app.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.smusic.app.R
import com.smusic.app.data.repository.DownloadRepository
import com.smusic.app.domain.model.DownloadState
import com.smusic.app.domain.usecase.GetStreamUrlUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.readAvailable
import java.io.File

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
        val artist = inputData.getString("artist")

        try {
            setForeground(createForegroundInfo(title))
        } catch (e: Exception) {
            Log.w("DownloadWorker", "Unable to set foreground: ${e.message}")
        }

        downloadRepository.updateDownloadProgress(videoId, DownloadState.DOWNLOADING, 1)

        return try {
            val streamResult = getStreamUrlUseCase(videoId, title, artist)
            if (streamResult.isFailure) {
                downloadRepository.updateDownloadProgress(videoId, DownloadState.FAILED, 0)
                return Result.retry()
            }

            val (url, bitrate) = streamResult.getOrThrow()
            val file = File(context.filesDir, "downloads/$videoId.m4a")
            file.parentFile?.mkdirs()

            val response = httpClient.get(url)
            val contentLength = response.headers[HttpHeaders.ContentLength]?.toLongOrNull() ?: -1L
            val channel = response.bodyAsChannel()

            val outputStream = file.outputStream()
            val buffer = ByteArray(16384)
            var totalBytesRead = 0L
            var lastReportedPercent = 1

            outputStream.use { output ->
                while (!channel.isClosedForRead) {
                    val bytesRead = channel.readAvailable(buffer, 0, buffer.size)
                    if (bytesRead <= 0) break
                    output.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead

                    if (contentLength > 0L) {
                        val progress = ((totalBytesRead * 100) / contentLength).toInt().coerceIn(1, 99)
                        if (progress >= lastReportedPercent + 2) {
                            lastReportedPercent = progress
                            downloadRepository.updateDownloadProgress(videoId, DownloadState.DOWNLOADING, progress)
                        }
                    }
                }
            }

            downloadRepository.completeDownload(
                videoId = videoId,
                filePath = file.absolutePath,
                fileSize = file.length(),
                bitrate = bitrate
            )

            Result.success()
        } catch (e: Exception) {
            Log.e("DownloadWorker", "Download failed for $videoId", e)
            downloadRepository.updateDownloadProgress(videoId, DownloadState.FAILED, 0)
            Result.failure()
        }
    }

    private fun createForegroundInfo(title: String): ForegroundInfo {
        val id = "download_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(id, "Downloads", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, id)
            .setContentTitle("Downloading $title")
            .setSmallIcon(R.drawable.ic_download)
            .setOngoing(true)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                1001,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(1001, notification)
        }
    }
}
