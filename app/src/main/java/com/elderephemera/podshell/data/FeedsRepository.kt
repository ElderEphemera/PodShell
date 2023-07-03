package com.elderephemera.podshell.data

import kotlinx.coroutines.flow.Flow

interface FeedsRepository {
    suspend fun insertFeed(feed: Feed)
    fun getAllFeeds(): Flow<List<Feed>>
}