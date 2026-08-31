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

package com.ct.oemec.translatetflite

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.newcalllib.expandingCapacity.IExpandingCapacity
import com.newcalllib.expandingCapacity.IExpandingCapacityCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Real (non-mock) [IExpandingCapacity] provider for the `Translate` EC module — see
 * README.md for the full picture. Same wire protocol as TestECManager's mock
 * (module/func/data JSON, `translateResultCallback` responses) so a mini-app built
 * against the mock works against this unmodified. What's different: `voice` here expects
 * real base64 PCM16 audio and actually runs it through two on-device TFLite models this
 * module does NOT ship — if they aren't present on the device, it reports that plainly
 * instead of pretending to have understood the speech.
 */
class TranslateTfliteManager(context: Context) {

    companion object {
        private const val TAG = "TranslateTfliteManager"
        private const val SAMPLE_RATE_HZ = 16000
    }

    private val appContext = context.applicationContext
    private val models = ModelRepository(appContext)
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    var callback: IExpandingCapacityCallback? = null
    private var myLanguage = "Chinese"
    private var otherLanguage = "English"

    val binder: IExpandingCapacity.Stub = object : IExpandingCapacity.Stub() {
        override fun request(content: String?) {
            val json = content?.let { runCatching { gson.fromJson(it, JsonObject::class.java) }.getOrNull() } ?: return
            val module = json.get("module")?.asString
            val func = json.get("func")?.asString
            val data = json.getAsJsonObject("data")
            if (module != "Translate") return
            when (func) {
                "languageList" -> respond("languageListCallback", mapOf("list" to listOf("Chinese", "English", "Japanese")))
                "setLanguage" -> {
                    data?.get("myLanguage")?.asString?.let { myLanguage = it }
                    data?.get("otherLanguage")?.asString?.let { otherLanguage = it }
                    respond("setLanguageCallback", mapOf("myLanguage" to myLanguage, "otherLanguage" to otherLanguage))
                }
                "start", "stop" -> { /* no persistent session in this reference; see TestECManager's mock for why */ }
                "voice" -> {
                    val audioBase64 = data?.get("audio")?.asString
                    if (audioBase64.isNullOrEmpty()) {
                        respondError("voice request missing 'audio' (base64 PCM16 mono @ ${SAMPLE_RATE_HZ}Hz)")
                        return
                    }
                    scope.launch { handleVoice(audioBase64) }
                }
            }
        }

        override fun setCallback(l: IExpandingCapacityCallback?) {
            callback = l
        }
    }

    fun release() {
        models.close()
    }

    private fun handleVoice(audioBase64: String) {
        val samples = runCatching { AudioCodec.decodePcm16Base64(audioBase64) }.getOrElse {
            respondError("failed to decode audio: ${it.message}")
            return
        }

        val asrInterpreter = models.getAsrInterpreter()
        val asrVocab = models.getAsrVocab()
        if (asrInterpreter == null || asrVocab == null) {
            respondError(
                "no ASR model found at ${models.asrModelFile} (+ ${models.asrVocabFile.name}) — " +
                    "place a real model there to get actual recognition; see README.md"
            )
            return
        }

        val recognizedText = runCatching { runAsr(asrInterpreter, asrVocab, samples) }.getOrElse {
            Log.e(TAG, "ASR inference failed", it)
            respondError("ASR inference failed: ${it.message}")
            return
        }

        val translateInterpreter = models.getTranslateInterpreter()
        val translateVocab = models.getTranslateVocab()
        val translatedText = if (translateInterpreter != null && translateVocab != null) {
            runCatching { runTranslate(translateInterpreter, translateVocab, recognizedText) }
                .getOrElse { "[translation failed: ${it.message}]" }
        } else {
            "[no translation model loaded — recognized text only]"
        }

        respond(
            "translateResultCallback",
            mapOf("myOriginal" to recognizedText, "myTranslate" to translatedText)
        )
    }

    /**
     * Assumes the simplest possible ASR contract: a single float32 input tensor of raw
     * mono PCM samples, and a single output tensor of per-timestep logits over `asrVocab`,
     * decoded greedily with CTC-style blank(index 0)/repeat collapsing. Real ASR models
     * (Whisper, Conformer, ...) usually want log-mel spectrogram features and a much
     * richer decoder (beam search, WordPiece/SentencePiece merging) instead — replace this
     * function to match whatever model you actually load. What's reusable regardless of
     * that: the interpreter lifecycle, dynamic input resizing, and the surrounding EC/error
     * plumbing above.
     */
    private fun runAsr(interpreter: Interpreter, vocab: List<String>, samples: FloatArray): String {
        interpreter.resizeInput(0, intArrayOf(1, samples.size))
        interpreter.allocateTensors()

        val inputBuffer = ByteBuffer.allocateDirect(samples.size * 4).order(ByteOrder.nativeOrder())
        samples.forEach { inputBuffer.putFloat(it) }
        inputBuffer.rewind()

        val outputShape = interpreter.getOutputTensor(0).shape() // expected [1, T, vocabSize]
        val timeSteps = outputShape.getOrElse(1) { 1 }
        val vocabSize = outputShape.getOrElse(2) { vocab.size }
        val outputBuffer = ByteBuffer.allocateDirect(timeSteps * vocabSize * 4).order(ByteOrder.nativeOrder())

        interpreter.run(inputBuffer, outputBuffer)

        outputBuffer.rewind()
        val sb = StringBuilder()
        var previousToken = -1
        for (t in 0 until timeSteps) {
            var bestIndex = 0
            var bestScore = Float.NEGATIVE_INFINITY
            for (v in 0 until vocabSize) {
                val score = outputBuffer.float
                if (score > bestScore) {
                    bestScore = score
                    bestIndex = v
                }
            }
            // CTC-style: skip blank (0) and immediate repeats
            if (bestIndex != 0 && bestIndex != previousToken && bestIndex < vocab.size) {
                sb.append(vocab[bestIndex])
            }
            previousToken = bestIndex
        }
        return sb.toString().ifEmpty { "[recognized empty]" }
    }

    /**
     * Same honesty note as [runAsr]: assumes a single int32 input tensor of whitespace-token
     * vocab ids and a single output logits tensor over `translateVocab`, greedily decoded.
     * A real MT model (e.g. a distilled Transformer) will want SentencePiece tokenization
     * and often a seq2seq decode loop with a start/end token, not a single forward pass —
     * replace this function to match your model.
     */
    private fun runTranslate(interpreter: Interpreter, vocab: List<String>, text: String): String {
        val tokenIds = text.split(" ").filter { it.isNotEmpty() }.map { token ->
            vocab.indexOf(token).let { if (it >= 0) it else 0 }
        }
        if (tokenIds.isEmpty()) return ""

        interpreter.resizeInput(0, intArrayOf(1, tokenIds.size))
        interpreter.allocateTensors()

        val inputBuffer = ByteBuffer.allocateDirect(tokenIds.size * 4).order(ByteOrder.nativeOrder())
        tokenIds.forEach { inputBuffer.putInt(it) }
        inputBuffer.rewind()

        val outputShape = interpreter.getOutputTensor(0).shape() // expected [1, T, vocabSize]
        val timeSteps = outputShape.getOrElse(1) { 1 }
        val vocabSize = outputShape.getOrElse(2) { vocab.size }
        val outputBuffer = ByteBuffer.allocateDirect(timeSteps * vocabSize * 4).order(ByteOrder.nativeOrder())

        interpreter.run(inputBuffer, outputBuffer)

        outputBuffer.rewind()
        val words = mutableListOf<String>()
        for (t in 0 until timeSteps) {
            var bestIndex = 0
            var bestScore = Float.NEGATIVE_INFINITY
            for (v in 0 until vocabSize) {
                val score = outputBuffer.float
                if (score > bestScore) {
                    bestScore = score
                    bestIndex = v
                }
            }
            if (bestIndex < vocab.size) words.add(vocab[bestIndex])
        }
        return words.joinToString(" ")
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
