/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * RpcTileService.kt is part of Rpc
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.rpc.feature_rpc_base.services

import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import androidx.appcompat.view.ContextThemeWrapper

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.my.rpc.feature_rpc_base.AppUtils
import com.my.rpc.preference.Prefs
import com.my.rpc.resources.R

class RpcTileService : TileService() {
    override fun onClick() {
        super.onClick()
        val ctx = this
        when (qsTile.state) {
            Tile.STATE_ACTIVE -> {
                ctx.stopService(Intent(ctx, Rpc::class.java))
                Toast.makeText(ctx, getString(R.string.stop_rpc_toast), Toast.LENGTH_SHORT).show()
            }
            Tile.STATE_INACTIVE -> {
                if (!isLocked) {
                    showDialog(createRpcChoosingDialog(ctx))
                } else {
                    val intent = Intent(ctx, DialogActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    ContextWrapper(ctx).startActivity(intent)
                }
            }
            else -> {}
        }
        updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onTileAdded() {
        super.onTileAdded()
        tileAdded = true
    }

    private fun createRpcChoosingDialog(ctx: Context): Dialog {
        val rpc = arrayOf(
            getString(R.string.main_rpc)
        )
        return MaterialAlertDialogBuilder(ContextThemeWrapper(ctx, com.my.rpc.feature_rpc_base.R.style.MyTileDialogTheme))
            .setTitle(getString(R.string.choose_rpc))
            .setSingleChoiceItems(rpc, -1) { dialog, which ->
                when (which) {
                    0 -> {
                        ctx.startForegroundService(Intent(ctx, Rpc::class.java))
                        Toast.makeText(ctx, getString(R.string.start_rpc_toast), Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
                dialog.dismiss()
            }
            .create()
    }

    private fun updateTile() {
        if (Prefs[Prefs.TOKEN, ""].isEmpty()) {
            qsTile.state = Tile.STATE_UNAVAILABLE
            qsTile.updateTile()
            return
        }
        when (AppUtils.rpcRunning()) {
            true -> {
                qsTile.state = Tile.STATE_ACTIVE
                qsTile.icon = Icon.createWithResource(this, R.drawable.ic_tile_stop)
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                    qsTile.subtitle = getSubtitle()
                }
            }
            false -> {
                qsTile.state = Tile.STATE_INACTIVE
                qsTile.icon = Icon.createWithResource(this, R.drawable.ic_tile_play)
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                    qsTile.subtitle = getString(R.string.off)
                }
            }
        }
        qsTile.updateTile()
    }

    private fun getSubtitle(): String {
        return getString(R.string.main_rpc)
    }

    companion object {
        @Volatile
        var tileAdded = false
    }
}

