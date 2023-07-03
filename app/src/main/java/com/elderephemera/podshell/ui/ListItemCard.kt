package com.elderephemera.podshell.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
            var expanded by remember { mutableStateOf(false) }
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { expanded = !expanded }
                        .padding(5.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .width(75.dp)
                            .fillMaxHeight()
                    ) {
                        Logo()
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = if (expanded) Int.MAX_VALUE else 2,
                            minLines = 2,
                        )
                        val uriHandler = LocalUriHandler.current
                        Text(
                            text = url,
                            fontSize = 13.sp,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = if (expanded) Int.MAX_VALUE else 1,
                            textDecoration = TextDecoration.Underline,
                            color = Color(0xFF64B5F6),
                            modifier = Modifier.clickable { uriHandler.openUri(url) }
                        )
                        Text(
                            text = subtitle,
                            fontSize = 13.sp,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = if (expanded) Int.MAX_VALUE else 1,
                        )
                    }
                }
                Divider(
                    color = MaterialTheme.colors.background,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(75.dp)
                        .fillMaxHeight()
                ) {
                    ActionButton()
                }
            }
            Divider(color = MaterialTheme.colors.background)
            Text(
                text = description,
                overflow = TextOverflow.Ellipsis,
                maxLines = if (expanded) Int.MAX_VALUE else 1,
                modifier = Modifier.padding(5.dp)
            )
        }
    }
}