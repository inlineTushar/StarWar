plugins {
    alias(libs.plugins.local.android.application)
}

android {
    namespace = "com.tsaha.nucleus"
}

dependencies {
    // Feature modules - app-specific dependencies
    implementation(project(":feature:starlist"))
    implementation(project(":feature:stardetail"))
}