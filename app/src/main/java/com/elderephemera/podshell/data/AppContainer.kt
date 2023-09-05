package com.elderephemera.podshell.data

import android.content.Context
import okhttp3.Call
import okhttp3.OkHttpClient

interface AppContainer {
    val feedsRepository: FeedsRepository
    val episodesRepository: EpisodesRepository

    val okHttpCallFactory: Call.Factory
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val feedsRepository by lazy {
        OfflineFeedsRepository(
            PodDatabase.getDatabase(context).feedDao(),
            episodesRepository,
            okHttpCallFactory,
        )
    }

    override val episodesRepository by lazy {
        OfflineEpisodesRepository(PodDatabase.getDatabase(context).episodesDao())
    }

    override val okHttpCallFactory: Call.Factory by lazy {
        OkHttpClient.Builder().run {
            addNetworkInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder().run {
                        // Buzzsprout rejects requests with the default OkHttp user agent
                        header("User-Agent", "podshell")
                        build()
                    }
                )
            }
            build()
        }
    }
}