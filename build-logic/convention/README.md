# Convention Plugins

This directory contains custom Gradle convention plugins that standardize configuration across the
project.

## Available Plugins

### 1. `local.android.application`

**Implementation**: `AndroidAppConventionPlugin`

Configures the main Android application module with:

- Android application plugin
- Kotlin Android plugin
- Compose compiler plugin
- Standard dependencies (core, lifecycle, compose, etc.)
- Build types (debug/release)
- Common test dependencies

**Usage**: Applied only to `:app` module.

### 2. `local.android.library`

**Implementation**: `AndroidLibraryConventionPlugin`

Base plugin for all Android library modules providing:

- Android library plugin
- Kotlin Android plugin
- Standard build configuration (compileSdk, minSdk, targetSdk)
- BuildConfig support
- Automatic resource prefixing based on module path
- Test infrastructure setup

**Usage**: Applied to all library modules as base configuration.

### 3. `local.android.library.compose`

**Implementation**: `AndroidLibraryComposeConventionPlugin`

Adds Jetpack Compose support to library modules:

- Compose compiler plugin
- Compose dependencies
- Compose-specific build features

**Usage**: Applied to modules using Compose UI (`:common:ui`, feature modules).

### 4. `local.android.library.koin` ⭐ NEW

**Implementation**: `AndroidLibraryKoinConventionPlugin`

Adds Koin dependency injection support to library modules:

- `api(koin-core)`
- `api(koin-android)`
- `api(koin-android-compat)`
- `testImplementation(koin-test)`
- `testImplementation(koin-test-junit4)`

**Usage**: Applied **only** to modules that need dependency injection.

**Benefits**:

- ✅ **DRY Principle**: Eliminates duplicate Koin dependency declarations
- ✅ **Opt-in**: Modules without DI don't get unnecessary dependencies
- ✅ **Centralized**: Update Koin configuration in one place
- ✅ **Consistent**: All DI modules use identical Koin setup
- ✅ **Type-safe**: Version catalog ensures correct dependencies

**Applied to**:

- `:common:core` (+ `koin-androidx-compose`)
- `:common:data`
- `:common:domain`

**NOT applied to**:

- `:common:ui` (pure UI components, no DI needed)
- `:common:navigation` (navigation types only)

### 5. `local.android.feature`

**Implementation**: `AndroidLibraryFeatureConventionPlugin`

Comprehensive plugin for feature modules combining:

- Base library configuration
- Common module dependencies (`:common:core`, `:common:ui`, etc.)
- Navigation support
- Koin test dependencies
- Unit testing setup (JUnit5, MockK, Turbine, etc.)
- Android UI testing setup (Compose UI tests, Espresso, etc.)

**Usage**: Applied to all feature modules (`:feature:planetlist`, `:feature:planetdetail`).

### 6. `local.android.lint`

**Implementation**: `AndroidLintConventionPlugin`

Configures Android Lint with:

- XML and SARIF reports
- Dependency checking
- Custom rule configuration

## Architecture Decision: Why NOT Put Koin in AndroidLibraryConventionPlugin?

### ❌ Bad Approach

```kotlin
// DON'T: Adding to AndroidLibraryConventionPlugin
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // This applies to ALL library modules
        dependencies {
            api(libs.koin.core) // ❌ Forces Koin on modules that don't need it
        }
    }
}
```

### ✅ Good Approach

```kotlin
// DO: Separate, opt-in plugin
class AndroidLibraryKoinConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // Only applied to modules that explicitly opt-in
        dependencies {
            api(libs.koin.core) // ✅ Intentional choice per module
        }
    }
}
```

### Reasoning

1. **Not all modules need DI**: UI component libraries, navigation types, and utility modules don't
   require Koin
2. **Separation of Concerns**: Base library configuration should not mandate architectural choices
3. **Dependency Minimization**: Smaller dependency graphs = faster builds and smaller APKs
4. **Flexibility**: New modules can choose whether they need DI support
5. **Clear Intent**: Explicit plugin application documents which modules use DI

## Module Dependency Strategy

```
┌─────────────────────────────────────────────┐
│  :common:core (Koin + Compose + Lifecycle)  │ ← api(koin-*)
└─────────────────┬───────────────────────────┘
                  │
    ┌─────────────┼─────────────┬─────────────┐
    │             │             │             │
┌───▼────┐   ┌───▼────┐   ┌───▼────┐   ┌────▼────┐
│ :data  │   │:domain │   │  :ui   │   │  :nav   │
│(Koin)  │   │(Koin)  │   │(no DI) │   │(no DI)  │
└────────┘   └────────┘   └────────┘   └─────────┘
```

- **Koin-enabled modules**: Explicitly apply `local.android.library.koin`
- **Pure modules**: Only apply `local.android.library` (and optionally `compose`)

## Example Usage

### Module with Koin

```kotlin
// common/data/build.gradle.kts
plugins {
    alias(libs.plugins.local.android.library)
    alias(libs.plugins.local.android.library.koin)  // ← Opt-in to Koin
    alias(libs.plugins.kotlin.serialization)
}
```

### Module without Koin

```kotlin
// common/ui/build.gradle.kts
plugins {
    alias(libs.plugins.local.android.library)
    alias(libs.plugins.local.android.library.compose)  // Compose only
}
```

### Feature Module

```kotlin
// feature/planetlist/build.gradle.kts
plugins {
    alias(libs.plugins.local.android.feature)  // Includes everything
    alias(libs.plugins.local.android.library.compose)
}
```

## Maintenance

When updating Koin:

1. Update version in `gradle/libs.versions.toml`
2. No need to touch individual module build files
3. All Koin-enabled modules get updated automatically via plugin

When adding a new module:

1. Start with `local.android.library`
2. Add `local.android.library.koin` only if module needs DI
3. Add `local.android.library.compose` only if module uses Compose UI
