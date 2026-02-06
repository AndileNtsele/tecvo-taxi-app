# Background Location Justification for Google Play Store

## App Name
TAXI by TECVO

## Permission Requested
`ACCESS_BACKGROUND_LOCATION`

## Core Functionality Requirement

### Why Background Location is Essential

**Primary Use Case**: Users wait at taxi stops/bus ranks while monitoring for available taxis and being visible to drivers.

**Real-World Scenario**:
1. Passenger opens TAXI app at taxi stop, selects "Town" destination
2. Passenger appears on map to drivers heading to town
3. Passenger receives call/WhatsApp message and switches apps
4. **Without background location**: Passenger disappears from map, drivers can't see them
5. **With background location**: Passenger remains visible while using other apps

### This is NOT Optional Enhancement
- Users naturally switch apps while waiting (calls, messages, social media)
- Waiting times can be 5-30 minutes at busy ranks
- Real-time visibility is the app's entire purpose
- Service is useless if users disappear when answering calls

## User Benefit

- **Drivers**: See all waiting passengers, even those temporarily using other apps
- **Passengers**: Remain discoverable while multitasking during wait times
- **Both**: Enables natural phone usage without breaking visibility service

## Transparency & Control

### User Notification
- Persistent notification shown: "Taxi - Active"
- Clear text: "Drivers/Passengers can see your location"
- Cannot be dismissed while service active

### User Control
- Background tracking ONLY when map screen is active
- Stops immediately when closing map or exiting app
- No tracking when app fully closed
- User initiates service by opening map

### Data Handling
- Location deleted immediately when map closed
- No location history stored
- No background tracking when app not in use
- Temporary service data only

## Privacy Policy Compliance

Full disclosure in privacy policy:
- Explains background location usage
- Clear about when tracking starts/stops
- Describes notification requirements
- Details data retention (none)

## Alternative Considered

**Foreground-only tracking**: Rejected because users would disappear from map when switching apps, defeating the app's core purpose of providing real-time visibility.

## Summary

Background location enables core taxi visibility service while users naturally use their phones. Service stops when map closes. Full transparency via persistent notification and privacy policy.