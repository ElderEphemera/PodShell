package com.elderephemera.podshell.data

import kotlinx.coroutines.flow.Flow

interface FeedsRepository {
    suspend fun insertFeed(url: String): Long
    fun getAllFeeds(): Flow<List<Feed>>
    suspend fun updateFeed(id: Long, url: String)
}