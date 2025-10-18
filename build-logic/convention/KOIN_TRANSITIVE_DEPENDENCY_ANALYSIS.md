# Koin Transitive Dependency Analysis

## Question: Is it OK to have Koin as a transitive dependency in `:feature:*` modules?

**Short Answer: YES, it's not only OK, but it's the CORRECT approach for this architecture.** ✅

## Why Koin is Transitive in Feature Modules

### Dependency Chain Visualization

```
:feature:planetlist
    |
    | implementation
    ↓
:common:core (has local.android.library.koin plugin)
    |
    | api (transitive!)
    ↓
io.insert-koin:koin-core:4.1.0
io.insert-koin:koin-android:4.1.0
io.insert-koin:koin-android-compat:4.1.0
io.insert-koin:koin-androidx-compose:4.1.0
```

### Actual Gradle Output

```gradle
:feature:planetlist (debugRuntimeClasspath)
+--- project :common:core
|    +--- io.insert-koin:koin-core:4.1.0
|    +--- io.insert-koin:koin-android:4.1.0
|    +--- io.insert-koin:koin-android-compat:4.1.0
|    +--- io.insert-koin:koin-androidx-compose:4.1.0
```

### Why It's Transitive

1. **`:common:core` declares Koin with `api` scope** (via plugin)
2. **Feature modules use `implementation(project(":common:core"))`**
3. **`api` dependencies are transitive** - they propagate to consumers

## Reasons Why This Is The RIGHT Design

### 1. ✅ **Feature Modules NEED Koin APIs**

Feature modules directly use Koin APIs in their code:

```kotlin
// feature/planetlist/di/PlanetListModule.kt
import org.koin.core.module.dsl.viewModel  // ← Needs koin-core
import org.koin.dsl.module                 // ← Needs koin-core

val planetListModule = module {           // ← Koin DSL
    viewModel { PlanetListViewModel(...) } // ← Koin DSL
}
```

```kotlin
// feature/planetlist/PlanetListScreen.kt
import org.koin.androidx.compose.koinViewModel  // ← Needs koin-androidx-compose

@Composable
fun PlanetListScreen(
    vm: PlanetListViewModel = koinViewModel()  // ← Koin Compose API
) { ... }
```

**If Koin wasn't transitive, this code wouldn't compile!**

### 2. ✅ **Follows Clean Architecture Principles**

```
Feature Layer (Presentation)
    ↓ depends on
Common/Core Layer (Infrastructure)
    ↓ provides
Cross-cutting concerns (DI, Lifecycle, Compose, etc.)
```

The feature layer **should** depend on infrastructure provided by core. This is standard layered
architecture.

### 3. ✅ **Avoids Dependency Declaration Duplication**

**BAD Approach** (if we made it `implementation` in core):

```kotlin
// :common:core/build.gradle.kts
dependencies {
    implementation(libs.koin.core)  // ← Not transitive
}

// :feature:planetlist/build.gradle.kts
dependencies {
    implementation(libs.koin.core)  // ← Must declare again!
    implementation(libs.koin.androidx.compose)  // ← And again!
}

// :feature:planetdetail/build.gradle.kts
dependencies {
    implementation(libs.koin.core)  // ← Duplication!
    implementation(libs.koin.androidx.compose)  // ← Duplication!
}
```

**GOOD Approach** (current with `api`):

```kotlin
// :common:core/build.gradle.kts
plugins {
    alias(libs.plugins.local.android.library.koin)  // ← api deps
}

// :feature:planetlist/build.gradle.kts
dependencies {
    implementation(project(":common:core"))  // ← That's it!
}
```

### 4. ✅ **Single Source of Truth**

- Koin version managed in ONE place: `gradle/libs.versions.toml`
- Applied through ONE plugin: `AndroidLibraryKoinConventionPlugin`
- Exposed through ONE module: `:common:core`
- **All feature modules automatically get compatible versions**

### 5. ✅ **Matches Compose, Lifecycle, and Other Framework Dependencies**

Look at `:common:core` - it provides ALL cross-cutting framework concerns as `api`:

```kotlin
// common/core/build.gradle.kts
dependencies {
    // ALL are api (transitive)
    api(libs.androidx.core.ktx)
    api(libs.androidx.lifecycle.runtime.ktx)
    api(libs.androidx.lifecycle.viewmodel.ktx)
    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.koin.core)              // ← Same pattern
    api(libs.koin.android)           // ← Same pattern
    api(libs.koin.androidx.compose)  // ← Same pattern
}
```

**Feature modules use ALL of these APIs directly:**

- `@Composable` functions → needs Compose (transitive ✓)
- ViewModels → needs Lifecycle (transitive ✓)
- `koinViewModel()` → needs Koin (transitive ✓)

## When Would Transitive Be WRONG?

### ❌ Anti-Pattern: Implementation Details Leaking

```kotlin
// BAD: :common:data exposes Ktor as api
dependencies {
    api(libs.ktor.client.core)  // ← Internal implementation!
}

// Now feature modules can accidentally use Ktor directly
// feature/planetlist/SomeScreen.kt
import io.ktor.client.HttpClient  // ← Should NOT be possible!
```

**Our project does this correctly:**

```kotlin
// :common:data/build.gradle.kts
dependencies {
    api(libs.koin.core)  // ← OK! DI is part of public API
    api(libs.ktor.client.core)  // ← OK for data layer!
    // But feature modules don't directly depend on :data
}
```

### ✅ Our Boundary Protection

```
:feature:planetlist
    ↓ depends on
:common:core (DI, Lifecycle, Compose) ✓ api exposure OK
:common:domain (Use cases) ✓ api exposure OK
    ↓ depends on
:common:data (Repositories) ✓ Hidden from features!
    ↓ uses internally
Ktor, Database, etc. ✓ Implementation details hidden!
```

## Comparison: `api` vs `implementation`

| Aspect | `api` (current) | `implementation` |
|--------|----------------|------------------|
| **Visibility** | Transitive to consumers | Hidden from consumers |
| **Feature module must declare?** | No | Yes |
| **Version consistency** | Automatic | Manual (error-prone) |
| **Compile-time check** | Yes | Only if declared |
| **Use case** | Public API, framework | Internal implementation |

## Real-World Example

### Current Setup (Correct)

```kotlin
// :common:core provides Koin as api
// :feature:planetlist/di/PlanetListModule.kt
import org.koin.dsl.module  // ← Compiles! (transitive from :core)

val planetListModule = module {
    viewModel { PlanetListViewModel(get()) }
}
```

### If We Changed to `implementation`

```kotlin
// :common:core provides Koin as implementation (NOT transitive)
// :feature:planetlist/di/PlanetListModule.kt
import org.koin.dsl.module  // ← COMPILATION ERROR!
                            // "Unresolved reference: org.koin"

val planetListModule = module {  // �� Can't compile
    viewModel { PlanetListViewModel(get()) }
}
```

**You'd need to add Koin to EVERY feature module manually:**

```kotlin
// :feature:planetlist/build.gradle.kts
dependencies {
    implementation(project(":common:core"))
    implementation(libs.koin.core)  // ← Now required
    implementation(libs.koin.androidx.compose)  // ← Now required
}

// :feature:planetdetail/build.gradle.kts
dependencies {
    implementation(project(":common:core"))
    implementation(libs.koin.core)  // ← Duplication!
    implementation(libs.koin.androidx.compose)  // ← Duplication!
}
```

## Best Practices Summary

### ✅ Use `api` (transitive) for:

1. **Framework APIs** that consumers directly call (Koin, Compose, Lifecycle)
2. **Public interfaces** from your architecture (Repository interfaces)
3. **DTOs/Models** shared across layers
4. **Cross-cutting concerns** (DI, logging facades, etc.)

### ✅ Use `implementation` (non-transitive) for:

1. **Internal implementation details** (Ktor client, database driver)
2. **Utilities** used only within the module
3. **Dependencies** that shouldn't leak to consumers

## Our Architecture Decision

```
┌────────────────────────────────────────────────────────┐
│ :common:core - Infrastructure Layer                   │
│                                                        │
│ Responsibilities:                                      │
│ • Provide DI framework (Koin) → api ✓                │
│ • Provide UI framework (Compose) → api ✓             │
│ • Provide Lifecycle APIs → api ✓                     │
│                                                        │
│ Why api?                                              │
│ Feature modules DIRECTLY USE these APIs in their code │
└────────────────────────────────────────────────────────┘
                        ↑
                        | implementation (depends on)
                        |
┌────────────────────────────────────────────────────────┐
│ :feature:* - Feature Layer                            │
│                                                        │
│ Uses from :core (transitive):                         │
│ • koinViewModel() → Koin Compose API                  │
│ • module { } → Koin DSL                               │
│ • @Composable → Compose API                           │
│ • ViewModel → Lifecycle API                           │
│                                                        │
│ Benefits:                                              │
│ • No duplicate declarations                            │
│ • Version consistency guaranteed                       │
│ • Compile-time safety                                  │
└────────────────────────────────────────────────────────┘
```

## Conclusion

**Is transitive Koin in feature modules OK?**

✅ **YES - It's not just OK, it's ESSENTIAL and follows best practices.**

**Reasons:**

1. Feature modules **directly use** Koin APIs (`koinViewModel()`, `module { }`, etc.)
2. Koin is a **framework/infrastructure concern** provided by `:common:core`
3. Makes Koin part of the **public API contract** of the core module
4. Eliminates duplicate declarations across feature modules
5. Ensures version consistency automatically
6. Standard practice for framework dependencies (same as Compose, Lifecycle)
7. Follows clean architecture: infrastructure layer provides tools to feature layer

**The transitive dependency is intentional, correct, and follows Android/Kotlin best practices.** 🎯
