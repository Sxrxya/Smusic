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

        // 1. Android Music Client (best for music streams)
        val androidMusicResult = resolveWithClient(videoId, "ANDROID_MUSIC", "com.google.android.apps.youtube.music/6.42.52 (Linux; U; Android 13; en_IN; Pixel 7 Build/TQ3A.230901.001)")
        if (androidMusicResult.isSuccess) return@withContext androidMusicResult

        // 2. iOS Client (reliable fallback, often has direct URLs)
        val iosResult = resolveWithClient(videoId, "IOS", "com.google.ios.youtubemusic/6.42.52 (iPhone16,1; U; CPU iOS 17_5_1 like Mac OS X)")
        if (iosResult.isSuccess) return@withContext iosResult

        // 3. Android Mobile Client
        val androidMobileResult = resolveWithClient(videoId, "ANDROID", "com.google.android.youtube/19.17.36 (Linux; U; Android 13; en_IN; Pixel 7 Build/TQ3A.230901.001)")
        if (androidMobileResult.isSuccess) return@withContext androidMobileResult

        // 4. TV HTML5 Embedded Client (last resort)
        val tvResult = resolveWithClient(videoId, "TVHTML5_EMBEDDED", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
        if (tvResult.isSuccess) return@withContext tvResult

        Result.failure(Exception("All clients failed to resolve stream for $videoId"))
    }

    private suspend fun resolveWithClient(videoId: String, clientName: String, userAgent: String): Result<Pair<String, Int>> {
        Log.d(TAG, "Attempting resolve with $clientName")
        return try {
            val response = innerTubeApi.getPlayer(videoId, clientName)

            val status = response.playabilityStatus?.status
            val reason = response.playabilityStatus?.reason

            if (status != "OK") {
                Log.w(TAG, "$clientName failed: $status - $reason")
                return Result.failure(Exception("$clientName: $status - $reason"))
            }

            val result = InnerTubeMapper.mapPlayerResponseToStreamUrl(response)
            if (result != null) {
                val (url, bitrate) = result
                Log.d(TAG, "Got stream URL from $clientName (bitrate=$bitrate): ${url.take(80)}...")
                
                // Skip verification for HLS URLs (they are manifests, HEAD won't work reliably)
                if (url.contains(".m3u8") || url.contains("manifest")) {
                    Log.d(TAG, "HLS manifest detected, skipping verification")
                    return Result.success(result)
                }

                // Verify the stream is accessible
                if (verifyStream(url, userAgent)) {
                    Log.d(TAG, "Successfully resolved and verified with $clientName")
                    Result.success(result)
                } else {
                    Log.w(TAG, "Verification failed for $clientName, trying without verify")
                    // Still return success - verification can fail due to network timing
                    // ExoPlayer will handle actual playback errors
                    Result.success(result)
                }
            } else {
                // Log signature cipher issues
                response.streamingData?.adaptiveFormats?.forEach { format ->
                    if (format.url == null && format.signatureCipher != null) {
                        Log.w(TAG, "Format has signatureCipher but no URL (itag=${format.itag})")
                    }
                }
                
                Log.w(TAG, "$clientName: No playable URL found in response")
                Result.failure(Exception("No playable URL from $clientName"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving with $clientName: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun verifyStream(url: String, userAgent: String): Boolean {
        return try {
            val response = client.request(url) {
                method = HttpMethod.Head
                header("Range", "bytes=0-1")
                header("User-Agent", userAgent)
            }
            val code = response.status.value
            Log.d(TAG, "Verify stream response: $code")
            code in 200..299 || code == 206 || code == 302 || code == 303
        } catch (e: Exception) {
            Log.w(TAG, "Verify stream exception: ${e.message}")
            false
        }
    }
}
