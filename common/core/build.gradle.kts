plugins {
    alias(libs.plugins.local.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.tsaha.nucleus.core"
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    // AndroidX Core and Lifecycle (ViewModel support)
    api(libs.androidx.core.ktx)
    api(libs.androidx.lifecycle.runtime.ktx)
    api(libs.androidx.lifecycle.viewmodel.ktx)
    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.androidx.lifecycle.runtime.compose)
    api(libs.androidx.lifecycle.viewmodel.savedstate)
    api(libs.androidx.lifecycle.livedata.ktx)
    api(libs.androidx.lifecycle.common.java8)

    // Koin - Dependency Injection
    api(libs.koin.core)
    api(libs.koin.android)
    api(libs.koin.android.compat)
    api(libs.koin.androidx.navigation)
    api(libs.koin.androidx.compose)

    // Ktor - Networking
    api(libs.ktor.client.core)
    api(libs.ktor.client.android)
    api(libs.ktor.client.okhttp)
    api(libs.ktor.client.content.negotiation)
    api(libs.ktor.client.logging)
    api(libs.ktor.serialization.kotlinx.json)

    // Kotlinx Serialization
    api(libs.kotlinx.serialization)

    // Coroutines (usually needed with ViewModel and Ktor)
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.android)

    // Testing dependencies
    testImplementation(libs.junit)
    testImplementation(libs.androidx.lifecycle.viewmodel.testing)
    testImplementation(libs.koin.test)
    testImplementation(libs.koin.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}