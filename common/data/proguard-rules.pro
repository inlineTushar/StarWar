# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Koin DI
-keep class org.koin.** { *; }
-keep class * extends org.koin.core.module.Module
-keepclassmembers class * {
    public <init>(...);
}

# Ktor
-keep class io.ktor.** { *; }

# Kotlinx Serialization
-keep class kotlinx.serialization.** { *; }
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Coroutines
-keep class kotlinx.coroutines.** { *; }

# OkHttp
-keep class okhttp3.** { *; }
-keep class okio.** { *; }