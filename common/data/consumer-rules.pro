# Consumer proguard rules for data module
# Add any consumer-specific proguard rules here

# Koin DI
-keep class org.koin.** { *; }

# Keep Ktor classes
-keep class io.ktor.** { *; }

# Keep kotlinx.serialization classes
-keep class kotlinx.serialization.** { *; }
-keep @kotlinx.serialization.Serializable class * { *; }

# Keep coroutines classes  
-keep class kotlinx.coroutines.** { *; }