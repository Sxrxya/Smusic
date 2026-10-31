package com.smusic.app.data.api

import android.util.Log
import com.smusic.app.domain.model.Song
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.http.HttpMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamResolver @Inject constructor(
    private val innerTubeApi: InnerTubeApi
) {
    private val client = KtorClient.httpClient
    private val TAG = "SMusicPlay"

    suspend fun resolve(videoId: String): Result<Pair<String, Int>> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Resolving stream for $videoId")

        // 1. Android Music Client (Default)
        val androidMusicResult = resolveWithClient(videoId, "ANDROID_MUSIC")
        if (androidMusicResult.isSuccess) return@withContext androidMusicResult

        // 2. Web Remix Client
        val webResult = resolveWithClient(videoId, "WEB_REMIX")
        if (webResult.isSuccess) return@withContext webResult

        // 3. Android Mobile Client
        val androidMobileResult = resolveWithClient(videoId, "ANDROID")
        if (androidMobileResult.isSuccess) return@withContext androidMobileResult

        Result.failure(Exception("All clients failed to resolve stream"))
    }

    private suspend fun resolveWithClient(videoId: String, clientName: String): Result<Pair<String, Int>> {
        Log.d(TAG, "Attempting resolve with $clientName")
        return try {
            val response = when (clientName) {
                "ANDROID_MUSIC" -> innerTubeApi.getPlayer(videoId)
                "WEB_REMIX" -> innerTubeApi.getPlayer(videoId) // Future: support specific client context in getPlayer
                else -> innerTubeApi.getPlayer(videoId)
            }

            val result = InnerTubeMapper.mapPlayerResponseToStreamUrl(response)
            if (result != null) {
                Log.d(TAG, "Resolved stream URL: ${result.first}")
                if (verifyStream(result.first)) {
                    Log.d(TAG, "Successfully resolved and verified with $clientName")
                    Result.success(result)
                } else {
                    Log.w(TAG, "Verification failed for $clientName")
                    Result.failure(Exception("Stream verification failed"))
                }
            } else {
                val reason = response.playabilityStatus?.reason ?: "Unknown error"
                val status = response.playabilityStatus?.status ?: "Unknown status"
                Log.w(TAG, "$clientName failed: $status - $reason")
                
                // Log if it's a signatureCipher issue
                response.streamingData?.adaptiveFormats?.forEach { format ->
                    if (format.url == null && format.signatureCipher != null) {
                        Log.w(TAG, "Format has signatureCipher but no URL. Cipher: ${format.signatureCipher.take(100)}...")
                    }
                }

                Result.failure(Exception(reason))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving with $clientName", e)
            Result.failure(e)
        }
    }

    private suspend fun verifyStream(url: String): Boolean {
        return try {
            val response = client.request(url) {
                method = HttpMethod.Head
                header("Range", "bytes=0-1")
                header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            }
            response.status.value in 200..299 || response.status.value == 206
        } catch (e: Exception) {
            false
        }
    }
}
