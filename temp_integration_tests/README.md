# 🚖 TAXI App Integration Tests - Real-Time Accuracy Suite

This directory contains comprehensive integration tests that validate the **critical real-time functionality** that defines this app's success. These tests ensure the core mission from CLAUDE.md is fulfilled:

> **"If someone appears on the map, they MUST actually be there and available"**

## 🎯 Why These Tests Are Critical

The TAXI app's success depends entirely on **real-time accuracy**. False positives (showing unavailable users) kill trust and functionality. These integration tests validate that:

- Users appear in Firebase within **2 seconds** of entering the map
- Users disappear from Firebase within **5 seconds** of leaving  
- Real-time discovery works under **all stress conditions**
- The app maintains accuracy during **network issues, navigation stress, and complex user flows**

## 📁 Test Suite Structure

### 🔧 `FirebaseIntegrationTestBase.kt`
**Base infrastructure for real Firebase integration testing**
- Connects to Firebase emulator for safe testing
- Provides session-based test isolation  
- Handles cleanup to prevent test interference
- **Requirement**: Firebase emulator running on port 9000

### 🔄 `FirebasePresenceLifecycleIntegrationTest.kt`  
**Tests the CRITICAL presence lifecycle requirements**
- ✅ User appears in Firebase within 2 seconds of entering map
- ✅ User disappears from Firebase within 5 seconds of leaving map
- ✅ User persists during app backgrounding from map
- ✅ Screen rotation doesn't affect presence
- ✅ Rapid navigation maintains accurate state

**Key Tests:**
- `user appears in Firebase within 2 seconds of entering map screen`
- `user disappears from Firebase within 5 seconds of leaving map screen`
- `rapid navigation between screens maintains accurate Firebase state`

### 🔍 `RealTimeUserDiscoveryIntegrationTest.kt`
**Tests the core app functionality - real-time user discovery**
- ✅ Driver discovers passenger within 5 seconds
- ✅ Passenger discovers driver within 5 seconds
- ✅ Strategic intelligence: drivers see competing drivers
- ✅ Strategic intelligence: passengers see other passengers for demand clustering
- ✅ Multi-user scale testing

**Key Tests:**
- `driver discovers passenger within 5 seconds of both being on map`
- `driver discovers competing drivers for strategic intelligence`
- `multiple users discover each other in real-time`

### 🚁 `NavigationStressIntegrationTest.kt`
**Tests real-time accuracy under navigation pressure**
- ✅ Rapid navigation between screens
- ✅ Multiple quick destination changes
- ✅ Screen rotation during navigation stress
- ✅ Back button behavior stress
- ✅ Memory pressure scenarios

**Key Tests:**
- `rapid navigation between screens maintains Firebase accuracy`
- `multiple quick destination changes maintain Firebase accuracy`
- `screen rotation during navigation maintains presence accurately`

### 🌐 `NetworkResilienceIntegrationTest.kt`
**Tests real-time accuracy under network stress**
- ✅ WiFi to mobile data switching
- ✅ Airplane mode cycles
- ✅ Poor connection scenarios  
- ✅ Firebase reconnection handling
- ✅ Network loss during critical operations

**Key Tests:**
- `wifi to mobile data switching maintains Firebase presence`
- `airplane mode cycle maintains Firebase accuracy after reconnection`
- `firebase reconnection maintains real-time accuracy`

### 🚀 `EndToEndUserFlowIntegrationTest.kt`
**Tests complete user journeys and real-world scenarios**
- ✅ Complete driver discovers passenger flow
- ✅ Strategic intelligence with multiple users
- ✅ South African taxi route simulation
- ✅ Full app lifecycle with backgrounding

**Key Tests:**
- `complete driver discovers passenger end to end flow`
- `strategic intelligence end to end flow with multiple users`  
- `south african taxi route end to end scenario`

## 🏃‍♂️ Running the Integration Tests

### Prerequisites
1. **Firebase Emulator** (required for safe testing):
   ```bash
   # Install Firebase CLI if needed
   npm install -g firebase-tools
   
   # Start Firebase emulator (required before running tests)
   firebase emulators:start --only database
   ```
   
   The emulator should be running on `127.0.0.1:9000`

2. **Test Configuration** in `local.properties`:
   ```properties
   FIREBASE_DATABASE_URL=your_firebase_url_here
   MAPS_API_KEY=your_maps_key_here
   ```

### Running Tests

**Run all integration tests:**
```bash
./gradlew testDebugUnitTest --tests "com.example.taxi.integration.*"
```

**Run specific test suites:**
```bash
# Firebase presence lifecycle tests
./gradlew testDebugUnitTest --tests "com.example.taxi.integration.FirebasePresenceLifecycleIntegrationTest"

# Real-time discovery tests  
./gradlew testDebugUnitTest --tests "com.example.taxi.integration.RealTimeUserDiscoveryIntegrationTest"

# Navigation stress tests
./gradlew testDebugUnitTest --tests "com.example.taxi.integration.NavigationStressIntegrationTest"

# Network resilience tests
./gradlew testDebugUnitTest --tests "com.example.taxi.integration.NetworkResilienceIntegrationTest"

# End-to-end flow tests
./gradlew testDebugUnitTest --tests "com.example.taxi.integration.EndToEndUserFlowIntegrationTest"
```

**Run with verbose output:**
```bash
./gradlew testDebugUnitTest --tests "com.example.taxi.integration.*" --info --stacktrace
```

## 📊 What These Tests Validate

### ✅ **Real-Time Accuracy Requirements**
- **2-second rule**: Users appear in Firebase within 2 seconds
- **5-second rule**: Users disappear from Firebase within 5 seconds  
- **No false positives**: Only actually available users appear on maps
- **Mutual discovery**: Users can see each other within 5 seconds

### ✅ **Strategic Intelligence Features**  
- **Driver intelligence**: See competing drivers to avoid oversupply
- **Passenger intelligence**: See other passengers for demand clustering
- **Market transparency**: Both sides have real-time supply/demand data

### ✅ **South African Taxi Context**
- **Town runs**: Routes to city centers/main hubs
- **Local runs**: Routes within local areas/townships
- **Real-world scenarios**: Township to CBD, taxi rank positioning

### ✅ **Stress Resilience**
- **Navigation stress**: Rapid screen changes don't break accuracy
- **Network stress**: WiFi switching, airplane mode, poor connections
- **Scale stress**: Multiple users discovering each other efficiently
- **Memory stress**: System works under resource constraints

## 🚨 Critical Debug Commands

When tests fail, use these commands to investigate:

```bash
# Monitor Firebase connections during tests
adb logcat | grep "Firebase"

# Track entity monitoring lifecycle
adb logcat | grep "MapViewModel"  

# Watch location updates
adb logcat | grep "LocationService"

# Monitor cleanup operations
adb logcat | grep "cleanup"
```

## 📈 Success Metrics

### **Performance Requirements Met:**
- ✅ User discovery: < 5 seconds
- ✅ Firebase write: < 2 seconds  
- ✅ Firebase cleanup: < 5 seconds
- ✅ Network recovery: < 3 seconds after reconnection

### **Reliability Requirements Met:**
- ✅ Zero false positives under stress
- ✅ 100% cleanup after user leaves
- ✅ Recovery from all network scenarios
- ✅ Stability through navigation stress

### **Strategic Intelligence Validated:**
- ✅ Cross-role visibility (drivers see passengers, passengers see drivers)
- ✅ Same-role strategic intelligence (optional competitive visibility)
- ✅ Real-time market transparency for positioning decisions

## 🎯 Test Philosophy

These integration tests follow the **"Trust but Verify"** principle:

1. **Trust** the unit tests for individual component logic
2. **Verify** the complete system delivers the core value proposition through integration testing
3. **Validate** that real-time accuracy is maintained under all stress conditions

The core app mission is simple but critical: **"Real-time taxi matching where users can find each other instantly."** These tests ensure that mission is fulfilled reliably.

## 🔄 Continuous Integration

These tests should be run:
- ✅ Before any release
- ✅ After any changes to Firebase code
- ✅ After any changes to LocationService 
- ✅ After any changes to MapViewModel
- ✅ Weekly for regression testing

**Remember: The map is the heartbeat. These tests ensure that heartbeat is strong and reliable.** 🚖💓