/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * Ext.kt is part of Rpc
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.rpc.feature_rpc_base

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import com.my.rpc.data.rpc.RpcImage
import com.my.rpc.data.utils.getAppInfo
import com.my.rpc.data.utils.toBitmap
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal suspend fun Notification.Builder.setLargeIcon(
    rpcImage: RpcImage?,
    context: Context,
): Notification.Builder = suspendCancellableCoroutine { continuation ->
    if (rpcImage == null) {
        continuation.resume(this)
        return@suspendCancellableCoroutine
    }

    val data = when (rpcImage) {
        is RpcImage.ApplicationIcon -> context.getAppInfo(rpcImage.packageName).toBitmap(context)
        is RpcImage.BitmapImage -> rpcImage.bitmap
        is RpcImage.DiscordImage -> "https://cdn.discordapp.com/${rpcImage.image}"
        is RpcImage.ExternalImage -> rpcImage.image
    }

    val imageLoader = NotificationImageLoaderHolder.get(context)
    val request = coil.request.ImageRequest.Builder(context)
        .data(data)
        .target(
            onSuccess = { drawable ->
                val bitmap = when (drawable) {
                    is BitmapDrawable -> drawable.bitmap
                    else -> createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight).apply {
                        val canvas = android.graphics.Canvas(this)
                        drawable.setBounds(0, 0, canvas.width, canvas.height)
                        drawable.draw(canvas)
                    }
                }

                val maxSize = 256
                val scaledBitmap = if (bitmap.width > maxSize || bitmap.height > maxSize) {
                    val scale = maxSize.toFloat() / maxOf(bitmap.width, bitmap.height)
                    bitmap.scale(
                        width = (bitmap.width * scale).toInt(),
                        height = (bitmap.height * scale).toInt()
                    )
                } else bitmap

                val optimizedBitmap = if (scaledBitmap.config != Bitmap.Config.ARGB_8888) {
                    scaledBitmap.copy(Bitmap.Config.ARGB_8888, false)
                } else scaledBitmap

                continuation.resume(this@setLargeIcon.setLargeIcon(optimizedBitmap))
            },
            onError = {
                continuation.resume(this@setLargeIcon)
            }
        )
        .build()

    val disposable = imageLoader.enqueue(request)

    continuation.invokeOnCancellation {
        disposable.dispose()
    }
}

/** Singleton holder for ImageLoader used in notification icon loading. Prevents memory leak from creating a new ImageLoader per update. */
private object NotificationImageLoaderHolder {
    @Volatile
    private var instance: coil.ImageLoader? = null

    fun get(context: Context): coil.ImageLoader {
        return instance ?: synchronized(this) {
            instance ?: coil.ImageLoader(context.applicationContext).also { instance = it }
        }
    }
}