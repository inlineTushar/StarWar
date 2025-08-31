# Consumer ProGuard rules for :common:core module
# These rules are automatically applied to any module that depends on this one

# ==============================
# AndroidX Lifecycle + ViewModel
# ==============================
-keep class androidx.lifecycle.** { *; }
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(...);
}

# Keep ViewModelProvider.Factory implementations
-keep class * implements androidx.lifecycle.ViewModelProvider$Factory {
    <init>(...);
}

# ==============================
# Koin Dependency Injection
# ==============================
-keep class org.koin.** { *; }
-keep class * extends org.koin.core.module.Module

# Keep Koin module definitions and functions
-keepclassmembers class * {
    org.koin.core.module.Module *;
}

# Keep classes with Koin annotations
-keep @org.koin.core.annotation.* class * { *; }

# Keep constructors for dependency injection
-keepclassmembers class * {
    public <init>(...);
}

# ==============================
# Kotlinx Coroutines
# ==============================
-keep class kotlinx.coroutines.** { *; }
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep suspend functions
-keepclassmembers class * {
    *** *(*, kotlin.coroutines.Continuation);
}

# ==============================
# AndroidX Core
# ==============================
-keep class androidx.core.** { *; }

# ==============================
# Reflection and Serialization Support
# ==============================
-keepattributes *Annotation*, InnerClasses
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature