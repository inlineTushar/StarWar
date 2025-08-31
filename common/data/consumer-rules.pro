# Consumer ProGuard rules for :common:data module
# These rules are automatically applied to any module that depends on this one

# ==============================
# Ktor HTTP Client
# ==============================
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { *; }

# Keep Ktor engine implementations
-keep class io.ktor.client.engine.** { *; }
-keep class io.ktor.client.engine.okhttp.** { *; }

# Keep Ktor serialization
-keep class io.ktor.serialization.** { *; }
-keep class io.ktor.serialization.kotlinx.** { *; }

# Keep Ktor client plugins
-keep class io.ktor.client.plugins.** { *; }

# ==============================
# OkHttp (Ktor's Android engine)
# ==============================
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class okio.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Keep OkHttp interceptors
-keep class * implements okhttp3.Interceptor {
    <init>(...);
    public okhttp3.Response intercept(okhttp3.Interceptor$Chain);
}

# ==============================
# Kotlinx Serialization
# ==============================
-keep class kotlinx.serialization.** { *; }
-keepattributes *Annotation*, InnerClasses

# Keep @Serializable classes - CRITICAL for your data models
-keep @kotlinx.serialization.Serializable class * {
    <fields>;
    <init>(...);
}

# Keep serialization descriptors
-keep class * implements kotlinx.serialization.KSerializer {
    <fields>;
    <methods>;
}

# Keep companion objects for serializable classes
-keepclassmembers @kotlinx.serialization.Serializable class * {
    public static **$Companion Companion;
}

# Keep serialization companion objects
-keepclassmembers class * {
    **$serializer $serializer(...);
}

# Don't warn about kotlinx.serialization compiler plugin generated code
-dontnote kotlinx.serialization.AnnotationsKt
-dontwarn kotlinx.serialization.KSerializer
-dontwarn kotlinx.serialization.Serializable

# ==============================
# Project-specific Data Models
# ==============================
# Keep all your @Serializable data models
-keep class com.tsaha.nucleus.data.model.** { *; }

# Keep API response classes
-keep class com.tsaha.nucleus.data.api.** { *; }

# ==============================
# Koin DI (inherited from core)
# ==============================
-keep class org.koin.** { *; }

# ==============================
# Coroutines (inherited from core)
# ==============================
-keep class kotlinx.coroutines.** { *; }