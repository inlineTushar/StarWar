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
            }
        }
    }
}
