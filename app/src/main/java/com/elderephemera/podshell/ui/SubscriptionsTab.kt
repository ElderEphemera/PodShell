package com.elderephemera.podshell.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.elderephemera.podshell.data.Feed
import com.elderephemera.podshell.data.FeedsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SubscriptionsTab(
    private val context: Context,
    private val feedsRepository: FeedsRepository,
) : AppTab {
    override val title = "SUBSCRIPTIONS"

    private var showDialog by mutableStateOf(false)

    @Composable
    override fun FabIcon() =
        Icon(Icons.Filled.Add, contentDescription = "Add podcast feed")
    override fun fabOnClick() { showDialog = true }

    override fun listItems(): Flow<List<ListItemCard>> =
        feedsRepository.getAllFeedInfo(context).map { it.map { feed ->
            object : ListItemCard {
                @Composable
                override fun Logo() = AsyncImage(
                    model = feed.logo,
                    contentDescription = feed.title,
                    contentScale = ContentScale.FillWidth,
                )

                override val title = feed.title
                override val url = feed.url
                override val subtitle = feed.numEpisodes.toString() + " Episodes"
                override val description = feed.description

                @Composable
                override fun ActionButton() = IconButton(onClick = {}) {
                    Icon(Icons.Filled.List, contentDescription = "Show feed episodes")
                }
            }
        }}

    @Composable
    private fun AddFeedDialog() = AnimatedVisibility(visible = showDialog) {
        Dialog(
            onDismissRequest = { showDialog = false },
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
                                showDialog = false
                            }
                        },
                    ) {
                        Text(text = "SUBSCRIBE")
                    }
                }
            }
        }
    }

    @Composable
    override fun Content() {
        AddFeedDialog()
        super.Content()
    }
}