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
                    }
                }

                buildFeatures {
                    // Enables Jetpack Compose for this module
                    compose = true
                }

                packaging {
                    resources {
                        excludes += "/META-INF/{AL2.0,LGPL2.1}"
                        excludes += "/META-INF/LICENSE.md"
                        excludes += "/META-INF/LICENSE-notice.md"
                        excludes += "/META-INF/NOTICE.md"
                    }
                }
            }

            dependencies {
                "implementation"(libs.findLibrary("androidx.navigation.compose").get())
                "implementation"(project(":common:navigation"))
                "implementation"(project(":common:core"))
                "implementation"(project(":common:ui"))
                "implementation"(project(":common:data"))

                // Unit testing dependencies
                "testImplementation"(libs.findLibrary("junit5").get())
                "testImplementation"(libs.findLibrary("mockk").get())
                "testImplementation"(libs.findLibrary("kotlinx.coroutines.test").get())
                "testImplementation"(libs.findLibrary("turbine").get())
                "testImplementation"(libs.findLibrary("koin.test").get())
                "testImplementation"(libs.findLibrary("koin.test.junit4").get())
                "testImplementation"(libs.findLibrary("junit").get())
                "testImplementation"(project(":common:core"))

                // Android UI testing dependencies (centralized for all feature modules)
                add("androidTestImplementation", libs.findLibrary("androidx.junit").get())
                add("androidTestImplementation", libs.findLibrary("androidx.espresso.core").get())
                add("androidTestImplementation", libs.findLibrary("androidx.ui.test.junit4").get())
                add("debugImplementation", libs.findLibrary("androidx.ui.test.manifest").get())
                add("debugImplementation", libs.findLibrary("androidx.ui.tooling").get())

                // Additional UI testing dependencies for Phase 4 Integration Tests
                add("androidTestImplementation", "androidx.test.ext:junit:1.2.1")
                add("androidTestImplementation", "androidx.test.espresso:espresso-core:3.6.1")
                add("androidTestImplementation", "androidx.compose.ui:ui-test-junit4:1.7.5")
                add("androidTestImplementation", "io.mockk:mockk-android:1.13.8")
                add("androidTestImplementation", "com.willowtreeapps.assertk:assertk:0.28.1")
                add("debugImplementation", "androidx.compose.ui:ui-test-manifest:1.7.5")
                add("debugImplementation", "androidx.compose.ui:ui-tooling:1.7.5")

                // Note: AssertK is also inherited from :common:core as api dependency for unit tests
                // Note: Additional UI Testing capabilities inherited from :common:ui for Phase 1-3 tests
            }
        }
    }
}
