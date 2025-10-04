# Consumer ProGuard rules for :common:domain module
# These rules are automatically applied to any module that depends on this one

# ==============================
# Kotlinx Serialization
# ==============================
-keep class kotlinx.serialization.** { *; }
-keepattributes *Annotation*, InnerClasses

# Keep @Serializable classes - CRITICAL for your domain models
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
# Project-specific Domain Models
# ==============================
# Keep all domain models and use cases
-keep class com.tsaha.nucleus.domain.model.** { *; }
-keep class com.tsaha.nucleus.domain.usecase.** { *; }

# ==============================
# Koin DI (inherited from core)
# ==============================
-keep class org.koin.** { *; }

# ==============================
# Coroutines (inherited from core)
# ==============================
-keep class kotlinx.coroutines.** { *; }
