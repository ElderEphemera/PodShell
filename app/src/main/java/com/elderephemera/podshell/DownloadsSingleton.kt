package com.elderephemera.podshell

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DownloadManager
import java.io.File
import java.util.concurrent.Executor

@OptIn(UnstableApi::class)
class DownloadsSingleton private constructor(context: Context) {
    companion object {
        @Volatile
        private var Instance: DownloadsSingleton? = null

        fun getInstance(context: Context): DownloadsSingleton =
            Instance ?: synchronized(this) {
                DownloadsSingleton(context).also { Instance = it }
            }

        fun getInstance(): DownloadsSingleton? = Instance
    }

    private val databaseProvider = StandaloneDatabaseProvider(context)
    private val downloadCache = SimpleCache(
        File(context.filesDir, "cache"),
        NoOpCacheEvictor(),
        databaseProvider,
    )
    private val httpDataSourceFactory = DefaultHttpDataSource.Factory()
    val cacheDataSourceFactory =
        CacheDataSource.Factory()
            .setCache(downloadCache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setCacheWriteDataSinkFactory(null)
    val downloadExecutor = Executor(Runnable::run)

    val downloadManager = DownloadManager(
        context,
        databaseProvider,
        downloadCache,
        httpDataSourceFactory,
        downloadExecutor
    )
}