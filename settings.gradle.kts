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

pluginManagement {
    includeBuild("build-logic")
    repositories {
        maven(url = "https://maven.aliyun.com/repository/public") {
            // Both aliyun mirrors serve a 502 for com.google.devtools.ksp instead of a
            // clean "not found", which Gradle treats as fatal rather than falling through
            // to the next repository - skip them for this group entirely.
            content { excludeGroup("com.google.devtools.ksp") }
        }
        maven(url = "https://maven.aliyun.com/repository/google") {
            content { excludeGroup("com.google.devtools.ksp") }
        }
        google()
        mavenCentral()
        gradlePluginPortal()
        flatDir {
            dirs("libs")
        }
    }
    resolutionStrategy {
        eachPlugin {
            // Gradle's plugin-marker/Plugin-Portal resolution path fails to find this
            // artifact in this environment even though it exists on Maven Central -
            // force it through ordinary module dependency resolution instead, which
            // works against the same declared repositories.
            if (requested.id.id == "com.google.devtools.ksp") {
                useModule("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:${requested.version}")
            }
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven(url = "https://maven.aliyun.com/repository/public") {
            content { excludeGroup("com.google.devtools.ksp") }
        }
        maven(url = "https://maven.aliyun.com/repository/google") {
            content { excludeGroup("com.google.devtools.ksp") }
        }
        maven(url = "https://jitpack.io")
        google()
        mavenCentral()
        flatDir {
            dirs("libs")
        }
    }
}

rootProject.name = "NewCall"
include(":app")
include(":core")
include(":base")
include(":oemec")
include(":nativelibs")
include(":testing")
include(":translate-tflite")
include(":translate-speechkit")
