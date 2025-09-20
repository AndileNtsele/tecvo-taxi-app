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
- `TABLET_RESTRICTIONS_IMPLEMENTATION.md` - Phone-only device restrictions

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
- ✅ **Phone-only app with comprehensive tablet restrictions**

### Testing Infrastructure
- `TestTaxiApplication` - Test isolation
- `TestAppModule` - Firebase mocking
- `TestFirebaseUtil` - Multi-mode initialization
- `BaseUITest` - Null-safe matchers
- Test credentials: 072 858 8857, OTP: 123456

## 📱 PHONE-ONLY RESTRICTIONS (CRITICAL)

**This app is STRICTLY phone-only and blocks ALL tablet devices.**

### Business Justification
- **91.21%** of SA internet traffic is mobile phones
- **Only 1%** tablet usage in South Africa
- **Target users**: Taxi drivers and commuters use smartphones, not tablets
- **Use case alignment**: Real-time location sharing while driving/walking requires mobile portability

### Multi-Level Implementation

#### 1. Play Store Blocking (AndroidManifest.xml)
```xml
<supports-screens
    android:largeScreens="false"
    android:xlargeScreens="false"
    android:compatibleWidthLimitDp="600" />
```
**Result**: Tablets cannot download from Play Store

#### 2. Runtime Detection (DeviceTypeUtil.kt)
- Advanced detection using screen size, density, diagonal calculations
- Blocks tablets that attempt sideloading
- Multi-criteria validation for accuracy
- **NEW**: Foldable phone detection and special handling

#### 3. Application-Level Blocking
- `TaxiApplication.onCreate()` - Early detection and logging
- `MainActivity.onCreate()` - User-facing dialog and app closure
- `LoginScreen` & `HomeScreen` - Backup protection at UI level

### 🔄 FOLDABLE PHONE SUPPORT (NEW)

**Foldable phones are treated as phones, NOT tablets, regardless of screen size when unfolded.**

#### Smart Foldable Handling
- **Folded state**: Works like any normal phone (e.g., Galaxy Z Fold cover screen 6.3")
- **Unfolded state**: UI constrained to phone dimensions (max ~600dp width)
- **Result**: App uses only "one phone screen" worth of space on unfolded display

#### Implementation Details
```kotlin
// DeviceTypeUtil.kt
fun isFoldablePhone(context: Context): Boolean {
    // Detects Samsung, Google, OnePlus, Oppo, Xiaomi, Motorola foldables
    // Always returns false for isTablet() regardless of screen size
}

// Screen dimension selection (all screens)
if (DeviceTypeUtil.isFoldablePhone(context)) {
    // Cap at phone dimensions even when unfolded
    when {
        screenWidthDp < 400f -> CompactSmallDimens
        screenWidthDp < 500f -> CompactMediumDimens
        else -> CompactDimens  // Max ~600dp
    }
}
```

#### Supported Foldable Brands
- **Samsung**: Galaxy Z Fold/Flip series (SM-F models)
- **Google**: Pixel Fold (Felix)
- **OnePlus**: Open series
- **Oppo**: Find N series
- **Xiaomi**: Mix Fold series
- **Motorola**: Razr series
- **Huawei**: Mate X series
- **Honor**: Magic V series
- **Vivo**: X Fold series

#### User Experience
**Galaxy Z Fold User:**
- **Folded**: Normal taxi app experience on 6.3" cover screen
- **Unfolded**: Same taxi app UI constrained to phone-sized window, rest of 7.6" screen available for multitasking

### What Users Experience
**Tablets**: *"This app isn't compatible with your device"* (Play Store) or friendly blocking dialog (sideloaded)
**Foldable Phones**: Works seamlessly in both folded and unfolded states

### Development Impact
- Focus UI/UX on 320dp-600dp range (phones only)
- Foldables get phone layouts regardless of actual screen size
- Tablet dimensions (>600dp) UNUSED except for true tablets (which are blocked)
- All screens optimized for one-handed mobile operation

### Files Modified
- `AndroidManifest.xml` - Play Store restrictions
- `utils/DeviceTypeUtil.kt` - Detection logic + foldable support
- `MainActivity.kt` - Primary blocking dialog
- `LoginScreen.kt`, `HomeScreen.kt`, `MapScreen.kt`, `RoleScreen.kt`, `SettingsScreen.kt` - Foldable dimension handling
- `TaxiApplication.kt` - Early detection
- All `*Dimens.kt` files - Phone-centric sizing

See `TABLET_RESTRICTIONS_IMPLEMENTATION.md` for complete technical documentation.

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
8. **PHONE-ONLY APP** - Tablets are blocked at all levels, focus on 320-600dp

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

### Tablet Restrictions Implementation (Added - January 2025)
- **Decision**: Strategic phone-only focus based on SA market data (91.21% mobile vs 1% tablets)
- **Implementation**: Multi-level blocking - Play Store, runtime detection, app-level enforcement
- **Files**: `AndroidManifest.xml`, `DeviceTypeUtil.kt`, `MainActivity.kt`, `LoginScreen.kt`, `HomeScreen.kt`, `TaxiApplication.kt`
- **User Experience**: Clear messaging explaining phone-only policy, graceful app closure on tablets
- **Status**: ✅ **Complete phone-only enforcement, zero tablet access possible**

### Foldable Phone Support Implementation (Added - January 2025)
- **Decision**: Smart constraint approach - foldables work but maintain phone UI dimensions
- **Business Logic**: Foldable phones are premium smartphones used by target market (taxi drivers)
- **Implementation**: Enhanced device detection + dimension capping at phone sizes
- **Technical Approach**: App occupies "one phone screen worth" of space when unfolded
- **Files Enhanced**: `DeviceTypeUtil.kt` (69 lines foldable detection), all screen dimension logic
- **Supported Devices**: Samsung Z Fold/Flip, Google Pixel Fold, OnePlus Open, Oppo Find N, Xiaomi Mix Fold, Motorola Razr, Huawei Mate X, Honor Magic V, Vivo X Fold
- **User Experience**:
  - **Folded**: Normal phone app experience on cover screen
  - **Unfolded**: Same phone UI constrained to phone dimensions, enabling multitasking
- **Status**: ✅ **Complete foldable support, seamless experience both folded/unfolded**

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

## 📈 RECENT MAJOR UPDATES

### January 2025 - Foldable Phone Support & Enhanced Device Detection
- **🔥 NEW FEATURE**: Comprehensive foldable phone support across all screens
- **🎯 TARGET MARKET EXPANSION**: Premium smartphone users (Galaxy Z Fold, Pixel Fold, etc.)
- **🏗️ TECHNICAL APPROACH**: Smart dimension constraint - phone UI on foldable hardware
- **📱 USER EXPERIENCE**: Seamless folded/unfolded states, multitasking capability when unfolded
- **🔧 IMPLEMENTATION**: 69-line foldable detection system, 9 major brand support
- **✅ VALIDATION**: Build tested, documentation updated, ready for deployment

### Key Technical Achievements
1. **Enhanced DeviceTypeUtil.kt**: Industry-leading foldable detection
2. **All-Screen Coverage**: MapScreen, HomeScreen, LoginScreen, RoleScreen, SettingsScreen
3. **Smart UI Constraint**: Maintains phone-optimized layouts regardless of physical screen size
4. **Future-Proof Design**: Supports emerging foldable form factors automatically