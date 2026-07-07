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

package com.my.rpc

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

import com.my.rpc.feature_about.about.About
import com.my.rpc.feature_about.about.Credits
import com.my.rpc.feature_about.about.CreditsScreenViewModel
import com.my.rpc.feature_home.Home

import com.my.rpc.feature_home.feature.homeFeaturesProvider
import com.my.rpc.feature_logs.LogScreen
import com.my.rpc.feature_logs.LogsViewModel
import com.my.rpc.feature_profile.ui.login.LoginScreen
import com.my.rpc.feature_profile.ui.user.UserScreen
import com.my.rpc.feature_profile.ui.user.UserViewModel
import com.my.rpc.feature_rpc_base.AppUtils
import com.my.rpc.feature_settings.rpc_settings.RpcSettings
import com.my.rpc.feature_settings.style.Appearance
import com.my.rpc.feature_settings.style.DarkThemePreferences
import com.my.rpc.feature_startup.StartUp
import com.my.rpc.navigation.Routes
import com.my.rpc.navigation.animatedComposable
import com.my.rpc.preference.Prefs
import xyz.dead8309.feature_rpc.RpcScreen
import xyz.dead8309.feature_rpc.RpcViewmodel
import xyz.dead8309.feature_rpc.apps.RpcAppsScreen

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun ComponentActivity.Rpc(
    usageAccessStatus: MutableState<Boolean>,
    notificationListenerAccess: MutableState<Boolean>,
) {
    Scaffold()
    {
        val navController = rememberAnimatedNavController()
        AnimatedNavHost(
            navController = navController,
            startDestination = if (Prefs[Prefs.IS_FIRST_LAUNCHED, true]) Routes.SETUP else Routes.HOME
        ) {
            animatedComposable(Routes.SETUP) {
                StartUp(
                    usageAccessStatus = usageAccessStatus,
                    mediaControlStatus = notificationListenerAccess,
                    navigateToHome = {
                        Prefs[Prefs.IS_FIRST_LAUNCHED] = false
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.SETUP) { inclusive = true }
                        }
                    },
                    navigateToLogin = {
                        navController.navigate(Routes.PROFILE)
                    })
            }
            animatedComposable(Routes.HOME) {
                val user = Prefs.getUser()
                Home(
                    features = homeFeaturesProvider(
                        navigateTo = { navController.navigate(it) },
                        hasUsageAccess = usageAccessStatus,
                        hasNotificationAccess = notificationListenerAccess,
                        userVerified = user?.verified == true
                    ),
                    user = user,
                    navigateToProfile = {
                        navController.navigate(Routes.PROFILE)
                    },
                    navigateToStyleAndAppearance = {
                        navController.navigate(Routes.STYLE_AND_APPEARANCE)
                    },
                    navigateToAbout = {
                        navController.navigate(Routes.ABOUT)
                    },
                    navigateToRpcSettings = {
                        navController.navigate(Routes.RPC_SETTINGS)
                    },
                    navigateToLogsScreen = {
                        navController.navigate(Routes.LOGS_SCREEN)
                    }
                )
            }

            animatedComposable(Routes.PROFILE) {
                var loggedIn by remember {
                    mutableStateOf(Prefs[Prefs.TOKEN, ""].isNotEmpty())
                }
                if (loggedIn) {
                    val viewModel by viewModels<UserViewModel>()
                    UserScreen(
                        state = viewModel.state.value,
                        onBackPressed = navController::popBackStack
                    )
                } else {
                    LoginScreen(
                        onBackPressed = navController::popBackStack,
                        onCompleted = {
                            loggedIn = true
                        },
                    )
                }
            }

            animatedComposable(Routes.STYLE_AND_APPEARANCE) {
                Appearance(onBackPressed = {
                    navController.popBackStack()
                }) {
                    navController.navigate(Routes.DARK_THEME)
                }
            }
            animatedComposable(Routes.DARK_THEME) {
                DarkThemePreferences {
                    navController.popBackStack()
                }
            }
            animatedComposable(Routes.RPC_SETTINGS) {
                RpcSettings { navController.popBackStack() }
            }
            animatedComposable(Routes.LOGS_SCREEN) {
                val viewModel by viewModels<LogsViewModel>()
                LogScreen(viewModel)
            }
            animatedComposable(Routes.ABOUT) {
                About(
                    onBackPressed = {
                        navController.popBackStack()
                    },
                    navigateToCredits = {
                        navController.navigate(Routes.CREDITS)
                    }
                )
            }
            animatedComposable(Routes.CREDITS) {
                val viewModel by viewModels<CreditsScreenViewModel>()
                Credits(
                    state = viewModel.creditScreenState.collectAsState().value,
                    onBackPressed = {
                        navController.popBackStack()
                    }
                )
            }

            val rpcViewModel by lazy {
                viewModels<RpcViewmodel>()
            }

            animatedComposable(Routes.RPC) {
                RpcScreen(
                    state = rpcViewModel.value.uiState.collectAsState().value,
                    onEvent = rpcViewModel.value::onEvent,
                    onBackPressed = { navController.popBackStack() },
                    hasUsageAccess = usageAccessStatus.value,
                    hasNotificationAccess = notificationListenerAccess.value,
                    navigateToAppSelection = {
                        navController.navigate(Routes.RPC_APPS)
                    },
                )
            }

            animatedComposable(Routes.RPC_APPS) {
                RpcAppsScreen(
                    onBackPressed = { navController.popBackStack() },
                    state = rpcViewModel.value.uiState.collectAsState().value,
                    onEvent = rpcViewModel.value::onEvent,
                )
            }
        }
    }
}