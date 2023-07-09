package com.elderephemera.podshell.data

import kotlinx.coroutines.flow.Flow

interface FeedsRepository {
    suspend fun insertFeed(url: String): Long
    suspend fun getFeed(id: Long): Feed
    fun getAllFeeds(): Flow<List<Feed>>
    suspend fun updateFeed(id: Long, url: String, markNew: Boolean)
    suspend fun deleteFeed(feed: Feed)
}