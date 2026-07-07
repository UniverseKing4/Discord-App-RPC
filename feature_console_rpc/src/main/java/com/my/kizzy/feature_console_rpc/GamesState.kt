/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * GamesState.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.kizzy.feature_console_rpc

import androidx.compose.runtime.Stable
import com.my.kizzy.domain.model.Game

@Stable
sealed interface GamesState {
    object Loading: GamesState
    class Success(val games: List<Game>): GamesState
    class Error(val error: String): GamesState
}