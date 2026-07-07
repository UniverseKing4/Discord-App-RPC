/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * RpcImage.kt is part of Rpc
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.rpc.data.rpc

import android.content.Context
import android.graphics.Bitmap
import com.my.rpc.domain.repository.RpcRepository
import com.my.rpc.preference.Prefs
import com.my.rpc.data.utils.getAppInfo
import com.my.rpc.data.utils.toBitmap
import com.my.rpc.data.utils.toFile
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

sealed class RpcImage {
    abstract suspend fun resolveImage(repository: RpcRepository): String?

    companion object {
        private var savedAppIcons: HashMap<String, String>? = null
        private var savedArtworks: HashMap<String, String>? = null

        fun getSavedAppIcons(): HashMap<String, String> {
            if (savedAppIcons == null) {
                val data = Prefs[Prefs.SAVED_IMAGES, "{}"]
                savedAppIcons = try { Json.decodeFromString(data) } catch (e: Exception) { HashMap() }
            }
            return savedAppIcons!!
        }

        fun saveAppIcon(packageName: String, url: String) {
            val map = getSavedAppIcons()
            map[packageName] = url
            Prefs[Prefs.SAVED_IMAGES] = Json.encodeToString(map)
        }

        fun getSavedArtworks(): HashMap<String, String> {
            if (savedArtworks == null) {
                val data = Prefs[Prefs.SAVED_ARTWORK, "{}"]
                savedArtworks = try { Json.decodeFromString(data) } catch (e: Exception) { HashMap() }
            }
            return savedArtworks!!
        }

        fun saveArtwork(schema: String, url: String) {
            val map = getSavedArtworks()
            map[schema] = url
            Prefs[Prefs.SAVED_ARTWORK] = Json.encodeToString(map)
        }

        fun clearCache() {
            savedAppIcons = null
            savedArtworks = null
        }
    }

    class DiscordImage(val image: String) : RpcImage() {
        override suspend fun resolveImage(repository: RpcRepository): String {
            return "mp:${image}"
        }
    }

    class ExternalImage(val image: String) : RpcImage() {
        override suspend fun resolveImage(repository: RpcRepository): String? {
            return repository.getImage(image)
        }
    }

    class ApplicationIcon(val packageName: String, private val context: Context) : RpcImage() {
        override suspend fun resolveImage(repository: RpcRepository): String? {
            val savedImages = getSavedAppIcons()
            return if (savedImages.containsKey(packageName))
                savedImages[packageName]
            else
                retrieveImageFromApi(packageName, context, repository)
        }

        private suspend fun retrieveImageFromApi(
            packageName: String,
            context: Context,
            repository: RpcRepository,
        ): String? {
            val applicationInfo = context.getAppInfo(packageName)
            val bitmap = applicationInfo.toBitmap(context)
            val response = repository.uploadImage(bitmap.toFile(context, "image"))
            response?.let {
                saveAppIcon(packageName, it)
            }
            return response
        }
    }

    class BitmapImage(
        private val context: Context,
        val bitmap: Bitmap?,
        private val packageName: String,
        val title: String,
    ) : RpcImage() {
        override suspend fun resolveImage(repository: RpcRepository): String? {
            val schema = "${this.packageName}:${this.title}"
            val savedImages = getSavedArtworks()
            return if (savedImages.containsKey(schema))
                savedImages[schema]
            else {
                val result = repository.uploadImage(bitmap.toFile(this.context, "art"))
                result?.let {
                    saveArtwork(schema, it)
                }
                result
            }
        }
    }
}
