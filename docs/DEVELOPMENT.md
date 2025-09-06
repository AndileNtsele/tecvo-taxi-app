# 🚀 DEVELOPMENT GUIDE

## **BUILD AND DEVELOPMENT COMMANDS**

### **Building the Project**
```bash
# Clean and build debug APK
./gradlew clean assembleDebug

# Build release APK
./gradlew assembleRelease

# Check for any Proguard/R8 issues in release builds
./gradlew assembleRelease
```

### **Testing Commands**
```bash
# Run all unit tests (175+ tests)
./gradlew testDebugUnitTest

# Run specific test class
./gradlew testDebugUnitTest --tests "com.example.taxi.viewmodel.MapViewModelTest"

# Run all UI tests (140+ tests)
./gradlew connectedAndroidTest

# Run specific UI test class
./gradlew connectedAndroidTest --tests "*MapScreenUITest*"
```

### **Development Workflows**
```bash
# Development cycle
./gradlew clean assembleDebug    # Clean build
./gradlew testDebugUnitTest      # Verify unit tests
./gradlew connectedAndroidTest   # Verify UI tests (if device available)
```

## **🎯 TESTING FOR REAL-TIME ACCURACY**

### **Critical Test Scenarios**
All changes MUST be tested with these real-world scenarios:

1. **Multi-User Discovery**: 
   - 2+ devices with same destination
   - Verify users appear on each other's maps within 5 seconds
   - Test both driver-passenger and same-role discovery

2. **Presence Lifecycle**:
   - User enters map → appears in Firebase within 2 seconds
   - User leaves map → disappears from Firebase within 5 seconds
   - User backgrounds app from map → stays in Firebase
   - App termination → user removed from Firebase

3. **Navigation Stress Tests**:
   - Rapid navigation between screens
   - Screen rotation while on map
   - Back button behavior
   - Multiple quick destination changes

4. **Network Resilience**:
   - WiFi to mobile data switching
   - Airplane mode cycles
   - Poor connection scenarios
   - Firebase reconnection handling

### **🚨 CRITICAL DEBUGGING COMMANDS**
When investigating real-time issues:
```bash
# Monitor Firebase connections
adb logcat | grep "Firebase"

# Track entity monitoring lifecycle  
adb logcat | grep "MapViewModel"

# Watch location updates
adb logcat | grep "LocationService"

# Monitor cleanup operations
adb logcat | grep "cleanup"
```

## **DEVELOPMENT PATTERNS**

### **Map-Centric Development Approach**
Every code change must be evaluated against map accuracy:

1. **Does this improve real-time user presence accuracy?**
2. **Does this maintain Firebase presence consistency?**
3. **Does this enhance the core taxi matching functionality?**

### **Firebase Integration Patterns**
- **Write on entry**: User enters map → Firebase presence written
- **Maintain during lifecycle**: App backgrounding, screen recreation
- **Clean on true exit**: User leaves map or app terminates
- **Never cleanup on ViewModel disposal** - breaks real-time accuracy

### **Error Handling Patterns**
- Map functionality takes precedence in error recovery
- Graceful degradation when external services fail
- User feedback for network issues
- Automatic retry mechanisms for Firebase operations

## **PERFORMANCE GUIDELINES**

### **Memory Management**
- Use lazy initialization for services
- Properly dispose of Firebase listeners
- Monitor for memory leaks with LeakCanary
- Optimize Compose recomposition

### **Network Optimization**
- Efficient Firebase queries with proper indexing
- Location update throttling
- Background/foreground operation separation
- Connection state awareness

### **Battery Optimization**
- Location accuracy vs battery trade-offs
- Background operation minimization
- Proper service lifecycle management
- Doze mode considerations

## **CODE QUALITY**

### **Testing Requirements**
- **Unit test coverage**: Maintain 175+ tests
- **UI test coverage**: Maintain 140+ tests  
- **Integration testing**: Firebase real-time functionality
- **Performance testing**: Extended usage scenarios

### **Code Review Checklist**
- [ ] Real-time accuracy maintained?
- [ ] Firebase presence properly managed?
- [ ] Unit tests updated/added?
- [ ] UI tests cover new functionality?
- [ ] Performance impact assessed?
- [ ] Error handling implemented?

### **Lint and Quality Checks**
```bash
# Clean build to catch compilation issues
./gradlew clean assembleDebug

# Verify test coverage
./gradlew testDebugUnitTest
./gradlew connectedAndroidTest
```

## **DEPLOYMENT**

### **Release Preparation**
1. Update version code/name in `build.gradle.kts`
2. Run full test suite
3. Generate release APK
4. Test on multiple devices
5. Verify Firebase production configuration

### **Monitoring**
- Firebase Crashlytics for crash reporting
- Firebase Analytics for user behavior
- Firebase Performance for app performance
- Custom logging for real-time accuracy metrics

## **TROUBLESHOOTING**

### **Common Development Issues**
- **Firebase connection failures**: Check google-services.json
- **Location permission issues**: Verify manifest and runtime permissions
- **Map loading problems**: Check Maps API key configuration
- **Test failures**: Ensure emulator/device has Google Play Services

### **Real-Time Debugging**
- Use Firebase Console to monitor user presence
- Check network connectivity during testing
- Monitor location services status
- Verify proper cleanup operations