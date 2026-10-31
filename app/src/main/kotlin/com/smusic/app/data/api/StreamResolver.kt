package com.smusic.app.data.api

import android.util.Log
import com.smusic.app.data.db.dao.SongDao
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
import java.net.URLEncoder
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamResolver @Inject constructor(
    private val innerTubeApi: InnerTubeApi,
    private val songDao: SongDao
) {
    private val client = KtorClient.httpClient
    private val TAG = "SMusicPlay"

    suspend fun resolve(
        videoId: String,
        title: String? = null,
        artist: String? = null
    ): Result<Pair<String, Int>> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Resolving stream for $videoId (title=$title, artist=$artist)")

        // 1. Resolve song title & artist if not directly supplied
        var songTitle = title
        var songArtist = artist
        if (songTitle.isNullOrBlank()) {
            try {
                val cached = songDao.getSongById(videoId)
                if (cached != null) {
                    songTitle = cached.title
                    songArtist = cached.artist
                    Log.d(TAG, "Found song in database: $songTitle by $songArtist")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not check SongDao: ${e.message}")
            }
        }

        // 2. High-speed, unthrottled 320kbps Direct CDN Stream (bypasses YouTube bot-guard/poToken blocks)
        if (!songTitle.isNullOrBlank()) {
            val cdnResult = resolveWithDirectCdn(songTitle, songArtist)
            if (cdnResult.isSuccess) {
                return@withContext cdnResult
            }
        }

        // 3. Android VR Client (unencrypted direct streams for YouTube)
        val vrResult = resolveWithClient(
            videoId,
            "ANDROID_VR",
            "Mozilla/5.0 (Linux; Android 12; Quest 3 Build/SQ3A.220605.009.A1) AppleWebKit/537.36 (KHTML, like Gecko) OculusBrowser/32.0.0.0.0 Safari/537.36"
        )
        if (vrResult.isSuccess) return@withContext vrResult

        // 4. Android Creator Client
        val creatorResult = resolveWithClient(
            videoId,
            "ANDROID_CREATOR",
            "com.google.android.youtube/19.17.36 (Linux; U; Android 13; en_IN; Pixel 7 Build/TQ3A.230901.001)"
        )
        if (creatorResult.isSuccess) return@withContext creatorResult

        // 5. Android Music Client
        val androidMusicResult = resolveWithClient(
            videoId,
            "ANDROID_MUSIC",
            "com.google.android.apps.youtube.music/6.42.52 (Linux; U; Android 13; en_IN; Pixel 7 Build/TQ3A.230901.001)"
        )
        if (androidMusicResult.isSuccess) return@withContext androidMusicResult

        // 6. iOS Client
        val iosResult = resolveWithClient(
            videoId,
            "IOS",
            "com.google.ios.youtubemusic/6.42.52 (iPhone16,1; U; CPU iOS 17_5_1 like Mac OS X)"
        )
        if (iosResult.isSuccess) return@withContext iosResult

        // 7. Android Mobile Client
        val androidMobileResult = resolveWithClient(
            videoId,
            "ANDROID",
            "com.google.android.youtube/19.17.36 (Linux; U; Android 13; en_IN; Pixel 7 Build/TQ3A.230901.001)"
        )
        if (androidMobileResult.isSuccess) return@withContext androidMobileResult

        // 8. External Piped API Fallback
        val pipedResult = resolveWithPiped(videoId)
        if (pipedResult.isSuccess) return@withContext pipedResult

        Result.failure(Exception("All streaming sources failed to resolve stream for $videoId"))
    }

    private suspend fun resolveWithDirectCdn(title: String, artist: String?): Result<Pair<String, Int>> {
        val cleanTitle = cleanSongTitle(title)
        
        // Try with title + artist first
        if (!artist.isNullOrBlank()) {
            val result = querySaavnStream("$cleanTitle $artist")
            if (result != null) return Result.success(result)
        }

        // Try with cleaned title only
        val result = querySaavnStream(cleanTitle)
        if (result != null) return Result.success(result)

        return Result.failure(Exception("Direct CDN stream not found for: $title"))
    }

    private suspend fun querySaavnStream(query: String): Pair<String, Int>? {
        return try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val searchUrl = "https://www.jiosaavn.com/api.php?__call=autocomplete.get&_format=json&_marker=0&cc=in&includeMetaTags=1&query=$encodedQuery"

            val searchResponseText: String = client.request(searchUrl) {
                method = HttpMethod.Get
                header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            }.body()

            val searchJson = Json.parseToJsonElement(searchResponseText).jsonObject
            val songsArray = searchJson["songs"]?.jsonObject?.get("data")?.jsonArray
            if (songsArray.isNullOrEmpty()) return null

            val firstSong = songsArray[0].jsonObject
            val songId = firstSong["id"]?.jsonPrimitive?.contentOrNull ?: return null

            val detailUrl = "https://www.jiosaavn.com/api.php?__call=song.getDetails&cc=in&_marker=0%3F_marker%3D0&_format=json&pids=$songId"
            val detailResponseText: String = client.request(detailUrl) {
                method = HttpMethod.Get
                header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            }.body()

            val detailJson = Json.parseToJsonElement(detailResponseText).jsonObject
            val songDetails = detailJson[songId]?.jsonObject
            val encryptedMediaUrl = songDetails?.get("encrypted_media_url")?.jsonPrimitive?.contentOrNull ?: return null

            val decryptedUrl = decryptSaavnUrl(encryptedMediaUrl) ?: return null
            val streamUrl = decryptedUrl.replace("_96.mp4", "_320.mp4").replace("_160.mp4", "_320.mp4")
            
            Log.d(TAG, "Successfully resolved high-quality 320kbps CDN stream for: $query")
            Pair(streamUrl, 320000)
        } catch (e: Exception) {
            Log.w(TAG, "Saavn stream resolution error for $query: ${e.message}")
            null
        }
    }

    private fun decryptSaavnUrl(encryptedUrl: String): String? {
        return try {
            val keyBytes = "38346591".toByteArray(Charsets.UTF_8)
            val keySpec = SecretKeySpec(keyBytes, "DES")
            val cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            
            val decoded = try {
                android.util.Base64.decode(encryptedUrl.trim(), android.util.Base64.DEFAULT)
            } catch (e: Throwable) {
                java.util.Base64.getDecoder().decode(encryptedUrl.trim())
            }
            
            val decryptedBytes = cipher.doFinal(decoded)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "DES URL decryption failed: ${e.message}")
            null
        }
    }

    private fun cleanSongTitle(title: String): String {
        var clean = title.replace(Regex("(?i)[\\[(][^\\])]*(?:official|video|audio|lyrics?|remix|hd|4k|feat\\.?|ft\\.?)[^\\])]*[\\])]"), "")
        clean = clean.replace(Regex("\\s*\\|.*$"), "")
        return clean.trim().ifBlank { title }
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
