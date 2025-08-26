import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
    alias(libs.plugins.android.lint)
}

group = "com.tsaha.nucleus.buildlogic"

// Configure the build-logic plugins to target JDK 17
// This matches the JDK used to build the project, and is not related to what is running on device.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.android.tools.common)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    lintChecks(libs.androidx.lint.gradle)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("androidApp") {
            id = libs.plugins.local.android.application.get().pluginId
            implementationClass = "com.tsaha.nucleus.plugin.AndroidAppConventionPlugin"
        }
        register("androidLibrary") {
            id = libs.plugins.local.android.library.asProvider().get().pluginId
            implementationClass = "com.tsaha.nucleus.plugin.AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = libs.plugins.local.android.library.compose.get().pluginId
            implementationClass = "com.tsaha.nucleus.plugin.AndroidLibraryComposeConventionPlugin"
        }
        register("androidFeature") {
            id = libs.plugins.local.android.feature.get().pluginId
            implementationClass = "com.tsaha.nucleus.plugin.AndroidLibraryFeatureConventionPlugin"
        }
        register("androidLint") {
            id = libs.plugins.local.android.lint.get().pluginId
            implementationClass = "com.tsaha.nucleus.plugin.AndroidLintConventionPlugin"
        }
    }
}