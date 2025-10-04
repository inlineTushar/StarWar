plugins {
    alias(libs.plugins.local.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.tsaha.nucleus.domain"
}

dependencies {
    // Coroutines - All available coroutines libraries
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.android)

    // Koin - Dependency Injection (Core dependencies only)
    api(libs.koin.core)
    api(libs.koin.android)
    api(libs.koin.android.compat)

    // Kotlinx Serialization
    api(libs.kotlinx.serialization)

    // AndroidX Core (useful for domain operations)
    api(libs.androidx.core.ktx)

    // Dependency on data layer
    implementation(project(":common:data"))
    implementation(project(":common:core"))

    // Testing dependencies
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.koin.test)
    testImplementation(libs.koin.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
