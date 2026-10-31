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
    private val baseUrl = "https://music.youtube.com/youtubei/v1"
    private val apiKey = "AIzaSyC9XL3ZjWddXya6X74dJoCTL-KLET5YdCE"

    private val webRemixContext = InnerTubeContext(
        client = InnerTubeClient(
            clientName = "WEB_REMIX",
            clientVersion = "1.20231204.01.00",
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

    private val iosContext = InnerTubeContext(
        client = InnerTubeClient(
            clientName = "IOS",
            clientVersion = "19.29.1",
            hl = "en",
            gl = "IN",
        )
    )

    private val iosMusicContext = InnerTubeContext(
        client = InnerTubeClient(
            clientName = "IOS_MUSIC",
            clientVersion = "6.42.52",
            hl = "en",
            gl = "IN",
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
        return client.post("$baseUrl/search?key=$apiKey") {
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
        clientType: String = "ANDROID_MUSIC"
    ): PlayerResponse {
        require(videoId.isNotBlank()) { "Video ID cannot be blank" }
        require(videoId.length == 11) { "Invalid YouTube video ID: $videoId" }

        val context = when (clientType) {
            "ANDROID_MUSIC" -> androidMusicContext
            "WEB_REMIX" -> webRemixContext
            "IOS" -> iosContext
            "IOS_MUSIC" -> iosMusicContext
            "TVHTML5_SIMPLY_EMBEDDED_PLAYER", "TVHTML5_EMBEDDED" -> tvHtml5Context
            "ANDROID" -> androidMobileContext
            else -> androidMusicContext
        }

        val userAgent = when (clientType) {
            "ANDROID_MUSIC" -> "com.google.android.apps.youtube.music/6.42.52 (Linux; U; Android 13; en_IN; Pixel 7 Build/TQ3A.230901.001)"
            "ANDROID" -> "com.google.android.youtube/19.17.36 (Linux; U; Android 13; en_IN; Pixel 7 Build/TQ3A.230901.001)"
            "IOS", "IOS_MUSIC" -> "com.google.ios.youtubemusic/6.42.52 (iPhone16,1; U; CPU iOS 17_5_1 like Mac OS X)"
            else -> "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        }

        Log.d("SMusicPlay", "getPlayer videoId=$videoId clientType=$clientType")

        return try {
            client.post("$baseUrl/player?key=$apiKey") {
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
                        // Don't send playbackContext — stale signatureTimestamp causes
                        // YouTube to return signatureCipher instead of direct URLs
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
        return client.post("$baseUrl/browse?key=$apiKey") {
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
        return client.post("$baseUrl/next?key=$apiKey") {
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
