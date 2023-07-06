package com.elderephemera.podshell.data

import com.prof.rssparser.Parser
import kotlinx.coroutines.flow.first

class OfflineFeedsRepository(
    private val feedDao: FeedDao,
    private val episodesRepository: EpisodesRepository,
) : FeedsRepository {
    override suspend fun insertFeed(url: String) = feedDao.insert(Feed(
        logo = null,
        title = url,
        url = url,
        description = "",
    ))
    override fun getAllFeeds() = feedDao.getAll()

    private val parser = Parser.Builder().build()
    override suspend fun updateFeed(id: Long, url: String) {
        val channel = parser.getChannel(url)
        val feed = Feed(
            id = id,
            logo = channel.image?.url,
            title = channel.title ?: "",
            url = channel.link ?: url,
            description = channel.description ?: "",
        )
        feedDao.update(feed)
        val existing = episodesRepository.getAllFeedEpisodes(feed).first()
        for (article in channel.articles) {
            val old = existing.firstOrNull { it.guid == article.guid }
            val new = Episode(
                guid = article.guid ?: article.hashCode().toString(),
                feedId = id,
                title = article.title ?: "",
                url = article.link ?: url,
                pubDate = article.pubDate ?: "unknown",
                description = article.description ?: "",
            )
            if (old != null) {
                episodesRepository.updateEpisode(new.copy(id = old.id))
            } else {
                episodesRepository.insertEpisode(new)
            }
        }
    }
}