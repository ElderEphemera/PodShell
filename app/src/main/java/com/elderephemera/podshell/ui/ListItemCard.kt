package com.elderephemera.podshell.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

interface ListItemCard {
    @Composable
    fun Logo()

    val title: String
    val url: String
    val subtitle: String
    val description: String

    @Composable
    fun ActionButton()

    @Composable
    fun Content() {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colors.surface)
                .fillMaxWidth()
        ) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {}
                        .padding(3.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.width(75.dp).fillMaxHeight()
                    ) {
                        Logo()
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title)
                        Text(url)
                        Text(subtitle)
                    }
                }
                Divider(
                    color = MaterialTheme.colors.background,
                    modifier = Modifier.fillMaxHeight().width(1.dp)
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.width(75.dp).fillMaxHeight()
                ) {
                    ActionButton()
                }
            }
            Divider(color = MaterialTheme.colors.background)
            Text(description)
        }
    }
}