/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * UiEvent.kt is part of Rpc
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package xyz.dead8309.feature_rpc

sealed interface UiEvent {
    data class ToggleAppsRpcPart(val enabled: Boolean) : UiEvent
    data class ToggleMediaRpcPart(val enabled: Boolean) : UiEvent
    data class SetTemplateName(val value: String) : UiEvent
    data class SetTemplateDetails(val value: String) : UiEvent
    data class SetTemplateState(val value: String) : UiEvent
    data class ToggleAppEnabled(val packageName: String) : UiEvent
    data class SetAppActivityType(val packageName: String, val activityType: Int) : UiEvent
    data class ToggleShowCoverArt(val isChecked: Boolean) : UiEvent
    data class ToggleShowAlbumName(val isChecked: Boolean) : UiEvent
    data class ToggleShowAppIcon(val isChecked: Boolean) : UiEvent
    data class ToggleShowPlaybackState(val enabled: Boolean) : UiEvent
    data class ToggleShowAppAndPauseIcon(val enabled: Boolean) : UiEvent
    data class ToggleEnableTimestamps(val enabled: Boolean) : UiEvent
    data class ToggleHideOnPause(val enabled: Boolean) : UiEvent
}
