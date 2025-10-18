# Koin Quick Reference

## At a Glance

```
Plugin → Module → DI Code → UI Usage
──────────────────────────────────────
AndroidLibraryKoinConventionPlugin
    ↓
:common:core + :common:data + :common:domain
    ↓
Koin modules (coreModule, dataModule, domainModule, etc.)
    ↓
startKoin() in NucleusApp
    ↓
koinViewModel() in Compose screens
```

## Where Koin Lives

| Module | Has Plugin? | Has DI Module? | Purpose |
|--------|-------------|----------------|---------|
| `:common:core` | ✅ Yes | ✅ coreModule (empty) | Koin base + Compose |
| `:common:data` | ✅ Yes | ✅ dataModule, httpModule | Repositories, APIs |
| `:common:domain` | ✅ Yes | ✅ domainModule | Use cases |
| `:common:ui` | ❌ No | ❌ No | Pure UI components |
| `:common:navigation` | ❌ No | ❌ No | Navigation types |
| `:feature:planetlist` | ❌ No* | ✅ planetListModule | ViewModels |
| `:feature:planetdetail` | ❌ No* | ✅ planetDetailModule | ViewModels |
| `:app` | ❌ No* | ❌ No | App initialization |

*Feature modules inherit Koin transitively from `:common:core`

## Quick Commands

### Apply Plugin to New Module

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.local.android.library)
    alias(libs.plugins.local.android.library.koin)  // ← Add this
}
```

### Create a Koin Module

```kotlin
// di/MyModule.kt
import org.koin.dsl.module

val myModule = module {
    // Singleton
    singleOf(::MyRepositoryImpl) { bind<MyRepository>() }
    
    // ViewModel
    viewModel { MyViewModel(get()) }
    
    // Parameterized ViewModel
    viewModel { (id: String) -> MyDetailViewModel(id, get()) }
}
```

### Register Module in App

```kotlin
// NucleusApp.kt
startKoin {
    androidContext(this@NucleusApp)
    modules(
        coreModule,
        dataModule,
        domainModule,
        myModule  // ← Add here
    )
}
```

### Inject in Compose

```kotlin
// Simple injection
@Composable
fun MyScreen(
    vm: MyViewModel = koinViewModel()
) { ... }

// Parameterized injection
@Composable
fun MyDetailScreen(
    id: String,
    vm: MyDetailViewModel = koinViewModel { parametersOf(id) }
) { ... }
```

### Testing with Koin

```kotlin
class MyTest : KoinTest {
    @Before
    fun setup() {
        startKoin {
            modules(
                module {
                    single<MyRepository> { mockk() }
                }
            )
        }
    }
    
    @Test
    fun test() {
        val repo: MyRepository = get()
        // test logic
    }
    
    @After
    fun tearDown() {
        stopKoin()
    }
}
```

## Koin DSL Cheat Sheet

| DSL | Purpose | Scope | Example |
|-----|---------|-------|---------|
| `single { }` | Singleton | App-wide | `single<HttpClient> { ... }` |
| `singleOf()` | Singleton w/ constructor | App-wide | `singleOf(::RepoImpl)` |
| `factory { }` | New instance each time | Per request | `factory { MyClass() }` |
| `viewModel { }` | ViewModel | Per screen | `viewModel { MyVM(get()) }` |
| `get()` | Resolve dependency | In module | `MyClass(get())` |
| `bind<T>()` | Interface binding | With definition | `{ bind<Interface>() }` |
| `includes()` | Compose modules | In module | `includes(httpModule)` |
| `parametersOf()` | Runtime params | At injection | `parametersOf(id)` |

## Dependency Flow Example

```
User opens PlanetDetailScreen with planetId = "123"
    ↓
koinViewModel { parametersOf("123") }
    ↓
Koin looks up planetDetailModule
    ↓
viewModel { (planetId: String) -> 
    PlanetDetailViewModel(planetId, get())
}
    ↓
Resolves get() → PlanetRepository from dataModule
    ↓
Injects: PlanetDetailViewModel("123", planetRepositoryImpl)
    ↓
ViewModel ready for use in Compose
```

## File Locations

```
StarWar/
├── app/src/main/java/com/tsaha/nucleus/
│   └── NucleusApp.kt                           ← startKoin()
│
├── common/core/src/main/java/.../di/
│   └── CoreModule.kt                           ← coreModule
│
├── common/data/src/main/java/.../di/
│   ├── HttpModule.kt                           ← httpModule
│   └── DataModule.kt                           ← dataModule
│
├── common/domain/src/main/java/.../di/
│   └── DomainModule.kt                         ← domainModule
│
├── feature/planetlist/src/main/java/.../di/
│   └── PlanetListModule.kt                     ← planetListModule
│
├── feature/planetdetail/src/main/java/.../di/
│   └── PlanetDetailModule.kt                   ← planetDetailModule
│
└── build-logic/convention/.../plugin/
    └── AndroidLibraryKoinConventionPlugin.kt   ← Plugin
```

## Common Patterns

### Pattern 1: Repository with Interface Binding

```kotlin
val dataModule = module {
    singleOf(::PlanetRepositoryImpl) { bind<PlanetRepository>() }
}
```

### Pattern 2: ViewModel with Use Case Dependency

```kotlin
val featureModule = module {
    viewModel { MyViewModel(planetListUseCase = get()) }
}
```

### Pattern 3: Module Composition

```kotlin
val dataModule = module {
    includes(httpModule)  // Compose smaller modules
    // ... other definitions
}
```

### Pattern 4: Parameterized ViewModel

```kotlin
val detailModule = module {
    viewModel { (id: String) ->
        DetailViewModel(id = id, repo = get())
    }
}
```

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `NoBeanDefFoundException` | Ensure module is registered in `startKoin { modules(...) }` |
| ViewModel not injecting | Check feature module depends on `:common:core` |
| Test failing with Koin error | Add `stopKoin()` in `@After` method |
| Circular dependency | Refactor or use `lazy { get() }` |
| Wrong instance injected | Check `single` vs `factory` scope |

## Migration Checklist

When adding Koin to a new module:

- [ ] Add `alias(libs.plugins.local.android.library.koin)` to plugins
- [ ] Create DI module file in `src/main/java/.../di/`
- [ ] Define module with `val myModule = module { ... }`
- [ ] Register module in `NucleusApp.startKoin { modules(...) }`
- [ ] Sync Gradle
- [ ] Use `koinViewModel()` or `get()` to inject dependencies

## Best Practices

1. ✅ **One module file per layer** (e.g., `DataModule.kt`, `DomainModule.kt`)
2. ✅ **Use `singleOf()` for simple constructors** - cleaner than `single { }`
3. ✅ **Always bind interfaces** - `{ bind<Interface>() }`
4. ✅ **Module composition** - Use `includes()` for related modules
5. ✅ **Parameterized ViewModels** - For detail screens with IDs
6. ✅ **Stop Koin in tests** - Prevent state leakage between tests
7. ❌ **Don't use Koin in pure modules** - Keep UI/navigation Koin-free
8. ❌ **Don't nest modules** - Keep flat structure in `startKoin`
