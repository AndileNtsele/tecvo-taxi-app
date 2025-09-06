package com.example.taxi.screens.mapscreens
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlinx.coroutines.delay
import com.example.taxi.services.LocationService
import com.example.taxi.services.MapsInitializationManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import timber.log.Timber

private const val TAG = "MapScreenRendering"

/**
 * Safely tests if CameraUpdateFactory is initialized
 */
private fun isCameraUpdateFactoryReady(): Boolean {
    return try {
        // Test CameraUpdateFactory availability with a simple operation
        CameraUpdateFactory.newLatLng(LatLng(0.0, 0.0))
        true
    } catch (e: Exception) {
        Timber.tag(TAG).d("CameraUpdateFactory not ready: ${e.message}")
        false
    }
}

/**
 * Marker that blinks to show user's location.
 */
@Composable
fun FlashingUserMarker(
    location: LatLng,
    iconBitmapDescriptor: BitmapDescriptor,
    markerTitle: String = "Me",
    animationDurationMs: Int = 500
) {
// Create infinite transition for pulsing effect
    val infiniteTransition = rememberInfiniteTransition(label = "markerPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDurationMs),
            repeatMode = RepeatMode.Reverse
        ),
        label = "markerAlpha"
    )
// Create the actual map marker with animation
    Marker(
        state = MarkerState(position = location),
        icon = iconBitmapDescriptor,
        title = markerTitle,
        snippet = "I am here!",
        alpha = alpha
    )
}
/**
 * Camera management with debounced updates and robust initialization
 */
@Composable
fun MapCameraEffect(
    currentLocation: LatLng?,
    cameraPositionState: CameraPositionState,
    mapsInitManager: MapsInitializationManager,
    defaultZoom: Float = 15.0f
) {
    val lastCameraUpdate = remember { mutableLongStateOf(0) }
    val cameraUpdateThreshold = 2000L // Increased to 2 seconds to reduce updates
    
    LaunchedEffect(currentLocation) {
        currentLocation?.let { location ->
            try {
                // Check if coroutine is still active before proceeding
                if (!isActive) return@LaunchedEffect
                
                val currentTime = System.currentTimeMillis()
                
                // Performance optimization: check conditions before expensive operations
                if (currentTime - lastCameraUpdate.longValue <= cameraUpdateThreshold || 
                    cameraPositionState.isMoving) {
                    return@LaunchedEffect
                }
                
                // Critical Fix: Use robust initialization manager
                if (!mapsInitManager.isInitialized()) {
                    val initSuccess = mapsInitManager.ensureInitialized()
                    if (!initSuccess) {
                        Timber.tag(TAG).w("Maps initialization failed, skipping camera update")
                        Timber.tag(TAG).d("Initialization stats: ${mapsInitManager.getInitializationStats()}")
                        return@LaunchedEffect
                    }
                    Timber.tag(TAG).i("Maps initialization completed successfully")
                }
                
                // Move expensive operations to background thread
                withContext(Dispatchers.Default) {
                    if (!isActive) return@withContext
                    
                    val currentTarget = cameraPositionState.position.target
                    val locationService = LocationService.getInstance()
                    val distance = locationService.calculateDistance(currentTarget, location)
                    
                    // Only proceed if significant movement (more than 20 meters for better performance)
                    if (distance > 0.02) { // 20 meters in km
                        // Yield to prevent blocking
                        yield()
                        
                        if (!isActive) return@withContext
                        
                        // Safe camera update with proper error handling
                        val cameraUpdate = try {
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.fromLatLngZoom(location, defaultZoom)
                            )
                        } catch (e: Exception) {
                            // Reset initialization state if error occurs
                            mapsInitManager.resetInitialization()
                            Timber.tag(TAG).w(e, "CameraUpdateFactory error, resetting initialization state")
                            return@withContext
                        }
                        
                        // Switch back to Main thread for UI update
                        withContext(Dispatchers.Main) {
                            if (!isActive) return@withContext
                            
                            try {
                                cameraPositionState.animate(
                                    cameraUpdate,
                                    300 // Shorter animation for better performance
                                )
                                lastCameraUpdate.longValue = currentTime
                                Timber.tag(TAG).d("Camera updated to: ${location.latitude}, ${location.longitude}")
                            } catch (e: Exception) {
                                Timber.tag(TAG).e(e, "Error during camera animation: ${e.message}")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error in camera effect: ${e.message}")
            }
        }
    }
}
/**
 * Creates map properties for the Google Map
 */
@Composable
fun createMapProperties(
    mapType: MapType
): MapProperties {
    return MapProperties(
        mapType = mapType,
        isMyLocationEnabled = false
    )
}
/**
 * Creates map UI settings for the Google Map
 */
@Composable
fun createMapUiSettings(): MapUiSettings {
    return MapUiSettings(
        zoomControlsEnabled = true,
        zoomGesturesEnabled = true,
        scrollGesturesEnabled = true,
        compassEnabled = false,
        myLocationButtonEnabled = false
    )
}