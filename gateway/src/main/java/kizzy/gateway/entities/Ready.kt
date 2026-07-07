/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * Ready.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package kizzy.gateway.entities


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Ready(
    @SerialName("resume_gateway_url")
    val resumeGatewayUrl: String? = null,
    @SerialName("session_id")
    val sessionId: String? = null,
    @SerialName("user")
    val user: ReadyUser? = null,
)

@Serializable
data class ReadyUser(
    @SerialName("id")
    val id: String? = null,
    @SerialName("bio")
    val bio: String? = null,
    @SerialName("premium_type")
    val premiumType: Int? = null,
)
