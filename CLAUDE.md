# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 🚖 TAXI APP - CORE PURPOSE

**Purpose**: Real-time visibility app for SA taxi users - "birds eye view" of available taxis and passengers.
**Company**: TECVO | **Package**: `com.tecvo.taxi` (Production-ready)
**Philosophy**: Enhance existing taxi behavior, don't change it.

### Three Simple Screens
1. **Role Selection**: Driver or Passenger
2. **Destination**: TOWN (up) or LOCAL (down)
3. **Map**: Real-time visibility of users going same direction

### 🔥 CRITICAL DEVELOPMENT RULE
> **EVERY CODE CHANGE MUST MAINTAIN REAL-TIME VISIBILITY ACCURACY**
>
> False positives destroy trust. If someone appears on the map, they MUST be there and available.

## 🔒 PRIVACY-BY-DESIGN

**Service-based model, NOT data collection app**
- Temporary location data only while on map
- Automatic cleanup when leaving map (`MapViewModel.cleanupFirebaseUserData()`)
- No permanent storage, profiles, or tracking
- Firebase structure: `drivers/town/{userId}`, `passengers/local/{userId}`

### Development Rules
- ❌ Never store location history or permanent data
- ❌ Never add analytics/tracking beyond operational needs
- ❌ Never weaken automatic cleanup systems
- ✅ Always include cleanup logic for temporary data
- ✅ Enhance real-time visibility accuracy
- ✅ Keep data flows temporary

## 📚 PROJECT STRUCTURE

```
app/src/main/java/com/tecvo/taxi/
├── components/     # 9 UI components (AnimatedHeader, CountBadge, etc.)
├── screens/        # Map, Login, Home, Role Selection screens
├── viewmodel/      # 6 ViewModels with MVVM architecture
├── services/       # 15 services (Location, Notification, Geocoding, etc.)
├── navigation/     # Compose Navigation
├── ui/            # Theme, dialogs, typography (Joti One font)
├── utils/         # Utilities and helpers
├── models/        # Data models
├── di/            # Hilt dependency injection
└── integration/   # External services
```

### Documentation
- `docs/ARCHITECTURE.md` - Technical architecture
- `docs/DEVELOPMENT.md` - Build commands
- `docs/TESTING.md` - Test infrastructure
- `docs/FIREBASE.md` - Real-time patterns
- `docs/CONFIGURATION.md` - Setup instructions
- `docs/COMPLETED_FEATURES.md` - Feature documentation

## 🎨 BRANDING

- **Font**: Joti One (`JotiOneText` components)
- **Icons**: Complete set (hdpi to xxxhdpi)
- **Material 3**: Modern design system

```kotlin
// Use JotiOneText for brand consistency
JotiOneText(
    text = "TOWN",
    fontSize = 24.sp,
    color = Color.White
)
```

## 🛠️ DEVELOPMENT

### Commands
```bash
# Tests (193 unit tests, 242 UI tests)
./gradlew testDebugUnitTest
./gradlew connectedAndroidTest

# Build
./gradlew assembleDebug
./gradlew assembleRelease

# KAPT Health Check
./gradlew kaptGenerateStubsDebugKotlin
```

### Current Status
- ✅ 82 Kotlin files, 33 test files
- ✅ 193 unit tests (100% pass rate)
- ✅ 242 UI tests (infrastructure ready)
- ✅ Production package: `com.tecvo.taxi`
- ✅ **Play Store ready (100% complete)**
- ✅ **Security hardened with Google's Secrets Gradle Plugin**

### Testing Infrastructure
- `TestTaxiApplication` - Test isolation
- `TestAppModule` - Firebase mocking
- `TestFirebaseUtil` - Multi-mode initialization
- `BaseUITest` - Null-safe matchers
- Test credentials: 072 858 8857, OTP: 123456

## ⚡ STRATEGIC PRIORITIES

### Fuel Economy Focus
App's value: Save R200-500/day in wasted fuel during off-peak hours.
- One false positive = R20 wasted = app deleted
- Most valuable 9am-3pm when passengers are scarce

### Rural-First Design
- Must work on 2G/EDGE (50-150 kbps)
- Handle 10-20km coverage gaps
- Minimize data costs for prepaid users
- Battery optimization for 30-60 minute waits

### Scaling Costs (1000 users)
- Firebase: $20-50/month (cleanup keeps it lean)
- Google Maps API: $300-800/month (main cost)
- Total: ~R1,000-2,500/month

### Development Warnings
**Never implement:**
- Heavy features during peak hours
- Data-intensive features without offline fallback
- Anything causing false positives
- Global timeouts interfering with navigation

**Always consider:**
- Will this work on rural 2G?
- Does this save driver fuel?
- Can this handle loadshedding?
- Is data cost justified?

## 🚨 CRITICAL REMINDERS

1. **Real-time accuracy is everything** - Trust depends on it
2. **Firebase cleanup is mandatory** - Maintains visibility accuracy
3. **3-screen simplicity** - Role → TOWN/LOCAL → Map
4. **SA taxi authenticity** - TOWN (up) and LOCAL (down) like at ranks
5. **Privacy-by-design** - Temporary service, not data collection
6. **Test coverage** - 193 tests ensure reliability
7. **Navigation timing** - No global timeouts that interfere with user flow

## 🔧 CRITICAL BUG FIXES

### Navigation Timing Issue (Fixed)
- **Problem**: 10-second timeout in `MainActivity` redirected users during permission flow
- **Solution**: Removed problematic timeout
- **File**: `MainActivity.kt` (handlePostRegistrationPermissions)

### API Key Security Issue (Fixed - September 2025)
- **Problem**: Real API keys exposed in repository files (Play Store blocking issue)
- **Solution**: Implemented Google's Secrets Gradle Plugin for secure key management
- **Files**: `build.gradle.kts`, `secrets.properties`, `local.defaults.properties`
- **Status**: ✅ **100% Play Store compliant, zero code changes required**

## 🔒 API KEY SECURITY (CRITICAL)

**⚠️ NEVER COMMIT REAL API KEYS TO GIT ⚠️**

### Secure Setup (Google's Secrets Gradle Plugin)
```bash
# File structure for API key security:
secrets.properties           # REAL keys (Git-ignored, NEVER commit)
local.defaults.properties    # Placeholder keys (Git-tracked, safe)
local.properties            # Contains placeholders only
```

### Development Workflow
1. **Real keys** go in `secrets.properties` (automatically Git-ignored)
2. **Placeholder keys** in `local.defaults.properties` (safe for Git)
3. **BuildConfig generation** handled automatically by plugin
4. **No code changes needed** - all existing `BuildConfig.MAPS_API_KEY` usage preserved

### Security Validation
```bash
./validate_security.bat  # Must pass before any Git commits
```

**Key files that must NEVER contain real API keys:**
- `local.properties` (placeholders only)
- `build.gradle.kts` (no hardcoded keys)
- Any file tracked by Git

## 📝 IMPORTANT INSTRUCTIONS

- Do what's asked; nothing more, nothing less
- NEVER create files unless absolutely necessary
- ALWAYS prefer editing existing files
- NEVER proactively create documentation files
- Follow existing code conventions and patterns
- Check package.json/build.gradle before assuming libraries
- NO comments unless explicitly requested
- **CRITICAL**: Run `./validate_security.bat` before any commits