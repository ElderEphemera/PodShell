package com.elderephemera.podshell.data

import android.content.Context

interface AppContainer {
    val feedsRepository: FeedsRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val feedsRepository by lazy {
        OfflineFeedsRepository(PodDatabase.getDatabase(context).feedDao())
    }
}