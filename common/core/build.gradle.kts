plugins {
    alias(libs.plugins.local.android.library)
    alias(libs.plugins.local.android.library.koin)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.tsaha.nucleus.core"
}

dependencies {
    // AndroidX Core and Lifecycle (ViewModel support)
    api(libs.androidx.core.ktx)
    api(libs.androidx.lifecycle.runtime.ktx)
    api(libs.androidx.lifecycle.viewmodel.ktx)
    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.androidx.lifecycle.runtime.compose)
    api(libs.androidx.lifecycle.viewmodel.savedstate)

    // Koin - Compose integration (specific to core module)
    // Note: koin-core, koin-android, koin-android-compat now provided by local.android.library.koin plugin
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
    api(libs.assertk)
    testImplementation(libs.androidx.lifecycle.viewmodel.testing)
    // Note: koin-test, koin-test-junit4 now provided by local.android.library.koin plugin
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}