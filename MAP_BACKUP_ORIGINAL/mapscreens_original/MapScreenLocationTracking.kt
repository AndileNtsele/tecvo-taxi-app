package com.example.taxi.screens.mapscreens
import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.taxi.services.LocationService
import com.example.taxi.viewmodel.MapViewModel
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber
import javax.inject.Inject

private const val TAG = "MapScreenLocation"
// Update MapScreenLocationTracking.kt
class MapScreenLocationTracker @Inject constructor(
    private val locationService: LocationService
) {
    fun requestLocationPermission(
        activity: Activity,
        permissionLauncher: ActivityResultLauncher<String>,
        onPermissionGranted: () -> Unit
    ) {
        Timber.tag(TAG).d("Requesting location permission")
        locationService.requestLocationPermission(
            activity = activity,
            permissionLauncher = permissionLauncher,
            onPermissionGranted = onPermissionGranted
        )
    }
    fun startLocationUpdates(interval: Long = 30000L) {
        locationService.startLocationUpdates(interval = interval)
        Timber.tag(TAG).d("Started location updates with interval $interval ms")
    }
    fun stopLocationUpdates() {
        locationService.stopLocationUpdates()
        Timber.tag(TAG).d("Stopped location updates")
    }
    fun getLastLocation(
        onSuccess: (LatLng) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        locationService.getLastLocation(onSuccess, onFailure)
    }
    fun calculateDistance(point1: LatLng, point2: LatLng): Double {
        return locationService.calculateDistance(point1, point2)
    }
}
@Composable
fun rememberMapScreenLocationTracker(
    errorHandler: MapScreenErrorHandler,
    locationService: LocationService
): MapScreenLocationTracker {
    return remember(errorHandler) {
        MapScreenLocationTracker(locationService)
    }
}
@Composable
fun LocationTrackerEffect(
    locationTracker: MapScreenLocationTracker,
    screenName: String,
    viewModel: MapViewModel = hiltViewModel()
) {
    DisposableEffect(locationTracker) {
        Timber.tag(TAG).d("Starting location tracking for $screenName")
        locationTracker.startLocationUpdates()
        onDispose {
            Timber.tag(TAG).d("Stopping location tracking for $screenName")
            locationTracker.stopLocationUpdates()
            viewModel.cleanup() // Add this line
        }
    }
}
