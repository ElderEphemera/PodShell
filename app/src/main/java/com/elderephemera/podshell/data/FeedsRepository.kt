package com.elderephemera.podshell.data

import android.content.Context
import com.prof.rssparser.Parser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapMerge

interface FeedsRepository {
    suspend fun insertFeed(feed: Feed)
    fun getAllFeeds(): Flow<List<Feed>>

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAllFeedInfo(context: Context): Flow<List<FeedInfo>> {
        val parser = Parser.Builder()
            .context(context)
            .cacheExpirationMillis(24L * 60L * 60L * 1000L) // one day
            .build()
        return getAllFeeds().flatMapMerge { feeds ->
            combine(feeds.map { it.info(parser) }) { it.toList() }
        }
    }
}