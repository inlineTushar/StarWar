package com.tsaha.nucleus.plugin

import com.android.build.api.dsl.ApplicationExtension
import com.tsaha.nucleus.ext.configureKotlin
import com.tsaha.nucleus.ext.libs
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class AndroidAppConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.application")
            apply(plugin = "org.jetbrains.kotlin.android")
            apply(plugin = "org.jetbrains.kotlin.plugin.compose")
            apply(plugin = "local.android.lint")

            extensions.configure<ApplicationExtension> {
                configureAppKotlinAndroid(this)
                configureAndroidCompose(this)
                configureAppSpecifics(this)
            }

            dependencies {
                // Common modules
                "implementation"(project(":common:core"))
                "implementation"(project(":common:navigation"))
                "implementation"(project(":common:ui"))

                // Core Android dependencies
                "implementation"(libs.findLibrary("androidx.core.ktx").get())
                "implementation"(libs.findLibrary("androidx.lifecycle.runtime.ktx").get())
                "implementation"(libs.findLibrary("androidx.activity.compose").get())

                // Compose BOM and UI dependencies
                "implementation"(platform(libs.findLibrary("androidx.compose.bom").get()))
                "implementation"(libs.findLibrary("androidx.ui").get())
                "implementation"(libs.findLibrary("androidx.ui.graphics").get())
                "implementation"(libs.findLibrary("androidx.ui.tooling.preview").get())
                "implementation"(libs.findLibrary("androidx.material3").get())

                // Test dependencies
                "testImplementation"(libs.findLibrary("junit").get())
                "androidTestImplementation"(libs.findLibrary("androidx.junit").get())
                "androidTestImplementation"(libs.findLibrary("androidx.espresso.core").get())
                "androidTestImplementation"(
                    platform(
                        libs.findLibrary("androidx.compose.bom").get()
                    )
                )
                "androidTestImplementation"(libs.findLibrary("androidx.ui.test.junit4").get())

                // Debug dependencies
                "debugImplementation"(libs.findLibrary("androidx.ui.tooling").get())
                "debugImplementation"(libs.findLibrary("androidx.ui.test.manifest").get())
            }
        }
    }
}

/**
 * Configure base Kotlin with Android options for app module
 */
private fun Project.configureAppKotlinAndroid(appExtension: ApplicationExtension) {
    appExtension.apply {
        compileSdk = 36

        defaultConfig {
            applicationId = "com.tsaha.nucleus"
            minSdk = 24
            targetSdk = 36
            versionCode = 1
            versionName = "1.0"
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
    }
    configureKotlin<KotlinAndroidProjectExtension>()
}

/**
 * Configure app-specific options
 */
private fun Project.configureAppSpecifics(appExtension: ApplicationExtension) {
    appExtension.apply {
        buildTypes {
            release {
                isMinifyEnabled = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
        }
    }
}