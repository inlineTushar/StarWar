# Consumer ProGuard rules for :feature:planetlist module
# These rules are automatically applied to any module that depends on this one

# ==============================
# Feature-specific ViewModels
# ==============================
-keep class com.tsaha.planetlist.** { *; }

# Keep ViewModel classes
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ==============================
# Jetpack Compose
# ==============================
-keep class androidx.compose.** { *; }
-keep @androidx.compose.runtime.Composable class * { *; }

# Keep composable functions
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# ==============================
# Koin ViewModel
# ==============================
# Keep parameterized ViewModels for Koin
-keep class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}