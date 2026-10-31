package com.smusic.app.data.api

import android.util.Log
import com.smusic.app.domain.model.Song
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.http.HttpMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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

        // 1. Android VR Client (best for direct unencrypted streams)
        val vrResult = resolveWithClient(
            videoId,
            "ANDROID_VR",
            "Mozilla/5.0 (Linux; Android 12; Quest 3 Build/SQ3A.220605.009.A1) AppleWebKit/537.36 (KHTML, like Gecko) OculusBrowser/32.0.0.0.0 Safari/537.36"
        )
        if (vrResult.isSuccess) return@withContext vrResult

        // 2. Android Creator Client
        val creatorResult = resolveWithClient(
            videoId,
            "ANDROID_CREATOR",
            "com.google.android.youtube/19.17.36 (Linux; U; Android 13; en_IN; Pixel 7 Build/TQ3A.230901.001)"
        )
        if (creatorResult.isSuccess) return@withContext creatorResult

        // 3. Android Music Client
        val androidMusicResult = resolveWithClient(
            videoId,
            "ANDROID_MUSIC",
            "com.google.android.apps.youtube.music/6.42.52 (Linux; U; Android 13; en_IN; Pixel 7 Build/TQ3A.230901.001)"
        )
        if (androidMusicResult.isSuccess) return@withContext androidMusicResult

        // 4. iOS Client
        val iosResult = resolveWithClient(
            videoId,
            "IOS",
            "com.google.ios.youtubemusic/6.42.52 (iPhone16,1; U; CPU iOS 17_5_1 like Mac OS X)"
        )
        if (iosResult.isSuccess) return@withContext iosResult

        // 5. Android Mobile Client
        val androidMobileResult = resolveWithClient(
            videoId,
            "ANDROID",
            "com.google.android.youtube/19.17.36 (Linux; U; Android 13; en_IN; Pixel 7 Build/TQ3A.230901.001)"
        )
        if (androidMobileResult.isSuccess) return@withContext androidMobileResult

        // 6. External Piped API Fallback (handles ciphered tracks seamlessly)
        val pipedResult = resolveWithPiped(videoId)
        if (pipedResult.isSuccess) return@withContext pipedResult

        Result.failure(Exception("All clients and fallbacks failed to resolve stream for $videoId"))
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
                Log.d(TAG, "Successfully resolved with $clientName (bitrate=$bitrate)")
                Result.success(result)
            } else {
                Log.w(TAG, "$clientName: No direct playable URL found in response")
                Result.failure(Exception("No playable URL from $clientName"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving with $clientName: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun resolveWithPiped(videoId: String): Result<Pair<String, Int>> {
        val pipedInstances = listOf(
            "https://api.piped.private.coffee",
            "https://pipedapi.kavin.rocks",
            "https://pipedapi.tokhmi.xyz"
        )
        for (instance in pipedInstances) {
            try {
                Log.d(TAG, "Attempting resolve with Piped: $instance/streams/$videoId")
                val responseText: String = client.request("$instance/streams/$videoId") {
                    method = HttpMethod.Get
                    header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                }.body()

                val jsonElement = Json.parseToJsonElement(responseText)
                val jsonObj = jsonElement.jsonObject
                val audioStreams = jsonObj["audioStreams"]?.jsonArray
                if (audioStreams != null && audioStreams.isNotEmpty()) {
                    val best = audioStreams.mapNotNull { it.jsonObject }.maxByOrNull {
                        it["bitrate"]?.jsonPrimitive?.intOrNull ?: 0
                    }
                    val url = best?.get("url")?.jsonPrimitive?.contentOrNull
                    val bitrate = best?.get("bitrate")?.jsonPrimitive?.intOrNull ?: 128000
                    if (!url.isNullOrBlank()) {
                        Log.d(TAG, "Successfully resolved stream via Piped ($instance)")
                        return Result.success(Pair(url, bitrate))
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Piped instance $instance failed for $videoId: ${e.message}")
            }
        }
        return Result.failure(Exception("All Piped instances failed for $videoId"))
    }
}
