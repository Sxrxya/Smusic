package com.smusic.app.data.api

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import org.json.JSONObject
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsApi @Inject constructor() {

    private val client = KtorClient.httpClient
    private val baseUrl = "https://lrclib.net/api"

    suspend fun getLyrics(artistName: String, trackName: String): LrcLibResponse? {
        return try {
            client.get("$baseUrl/get") {
                parameter("artist_name", artistName)
                parameter("track_name", trackName)
            }.body<LrcLibResponse>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun searchLyrics(query: String): List<LrcLibResponse> {
        return try {
            client.get("$baseUrl/search") {
                parameter("q", query)
            }.body<List<LrcLibResponse>>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getJioSaavnLyrics(query: String): String? {
        return try {
            val cleanQuery = query.replace(Regex("(?i)\\b(official|video|audio|lyric|lyrics|full song|song|hd|4k)\\b"), "").trim()
            val encoded = URLEncoder.encode(cleanQuery, "UTF-8")
            val searchUrl = "https://www.jiosaavn.com/api.php?__call=autocomplete.get&_format=json&query=$encoded"
            val responseText = client.get(searchUrl) {
                header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
            }.bodyAsText()

            val jsonObj = JSONObject(responseText)
            val songsObj = jsonObj.optJSONObject("songs")
            val dataArray = songsObj?.optJSONArray("data")
            if (dataArray != null && dataArray.length() > 0) {
                val firstSong = dataArray.getJSONObject(0)
                val songId = firstSong.optString("id")
                if (songId.isNotBlank()) {
                    val detUrl = "https://www.jiosaavn.com/api.php?__call=song.getDetails&pids=$songId&_format=json"
                    val detText = client.get(detUrl) {
                        header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    }.bodyAsText()

                    val detJson = JSONObject(detText)
                    val item = detJson.optJSONObject(songId)
                    val hasLyrics = item?.optString("has_lyrics") ?: "false"
                    if (hasLyrics == "true") {
                        val lyrUrl = "https://www.jiosaavn.com/api.php?__call=lyrics.getLyrics&lyrics_id=$songId&ctx=web6dot0&_format=json"
                        val lyrText = client.get(lyrUrl) {
                            header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                        }.bodyAsText()

                        val lyrJson = JSONObject(lyrText)
                        val rawLyrics = lyrJson.optString("lyrics", "")
                        if (rawLyrics.isNotBlank()) {
                            return rawLyrics
                                .replace("<br>", "\n")
                                .replace("<br/>", "\n")
                                .replace("<br />", "\n")
                                .replace("&quot;", "\"")
                                .replace("&#039;", "'")
                                .replace("&amp;", "&")
                        }
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}
