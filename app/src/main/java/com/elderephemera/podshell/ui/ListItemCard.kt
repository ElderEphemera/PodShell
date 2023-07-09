package com.elderephemera.podshell.ui

import android.text.TextUtils
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat

interface ListItemCard {
    val showLogo: Boolean
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
                    if (showLogo) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(75.dp)
                        ) {
                            Logo()
                        }
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
            val onSurface = MaterialTheme.colors.onSurface.toArgb()
            AndroidView(
                factory = { context ->
                    TextView(context).apply {
                        text = HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_LEGACY)
                        ellipsize = TextUtils.TruncateAt.END
                        maxLines = if (expanded) Int.MAX_VALUE else 1
                        setTextColor(onSurface)
                    }
                },
                update = {
                    it.text = HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    it.maxLines = if (expanded) Int.MAX_VALUE else 1
                },
                modifier = Modifier.padding(5.dp)
            )
        }
    }
}

@Composable
fun List<ListItemCard>.ItemCardList() = LazyColumn(
    verticalArrangement = Arrangement.spacedBy(10.dp),
    modifier = Modifier
        .fillMaxSize()
        .padding(10.dp)
) {
    items(this@ItemCardList) {
        it.Content()
    }
}