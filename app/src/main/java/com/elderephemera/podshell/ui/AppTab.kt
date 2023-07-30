package com.elderephemera.podshell.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.Flow

interface AppTab {
    val title : String

    @Composable
    fun Fab()

    fun listItems(): Flow<List<ListItemCard>>

    @Composable
    fun Content() = Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            val listItems by listItems().collectAsState(listOf())
            listItems.ItemCardList()
        }
    }
}