/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * FeaturesProvider.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.kizzy.feature_home.feature

import android.content.Intent
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.my.kizzy.feature_rpc_base.AppUtils
import com.my.kizzy.feature_rpc_base.services.AppDetectionService
import com.my.kizzy.feature_rpc_base.services.CustomRpcService
import com.my.kizzy.feature_rpc_base.services.ExperimentalRpc
import com.my.kizzy.feature_rpc_base.services.MediaRpcService
import com.my.kizzy.navigation.Routes
import com.my.kizzy.preference.Prefs
import com.my.kizzy.resources.R

@Composable
fun homeFeaturesProvider(
    navigateTo: (String) -> Unit,
    hasUsageAccess: MutableState<Boolean>,
    hasNotificationAccess: MutableState<Boolean>,
    userVerified: Boolean,
): List<HomeFeature> {
    val ctx = LocalContext.current
    return listOf(
        HomeFeature(
            title = "RPC option feature",
            icon = R.drawable.ic_dev_rpc,
            route = Routes.EXPERIMENTAL_RPC,
            onClick = { navigateTo(it) },
            isChecked = AppUtils.experimentalRpcRunning(),
            onCheckedChange = {
                if (it) {
                    ctx.stopService(Intent(ctx, MediaRpcService::class.java))
                    ctx.stopService(Intent(ctx, CustomRpcService::class.java))
                    ctx.stopService(Intent(ctx, AppDetectionService::class.java))
                    ctx.startService(Intent(ctx, ExperimentalRpc::class.java))
                } else
                    ctx.stopService(Intent(ctx, ExperimentalRpc::class.java))
            },
            shape = RoundedCornerShape(20.dp, 44.dp, 20.dp, 44.dp),
            showSwitch = hasUsageAccess.value && hasNotificationAccess.value && userVerified,
            tooltipText = stringResource(id = R.string.main_experimentalRpc_details),
            featureDocsLink = ToolTipContent.EXPERIMENTAL_RPC_DOCS_LINK
        )
    )
}
