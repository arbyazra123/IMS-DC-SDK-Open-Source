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

plugins {
    id("newcall.android.library")
}

android {
    namespace = "com.ct.oemec.speechkittranslate"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    // IExpandingCapacity/IExpandingCapacityCallback come from oemec's own AIDL sources -
    // compileOnly so we don't compile a duplicate copy of those generated classes (see
    // translate-tflite's build.gradle.kts for the dex-merge failure this avoids).
    compileOnly(project(":oemec"))
    implementation(libs.core.ktx)
    implementation(libs.gson)

    // Real, on-device, Play-Services-backed ASR (android.speech.SpeechRecognizer, platform
    // API - no extra dependency) + MT (ML Kit Translate). See README.md for what this
    // actually requires on-device (Google Play Services, one-time model download).
    implementation("com.google.mlkit:translate:17.0.3")
}
