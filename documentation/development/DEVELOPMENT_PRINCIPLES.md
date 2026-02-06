# TAXI App - Development Principles

## Core Mission Statement
**Enable real-time discovery between drivers and passengers heading to the same destination through accurate, live map presence.**

## The Fundamental Rule
> **MAP ACCURACY OVER EVERYTHING**
> 
> Every code change, every feature, every optimization must be evaluated against one question:
> "Does this improve or maintain the accuracy of real-time user presence on the map?"

## Key Development Principles

### 1. Real-Time Data Sanctity
- **Firebase presence = ground truth of availability**
- Write to Firebase ONLY when user is genuinely available on map
- Remove from Firebase IMMEDIATELY when user becomes unavailable
- Never show stale or phantom users - better to show nothing than wrong information

### 2. Map-First Decision Making
When facing technical decisions, prioritize in this order:
1. **Map functionality and accuracy**
2. Real-time data consistency
3. User experience on map
4. Performance of map operations
5. All other features

### 3. Lifecycle Precision
- **Map Entry**: User becomes available → Write to Firebase
- **Map Active**: User stays available → Maintain Firebase presence
- **Map Background**: User temporarily unavailable → Maintain presence (will return)
- **Map Exit**: User no longer available → Remove from Firebase
- **App Termination**: User unavailable → Clean removal from Firebase

### 4. Error Handling Philosophy
- **Fail Safe**: When in doubt, remove user from Firebase rather than show incorrect data
- **Graceful Degradation**: Map should function even with network issues
- **User Transparency**: Show connection status, don't hide problems
- **Recovery Priority**: Restore map functionality first, other features second

## Code Implementation Standards

### Firebase Operations
```kotlin
// ✅ CORRECT: Only write when truly available
fun enterMap() {
    // User is now available for matching
    writeToFirebase()
    startEntityMonitoring()
}

// ✅ CORRECT: Only remove when truly unavailable  
fun exitMap() {
    // User no longer available for matching
    removeFromFirebase()
    stopEntityMonitoring()
}

// ❌ WRONG: Cleanup on ViewModel disposal (screen recreation)
override fun onCleared() {
    removeFromFirebase() // This removes available users!
}
```

### Entity Monitoring
```kotlin
// ✅ CORRECT: Stable monitoring with state management
private var isMonitoring = false

fun startMonitoring() {
    if (isMonitoring) return // Prevent duplicates
    isMonitoring = true
    // Set up Firebase listeners
}

// ❌ WRONG: Unstable start/stop cycles
fun startMonitoring() {
    stopMonitoring() // Causes rapid cycling
    // Set up new listeners
}
```

## Testing Requirements

### Real-World Scenarios
Every feature must be tested with:
1. **Multiple Users**: At least 2 devices/accounts simultaneously
2. **Same Destination**: Users selecting identical destinations
3. **Network Variations**: WiFi, mobile data, airplane mode
4. **App Lifecycle**: Background, foreground, screen rotation
5. **Navigation Patterns**: Rapid navigation, back button, deep linking

### Success Criteria
- **Discovery Works**: Users find each other within 5 seconds when available
- **No Phantom Users**: Shown users are actually available (0% false positive rate)
- **Clean Removal**: Users disappear from map when unavailable within 5 seconds
- **Stable Monitoring**: No rapid start/stop cycles in entity monitoring

## Common Anti-Patterns to Avoid

### ❌ Premature Firebase Cleanup
```kotlin
// Wrong: Cleanup on screen recreation
DisposableEffect(Unit) {
    onDispose { 
        cleanupFirebase() // Removes available user!
    }
}
```

### ❌ Unstable Entity Monitoring
```kotlin
// Wrong: Rapid cycling
LaunchedEffect(dependency) {
    startMonitoring() // Called on every recomposition
}
```

### ❌ Multiple Simultaneous Sessions
```kotlin
// Wrong: No state management
fun startMonitoring() {
    // No check if already monitoring
    createNewListeners() // Creates duplicates
}
```

## Architecture Alignment

### Service Responsibilities
- **LocationService**: Accurate GPS tracking and Firebase location updates
- **MapViewModel**: Entity monitoring and real-time state management  
- **NotificationService**: Background presence maintenance
- **ErrorHandlingService**: Map-focused error recovery

### Data Flow
```
User Action → Map Screen → MapViewModel → Firebase → Other Users' Maps
     ↑                                                        ↓
User sees others  ←  Entity Monitoring  ←  Firebase Updates  ←
```

## Quality Assurance

### Code Review Checklist
- [ ] Does this change affect Firebase presence management?
- [ ] Are there any new cleanup operations that could remove available users?
- [ ] Does entity monitoring remain stable?
- [ ] Are real-time updates preserved?
- [ ] Does the map remain the priority?

### Testing Checklist  
- [ ] Multiple users can discover each other
- [ ] Users disappear when they leave
- [ ] No phantom users shown
- [ ] Rapid navigation doesn't break discovery
- [ ] App backgrounding preserves presence

## Maintenance Guidelines

### Regular Monitoring
- Check Firebase console for data integrity
- Monitor logs for entity monitoring stability  
- Verify real-time update latency
- Test discovery scenarios weekly

### Performance Optimization
- Optimize for map rendering speed
- Prioritize Firebase operations
- Cache entity data efficiently
- Minimize main thread blocking

---

*These principles ensure every development decision serves the core mission of accurate, real-time taxi matching through map presence.*