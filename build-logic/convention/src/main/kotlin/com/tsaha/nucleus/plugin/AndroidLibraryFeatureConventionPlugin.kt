package com.tsaha.nucleus.plugin

import com.android.build.gradle.LibraryExtension
import com.tsaha.nucleus.ext.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "local.android.library")

            extensions.configure<LibraryExtension> {
                testOptions.animationsDisabled = true

                // Enable proper Android unit testing configuration
                testOptions {
                    unitTests {
                        isIncludeAndroidResources = true
                        // Remove useJUnitPlatform() to fix test discovery issues
                        // Android unit tests work better with the default JUnit4 runner
                    }
                }

                buildFeatures {
                    // Enables Jetpack Compose for this module
                    compose = true
                }
            }

            dependencies {
                "implementation"(libs.findLibrary("androidx.navigation.compose").get())
                "implementation"(project(":common:navigation"))
                "implementation"(project(":common:core"))
                "implementation"(project(":common:ui"))
                "implementation"(project(":common:data"))

                // JUnit5 and testing dependencies
                "testImplementation"(libs.findLibrary("junit5").get())
                "testImplementation"(libs.findLibrary("mockk").get())
                "testImplementation"(libs.findLibrary("kotlinx.coroutines.test").get())
                "testImplementation"(libs.findLibrary("turbine").get())
                "testImplementation"(libs.findLibrary("koin.test").get())
                "testImplementation"(libs.findLibrary("koin.test.junit4").get())

                // Keep JUnit4 for legacy tests
                "testImplementation"(libs.findLibrary("junit").get())
            }
        }
    }
}
