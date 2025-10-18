package com.tsaha.nucleus.plugin

import com.tsaha.nucleus.ext.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Convention plugin that adds dependency injection to library modules.
 *
 * Usage in module's build.gradle.kts:
 * ```
 * plugins {
 *     alias(libs.plugins.local.android.library)
 *     alias(libs.plugins.local.android.library.hilt)
 * }
 * ```
 */
class AndroidLibraryHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.dagger.hilt.android")
                apply("com.google.devtools.ksp")
            }

            dependencies {
                // Dependency Injection
                "implementation"(libs.findLibrary("hilt.android").get())
                "ksp"(libs.findLibrary("hilt.compiler").get())

                // Testing dependencies
                "testImplementation"(libs.findLibrary("hilt.android.testing").get())
                "kspTest"(libs.findLibrary("hilt.compiler").get())

                "androidTestImplementation"(libs.findLibrary("hilt.android.testing").get())
                "kspAndroidTest"(libs.findLibrary("hilt.compiler").get())
            }
        }
    }
}
