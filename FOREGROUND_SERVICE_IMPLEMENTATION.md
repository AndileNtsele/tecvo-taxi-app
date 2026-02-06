# Foreground Service Implementation Summary

## What Was Done

Implemented persistent foreground service for continuous background location tracking for BOTH passengers and drivers. This allows users to remain visible on the map and receive notifications even when the app is backgrounded (e.g., when using WhatsApp).

## Files Created

### 1. LocationForegroundService.kt
**Path:** `app/src/main/java/com/tecvo/taxi/services/LocationForegroundService.kt`

Foreground service that:
- Shows persistent notification when active
- Keeps LocationService running in background
- Updates Firebase with user location continuously
- Works for both passengers and drivers

### 2. LocationForegroundServiceManager.kt  
**Path:** `app/src/main/java/com/tecvo/taxi/services/LocationForegroundServiceManager.kt`

Manager class that:
- Centralized control for starting/stopping the service
- Prevents redundant service starts
- Handles parameter changes (userType, destination)

## Files Modified

### 1. AndroidManifest.xml
**Added:**
- `ACCESS_BACKGROUND_LOCATION` permission
- `FOREGROUND_SERVICE` permission  
- `FOREGROUND_SERVICE_LOCATION` permission
- Service declaration for LocationForegroundService

### 2. MapScreenLocationTracking.kt
**Updated:**
- Integrated LocationForegroundServiceManager
- Modified `startLocationUpdates()` to start foreground service
- Modified `stopLocationUpdates()` to stop foreground service
- Updated `LocationTrackerEffect()` to pass user info (userId, userType, destination)
- Updated `rememberMapScreenLocationTracker()` to inject service manager

## How It Works

### Workflow:
1. User opens map screen (passenger or driver)
2. `LocationTrackerEffect` detects user info
3. Calls `locationTracker.startLocationUpdates(userId, userType, destination)`
4. Service manager starts `LocationForegroundService`
5. Foreground service shows persistent notification
6. LocationService continues tracking even when app is backgrounded
7. Firebase receives continuous location updates
8. User remains visible to drivers/passengers
9. Notifications about nearby entities still work

### When User Backgrounds App:
- Notification appears: "Taxi - Active"
- Text: "Drivers can see your location" (for passengers)
- Text: "Passengers can see your location" (for drivers)
- Location tracking continues
- Firebase updates continue
- User stays on map for others to see

### When User Closes Map:
- `LocationTrackerEffect` onDispose triggers
- Stops foreground service
- Removes persistent notification
- Cleans up location updates

## Still Needs Implementation

**CRITICAL:** MapScreen.kt needs updating to pass user info to LocationTrackerEffect.

Currently line 250 in MapScreen.kt calls:
```kotlin
LocationTrackerEffect(locationTracker, screenName)
```

Needs to be updated to:
```kotlin
LocationTrackerEffect(
    locationTracker = locationTracker,
    screenName = screenName,
    userId = userId ?:"",
    userType = userType,
    destination = destination
)
```

## Testing Checklist

- [ ] Passenger starts map → notification appears
- [ ] Passenger switches to WhatsApp → still visible on map
- [ ] Driver starts map → notification appears  
- [ ] Driver switches to WhatsApp → still visible on map
- [ ] Passenger closes map → notification disappears
- [ ] Driver closes map → notification disappears
- [ ] Notifications about nearby entities work in background
- [ ] Firebase location updates continue in background
- [ ] Battery usage is reasonable

## User Experience

**Before:** Users disappeared from map when backgrounding app

**After:** Users remain visible continuously with clear notification showing tracking is active

**Notification:** Cannot be dismissed by user (by design, required for location tracking)
