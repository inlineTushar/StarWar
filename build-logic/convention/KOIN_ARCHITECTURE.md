# Koin Architecture Visualization

This document visualizes how Koin dependency injection flows through the StarWar project, from the
convention plugin to feature modules.

## 1. Plugin-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    gradle/libs.versions.toml                    │
│                                                                 │
│  [versions]                                                     │
│  koin = "4.1.0"                                                 │
│                                                                 │
│  [libraries]                                                    │
│  koin-core = { ... version.ref = "koin" }                      │
│  koin-android = { ... version.ref = "koin" }                   │
│  koin-android-compat = { ... version.ref = "koin" }            │
│  koin-androidx-compose = { ... version.ref = "koin" }          │
│  koin-test = { ... version.ref = "koin" }                      │
│  koin-test-junit4 = { ... version.ref = "koin" }               │
│                                                                 │
│  [plugins]                                                      │
│  local-android-library-koin = { id = "..." }                   │
└─────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│      AndroidLibraryKoinConventionPlugin (NEW!)                  │
│      build-logic/convention/.../plugin/                         │
│                                                                 │
│  class AndroidLibraryKoinConventionPlugin : Plugin<Project> {  │
│      override fun apply(target: Project) {                     │
│          dependencies {                                         │
│              "api"(koin-core)           ◄── Transitive         │
│              "api"(koin-android)        ◄── Transitive         │
│              "api"(koin-android-compat) ◄── Transitive         │
│              "testImplementation"(koin-test)                    │
│              "testImplementation"(koin-test-junit4)             │
│          }                                                      │
│      }                                                          │
│  }                                                              │
└─────────────────────────────────────────────────────────────────┘
```

## 2. Module Dependency Hierarchy

```
┌────────────────────────────────────────────────────────────────────────────┐
│                          :app (Application)                                │
│  • Initializes Koin with startKoin { }                                    │
│  • Depends on: :feature:*, :common:*                                      │
│  • Gets Koin transitively from :common:core                               │
└────────────────────────────────────────────────────────────────────────────┘
                                    │
                ┌───────────────────┼───────────────────┐
                │                   │                   │
                ▼                   ▼                   ▼
┌───────────────────────┐ ┌───────────────────┐ ┌───────────────────┐
│ :feature:planetlist   │ │:feature:planetdetail│ │  :common:core    │
│ ┌───────────────────┐ │ │ ┌───────────────┐ │ │ ┌��─────────────┐ │
│ │ Plugins:          │ │ │ │ Plugins:      │ │ │ │ Plugins:     │ │
│ │ • local.android.  │ │ │ │ • local.android│ │ │ │ • local.     │ │
│ │   feature         │ │ │ │   .feature    │ │ │ │   android.   │ │
│ │ • local.android.  │ │ │ │ • local.android│ │ │ │   library    │ │
│ │   library.compose │ │ │ │   .library.   │ │ │ │ • local.     │ │
│ │                   │ │ │ │   compose     │ │ │ │   android.   │ │
│ └───────────────────┘ │ │ └───────────────┘ │ │ │   library.   │ │
│                       │ │                   │ │ │   koin ✓     │ │
│ Uses Koin via:        │ │ Uses Koin via:    │ │ └──────────────┘ │
│ • Transitive from     │ │ • Transitive from │ │                  │
│   :common:core        │ │   :common:core    │ │ Provides:        │
│                       │ │                   │ │ • api(koin-*)    │
│ DI Module:            │ │ DI Module:        │ │ • Compose        │
│ planetListModule      │ │ planetDetailModule│ │ • Lifecycle      │
└───────────────────────┘ └───────────────────┘ └───────────────────┘
        │ depends                 │ depends             │ api
        └─────────────────────────┼─────────────────────┘
                                  │
                ┌─────────────────┼─────────────────┬──────────────┐
                ▼                 ▼                 ▼              ▼
    ┌─────────────────┐ ┌─────────────────┐ ┌──────────┐ ��─────────────┐
    │  :common:data   │ │:common:domain   │ │:common:ui│ │:common:nav  │
    │ ┌─────────────┐ │ │ ┌─────────────┐ │ │          │ │             │
    │ │ Plugins:    │ │ │ │ Plugins:    │ │ │ Plugins: │ │ Plugins:    │
    │ │ • local.    │ │ │ │ • local.    │ │ │ • local. │ │ • local.    │
    │ │   android.  │ │ │ │   android.  │ │ │   android│ │   android.  │
    │ │   library   │ │ │ │   library   │ │ │   .library│ │   library  │
    │ │ • local.    │ │ │ │ • local.    │ │ │ • local. │ │             │
    │ │   android.  │ │ │ │   android.  │ │ │   android│ │ NO KOIN ✗   │
    │ │   library.  │ │ │ │   library.  │ │ │   .library│ │             │
    │ │   koin ✓    │ │ │ │   koin ✓    │ │ │   .compose│ │            │
    │ └─────────────┘ │ │ └─────────────┘ │ │          │ │             │
    │                 │ │                 │ │ NO KOIN ✗│ │             │
    │ DI Module:      │ │ DI Module:      │ │          │ │             │
    │ • dataModule    │ │ • domainModule  │ │          │ │             │
    │ • httpModule    │ │                 │ │          │ │             │
    └─────────────────┘ └─────────────────┘ └──────────┘ └─────────────┘
```

## 3. Koin Module Initialization Flow

```
┌──────────────────────────────────────────────────────────────────────┐
│  Step 1: Application Startup                                        │
│  File: app/src/main/java/com/tsaha/nucleus/NucleusApp.kt           │
└──────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
        ┌───────────────────────────────────────┐
        │  class NucleusApp : Application() {   │
        │      override fun onCreate() {        │
        │          super.onCreate()             │
        │          startKoin {                  │ ◄── Koin Initialization
        │              androidContext(this)     │
        │              modules(                 │
        │                  coreModule,          │ ◄── Step 2
        │                  dataModule,          │ ◄── Step 3
        │                  domainModule,        │ ◄── Step 4
        │                  planetListModule,    │ ◄── Step 5
        │                  planetDetailModule   │ ◄── Step 6
        │              )                        │
        │          }                            │
        │      }                                │
        │  }                                    │
        └───────────────────────────────────────┘
                                │
                                ▼
┌──────────────────────────────────────────────────────────────────────┐
│  Step 2: Core Module (Empty placeholder)                            │
│  File: common/core/src/main/java/.../di/CoreModule.kt              │
│                                                                      │
│  val coreModule = module {                                          │
│      // Currently empty - reserved for core dependencies           │
│  }                                                                  │
└──────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌──────────────────────────────────────────────────────────────────────┐
│  Step 3: Data Module (HTTP + Repository layer)                      │
│  File: common/data/src/main/java/.../di/DataModule.kt              │
│                                                                      │
│  val httpModule = module {                                          │
│      single<HttpClient> { httpClient() }  ◄── Ktor HTTP client     │
│  }                                                                  │
│                                                                      │
│  val dataModule = module {                                          │
│      includes(httpModule)                 ◄── Module composition   │
│      singleOf(::PlanetApiImpl) { bind<PlanetApi>() }               │
│      singleOf(::InMemoryPlanetDataSource) {                        │
│          bind<PlanetLocalDataSource>()                             │
│      }                                                              │
│      singleOf(::PlanetRemoteDataSource)                            │
│      singleOf(::PlanetRepositoryImpl) {                            │
│          bind<PlanetRepository>()                                  │
│      }                                                              │
│  }                                                                  │
│                                                                      │
│  Koin DSL Used:                                                     │
│  • single<T> - Singleton instance                                  │
│  • singleOf() - Constructor injection                              │
│  • bind<Interface>() - Interface binding                           │
│  • includes() - Module composition                                 │
└──────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌──────────────────────────────────────────────────────────────────────┐
│  Step 4: Domain Module (Use Cases)                                  │
│  File: common/domain/src/main/java/.../di/DomainModule.kt          │
│                                                                      │
│  val domainModule = module {                                        │
│      singleOf(::PlanetListUseCase)    ◄── Business logic           │
│  }                                                                  │
│                                                                      │
│  Dependencies resolved from dataModule via get()                   │
└──────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌──────────────────────────────────────────────────────────────────────┐
│  Step 5: PlanetList Feature Module (ViewModel)                      │
│  File: feature/planetlist/src/main/java/.../di/PlanetListModule.kt │
│                                                                      │
│  val planetListModule = module {                                    │
│      viewModel {                                                    │
│          PlanetListViewModel(                                       │
│              planetListUseCase = get()  ◄── Resolved from domain   │
│          )                                                          │
│      }                                                              │
│  }                                                                  │
│                                                                      │
│  Koin DSL Used:                                                     │
│  • viewModel { } - ViewModel factory                               │
│  • get() - Dependency resolution                                   │
└──────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌──────────────────────────────────────────────────────────────────────┐
│  Step 6: PlanetDetail Feature Module (Parameterized ViewModel)      │
│  File: feature/planetdetail/src/main/java/.../di/PlanetDetailModule│
│                                                                      │
│  val planetDetailModule = module {                                  │
│      viewModel { (planetId: String) ->    ◄── Runtime parameter    │
│          PlanetDetailViewModel(                                     │
│              planetId = planetId,                                   │
│              planetRepository = get()     ◄── Resolved from data   │
│          )                                                          │
│      }                                                              │
│  }                                                                  │
│                                                                      │
│  Koin DSL Used:                                                     │
│  • viewModel { (param) -> } - Parameterized ViewModel              │
│  • get() - Dependency resolution                                   │
└──────────────────────────────────────────────────────────────────────┘
```

## 4. Compose Integration Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│  UI Layer: Compose Screens                                         │
└─────────────────────────────────────────────────────────────────────┘
                                │
        ┌───────────────────────┴───────────────────────┐
        │                                               │
        ▼                                               ▼
┌──────────────────────────┐              ┌──────────────────────────┐
│  PlanetListScreen.kt     │              │  PlanetDetailScreen.kt   │
│                          │              │                          │
│  @Composable             │              │  @Composable             │
│  fun PlanetListScreen(   │              │  fun PlanetDetailScreen( │
│      vm: PlanetList      │              │      planetId: String,   │
│        ViewModel =       │              │      vm: PlanetDetail    │
│        koinViewModel()   │◄─────┐       │        ViewModel =       │
│  ) { ... }               │      │       │        koinViewModel {   │
└──────────────────────────┘      │       │          parametersOf(   │
                                  │       │            planetId      │
        ▲                         │       │          )               │
        │                         │       │        }                 │◄─┐
        │ Injects                 │       │  ) { ... }               │  │
        │                         │       └──────────────────────────┘  │
        │                         │                                     │
┌───────┴──────────────────┐      │              ┌─────────────────────┴┐
│  koin-androidx-compose   │      │              │  koin-androidx-compose│
│  (from :common:core)     │      │              │  (from :common:core)  │
│                          │      │              │                       │
│  • koinViewModel()       │──────┘              │  • koinViewModel { }  │─┘
│  • get()                 │                     │  • parametersOf()     │
│  • getKoin()             │                     │  • get()              │
└──────────────────────────┘                     └───────────────────────┘
            │                                                │
            │                                                │
            └────────────────────┬───────────────────────────┘
                                 ▼
                ┌────────────────────────────────┐
                │  Koin Container                │
                │                                │
                │  Resolves:                     │
                │  • planetListModule            │
                │  • planetDetailModule          │
                │                                │
                │  With dependencies from:       │
                │  • domainModule                │
                │  • dataModule                  │
                │  • httpModule                  │
                └────────────────────────────────┘
```

## 5. Dependency Resolution Graph

```
Feature Layer (ViewModels)
══════════════════════════════════════════════════════════════
┌─────────────────────┐              ┌──────────────────────┐
│ PlanetListViewModel │              │ PlanetDetailViewModel│
│                     │              │                      │
│ Dependencies:       │              │ Dependencies:        │
│ • PlanetListUseCase │              │ • planetId: String   │
│   (from domain)     │              │ • PlanetRepository   │
└──────────┬──────────┘              │   (from data)        │
           │                         └──────────┬───────────┘
           │ get()                              │ get()
           ▼                                    │
Domain Layer (Use Cases)                        │
══════════════════════════════════════════      │
┌──────────────────┐                            │
│ PlanetListUseCase│                            │
│                  │                            │
│ Dependencies:    │                            │
│ • PlanetRepository│◄──────────────────────────┘
│   (from data)    │
└────────┬─────────┘
         │ get()
         ▼
Data Layer (Repository + DataSources + API)
══════════════════════════════════════════════════════════════
┌──────────────────────┐
│ PlanetRepositoryImpl │ implements PlanetRepository
│                      │
│ Dependencies:        │
│ • PlanetRemoteDS ────┼──┐
│ • PlanetLocalDS  ────┼──┼──┐
└──────────────────────┘  │  │
                          │  │
    ┌─────────────────────┘  │
    │                        │
    ▼                        ▼
┌──────────────────┐  ┌─────────────────────────┐
│ PlanetRemoteDS   │  │ InMemoryPlanetDataSource│
│                  │  │ implements PlanetLocalDS│
│ Dependencies:    │  └─────────────────────────┘
│ • PlanetApi ─────┼──┐
└──────────────────┘  │
                      │
    ┌─────────────────┘
    │
    ▼
┌────────────────┐
│ PlanetApiImpl  │ implements PlanetApi
│                │
│ Dependencies:  │
│ • HttpClient ──┼──┐
└────────────────┘  │
                    │
    ┌───────────────┘
    │
    ▼
┌──────────────┐
│  HttpClient  │ (Ktor)
│              │
│ Configured:  │
│ • JSON       │
│ • Logging    │
│ • Timeout    │
└──────────────┘
```

## 6. Convention Plugin Impact

```
WITHOUT Plugin (BEFORE)          WITH Plugin (AFTER)
═══════════════════════          ═══════════════════════

:common:core                     :common:core
├─ build.gradle.kts              ├─ build.gradle.kts
│  dependencies {                │  plugins {
│    api(koin-core)              │    local.android.library.koin ✓
│    api(koin-android)           │  }
│    api(koin-android-compat)    │  dependencies {
│    api(koin-androidx-compose)  │    api(koin-androidx-compose)
│    testImpl(koin-test)         │  }
│    testImpl(koin-test-junit4)  │
│  }                             │
└─ ❌ 6 Koin dependencies        └─ ✅ 1 Koin dependency + plugin

:common:data                     :common:data
├─ build.gradle.kts              ├─ build.gradle.kts
│  dependencies {                │  plugins {
│    api(koin-core)              │    local.android.library.koin ✓
│    api(koin-android)           │  }
│    api(koin-android-compat)    │  // Koin deps from plugin
│    testImpl(koin-test)         │
│    testImpl(koin-test-junit4)  │
│  }                             │
└─ ❌ 5 Koin dependencies        └─ ✅ 0 Koin dependencies

:common:domain                   :common:domain
├─ build.gradle.kts              ├─ build.gradle.kts
│  dependencies {                │  plugins {
│    api(koin-core)              │    local.android.library.koin ✓
│    api(koin-android)           │  }
│    api(koin-android-compat)    │  // Koin deps from plugin
│    testImpl(koin-test)         │
│    testImpl(koin-test-junit4)  │
│  }                             │
└─ ❌ 5 Koin dependencies        └─ ✅ 0 Koin dependencies

:common:ui                       :common:ui
├─ build.gradle.kts              ├─ build.gradle.kts
│  dependencies {                │  plugins {
│    // No Koin ✓                │    local.android.library
│  }                             │    local.android.library.compose
└─ ✅ Pure UI module             │  }
                                 │  // No Koin ✓
                                 └─ ✅ Pure UI module

TOTAL: 16 Koin dependencies      TOTAL: 1 Koin dependency + 3 plugins
       across 3 modules                 Reduction: ~93%
```

## 7. Testing Integration

```
Test Layer (Using Koin Testing)
═══════════════════════════════════════════════════════════════

Unit Tests (provided by plugin)
────────────────────────────────
• testImplementation(koin-test)
• testImplementation(koin-test-junit4)

┌────────────────────────────────────────┐
│  Feature Module Tests                 │
│  (e.g., PlanetListViewModelTest)      │
│                                        │
│  class PlanetListViewModelTest :      │
│      KoinTest {                        │◄─── koin-test
│                                        │
│      @Before                           │
│      fun setup() {                     │
│          startKoin {                   │◄─── Start test Koin
│              modules(testModule)       │
│          }                             │
│      }                                 │
│                                        │
│      @Test                             │
│      fun test() {                      │
│          val useCase: PlanetListUseCase│
│              = get()                   │◄─── Inject from Koin
│          // test logic                 │
│      }                                 │
│                                        │
│      @After                            │
│      fun tearDown() {                  │
│          stopKoin()                    │◄─── Clean up
│      }                                 │
│  }                                     │
└────────────────────────────────────────┘
```

## 8. Module Interaction Summary

```
┌─────────────────────────────────────────────────────────────────┐
│                        APPLICATION LAYER                        │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  :app Module                                             │  │
│  │  • NucleusApp.onCreate() → startKoin { modules(...) }  │  │
│  │  • Aggregates all modules                               │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                             │
            ┌────────────────┼────────────────┐
            ▼                ▼                ▼
┌──────────────────┐ ┌──────────────┐ ┌──────────────────┐
│  FEATURE LAYER   │ │ DOMAIN LAYER │ │   DATA LAYER     │
│                  │ │              │ │                  │
│  ViewModels:     │ │  Use Cases:  │ │  Repositories:   │
│  • Planet        │ │  • Planet    │ │  • Planet        │
│    ListVM        │ │    ListUC    │ │    Repository    │
│  • Planet        │ │              │ │  DataSources:    │
│    DetailVM      │ │  Modules:    │ │  • Remote        │
│                  │ │  • domain    │ │  • Local         │
│  Modules:        │ │    Module    │ │  APIs:           │
│  • planetList    │ │              │ │  • PlanetApi     │
│    Module        │ │  Plugin:     │ │  HTTP:           │
│  • planetDetail  │ │  ✓ local.    │ │  • HttpClient    │
│    Module        │ │    android.  │ │                  │
│                  │ │    library.  │ │  Modules:        │
│  Plugin via      │ │    koin      │ │  • dataModule    │
│  feature plugin: │ │              │ │  • httpModule    │
│  ✓ Inherits      │ │  Provides:   │ │                  │
│    Koin from     │ │  • Koin DI   │ │  Plugin:         │
│    :common:core  │ │              │ │  ✓ local.        │
└──────────────────┘ └──────────────┘ │    android.      │
                                      │    library.      │
                                      │    koin          │
                                      │                  │
                                      │  Provides:       │
                                      │  • Koin DI       │
                                      └──────────────────┘

Legend:
═══════
┌─┐  Module boundary
│ │  Contains code
└─┘

✓    Plugin applied
→    Data flow
◄──  Dependency injection
```

## Key Takeaways

1. **Plugin Level**: `AndroidLibraryKoinConventionPlugin` centralizes Koin dependency management
2. **Module Level**: Three layers use Koin (data, domain, feature), two don't (ui, navigation)
3. **Initialization**: Single `startKoin` call in `NucleusApp` aggregates all modules
4. **Composition**: Modules compose via `includes()` and resolve via `get()`
5. **Testing**: Plugin automatically includes Koin test dependencies
6. **Transitive**: Feature modules inherit Koin from `:common:core` without explicit dependencies
7. **Selective**: UI and navigation modules remain Koin-free for clarity and minimal dependencies
