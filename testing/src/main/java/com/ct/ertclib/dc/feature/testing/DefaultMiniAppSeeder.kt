/*
 * Copyright 2025-China Telecom Research Institute.
 * All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ct.ertclib.dc.feature.testing

import android.content.Context
import com.blankj.utilcode.util.SPUtils
import com.ct.ertclib.dc.core.data.model.MiniAppInfo
import com.ct.ertclib.dc.core.utils.common.Base64Utils
import com.ct.ertclib.dc.core.utils.common.JsonUtil
import com.ct.ertclib.dc.core.utils.logger.Logger
import java.io.File
import java.io.FileOutputStream

/**
 * Seeds the Local-test "MiniApp Repository" (the same `TestMiniAppList` SPUtils entry that
 * [LocalTestMiniAppEditActivity]'s Save button writes to, and [LocalTestMiniAppWarehouseActivity]
 * / [TestImsDataChannelImpl] read from) with the Avatar and Translation demo mini-apps, so
 * they're available out of the box instead of requiring `adb push` + manual "Edit MiniApp"
 * configuration. This does not replace or bypass that manual flow — it just writes the same
 * kind of entry a user would, from packages bundled as assets instead of an sdcard file, so
 * downloading/adding other mini-apps that way still works exactly as before.
 */
object DefaultMiniAppSeeder {
    private const val TAG = "DefaultMiniAppSeeder"
    private const val SEEDED_FLAG_KEY = "DefaultMiniAppsSeededV1"
    private const val MINI_APP_LIST_KEY = "TestMiniAppList"

    private val sLogger = Logger.getLogger(TAG)

    private data class DefaultApp(
        val appId: String,
        val appName: String,
        val assetFileName: String,
        val supportScene: Int // SupportScene: 1=AUDIO, 2=VIDEO, 3=ALL
    )

    private val DEFAULT_APPS = listOf(
        DefaultApp(appId = "601", appName = "AI Avatar", assetFileName = "avatar_miniapp.zip", supportScene = 2),
        DefaultApp(appId = "602", appName = "Live Translate", assetFileName = "translation_miniapp.zip", supportScene = 3)
    )

    fun seedIfNeeded(context: Context) {
        val sp = SPUtils.getInstance()
        if (sp.getBoolean(SEEDED_FLAG_KEY, false)) return

        val existingList = sp.getString(MINI_APP_LIST_KEY)
        val existingAppIds = parseAppIds(existingList)

        val builder = StringBuilder(existingList ?: "")
        DEFAULT_APPS.forEach { app ->
            if (app.appId in existingAppIds) {
                sLogger.info("seedIfNeeded appId:${app.appId} already present, skipping")
                return@forEach
            }
            val zipPath = copyAssetToInternalStorage(context, app.assetFileName)
            if (zipPath == null) {
                sLogger.error("seedIfNeeded failed to stage asset ${app.assetFileName}")
                return@forEach
            }
            val miniAppInfo = MiniAppInfo(
                appId = app.appId,
                appName = app.appName,
                appIcon = null,
                autoLaunch = false,
                autoLoad = false,
                callId = "",
                eTag = "1.0.0",
                ifWorkWithoutPeerDc = true,
                isOutgoingCall = false,
                myNumber = null,
                path = null,
                phase = "INCALL",
                qosHint = "loss=0.0002;latency=600",
                remoteNumber = null,
                slotId = 0,
                supportScene = app.supportScene,
                isStartAfterInstalled = true,
                lastUseTime = 0
            )
            val entry = Base64Utils.encodeToBase64(JsonUtil.toJson(miniAppInfo)) + "&zipPath=" + zipPath
            if (builder.isNotEmpty()) builder.append(",")
            builder.append(entry)
            sLogger.info("seedIfNeeded added default mini-app appId:${app.appId} zipPath:$zipPath")
        }
        sp.put(MINI_APP_LIST_KEY, builder.toString())
        sp.put(SEEDED_FLAG_KEY, true)
    }

    private fun parseAppIds(list: String?): Set<String> {
        if (list.isNullOrEmpty()) return emptySet()
        return list.split(",").mapNotNull { item ->
            runCatching {
                val split = item.split("&zipPath=")
                JsonUtil.fromJson(Base64Utils.decodeFromBase64(split[0]), MiniAppInfo::class.java)?.appId
            }.getOrNull()
        }.toSet()
    }

    private fun copyAssetToInternalStorage(context: Context, assetFileName: String): String? {
        return try {
            val outDir = File(context.filesDir, "default_miniapps")
            if (!outDir.exists()) outDir.mkdirs()
            val outFile = File(outDir, assetFileName)
            if (!outFile.exists()) {
                context.assets.open(assetFileName).use { input ->
                    FileOutputStream(outFile).use { output -> input.copyTo(output) }
                }
            }
            outFile.absolutePath
        } catch (e: Exception) {
            sLogger.error("copyAssetToInternalStorage failed for $assetFileName", e)
            null
        }
    }
}
