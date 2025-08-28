package com.tsaha.nucleus.plugin

import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.LibraryExtension
import com.tsaha.nucleus.ext.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import kotlin.apply

class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "local.android.library")
            apply(plugin = "org.jetbrains.kotlin.plugin.compose")

            val extension = extensions.getByType<LibraryExtension>()
            configureAndroidCompose(extension)
        }
    }
}

/**
 * Configure Compose-specific options
 */
internal fun Project.configureAndroidCompose(commonExtension: CommonExtension<*, *, *, *, *, *>) {
    commonExtension.apply {
        buildFeatures {
            compose = true
        }

        dependencies {
            val composeBom = libs.findLibrary("androidx-compose-bom").get()
            "implementation"(platform(composeBom))
            "implementation"(libs.findLibrary("androidx.material3").get())
            "implementation"(libs.findLibrary("androidx-ui-tooling-preview").get())
            "implementation"(project(":common:data"))

            "androidTestImplementation"(platform(composeBom))
            "debugImplementation"(libs.findLibrary("androidx-ui-tooling").get())
        }

        testOptions {
            unitTests {
                // For Robolectric
                isIncludeAndroidResources = true
            }
        }
    }
}