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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.elderephemera.podshell.data.Feed
import com.elderephemera.podshell.data.FeedDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SubscriptionsTab(private val feedDao: FeedDao) : AppTab {
    override val title = "SUBSCRIPTIONS"

    private var showDialog by mutableStateOf(false)

    @Composable
    override fun FabIcon() =
        Icon(Icons.Filled.Add, contentDescription = "Add podcast feed")
    override fun fabOnClick() { showDialog = true }

    override fun listItems(): Flow<List<ListItemCard>> =
        feedDao.getAll().map { it.map { feed ->
            object : ListItemCard {
                @Composable
                override fun Logo() {}

                override val title = feed.url
                override val url = ""
                override val subtitle = ""
                override val description = ""

                @Composable
                override fun ActionButton() {}
            }
        }}

    @Composable
    override fun AdditionalContent() = AnimatedVisibility(visible = showDialog) {
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
                                feedDao.insert(Feed(url = feedUrl))
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
}