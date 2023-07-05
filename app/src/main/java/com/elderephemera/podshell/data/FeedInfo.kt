package com.elderephemera.podshell.data

import com.prof.rssparser.Article
import com.prof.rssparser.Channel
import com.prof.rssparser.Parser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class FeedInfo(
    val logo: String?,
    val title: String,
    val url: String,
    val description: String,
    val episodes: List<EpisodeInfo>,
) {
    constructor(channel: Channel, url: String) : this(
        logo = channel.image?.url,
        title = channel.title ?: "",
        url = channel.link ?: url,
        description = channel.description ?: "",
        episodes = channel.articles.map(::EpisodeInfo),
    )
}

data class EpisodeInfo(
    val guid: String,
    val title: String,
    val url: String,
    val pubDate: String,
    val description: String,
) {
    constructor(article: Article) : this(
        guid = article.guid ?: article.hashCode().toString(),
        title = article.title ?: "",
        url = article.link ?: "",
        pubDate = article.pubDate ?: "unknown",
        description = article.description ?: "",
    )
}

fun Feed.info(parser: Parser): Flow<FeedInfo> {
    return flow {
        try {
            val channel = parser.getChannel(url)
            emit(FeedInfo(channel, url))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}