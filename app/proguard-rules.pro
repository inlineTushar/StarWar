# =============================================================================
# ProGuard Configuration for Nucleus Android App - RELEASE BUILD
# =============================================================================
# 
# This file contains the main ProGuard rules for the app module.
# Module-specific rules are handled via consumer-rules.pro in each library module.
#
# IMPORTANT: Consumer rules from library modules are automatically included:
# - :common:core (Koin, Lifecycle, Coroutines)  
# - :common:data (Ktor, Serialization, OkHttp)
# - :common:navigation (Navigation Compose)
# - :feature:planetlist (Feature-specific ViewModels)
# - :feature:planetdetail (Feature-specific ViewModels)
#
# =============================================================================

# ==============================
# GENERAL ANDROID OPTIMIZATIONS
# ==============================

# Keep debugging information for crash reports
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-renamesourcefileattribute SourceFile

# Keep generic signatures for reflection
-keepattributes *Annotation*,InnerClasses,EnclosingMethod

# ==============================
# JETPACK COMPOSE (App-level)
# ==============================

# Keep main Activity with Compose
-keep class com.tsaha.nucleus.MainActivity { *; }

# Keep Compose compiler annotations
-keep class androidx.compose.runtime.Composable
-keep @androidx.compose.runtime.Composable class * { *; }

# Keep all Compose-related classes
-keep class androidx.compose.** { *; }
-keep class androidx.activity.compose.** { *; }

# Keep composable functions and their parameters
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# Keep Material 3 components
-keep class androidx.compose.material3.** { *; }

# ==============================
# APPLICATION CLASS
# ==============================

# Keep Application class and any custom Application classes
-keep class * extends android.app.Application { *; }
-keep class com.tsaha.nucleus.NucleusApp { *; }

# ==============================
# KOTLIN SPECIFIC
# ==============================

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep Kotlin intrinsics
-keep class kotlin.jvm.internal.** { *; }

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ==============================
# ANDROID FRAMEWORK
# ==============================

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom views and their constructors
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
    *** get*();
}

# Keep Activity/Fragment/Service classes
-keep class * extends androidx.activity.ComponentActivity { *; }
-keep class * extends androidx.fragment.app.Fragment { *; }
-keep class * extends android.app.Service { *; }

# ==============================
# REMOVE LOGGING (PRODUCTION OPTIMIZATION)
# ==============================

# Remove all Log calls (be careful in production - only if you're sure)
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# ==============================
# OPTIMIZATION SETTINGS
# ==============================

# Optimization passes
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# Enable aggressive overloading
-overloadaggressively

# Remove unused code more aggressively
-repackageclasses ''

# ==============================
# WARNINGS TO SUPPRESS
# ==============================

# Suppress warnings for missing classes that are optional
-dontwarn java.lang.instrument.ClassFileTransformer
-dontwarn sun.misc.SignalHandler
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean

# Suppress warnings for optional dependencies
-dontwarn org.slf4j.**
-dontwarn org.apache.log4j.**
-dontwarn org.apache.commons.logging.**

# ==============================
# TESTING FRAMEWORKS (Remove in release)
# ==============================

# Remove test-only classes and methods in production
-assumenosideeffects class * {
    *** junit*(...);
    *** test*(...);
}

# ==============================
# PROJECT PACKAGE STRUCTURE
# ==============================

# Keep main package structure readable for easier debugging
-keep class com.tsaha.nucleus.** { *; }

# ==============================
# FINAL NOTES
# ==============================
#
# Consumer rules automatically included from:
# - :common:core -> Koin DI, AndroidX Lifecycle, Coroutines, Core-ktx
# - :common:data -> Ktor HTTP client, OkHttp, Kotlinx Serialization, Data models  
# - :common:navigation -> Navigation Compose, Route serialization
# - :common:ui -> (No consumer rules needed - covered by app-level Compose rules)
# - :feature:planetlist -> Feature ViewModels and Compose components
# - :feature:planetdetail -> Feature ViewModels and Compose components
#
# =============================================================================