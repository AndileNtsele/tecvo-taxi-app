package com.example.taxi.screens.mapscreens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.example.taxi.services.LocationService
import com.example.taxi.services.NotificationService
import com.example.taxi.utils.ConnectivityManager
import timber.log.Timber
import javax.inject.Inject

private const val TAG = "MapScreenNotification"

class MapScreenNotificationManager @Inject constructor(
    private val connectivityManager: ConnectivityManager,
    private val notificationService: NotificationService,
    private val locationService: LocationService
) {
    val showOfflineMessage = connectivityManager.showOfflineMessage

    fun setupNotificationTracking(
        screenName: String,
        userId: String,
        userType: String,
        destination: String,
        isNotificationEnabled: Boolean // ADD THIS parameter
    ) {
        // Track screen for notifications (this is just tracking, not actual notifications)
        notificationService.enterMapScreen(screenName)

        // ONLY start proximity monitoring if notifications are enabled
        if (isNotificationEnabled) {
            // Start actual proximity monitoring
            locationService.currentLocation.value?.let { location ->
                notificationService.startMonitoring(
                    userId = userId,
                    location = location,
                    destination = destination,
                    userType = userType
                )
                Timber.tag(TAG).d("Started proximity monitoring for $userType $userId at $destination")
            } ?: run {
                // If no current location, try to get last location first
                locationService.getLastLocation(
                    onSuccess = { location ->
                        notificationService.startMonitoring(
                            userId = userId,
                            location = location,
                            destination = destination,
                            userType = userType
                        )
                        Timber.tag(TAG).d("Started proximity monitoring with last known location for $userType $userId")
                    },
                    onFailure = { error ->
                        Timber.tag(TAG).w("Could not get location to start monitoring: ${error.message}")
                    }
                )
            }
        } else {
            Timber.tag(TAG).d("Notifications disabled - skipping proximity monitoring setup")
            // Make sure monitoring is stopped if it was previously running
            notificationService.stopMonitoring()
        }

        Timber.tag(TAG).d("Set up notification tracking for $screenName (enabled: $isNotificationEnabled)")
    }

    fun updateNotificationState(
        userId: String,
        userType: String,
        destination: String,
        isEnabled: Boolean
    ) {
        if (isEnabled) {
            // Start monitoring if not already started
            locationService.currentLocation.value?.let { location ->
                notificationService.startMonitoring(
                    userId = userId,
                    location = location,
                    destination = destination,
                    userType = userType
                )
                Timber.tag(TAG).d("Enabled notifications - started monitoring")
            }
        } else {
            // Stop monitoring
            notificationService.stopMonitoring()
            Timber.tag(TAG).d("Disabled notifications - stopped monitoring")
        }
    }

    fun cleanupNotificationTracking(screenName: String) {
        notificationService.exitMapScreen(screenName)
        notificationService.stopMonitoring()
        Timber.tag(TAG).d("Cleaned up notification tracking for $screenName")
    }

    fun cleanup() {
        connectivityManager.cleanup()
        notificationService.stopMonitoring()
    }
}

@Composable
fun rememberMapScreenNotificationManager(
    connectivityManager: ConnectivityManager,
    notificationService: NotificationService,
    locationService: LocationService
): MapScreenNotificationManager {
    return remember {
        MapScreenNotificationManager(connectivityManager, notificationService, locationService)
    }
}

@Composable
fun NotificationManagerEffect(
    notificationManager: MapScreenNotificationManager,
    screenName: String,
    userId: String,
    userType: String,
    destination: String,
    isNotificationEnabled: Boolean // ADD THIS parameter
) {
    DisposableEffect(screenName, userType, destination, userId, isNotificationEnabled) {
        notificationManager.setupNotificationTracking(
            screenName = screenName,
            userId = userId,
            userType = userType,
            destination = destination,
            isNotificationEnabled = isNotificationEnabled // PASS the enabled state
        )
        onDispose {
            notificationManager.cleanupNotificationTracking(screenName)
        }
    }
}