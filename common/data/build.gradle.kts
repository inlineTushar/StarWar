plugins {
    alias(libs.plugins.local.android.library)
    alias(libs.plugins.local.android.library.koin)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.tsaha.nucleus.data"
}

dependencies {
    // Coroutines - All available coroutines libraries
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.android)

    // Ktor - All available Ktor libraries from version catalog
    api(libs.ktor.client.core)
    api(libs.ktor.client.android)
    api(libs.ktor.client.okhttp)
    api(libs.ktor.client.content.negotiation)
    api(libs.ktor.client.logging)
    api(libs.ktor.serialization.kotlinx.json)

    // Note: Koin dependencies (koin-core, koin-android, koin-android-compat) 
    // now provided by local.android.library.koin plugin

    // Kotlinx Serialization (needed for Ktor)
    api(libs.kotlinx.serialization)

    // AndroidX Core (useful for data operations)
    api(libs.androidx.core.ktx)

    implementation(project(":common:core"))

    // Testing dependencies
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    // Note: koin-test, koin-test-junit4 now provided by local.android.library.koin plugin
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}