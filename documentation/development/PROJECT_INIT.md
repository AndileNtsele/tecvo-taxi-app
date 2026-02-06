# TAXI - Real-Time Driver-Passenger Matching App

## Core Purpose
**Real-time taxi matching app where drivers and passengers going to the same destination can find each other instantly on a live map.**

## How It Works
1. **Role & Destination Selection**: User selects role (driver/passenger) and destination (town/local)
2. **Map Entry**: Enters map screen → gets written to Firebase at `drivers/local/{userId}` or `passengers/local/{userId}`
3. **Real-Time Discovery**: Map displays all users with same destination in real-time
4. **Coordination**: Driver sees passengers, passenger sees drivers - they can coordinate pickup
5. **Exit Cleanup**: User leaves map → gets removed from Firebase (no longer available)

## Critical Requirements

### Real-Time Accuracy is EVERYTHING
- **If someone appears on the map, they MUST actually be there and available**
- **False positives (showing unavailable users) kill trust and functionality**
- Users must disappear from Firebase the moment they're no longer available
- Users must appear on Firebase the moment they become available

### The Map is the Heartbeat
- **The map is where the magic happens** - it's the core of the entire application
- Everything else (login, role selection, settings) is just setup for the core map experience
- Map functionality takes precedence over all other features
- Real-time presence on the map determines the app's success or failure

## Firebase Structure
```
/drivers
  /local
    /{userId}: { latitude, longitude, timestamp, ... }
  /town  
    /{userId}: { latitude, longitude, timestamp, ... }
/passengers
  /local
    /{userId}: { latitude, longitude, timestamp, ... }
  /town
    /{userId}: { latitude, longitude, timestamp, ... }
```

## Key Principles for Development

### 1. Real-Time Data Integrity
- Firebase cleanup ONLY when user truly leaves map (not screen recreation)
- Maintain presence during app backgrounding from map
- Remove presence immediately on navigation away from map
- No phantom users, no missing users

### 2. Map-Centric Architecture
- All major decisions should be evaluated against map functionality
- Performance optimizations should prioritize map rendering and entity discovery
- Error handling should ensure map remains functional
- Navigation should preserve map state when possible

### 3. User Trust Through Accuracy
- Location updates must be reliable and current
- Entity monitoring must be stable and consistent
- Firebase presence must accurately reflect actual availability
- System should fail safely (prefer not showing user vs showing unavailable user)

## Technical Implementation Notes

### Firebase Presence Management
- Write to Firebase: On map screen entry
- Maintain in Firebase: During screen recreation, app backgrounding from map
- Remove from Firebase: On navigation away from map, app termination

### Entity Monitoring
- Monitor both same-type (passengers see passengers) and opposite-type (passengers see drivers)
- Implement proper lifecycle management to prevent overlapping monitoring sessions
- Handle network connectivity changes gracefully
- Cache and throttle updates for performance

### Map Screen Lifecycle
- Entry: Initialize Firebase presence, start entity monitoring
- Active: Maintain real-time updates, handle location changes
- Background: Preserve presence, reduce update frequency
- Exit: Clean Firebase presence, stop monitoring

## Success Metrics
- **Discovery Rate**: % of users who find matches when both are available
- **False Positive Rate**: % of shown users who are actually unavailable (target: 0%)
- **Real-Time Latency**: Time between user availability change and map update
- **User Trust**: Measured by session duration and return usage

## Development Guidelines
1. **Always test with multiple users**: Real-world scenarios require multiple devices/accounts
2. **Prioritize map functionality**: When in doubt, choose the solution that best serves map accuracy
3. **Monitor Firebase data**: Regularly verify data integrity in Firebase console
4. **Test lifecycle scenarios**: App backgrounding, screen rotation, navigation flows
5. **Validate real-time updates**: Ensure changes propagate within acceptable timeframes

---

*This document should be referenced for all architectural decisions, feature implementations, and bug fixes to ensure alignment with the core purpose of real-time taxi matching.*