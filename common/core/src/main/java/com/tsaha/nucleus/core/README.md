# Core Module (:common:core)

This module provides common dependencies and utilities for ViewModel, Koin (DI), and Ktor (
networking) throughout the application.

## Dependencies Included

### ViewModel & Lifecycle

- `androidx-lifecycle-runtime-ktx`
- `androidx-lifecycle-viewmodel-ktx`
- `androidx-lifecycle-viewmodel-compose`
- `androidx-lifecycle-runtime-compose`
- `androidx-lifecycle-viewmodel-savedstate`
- `androidx-lifecycle-livedata-ktx`
- `androidx-lifecycle-common-java8`

### Koin (Dependency Injection)

- `koin-core`
- `koin-android`
- `koin-android-compat`
- `koin-androidx-navigation`
- `koin-androidx-compose`

### Ktor (Networking)

- `ktor-client-core`
- `ktor-client-android`
- `ktor-client-okhttp`
- `ktor-client-content-negotiation`
- `ktor-client-logging`
- `ktor-serialization-kotlinx-json`

### Coroutines & Serialization

- `kotlinx-coroutines-core`
- `kotlinx-coroutines-android`
- `kotlinx-serialization`

## Usage

### Using the Core Module in Other Modules

Add this to your module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":common:core"))
}
```

### Using BaseViewModel

```kotlin
class MyViewModel : BaseViewModel() {
    private val _uiState = MutableStateFlow(MyUiState())
    val uiState = _uiState.asStateFlow()
    
    fun performOperation() {
        safeViewModelScope.launch {
            // Your coroutine code here
            // Errors will be handled by BaseViewModel
        }
    }
}
```

### Using Core DI Module

```kotlin
// In your Application class
startKoin {
    androidContext(this)
    modules(coreModule, /* other modules */)
}
```

The `coreModule` automatically provides:

- **`HttpClient`**: Pre-configured with OkHttp, content negotiation, and auto-logging

### Dependency Injection Usage

```kotlin
// Option 1: Constructor injection (recommended)
class ApiRepository(
    private val httpClient: HttpClient
) {
    suspend fun fetchData(): ApiResponse {
        return httpClient.get("https://api.example.com/data")
    }
}

// Option 2: Field injection in ViewModel
class MyViewModel : BaseViewModel() {
    private val httpClient: HttpClient by inject()
    
    fun loadData() {
        safeViewModelScope.launch {
            val data = httpClient.get("https://api.example.com/data")
        }
    }
}

// Option 3: Direct creation (when DI not available)
class MyRepository {
    private val httpClient = NetworkClient.create()
    
    suspend fun fetchData(): ApiResponse {
        return httpClient.get("https://api.example.com/data")
    }
}
```

### Logging Levels

- **Debug builds**: `LogLevel.BODY` - Full request/response logging
- **Release builds**: `LogLevel.NONE` - No logging for performance and security

## Testing

The module includes testing dependencies:

- `androidx-lifecycle-viewmodel-testing`
- `koin-test`
- `koin-test-junit4`