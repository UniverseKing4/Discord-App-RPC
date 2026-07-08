/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * RpcRPC.kt is part of Rpc
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.rpc.data.rpc

import com.my.rpc.domain.interfaces.Logger
import com.my.rpc.domain.repository.RpcRepository
import com.my.rpc.preference.Prefs
import com.my.rpc.preference.Prefs.CUSTOM_ACTIVITY_TYPE
import rpc.gateway.DiscordWebSocket
import rpc.gateway.entities.presence.Activity
import rpc.gateway.entities.presence.Assets
import rpc.gateway.entities.presence.Metadata
import rpc.gateway.entities.presence.Party
import rpc.gateway.entities.presence.Presence
import rpc.gateway.entities.presence.Timestamps


class RpcRPC(
    private val token: String,
    private val rpcRepository: RpcRepository,
    private val discordWebSocket: DiscordWebSocket,
    private val logger: Logger
) {
    private lateinit var presence: Presence
    private var activityName: String? = null
    private var applicationIdNumber = Prefs[Prefs.CUSTOM_ACTIVITY_APPLICATION_ID, Constants.APPLICATION_ID]
    private var details: String? = null
    private var state: String? = null
    private var party: Party? = null
    private var largeImage: RpcImage? = null
    private var smallImage: RpcImage? = null
    private var largeText: String? = null
    private var smallText: String? = null
    private var status: String? = null
    private var startTimestamps: Long? = null
    private var stopTimestamps: Long? = null
    private var type: Int = 0
    private var platform: String? = null
    private val buttons = ArrayList<String>(2)
    private val buttonUrl = ArrayList<String>(2)
    private var url: String? = null

    fun closeRPC() {
        discordWebSocket.close()
    }

    fun isRpcRunning(): Boolean {
        return discordWebSocket.isWebSocketConnected()
    }

    /**
     * Use Regex to check if token are valid
     * @return true if token is valid else false
     *
     * source: [#token-structure](https://gist.github.com/aydynx/5d29e903417354fd25641b98efc9d437#token-structure)
     */
    private fun isUserTokenValid(): Boolean {
        /*val regex = Regex(
            "[a-z\\d]{24}\\.[a-z\\d]{6}\\.([\\w-]{107}|[\\w-]{38}|[\\w-]{27})|mfa\\.[\\w-]{84}",
            RegexOption.IGNORE_CASE
        )
        return regex.matches(token)*/
        return token.isNotBlank()
    }

    /**
     * Activity Name of Rpc
     *
     * @param activity_name
     * @return
     */
    fun setName(activity_name: String?): RpcRPC {
        this.activityName = activity_name
        return this
    }

    /**
     * Details of Rpc
     *
     * @param details
     * @return
     */
    fun setDetails(details: String?): RpcRPC {
        this.details = details
        return this
    }

    /**
     * Rpc State
     *
     * @param state
     * @return
     */
    fun setState(state: String?): RpcRPC {
        this.state = state
        return this
    }

    /**
     * Party of Rpc
     *
     * @param current Current party size
     * @param max Max party size
     * @return
     */
    fun setPartySize(current: Int?, max: Int?): RpcRPC {
        if (current != null && max != null) {
            this.party = Party(
                id = "rpc",
                size = arrayOf(current, max)
            )
        }
        return this
    }

    /**
     * Large image on rpc
     * How to get Image ?
     * Upload image to any discord chat and copy its media link it should look like "https://media.discordapp.net/attachments/90202992002/xyz.png" now just use the image link from attachments part
     * so it would look like: .setLargeImage("attachments/90202992002/xyz.png")
     * @param large_image
     * @return
     */
    fun setLargeImage(large_image: RpcImage?, large_text: String? = null): RpcRPC {
        this.largeText = large_text.takeIf { !it.isNullOrEmpty() }
        this.largeImage = large_image
        return this
    }

    /**
     * Small image on Rpc
     *
     * @param small_image
     * @return
     */
    fun setSmallImage(small_image: RpcImage?, small_text: String? = null): RpcRPC {
        this.smallText = small_text.takeIf { !it.isNullOrEmpty() }
        this.smallImage = small_image
        return this
    }

    /**
     * start timestamps
     *
     * @param start_timestamps
     * @return
     */
    fun setStartTimestamps(start_timestamps: Long?): RpcRPC {
        this.startTimestamps = start_timestamps
        return this
    }

    /**
     * stop timestamps
     *
     * @param stop_timestamps
     * @return
     */
    fun setStopTimestamps(stop_timestamps: Long?): RpcRPC {
        this.stopTimestamps = stop_timestamps
        return this
    }

    /**
     * Activity Types
     * 0: Playing
     * 1: Streaming
     * 2: Listening
     * 3: Watching
     * 5: Competing
     *
     * @param type
     * @return
     */

    fun setType(type: Int): RpcRPC {
        this.type = if (type in 0..3 || type == 5) type else 0
        return this
    }

    /** Status type for profile online,idle,dnd
     *
     * @param status
     * @return
     */
    fun setStatus(status: String?): RpcRPC {
        this.status = status
        return this
    }

    /** Platform type for profile xbox,playstation,pc,ios,android etc.
     *
     * @param platform
     * @return
     */
    fun setPlatform(platform: String?): RpcRPC {
        this.platform = platform
        return this
    }

    /**
     * Button1 text
     * @param button1_Text
     * @return
     */
    fun setButton1(button1_Text: String?): RpcRPC {
        buttons.clear()
        buttonUrl.clear()
        button1_Text?.let { buttons.add(it) }
        return this
    }

    /**
     * Button2 text
     * @param button2_text
     * @return
     */
    fun setButton2(button2_text: String?): RpcRPC {
        button2_text?.let { buttons.add(it) }
        return this
    }

    /**
     * Button1 url
     * @param url
     * @return
     */
    fun setButton1URL(url: String?): RpcRPC {
        url?.let { buttonUrl.add(it) }
        return this
    }

    /**
     * Button2 url
     * @param url
     * @return
     */
    fun setButton2URL(url: String?): RpcRPC {
        url?.let { buttonUrl.add(it) }
        return this
    }
    /**
     * Streaming Url
     * @param url The streaming type currently only supports Twitch and YouTube.
     * Only https://twitch.tv/ and https://youtube.com/ urls will work
     */
    fun setStreamUrl(url: String?): RpcRPC {
        this.url = url
        return this
    }

    private fun String.sanitize(): String {
        return if (this.length > 128) {
            this.substring(0, 128)
        } else {
            this
        }
    }

    suspend fun build() {
        presence = Presence(
            activities = listOf(
                Activity(
                    name = activityName,
                    state = state?.sanitize(),
                    details = details?.sanitize(),
                    party = party.takeIf { party != null },
                    type = type,
                    platform = platform?.sanitize(),
                    timestamps = Timestamps(
                        start = startTimestamps,
                        end = stopTimestamps
                    ).takeIf { startTimestamps != null || stopTimestamps != null },
                    assets = Assets(
                        largeImage = largeImage?.resolveImage(rpcRepository),
                        smallImage = smallImage?.resolveImage(rpcRepository),
                        largeText = largeText?.sanitize(),
                        smallText = smallText?.sanitize()
                    ).takeIf { largeImage != null || smallImage != null },
                    buttons = buttons.toList().takeIf { it.isNotEmpty() },
                    metadata = Metadata(buttonUrls = buttonUrl.toList()).takeIf { buttonUrl.isNotEmpty() },
                    applicationId = applicationIdNumber.takeIf { it.isNotEmpty() } ?: Constants.APPLICATION_ID,
                    url = url
                )
            ),
            afk = true,
            since = startTimestamps ?: System.currentTimeMillis(),
            status = status
        )
        connectToWebSocket()
    }
    private suspend fun connectToWebSocket() {
        if (!isUserTokenValid()) {
            logger.e(
                tag = "RpcRPC",
                event = "Token Seems to be invalid, Please Login if you haven't"
            )
            return
        }
        discordWebSocket.connect()
        discordWebSocket.sendActivity(presence)
    }

    suspend fun updateRPC(commonRpc: CommonRpc, enableTimestamps: Boolean? = true) {
        if (!discordWebSocket.isActive) return
        val time = if (commonRpc.time != null) {
            Timestamps(end = commonRpc.time.end, start = commonRpc.time.start)
        } else {
            null
        }
        if (commonRpc.partyCurrentSize != null && commonRpc.partyMaxSize != null)
            Party(id = "rpc", size = arrayOf(commonRpc.partyCurrentSize, commonRpc.partyMaxSize)).also { party = it }
        discordWebSocket.sendActivity(
            Presence(
                activities = listOf(
                    Activity(
                        name = commonRpc.name,
                        details = commonRpc.details?.takeIf { it.isNotEmpty() }?.sanitize(),
                        state = commonRpc.state?.takeIf { it.isNotEmpty() }?.sanitize(),
                        type = commonRpc.type ?: Prefs[CUSTOM_ACTIVITY_TYPE, 0],
                        platform = commonRpc.platform?.sanitize(),
                        timestamps = time.takeIf { enableTimestamps == true },
                        assets = Assets(
                                largeImage = commonRpc.largeImage?.resolveImage(rpcRepository),
                                smallImage = commonRpc.smallImage?.resolveImage(rpcRepository),
                                largeText = commonRpc.largeText?.sanitize(),
                                smallText = commonRpc.smallText?.sanitize()
                            ).takeIf { commonRpc.largeImage != null || commonRpc.smallImage != null },
                        party = party.takeIf { party != null },
                        buttons = buttons.toList().takeIf { it.isNotEmpty() },
                        metadata = Metadata(buttonUrls = buttonUrl.toList()).takeIf { buttonUrl.isNotEmpty() },
                        applicationId = applicationIdNumber.takeIf { it.isNotEmpty() } ?: Constants.APPLICATION_ID

                    )
                ),
                afk = true,
                since = startTimestamps,
                status = this.status
            )
        )
    }
}
