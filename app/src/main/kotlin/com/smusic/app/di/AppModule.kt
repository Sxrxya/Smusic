package com.smusic.app.di

import android.content.Context
import androidx.room.Room
import com.smusic.app.data.db.SMusicDatabase
import com.smusic.app.data.db.dao.AlbumDao
import com.smusic.app.data.db.dao.ArtistDao
import com.smusic.app.data.db.dao.DownloadDao
import com.smusic.app.data.db.dao.PlaylistDao
import com.smusic.app.data.db.dao.RecentlyPlayedDao
import com.smusic.app.data.db.dao.SongDao
import com.smusic.app.data.db.dao.SongStatsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideHttpClient(json: Json): HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            level = LogLevel.NONE
        }
        engine {
            connectTimeout = 15_000
            socketTimeout = 30_000
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SMusicDatabase {
        return Room.databaseBuilder(
            context,
            SMusicDatabase::class.java,
            "smusic_database",
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideSongDao(db: SMusicDatabase): SongDao = db.songDao()

    @Provides
    fun providePlaylistDao(db: SMusicDatabase): PlaylistDao = db.playlistDao()

    @Provides
    fun provideArtistDao(db: SMusicDatabase): ArtistDao = db.artistDao()

    @Provides
    fun provideAlbumDao(db: SMusicDatabase): AlbumDao = db.albumDao()

    @Provides
    fun provideRecentlyPlayedDao(db: SMusicDatabase): RecentlyPlayedDao = db.recentlyPlayedDao()

    @Provides
    fun provideDownloadDao(db: SMusicDatabase): DownloadDao = db.downloadDao()

    @Provides
    fun provideSongStatsDao(db: SMusicDatabase): SongStatsDao = db.songStatsDao()
}
