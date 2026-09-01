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
    namespace = "com.ct.oemec.translatetflite"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    // IExpandingCapacity/IExpandingCapacityCallback come from oemec's own AIDL sources -
    // compileOnly so we don't compile a second, duplicate copy of those generated classes
    // into this module (that caused a duplicate-class dex-merge failure). At runtime the
    // real classes are already present because oemec is on the app's classpath too.
    compileOnly(project(":oemec"))
    implementation(libs.core.ktx)
    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines.android)

    // Reference-only real IEC provider for the Translate module (see README.md).
    // Pinned versions; this module ships NO model files — see README for the
    // external model-file contract.
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
}
