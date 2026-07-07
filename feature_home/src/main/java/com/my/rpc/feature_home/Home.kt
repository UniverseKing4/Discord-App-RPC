/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * Home.kt is part of Rpc
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.rpc.feature_home

import android.content.ComponentName
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage

import com.my.rpc.domain.model.user.User
import com.my.rpc.feature_home.feature.Features
import com.my.rpc.feature_home.feature.HomeFeature
import com.my.rpc.feature_home.feature.ToolTipContent
import com.my.rpc.feature_rpc_base.services.RpcTileService
import com.my.rpc.feature_settings.SettingsDrawer
import com.my.rpc.resources.R
import com.my.rpc.ui.components.ChipSection
import com.my.rpc.ui.components.UpdateDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    features: List<HomeFeature>,
    user: User?,
    navigateToProfile: () -> Unit,
    navigateToStyleAndAppearance: () -> Unit,
    navigateToAbout: () -> Unit,
    navigateToRpcSettings: () -> Unit,
    navigateToLogsScreen: () -> Unit,
) {
    val ctx = LocalContext.current
    var homeItems by remember {
        mutableStateOf(features)
    }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            rememberTopAppBarState(),
            canScroll = { true })
    val isCollapsed = scrollBehavior.state.collapsedFraction > 0.55f

    OnLifecycleEvent { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            homeItems = features
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
                SettingsDrawer(
                    user = user,
                    showRpcQuickieRequestItem = !RpcTileService.tileAdded.value,
                    onRequestAddTile = {
                        val cn = ComponentName(ctx, RpcTileService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val sbm = ctx.getSystemService(android.app.StatusBarManager::class.java)
                            sbm.requestAddTileService(
                                    cn,
                                    ctx.getString(R.string.qs_tile_label),
                                    android.graphics.drawable.Icon.createWithResource(ctx, R.drawable.ic_tile_play),
                                    {}
                            ) {}
                        }
                    },
                    navigateToProfile = navigateToProfile,
                    navigateToStyleAndAppearance = navigateToStyleAndAppearance,
                    navigateToAbout = navigateToAbout,
                    navigateToRpcSettings = navigateToRpcSettings,
                    navigateToLogsScreen = navigateToLogsScreen
                )
            }
        }) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.welcome) + ", ${user?.globalName ?: user?.username ?: ""}",
                            style = if (isCollapsed) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineLarge,
                            maxLines = if (isCollapsed) 1 else Int.MAX_VALUE,
                            overflow = if (isCollapsed) androidx.compose.ui.text.style.TextOverflow.Ellipsis else androidx.compose.ui.text.style.TextOverflow.Clip,
                            modifier = Modifier.padding(end = if (isCollapsed) 0.dp else 12.dp)
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                        ) {
                            Icon(
                                Icons.Outlined.Menu, Icons.Outlined.Menu.name,
                            )
                        }
                    },
                    actions = {

                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { navigateToProfile() }) {
                            if (user != null) {
                                AsyncImage(
                                    model = user.getAvatarImage(),
                                    modifier = Modifier
                                        .size(52.dp)
                                        .border(
                                            2.dp,
                                            MaterialTheme.colorScheme.secondaryContainer,
                                            CircleShape,
                                        )
                                        .clip(CircleShape),
                                    placeholder = painterResource(R.drawable.error_avatar),
                                    contentDescription = user.username
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = Icons.Default.Person.name,
                                )
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item {
                    Features(homeItems) {
                        homeItems = homeItems.mapIndexed { j, item ->
                            if (it == j) {
                                item.copy(isChecked = !item.isChecked)
                            } else {
                                if (item.isChecked) {
                                    item.copy(isChecked = false)
                                } else {
                                    item
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun OnLifecycleEvent(onEvent: (owner: LifecycleOwner, event: Lifecycle.Event) -> Unit) {
    val eventHandler = rememberUpdatedState(onEvent)
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)

    DisposableEffect(lifecycleOwner.value) {
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer = LifecycleEventObserver { owner, event ->
            eventHandler.value(owner, event)
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    Home(
        features = fakeFeatures,
        user = fakeUser,
        navigateToProfile = { },
        navigateToStyleAndAppearance = { },
        navigateToAbout = { },
        navigateToRpcSettings = { }) {

    }
}

val fakeFeatures = listOf(
    HomeFeature(
        title = "RPC",
        icon = R.drawable.ic_dev_rpc,
        shape = RoundedCornerShape(20.dp, 44.dp, 20.dp, 44.dp),
        tooltipText = ToolTipContent.RPC_DOCS
    )
)

val fakeUser = User(
    accentColor = null,
    avatar = null,
    avatarDecoration = null,
    badges = null,
    banner = null,
    bannerColor = null,
    discriminator = "3050",
    id = null,
    publicFlags = null,
    username = "yzziK",
    special = null,
    verified = false,
    nitro = true,
    bio = "Hello 👋"
)