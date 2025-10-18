package com.tsaha.nucleus.plugin

import com.tsaha.nucleus.ext.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Convention plugin that adds Koin dependencies to library modules.
 * Apply this only to modules that require dependency injection.
 *
 * Usage in module's build.gradle.kts:
 * ```
 * plugins {
 *     alias(libs.plugins.local.android.library)
 *     alias(libs.plugins.local.android.library.koin)
 * }
 * ```
 */
class AndroidLibraryKoinConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            dependencies {
                // Koin - Dependency Injection (Core dependencies)
                "api"(libs.findLibrary("koin.core").get())
                "api"(libs.findLibrary("koin.android").get())
                "api"(libs.findLibrary("koin.android.compat").get())

                // Koin - Testing dependencies
                "testImplementation"(libs.findLibrary("koin.test").get())
                "testImplementation"(libs.findLibrary("koin.test.junit4").get())
            }
        }
    }
}
