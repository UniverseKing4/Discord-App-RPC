/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * RpcScreen.kt is part of Rpc
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package xyz.dead8309.feature_rpc

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppsOutage
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AppSettingsAlt
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.my.rpc.data.rpc.TemplateKeys
import com.my.rpc.feature_rpc_base.AppUtils
import com.my.rpc.feature_rpc_base.services.Rpc
import com.my.rpc.resources.R
import com.my.rpc.ui.components.BackButton
import com.my.rpc.ui.components.RpcFieldWithCompletions
import com.my.rpc.ui.components.SettingItem
import com.my.rpc.ui.components.Subtitle
import com.my.rpc.ui.components.SwitchBar
import com.my.rpc.ui.components.preference.PreferenceSwitch
import com.my.rpc.ui.components.preference.PreferencesHint

private val completions = listOf(
    TemplateKeys.MEDIA_TITLE to R.string.completion_media_title,
    TemplateKeys.MEDIA_ARTIST to R.string.completion_media_artist,
    TemplateKeys.MEDIA_AUTHOR to R.string.completion_media_author,
    TemplateKeys.APP_NAME to R.string.completion_app_name,
)

@Composable
fun RpcSettingsContent(
    state: UiState,
    hasUsageAccess: Boolean,
    hasNotificationAccess: Boolean,
    navigateToAppSelection: () -> Unit,
    onEvent: (UiEvent) -> Unit,
) {
    val context = LocalContext.current
    var rpcRunning by remember { mutableStateOf(AppUtils.rpcRunning()) }

    Column(modifier = Modifier.fillMaxWidth()) {
        AnimatedVisibility(
            visible = !hasUsageAccess
        ) {
            PreferencesHint(
                title = stringResource(id = R.string.usage_access),
                description = stringResource(id = R.string.usage_access_desc),
                icon = Icons.Default.AppsOutage,
            ) {
                when (hasUsageAccess) {
                    false -> context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    else -> Unit
                }
            }
        }
        AnimatedVisibility(
            visible = !hasNotificationAccess
        ) {
            PreferencesHint(
                title = stringResource(id = R.string.permission_required),
                description = stringResource(id = R.string.request_for_notification_access),
                icon = Icons.Default.Warning,
            ) {
                if (!hasNotificationAccess) {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }
            }
        }

        SwitchBar(
            title = stringResource(id = R.string.enable_rpc),
            isChecked = rpcRunning,
            enabled = hasUsageAccess && hasNotificationAccess,
        ) {
            rpcRunning = !rpcRunning
            when (rpcRunning) {
                true -> {
                    context.startService(Intent(context, Rpc::class.java))
                }

                false -> context.stopService(Intent(context, Rpc::class.java))
            }
        }

        Column {
            Subtitle(text = stringResource(R.string.general_settings))
            
            PreferenceSwitch(
                title = stringResource(R.string.enable_appsRpc),
                description = stringResource(R.string.rpc_detect_apps),
                isChecked = state.isAppsRpcPartEnabled,
                onClick = {
                    onEvent(UiEvent.ToggleAppsRpcPart(!state.isAppsRpcPartEnabled))
                },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_apps),
                        modifier = Modifier
                            .padding(start = 8.dp, end = 16.dp)
                            .size(24.dp),
                        contentDescription = null
                    )
                },
            )
            
            PreferenceSwitch(
                title = stringResource(R.string.enable_mediaRpc),
                description = stringResource(R.string.rpc_detect_media),
                isChecked = state.isMediaRpcPartEnabled,
                onClick = {
                    onEvent(UiEvent.ToggleMediaRpcPart(!state.isMediaRpcPartEnabled))
                },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_media_rpc),
                        modifier = Modifier
                            .padding(start = 8.dp, end = 16.dp)
                            .size(24.dp),
                        contentDescription = null
                    )
                },
            )
            
            SettingItem(
                title = stringResource(R.string.app_preferences),
                description = stringResource(R.string.app_preferences_desc),
                icon = Icons.Outlined.AppSettingsAlt
            ) {
                navigateToAppSelection()
            }
            
            Subtitle(
                text = stringResource(R.string.main_mediaRpc)
            )
            
            PreferenceSwitch(
                title = stringResource(R.string.show_cover_art),
                description = stringResource(R.string.show_cover_art_desc),
                isChecked = state.showCoverArt,
                onClick = {
                    onEvent(UiEvent.ToggleShowCoverArt(!state.showCoverArt))
                },
                icon = Icons.Outlined.Image,
            )

            PreferenceSwitch(
                title = stringResource(R.string.show_album_name),
                description = stringResource(R.string.show_album_name_desc),
                isChecked = state.showAlbumName,
                onClick = {
                    onEvent(UiEvent.ToggleShowAlbumName(!state.showAlbumName))
                },
                icon = Icons.Outlined.Info,
            )
            
            PreferenceSwitch(
                title = stringResource(R.string.show_app_and_pause_icon),
                description = stringResource(R.string.show_app_and_pause_icon_desc),
                isChecked = state.showAppAndPauseIcon,
                onClick = {
                    onEvent(UiEvent.ToggleShowAppAndPauseIcon(!state.showAppAndPauseIcon))
                },
                icon = Icons.Default.PlayCircleOutline
            )
            
            PreferenceSwitch(
                title = stringResource(R.string.show_app_icon),
                isChecked = state.showAppIcon,
                onClick = {
                    onEvent(UiEvent.ToggleShowAppIcon(!state.showAppIcon))
                },
                icon = Icons.Outlined.Apps,
            )
            
            PreferenceSwitch(
                title = stringResource(R.string.show_playback_state),
                isChecked = state.showPlaybackState,
                onClick = {
                    onEvent(UiEvent.ToggleShowPlaybackState(!state.showPlaybackState))
                },
                icon = Icons.Default.PlayCircle
            )
            
            PreferenceSwitch(
                title = stringResource(R.string.enable_timestamps),
                isChecked = state.enableTimestamps,
                onClick = {
                    onEvent(UiEvent.ToggleEnableTimestamps(!state.enableTimestamps))
                },
                icon = Icons.Default.Timer
            )
            
            PreferenceSwitch(
                title = stringResource(R.string.hide_on_pause),
                isChecked = state.hideOnPause,
                onClick = {
                    onEvent(UiEvent.ToggleHideOnPause(!state.hideOnPause))
                },
                icon = Icons.Default.PauseCircle
            )
            
            Subtitle(text = stringResource(R.string.advance_settings))
            
            RpcFieldWithCompletions(
                value = state.templateName,
                label = R.string.activity_name,
                onValueChange = { onEvent(UiEvent.SetTemplateName(it)) },
                completionList = completions
            )
            
            RpcFieldWithCompletions(
                value = state.templateDetails,
                label = R.string.activity_details,
                onValueChange = { onEvent(UiEvent.SetTemplateDetails(it)) },
                completionList = completions
            )
            
            RpcFieldWithCompletions(
                value = state.templateState,
                label = R.string.activity_state,
                onValueChange = { onEvent(UiEvent.SetTemplateState(it)) },
                completionList = completions
            )
            
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                )
                Text(
                    text = stringResource(R.string.rpc_templates_note),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Light,
                )
            }
        }
    }
}


