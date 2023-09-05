package com.elderephemera.podshell.data

import com.prof.rssparser.Parser
import kotlinx.coroutines.flow.first
import okhttp3.Call
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class OfflineFeedsRepository(
    private val feedDao: FeedDao,
    private val episodesRepository: EpisodesRepository,
    callFactory: Call.Factory,
) : FeedsRepository {
    override suspend fun insertFeed(url: String) = feedDao.insert(Feed(
        logo = null,
        rss = url,
        title = url,
        url = url,
        description = "",
    ))

    override suspend fun getFeed(id: Long) = feedDao.get(id)

    override fun getAllFeeds() = feedDao.getAll()

    private val parser = Parser.Builder(callFactory = callFactory).build()
    override suspend fun updateFeed(id: Long, url: String, markNew: Boolean) {
        val channel = try { parser.getChannel(url) } catch (e: Exception) {
            feedDao.setError(id, error = "Refresh failed: ${e.message ?: "Unknown error"}")
            return
        }
        val feed = Feed(
            id = id,
            rss = url,
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
                source = article.audio ?: "",
                inPlaylist = old?.inPlaylist ?: false,
                new = old?.new ?: markNew,
                position = old?.position,
                length = parseDuration(article.itunesArticleData?.duration) ?: old?.length,
                logo = article.image ?: article.itunesArticleData?.image ?: feed.logo,
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

    private fun parseDuration(durationString: String?): Long? = durationString
        ?.split(":")
        ?.reversed()
        ?.zip(listOf(DurationUnit.SECONDS, DurationUnit.MINUTES, DurationUnit.HOURS))
        { part, unit -> part.toIntOrNull()?.toDuration(unit) }
        ?.filterNotNull()
        ?.fold(Duration.ZERO, Duration::plus)
        ?.inWholeMilliseconds

    override suspend fun deleteFeed(feed: Feed) = feedDao.delete(feed)
}