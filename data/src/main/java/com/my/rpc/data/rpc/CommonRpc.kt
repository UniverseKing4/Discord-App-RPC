/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * CommonRpc.kt is part of Rpc
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.rpc.data.rpc

data class CommonRpc(
    val name: String = "",
    val type: Int? = null,
    val details: String? = "",
    val state: String? = "",
    val partyCurrentSize: Int? = null,
    val partyMaxSize: Int? = null,
    val largeImage: RpcImage? = null,
    val smallImage: RpcImage? = null,
    val largeText: String? = null,
    val smallText: String? = null,
    val time: Timestamps? = null,
    val packageName: String = "",
    val platform: String? = null
)
