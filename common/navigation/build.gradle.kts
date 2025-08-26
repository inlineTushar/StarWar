plugins {
    alias(libs.plugins.local.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.tsaha.nucleus.navigation"
}

dependencies {
    implementation(libs.kotlinx.serialization)
    api(libs.androidx.navigation.compose)
}
