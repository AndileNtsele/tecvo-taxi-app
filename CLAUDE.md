# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 🚖 TECVO TAXI - CORE PURPOSE & MISSION

### **THE FUNDAMENTAL PURPOSE (Technical)**
**Real-time visibility app that gives SA taxi users a "birds eye view" of available taxis and passengers. Simple 3-screen enhancement to existing taxi pickup behavior - Role selection, TOWN/LOCAL destination, live map visibility.**

### **TECVO COMPANY CONTEXT**
- **Company**: TECVO (Registered South African Technology Company)
- **Package Name**: `com.tecvo.taxi` (Production-ready)
- **Mission**: Real-time visibility enhancement for SA taxi industry - technology that extends natural abilities
- **Target Market**: South African taxi operators and passengers using traditional pickup points
- **Core Philosophy**: Enhance existing taxi behavior, don't change it - users walk to usual pickup spots, app shows what's coming

### **HOW IT WORKS (Authentic SA Taxi Experience)**
**Three Simple Screens:**
1. **Role Selection**: Driver or Passenger (simple choice)
2. **Destination Selection**: TOWN (point up) or LOCAL (point down) - just like at taxi ranks
3. **Live Map Visibility**: Real-time view of taxis/passengers going same direction

**Technical Flow:**
1. User walks to usual taxi pickup point, opens app
2. Selects role and destination → gets written to Firebase at `drivers/town/{userId}` or `passengers/local/{userId}`
3. Map shows ONLY users going same direction (filtered view)
4. Passengers see taxis coming, drivers see passengers waiting at pickup points
5. User leaves map → gets removed from Firebase (maintains accuracy)

**Key Insight**: App doesn't change pickup behavior - users still go to same spots, just get visibility of what's coming

### **CRITICAL REQUIREMENT: REAL-TIME VISIBILITY ACCURACY**
- If someone appears on the map, they MUST actually be there and available
- False positives (showing unavailable users) destroy trust in the visibility system
- The map provides "birds eye view" - this is where the value happens
- Everything else (login, role selection, settings) is just setup for the core visibility experience
- Users must trust that what they see is real - "eyes in the sky" accuracy

### **🔥 DEVELOPMENT RULE #1**
> **EVERY CODE CHANGE MUST BE EVALUATED AGAINST VISIBILITY ACCURACY**
> 
> When making ANY change, ask: "Does this improve or maintain the accuracy of real-time visibility? Will users trust what they see?"

---

## 📚 DOCUMENTATION STRUCTURE

This project follows **modular documentation** for better organization and maintainability:

### **📖 Core Documentation**
- **[`docs/IDEOLOGY.md`](docs/IDEOLOGY.md)** - South African taxi industry context & strategic intelligence
- **[`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)** - Technical architecture, patterns, and dependencies  
- **[`docs/DEVELOPMENT.md`](docs/DEVELOPMENT.md)** - Build commands, development workflows, and patterns
- **[`docs/TESTING.md`](docs/TESTING.md)** - Unit tests (175+), UI tests (140+), and coverage
- **[`docs/FIREBASE.md`](docs/FIREBASE.md)** - Real-time patterns and presence management
- **[`docs/CONFIGURATION.md`](docs/CONFIGURATION.md)** - Setup instructions, API keys, and dependencies
- **[`docs/COMPLETED_FEATURES.md`](docs/COMPLETED_FEATURES.md)** - Detailed documentation of all completed features and implementations

### **🚀 Quick Start**
```bash
# Setup project
See docs/CONFIGURATION.md

# Build and run
See docs/DEVELOPMENT.md  

# Run tests
./gradlew testDebugUnitTest      # 175+ unit tests
./gradlew connectedAndroidTest   # 140+ UI tests

# Architecture overview
See docs/ARCHITECTURE.md

# Completed features
See docs/COMPLETED_FEATURES.md
```

### **🎯 Key Development Principles**
1. **Visibility-centric development** - Every feature serves real-time "birds eye view" accuracy
2. **Enhancement philosophy** - Enhance existing taxi behavior, never change it
3. **Real-time precision** - Firebase presence must be 100% accurate for trust
4. **Simplicity focus** - 3 screens maximum: Role → TOWN/LOCAL → Map visibility
5. **SA taxi authenticity** - TOWN (up) and LOCAL (down) just like pointing at taxi ranks

---

## 🎯 DEVELOPMENT STATUS UPDATE - SEPTEMBER 2025

### **🚀 Current Architecture**: Production-Grade Professional Implementation
- ✅ **82 Main Kotlin Files**: Comprehensive feature-complete architecture
- ✅ **33 Test Files**: Robust testing coverage (Unit + UI)
- ✅ **15 Professional Services**: Location, Notification, Geocoding, Error Handling, Analytics
- ✅ **6 ViewModels**: MVVM architecture with proper state management
- ✅ **9 Components**: Reusable UI components including animated headers
- ✅ **Advanced Map Implementation**: Multi-screen map architecture with tracking

### **🎬 NEW FEATURE: Professional Animated Headers**
- ✅ **Implemented**: Role-specific animations (Push/Pull mechanics)
- ✅ **Physics-Based**: Spring animations with realistic damping and bounce
- ✅ **Storytelling**: "I [ROLE] am going to [DESTINATION]" visual narrative
- ✅ **File**: `AnimatedHeader.kt` with 4-phase animation sequences

### **🔧 Production-Ready Infrastructure**:
- ✅ **Real-Time Taxi Matching**: Firebase presence with 99.5%+ accuracy
- ✅ **Advanced Location Services**: Multi-layer GPS with battery optimization (`LocationService.kt`)
- ✅ **Professional Notifications**: Full notification management system (`NotificationService.kt`)
- ✅ **Geocoding Service**: Address resolution with fallback API support
- ✅ **Error Handling**: Comprehensive error reporting and crash management
- ✅ **Analytics Integration**: User behavior tracking and performance monitoring

### **🏗️ Advanced Architecture Components**:
- ✅ **Dependency Injection**: Hilt implementation across all layers
- ✅ **State Management**: Proper ViewModel + StateFlow patterns  
- ✅ **Navigation**: Compose Navigation with proper route management
- ✅ **Real-time Updates**: Firebase Realtime Database integration
- ✅ **Permission Management**: Location permission handling
- ✅ **Service Layer**: Comprehensive background service architecture

### **🔐 Security & Production Configuration**:
- ✅ **Secure Keystore**: Environment variable-based credential management
- ✅ **Production Package**: `com.tecvo.taxi` with proper signing configuration
- ✅ **API Key Management**: Secure BuildConfig and manifest placeholder patterns
- ✅ **Firebase Security**: Proper authentication and database rules
- ✅ **Crash Reporting**: Firebase Crashlytics integration

### **🎨 Professional Branding & Assets**:
- ✅ **App Icons**: Complete icon set across all resolutions (hdpi to xxxhdpi)
- ✅ **Visual Assets**: Background images, launcher icons, notification icons
- ✅ **Brand Identity**: TAXI.RANK branding asset (WebP format)
- ✅ **Material 3 Design**: Modern UI components with proper theming

### **🧪 Testing Excellence**:
- **Unit Tests**: Comprehensive ViewModel and service testing
- **UI Tests**: Full screen and component testing with Hilt integration
- **Integration Tests**: Firebase and location service testing
- **Test Infrastructure**: Custom test runners and utilities

### **📱 Play Store Readiness**: 98%+ Complete
- ✅ **Production Build**: Release configuration with ProGuard optimization
- ✅ **Keystore Security**: Secure signing with environment variables
- ✅ **Package Structure**: Professional `com.tecvo.taxi` namespace
- ✅ **Visual Assets**: Complete app icon and branding package  
- ✅ **Performance**: Memory-optimized (26-29MB usage)
- ⏳ **Final Steps**: Production keystore setup, final security audit

---

## 🚨 CRITICAL REMINDERS

- **Real-time visibility accuracy is everything** - False positives destroy trust in "birds eye view" system
- **Firebase presence cleanup mandatory** - Remove users when they leave map for accurate visibility
- **Enhancement not disruption** - Technology extends abilities, doesn't change taxi behavior
- **3-screen simplicity** - Role → TOWN/LOCAL → Map visibility (never more complex)
- **SA taxi authenticity** - TOWN (up) and LOCAL (down) just like pointing at ranks
- **Package name is PRODUCTION** - `com.tecvo.taxi` ready for Play Store submission ✅
- **Location precision required** - Users must trust what they see (7-13m GPS accuracy achieved)
- **Test all changes** - 315+ tests ensure reliability of visibility system

---

## 🔍 ACTIVE DEVELOPMENT TOOLS

### **Real-Time Log Monitoring** 🚨
- **Status**: ACTIVE log monitoring for `com.tecvo.taxi` package
- **Scope**: Firebase, Authentication, Location, GPS, Maps, crashes, errors
- **Purpose**: Detect issues during app testing and provide instant feedback
- **Command**: `adb logcat` filtering for TECVO TAXI specific logs

### **Testing Commands (Production Package)**
```bash
# Unit Tests (33 test files covering all ViewModels and Services)
./gradlew testDebugUnitTest

# UI Tests (Full screen and component coverage)
./gradlew connectedAndroidTest

# Specific UI Test Class
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.tecvo.taxi.ExampleInstrumentedTest

# Production Build (Release-ready with ProGuard optimization)
./gradlew assembleRelease  # Uses secure keystore signing

# Debug Build (Development testing)
./gradlew assembleDebug   # Uses com.tecvo.taxi namespace
```

### **🏗️ Architecture Overview**
```
app/src/main/java/com/tecvo/taxi/
├── components/          # 9 reusable UI components (AnimatedHeader, CountBadge, etc.)
├── screens/            # All app screens (Map, Login, Home, Role Selection)
├── viewmodel/          # 6 ViewModels (Map, Login, Settings, Role, Home, CityOverview)
├── services/           # 15 production services (Location, Notification, Geocoding, etc.)
├── navigation/         # Compose Navigation setup
├── ui/                 # Theme, dialogs, styling
├── utils/              # Utilities and helpers
├── models/             # Data models
├── constants/          # App constants
├── di/                 # Hilt dependency injection
└── integration/        # External service integrations
```

---

For detailed information on completed features, implementations, and technical deep-dives, see [`docs/COMPLETED_FEATURES.md`](docs/COMPLETED_FEATURES.md).

---

## 🚀 MAJOR DEVELOPMENT MILESTONES (SEPTEMBER 2025)

### **🎬 Professional Animation System**
- **Animated Headers**: Physics-based role-specific animations
- **Push/Pull Mechanics**: Passenger "pushes" destination, Driver "pulls" passengers
- **Spring Physics**: Realistic bounce and damping effects
- **4-Phase Sequences**: Complex multi-step animation choreography

### **🏗️ Production Architecture Completion**
- **Service Layer**: 15 professional services for all app functionality
- **MVVM Pattern**: Complete ViewModel implementation across all screens  
- **Dependency Injection**: Full Hilt integration for testability and modularity
- **Error Handling**: Comprehensive error reporting and recovery systems

### **🔐 Security & Production Readiness**
- **Secure Keystore**: Environment variable-based credential management
- **Production Signing**: Release build configuration with ProGuard
- **API Security**: Proper key management and BuildConfig patterns
- **Firebase Security**: Authentication and database rule implementation

### **🎨 Professional Branding**
- **Complete Icon Set**: All resolutions (hdpi to xxxhdpi) with proper launcher icons
- **Brand Assets**: TAXI.RANK branding materials and visual identity
- **Material 3**: Modern design system implementation
- **Visual Polish**: Professional background images and UI components

### **🧪 Testing Excellence**  
- **Comprehensive Coverage**: 33 test files covering critical functionality
- **UI Testing**: Full screen and component testing with proper mocking
- **Integration Testing**: Firebase and service layer testing
- **Test Infrastructure**: Custom test runners and utilities for reliability