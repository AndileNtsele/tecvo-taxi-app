# 🏗️ ARCHITECTURE OVERVIEW

This is an Android taxi application built with Kotlin and Jetpack Compose. The app supports both passenger and driver roles with real-time location tracking, Google Maps integration, and Firebase backend services using modern Android architecture patterns.

## **CORE ARCHITECTURE**
- **MVVM (Model-View-ViewModel)** with Jetpack Compose UI
- **Hilt Dependency Injection** for dependency management
- **Repository Pattern** for data abstraction
- **Service-oriented architecture** for background operations
- **Single Activity** architecture with Navigation Compose

## **KEY COMPONENTS**

### **Application Layer**
- `TaxiApplication.kt` - Main application class with Hilt setup and service initialization
- `MainActivity.kt` - Single activity hosting all navigation

### **Dependency Injection (`di/`)**
- `AppModule.kt` - Core singletons (Firebase, SharedPreferences, managers)
- `RepositoryModule.kt` - Data layer dependencies
- `PermissionModule.kt` - Permission handling dependencies
- `ScreenComponentsModule.kt` - UI component dependencies

### **Services (`services/`)**
- `LocationService.kt` - GPS tracking and location updates
- `NotificationService.kt` - Push notifications and local alerts
- `AppInitManager.kt` - Service initialization orchestration
- `ErrorHandlingService.kt` - Centralized error handling with Crashlytics
- `GeocodingService.kt` - Address geocoding with primary/secondary API keys
- Various managers for state coordination (LocationServiceManager, NotificationStateManager, etc.)

### **Screen Architecture (`screens/`)**
- **Login Flow**: `loginscreens/LoginScreen.kt`
- **Role Selection**: `rolescreen/RoleScreen.kt`  
- **Home Dashboard**: `homescreens/HomeScreen.kt`
- **Map Interface**: `mapscreens/` - Componentized map functionality:
  - `MapScreen.kt` - Main map container
  - `MapScreenEntityTracking.kt` - Entity tracking logic
  - `MapScreenLocationTracking.kt` - Location-specific operations
  - `MapScreenUIControls.kt` - Map controls and overlays
  - `MapScreenStateManagement.kt` - State handling
  - `MapScreenMapRendering.kt` - Map display and rendering
  - `MapScreenErrorHandling.kt` - Error handling for map operations
  - `MapScreenNotificationManagement.kt` - Notification coordination
- **Settings**: `settingsscreen/SettingsScreen.kt`

### **Data Layer**
- `repository/AuthRepository.kt` - Firebase authentication operations
- `repository/UserPreferencesRepository.kt` - DataStore preferences management
- `model/` - Data classes (User.kt, Location.kt)

## **🎯 MAP-CENTRIC DEVELOPMENT PATTERNS**

### **Firebase Presence Management (CRITICAL)**
- **Write to Firebase**: ONLY when user enters map screen and is genuinely available
- **Maintain in Firebase**: During screen recreation, app backgrounding from map
- **Remove from Firebase**: ONLY when user truly leaves map or app terminates
- **NEVER cleanup on ViewModel disposal** - this removes available users!

### **Entity Monitoring Lifecycle**
- Start monitoring: On map screen entry with state guards to prevent duplicates
- Maintain monitoring: Stable Firebase listeners with proper error handling
- Stop monitoring: On true map exit with complete state reset
- **Prevent rapid start/stop cycles** that break user discovery

### **Real-Time Accuracy Principles**
```kotlin
// ✅ CORRECT: Navigation-based cleanup
DisposableEffect(navController) {
    onDispose {
        if (leavingMapScreens && !appInBackground) {
            cleanupFirebasePresence() // Only when truly unavailable
        }
    }
}

// ❌ WRONG: ViewModel-based cleanup  
override fun onCleared() {
    cleanupFirebasePresence() // Removes available users!
}
```

## **SERVICE INITIALIZATION**
- Lazy initialization pattern through `AppInitManager.kt` prevents startup delays
- Service dependencies orchestrated to avoid circular dependencies
- Application-scoped coroutines manage background operations

## **LOCATION ARCHITECTURE**
- Google Play Services Location API for GPS tracking
- Permission handling abstracted through `PermissionManagerImpl.kt`
- Background/foreground location updates coordinated via service managers
- **Location updates directly feed Firebase presence for real-time accuracy**

## **ERROR HANDLING STRATEGY**
- Centralized error handling via `ErrorHandlingService.kt`
- Firebase Crashlytics integration for production error reporting
- Timber logging with conditional debug trees
- **Map functionality takes precedence in error recovery scenarios**

## **MAP SCREEN COMPONENTIZATION**
The map interface is split into focused, reusable components:
- **Entity tracking** - Real-time location and entity management
- **Location services** - GPS and location-specific operations  
- **UI controls** - Map overlays, buttons, and user interactions
- **State management** - Centralized state coordination
- **Error handling** - Map-specific error scenarios
- **Notification management** - Location-based alerts and updates

This modular approach enables code reuse across different role-specific map implementations and maintains separation of concerns for complex map functionality.

## **KEY DEPENDENCIES**
- **UI Framework**: Jetpack Compose with Material Design 3
- **Dependency Injection**: Hilt  
- **Async Programming**: Kotlin Coroutines + Flow
- **Maps**: Google Maps Compose + Maps Utils
- **Backend**: Firebase suite (Auth, Database, Analytics, Crashlytics, Performance)
- **Networking**: Retrofit + OkHttp with Gson
- **Image Loading**: Coil Compose
- **Logging**: Timber
- **Testing**: JUnit, MockK, Mockito, Robolectric, Truth, Turbine

## **BUILD CONFIGURATION DETAILS**

### **Gradle Setup**
- Uses Gradle Version Catalogs (`gradle/libs.versions.toml`) for dependency management
- Kotlin 2.0.0 with Compose compiler compatibility
- Target SDK 35, Min SDK 26
- ProGuard enabled for release builds with resource shrinking
- Kapt optimizations for Hilt compilation performance

### **Performance Optimizations**
- Lazy service initialization prevents application startup delays
- Background dispatchers prevent main thread blocking
- Timber conditional logging based on build type
- LeakCanary integration for debug memory leak detection
- Multidex enabled for debug builds handling large dependency sets

## **OVERVIEW FEATURE INTEGRATION**
The codebase includes a recently integrated overview feature that:
- Uses complete architectural isolation in `com.example.taxi.overview.*` package
- Implements geographic intelligence with boundary visualization
- Integrates with Google Places API with South Africa bias  
- Has built-in performance monitoring and auto-disable capabilities
- Uses overlay architecture to avoid core functionality changes