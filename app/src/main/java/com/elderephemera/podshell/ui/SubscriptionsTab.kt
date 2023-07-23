package com.elderephemera.podshell.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.elderephemera.podshell.RefreshService
import com.elderephemera.podshell.data.Episode
import com.elderephemera.podshell.data.EpisodesRepository
import com.elderephemera.podshell.data.Feed
import com.elderephemera.podshell.data.FeedsRepository
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
    private fun AddFeedDialog() {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
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
                    var feedUrl by remember { mutableStateOf("") }
                    Text(text = "Add Subscription", fontSize = 20.sp)
                    TextField(
                        value = feedUrl,
                        onValueChange = { feedUrl = it },
                        placeholder = { Text(text = "Paste feed URL here") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    val feedId = feedsRepository.insertFeed(feedUrl)
                                    feedsRepository.updateFeed(feedId, feedUrl, markNew = false)
                                    RefreshService.ensureRefreshScheduled(context)
                                }
                                showAddFeedDialog = false
                            },
                        ) {
                            Text(text = "SUBSCRIBE")
                        }
                    }
                }
            }
        }
    }

    private var unsubscribeDialogFeed: Feed? by mutableStateOf(null)
    private var showUnsubscribeDialog by mutableStateOf(false)
    @Composable
    private fun UnsubscribeDialog() {
        val coroutineScope = rememberCoroutineScope()
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
    override fun Content() {
        AddFeedDialog()
        UnsubscribeDialog()
        ListDialog()
        super.Content()
    }
}