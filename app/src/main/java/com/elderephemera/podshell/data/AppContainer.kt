package com.elderephemera.podshell.data

import android.content.Context

interface AppContainer {
    val feedDao: FeedDao
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val feedDao: FeedDao by lazy {
        PodDatabase.getDatabase(context).feedDao()
    }
}