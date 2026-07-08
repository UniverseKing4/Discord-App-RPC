/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * ShortcutsActivity.kt is part of Rpc
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.rpc.feature_rpc_base.services

import android.app.Activity
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.widget.Toast
import com.my.rpc.resources.R

class ShortcutsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (SDK_INT >= 27) setShowWhenLocked(true)
        handleShortcut()
    }

    private fun handleShortcut() {
        intent?.action?.let {
            when (it) {
                Intents.START_RPC_SHORTCUT -> {
                    stopService(Intent(this, Rpc::class.java))

                    startService(Intent(this, Rpc::class.java))
                    Toast.makeText(
                        this, getString(R.string.start_rpc_toast), Toast.LENGTH_SHORT
                    ).show()
                }

                Intents.STOP_RPC -> {
                    stopService(Intent(this, Rpc::class.java))

                    Toast.makeText(this, getString(R.string.stop_rpc_toast), Toast.LENGTH_SHORT)
                        .show()
                }

                else -> Unit
            }
        }

        // Close the activity
        finish()
    }

    companion object {
        object Intents {
            const val START_RPC_SHORTCUT =
                "com.my.rpc.intent.action.START_RPC"
            const val STOP_RPC = "com.my.rpc.intent.action.STOP_RPC"
        }
    }
}