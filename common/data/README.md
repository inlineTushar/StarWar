# Data Module - SWAPI Planets API

This module provides a complete implementation for fetching planets from
the [SWAPI (Star Wars API)](https://swapi.tech) with Ktor HTTP client and Koin dependency injection.

## Features

- **Planets API**: Fetch paginated planets from SWAPI
- **Ktor Integration**: HTTP client with proper error handling
- **Koin DI**: Parameterized dependency injection with interface binding
- **Kotlin Result**: Type-safe error handling
- **Pagination Support**: Check for next pages
- **Clean Architecture**: Interface-based design for better testability

## API Endpoint

```
GET https://swapi.tech/api/planets?page=1&limit=10
```

## Architecture

The module follows **Interface-Implementation pattern** for better separation of concerns:

```
Interface           Implementation
---------           --------------
PlanetApi    ←→     PlanetApiImpl
PlanetRepository ←→ PlanetRepositoryImpl
```

## Components

### 1. Data Models

#### `Planet`

```kotlin
@Serializable
data class Planet(
    val uid: String,
    val name: String,
    val url: String
)
```

#### `PaginationInfo`

```kotlin
data class PaginationInfo(
    val hasNext: Boolean
)
```

### 2. API Layer

#### Interface

```kotlin
interface PlanetApi {
    suspend fun getPlanets(pageNumber: Int, limit: Int): Result<Pair<PaginationInfo, List<Planet>>>
}
```

#### Implementation

```kotlin
class PlanetApiImpl(private val httpClient: HttpClient) : PlanetApi {
    // Ktor HTTP implementation
}
```

### 3. Repository Layer

#### Interface

```kotlin
interface PlanetRepository {
    suspend fun getPlanets(pageNumber: Int = 1, limit: Int = 10): Result<Pair<PaginationInfo, List<Planet>>>
    suspend fun getFirstPage(limit: Int = 10): Result<Pair<PaginationInfo, List<Planet>>>
}
```

#### Implementation

```kotlin
class PlanetRepositoryImpl(private val planetApi: PlanetApi) : PlanetRepository {
    // Business logic and validation
}
```

## Dependency Injection

Koin binds implementations to interfaces automatically:

```kotlin
val dataModule = module {
    includes(httpModule)
    
    // API: PlanetApiImpl bound to PlanetApi interface
    singleOf(::PlanetApiImpl) { bind<PlanetApi>() }
    
    // Repository: PlanetRepositoryImpl bound to PlanetRepository interface
    singleOf(::PlanetRepositoryImpl) { bind<PlanetRepository>() }
}
```

## Usage

### 1. Setup Koin DI

Include the `dataModule` in your Koin configuration:

```kotlin
startKoin {
    modules(dataModule)
}
```

### 2. Inject Repository Interface

```kotlin
class YourClass : KoinComponent {
    // Inject interface - implementation resolved automatically
    private val planetRepository: PlanetRepository by inject()
}
```

### 3. Fetch Planets

```kotlin
// Fetch first page
val result = planetRepository.getFirstPage()
result.onSuccess { (paginationInfo, planets) ->
    println("Loaded ${planets.size} planets")
    println("Has more pages: ${paginationInfo.hasNext}")
    
    planets.forEach { planet ->
        println("${planet.name} - ${planet.uid}")
    }
}

// Fetch specific page
val pageResult = planetRepository.getPlanets(pageNumber = 2, limit = 5)
```

## Benefits of Interface Pattern

✅ **Testability**: Easy to mock interfaces for unit tests  
✅ **Flexibility**: Swap implementations without changing client code  
✅ **Dependency Inversion**: Depend on abstractions, not concrete classes  
✅ **Clean Architecture**: Clear separation between contracts and implementations

## Error Handling

The API returns `Result<T>` for type-safe error handling:

```kotlin
planetRepository.getPlanets(1, 10)
    .onSuccess { (pagination, planets) -> 
        // Handle success
    }
    .onFailure { exception -> 
        // Handle error
        when (exception) {
            is IllegalArgumentException -> // Parameter validation error
            else -> // Network or parsing error
        }
    }
```

## Dependencies

This module uses:
- **Ktor Client**: For HTTP requests
- **Kotlinx Serialization**: For JSON parsing
- **Koin**: For dependency injection with interface binding
- **Kotlin Coroutines**: For async operations

## Example Response

The API returns planets in this format:
```json
{
  "message": "ok",
  "next": "https://swapi.tech/api/planets?page=2&limit=10",
  "results": [
    {
      "uid": "1",
      "name": "Tatooine", 
      "url": "https://www.swapi.tech/api/planets/1"
    }
  ]
}
```

## Testing

See `DataModuleTest.kt` for examples of how to test the interface-based DI implementation.