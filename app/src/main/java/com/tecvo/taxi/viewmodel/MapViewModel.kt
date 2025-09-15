package com.tecvo.taxi.viewmodel
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecvo.taxi.services.ErrorHandlingService
import com.tecvo.taxi.services.ErrorHandlingService.AppError
import com.tecvo.taxi.services.LocationService
import com.tecvo.taxi.utils.ConnectivityManager
import com.tecvo.taxi.utils.FirebaseCleanupUtil
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.maps.android.compose.MapType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.tecvo.taxi.repository.UserPreferencesRepository
import timber.log.Timber
import javax.inject.Inject

private const val TAG = "MapViewModel"
/**
 * ViewModel for the Map screen, handling all business logic and state management related to
 * displaying and interacting with the map, locations, and entities.
 *
 * Fully migrated to use Hilt for dependency injection.
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val locationService: LocationService,
    private val errorHandlingService: ErrorHandlingService,
    private val connectivityManager: ConnectivityManager,
    private val firebaseDatabase: FirebaseDatabase,
    private val sharedPreferences: SharedPreferences,
    private val memoryOptimizer: com.tecvo.taxi.utils.MemoryOptimizationManager,
    private val geocodingService: com.tecvo.taxi.services.GeocodingService,
    @ApplicationContext private val context: Context
) : ViewModel() {
    // Location states - derived from LocationService
    val currentLocation = locationService.currentLocation
    // User data
    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId.asStateFlow()
    private val _userType = MutableStateFlow("passenger") // Default type
    val userType: StateFlow<String> = _userType.asStateFlow()
    private val _currentDestination = MutableStateFlow("town") // Default destination
    val currentDestination: StateFlow<String> = _currentDestination.asStateFlow()
    // Entity locations
    private val _primaryEntityLocations = MutableStateFlow<List<LatLng>>(emptyList())
    val primaryEntityLocations: StateFlow<List<LatLng>> = _primaryEntityLocations.asStateFlow()
    private val _secondaryEntityLocations = MutableStateFlow<List<LatLng>>(emptyList())
    val secondaryEntityLocations: StateFlow<List<LatLng>> = _secondaryEntityLocations.asStateFlow()
    // Count states
    private val _nearbyPrimaryCount = MutableStateFlow(0)
    val nearbyPrimaryCount: StateFlow<Int> = _nearbyPrimaryCount.asStateFlow()
    private val _nearbySecondaryCount = MutableStateFlow(0)
    val nearbySecondaryCount: StateFlow<Int> = _nearbySecondaryCount.asStateFlow()
    // UI states
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _permissionDenied = MutableStateFlow(false)
    val permissionDenied: StateFlow<Boolean> = _permissionDenied.asStateFlow()
    private val _showSameTypeEntities = MutableStateFlow(true)
    val showSameTypeEntities: StateFlow<Boolean> = _showSameTypeEntities.asStateFlow()
    private val _mapType = MutableStateFlow(MapType.NORMAL)
    val mapType: StateFlow<MapType> = _mapType.asStateFlow()
    // Search radius state
    private val _searchRadius = MutableStateFlow(0.5f)
    val searchRadius: StateFlow<Float> = _searchRadius.asStateFlow()
    // Notification settings
    private val _notifyOppositeRole = MutableStateFlow(true)

    // Connectivity monitoring
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()
    
    // Marker click states for showing area information
    private val _selectedMarkerLocation = MutableStateFlow<LatLng?>(null)
    val selectedMarkerLocation: StateFlow<LatLng?> = _selectedMarkerLocation.asStateFlow()
    private val _selectedMarkerAreaName = MutableStateFlow<String?>(null)
    val selectedMarkerAreaName: StateFlow<String?> = _selectedMarkerAreaName.asStateFlow()
    private val _selectedMarkerType = MutableStateFlow<String?>(null)
    val selectedMarkerType: StateFlow<String?> = _selectedMarkerType.asStateFlow()
    private val _isLoadingAreaInfo = MutableStateFlow(false)
    val isLoadingAreaInfo: StateFlow<Boolean> = _isLoadingAreaInfo.asStateFlow()
    // Tracking jobs
    private var locationListenerJob: Job? = null
    private var preferenceObserverJob: Job? = null
    private var markerPopupJob: Job? = null
    
    // Performance optimization: caching and throttling
    private var lastEntityFetchTime = 0L
    private val entityFetchThrottleMs = 10000L // 10 seconds between fetches
    private var cachedPrimaryEntities: List<LatLng> = emptyList()
    private var cachedSecondaryEntities: List<LatLng> = emptyList()
    private val entityCacheTimeoutMs = 30000L // 30 second cache
    private var entityCacheTime = 0L
    // Firebase listeners to properly clean up
    private var primaryEntityListener: ValueEventListener? = null
    private var secondaryEntityListener: ValueEventListener? = null
    
    // Monitoring state to prevent overlapping sessions
    private var isMonitoring = false
    private var isStoppingMonitoring = false
    init {
// Observe notification radius changes from the preferences repository
        preferenceObserverJob = viewModelScope.launch {
            preferencesRepository.notificationRadiusFlow.collect { radius ->
                if (_searchRadius.value != radius) {
                    _searchRadius.value = radius
                    // Update nearby counts with new radius on background thread
                    launch(kotlinx.coroutines.Dispatchers.Default) {
                        updateNearbyCounts()
                    }
                }
            }
        }
// Observe notification settings for opposite role
        viewModelScope.launch {
            preferencesRepository.notifyDifferentRoleFlow.collect { enabled ->
                if (_notifyOppositeRole.value != enabled) {
                    _notifyOppositeRole.value = enabled
                }
            }
        }
// Observe current location to update nearby counts on background thread
        viewModelScope.launch {
            locationService.currentLocation.collectLatest { location ->
                if (location != null) {
                    // Move expensive calculation to background thread
                    launch(kotlinx.coroutines.Dispatchers.Default) {
                        updateNearbyCounts()
                    }
                }
            }
        }
// Monitor connectivity status on background thread
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            connectivityManager.isOnline.collect { isOnline ->
                if (isOnline && _isOffline.value) {
                    // We've come back online - restart monitoring on main dispatcher
                    launch(kotlinx.coroutines.Dispatchers.Main.immediate) {
                        _isOffline.value = false
                        startMonitoring()
                    }
                } else if (!isOnline && !_isOffline.value) {
                    // We've gone offline
                    launch(kotlinx.coroutines.Dispatchers.Main.immediate) {
                        _isOffline.value = true
                    }
                }
            }
        }
    }
    /**
     * Initialize the ViewModel with user data and start monitoring
     */
    fun initialize(userId: String, userType: String, destination: String) {
        Timber.tag(TAG)
            .i("Initializing MapViewModel with userId=$userId, userType=$userType, destination=$destination")
        _userId.value = userId
        _userType.value = userType
        _currentDestination.value = destination
// Set user info in LocationService
        locationService.setUserInfo(userId, userType, destination)
// Load saved radius and notification preferences from preferences
        viewModelScope.launch {
// Initial radius loading (reactive updates will handle subsequent changes)
            _searchRadius.value = preferencesRepository.notificationRadiusFlow.value
// Load opposite role notification setting
            val prefKey = if (userType == "driver") "notify_passengers" else "notify_drivers"
            _notifyOppositeRole.value = sharedPreferences.getBoolean(prefKey, true)
// Start monitoring entities after loading preferences
            startMonitoring()
        }
// Simulate loading for better UX
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _isLoading.value = false
        }
    }
    /**
     * Start monitoring entities based on user type and destination
     */
    private fun startMonitoring() {
        // Prevent overlapping monitoring sessions
        if (isMonitoring || isStoppingMonitoring) {
            Timber.tag(TAG).d("Entity monitoring already active or stopping - skipping duplicate start")
            return
        }
        
        Timber.tag(TAG).d("Starting entity monitoring")
        val userId = _userId.value ?: return
        val userType = _userType.value
        val destination = _currentDestination.value
// Check connectivity first
        if (!connectivityManager.isNetworkAvailable()) {
            _isOffline.value = true
            Timber.tag(TAG).d("Device is offline, not starting Firebase monitoring")
            return
        }
        
        // Set monitoring state
        isMonitoring = true
        
// Clean up any existing listeners first (but don't change monitoring state)
        cleanupListenersOnly()
// Set up primary entity listener (same type as user)
        val primaryPath = "${userType}s/$destination"
        val primaryRef = firebaseDatabase.reference.child(primaryPath)
// Apply query optimization - limit to most recent entities and order by timestamp
        val queryLimit = 50 // Reasonable number based on UI needs
        val primaryQuery = primaryRef
            .orderByChild("timestamp") // Order by timestamp to get most recent
            .startAt((System.currentTimeMillis() - 3600000).toDouble()) // Convert timestamp to Double
            .limitToFirst(queryLimit) // Limit to the most recent entries
        primaryEntityListener = primaryQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentTime = System.currentTimeMillis()
                
                // Performance optimization: throttle rapid Firebase updates
                if (currentTime - lastEntityFetchTime < entityFetchThrottleMs) {
                    Timber.tag(TAG).d("Primary entity fetch throttled")
                    return
                }
                
                val entities = mutableListOf<LatLng>()
                var processedCount = 0
                
                snapshot.children.forEach { entitySnapshot ->
                    // Skip current user
                    if (entitySnapshot.key == userId) return@forEach
                    
                    try {
                        val lat = entitySnapshot.child("latitude").getValue(Double::class.java)
                        val lng = entitySnapshot.child("longitude").getValue(Double::class.java)
                        
                        if (lat != null && lng != null && 
                            lat >= -90.0 && lat <= 90.0 && 
                            lng >= -180.0 && lng <= 180.0) {
                            entities.add(LatLng(lat, lng))
                            processedCount++
                        }
                    } catch (e: Exception) {
                        if (processedCount < 3) { // Only log first 3 errors
                            Timber.tag(TAG).e(e, "Error parsing primary entity: ${entitySnapshot.key}")
                        }
                    }
                }
                
                // Performance optimization: only update if data has actually changed
                if (entities != cachedPrimaryEntities) {
                    cachedPrimaryEntities = entities.toList()
                    _primaryEntityLocations.value = entities
                    entityCacheTime = currentTime
                    lastEntityFetchTime = currentTime
                    
                    updateNearbyCounts()
                    Timber.tag(TAG)
                        .d("Updated ${entities.size} $userType entities (limited to $queryLimit)")
                } else {
                    Timber.tag(TAG).d("Primary entities unchanged - skipping update")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Timber.tag(TAG).e(error.toException(), "Error monitoring $userType entities")
            }
        })
// Set up secondary entity listener (opposite type as user)
        val secondaryType = if (userType == "driver") "passenger" else "driver"
        val secondaryPath = "${secondaryType}s/$destination"
        val secondaryRef = firebaseDatabase.reference.child(secondaryPath)
// Apply the same optimization to secondary entities
        val secondaryQuery = secondaryRef
            .orderByChild("timestamp")
            .startAt((System.currentTimeMillis() - 3600000).toDouble()) // Convert timestamp to Double
            .limitToFirst(queryLimit)
        secondaryEntityListener = secondaryQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentTime = System.currentTimeMillis()
                
                // Performance optimization: throttle rapid Firebase updates
                if (currentTime - lastEntityFetchTime < entityFetchThrottleMs) {
                    Timber.tag(TAG).d("Secondary entity fetch throttled")
                    return
                }
                
                val entities = mutableListOf<LatLng>()
                var processedCount = 0
                
                snapshot.children.forEach { entitySnapshot ->
                    // Skip current user - fix for duplicate marker issue
                    if (entitySnapshot.key == userId) return@forEach
                    
                    try {
                        val lat = entitySnapshot.child("latitude").getValue(Double::class.java)
                        val lng = entitySnapshot.child("longitude").getValue(Double::class.java)
                        
                        if (lat != null && lng != null && 
                            lat >= -90.0 && lat <= 90.0 && 
                            lng >= -180.0 && lng <= 180.0) {
                            entities.add(LatLng(lat, lng))
                            processedCount++
                        }
                    } catch (e: Exception) {
                        if (processedCount < 3) { // Only log first 3 errors
                            Timber.tag(TAG).e(e, "Error parsing secondary entity: ${entitySnapshot.key}")
                        }
                    }
                }
                
                // Performance optimization: only update if data has actually changed
                if (entities != cachedSecondaryEntities) {
                    cachedSecondaryEntities = entities.toList()
                    _secondaryEntityLocations.value = entities
                    entityCacheTime = currentTime
                    lastEntityFetchTime = currentTime
                    
                    updateNearbyCounts()
                    Timber.tag(TAG)
                        .d("Updated ${entities.size} $secondaryType entities (limited to $queryLimit)")
                } else {
                    Timber.tag(TAG).d("Secondary entities unchanged - skipping update")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.tag(TAG).e(error.toException(), "Error monitoring $secondaryType entities")
            }
        })
        Timber.tag(TAG)
            .d("Entity monitoring started for $userType and $secondaryType with destination $destination")
    }
    /**
     * Clean up Firebase listeners only (without changing monitoring state)
     */
    private fun cleanupListenersOnly() {
        primaryEntityListener?.let {
            try {
                val primaryType = _userType.value
                val destination = _currentDestination.value
                val path = "${primaryType}s/$destination"
                firebaseDatabase.reference.child(path).removeEventListener(it)
                primaryEntityListener = null
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error removing primary entity listener during cleanup")
            }
        }
        secondaryEntityListener?.let {
            try {
                val secondaryType = if (_userType.value == "driver") "passenger" else "driver"
                val destination = _currentDestination.value
                val path = "${secondaryType}s/$destination"
                firebaseDatabase.reference.child(path).removeEventListener(it)
                secondaryEntityListener = null
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error removing secondary entity listener during cleanup")
            }
        }
    }

    /**
     * Stop monitoring entities - called when exiting map screen
     */
    fun stopMonitoring() {
        if (!isMonitoring) {
            Timber.tag(TAG).d("Entity monitoring not active - skipping stop")
            return
        }
        
        isStoppingMonitoring = true
        Timber.tag(TAG).d("Stopping entity monitoring")
// Remove Firebase listeners
        primaryEntityListener?.let {
            try {
                val primaryType = _userType.value
                val destination = _currentDestination.value
                val path = "${primaryType}s/$destination"
                firebaseDatabase.reference.child(path).removeEventListener(it)
                primaryEntityListener = null
                Timber.tag(TAG).d("Removed primary entity listener")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error removing primary entity listener")
            }
        }
        secondaryEntityListener?.let {
            try {
                val secondaryType = if (_userType.value == "driver") "passenger" else "driver"
                val destination = _currentDestination.value
                val path = "${secondaryType}s/$destination"
                firebaseDatabase.reference.child(path).removeEventListener(it)
                secondaryEntityListener = null
                Timber.tag(TAG).d("Removed secondary entity listener")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error removing secondary entity listener")
            }
        }
// Clear entity lists
        _primaryEntityLocations.value = emptyList()
        _secondaryEntityLocations.value = emptyList()
        
        // Reset monitoring state
        isMonitoring = false
        isStoppingMonitoring = false
        
        Timber.tag(TAG).d("Entity monitoring stopped and state reset")
    }
    /**
     * Update location from Android Location
     * Delegates to LocationService
     */
    fun updateLocation(location: android.location.Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        locationService.updateLocation(latLng)
    }
    fun updateLocation(latLng: LatLng) {
        locationService.updateLocation(latLng)
    }
    /**
     * Update the counts of nearby entities with performance optimizations
     */
    fun updateNearbyCounts() {
        val location = currentLocation.value ?: return
        val primaryLocations = _primaryEntityLocations.value
        val secondaryLocations = _secondaryEntityLocations.value
        val notificationRadiusKm = _searchRadius.value.toDouble()
        
        // Performance optimization: batch distance calculations
        var nearbyPrimary = 0
        var nearbySecondary = 0
        
        // Count primary entities with optimized distance calculation
        for (entityLocation in primaryLocations) {
            if (locationService.calculateDistance(location, entityLocation) <= notificationRadiusKm) {
                nearbyPrimary++
            }
        }
        
        // Count secondary entities
        for (entityLocation in secondaryLocations) {
            if (locationService.calculateDistance(location, entityLocation) <= notificationRadiusKm) {
                nearbySecondary++
            }
        }
        
        // Performance optimization: only update state if values have actually changed
        var hasChanged = false
        if (nearbyPrimary != _nearbyPrimaryCount.value) {
            _nearbyPrimaryCount.value = nearbyPrimary
            hasChanged = true
        }
        if (nearbySecondary != _nearbySecondaryCount.value) {
            _nearbySecondaryCount.value = nearbySecondary
            hasChanged = true
        }
        
        // Only log if counts actually changed to reduce log spam
        if (hasChanged) {
            Timber.tag(TAG)
                .d("Updated counts: $nearbyPrimary nearby ${_userType.value} entities and $nearbySecondary nearby opposite entities within ${notificationRadiusKm}km")
        }
    }
    /**
     * Toggle visibility of same-type entities
     */
    fun toggleSameTypeEntitiesVisibility() {
        _showSameTypeEntities.value = !_showSameTypeEntities.value
        Timber.tag(TAG)
            .d("Toggled ${_userType.value} visibility to: ${_showSameTypeEntities.value}")
    }
    /**
     * Toggle notifications for opposite role entities
     */
    fun toggleOppositeRoleNotifications() {
        _notifyOppositeRole.value = !_notifyOppositeRole.value
// Save to shared preferences
        viewModelScope.launch {
// Save based on user type
            val prefKey = if (_userType.value == "driver") "notify_passengers" else "notify_drivers"
            sharedPreferences.edit {
                putBoolean(prefKey, _notifyOppositeRole.value)
            }
// Use application context for app-level updates
            updateApplicationNotificationSettings()
            Timber.tag(TAG)
                .d("Opposite role notifications ${if (_notifyOppositeRole.value) "enabled" else "disabled"}")
        }
    }
    /**
     * Toggle map type between Normal and Hybrid
     */
    fun toggleMapType() {
        _mapType.value = if (_mapType.value == MapType.NORMAL) MapType.HYBRID else MapType.NORMAL
        Timber.tag(TAG)
            .d("Toggled map type to: ${if (_mapType.value == MapType.NORMAL) "Normal" else "Hybrid"}")
    }
    /**
     * Update search radius and save to preferences
     * This now only needs to save to preferences, as the flow collection will update the ViewModel state
     */
    fun updateSearchRadius(radiusKm: Float) {
// Save to preferences repository (which will emit the updated value via flow)
        viewModelScope.launch {
            preferencesRepository.setNotificationRadius(radiusKm)
// Update app-level notification service
            updateApplicationNotificationSettings()
            Timber.tag(TAG).d("Search radius updated to $radiusKm km")
        }
    }
    /**
     * Helper method to update application-level notification settings
     */
    private fun updateApplicationNotificationSettings() {
// This is a simplified approach that maintains compatibility
        val appContext = context.applicationContext
        val taxiApplication = appContext as? com.tecvo.taxi.TaxiApplication
        taxiApplication?.updateNotificationServicePreferences()
    }
    /**
     * Update location permission status and handle permission denial
     * Delegates to LocationService to check permission
     *
     * @param granted Whether the permission was granted (true) or denied (false)
     */
    fun updateLocationPermission(granted: Boolean) {
        if (!granted) {
            handlePermissionDenial()
        }
    }
    private fun handlePermissionDenial() {
        _permissionDenied.value = true
// Use the error handling service to categorize and log the error
        val error = AppError.PermissionError(
            "LOCATION",
            "Location permission denied by user"
        )
        errorHandlingService.logError(error, TAG)
    }

    /**
     * Clean up Firebase user data when no longer needed
     * Made public for navigation-based cleanup
     */
    fun cleanupFirebaseUserData() {
        val userId = _userId.value ?: return
        val userType = _userType.value
        val destination = _currentDestination.value

        if (userId.isNotEmpty()) {
            val userRef = firebaseDatabase.reference.child("${userType}s/$destination/$userId")
            FirebaseCleanupUtil.removeUserData(
                userRef,
                viewModelScope,
                userId,
                userType,
                destination
            )
        }
    }

    /**
     * Remove user from Firebase and clean up resources when no longer needed
     */
    fun cleanup() {
        Timber.tag(TAG).i("Cleaning up MapViewModel")
        
        // Cancel any pending jobs
        locationListenerJob?.cancel()
        preferenceObserverJob?.cancel()
        markerPopupJob?.cancel()
        
        // Remove Firebase listeners
        primaryEntityListener?.let {
            try {
                val primaryType = _userType.value
                val destination = _currentDestination.value
                val path = "${primaryType}s/$destination"
                firebaseDatabase.reference.child(path).removeEventListener(it)
                Timber.tag(TAG).d("Removed primary entity listener")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error removing primary entity listener")
            }
        }
        secondaryEntityListener?.let {
            try {
                val secondaryType = if (_userType.value == "driver") "passenger" else "driver"
                val destination = _currentDestination.value
                val path = "${secondaryType}s/$destination"
                firebaseDatabase.reference.child(path).removeEventListener(it)
                Timber.tag(TAG).d("Removed secondary entity listener")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error removing secondary entity listener")
            }
        }
        
        // Null out the references
        primaryEntityListener = null
        secondaryEntityListener = null

        // Firebase cleanup removed - should only happen on true map exit, not ViewModel disposal

        // Clear entity lists and cached data to free memory
        _primaryEntityLocations.value = emptyList()
        _secondaryEntityLocations.value = emptyList()
        
        // Performance optimization: reset cache variables and free memory
        lastEntityFetchTime = 0L
        cachedPrimaryEntities = emptyList()
        cachedSecondaryEntities = emptyList()
        entityCacheTime = 0L
        
        // Stop location updates - use default method since stopLocationUpdates doesn't take parameter
        locationService.stopLocationUpdates()
        
        // Perform memory optimization
        memoryOptimizer.performImmediateCleanup()
        
        Timber.tag(TAG).i("MapViewModel cleanup completed. Memory stats: ${memoryOptimizer.getStats()}")
    }

    /**
     * Handle marker click to show area information
     */
    fun onMarkerClick(location: LatLng, markerType: String) {
        Timber.tag(TAG).d("Marker clicked at: ${location.latitude}, ${location.longitude}, type: $markerType")
        
        // Cancel any existing popup auto-clear job
        markerPopupJob?.cancel()
        
        // Set the selected marker info
        _selectedMarkerLocation.value = location
        _selectedMarkerType.value = markerType
        _isLoadingAreaInfo.value = true
        
        // Get area information in the background
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val areaName = geocodingService.getPlaceNameFromCoordinates(
                    location.latitude,
                    location.longitude
                )
                
                // Update UI on main thread
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _selectedMarkerAreaName.value = areaName
                    _isLoadingAreaInfo.value = false
                    Timber.tag(TAG).d("Area name retrieved: $areaName for $markerType")
                    
                    // Start auto-clear timer after area info is loaded
                    startPopupAutoClear()
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _selectedMarkerAreaName.value = "Area information unavailable"
                    _isLoadingAreaInfo.value = false
                    Timber.tag(TAG).w(e, "Failed to get area name for marker at ${location.latitude}, ${location.longitude}")
                    
                    // Start auto-clear timer even if geocoding fails
                    startPopupAutoClear()
                }
            }
        }
    }
    
    /**
     * Start a 3-second timer to automatically clear the popup
     */
    private fun startPopupAutoClear() {
        markerPopupJob = viewModelScope.launch {
            kotlinx.coroutines.delay(3000) // Wait 3 seconds
            clearSelectedMarker()
            Timber.tag(TAG).d("Auto-cleared marker popup after 3 seconds")
        }
    }
    
    /**
     * Clear selected marker information
     */
    fun clearSelectedMarker() {
        _selectedMarkerLocation.value = null
        _selectedMarkerAreaName.value = null
        _selectedMarkerType.value = null
        _isLoadingAreaInfo.value = false
    }

    /**
     * Handle cleanup when ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        cleanup()
        connectivityManager.cleanup()
        
        // Final memory optimization
        try {
            memoryOptimizer.suggestGcIfNeeded()
        } catch (e: Exception) {
            Timber.tag(TAG).w("Error suggesting GC during onCleared: ${e.message}")
        }
    }
}