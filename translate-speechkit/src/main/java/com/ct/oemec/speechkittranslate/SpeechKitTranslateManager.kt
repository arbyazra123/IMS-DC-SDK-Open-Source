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

package com.ct.oemec.speechkittranslate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.newcalllib.expandingCapacity.IExpandingCapacity
import com.newcalllib.expandingCapacity.IExpandingCapacityCallback

/**
 * Real (non-mock, non-model-less-reference) [IExpandingCapacity] provider for the
 * `Translate` EC module, built from platform/Play-Services pieces instead of a raw
 * TFLite [org.tensorflow.lite.Interpreter] — see README.md for why, and what's different
 * about how it owns the microphone compared to the mock/translate-tflite.
 */
class SpeechKitTranslateManager(context: Context) {

    companion object {
        private const val TAG = "SpeechKitTranslateManager"
        private const val CONSECUTIVE_AUDIO_ERROR_LIMIT = 3

        // Extend these two maps together to add a language - both android.speech's locale
        // tags and ML Kit's TranslateLanguage constants need an entry.
        private val LANGUAGE_TO_RECOGNIZER_LOCALE = mapOf(
            "Indonesian" to "id-ID",
            "English" to "en-US"
        )
        private val LANGUAGE_TO_MLKIT = mapOf(
            "Indonesian" to TranslateLanguage.INDONESIAN,
            "English" to TranslateLanguage.ENGLISH
        )
    }

    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private val gson = Gson()

    var callback: IExpandingCapacityCallback? = null
    private var myLanguage = "Indonesian"
    private var otherLanguage = "English"
    private var translator: Translator? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var consecutiveAudioErrors = 0

    init {
        prepareTranslator()
    }

    val binder: IExpandingCapacity.Stub = object : IExpandingCapacity.Stub() {
        override fun request(content: String?) {
            val json = content?.let { runCatching { gson.fromJson(it, JsonObject::class.java) }.getOrNull() } ?: return
            if (json.get("module")?.asString != "Translate") return
            val data = json.getAsJsonObject("data")
            when (json.get("func")?.asString) {
                "languageList" -> respond("languageListCallback", mapOf("list" to LANGUAGE_TO_RECOGNIZER_LOCALE.keys.toList()))
                "setLanguage" -> {
                    data?.get("myLanguage")?.asString?.let { myLanguage = it }
                    data?.get("otherLanguage")?.asString?.let { otherLanguage = it }
                    prepareTranslator()
                    respond("setLanguageCallback", mapOf("myLanguage" to myLanguage, "otherLanguage" to otherLanguage))
                }
                "start" -> mainHandler.post { startListening() }
                "stop" -> mainHandler.post { stopListening() }
                "voice" -> {
                    // SpeechRecognizer owns the microphone directly and continuously between
                    // start/stop - it doesn't accept pre-recorded audio, so the mini-app's own
                    // captured buffer (still sent for compatibility with the other two
                    // providers) is simply unused here.
                }
            }
        }

        override fun setCallback(l: IExpandingCapacityCallback?) {
            callback = l
        }
    }

    fun release() {
        mainHandler.post {
            stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
        }
        translator?.close()
        translator = null
    }

    private fun prepareTranslator() {
        translator?.close()
        val sourceLang = LANGUAGE_TO_MLKIT[myLanguage] ?: TranslateLanguage.INDONESIAN
        val targetLang = LANGUAGE_TO_MLKIT[otherLanguage] ?: TranslateLanguage.ENGLISH
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLang)
            .setTargetLanguage(targetLang)
            .build()
        val newTranslator = Translation.getClient(options)
        // One-time download per language pair; needs internet the first time, works
        // offline afterward. Failure here (e.g. no network yet) is reported, not silent.
        newTranslator.downloadModelIfNeeded()
            .addOnSuccessListener { Log.i(TAG, "translation model ready: $myLanguage -> $otherLanguage") }
            .addOnFailureListener { e ->
                Log.e(TAG, "translation model download failed", e)
                respondError("translation model download failed (needs internet the first time): ${e.message}")
            }
        translator = newTranslator
    }

    private fun startListening() {
        if (isListening) return
        if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
            respondError("SpeechRecognizer not available on this device (needs Google Play Services / a Google Play system image on emulators)")
            return
        }
        isListening = true
        consecutiveAudioErrors = 0
        val locale = LANGUAGE_TO_RECOGNIZER_LOCALE[myLanguage] ?: "id-ID"
        val recognizer = speechRecognizer ?: SpeechRecognizer.createSpeechRecognizer(appContext).also { speechRecognizer = it }
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                consecutiveAudioErrors = 0
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                Log.d(TAG, "onResults matches:$matches")
                val text = matches?.firstOrNull()
                if (!text.isNullOrEmpty()) {
                    handleRecognizedText(text)
                } else {
                    Log.d(TAG, "onResults had no usable text")
                }
                restartIfListening(recognizer, locale)
            }

            override fun onError(error: Int) {
                Log.d(TAG, "onError code:$error (${errorName(error)})")
                if (error == SpeechRecognizer.ERROR_AUDIO) {
                    consecutiveAudioErrors++
                    if (consecutiveAudioErrors >= CONSECUTIVE_AUDIO_ERROR_LIMIT) {
                        respondError(
                            "microphone unavailable after $consecutiveAudioErrors attempts - " +
                                "something else (e.g. this mini-app's own getUserMedia capture) " +
                                "may be holding it"
                        )
                        isListening = false
                        return
                    }
                }
                // ERROR_NO_MATCH / ERROR_SPEECH_TIMEOUT are routine (silence) - just keep listening
                restartIfListening(recognizer, locale)
            }

            override fun onReadyForSpeech(params: Bundle?) { Log.d(TAG, "onReadyForSpeech") }
            override fun onBeginningOfSpeech() { Log.d(TAG, "onBeginningOfSpeech") }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { Log.d(TAG, "onEndOfSpeech") }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        recognizer.startListening(buildRecognizerIntent(locale))
    }

    private fun restartIfListening(recognizer: SpeechRecognizer, locale: String) {
        if (isListening) {
            recognizer.startListening(buildRecognizerIntent(locale))
        }
    }

    private fun buildRecognizerIntent(locale: String): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale)
        }
    }

    private fun errorName(error: Int): String = when (error) {
        SpeechRecognizer.ERROR_AUDIO -> "ERROR_AUDIO"
        SpeechRecognizer.ERROR_CLIENT -> "ERROR_CLIENT"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "ERROR_INSUFFICIENT_PERMISSIONS"
        SpeechRecognizer.ERROR_NETWORK -> "ERROR_NETWORK"
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "ERROR_NETWORK_TIMEOUT"
        SpeechRecognizer.ERROR_NO_MATCH -> "ERROR_NO_MATCH"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "ERROR_RECOGNIZER_BUSY"
        SpeechRecognizer.ERROR_SERVER -> "ERROR_SERVER"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "ERROR_SPEECH_TIMEOUT"
        else -> "UNKNOWN($error)"
    }

    private fun stopListening() {
        isListening = false
        speechRecognizer?.stopListening()
        speechRecognizer?.cancel()
    }

    private fun handleRecognizedText(text: String) {
        val activeTranslator = translator
        if (activeTranslator == null) {
            respond("translateResultCallback", mapOf("myOriginal" to text, "myTranslate" to "[no translator ready]"))
            return
        }
        activeTranslator.translate(text)
            .addOnSuccessListener { translated ->
                respond("translateResultCallback", mapOf("myOriginal" to text, "myTranslate" to translated))
            }
            .addOnFailureListener { e ->
                respondError("translation failed: ${e.message}")
            }
    }

    private fun respond(func: String, data: Map<String, Any?>) {
        val payload = mapOf("module" to "Translate", "func" to func, "data" to data)
        callback?.onCallback(gson.toJson(payload))
    }

    private fun respondError(reason: String) {
        Log.w(TAG, reason)
        respond("translateErrorCallback", mapOf("reason" to reason))
    }
}
