package com.elderephemera.podshell.ui

import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.util.TypedValue.COMPLEX_UNIT_SP
import android.widget.TextView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.elderephemera.podshell.prefOverrideTextSize
import com.elderephemera.podshell.vibrateClick
import kotlinx.coroutines.CoroutineScope

interface ListItemCard {
    val key: Long

    val showLogo: Boolean
    @Composable
    fun Logo()

    val title: String
    val url: String
    val subtitle: String
    val description: String

    val hasError get() = false

    @Composable
    fun ActionButton()

    fun onLongClick(coroutineScope: CoroutineScope) {}

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun Content() {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        Column(
            modifier = Modifier
                .background(MaterialTheme.colors.surface)
                .fillMaxWidth()
        ) {
            var expanded by remember { mutableStateOf(false) }
            val linkColor = Color(0xFF64B5F6)
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .weight(1f)
                        .combinedClickable(
                            onClick = { expanded = !expanded },
                            onLongClick = {
                                vibrateClick(context)
                                onLongClick(coroutineScope)
                            }
                        )
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
                            fontSize = 18.xp,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = if (expanded) Int.MAX_VALUE else 2,
                            minLines = 2,
                        )
                        val uriHandler = LocalUriHandler.current
                        Text(
                            text = url,
                            fontSize = 13.xp,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = if (expanded) Int.MAX_VALUE else 1,
                            textDecoration = TextDecoration.Underline,
                            color = linkColor,
                            modifier = Modifier.clickable { uriHandler.openUri(url) }
                        )
                        Text(
                            text = subtitle,
                            fontSize = 13.xp,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = if (expanded) Int.MAX_VALUE else 1,
                            color =
                                if (hasError) MaterialTheme.colors.error
                                else MaterialTheme.colors.onSurface
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
            val overrideTextSize by context.prefOverrideTextSize.state()
            val textUnit by remember { derivedStateOf {
                if (overrideTextSize) COMPLEX_UNIT_DIP else COMPLEX_UNIT_SP
            }}
            AndroidView(
                factory = { context ->
                    TextView(context).apply {
                        text = HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_LEGACY)
                        ellipsize = TextUtils.TruncateAt.END
                        maxLines = if (expanded) Int.MAX_VALUE else 1
                        setTextColor(onSurface)
                        setLinkTextColor(linkColor.toArgb())
                        setTextSize(textUnit, 16f)
                        linksClickable = true
                        movementMethod = LinkMovementMethod.getInstance()
                    }
                },
                update = {
                    it.text = HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    it.maxLines = if (expanded) Int.MAX_VALUE else 1
                    it.setTextSize(textUnit, 16f)
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
    items(this@ItemCardList, key = ListItemCard::key) {
        it.Content()
    }
}