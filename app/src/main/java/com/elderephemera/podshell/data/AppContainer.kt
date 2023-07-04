package com.elderephemera.podshell.data

import android.content.Context

interface AppContainer {
    val feedsRepository: FeedsRepository
    val episodesRepository: EpisodesRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val feedsRepository by lazy {
        OfflineFeedsRepository(PodDatabase.getDatabase(context).feedDao())
    }

    override val episodesRepository by lazy {
        OfflineEpisodesRepository(PodDatabase.getDatabase(context).episodesDao())
    }
}