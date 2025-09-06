# ⚙️ CONFIGURATION & SETUP GUIDE

## **ESSENTIAL SETUP FILES**

### **1. local.properties** 
Copy from `local.properties.template` and configure:

```properties
# Google Maps API Key (Required)
MAPS_API_KEY=your_google_maps_api_key_here

# Geocoding API Keys (Required)
GEOCODING_API_KEY=your_primary_geocoding_api_key
GEOCODING_API_KEY_SECONDARY=your_secondary_geocoding_api_key

# Firebase Database URL (Required)
FIREBASE_DATABASE_URL=https://your-project-default-rtdb.firebaseio.com/

# Android SDK Directory (Required)
sdk.dir=C:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
```

### **2. google-services.json**
- Download from Firebase Console
- Place in `app/` directory  
- **NEVER commit to version control**

## **FIREBASE CONFIGURATION**

### **Firebase Console Setup**
1. **Create Firebase Project**
   - Go to [Firebase Console](https://console.firebase.google.com)
   - Create new project or select existing
   - Enable Google Analytics (recommended)

2. **Add Android App**
   - Package name: `com.example.taxi` (change to your unique package)
   - App nickname: "Taxi App"
   - SHA-1 certificate: Generate for debug/release signing

3. **Enable Required Services**
   - **Authentication**: Enable Google Sign-In provider
   - **Realtime Database**: Create in test mode, then secure with rules
   - **Analytics**: Enable for user behavior tracking
   - **Crashlytics**: Enable for crash reporting
   - **Performance**: Enable for performance monitoring

### **Firebase Realtime Database Rules**
```javascript
{
  "rules": {
    "drivers": {
      "$destination": {
        "$userId": {
          ".write": "$userId === auth.uid",
          ".read": true,
          ".validate": "newData.hasChildren(['userId', 'role', 'destination', 'location', 'timestamp'])"
        }
      }
    },
    "passengers": {
      "$destination": {  
        "$userId": {
          ".write": "$userId === auth.uid",
          ".read": true,
          ".validate": "newData.hasChildren(['userId', 'role', 'destination', 'location', 'timestamp'])"
        }
      }
    }
  }
}
```

## **GOOGLE SERVICES CONFIGURATION**

### **Google Maps API Setup**
1. **Enable APIs in Google Cloud Console**
   - Maps SDK for Android
   - Places API (if using places features)
   - Geocoding API

2. **Create API Keys**
   - Main Maps API key for app
   - Restricted API key for geocoding
   - Secondary API key for redundancy

3. **API Key Restrictions**
   - Restrict to your package name
   - Restrict to specific APIs only
   - Set usage quotas as needed

### **Google Sign-In Configuration**
1. **OAuth 2.0 Configuration**
   - Web client ID in google-services.json
   - SHA-1 fingerprints for debug/release
   - Authorized domains if using custom domains

## **ANDROID DEVELOPMENT SETUP**

### **Required Tools**
- **Android Studio**: Arctic Fox or later
- **JDK**: Version 11 or later
- **Android SDK**: API 26 (minimum) to API 35 (target)
- **Google Play Services**: Latest version on test device/emulator

### **Gradle Configuration**
The project uses Gradle Version Catalogs in `gradle/libs.versions.toml`:

```toml
[versions]
kotlin = "2.0.0"
compose = "1.7.5"
hilt = "2.48"
firebase = "32.7.0"

[libraries]
# Core dependencies defined in version catalog
# See gradle/libs.versions.toml for complete list
```

### **Build Variants**
```kotlin
buildTypes {
    debug {
        isDebuggable = true
        applicationIdSuffix = ".debug"
        // Uses debug google-services.json
    }
    release {
        isMinifyEnabled = true
        proguardFiles("proguard-android-optimize.txt", "proguard-rules.pro")
        // Uses release google-services.json
    }
}
```

## **ENVIRONMENT-SPECIFIC CONFIGURATION**

### **Development Environment**
```properties
# local.properties for development
MAPS_API_KEY=AIza...debug_key
FIREBASE_DATABASE_URL=https://taxi-app-dev-default-rtdb.firebaseio.com/
```

### **Production Environment** 
```properties  
# local.properties for production
MAPS_API_KEY=AIza...production_key
FIREBASE_DATABASE_URL=https://taxi-app-prod-default-rtdb.firebaseio.com/
```

### **Testing Environment**
```properties
# local.properties for testing
MAPS_API_KEY=AIza...test_key
FIREBASE_DATABASE_URL=https://taxi-app-test-default-rtdb.firebaseio.com/
```

## **SECURITY CONFIGURATION**

### **API Key Security**
- Use different API keys for development/production
- Implement proper API key restrictions
- Rotate keys regularly
- Monitor API usage in Google Cloud Console

### **Firebase Security**
- Implement proper database rules
- Use authentication for all write operations
- Enable App Check for additional security
- Monitor usage in Firebase Console

### **App Signing**
```kotlin
// keystore.properties (not in version control)
storePassword=your_keystore_password
keyPassword=your_key_password
keyAlias=your_key_alias
storeFile=path/to/your/keystore.jks
```

## **TESTING CONFIGURATION**

### **Unit Testing Setup**
- **Robolectric**: For Android framework testing
- **MockK**: For Kotlin-friendly mocking
- **Turbine**: For testing Flows
- **Truth**: For fluent assertions

### **UI Testing Setup**
- **Espresso**: For UI interactions
- **Compose Testing**: For Compose UI testing
- **Hilt Testing**: For dependency injection in tests
- **Firebase Emulator**: For integration testing

### **Test Configuration**
```kotlin
testOptions {
    unitTests {
        isIncludeAndroidResources = true
        
        all {
            // Memory and performance optimizations
            it.maxHeapSize = "2g"
            it.jvmArgs("-XX:+EnableDynamicAgentLoading")
        }
    }
}
```

## **PERFORMANCE CONFIGURATION**

### **Memory Optimization**
```kotlin
android {
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES" 
            // Additional exclusions for smaller APK
        }
    }
}
```

### **Network Configuration**
```xml
<!-- network_security_config.xml -->
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">firebaseio.com</domain>
        <domain includeSubdomains="true">googleapis.com</domain>
    </domain-config>
</network-security-config>
```

## **MONITORING CONFIGURATION**

### **Firebase Analytics**
```kotlin
// Enable in Application class
FirebaseAnalytics.getInstance(this).apply {
    setAnalyticsCollectionEnabled(true)
    setUserId(currentUserId)
}
```

### **Crashlytics Configuration**
```kotlin
// Enable in Application class  
FirebaseCrashlytics.getInstance().apply {
    setCrashlyticsCollectionEnabled(true)
    setUserId(currentUserId)
}
```

### **Performance Monitoring**
```kotlin
// Enable in Application class
FirebasePerformance.getInstance().apply {
    isPerformanceCollectionEnabled = true
}
```

## **TROUBLESHOOTING COMMON ISSUES**

### **Firebase Connection Issues**
- Verify google-services.json is in correct location
- Check Firebase Database URL in local.properties
- Ensure Firebase project has correct package name
- Verify internet connectivity and firewall settings

### **Maps Not Loading**
- Verify MAPS_API_KEY in local.properties
- Check API key restrictions in Google Cloud Console
- Ensure Maps SDK for Android is enabled
- Check device has Google Play Services

### **Authentication Failures**
- Verify SHA-1 fingerprints in Firebase Console
- Check OAuth 2.0 client configuration
- Ensure Google Sign-In is enabled in Firebase Auth
- Verify app package name matches Firebase configuration

### **Build Issues**
- Check Gradle sync
- Verify all required dependencies are included
- Clear build cache: `./gradlew clean`
- Check Kotlin/Compose compatibility versions