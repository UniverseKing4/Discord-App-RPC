/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * AppUtils.kt is part of Rpc
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.rpc.feature_rpc_base

import android.content.Context

import javax.inject.Singleton

@Singleton
object AppUtils {
    /** Reliable service-running flag — set by Rpc service itself. */
    @Volatile
    var isRpcServiceRunning = false

    fun init(context: Context) {
        // Retained for backward compatibility
    }

    fun rpcRunning(): Boolean {
        return isRpcServiceRunning
    }
}