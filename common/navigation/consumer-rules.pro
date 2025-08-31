# Consumer ProGuard rules for :common:navigation module
# These rules are automatically applied to any module that depends on this one

# ==============================
# AndroidX Navigation Compose
# ==============================
-keep class androidx.navigation.** { *; }
-keep class androidx.navigation.compose.** { *; }

# Keep NavController and related classes
-keep class androidx.navigation.NavController { *; }
-keep class androidx.navigation.NavDestination { *; }
-keep class androidx.navigation.NavGraph { *; }

# ==============================
# Navigation Routes (Serializable)
# ==============================
# Keep all route classes and their serialization
-keep class com.tsaha.navigation.** { *; }

# Keep sealed class hierarchy for routes
-keep class * extends com.tsaha.navigation.Route { *; }

# Keep serialization for navigation routes
-keep @kotlinx.serialization.Serializable class com.tsaha.navigation.** {
    <fields>;
    <init>(...);
}

# ==============================
# Kotlinx Serialization (for routes)
# ==============================
-keep class kotlinx.serialization.** { *; }
-keep @kotlinx.serialization.Serializable class * {
    <fields>;
    <init>(...);
}