plugins {
    alias(libs.plugins.local.android.library)
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

    // Koin - Dependency Injection (Core dependencies only)
    api(libs.koin.core)
    api(libs.koin.android)
    api(libs.koin.android.compat)

    // Kotlinx Serialization (needed for Ktor)
    api(libs.kotlinx.serialization)

    // AndroidX Core (useful for data operations)
    api(libs.androidx.core.ktx)

    // Testing dependencies
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.koin.test)
    testImplementation(libs.koin.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}