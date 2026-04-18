package com.smusic.app.data.api

import com.smusic.app.domain.model.Album
import com.smusic.app.domain.model.Artist
import com.smusic.app.domain.model.LyricLine
import com.smusic.app.domain.model.Song

object InnerTubeMapper {

    fun mapSearchResponseToSongs(response: SearchResponse): List<Song> {
        val results = mutableListOf<Song>()
        val sections = response.contents
            ?.tabbedSearchResultsRenderer?.tabs?.firstOrNull()
            ?.tabRenderer?.content?.sectionListRenderer?.contents
            ?: return results

        for (section in sections) {
            val items = section.musicShelfRenderer?.contents ?: continue
            for (item in items) {
                val renderer = item.musicResponsiveListItemRenderer ?: continue
                val song = mapRendererToSong(renderer)
                if (song != null) {
                    results.add(song)
                }
            }
        }
        return results
    }

    private fun mapRendererToSong(renderer: MusicResponsiveListItemRenderer): Song? {
        val columns = renderer.flexColumns ?: return null
        if (columns.size < 2) return null

        val titleRuns = columns[0].musicResponsiveListItemFlexColumnRenderer
            ?.text?.runs ?: return null
        val title = titleRuns.firstOrNull()?.text ?: return null

        val videoId = renderer.playlistItemData?.videoId
            ?: titleRuns.firstOrNull()?.navigationEndpoint?.watchEndpoint?.videoId
            ?: renderer.overlay?.musicItemThumbnailOverlayRenderer?.content
                ?.musicPlayButtonRenderer?.playNavigationEndpoint?.watchEndpoint?.videoId
            ?: return null

        val secondColumnRuns = columns[1].musicResponsiveListItemFlexColumnRenderer
            ?.text?.runs ?: emptyList()

        var artist = ""
        var artistId = ""
        var album = ""
        var albumId = ""
        var duration = ""
        var year = ""

        for (run in secondColumnRuns) {
            val text = run.text ?: continue
            if (text == " • " || text == " & " || text == ", ") continue

            val browseEp = run.navigationEndpoint?.browseEndpoint
            val pageType = browseEp?.browseEndpointContextSupportedConfigs
                ?.browseEndpointContextMusicConfig?.pageType

            when {
                pageType == "MUSIC_PAGE_TYPE_ARTIST" -> {
                    artist = text
                    artistId = browseEp?.browseId ?: ""
                }
                pageType == "MUSIC_PAGE_TYPE_ALBUM" -> {
                    album = text
                    albumId = browseEp?.browseId ?: ""
                }
                text.matches(Regex("\\d+:\\d+")) -> {
                    duration = text
                }
                text.matches(Regex("\\d{4}")) -> {
                    year = text
                }
                artist.isEmpty() && browseEp == null -> {
                    // Could be artist without navigation
                    if (!text.matches(Regex("\\d+.*"))) {
                        artist = text
                    }
                }
            }
        }

        val thumbnails = renderer.thumbnail?.musicThumbnailRenderer
            ?.thumbnail?.thumbnails ?: emptyList()
        val thumbnailUrl = thumbnails.maxByOrNull { it.width ?: 0 }?.url ?: ""

        val durationSeconds = parseDuration(duration)

        return Song(
            videoId = videoId,
            title = title,
            artist = artist,
            artistId = artistId,
            album = album,
            albumId = albumId,
            thumbnailUrl = thumbnailUrl,
            durationSeconds = durationSeconds,
            year = year,
        )
    }

    fun mapPlayerResponseToStreamUrl(response: PlayerResponse): Pair<String, Int>? {
        if (response.playabilityStatus?.status != "OK") return null

        val audioFormats = response.streamingData?.adaptiveFormats
            ?.filter { it.mimeType?.contains("audio") == true }
            ?.sortedByDescending { it.bitrate ?: 0 }
            ?: return null

        val bestFormat = audioFormats.firstOrNull() ?: return null
        val url = bestFormat.url ?: return null
        val bitrate = bestFormat.bitrate ?: 0

        return Pair(url, bitrate)
    }

    fun mapBrowseResponseToSongs(response: BrowseResponse): List<Song> {
        val results = mutableListOf<Song>()
        val sections = response.contents?.singleColumnBrowseResultsRenderer
            ?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents
            ?: return results

        for (section in sections) {
            val items = section.musicShelfRenderer?.contents ?: continue
            for (item in items) {
                val renderer = item.musicResponsiveListItemRenderer ?: continue
                val song = mapRendererToSong(renderer)
                if (song != null) {
                    results.add(song)
                }
            }
        }
        return results
    }

    fun mapBrowseResponseToArtist(response: BrowseResponse, artistId: String): Artist {
        val header = response.header?.musicImmersiveHeaderRenderer
        val name = header?.title?.runs?.firstOrNull()?.text ?: ""
        val thumbnail = header?.thumbnail?.musicThumbnailRenderer
            ?.thumbnail?.thumbnails?.maxByOrNull { it.width ?: 0 }?.url ?: ""
        val subscriberCount = header?.subscriptionButton
            ?.subscribeButtonRenderer?.subscriberCountText
            ?.runs?.firstOrNull()?.text ?: ""
        val description = header?.description?.runs?.firstOrNull()?.text ?: ""
        val topSongs = mapBrowseResponseToSongs(response)

        return Artist(
            artistId = artistId,
            name = name,
            thumbnailUrl = thumbnail,
            subscriberCount = subscriberCount,
            description = description,
            topSongs = topSongs,
        )
    }

    fun mapLrcToLyricLines(lrcContent: String?): List<LyricLine> {
        if (lrcContent.isNullOrBlank()) return emptyList()
        val lines = mutableListOf<LyricLine>()
        val regex = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})](.*)""")

        for (line in lrcContent.lines()) {
            val match = regex.find(line) ?: continue
            val (minutes, seconds, hundredths, text) = match.destructured
            val timeMs = minutes.toLong() * 60_000 +
                    seconds.toLong() * 1000 +
                    hundredths.padEnd(3, '0').toLong()
            lines.add(LyricLine(timeMs = timeMs, text = text.trim()))
        }
        return lines.sortedBy { it.timeMs }
    }

    private fun parseDuration(duration: String): Int {
        if (duration.isBlank()) return 0
        val parts = duration.split(":")
        return when (parts.size) {
            2 -> {
                val minutes = parts[0].toIntOrNull() ?: 0
                val seconds = parts[1].toIntOrNull() ?: 0
                minutes * 60 + seconds
            }
            3 -> {
                val hours = parts[0].toIntOrNull() ?: 0
                val minutes = parts[1].toIntOrNull() ?: 0
                val seconds = parts[2].toIntOrNull() ?: 0
                hours * 3600 + minutes * 60 + seconds
            }
            else -> 0
        }
    }
}
