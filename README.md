# 🪐 Star Wars Planets Explorer

A modern Android application showcasing **Clean Architecture**, **Modular Design**, and *
*Squad-Based Development** practices. This app displays Star Wars planets with detailed information,
featuring a sophisticated two-phase data loading strategy and comprehensive testing coverage.

## 📸 Preview

<table align="center">
<tr>
<td align="center">
<h3>Planet List Screen</h3>
<img src="./screenshot/Screen%231.png" width="300" alt="Planet List Screen"/>
<br>
<em>Two-phase loading with shimmer effects</em>
</td>
<td align="center">
<h3>Planet Detail Screen</h3>
<img src="./screenshot/Screen%232.png" width="300" alt="Planet Detail Screen"/>
<br>
<em>Gradient styling and comprehensive planet info</em>
</td>
</tr>
</table>

<p align="center"><strong>Modern Material 3 design with progressive loading and accessibility support</strong></p>

> 📂 **Screenshots Location**: [`screenshot/`](./screenshot/) directory  
> 🖼️ **Files**: `Screen#1.png` (Planet List) • `Screen#2.png` (Planet Details)  
> 💡 **Tip**: If images don't load, check your markdown viewer's support for relative paths

## 📱 Features

- **Planet List**: Browse all Star Wars planets with initial loading and progressive detail
  enhancement
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

- **`feature:planetlist`**: Planet browsing and list management
- **`feature:planetdetail`**: Individual planet detail screens
- **Squad Ownership**: Feature squads can own individual feature modules
- **Benefits**: Independent development, deployment, and testing

#### **Common Modules** (`common:*`)

- **`common:core`**: Shared utilities, base classes, network configuration
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
│            (Screens, ViewModels, UI)            │
├─────────────────────────────────────────────────┤
│                 💼 Domain                       │
│              (UseCases, Models)                 │
├─────────────────────────────────────────────────┤
│                 💾 Data                         │
│         (Repositories, DataSources, API)        │
└─────────────────────────────────────────────────┘
```

#### **Presentation Layer** 📱

- **Composables**: Modern Jetpack Compose UI
- **ViewModels**: State management with coroutines
- **UI State**: Immutable state classes with sealed hierarchies

#### **Domain Layer** 💼

- **Use Cases**: Business logic encapsulation (`PlanetListUseCase`)
- **Models**: Pure domain entities (`Planet`, `PlanetDetails`)
- **Interfaces**: Repository contracts

#### **Data Layer** 💾

- **Repositories**: Data access abstraction (`PlanetRepository`)
- **Data Sources**: API and local data management
- **Network**: Ktor HTTP client with serialization

## 🛠️ Core Technologies

### **Android Stack**

- 🎯 **Kotlin 2.2.10** - Modern programming language
- 📱 **Jetpack Compose** (BOM 2025.08.01) - Declarative UI
- 🏗️ **Material 3** - Google's latest design system
- 🧭 **Navigation Compose 2.9.3** - Type-safe navigation

### **Architecture Components**

- 🔄 **Lifecycle 2.9.3** - ViewModel, LiveData, StateFlow
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

The application implements a **sophisticated two-phase loading strategy** optimized for user
experience:

### 🔄 Phase 1: Initial Load (Fast)

```
API Call → Basic Planet List → Immediate UI Display
```

- Fetches basic planet information (names, UIDs)
- Displays planets immediately with loading shimmer
- Provides instant visual feedback to users

### 📝 Phase 2: Detail Enhancement (Progressive)

```
Concurrent API Calls → Planet Details → Progressive UI Updates
```
- Loads detailed planet information concurrently
- Updates UI progressively as each planet's details arrive
- Uses default concurrency from Kotlin coroutines (16 concurrent requests)
- Handles errors gracefully without blocking other planets

### 💡 Implementation Details

```kotlin
// PlanetListUseCase - Two-phase loading implementation
fun observePlanets(pageSize: Int = 10): Flow<PlanetListUiState> = flow {
    emit(ListLoading)                              // Phase 0: Loading state
    
    val planets = planetRepository.getPlanetsWithPagination(pageSize)
    emit(ListSuccess(planets.asLoadingItems()))    // Phase 1: Quick display
    
    planets.asFlow()
        .flatMapMerge { planet ->
            // Phase 2: Progressive enhancement (uses DEFAULT_CONCURRENCY = 16)
            planetRepository.getPlanet(planet.uid)
        }
        .collect { planetDetails ->
            updateStateWithDetails(planetDetails)  // Progressive UI updates
        }
}
```

### 🎯 Benefits

- **⚡ Fast Initial Load**: Users see content within milliseconds
- **📈 Progressive Enhancement**: Details appear as they're loaded
- **🚦 Optimized Network**: Concurrent API requests with Kotlin coroutines default (16)
- **💪 Resilient**: Individual planet failures don't affect others

## 🧪 Testing Coverage

### 📊 Test Statistics

- **📁 Total Test Files**: 11 files
- **🧪 Unit Tests**: 4 files (~51 test methods)
- **🎭 UI Tests**: 7 files (~58 test methods)
- **📈 Total Test Methods**: ~109 test methods
- **✅ Current Status**: All tests passing

### 🏗️ Testing Architecture

#### **Unit Tests** 🧪

```
📁 Unit Test Coverage:
├── 🧠 ViewModels (15 tests)
│   ├── PlanetDetailViewModelTest.kt
│   └── PlanetListViewModelTest.kt
├── 💼 Use Cases (13 tests) 
│   └── PlanetListUseCaseTest.kt
├── 💾 Data Sources (11 tests)
│   └── InMemoryPlanetDetailsDataSourceTest.kt
└── 📊 Business Logic Testing
```

**Key Test Coverage:**

- ✅ **State Management**: ViewModel state transitions
- ✅ **Business Logic**: Use case implementations
- ✅ **Data Operations**: Repository and data source logic
- ✅ **Error Handling**: Network failures and edge cases
- ✅ **Coroutines**: Async operations and flow testing

#### **UI Tests** 🎭

```
📁 UI Test Coverage:
├── 🧩 Component Tests (42 tests)
│   ├── PlanetComposableTest.kt (11 tests)
│   ├── NucleusAppBarTest.kt (9 tests)  
│   ├── ProgressBarComposableTest.kt (9 tests)
│   ├── ShimmerComposableTest.kt (7 tests)
│   └── ErrorComposableTest.kt (6 tests)
└── 📱 Integration Tests (16 tests)
    ├── PlanetListScreenTest.kt (8 tests)
    └── PlanetDetailsScreenTest.kt (8 tests)
```

**Key Test Coverage:**

- ✅ **UI Components**: Individual composable behavior
- ✅ **Screen Integration**: Full screen with ViewModel integration
- ✅ **User Interactions**: Clicks, navigation, accessibility
- ✅ **State Rendering**: Loading, success, error states
- ✅ **Visual Validation**: Text display, styling, layout

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
cd Nucleus

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
Nucleus/
├── 🚀 app/                           # Application module
│   └── src/main/java/                # App entry point & DI setup
├── 🌟 feature/                       # Feature modules
│   ├── planetlist/                   # Planet list feature
│   │   ├── src/main/                 # Screen, ViewModel, UseCase
│   │   ├── src/test/                 # Unit tests (11 tests)
│   │   └── src/androidTest/          # UI tests (8 tests)
│   └── planetdetail/                 # Planet detail feature
│       ├── src/main/                 # Screen, ViewModel
│       ├── src/test/                 # Unit tests (15 tests)
│       └── src/androidTest/          # UI tests (8 tests)
├── 🔧 common/                        # Shared modules
│   ├── core/                         # Base classes & utilities
│   ├── data/                         # Repository & API layer
│   │   └── src/test/                 # Unit tests (25 tests)
│   ├── ui/                           # Reusable UI components  
│   │   └── src/androidTest/          # UI component tests (42 tests)
│   └── navigation/                   # Navigation logic
└── 🛠️ build-logic/                   # Custom Gradle plugins
```

## 🎨 UI Components

### **Design System**

- **🎨 Material 3**: Modern Google design language
- **🔤 Typography**: Monospace fonts with gradient styling
- **🌈 Theming**: Dynamic styling with random gradient colors
- **♿ Accessibility**: Full semantic labeling and navigation support

### **Key Components**

- **`PlanetComposable`**: Main planet list item with click handling
- **`PlanetNameComposable`**: Stylable planet names with gradient effects
- **`NucleusAppBar`**: Consistent app bar with back navigation
- **`ShimmerComposable`**: Loading animation for better UX
- **`ErrorComposable`**: Unified error state handling

## 🔄 State Management

### **UI State Pattern**

```kotlin
// Hierarchical state management
sealed class PlanetDetailsUiState {
    data object DetailsLoading : PlanetDetailsUiState
    data class DetailsSuccess(val details: PlanetDetails) : PlanetDetailsUiState  
    data class DetailsError(val errorMessage: String? = null) : PlanetDetailsUiState
}
```

### **Data Flow**

```
🔄 User Action → ViewModel → UseCase → Repository → DataSource → API
                     ↓
📱 UI State ←─── StateFlow ←─── Domain Logic ←─── Data Layer ←─── Network
```

## 🏗️ Build System

### **Custom Gradle Plugins**

The project uses custom convention plugins for consistent configuration:

- **`local.android.application`**: App module configuration
- **`local.android.library`**: Common library setup
- **`local.android.library.compose`**: Compose-enabled libraries
- **`local.android.feature`**: Feature module conventions

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
- 🔍 **Testability**: High test coverage with isolated unit and UI tests
- 🛡️ **Type Safety**: Compile-time guarantees with sealed classes
- 📱 **Modern UI**: Declarative Compose with Material 3

### **For Users**

- ⚡ **Fast Loading**: Two-phase loading for immediate visual feedback
- 🎨 **Beautiful UI**: Modern design with gradient effects and animations
- ♿ **Accessible**: Full accessibility support for all users
- 🔄 **Reliable**: Robust error handling and offline capabilities

## 🧪 Testing Philosophy

### **Testing Pyramid Implementation**

```
        🔺 UI Tests (58 tests)
       /                    \
      /   Integration Tests  \
     /________________________\
    /                          \
   /      Unit Tests (51 tests)  \
  /________________________________\
```

### **Unit Test Coverage**

- **ViewModels**: State management, business logic, navigation events
- **Use Cases**: Domain logic, data transformation, error handling
- **Repositories**: Data access patterns, API integration
- **Data Sources**: Mock data generation, caching strategies

### **UI Test Coverage**

- **Component Tests**: Individual composable behavior and styling
- **Integration Tests**: Full screen flows with ViewModel integration
- **Accessibility Tests**: Semantic labels and navigation
- **State Tests**: Loading, success, and error state rendering

### **Test Quality Metrics**

- ✅ **100% Critical Path Coverage**: All user journeys tested
- ✅ **Error Scenario Testing**: Network failures, empty states
- ✅ **Performance Testing**: Concurrent operations, memory usage
- ✅ **Accessibility Validation**: Screen reader compatibility

## 📋 Development Guidelines

### **Adding New Features**

1. Create feature module in `feature/` directory
2. Implement Clean Architecture layers (UI → Domain → Data)
3. Add comprehensive test coverage (unit + UI)
4. Update dependency graph in this README