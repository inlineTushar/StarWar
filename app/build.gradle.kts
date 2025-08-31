plugins {
    alias(libs.plugins.local.android.application)
}

android {
    namespace = "com.tsaha.nucleus"

    signingConfigs {
        create("debugSign") {
            storeFile = file("${rootDir}/keystore/debug.keystore.jks")
            keyAlias = "debugKey"
            keyPassword = "android"
            storePassword = "android"
        }
        create("releaseSign") {
            storeFile = file("${rootDir}/keystore/release.keystore.jks")
            // source from /Users/{user.name}/gradle.properties
            keyAlias = project.findProperty("STAR_RELEASE_KEY_ALIAS") as String?
                ?: throw GradleException("STAR_RELEASE_KEY_ALIAS property not found in gradle.properties")
            keyPassword = project.findProperty("STAR_RELEASE_KEY_PASSWORD") as String?
                ?: throw GradleException("STAR_RELEASE_KEY_PASSWORD property not found in gradle.properties")
            storePassword = project.findProperty("STAR_RELEASE_STORE_PASSWORD") as String?
                ?: throw GradleException("STAR_RELEASE_STORE_PASSWORD property not found in gradle.properties")
        }
    }
    buildTypes {
        debug {
            applicationIdSuffix = ".starwars.debug"
            signingConfig = signingConfigs.getByName("debugSign")
        }
        release {
            applicationIdSuffix = ".starwars.release"
            signingConfig = signingConfigs.getByName("releaseSign")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    // Feature modules - app-specific dependencies
    implementation(project(":feature:planetlist"))
    implementation(project(":feature:planetdetail"))
}