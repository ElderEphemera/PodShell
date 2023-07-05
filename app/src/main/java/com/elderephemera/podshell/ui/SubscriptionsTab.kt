package com.elderephemera.podshell.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.elderephemera.podshell.data.Feed
import com.elderephemera.podshell.data.FeedInfo
import com.elderephemera.podshell.data.FeedsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SubscriptionsTab(
    private val context: Context,
    private val feedsRepository: FeedsRepository,
) : AppTab {
    override val title = "SUBSCRIPTIONS"

    @Composable
    override fun FabIcon() =
        Icon(Icons.Filled.Add, contentDescription = "Add podcast feed")
    override fun fabOnClick() { showAddFeedDialog = true }

    override fun listItems(): Flow<List<ListItemCard>> =
        feedsRepository.getAllFeedInfo(context).map {
            it.map { feed ->
                FeedListItemCard(feed) {
                    listDialogFeed = feed
                    showListDialog = true
                }
            }
        }

    private var showAddFeedDialog by mutableStateOf(false)
    @Composable
    private fun AddFeedDialog() = AnimatedVisibility(visible = showAddFeedDialog) {
        Dialog(
            onDismissRequest = { showAddFeedDialog = false },
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colors.background)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val dataScope = rememberCoroutineScope()
                var feedUrl by remember { mutableStateOf("") }
                Text(text = "Add Subscription", fontSize = 20.sp)
                TextField(
                    value = feedUrl,
                    onValueChange = { feedUrl = it },
                    placeholder = { Text(text = "Paste feed URL here") },
                )
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    TextButton(
                        onClick = {
                            dataScope.launch {
                                feedsRepository.insertFeed(Feed(url = feedUrl))
                                showAddFeedDialog = false
                            }
                        },
                    ) {
                        Text(text = "SUBSCRIBE")
                    }
                }
            }
        }
    }

    private var listDialogFeed: FeedInfo? by mutableStateOf(null)
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
                listDialogFeed?.episodes?.map(::EpisodeListItemCard)?.ItemCardList()
            }
        }
    }

    @Composable
    override fun Content() {
        AddFeedDialog()
        ListDialog()
        super.Content()
    }
}