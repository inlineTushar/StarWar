plugins {
    alias(libs.plugins.local.android.library.compose)
}

android {
    namespace = "com.tsaha.nucleus.ui"
}
dependencies {
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)

    // Testing dependencies
    testImplementation(libs.junit)
}
