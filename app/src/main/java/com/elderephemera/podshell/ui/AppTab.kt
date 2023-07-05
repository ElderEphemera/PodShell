package com.elderephemera.podshell.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface AppTab {
    val title : String

    @Composable
    fun FabIcon()
    fun fabOnClick()

    fun listItems(): Flow<List<ListItemCard>> = flowOf(listOf())

    @Composable
    fun Content() = Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = this::fabOnClick,
                backgroundColor = MaterialTheme.colors.primary,
                content = { FabIcon() },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            val listItems by listItems().collectAsState(listOf())
            listItems.ItemCardList()
        }
    }
}