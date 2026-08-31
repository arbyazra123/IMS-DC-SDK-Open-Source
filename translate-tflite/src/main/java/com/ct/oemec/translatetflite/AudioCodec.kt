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

import android.util.Base64
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Decodes the base64 16-bit PCM mono audio the mini-app sends in a "voice" request into a
 * normalized float array. This is the one piece of the pipeline that's genuinely
 * model-agnostic (raw PCM -> [-1,1] floats); everything downstream of it (what shape/rate
 * the model actually wants) is model-specific — see README.md.
 */
object AudioCodec {

    fun decodePcm16Base64(base64Audio: String): FloatArray {
        val bytes = Base64.decode(base64Audio, Base64.DEFAULT)
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val sampleCount = bytes.size / 2
        val samples = FloatArray(sampleCount)
        for (i in 0 until sampleCount) {
            samples[i] = buffer.short / 32768f
        }
        return samples
    }
}
