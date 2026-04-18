package com.smusic.app.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─── Common Context ─────────────────────────────────────────
@Serializable
data class InnerTubeContext(
    val client: InnerTubeClient,
)

@Serializable
data class InnerTubeClient(
    val clientName: String,
    val clientVersion: String,
    val hl: String = "en",
    val gl: String = "IN",
    val androidSdkVersion: Int? = null,
)

// ─── Search Request/Response ────────────────────────────────
@Serializable
data class SearchRequest(
    val context: InnerTubeContext,
    val query: String,
    val params: String? = "EgWKAQIIAWoKEAoQAxAEEAkQBQ==",
)

@Serializable
data class SearchResponse(
    val contents: SearchContents? = null,
)

@Serializable
data class SearchContents(
    val tabbedSearchResultsRenderer: TabbedSearchResultsRenderer? = null,
)

@Serializable
data class TabbedSearchResultsRenderer(
    val tabs: List<SearchTab>? = null,
)

@Serializable
data class SearchTab(
    val tabRenderer: SearchTabRenderer? = null,
)

@Serializable
data class SearchTabRenderer(
    val content: SearchTabContent? = null,
)

@Serializable
data class SearchTabContent(
    val sectionListRenderer: SectionListRenderer? = null,
)

@Serializable
data class SectionListRenderer(
    val contents: List<SectionContent>? = null,
)

@Serializable
data class SectionContent(
    val musicShelfRenderer: MusicShelfRenderer? = null,
)

@Serializable
data class MusicShelfRenderer(
    val contents: List<MusicShelfContent>? = null,
    val title: ShelfTitle? = null,
)

@Serializable
data class ShelfTitle(
    val runs: List<TextRun>? = null,
)

@Serializable
data class MusicShelfContent(
    val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer? = null,
)

@Serializable
data class MusicResponsiveListItemRenderer(
    val flexColumns: List<FlexColumn>? = null,
    val thumbnail: ThumbnailRenderer? = null,
    val overlay: ItemOverlay? = null,
    val playlistItemData: PlaylistItemData? = null,
)

@Serializable
data class PlaylistItemData(
    val videoId: String? = null,
)

@Serializable
data class FlexColumn(
    val musicResponsiveListItemFlexColumnRenderer: FlexColumnRenderer? = null,
)

@Serializable
data class FlexColumnRenderer(
    val text: FlexColumnText? = null,
)

@Serializable
data class FlexColumnText(
    val runs: List<TextRun>? = null,
)

@Serializable
data class TextRun(
    val text: String? = null,
    val navigationEndpoint: NavigationEndpoint? = null,
)

@Serializable
data class NavigationEndpoint(
    val browseEndpoint: BrowseEndpoint? = null,
    val watchEndpoint: WatchEndpoint? = null,
)

@Serializable
data class BrowseEndpoint(
    val browseId: String? = null,
    val browseEndpointContextSupportedConfigs: BrowseEndpointConfigs? = null,
)

@Serializable
data class BrowseEndpointConfigs(
    val browseEndpointContextMusicConfig: BrowseEndpointMusicConfig? = null,
)

@Serializable
data class BrowseEndpointMusicConfig(
    val pageType: String? = null,
)

@Serializable
data class WatchEndpoint(
    val videoId: String? = null,
)

@Serializable
data class ThumbnailRenderer(
    val musicThumbnailRenderer: MusicThumbnailRenderer? = null,
)

@Serializable
data class MusicThumbnailRenderer(
    val thumbnail: ThumbnailDetails? = null,
)

@Serializable
data class ThumbnailDetails(
    val thumbnails: List<ThumbnailDto>? = null,
)

@Serializable
data class ThumbnailDto(
    val url: String? = null,
    val width: Int? = null,
    val height: Int? = null,
)

@Serializable
data class ItemOverlay(
    val musicItemThumbnailOverlayRenderer: MusicItemThumbnailOverlayRenderer? = null,
)

@Serializable
data class MusicItemThumbnailOverlayRenderer(
    val content: OverlayContent? = null,
)

@Serializable
data class OverlayContent(
    val musicPlayButtonRenderer: MusicPlayButtonRenderer? = null,
)

@Serializable
data class MusicPlayButtonRenderer(
    val playNavigationEndpoint: NavigationEndpoint? = null,
)

// ─── Player Request/Response ────────────────────────────────
@Serializable
data class PlayerRequest(
    val context: InnerTubeContext,
    val videoId: String,
    val playbackContext: PlaybackContext? = null,
)

@Serializable
data class PlaybackContext(
    val contentPlaybackContext: ContentPlaybackContext? = null,
)

@Serializable
data class ContentPlaybackContext(
    val signatureTimestamp: Int? = null,
)

@Serializable
data class PlayerResponse(
    val streamingData: StreamingData? = null,
    val videoDetails: VideoDetails? = null,
    val playabilityStatus: PlayabilityStatus? = null,
)

@Serializable
data class PlayabilityStatus(
    val status: String? = null,
    val reason: String? = null,
)

@Serializable
data class StreamingData(
    val adaptiveFormats: List<AdaptiveFormat>? = null,
    val expiresInSeconds: String? = null,
)

@Serializable
data class AdaptiveFormat(
    val itag: Int? = null,
    val url: String? = null,
    val mimeType: String? = null,
    val bitrate: Int? = null,
    val contentLength: String? = null,
    val approxDurationMs: String? = null,
    val audioQuality: String? = null,
    val audioSampleRate: String? = null,
    val audioChannels: Int? = null,
    val quality: String? = null,
)

@Serializable
data class VideoDetails(
    val videoId: String? = null,
    val title: String? = null,
    val lengthSeconds: String? = null,
    val channelId: String? = null,
    val author: String? = null,
    val thumbnail: ThumbnailDetails? = null,
    val shortDescription: String? = null,
)

// ─── Browse Request/Response ────────────────────────────────
@Serializable
data class BrowseRequest(
    val context: InnerTubeContext,
    val browseId: String,
)

@Serializable
data class BrowseResponse(
    val contents: BrowseContents? = null,
    val header: BrowseHeader? = null,
    val background: BrowseBackground? = null,
)

@Serializable
data class BrowseContents(
    val singleColumnBrowseResultsRenderer: SingleColumnBrowseResultsRenderer? = null,
)

@Serializable
data class SingleColumnBrowseResultsRenderer(
    val tabs: List<BrowseTab>? = null,
)

@Serializable
data class BrowseTab(
    val tabRenderer: BrowseTabRenderer? = null,
)

@Serializable
data class BrowseTabRenderer(
    val content: BrowseTabContent? = null,
)

@Serializable
data class BrowseTabContent(
    val sectionListRenderer: SectionListRenderer? = null,
)

@Serializable
data class BrowseHeader(
    val musicImmersiveHeaderRenderer: MusicImmersiveHeaderRenderer? = null,
    val musicDetailHeaderRenderer: MusicDetailHeaderRenderer? = null,
)

@Serializable
data class MusicImmersiveHeaderRenderer(
    val title: ShelfTitle? = null,
    val subscriptionButton: SubscriptionButton? = null,
    val thumbnail: ThumbnailRenderer? = null,
    val description: ShelfTitle? = null,
)

@Serializable
data class MusicDetailHeaderRenderer(
    val title: ShelfTitle? = null,
    val subtitle: ShelfTitle? = null,
    val thumbnail: ThumbnailRenderer? = null,
)

@Serializable
data class SubscriptionButton(
    val subscribeButtonRenderer: SubscribeButtonRenderer? = null,
)

@Serializable
data class SubscribeButtonRenderer(
    val subscriberCountText: ShelfTitle? = null,
)

@Serializable
data class BrowseBackground(
    val musicThumbnailRenderer: MusicThumbnailRenderer? = null,
)

// ─── Lyrics ─────────────────────────────────────────────────
@Serializable
data class LrcLibResponse(
    val id: Int? = null,
    val trackName: String? = null,
    val artistName: String? = null,
    val albumName: String? = null,
    val duration: Double? = null,
    val syncedLyrics: String? = null,
    val plainLyrics: String? = null,
)
