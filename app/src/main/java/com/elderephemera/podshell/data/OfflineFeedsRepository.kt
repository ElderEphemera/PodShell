package com.elderephemera.podshell.data

import kotlinx.coroutines.flow.Flow

class OfflineFeedsRepository(private val feedDao: FeedDao) : FeedsRepository {
    override suspend fun insertFeed(feed: Feed) = feedDao.insert(feed)
    override fun getAllFeeds(): Flow<List<Feed>> = feedDao.getAll()
}