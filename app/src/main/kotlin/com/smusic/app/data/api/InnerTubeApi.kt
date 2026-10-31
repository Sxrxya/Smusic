package com.smusic.app.data.api

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
            clientVersion = "1.20230501.01.00",
            hl = "en",
            gl = "IN",
        )
    )

    private val androidMusicContext = InnerTubeContext(
        client = InnerTubeClient(
            clientName = "ANDROID_MUSIC",
            clientVersion = "5.44.54",
            hl = "en",
            gl = "IN",
            androidSdkVersion = 31,
        )
    )

    private val androidMobileContext = InnerTubeContext(
        client = InnerTubeClient(
            clientName = "ANDROID",
            clientVersion = "19.05.36",
            hl = "en",
            gl = "IN",
            androidSdkVersion = 31,
        )
    )

    suspend fun search(query: String): SearchResponse {
        return client.post("$baseUrl/search?key=$apiKey") {
            header("X-YouTube-Client-Name", "67")
            header("X-YouTube-Client-Version", "1.20230501.01.00")
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

    private val iosContext = InnerTubeContext(
        client = InnerTubeClient(
            clientName = "IOS",
            clientVersion = "19.05.36",
            hl = "en",
            gl = "IN",
        )
    )

    private val tvHtml5Context = InnerTubeContext(
        client = InnerTubeClient(
            clientName = "TVHTML5_EMBEDDED",
            clientVersion = "2.20230607.00.00",
            hl = "en",
            gl = "IN",
        )
    )

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
            "TVHTML5_EMBEDDED" -> tvHtml5Context
            "ANDROID" -> androidMobileContext
            else -> androidMusicContext
        }

        return client.post("$baseUrl/player?key=$apiKey") {
            header("Content-Type", "application/json")
            if (clientType == "ANDROID_MUSIC" || clientType == "ANDROID") {
                header("User-Agent", "com.google.android.apps.youtube.music/5.44.54 (Linux; U; Android 12; en_IN; Pixel 6 Build/SD1A.210817.036)")
            }
            setBody(
                PlayerRequest(
                    context = context,
                    videoId = videoId,
                    playbackContext = PlaybackContext(
                        contentPlaybackContext = ContentPlaybackContext(
                            signatureTimestamp = 20188,
                        )
                    ),
                )
            )
        }.body()
    }

    suspend fun browse(browseId: String): BrowseResponse {
        return client.post("$baseUrl/browse?key=$apiKey") {
            header("X-YouTube-Client-Name", "67")
            header("X-YouTube-Client-Version", "1.20230501.01.00")
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
