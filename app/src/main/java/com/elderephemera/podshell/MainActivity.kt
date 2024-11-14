package com.elderephemera.podshell

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.Coil
import coil.ImageLoader
import com.elderephemera.podshell.data.AppDataContainer
import com.elderephemera.podshell.ui.*

class MainActivity : ComponentActivity() {
    private lateinit var fileManager: AppFileManager
    private var controller: MediaController? by mutableStateOf(null)

    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val specifiedTab = intent.extras?.getInt("tab")

        if (specifiedTab == 1) {
            RefreshWorker.cancelNotification(this)
        }

        val appContainer = AppDataContainer(applicationContext)

        fileManager = AppFileManager(
            activityResultRegistry,
            contentResolver,
            appContainer.feedsRepository,
        )
        lifecycle.addObserver(fileManager)

        val sessionToken =
            SessionToken(this, ComponentName(this, PlayerService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener(
            { controller = controllerFuture.get() },
            ContextCompat.getMainExecutor(this)
        )

        val imageLoader = ImageLoader.Builder(this)
            .respectCacheHeaders(enable = false)
            .callFactory(appContainer.okHttpCallFactory)
            .build()
        Coil.setImageLoader(imageLoader)

        setContent {
            Main(appContainer, controller, fileManager, specifiedTab)
        }
    }

    override fun onDestroy() {
        controller?.release()
        super.onDestroy()
    }
}