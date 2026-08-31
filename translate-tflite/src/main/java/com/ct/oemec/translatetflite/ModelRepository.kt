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
import org.tensorflow.lite.Interpreter
import java.io.File

/**
 * Resolves and lazily loads the two on-device models this reference provider needs, from a
 * fixed external-storage location — this module ships NO .tflite/vocab files itself (see
 * README.md). Callers ask for a model; if the file isn't there, they get null back and are
 * expected to fail gracefully rather than crash.
 */
class ModelRepository(context: Context) {

    private val modelDir: File = File(context.getExternalFilesDir(null), "translate_tflite_models")

    private var asrInterpreter: Interpreter? = null
    private var translateInterpreter: Interpreter? = null
    private var asrVocab: List<String>? = null
    private var translateVocab: List<String>? = null

    val asrModelFile get() = File(modelDir, "asr.tflite")
    val asrVocabFile get() = File(modelDir, "asr_vocab.txt")
    val translateModelFile get() = File(modelDir, "translate.tflite")
    val translateVocabFile get() = File(modelDir, "translate_vocab.txt")

    fun getAsrInterpreter(): Interpreter? {
        if (asrInterpreter == null && asrModelFile.exists()) {
            asrInterpreter = runCatching { Interpreter(asrModelFile) }.getOrNull()
        }
        return asrInterpreter
    }

    fun getAsrVocab(): List<String>? {
        if (asrVocab == null && asrVocabFile.exists()) {
            asrVocab = runCatching { asrVocabFile.readLines() }.getOrNull()
        }
        return asrVocab
    }

    fun getTranslateInterpreter(): Interpreter? {
        if (translateInterpreter == null && translateModelFile.exists()) {
            translateInterpreter = runCatching { Interpreter(translateModelFile) }.getOrNull()
        }
        return translateInterpreter
    }

    fun getTranslateVocab(): List<String>? {
        if (translateVocab == null && translateVocabFile.exists()) {
            translateVocab = runCatching { translateVocabFile.readLines() }.getOrNull()
        }
        return translateVocab
    }

    fun close() {
        asrInterpreter?.close()
        translateInterpreter?.close()
        asrInterpreter = null
        translateInterpreter = null
        asrVocab = null
        translateVocab = null
    }
}
