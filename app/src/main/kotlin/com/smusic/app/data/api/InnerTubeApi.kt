package com.smusic.app.data.api

import android.util.Log
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InnerTubeApi @Inject constructor() {

    private val client = KtorClient.httpClient
    private val musicBaseUrl = "https://music.youtube.com/youtubei/v1"
    private val youtubeBaseUrl = "https://www.youtube.com/youtubei/v1"
    private val apiKey = "AIzaSyC9XL3ZjWddXya6X74dJoCTL-KLET5YdCE"

    private val webRemixContext = InnerTubeContext(
        client = InnerTubeClient(
            clientName = "WEB_REMIX",
            clientVersion = "1.20231204.01.00",
            hl = "en",
            gl = "IN",
        )
    )

    private val androidVrContext = InnerTubeContext(
        client = InnerTubeClient(
            clientName = "ANDROID_VR",
            clientVersion = "1.37",
            deviceMake = "Oculus",
            deviceModel = "Quest 3",
            osName = "Android",
            osVersion = "12",
            platform = "MOBILE",
            hl = "en",
            gl = "IN",
        )
    )

    private val androidMusicContext = InnerTubeContext(
        client = InnerTubeClient(
            clientName = "ANDROID_MUSIC",
            clientVersion = "6.42.52",
            hl = "en",
            gl = "IN",
            androidSdkVersion = 33,
        )
    )

    private val androidMobileContext = InnerTubeContext(
        client = InnerTubeClient(
            clientName = "ANDROID",
            clientVersion = "19.17.36",
            hl = "en",
            gl = "IN",
            androidSdkVersion = 33,
        )
    )

    private val androidCreatorContext = InnerTubeContext(
        client = InnerTubeClient(
            clientName = "ANDROID_CREATOR",
            clientVersion = "22.36.102",
            androidSdkVersion = 30,
            hl = "en",
            gl = "IN",
        )
    )

    private val iosContext = InnerTubeContext(
        client = InnerTubeClient(
            clientName = "IOS",
            clientVersion = "19.29.1",
            hl = "en",
            gl = "IN",
            deviceMake = "Apple",
            deviceModel = "iPhone16,1",
        )
    )

    private val tvHtml5Context = InnerTubeContext(
        client = InnerTubeClient(
            clientName = "TVHTML5_SIMPLY_EMBEDDED_PLAYER",
            clientVersion = "2.0",
            hl = "en",
            gl = "IN",
        )
    )

    suspend fun search(query: String): SearchResponse {
        return client.post("$musicBaseUrl/search?key=$apiKey") {
            header("Content-Type", "application/json")
            header("X-YouTube-Client-Name", "67")
            header("X-YouTube-Client-Version", "1.20231204.01.00")
            header("Origin", "https://music.youtube.com")
            header("Referer", "https://music.youtube.com/")
            setBody(
                SearchRequest(
                    context = webRemixContext,
                    query = query,
                )
            )
        }.body()
    }

    suspend fun getPlayer(
        videoId: String,
        clientType: String = "ANDROID_VR"
    ): PlayerResponse {
        require(videoId.isNotBlank()) { "Video ID cannot be blank" }
        require(videoId.length == 11) { "Invalid YouTube video ID: $videoId" }

        val context = when (clientType) {
            "ANDROID_VR" -> androidVrContext
            "ANDROID_MUSIC" -> androidMusicContext
            "ANDROID_CREATOR" -> androidCreatorContext
            "WEB_REMIX" -> webRemixContext
            "IOS" -> iosContext
            "TVHTML5_SIMPLY_EMBEDDED_PLAYER", "TVHTML5_EMBEDDED" -> tvHtml5Context
            "ANDROID" -> androidMobileContext
            else -> androidVrContext
        }

        val userAgent = when (clientType) {
            "ANDROID_VR" -> "Mozilla/5.0 (Linux; Android 12; Quest 3 Build/SQ3A.220605.009.A1) AppleWebKit/537.36 (KHTML, like Gecko) OculusBrowser/32.0.0.0.0 Safari/537.36"
            "ANDROID_MUSIC" -> "com.google.android.apps.youtube.music/6.42.52 (Linux; U; Android 13; en_IN; Pixel 7 Build/TQ3A.230901.001)"
            "ANDROID", "ANDROID_CREATOR" -> "com.google.android.youtube/19.17.36 (Linux; U; Android 13; en_IN; Pixel 7 Build/TQ3A.230901.001)"
            "IOS" -> "com.google.ios.youtubemusic/6.42.52 (iPhone16,1; U; CPU iOS 17_5_1 like Mac OS X)"
            "WEB_REMIX" -> "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            else -> "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        }

        // Mobile clients must use www.youtube.com, WEB_REMIX uses music.youtube.com
        val targetBaseUrl = if (clientType == "WEB_REMIX") musicBaseUrl else youtubeBaseUrl

        Log.d("SMusicPlay", "getPlayer videoId=$videoId clientType=$clientType url=$targetBaseUrl/player")

        return try {
            client.post("$targetBaseUrl/player?key=$apiKey") {
                header("Content-Type", "application/json")
                header("User-Agent", userAgent)
                if (clientType == "WEB_REMIX") {
                    header("Origin", "https://music.youtube.com")
                    header("Referer", "https://music.youtube.com/")
                }
                setBody(
                    PlayerRequest(
                        context = context,
                        videoId = videoId,
                        playbackContext = null,
                    )
                )
            }.body()
        } catch (e: Exception) {
            Log.e("SMusicPlay", "getPlayer failed for $videoId ($clientType): ${e.message}")
            PlayerResponse(
                playabilityStatus = PlayabilityStatus(
                    status = "ERROR",
                    reason = e.message ?: "Network error"
                )
            )
        }
    }

    suspend fun browse(browseId: String): BrowseResponse {
        return client.post("$musicBaseUrl/browse?key=$apiKey") {
            header("Content-Type", "application/json")
            header("X-YouTube-Client-Name", "67")
            header("X-YouTube-Client-Version", "1.20231204.01.00")
            header("Origin", "https://music.youtube.com")
            header("Referer", "https://music.youtube.com/")
            setBody(
                BrowseRequest(
                    context = webRemixContext,
                    browseId = browseId,
                )
            )
        }.body()
    }

    suspend fun getHome(): BrowseResponse {
        return browse("FEmusic_home")
    }

    suspend fun getTrending(): BrowseResponse {
        return browse("FEmusic_trending")
    }

    suspend fun getNext(videoId: String, playlistId: String? = null): NextResponse {
        return client.post("$musicBaseUrl/next?key=$apiKey") {
            header("Content-Type", "application/json")
            setBody(
                NextRequest(
                    context = androidMusicContext,
                    videoId = videoId,
                    playlistId = playlistId,
                )
            )
        }.body()
    }
}
