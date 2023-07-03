package com.elderephemera.podshell.data

import com.prof.rssparser.Parser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class FeedInfo(
    val logo: String?,
    val title: String,
    val url: String,
    val description: String,
    val numEpisodes: Int,
)

fun Feed.info(parser: Parser): Flow<FeedInfo> {
    return flow {
        try {
            val channel = parser.getChannel(url)
            channel.lastBuildDate
            emit(FeedInfo(
                logo = channel.image?.url,
                title = channel.title ?: "",
                url = channel.link ?: url,
                description = channel.description ?: "",
                numEpisodes = channel.articles.size,
            ))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}