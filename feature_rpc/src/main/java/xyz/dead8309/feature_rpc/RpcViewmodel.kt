/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * RpcViewmodel.kt is part of Rpc
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package xyz.dead8309.feature_rpc

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.rpc.data.utils.getInstalledApps
import com.my.rpc.preference.Prefs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RpcViewmodel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        UiState(
            isAppsLoading = true,
            isAppsRpcPartEnabled = Prefs[Prefs.RPC_USE_APPS_RPC, true],
            isMediaRpcPartEnabled = Prefs[Prefs.RPC_USE_MEDIA_RPC, true],
            templateName = Prefs[Prefs.RPC_TEMPLATE_NAME, ""],
            templateDetails = Prefs[Prefs.RPC_TEMPLATE_DETAILS, ""],
            templateState = Prefs[Prefs.RPC_TEMPLATE_STATE, ""],
            showCoverArt = Prefs[Prefs.RPC_SHOW_COVER_ART, false],
            showAlbumName = Prefs[Prefs.RPC_SHOW_ALBUM_NAME, true],
            showAppIcon = Prefs[Prefs.RPC_SHOW_APP_ICON, false],
            showPlaybackState = Prefs[Prefs.RPC_SHOW_PLAYBACK_STATE, false],
            showAppAndPauseIcon = Prefs[Prefs.RPC_SHOW_APP_AND_PAUSE_ICON, false],
            enableTimestamps = Prefs[Prefs.RPC_ENABLE_TIMESTAMPS, false],
            hideOnPause = Prefs[Prefs.RPC_HIDE_ON_PAUSE, false],
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val appList = getInstalledApps(
                context = context,
                isEnabled = Prefs::isRpcAppEnabled
            )
            val enabledApps = appList.associate { it.pkg to it.isChecked }
            val savedActivityTypes = Prefs.getAppActivityTypes()
            val appActivityTypes = appList.associate { it.pkg to (savedActivityTypes[it.pkg] ?: 0) }
            _uiState.update {
                it.copy(
                    installedApps = appList,
                    enabledApps = enabledApps,
                    appActivityTypes = appActivityTypes,
                    isAppsLoading = false
                )
            }
        }
    }

    fun onEvent(event: UiEvent) {
        viewModelScope.launch {
            when (event) {
                is UiEvent.ToggleAppsRpcPart -> {
                    Prefs[Prefs.RPC_USE_APPS_RPC] = event.enabled
                    _uiState.update { it.copy(isAppsRpcPartEnabled = event.enabled) }
                }

                is UiEvent.ToggleMediaRpcPart -> {
                    Prefs[Prefs.RPC_USE_MEDIA_RPC] = event.enabled
                    _uiState.update { it.copy(isMediaRpcPartEnabled = event.enabled) }
                }

                is UiEvent.SetTemplateName -> {
                    Prefs[Prefs.RPC_TEMPLATE_NAME] = event.value
                    _uiState.update { it.copy(templateName = event.value) }
                }

                is UiEvent.SetTemplateDetails -> {
                    Prefs[Prefs.RPC_TEMPLATE_DETAILS] = event.value
                    _uiState.update { it.copy(templateDetails = event.value) }
                }

                is UiEvent.SetTemplateState -> {
                    Prefs[Prefs.RPC_TEMPLATE_STATE] = event.value
                    _uiState.update { it.copy(templateState = event.value) }
                }

                is UiEvent.ToggleAppEnabled -> {
                    val current = _uiState.value.enabledApps[event.packageName] ?: false
                    Prefs.saveRpcAppToPrefs(event.packageName)
                    _uiState.update { state ->
                        state.copy(
                            enabledApps = state.enabledApps.toMutableMap().apply {
                                this[event.packageName] = !current
                            }
                        )
                    }
                }

                is UiEvent.SetAppActivityType -> {
                    val typeInt = event.activityType
                    Prefs.saveAppActivityType(event.packageName, typeInt)
                    _uiState.update { state ->
                        state.copy(
                            appActivityTypes = state.appActivityTypes.toMutableMap().apply {
                                this[event.packageName] = typeInt
                            }
                        )
                    }
                }
                is UiEvent.ToggleShowCoverArt -> {
                    Prefs[Prefs.RPC_SHOW_COVER_ART] = event.isChecked
                    _uiState.update { it.copy(showCoverArt = event.isChecked) }
                }

                is UiEvent.ToggleShowAlbumName -> {
                    Prefs[Prefs.RPC_SHOW_ALBUM_NAME] = event.isChecked
                    _uiState.update { it.copy(showAlbumName = event.isChecked) }
                }

                is UiEvent.ToggleShowAppIcon -> {
                    Prefs[Prefs.RPC_SHOW_APP_ICON] = event.isChecked
                    _uiState.update {
                        it.copy(showAppIcon = event.isChecked)
                    }
                    if (event.isChecked) {
                        Prefs[Prefs.RPC_SHOW_PLAYBACK_STATE] = false
                        Prefs[Prefs.RPC_SHOW_APP_AND_PAUSE_ICON] = false
                        _uiState.update { it.copy(showPlaybackState = false, showAppAndPauseIcon = false) }
                    }
                }

                is UiEvent.ToggleShowPlaybackState -> {
                    Prefs[Prefs.RPC_SHOW_PLAYBACK_STATE] = event.enabled
                    _uiState.update {
                        it.copy(showPlaybackState = event.enabled)
                    }
                    if (event.enabled) {
                        Prefs[Prefs.RPC_SHOW_APP_ICON] = false
                        Prefs[Prefs.RPC_SHOW_APP_AND_PAUSE_ICON] = false
                        _uiState.update { it.copy(showAppIcon = false, showAppAndPauseIcon = false) }
                    }
                }

                is UiEvent.ToggleShowAppAndPauseIcon -> {
                    Prefs[Prefs.RPC_SHOW_APP_AND_PAUSE_ICON] = event.enabled
                    _uiState.update {
                        it.copy(showAppAndPauseIcon = event.enabled)
                    }
                    if (event.enabled) {
                        Prefs[Prefs.RPC_SHOW_APP_ICON] = false
                        Prefs[Prefs.RPC_SHOW_PLAYBACK_STATE] = false
                        _uiState.update { it.copy(showAppIcon = false, showPlaybackState = false) }
                    }
                }

                is UiEvent.ToggleEnableTimestamps -> {
                    Prefs[Prefs.RPC_ENABLE_TIMESTAMPS] = event.enabled
                    _uiState.update { it.copy(enableTimestamps = event.enabled) }
                }

                is UiEvent.ToggleHideOnPause -> {
                    Prefs[Prefs.RPC_HIDE_ON_PAUSE] = event.enabled
                    _uiState.update { it.copy(hideOnPause = event.enabled) }
                }
            }
        }
    }
}
