/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * Rpc.kt is part of Rpc
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.rpc.feature_rpc_base.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Build
import android.os.IBinder
import com.my.rpc.data.get_current_data.app.GetCurrentlyRunningApp
import com.my.rpc.data.get_current_data.media.GetCurrentPlayingMediaAll
import com.my.rpc.data.get_current_data.media.RichMediaMetadata
import com.my.rpc.data.rpc.CommonRpc
import com.my.rpc.data.rpc.RpcRPC
import com.my.rpc.data.rpc.RpcImage
import com.my.rpc.data.rpc.TemplateKeys
import com.my.rpc.data.rpc.TemplateProcessor
import com.my.rpc.data.rpc.Timestamps
import com.my.rpc.domain.interfaces.Logger
import com.my.rpc.domain.model.rpc.RpcButtons
import com.my.rpc.feature_rpc_base.AppUtils
import com.my.rpc.feature_rpc_base.Constants
import com.my.rpc.feature_rpc_base.setLargeIcon
import com.my.rpc.preference.Prefs
import com.my.rpc.resources.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

private const val TAG = "RpcRPC"

@Suppress("DEPRECATION")
@AndroidEntryPoint
class Rpc : Service() {

    @Inject
    lateinit var scope: CoroutineScope

    @Inject
    lateinit var rpcRPC: RpcRPC

    @Inject
    lateinit var getCurrentPlayingMediaAll: GetCurrentPlayingMediaAll

    @Inject
    lateinit var getCurrentlyRunningApp: GetCurrentlyRunningApp

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var notificationBuilder: Notification.Builder

    private lateinit var mediaSessionManager: MediaSessionManager

    private var currentMediaController: MediaController? = null
    private val mediaControllerCallback = MediaControllerCallback()

    private var isMediaSessionActive = false

    // Settings are loaded once in onStartCommand and cached
    private var useAppsRpc = false
    private var useMediaRpc = false
    private var templateName = ""
    private var templateDetails = ""
    private var templateState = ""
    private var appActivityTypes: Map<String, Int> = emptyMap()
    private var enabledRpcApps: List<String> = emptyList()

    // Cached preference values read once per service start — avoids repeated SharedPreferences I/O
    private var cachedHideOnPause = false
    private var cachedShowCoverArt = false
    private var cachedShowAppIcon = false
    private var cachedShowAppAndPauseIcon = false
    private var cachedShowPlaybackState = false
    private var cachedShowAlbumName = false
    private var cachedEnableTimestamps = false
    private var cachedCustomStatus = "dnd"
    private var cachedUseRpcButtons = false
    private var cachedRpcButtons: RpcButtons = RpcButtons()
    private var cachedUseLowResIcon = false

    /** Tracked app detection job to prevent concurrent polling loops. */
    private var appDetectionJob: Job? = null

    /** Whether notification actions have already been added for this lifecycle. */
    private var notificationActionsAdded = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action.equals(Constants.ACTION_STOP_SERVICE)) {
            stopSelf()
        } else if (intent?.action.equals(Constants.ACTION_RESTART_SERVICE)) {
            stopSelf()
            // Post start to avoid race with stopSelf/onDestroy
            android.os.Handler(mainLooper).post {
                startForegroundService(Intent(this, Rpc::class.java))
            }
        } else {
            val stopIntent = Intent(this, Rpc::class.java)
            stopIntent.action = Constants.ACTION_STOP_SERVICE
            val pendingIntent: PendingIntent = PendingIntent.getService(
                this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE
            )
            val restartIntent = Intent(this, Rpc::class.java)
            restartIntent.action = Constants.ACTION_RESTART_SERVICE
            val restartPendingIntent: PendingIntent = PendingIntent.getService(
                this, 2, restartIntent, PendingIntent.FLAG_IMMUTABLE
            )

            // Only add notification actions once to prevent accumulation
            if (!notificationActionsAdded) {
                notificationBuilder
                    .setSmallIcon(R.drawable.ic_dev_rpc)
                    .setContentTitle(getString(R.string.service_enabled))
                    .addAction(
                        R.drawable.ic_dev_rpc,
                        getString(R.string.restart),
                        restartPendingIntent
                    )
                    .addAction(R.drawable.ic_dev_rpc, getString(R.string.exit), pendingIntent)
                notificationActionsAdded = true
            } else {
                notificationBuilder
                    .setSmallIcon(R.drawable.ic_dev_rpc)
                    .setContentTitle(getString(R.string.service_enabled))
            }

            val notification = notificationBuilder.build()
            AppUtils.isRpcServiceRunning = true
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                startForeground(Constants.NOTIFICATION_ID, notification)
            } else {
                startForeground(Constants.NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            }


            mediaSessionManager = getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
            mediaSessionManager.addOnActiveSessionsChangedListener(
                ::activeSessionsListener,
                ComponentName(this, NotificationListener::class.java)
            )

            // Load all settings once and cache them
            reloadSettings()

            val initialMediaSessions = mediaSessionManager.getActiveSessions(
                ComponentName(this, NotificationListener::class.java)
            )
            var mediaActiveInitially = false
            if (useMediaRpc && initialMediaSessions.isNotEmpty()) {
                val firstActiveMediaController = initialMediaSessions.firstOrNull {
                    enabledRpcApps.contains(it.packageName)
                }
                if (firstActiveMediaController != null) {
                    mediaActiveInitially = true
                    activeSessionsListener(listOf(firstActiveMediaController), false)
                }
            }

            if (!mediaActiveInitially) {
                if (useAppsRpc) {
                    startAppDetectionCoroutine()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /** Reload all preference values once and cache them. */
    private fun reloadSettings() {
        templateName = Prefs[Prefs.RPC_TEMPLATE_NAME, TemplateKeys.APP_NAME]
        templateDetails = Prefs[Prefs.RPC_TEMPLATE_DETAILS, TemplateKeys.MEDIA_TITLE]
        templateState = Prefs[Prefs.RPC_TEMPLATE_STATE, TemplateKeys.MEDIA_ARTIST]
        useAppsRpc = Prefs[Prefs.RPC_USE_APPS_RPC, true]
        useMediaRpc = Prefs[Prefs.RPC_USE_MEDIA_RPC, true]
        appActivityTypes = Prefs.getAppActivityTypes()
        enabledRpcApps = try {
            Json.decodeFromString(Prefs[Prefs.ENABLED_RPC_APPS, "[]"])
        } catch (_: Exception) {
            emptyList()
        }
        cachedHideOnPause = Prefs[Prefs.RPC_HIDE_ON_PAUSE, false]
        cachedShowCoverArt = Prefs[Prefs.RPC_SHOW_COVER_ART, true]
        cachedShowAppIcon = Prefs[Prefs.RPC_SHOW_APP_ICON, false]
        cachedShowAppAndPauseIcon = Prefs[Prefs.RPC_SHOW_APP_AND_PAUSE_ICON, false]
        cachedShowPlaybackState = Prefs[Prefs.RPC_SHOW_PLAYBACK_STATE, true]
        cachedShowAlbumName = Prefs[Prefs.RPC_SHOW_ALBUM_NAME, true]
        cachedEnableTimestamps = Prefs[Prefs.RPC_ENABLE_TIMESTAMPS, true]
        cachedCustomStatus = Prefs[Prefs.CUSTOM_ACTIVITY_STATUS, "dnd"]
        cachedUseRpcButtons = Prefs[Prefs.USE_RPC_BUTTONS, false]
        cachedRpcButtons = try {
            Json.decodeFromString<RpcButtons>(Prefs[Prefs.RPC_BUTTONS_DATA, "{}"])
        } catch (_: Exception) {
            RpcButtons()
        }
        cachedUseLowResIcon = Prefs[Prefs.RPC_USE_LOW_RES_ICON, false]
    }

    private fun startAppDetectionCoroutine() {
        logger.d(TAG, "Starting app detection coroutine")

        // Cancel any existing app detection to prevent multiple concurrent loops
        appDetectionJob?.cancel()

        var currentPackageName = ""
        val startTimestamps = Timestamps(start = System.currentTimeMillis())

        appDetectionJob = scope.launch {
            while (isActive) {
                val currentApp = getCurrentlyRunningApp()

                if (
                    currentApp.name.isNotEmpty() &&
                    currentApp.packageName != currentPackageName &&
                    enabledRpcApps.contains(currentApp.packageName)
                ) {
                    currentPackageName = currentApp.packageName
                    updatePresence(appInfo = currentApp.copy(time = startTimestamps))
                } else if (currentApp.name.isNotEmpty() && currentApp.packageName != currentPackageName) {
                    currentPackageName = ""
                    if (!isMediaSessionActive || !useMediaRpc) {
                        updatePresence(CommonRpc())
                    }
                }
                delay(5000)
            }
        }
    }

    private suspend fun updatePresence(
        appInfo: CommonRpc? = null,
        richMediaInfo: RichMediaMetadata? = null,
        rawMediaMetadata: MediaMetadata? = null,
    ) {
        val rpcButtons = cachedRpcButtons

        val finalName: String?
        val finalDetails: String?
        val finalState: String?
        var finalLargeImage: RpcImage?
        var finalSmallImage: RpcImage?
        var finalLargeText: String?
        var finalSmallText: String?
        var finalTimestamps: Timestamps?
        var effectivePackageName: String?

        // Hide media on pause if enabled
        if (richMediaInfo != null &&
            cachedHideOnPause &&
            (richMediaInfo.playbackState == PlaybackState.STATE_PAUSED ||
                    richMediaInfo.playbackState == PlaybackState.STATE_STOPPED)
        ) {
            if (useAppsRpc) {
                startAppDetectionCoroutine()
            } else if (rpcRPC.isRpcRunning()) {
                rpcRPC.closeRPC()
                notificationManager.notify(
                    Constants.NOTIFICATION_ID, notificationBuilder
                        .setContentTitle(getString(R.string.service_enabled))
                        .setContentText(getString(R.string.idling_notification))
                        .build()
                )
            }
            return
        }

        val currentContextIsMedia =
            useMediaRpc && richMediaInfo != null && richMediaInfo.appName != null
        val currentContextIsApp = useAppsRpc && appInfo != null && appInfo.name.isNotEmpty()

        val processor = TemplateProcessor(
            mediaMetadata = rawMediaMetadata,
            mediaPlayerAppName = richMediaInfo?.appName,
            mediaPlayerPackageName = richMediaInfo?.packageName,
            detectedAppInfo = appInfo
        )

        if (currentContextIsMedia) {
            effectivePackageName = richMediaInfo?.packageName

            logger.d(TAG, "Processing Rich Media Context")
            finalName = processor.process(templateName) ?: richMediaInfo?.appName
            finalDetails = processor.process(templateDetails) ?: richMediaInfo?.title
            finalState = processor.process(templateState) ?: richMediaInfo?.artist

            finalLargeImage = when {
                cachedShowCoverArt -> richMediaInfo?.coverArt
                cachedShowAppIcon -> richMediaInfo?.appIcon
                else -> null
            }

            finalSmallImage = when {
                cachedShowAppAndPauseIcon -> {
                    if (richMediaInfo?.playbackState == PlaybackState.STATE_PAUSED || richMediaInfo?.playbackState == PlaybackState.STATE_STOPPED) {
                        richMediaInfo?.playbackStateIcon
                    } else {
                        richMediaInfo?.appIcon
                    }
                }

                cachedShowPlaybackState ->
                    richMediaInfo?.playbackStateIcon

                cachedShowAppIcon && finalLargeImage != richMediaInfo?.appIcon ->
                    richMediaInfo?.appIcon

                else -> null
            }

            finalLargeText = if (cachedShowAlbumName) richMediaInfo?.album else null
            finalSmallText =
                if (finalSmallImage == richMediaInfo?.appIcon) richMediaInfo?.appName else null

            finalTimestamps = if (cachedEnableTimestamps)
                richMediaInfo?.timestamps else null

        } else if (currentContextIsApp) {
            effectivePackageName = appInfo?.packageName
            logger.d(TAG, "Processing App Context")
            finalName = processor.process(templateName) ?: appInfo?.name
            finalDetails = processor.process(templateDetails) ?: appInfo?.details
            finalState = processor.process(templateState) ?: appInfo?.state

            finalLargeImage = appInfo?.largeImage
            finalSmallImage = appInfo?.smallImage
            finalLargeText = appInfo?.largeText
            finalSmallText = appInfo?.smallText
            finalTimestamps =
                if (cachedEnableTimestamps) appInfo?.time else null
        } else {
            logger.d(TAG, "No active context (App or Media) or both disabled.")
            if (rpcRPC.isRpcRunning()) {
                rpcRPC.closeRPC()
            }
            return
        }

        val rpcDataIsEmpty =
            finalName.isNullOrEmpty() && finalDetails.isNullOrEmpty() && finalState.isNullOrEmpty()

        if (rpcRPC.isRpcRunning()) {
            if (rpcDataIsEmpty) {
                logger.d(TAG, "Calculated RPC data is empty, stopping RPC.")
                rpcRPC.closeRPC()
                notificationManager.notify(
                    Constants.NOTIFICATION_ID, notificationBuilder
                        .setContentTitle(getString(R.string.service_enabled))
                        .setContentText(getString(R.string.idling_notification))
                        .build()
                )
                return
            }

            rpcRPC.updateRPC(
                commonRpc = CommonRpc(
                    name = finalName ?: "",
                    type = appActivityTypes[effectivePackageName] ?: 0,
                    details = finalDetails,
                    state = finalState,
                    largeImage = finalLargeImage,
                    smallImage = finalSmallImage,
                    largeText = finalLargeText,
                    smallText = finalSmallText,
                    time = finalTimestamps,
                    packageName = effectivePackageName ?: ""
                ),
                enableTimestamps = cachedEnableTimestamps
            )

        } else {
            if (rpcDataIsEmpty) {
                logger.d(TAG, "Calculated RPC data is empty, not starting RPC.")

                notificationManager.notify(
                    Constants.NOTIFICATION_ID, notificationBuilder
                        .setContentTitle(getString(R.string.service_enabled))
                        .setContentText(getString(R.string.idling_notification))
                        .build()
                )
                return
            }

            rpcRPC.apply {
                setName(finalName)
                setType(appActivityTypes[effectivePackageName] ?: 0)
                setStatus(cachedCustomStatus)
                setDetails(finalDetails)
                setState(finalState)
                setStartTimestamps(finalTimestamps?.start)
                setStopTimestamps(finalTimestamps?.end)
                setLargeImage(finalLargeImage, finalLargeText)
                setSmallImage(finalSmallImage, finalSmallText)
                if (cachedUseRpcButtons) {
                    with(rpcButtons) {
                        setButton1(button1.takeIf { it.isNotEmpty() })
                        setButton1URL(button1Url.takeIf { it.isNotEmpty() })
                        setButton2(button2.takeIf { it.isNotEmpty() })
                        setButton2URL(button2Url.takeIf { it.isNotEmpty() })
                    }
                }
                build()
            }
        }

        val notifTitle = finalName.takeIf { !it.isNullOrEmpty() } ?: getString(R.string.app_name)
        val notifText = finalDetails ?: finalState

        notificationManager.notify(
            Constants.NOTIFICATION_ID, notificationBuilder
                .setContentTitle(notifTitle)
                .setContentText(notifText)
                .setLargeIcon(rpcImage = finalLargeImage, context = this@Rpc)
                .build()
        )
    }

    private fun activeSessionsListener(
        mediaSessions: List<MediaController>?,
        isEvent: Boolean = true,
    ) {
        if (!useMediaRpc) {
            logger.i(TAG, "Media part of RPC is disabled.")
            if (useAppsRpc && !isMediaSessionActive) {
                scope.coroutineContext.cancelChildren()
                startAppDetectionCoroutine()
            } else if (!useAppsRpc) {
                scope.launch {
                    updatePresence(
                        appInfo = null,
                        richMediaInfo = null,
                        rawMediaMetadata = null
                    )
                }
            }
            return
        }
        logger.d(TAG, "Active media sessions changed")
        // Use non-blocking delay instead of runBlocking to avoid ANR
        if (isEvent) {
            scope.coroutineContext.cancelChildren()
            scope.launch {
                delay(1500)
                processMediaSessionChange(mediaSessions)
            }
            return
        }

        currentMediaController?.unregisterCallback(mediaControllerCallback)
        currentMediaController = null

        if (mediaSessions?.isNotEmpty() == true) {
            currentMediaController = mediaSessions.firstOrNull {
                enabledRpcApps.contains(it.packageName)
            }
            currentMediaController?.registerCallback(mediaControllerCallback)
        }

        scope.coroutineContext.cancelChildren()
        scope.launch {
            val richMediaData = getCurrentPlayingMediaAll()
            isMediaSessionActive = richMediaData.appName != null && currentMediaController != null

            if (isMediaSessionActive) {
                updatePresence(
                    richMediaInfo = richMediaData,
                    rawMediaMetadata = currentMediaController?.metadata
                )
            } else {
                if (useAppsRpc) {
                    startAppDetectionCoroutine()
                } else {
                    updatePresence(appInfo = null, richMediaInfo = null, rawMediaMetadata = null)
                }
            }
        }
    }

    /** Processes media session changes after the debounce delay (called from coroutine). */
    private suspend fun processMediaSessionChange(mediaSessions: List<MediaController>?) {
        currentMediaController?.unregisterCallback(mediaControllerCallback)
        currentMediaController = null

        if (mediaSessions?.isNotEmpty() == true) {
            currentMediaController = mediaSessions.firstOrNull {
                enabledRpcApps.contains(it.packageName)
            }
            currentMediaController?.registerCallback(mediaControllerCallback)
        }

        val richMediaData = getCurrentPlayingMediaAll()
        isMediaSessionActive = richMediaData.appName != null && currentMediaController != null

        if (isMediaSessionActive) {
            updatePresence(
                richMediaInfo = richMediaData,
                rawMediaMetadata = currentMediaController?.metadata
            )
        } else {
            if (useAppsRpc) {
                startAppDetectionCoroutine()
            } else {
                updatePresence(appInfo = null, richMediaInfo = null, rawMediaMetadata = null)
            }
        }
    }

    private inner class MediaControllerCallback : MediaController.Callback() {
        private fun handleMediaUpdate() {
            if (!useMediaRpc) return

            scope.coroutineContext.cancelChildren()
            scope.launch {
                delay(1000)
                val richMediaData = getCurrentPlayingMediaAll()
                isMediaSessionActive =
                    richMediaData.appName != null && currentMediaController != null

                if (isMediaSessionActive) {
                    updatePresence(
                        richMediaInfo = richMediaData,
                        rawMediaMetadata = currentMediaController?.metadata
                    )
                } else {
                    if (useAppsRpc) {
                        startAppDetectionCoroutine()
                    } else {
                        updatePresence(
                            appInfo = null,
                            richMediaInfo = null,
                            rawMediaMetadata = null
                        )
                    }
                }
            }
        }

        override fun onPlaybackStateChanged(state: PlaybackState?) {
            super.onPlaybackStateChanged(state)
            logger.d(TAG, "MediaControllerCallback: onPlaybackStateChanged")
            handleMediaUpdate()
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            super.onMetadataChanged(metadata)
            logger.d(TAG, "MediaControllerCallback: onMetadataChanged")
            handleMediaUpdate()
        }

        override fun onSessionDestroyed() {
            super.onSessionDestroyed()
            logger.d(TAG, "MediaControllerCallback: onSessionDestroyed")
            currentMediaController?.unregisterCallback(this)
            currentMediaController = null
            isMediaSessionActive = false

            scope.coroutineContext.cancelChildren()
            scope.launch {
                if (useAppsRpc) {
                    startAppDetectionCoroutine()
                } else {
                    updatePresence(appInfo = null, richMediaInfo = null, rawMediaMetadata = null)
                }
            }
        }
    }

    override fun onDestroy() {
        AppUtils.isRpcServiceRunning = false
        mediaSessionManager.removeOnActiveSessionsChangedListener(::activeSessionsListener)
        currentMediaController?.unregisterCallback(mediaControllerCallback)
        appDetectionJob?.cancel()
        scope.cancel()
        rpcRPC.closeRPC()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}