# 🪐 Star Wars Planets Explorer

A modern Android application showcasing **Clean Architecture**, **Modular Design**, and *
*Squad-Based Development** practices. This app displays Star Wars planets with detailed information,
featuring a sophisticated two-phase data loading strategy with pagination, real-time search
functionality, and comprehensive testing coverage.

## 📸 Preview

<table align="center">
<tr>
<td align="center">
<h3>Planet List Screen</h3>
<img src="./screenshot/Screen%231.png" width="300" alt="Planet List Screen"/>
<br>
<em>Two-phase loading with shimmer effects and search</em>
</td>
<td align="center">
<h3>Planet Detail Screen</h3>
<img src="./screenshot/Screen%232.png" width="300" alt="Planet Detail Screen"/>
<br>
<em>Gradient styling and comprehensive planet info</em>
</td>
</tr>
</table>

<p align="center"><strong>Modern Material 3 design with progressive loading, search, and accessibility support</strong></p>

> 📂 **Screenshots Location**: [`screenshot/`](./screenshot/) directory  
> 🖼️ **Files**: `Screen#1.png` (Planet List) • `Screen#2.png` (Planet Details)  
> 💡 **Tip**: If images don't load, check your markdown viewer's support for relative paths

## 📱 Features

- **Planet List**: Browse all Star Wars planets with pagination and progressive detail enhancement
- **Real-Time Search**: Search planets by name with debounced input (300ms) for optimal performance
- **Pagination**: Infinite scroll with automatic page loading (10 items per page)
- **Planet Details**: View comprehensive planet information with modern gradient styling
- **Responsive UI**: Adaptive Material 3 design with shimmer loading states
- **Offline-Ready**: Robust error handling and state management
- **Accessibility**: Full accessibility support with semantic labels

## 🏗️ Modular Architecture

The project follows a **multi-module architecture** designed for **squad-based development**,
enabling teams to work independently on different features while maintaining clear boundaries and
dependencies.

### 📊 Module Dependency Graph

```
                    🚀 app
                   /         \
                  /           \
         🌟 feature:         🌍 feature:
           planetlist        planetdetail
                |                 |
                 \               /
                  \             /
                   \           /
              📱 common:ui -------- 🧭 common:navigation
                    |                      |
                    |                      |
              💼 common:domain ------------|
                    |                      |
                    |                      |
              💾 common:data -------- ⚙️ common:core
                    |                      |
                     \                    /
                      \                  /
                       \________________/
```

### 🎯 Module Responsibilities

#### **App Module** (`app`)
- **Role**: Application entry point and dependency assembly
- **Squad**: Platform/DevOps Squad
- **Contains**: Main Activity, Navigation setup, DI configuration

#### **Feature Modules** (`feature:*`)

- **`feature:planetlist`**: Planet browsing, list management, search, and pagination
- **`feature:planetdetail`**: Individual planet detail screens
- **Squad Ownership**: Feature squads can own individual feature modules
- **Benefits**: Independent development, deployment, and testing

#### **Common Modules** (`common:*`)

- **`common:core`**: Shared utilities, base classes, network configuration, constants
- **`common:domain`**: Domain layer with use cases, domain models, and business logic
- **`common:data`**: Data layer, repositories, API models, data sources
- **`common:ui`**: Reusable UI components, themes, design system
- **`common:navigation`**: Navigation routes and shared navigation logic
- **Squad Ownership**: Platform/Infrastructure Squad

### 🏢 Squad-Based Development Benefits

1. **🔄 Parallel Development**: Multiple squads can work on different features simultaneously
2. **🚀 Independent Deployment**: Feature modules can be updated independently
3. **🧪 Isolated Testing**: Each module has its own test suite
4. **📦 Clear Ownership**: Well-defined boundaries between team responsibilities
5. **🔧 Technology Independence**: Different features can adopt new technologies at their own pace

## 🏛️ Clean Architecture

The project implements **Clean Architecture principles** with clear separation of concerns:

### 📋 Architecture Layers

```
┌─────────────────────────────────────────────────┐
│                 🎨 Presentation                 │
│         (Screens, ViewModels, UI States)        │
├─────────────────────────────────────────────────┤
│                 💼 Domain                       │
│         (UseCases, Domain Models, Logic)        │
├─────────────────────────────────────────────────┤
│                 💾 Data                         │
│         (Repositories, DataSources, API)        │
└─────────────────────────────────────────────────┘
```

#### **Presentation Layer** 📱
- **Composables**: Modern Jetpack Compose UI
- **ViewModels**: State management with coroutines and StateFlow
- **UI State**: Immutable UI-specific state classes with sealed hierarchies
- **Mappers**: Convert domain models to UI states

#### **Domain Layer** 💼
- **Use Cases**: Business logic encapsulation (`PlanetListUseCase`)
- **Domain Models**: Pure domain entities (`PlanetWithDetails`, `PlanetListResult`,
  `PlanetDetailsState`)
- **Business Logic**: Two-phase loading, pagination, search logic
- **Interfaces**: Repository contracts
- **No Android Dependencies**: Pure Kotlin logic

#### **Data Layer** 💾
- **Repositories**: Data access abstraction (`PlanetRepository`)
- **Data Sources**: Remote (API) and local (in-memory) data management
- **Network**: Ktor HTTP client with serialization
- **Models**: Data transfer objects (DTOs) and entity models

### 🔄 Data Flow Architecture

```
🎨 UI (Compose) 
    ↓ observes StateFlow
📱 ViewModel (UI State)
    ↓ calls
💼 Use Case (Domain Logic)
    ↓ uses
💾 Repository (Data Access)
    ↓ fetches from
🌐 Remote/Local Data Sources
```

## 🛠️ Core Technologies

### **Android Stack**
- 🎯 **Kotlin 2.2.10** - Modern programming language
- 📱 **Jetpack Compose** (BOM 2025.08.01) - Declarative UI
- 🏗️ **Material 3** - Google's latest design system
- 🧭 **Navigation Compose 2.9.3** - Type-safe navigation

### **Architecture Components**

- 🔄 **Lifecycle 2.9.3** - ViewModel, StateFlow, lifecycle-aware components
- 💉 **Koin 4.1.0** - Dependency injection framework
- 🌊 **Coroutines 1.10.2** - Asynchronous programming

### **Network & Data**
- 🌐 **Ktor 3.2.3** - HTTP client with content negotiation
- 📦 **Kotlinx Serialization 1.9.0** - JSON parsing
- 🔀 **StateFlow** - Reactive state management

### **Testing Framework**
- 🧪 **JUnit 4.13.2** - Unit testing framework
- 🤖 **AndroidX Test** - Android instrumentation testing
- 🎭 **MockK 1.14.5** - Mocking framework for Kotlin
- ✅ **AssertK 0.28.1** - Fluent assertions for Kotlin
- 💨 **Turbine 1.2.1** - Flow testing utilities

## 📊 Data Loading Strategy

The application implements a **sophisticated two-phase loading strategy with pagination** optimized
for user experience:

### 🔄 Phase 1: Initial Load (Fast)
```
API Call → Basic Planet List (Page 1) → Immediate UI Display
```

- Fetches basic planet information (names, UIDs) for the first page
- Displays planets immediately with loading shimmer
- Provides instant visual feedback to users
- Default page size: 10 items

### 📝 Phase 2: Detail Enhancement (Progressive)
```
Concurrent API Calls → Planet Details → Progressive UI Updates
```

- Loads detailed planet information concurrently for each page
- Updates UI progressively as each planet's details arrive
- Uses default concurrency from Kotlin coroutines (16 concurrent requests)
- Handles errors gracefully without blocking other planets

### 📄 Phase 3: Pagination (Infinite Scroll)

```
Scroll to Bottom → Load Next Page → Append to List → Enhance Details
```

- Automatically loads next page when user scrolls near the end
- Seamless infinite scroll experience
- Each page follows the two-phase loading strategy
- Tracks pagination state to prevent duplicate requests

### 🔍 Search Functionality

```
User Input → Debounce (300ms) → API Search → Display Results
```

- Real-time search with 300ms debounce for performance
- Searches planets by name
- Returns full planet details immediately (no two-phase for search)
- Clears search to return to paginated list

### 💡 Implementation Details

```kotlin
// PlanetListUseCase - Two-phase loading with pagination
fun observePlanets(
    loadNextFlow: Flow<Int>,
    pageSize: Int = PAGE_SIZE
): Flow<PlanetListResult> = flow {
    emit(PlanetListResult.Loading)                         // Phase 0: Loading state
    
    loadNextFlow
        .buffer(capacity = 0, onBufferOverflow = BufferOverflow.DROP_LATEST)
        .flatMapMerge(concurrency = 1) { pageNumber ->
            flow {
                // Phase 1: Fetch basic planet info for page
                val planetsResult = planetRepository.getPlanetsWithPagination(pageNumber)
                hasNext = planetsResult.getOrNull()?.first?.hasNext ?: true
                
                // Phase 2: Progressively enhance with details (16 concurrent requests)
                planets.asFlow()
                    .flatMapMerge { planet ->
                        planetRepository.getPlanet(planet.uid)
                    }
                    .collect { planetDetails ->
                        updateStateWithDetails(planetDetails)
                    }
            }
        }
}.flowOn(Dispatchers.IO)

// Search functionality with debounce
val uiState: StateFlow<PlanetListUiState> =
    searchQuery
        .debounce(300)  // Debounce search input
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) {
                planetListUseCase.observePlanets(nextPageCounterFlow)
            } else {
                planetListUseCase.searchPlanets(query)
            }
        }
```

### 🎯 Benefits

- **⚡ Fast Initial Load**: Users see content within milliseconds
- **📈 Progressive Enhancement**: Details appear as they're loaded
- **🚦 Optimized Network**: Concurrent API requests with Kotlin coroutines default (16)
- **💪 Resilient**: Individual planet failures don't affect others
- **♾️ Infinite Scroll**: Seamless pagination without manual load-more buttons
- **🔍 Smart Search**: Debounced input prevents unnecessary API calls

## 🧪 Testing Coverage

### 📊 Test Statistics

- **📁 Total Test Files**: 11 files
- **🧪 Unit Tests**: 4 files (~60+ test methods)
- **🎭 UI Tests**: 7 files (~65+ test methods)
- **📈 Total Test Methods**: ~125+ test methods
- **✅ Current Status**: All tests passing

### 🏗️ Testing Architecture

#### **Unit Tests** 🧪

```
📁 Unit Test Coverage:
├── 🧠 ViewModels (15+ tests)
│   ├── PlanetDetailViewModelTest.kt
│   └── PlanetListViewModelTest.kt (with search & pagination tests)
├── 💼 Use Cases (20+ tests)
│   └── PlanetListUseCaseTest.kt (pagination, search, error handling)
├── 💾 Data Sources (25+ tests)
│   └── InMemoryPlanetDataSourceTest.kt
└── 📊 Business Logic Testing
```

**Key Test Coverage:**
- ✅ **State Management**: ViewModel state transitions
- ✅ **Business Logic**: Use case implementations with pagination
- ✅ **Search Logic**: Debounced search and query handling
- ✅ **Pagination**: Multiple page loading and state tracking
- ✅ **Data Operations**: Repository and data source logic
- ✅ **Error Handling**: Network failures and edge cases
- ✅ **Coroutines**: Async operations and flow testing

#### **UI Tests** 🎭

```
📁 UI Test Coverage:
├── 🧩 Component Tests (~50 tests)
│   ├── PlanetComposableTest.kt (11 tests)
│   ├── NucleusAppBarTest.kt (9 tests)
│   ├── ProgressBarComposableTest.kt (9 tests)
│   ├── ShimmerComposableTest.kt (7 tests)
│   └── ErrorComposableTest.kt (6 tests)
└── 📱 Integration Tests (~15 tests)
    ├── PlanetListScreenTest.kt (search, pagination, list rendering)
    └── PlanetDetailsScreenTest.kt (detail display, navigation)
```

**Key Test Coverage:**
- ✅ **UI Components**: Individual composable behavior
- ✅ **Screen Integration**: Full screen with ViewModel integration
- ✅ **User Interactions**: Clicks, navigation, search input, accessibility
- ✅ **State Rendering**: Loading, success, error, search states
- ✅ **Visual Validation**: Text display, styling, layout
- ✅ **Search UI**: Search bar, query updates, clear functionality

### 🚀 Running Tests

```bash
# Run all tests
./gradlew test connectedAndroidTest

# Unit tests only
./gradlew test

# UI tests only
./gradlew connectedAndroidTest

# Specific module tests
./gradlew :feature:planetlist:test                    # Unit tests
./gradlew :feature:planetlist:connectedAndroidTest   # UI tests
./gradlew :common:domain:test                         # Domain layer tests

# Test with coverage
./gradlew testDebugUnitTest jacocoTestReport
```

## 🏃‍♂️ Quick Start

### Prerequisites

- Android Studio Hedgehog or newer
- JDK 17+
- Android SDK 34+

### Setup

```bash
# Clone the repository
git clone <repository-url>
cd StarWar

# Run the app
./gradlew installDebug

# Run all tests
./gradlew test connectedAndroidTest
```

## 🏗️ Build Configuration

### 📦 **Build Variants**

The project supports two build variants with different configurations:

| Variant     | Application ID                       | Signing          | Use Case              |
|-------------|--------------------------------------|------------------|-----------------------|
| **Debug**   | `com.tsaha.nucleus.starwars.debug`   | Debug keystore   | Development, testing  |
| **Release** | `com.tsaha.nucleus.starwars.release` | Release keystore | Production deployment |

### 🔐 **Keystore Management**

#### **Debug Build** 🛠️

- **Keystore**: `keystore/debug.keystore.jks` (included in repository)
- **Credentials**: Hardcoded for development convenience
- **Usage**: No additional setup required

```bash
# Build debug APK
./gradlew assembleDebug

# Install debug version
./gradlew installDebug
```

#### **Release Build** 🚀

- **Keystore**: `keystore/release.keystore.jks` (included in repository)
- **Credentials**: **Not included** for security reasons
- **Setup Required**: Contact developer for release signing credentials

##### **Release Build Setup**

1. **Contact Developer** 📞
    - Request release signing credentials from the project maintainer
    - You'll receive: `keyAlias`, `keyPassword`, and `storePassword`

2. **Configure Local Credentials** 🔑
   Create or update `~/.gradle/gradle.properties`:
   ```properties
   # Release signing configuration
   STAR_RELEASE_KEY_ALIAS=your_key_alias
   STAR_RELEASE_KEY_PASSWORD=your_key_password  
   STAR_RELEASE_STORE_PASSWORD=your_store_password
   ```

3. **Build Release APK** 📦
   ```bash
   # Build release APK
   ./gradlew assembleRelease
   
   # Install release version
   ./gradlew installRelease
   ```

##### **Security Notes** 🔒

- ✅ **Keystore files**: Included in repository (encrypted)
- ❌ **Passwords**: Never committed to version control
- 🏠 **Credentials**: Stored in user's local `gradle.properties`
- 🔐 **Production**: Release builds require valid signing credentials

##### **Troubleshooting** 🛠️

```bash
# If release build fails with credential errors:
# 1. Verify gradle.properties exists: ~/.gradle/gradle.properties
# 2. Check property names match exactly:
#    - STAR_RELEASE_KEY_ALIAS
#    - STAR_RELEASE_KEY_PASSWORD  
#    - STAR_RELEASE_STORE_PASSWORD
# 3. Contact developer for correct values

# Build with debug signing for testing
./gradlew assembleDebug  # Always works
```

## 📦 Project Structure

```
StarWar/
├── 🚀 app/                           # Application module
│   └── src/main/java/                # App entry point & DI setup
├── 🌟 feature/                       # Feature modules
│   ├── planetlist/                   # Planet list feature
│   │   ├── src/main/                 # Screen, ViewModel, Mappers
│   │   ├── src/test/                 # Unit tests (search, pagination)
│   │   └── src/androidTest/          # UI tests
│   └── planetdetail/                 # Planet detail feature
│       ├── src/main/                 # Screen, ViewModel
│       ├── src/test/                 # Unit tests
│       └── src/androidTest/          # UI tests
├── 🔧 common/                        # Shared modules
│   ├── core/                         # Base classes, utilities, constants
│   ├── domain/                       # Domain layer (NEW!)
│   │   ├── src/main/                 # Use cases, domain models
│   │   └── src/test/                 # Domain logic tests
│   ├── data/                         # Repository & API layer
│   │   └── src/test/                 # Data layer tests
│   ├── ui/                           # Reusable UI components  
│   │   └── src/androidTest/          # UI component tests
│   └── navigation/                   # Navigation logic
└── 🛠️ build-logic/                   # Custom Gradle plugins
    └── convention/                   # Convention plugins
```

## 🎨 UI Components

### **Design System**

- **🎨 Material 3**: Modern Google design language
- **🔤 Typography**: Monospace fonts with gradient styling
- **🌈 Theming**: Dynamic styling with random gradient colors
- **♿ Accessibility**: Full semantic labeling and navigation support
- **🔍 Search UI**: Modern search bar with clear functionality

### **Key Components**

- **`SearchBar`**: Real-time search with debounced input and clear button
- **`PlanetComposable`**: Main planet list item with click handling
- **`PlanetNameComposable`**: Stylable planet names with gradient effects
- **`NucleusAppBar`**: Consistent app bar with back navigation
- **`ShimmerComposable`**: Loading animation for better UX
- **`ErrorComposable`**: Unified error state handling
- **`LoadMoreComposable`**: Pagination loading indicator

## 🔄 State Management

### **UI State Pattern**

```kotlin
// Domain-level state (in common:domain)
sealed class PlanetListResult {
    data object Loading : PlanetListResult
    data class Error(val message: String?) : PlanetListResult
    data class Success(
        val items: List<PlanetWithDetails>,
        val isLoadingNextPage: Boolean = false
    ) : PlanetListResult
    data class SearchResult(
        val items: List<PlanetWithDetails>,
        val query: String,
        val isSearching: Boolean = false
    ) : PlanetListResult
}

// UI-level state (in feature modules)
sealed class PlanetListUiState {
    data object ListLoading : PlanetListUiState
    data class ListError(val errorMessage: String? = null) : PlanetListUiState
    data class ListSuccess(
        val planetItems: List<PlanetItem>,
        val isPageLoading: Boolean = false
    ) : PlanetListUiState
    data class SearchResult(
        val planetItems: List<PlanetItem>,
        val searchQuery: String,
        val isSearching: Boolean = false
    ) : PlanetListUiState
}
```

### **Data Flow**

```
🔄 User Action → ViewModel → UseCase → Repository → DataSource → API
                     ↓           ↓
📱 UI State ←─── Mapper ←─── Domain Model ←─── Data Layer ←─── Network
```

### **Domain vs. Presentation Separation**

- **Domain Layer**: Pure business logic with no Android dependencies
- **Mappers**: Convert domain models to UI-specific states in ViewModel
- **Benefits**: Testable business logic, reusable domain models, clear boundaries

## 🏗️ Build System

### **Custom Gradle Plugins**

The project uses custom convention plugins for consistent configuration:

- **`local.android.application`**: App module configuration
- **`local.android.library`**: Common library setup
- **`local.android.library.compose`**: Compose-enabled libraries
- **`local.android.library.koin`**: Koin DI dependencies (NEW!)
- **`local.android.feature`**: Feature module conventions (includes all common dependencies)

### **Koin DI Convention Plugin** 🆕

Centralized dependency injection setup:

- Automatically adds Koin dependencies to modules
- Ensures consistent DI configuration across the project
- Applied to `common:data`, `common:domain`, and feature modules

### **Version Catalog**

Centralized dependency management in `gradle/libs.versions.toml`:

- 📚 **Single source of truth** for all dependency versions
- 🔄 **Easy updates** across all modules
- 🎯 **Type-safe** dependency references

## 🚀 Key Architectural Benefits

### **For Squads**

- 🔄 **Independent Development**: Teams can work on separate features
- 🚢 **Autonomous Deployment**: Feature modules can be updated independently
- 📏 **Clear Boundaries**: Well-defined interfaces between modules
- 🎯 **Focused Testing**: Each squad maintains their own test suites

### **For Developers**

- 🧹 **Clean Code**: SOLID principles and clear separation of concerns
- 🏗️ **Domain-Driven Design**: Separate domain layer with pure business logic
- 🔍 **Testability**: High test coverage with isolated unit and UI tests
- 🛡️ **Type Safety**: Compile-time guarantees with sealed classes
- 📱 **Modern UI**: Declarative Compose with Material 3
- 💉 **Centralized DI**: Consistent dependency injection with Koin

### **For Users**

- ⚡ **Fast Loading**: Two-phase loading for immediate visual feedback
- 🔍 **Powerful Search**: Real-time search with instant results
- ♾️ **Seamless Pagination**: Infinite scroll without manual actions
- 🎨 **Beautiful UI**: Modern design with gradient effects and animations
- ♿ **Accessible**: Full accessibility support for all users
- 🔄 **Reliable**: Robust error handling and offline capabilities

## 🧪 Testing Philosophy

### **Testing Pyramid Implementation**

```
        🔺 UI Tests (~65 tests)
       /                        \
      /   Integration Tests      \
     /____________________________\
    /                              \
   /   Unit Tests (~60 tests)      \
  /________________________________\
```

### **Unit Test Coverage**

- **ViewModels**: State management, search, pagination, navigation events
- **Use Cases**: Domain logic, data transformation, pagination, search, error handling
- **Repositories**: Data access patterns, API integration, pagination
- **Data Sources**: Mock data generation, caching strategies
- **Mappers**: Domain to UI state conversions

### **UI Test Coverage**

- **Component Tests**: Individual composable behavior and styling (SearchBar, etc.)
- **Integration Tests**: Full screen flows with ViewModel integration
- **Search Tests**: Search bar interaction, query updates, results display
- **Pagination Tests**: Infinite scroll, load more indicator
- **Accessibility Tests**: Semantic labels and navigation
- **State Tests**: Loading, success, error, search state rendering

### **Test Quality Metrics**

- ✅ **100% Critical Path Coverage**: All user journeys tested
- ✅ **Error Scenario Testing**: Network failures, empty states
- ✅ **Performance Testing**: Concurrent operations, memory usage
- ✅ **Accessibility Validation**: Screen reader compatibility
- ✅ **Search Testing**: Debounce, query handling, result display
- ✅ **Pagination Testing**: Multiple pages, state tracking

## 📋 Development Guidelines

### **Adding New Features**

1. Create feature module in `feature/` directory
2. Implement Clean Architecture layers (UI → Domain → Data)
3. Define domain models in `common:domain`
4. Create mappers to convert domain models to UI states
5. Add comprehensive test coverage (unit + UI)
6. Update dependency graph in this README

### **Domain Layer Best Practices**

- Keep domain models pure (no Android dependencies)
- Use sealed classes for result types
- Implement business logic in use cases
- Return domain-specific models (not UI states)
- Let ViewModels handle UI state mapping

### **Search & Pagination Guidelines**

- Use debounce for search input (default 300ms)
- Implement pagination with configurable page size (default 10)
- Handle loading states for both initial load and pagination
- Track search mode vs. list mode separately
- Test edge cases (empty results, network errors)

## 🔧 Configuration

### **Network Configuration** (`common:core`)

```kotlin
const val BASE_URL = "https://www.swapi.tech/api"
const val PAGE_SIZE = 10
```

### **Search Configuration** (`feature:planetlist`)

```kotlin
searchQuery
    .debounce(300)  // Adjustable debounce timeout
    .distinctUntilChanged()
```

## 🎯 Recent Improvements

### **Architecture Refactoring** (Latest)

- ✅ Added `common:domain` module for domain layer separation
- ✅ Moved use cases and domain models out of feature modules
- ✅ Implemented domain-to-UI state mappers in ViewModels
- ✅ Added Koin DI convention plugin for centralized dependency management
- ✅ Improved module dependency structure

### **Feature Additions**

- ✅ Real-time search functionality with debounced input
- ✅ Pagination support with infinite scroll
- ✅ Search bar UI component
- ✅ Enhanced test coverage for search and pagination

### **Code Quality**

- ✅ Removed unused imports across all modules
- ✅ Refactored tests to use mock use cases and flows
- ✅ Improved test data models and coverage
- ✅ Updated to latest dependencies (AGP 8.13.0, Kotlin 2.2.10)

---

**Built with ❤️ using Modern Android Development practices**