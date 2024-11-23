package com.elderephemera.podshell.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.elderephemera.podshell.BuildConfig
import com.elderephemera.podshell.RefreshWorker
import com.elderephemera.podshell.data.Episode
import com.elderephemera.podshell.data.EpisodesRepository
import com.elderephemera.podshell.data.Feed
import com.elderephemera.podshell.data.FeedsRepository
import com.mr3y.podcastindex.ktor3.PodcastIndexClient
import com.mr3y.podcastindex.model.PodcastFeed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SubscriptionsTab(
    private val feedsRepository: FeedsRepository,
    private val episodesRepository: EpisodesRepository,
) : AppTab {
    override val title = "SUBS"

    @Composable
    override fun Fab() = FloatingActionButton(
        onClick = { showAddFeedDialog = true },
        backgroundColor = MaterialTheme.colors.primary,
        content = { Icon(Icons.Filled.Add, contentDescription = "Add podcast feed") },
    )

    override fun listItems(): Flow<List<ListItemCard>> =
        feedsRepository.getAllFeeds().map {
            it.map { feed ->
                FeedListItemCard(
                    feed,
                    openList = {
                        listDialogFeed = feed
                        listDialogEpisodes = episodesRepository.getAllFeedEpisodes(feed)
                        showListDialog = true
                    },
                    unsubscribe = {
                        unsubscribeDialogFeed = feed
                        showUnsubscribeDialog = true
                    }
                )
            }
        }

    private var showAddFeedDialog by mutableStateOf(false)
    @Composable
    private fun AddFeedDialog(coroutineScope: CoroutineScope) {
        val context = LocalContext.current
        AnimatedVisibility(visible = showAddFeedDialog) {
            Dialog(
                onDismissRequest = { showAddFeedDialog = false },
            ) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colors.background)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    var input by remember { mutableStateOf("") }
                    Text(text = "Add Subscription", style = MaterialTheme.typography.subtitle1)
                    TextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = { Text(text = "Enter search term or feed URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        TextButton(
                            onClick = {
                                subscribe(context, coroutineScope, input)
                                showAddFeedDialog = false
                            },
                        ) {
                            Text(text = "Subscribe")
                        }
                        TextButton(
                            onClick = {
                                showAddFeedDialog = false
                                searchDialogTerm = input
                                showSearchDialog = true
                            }
                        ) {
                            Text(text = "Search")
                        }
                    }
                }
            }
        }
    }

    private var searchDialogTerm by mutableStateOf("")
    private var showSearchDialog by mutableStateOf(false)
    @Composable
    private fun SearchDialog(coroutineScope: CoroutineScope) {
        val context = LocalContext.current
        val client = remember {
            PodcastIndexClient(
                authKey = BuildConfig.PODCAST_INDEX_KEY,
                authSecret = BuildConfig.PODCAST_INDEX_SECRET,
                userAgent = "PodShell/1.0"
            )
        }
        AnimatedVisibility(visible = showSearchDialog) {
            var searchResults: List<PodcastFeed>? by remember { mutableStateOf(null) }
            var searchError: String? by remember { mutableStateOf(null) }
            LaunchedEffect(searchDialogTerm) {
                try {
                    searchResults = client.search.forPodcastsByTerm(searchDialogTerm).feeds
                } catch (e: Exception) {
                    searchError = e.message
                }
            }
            Dialog(
                onDismissRequest = { showSearchDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                val results = searchResults
                val error = searchError
                if (error != null) {
                    AlertDialog(
                        title = { Text("Error") },
                        text = { Text(error, color = MaterialTheme.colors.error) },
                        onDismissRequest = { showSearchDialog = false },
                        confirmButton = { TextButton(onClick = { showSearchDialog = false }) {
                            Text("Continue")
                        } },
                    )
                } else if (results == null) {
                    CircularProgressIndicator()
                } else {
                    Column(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxSize()
                            .background(MaterialTheme.colors.background)
                    ) {
                        TopAppBar(
                            title = { Text("Search Results") },
                            navigationIcon = {
                                IconButton(onClick = { showSearchDialog = false }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            },
                            backgroundColor = MaterialTheme.colors.primary,
                            contentColor = MaterialTheme.colors.onPrimary,
                        )
                        results.map { feed ->
                            SearchItemCard(feed) {
                                subscribe(context, coroutineScope, feed.url)
                                showSearchDialog = false
                            }
                        }.ItemCardList()
                    }
                }
            }
        }
    }

    private fun subscribe(context: Context, coroutineScope: CoroutineScope, feedUrl: String) {
        coroutineScope.launch {
            val feedId = feedsRepository.insertFeed(feedUrl)
            feedsRepository.updateFeed(feedId, feedUrl, markNew = false)
            RefreshWorker.ensureRefreshScheduled(context)
        }
    }

    private var unsubscribeDialogFeed: Feed? by mutableStateOf(null)
    private var showUnsubscribeDialog by mutableStateOf(false)
    @Composable
    private fun UnsubscribeDialog(coroutineScope: CoroutineScope) {
        AnimatedVisibility(visible = showUnsubscribeDialog) {
            AlertDialog(
                title = { Text("Unsubscribe from " + unsubscribeDialogFeed?.title + "?") },
                onDismissRequest = { showUnsubscribeDialog = false },
                dismissButton = {
                    TextButton(onClick = { showUnsubscribeDialog = false }) { Text("CANCEL") }
                },
                confirmButton = {
                    TextButton(onClick = {
                        coroutineScope.launch {
                            unsubscribeDialogFeed?.let{ feedsRepository.deleteFeed(it) }
                        }
                        showUnsubscribeDialog = false
                    }) { Text("OK") }
                },
            )
        }
    }

    private var listDialogFeed: Feed? by mutableStateOf(null)
    private var listDialogEpisodes: Flow<List<Episode>> by mutableStateOf(flowOf(listOf()))
    private var showListDialog by mutableStateOf(false)
    @Composable
    private fun ListDialog() = AnimatedVisibility(visible = showListDialog) {
        Dialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            onDismissRequest = { showListDialog = false },
        ) {
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background)
            ) {
                Text(
                    text = listDialogFeed?.title ?: "",
                    color = MaterialTheme.colors.onPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background(MaterialTheme.colors.primary)
                        .fillMaxWidth()
                        .padding(10.dp)
                )
                val episodes by listDialogEpisodes.collectAsState(listOf())
                episodes.sortedByDescending { it.pubDateTime }.map {
                    EpisodeListItemCard(it, episodesRepository, showLogo = false)
                }.ItemCardList()
            }
        }
    }

    @Composable
    override fun Content(scrollConnection: NestedScrollConnection) {
        val coroutineScope = rememberCoroutineScope()
        AddFeedDialog(coroutineScope)
        SearchDialog(coroutineScope)
        UnsubscribeDialog(coroutineScope)
        ListDialog()
        super.Content(scrollConnection)
    }
}