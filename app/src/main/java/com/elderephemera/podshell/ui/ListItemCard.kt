package com.elderephemera.podshell.ui

import android.text.TextUtils
import android.text.method.LinkMovementMethod
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
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.elderephemera.podshell.ui.theme.linkColor
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
    fun Content() = Surface(elevation = 5.dp) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        Column(
            modifier = Modifier
                .background(MaterialTheme.colors.surface)
                .fillMaxWidth()
        ) {
            var expanded by remember { mutableStateOf(false) }
            val linkColor = linkColor()
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
                            style = MaterialTheme.typography.subtitle1,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = if (expanded) Int.MAX_VALUE else 2,
                            modifier = Modifier.defaultMinSize(
                                minHeight = lineHeightDp(MaterialTheme.typography.subtitle1)*2
                            ),
                        )
                        val uriHandler = LocalUriHandler.current
                        Text(
                            text = url,
                            style = MaterialTheme.typography.subtitle2,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = if (expanded) Int.MAX_VALUE else 1,
                            textDecoration = TextDecoration.Underline,
                            color = linkColor,
                            modifier = Modifier.clickable { uriHandler.openUri(url) }
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.subtitle2,
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
            val fontSize = MaterialTheme.typography.body1.fontSize.value
            AndroidView(
                factory = { context ->
                    TextView(context).apply {
                        text = HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_LEGACY)
                            .trim()
                        ellipsize = TextUtils.TruncateAt.END
                        maxLines = if (expanded) Int.MAX_VALUE else 1
                        setTextColor(onSurface)
                        setLinkTextColor(linkColor.toArgb())
                        textSize = fontSize
                        linksClickable = true
                        movementMethod = LinkMovementMethod.getInstance()
                    }
                },
                update = {
                    it.text = HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_LEGACY)
                        .trim()
                    it.maxLines = if (expanded) Int.MAX_VALUE else 1
                    it.textSize = fontSize
                    it.setTextColor(onSurface)
                },
                modifier = Modifier.padding(5.dp)
            )
        }
    }
}

/**
 * Calculate the height of a line of text in the given style.
 *
 * This is used as a workaround for minLines being broken.
 * See: https://issuetracker.google.com/issues/297974035
 */
@Composable
private fun lineHeightDp(style: TextStyle): Dp {
    val textMeasurer = rememberTextMeasurer(cacheSize = 1)
    val singleLineHeightPx = remember(textMeasurer, style) {
        textMeasurer.measure("", style).size.height
    }
    return with(LocalDensity.current) {
        singleLineHeightPx.toDp()
    }
}

@Composable
fun List<ListItemCard>.ItemCardList(
    scrollConnection: NestedScrollConnection = object : NestedScrollConnection {}
) = LazyColumn(
    verticalArrangement = Arrangement.spacedBy(10.dp),
    modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
        .padding(10.dp)
        .nestedScroll(scrollConnection)
) {
    items(this@ItemCardList, key = ListItemCard::key) {
        it.Content()
    }
}