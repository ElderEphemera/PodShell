package com.elderephemera.podshell

import android.content.ContentResolver
import android.util.Xml
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.elderephemera.podshell.data.Feed
import com.elderephemera.podshell.data.FeedsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlSerializer
import java.io.*
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

interface FileManager {
    fun importOpml()
    suspend fun exportOpml()
}

class AppFileManager(
    private val registry : ActivityResultRegistry,
    private val contentResolver: ContentResolver,
    private val feedsRepository: FeedsRepository,
) : DefaultLifecycleObserver, FileManager {
    private lateinit var createDocument: ActivityResultLauncher<String>
    private lateinit var openDocument: ActivityResultLauncher<Array<String>>

    private var feeds: List<Feed> = listOf()

    override fun onCreate(owner: LifecycleOwner) {
        createDocument = registry.register(
            "create-document",
            owner,
            CreateDocument(mimeType = "text/xml")
        ) { uri ->
            if (uri != null) {
                contentResolver.openFileDescriptor(uri, "w")?.use { descriptor ->
                    FileOutputStream(descriptor.fileDescriptor).use { outputStream ->
                        serializeOpml(feeds, outputStream)
                    }
                }
            }
        }

        openDocument = registry.register(
            "open-document",
            owner,
            OpenDocument()
        ) { uri ->
            if (uri != null) {
                contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                    FileInputStream(descriptor.fileDescriptor).use { inputStream ->
                        owner.lifecycleScope.launch {
                            val urls = parseOpml(inputStream)
                            addFeeds(urls)
                        }
                    }
                }
            }
        }
    }

    private suspend fun addFeeds(urls: List<String>) {
        val old = feedsRepository.getAllFeeds().first().map { it.rss }
        val new = urls.minus(old.toSet())
        for (url in new) {
            val id = feedsRepository.insertFeed(url)
            feedsRepository.updateFeed(id, url, markNew = false)
        }
    }

    override fun importOpml() {
        openDocument.launch(arrayOf("text/*"))
    }

    override suspend fun exportOpml() {
        feeds = feedsRepository.getAllFeeds().first()
        createDocument.launch("podshell-subscriptions.opml")
    }

    companion object {
        fun parseOpml(inputStream: InputStream): List<String> = buildList {
            Xml.newPullParser().run {
                setInput(inputStream, null)
                while (skipUntil { it == XmlPullParser.START_TAG && name == "outline" }) {
                    getAttributeValue(null, "xmlUrl")?.let(::add)
                }
            }
        }

        private fun XmlPullParser.skipUntil(predicate: (Int) -> Boolean): Boolean {
            var event = next()
            while (event != XmlPullParser.END_DOCUMENT && !predicate(event)) event = next()
            return event != XmlPullParser.END_DOCUMENT
        }

        fun serializeOpml(feeds: List<Feed>, outputStream: OutputStream) = Xml.newSerializer().run {
            setOutput(outputStream, "UTF-8")
            startDocument("UTF-8", false)

            tag("opml", "version" to "1.0") {
                tag("head") {
                    tag("title") { text("PodShell Subscriptions") }
                    tag("dateCreated") {
                        text(OffsetDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME))
                    }
                }
                tag("body") {
                    for (feed in feeds) {
                        tag(
                            "outline",
                            "type" to "rss",
                            "text" to feed.title,
                            "xmlUrl" to feed.rss,
                        )
                    }
                }
            }

            endDocument()
            flush()
        }

        private inline fun XmlSerializer.tag(
            name: String,
            vararg attributes: Pair<String, String>,
            contents: () -> Unit = {},
        ) {
            startTag(null, name)
            for ((attrName, attrValue) in attributes) attribute(null, attrName, attrValue)
            contents()
            endTag(null, name)
        }
    }
}