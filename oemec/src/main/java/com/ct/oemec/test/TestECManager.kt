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

package com.ct.oemec.test

import android.annotation.SuppressLint
import com.ct.oemec.utils.JsonUtil
import com.ct.oemec.utils.logger.Logger
import com.newcalllib.expandingCapacity.IExpandingCapacity
import com.newcalllib.expandingCapacity.IExpandingCapacityCallback
import java.util.Timer
import java.util.TimerTask

@SuppressLint("StaticFieldLeak")
object TestECManager {
    private val TAG = "TestECManager"
    private val sLogger = Logger.getLogger(TAG)
    var mCallback: IExpandingCapacityCallback? = null
    val mTestExpandingCapacity: TestExpandingCapacity = TestExpandingCapacity()
    private var mAvatarTimer: Timer? = null
    private var mAvatarTask: TimerTask? = null
    private var mCurrentAvatarId: String = "avatar_cat"
    private var mMyLanguage: String = "Indonesian"
    private var mOtherLanguage: String = "English"

    // module,func -> data，仅用于本地mock，模拟云端AI能力（真实场景中AI能力应部署在云端，
    // 通过运营商/终端厂商的拓展能力服务转发，而非集成在SDK或小程序内）
    private val AVATAR_LIST = listOf(
        mapOf("id" to "avatar_cat", "name" to "Cat", "thumbnail" to "avatar_cat_thumb.png"),
        mapOf("id" to "avatar_robot", "name" to "Robot", "thumbnail" to "avatar_robot_thumb.png"),
        mapOf("id" to "avatar_panda", "name" to "Panda", "thumbnail" to "avatar_panda_thumb.png")
    )

    // 按语言对循环返回的模拟翻译语料，真实实现中应替换为云端翻译服务返回的结果
    private val TRANSLATE_PHRASES = mapOf(
        ("Chinese" to "English") to listOf(
            mapOf("myOriginal" to "你好", "myTranslate" to "hello"),
            mapOf("myOriginal" to "今天天气不错", "myTranslate" to "the weather is nice today"),
            mapOf("myOriginal" to "再见", "myTranslate" to "goodbye")
        ),
        ("English" to "Chinese") to listOf(
            mapOf("myOriginal" to "good morning", "myTranslate" to "早上好"),
            mapOf("myOriginal" to "how are you", "myTranslate" to "你好吗"),
            mapOf("myOriginal" to "see you later", "myTranslate" to "回头见")
        ),
        ("Chinese" to "Japanese") to listOf(
            mapOf("myOriginal" to "你好", "myTranslate" to "こんにちは"),
            mapOf("myOriginal" to "谢谢", "myTranslate" to "ありがとう")
        ),
        ("Indonesian" to "English") to listOf(
            mapOf("myOriginal" to "halo", "myTranslate" to "hello"),
            mapOf("myOriginal" to "cuacanya bagus hari ini", "myTranslate" to "the weather is nice today"),
            mapOf("myOriginal" to "sampai jumpa", "myTranslate" to "goodbye")
        ),
        ("English" to "Indonesian") to listOf(
            mapOf("myOriginal" to "good morning", "myTranslate" to "selamat pagi"),
            mapOf("myOriginal" to "how are you", "myTranslate" to "apa kabar"),
            mapOf("myOriginal" to "see you later", "myTranslate" to "sampai nanti")
        )
    )
    private var mMyPhraseIndex = 0

    fun onUnbind() {
        mCallback = null
        stopAvatar()
    }

    class TestExpandingCapacity : IExpandingCapacity.Stub() {
        override fun request(content: String?) {
            sLogger.info("request content: $content")
            val requestData = content?.let { JsonUtil.fromJson(it, OEMECBaseData::class.java) }
            sLogger.info("request requestData: ${requestData?.module},${requestData?.func}")
            when(requestData?.module){
                "AIVideo" -> {
                    when(requestData.func){
                        "detect" -> {
                            val responseData = OEMECBaseData(
                                "AIVideo",
                                "detectCallback",
                                mutableMapOf("isAI" to true)
                            )
                            val responseString = JsonUtil.toJson(responseData)
                            mCallback?.onCallback(responseString)
                        }
                    }
                }
                "Translate" -> {
                    when(requestData.func){
                        "languageList" -> {
                            val responseData = OEMECBaseData(
                                "Translate",
                                "languageListCallback",
                                mutableMapOf("list" to mutableListOf("Indonesian", "English", "Chinese", "Japanese"))
                            )
                            val responseString = JsonUtil.toJson(responseData)
                            mCallback?.onCallback(responseString)
                        }
                        "setLanguage" -> {
                            val requestDataDetail = content.let { JsonUtil.fromJson(it, TranslateLanguageRequest::class.java) }
                            requestDataDetail?.data?.myLanguage?.let { mMyLanguage = it }
                            requestDataDetail?.data?.otherLanguage?.let { mOtherLanguage = it }
                            mMyPhraseIndex = 0
                            val responseData = OEMECBaseData(
                                "Translate",
                                "setLanguageCallback",
                                mutableMapOf("myLanguage" to mMyLanguage, "otherLanguage" to mOtherLanguage)
                            )
                            val responseString = JsonUtil.toJson(responseData)
                            mCallback?.onCallback(responseString)
                        }
                        "start", "stop" -> {
                            // 本mock中翻译按“说一句话触发一次”（见voice分支），没有需要开关的持续会话；
                            // 真实的流式云端语音识别服务通常需要显式start/stop一个识别会话，
                            // 此处保留该函数以维持JS接口不变，暂不需要额外处理
                        }
                        "voice" -> {
                            // 由小程序端麦克风检测到本端说话时触发（仅mock：真实场景中应携带音频数据，
                            // 交由云端语音识别+翻译服务处理；此处直接返回预设语料模拟识别+翻译结果）。
                            // 对端听到的内容不会经此EC通道产生——那是对端自己小程序实例的本端麦克风结果，
                            // 需要由小程序自己通过ADC（Application Data Channel）发送给对端，而不是本地mock出来
                            pushMyUtterance()
                        }
                    }
                }
                "Avatar" -> {
                    when(requestData.func){
                        "avatarList" -> {
                            val responseData = OEMECBaseData(
                                "Avatar",
                                "avatarListCallback",
                                mutableMapOf("list" to AVATAR_LIST)
                            )
                            val responseString = JsonUtil.toJson(responseData)
                            mCallback?.onCallback(responseString)
                        }
                        "setAvatar" -> {
                            val requestDataDetail = content.let { JsonUtil.fromJson(it, SetAvatarRequest::class.java) }
                            requestDataDetail?.data?.avatarId?.let { mCurrentAvatarId = it }
                            val responseData = OEMECBaseData(
                                "Avatar",
                                "setAvatarCallback",
                                mutableMapOf("avatarId" to mCurrentAvatarId)
                            )
                            val responseString = JsonUtil.toJson(responseData)
                            mCallback?.onCallback(responseString)
                        }
                        "setAvatarEnable" -> {
                            val requestDataDetail = content.let { JsonUtil.fromJson(it, SetAvatarEnableRequest::class.java) }
                            if (requestDataDetail?.data?.isEnable == true) {
                                startAvatar()
                            } else {
                                stopAvatar()
                            }
                        }
                    }
                }
                "NewCallSDK" -> {
                    when(requestData.func){
                        "setExpandEnable" -> {
                            val requestDataDetail = content.let { JsonUtil.fromJson(it, ECExpand::class.java) }
                            if (requestDataDetail?.data?.isEnable == true){
                                val responseData = OEMECBaseData(
                                    "NewCallSDK",
                                    "expand",
                                    null
                                )
                                val responseString = JsonUtil.toJson(responseData)
                                mCallback?.onCallback(responseString)
                            } else {
                                val responseData = OEMECBaseData(
                                    "NewCallSDK",
                                    "queryExpandEnable",
                                    null
                                )
                                val responseString = JsonUtil.toJson(responseData)
                                mCallback?.onCallback(responseString)
                            }
                        }
                    }
                }
            }
        }

        override fun setCallback(l: IExpandingCapacityCallback?) {
            if (l != null) {
                mCallback = l
            } else {
                sLogger.info("setCallback l is null")
            }
        }
    }

    // voice：由小程序端麦克风检测到本端说话时触发一次，模拟识别+翻译本端刚说的一句话。
    // 对端要看到这句话的翻译结果，需由小程序自己通过ADC发给对端的小程序实例——
    // 这个mock只负责本端"识别+翻译"这一步，不负责把结果送到对端
    fun pushMyUtterance(){
        val myPhrases = TRANSLATE_PHRASES[mMyLanguage to mOtherLanguage] ?: TRANSLATE_PHRASES[("Chinese" to "English")]!!
        val myPhrase = myPhrases[mMyPhraseIndex % myPhrases.size]
        mMyPhraseIndex++
        val responseData = OEMECBaseData(
            "Translate",
            "translateResultCallback",
            mutableMapOf(
                "myOriginal" to myPhrase["myOriginal"],
                "myTranslate" to myPhrase["myTranslate"]
            )
        )
        val responseString = JsonUtil.toJson(responseData)
        mCallback?.onCallback(responseString)
    }

    private val AVATAR_EXPRESSIONS = listOf("neutral", "talking", "smiling")
    private var mAvatarFrameIndex = 0

    fun startAvatar(){
        if (mAvatarTimer == null) {
            mAvatarTimer = Timer()
        }
        if (mAvatarTask == null){
            mAvatarTask = object : TimerTask(){
                override fun run() {
                    // 模拟caller说话时头像口型/表情的周期性变化，真实实现应来自云端AI视频驱动能力
                    val mouthOpen = mAvatarFrameIndex % 2 == 0
                    val expression = AVATAR_EXPRESSIONS[mAvatarFrameIndex % AVATAR_EXPRESSIONS.size]
                    mAvatarFrameIndex++
                    val responseData = OEMECBaseData(
                        "Avatar",
                        "avatarFrameCallback",
                        mutableMapOf(
                            "avatarId" to mCurrentAvatarId,
                            "mouthOpen" to mouthOpen,
                            "expression" to expression
                        )
                    )
                    val responseString = JsonUtil.toJson(responseData)
                    mCallback?.onCallback(responseString)
                }
            }
        }
        mAvatarTimer!!.schedule(mAvatarTask, 0, 300)
    }

    fun stopAvatar(){
        if (mAvatarTimer != null) {
            mAvatarTimer!!.cancel()
            mAvatarTimer = null
        }
        if (mAvatarTask != null){
            mAvatarTask!!.cancel()
            mAvatarTask = null
        }
        mAvatarFrameIndex = 0
    }
}

data class TranslateLanguageRequest(
    var module: String,
    var func: String,
    var data: TranslateLanguageData
)
data class TranslateLanguageData(
    var myLanguage: String,
    var otherLanguage: String
)

data class SetAvatarRequest(
    var module: String,
    var func: String,
    var data: SetAvatarData
)
data class SetAvatarData(
    var avatarId: String
)

data class SetAvatarEnableRequest(
    var module: String,
    var func: String,
    var data: SetAvatarEnableData
)
data class SetAvatarEnableData(
    var isEnable: Boolean
)