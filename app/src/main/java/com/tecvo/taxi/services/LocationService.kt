package com.tecvo.taxi.services

import android.app.Activity
import android.content.Context
import android.location.Location
import android.os.BatteryManager
import android.os.Looper
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.tecvo.taxi.BuildConfig
import com.tecvo.taxi.permissions.PermissionManager
import com.tecvo.taxi.services.ErrorHandlingService.AppError
import com.tecvo.taxi.utils.executeWithRetry
import com.tecvo.taxi.utils.launchSafely
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import com.tecvo.taxi.model.Location as ModelLocation

/**
 * Centralized service for all location-related operations in the app.
 */
@Singleton
open class LocationService @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val errorHandlingService: ErrorHandlingService,
    private val permissionManager: PermissionManager,
    private val locationStateManager: LocationServiceStateManager
) {
    // Flag for background permission status
    private var hasBackgroundPermission = false

    // Constants
    private val tag = "LocationService"
    private val defaultUpdateIntervalMs = 30000L // 30 seconds
    private val fastUpdateIntervalMs = 15000L // 15 seconds
    private val backgroundUpdateIntervalMs = 300000L // 5 minutes
    private val backgroundMinUpdateIntervalMs = 120000L // 2 minutes
    private val lowBatteryUpdateIntervalMs = 600000L // 10 minutes
    private val maxRetryAttempts = 3
    private val baseRetryDelayMs = 500L

    // Distance thresholds for updates (in meters)
    private val defaultMinDisplacement = 20f
    private val chargingMinDisplacement = 10f
    private val backgroundMinDisplacement = 50f
    private val lowBatteryMinDisplacement = 100f

    // Initialization tracking
    private var isFullyInitialized = false

    // Performance Optimization: Cache expensive calculations
    private var lastBatteryCheckTime = 0L
    private var cachedBatteryLevel = 100
    private var cachedIsCharging = false
    private var cachedBatteryManager: BatteryManager? = null
    private val batteryCheckCacheMs = 30000L // Cache battery status for 30 seconds
    
    // Performance Optimization: Prevent unnecessary start/stop cycles
    private var lastRequestedInterval = 0L
    private var lastRequestedPriority: Int? = null

    // Location client and callback
    // This will be the actual client instance
    private var _fusedLocationClient: FusedLocationProviderClient? = null

    // This is the property the rest of the code will use
    protected open val fusedLocationClient: FusedLocationProviderClient
        get() = _fusedLocationClient ?: LocationServices.getFusedLocationProviderClient(applicationContext).also { _fusedLocationClient = it }

    // Add a setter for testing - Changed from protected to public
    open fun setFusedLocationClient(client: FusedLocationProviderClient) {
        _fusedLocationClient = client
    }

    /**
     * Performance Optimization: Get cached battery status to avoid expensive system calls
     */
    private fun getCachedBatteryStatus(): Pair<Int, Boolean> {
        val currentTime = System.currentTimeMillis()
        
        if (currentTime - lastBatteryCheckTime > batteryCheckCacheMs || cachedBatteryManager == null) {
            // Cache expired or not initialized - refresh
            try {
                if (cachedBatteryManager == null) {
                    cachedBatteryManager = applicationContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                }
                
                cachedBatteryLevel = cachedBatteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
                cachedIsCharging = cachedBatteryManager?.isCharging ?: false
                lastBatteryCheckTime = currentTime
                
                if (BuildConfig.DEBUG && currentTime - lastBatteryCheckTime > batteryCheckCacheMs * 2) {
                    Timber.tag(tag).d("Battery status refreshed: level=%d%%, charging=%s", cachedBatteryLevel, cachedIsCharging)
                }
            } catch (e: Exception) {
                Timber.tag(tag).w(e, "Error getting battery status, using cached values")
            }
        }
        
        return Pair(cachedBatteryLevel, cachedIsCharging)
    }

    private var locationCallback: LocationCallback? = null

    // Location state flows
    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation.asStateFlow()

    private val _lastUpdateTime = MutableStateFlow<Long>(0L)
    val lastUpdateTime: StateFlow<Long> = _lastUpdateTime.asStateFlow()

    private val _locationPermissionGranted = MutableStateFlow(false)
    val locationPermissionGranted: StateFlow<Boolean> = _locationPermissionGranted.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    // User data state
    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId.asStateFlow()

    private val _userType = MutableStateFlow<String?>(null)
    val userType: StateFlow<String?> = _userType.asStateFlow()

    private val _destination = MutableStateFlow<String?>(null)
    val destination: StateFlow<String?> = _destination.asStateFlow()

    // Firebase references
    private var userRef: DatabaseReference? = null
    private val database = FirebaseDatabase.getInstance()

    // Background/foreground state
    private var isInBackground = false
    private var lastBackgroundUpdateTime = 0L
    
    // Performance optimization: debouncing and batching
    private var lastFirebaseUpdateTime = 0L
    private var pendingLocationUpdate: Job? = null
    private val firebaseUpdateDebounceMs = 5000L // 5 second debounce for Firebase updates
    private var lastLocationUpdate: LatLng? = null
    private val minimumDistanceForUpdate = 10.0 // meters

    // Coroutine scope for operations
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Retry mechanisms
    private var updateRetryJob: Job? = null
    
    // Permission flow collection jobs
    private var locationPermissionFlowJob: Job? = null
    private var backgroundPermissionFlowJob: Job? = null

    init {
        try {
            // Set initial permission state by checking with PermissionManager
            _locationPermissionGranted.value = permissionManager.isLocationPermissionGranted()

            // Set background permission state
            hasBackgroundPermission = permissionManager.isBackgroundLocationPermissionGranted()

            // Initialize userId from Firebase Auth if available
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                _userId.value = currentUser.uid
            }

            isFullyInitialized = true
            Timber.tag(tag).i("LocationService initialized. Permission granted: %s", _locationPermissionGranted.value)

            // Register this instance with the companion object for backward compatibility
            instance = this

            // Set up flow collection to update our permission states
            locationPermissionFlowJob = serviceScope.launch {
                permissionManager.locationPermissionFlow.collect { granted ->
                    _locationPermissionGranted.value = granted
                }
            }

            backgroundPermissionFlowJob = serviceScope.launch {
                permissionManager.backgroundLocationPermissionFlow.collect { granted ->
                    hasBackgroundPermission = granted
                }
            }
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Error during LocationService initialization: %s", e.message)
            // Mark as initialized to prevent further init attempts that might fail
            isFullyInitialized = true
        }
    }

    companion object {
        @Volatile
        private var instance: LocationService? = null

        // For backward compatibility during migration
        fun getInstance(): LocationService {
            return instance ?: throw IllegalStateException(
                "LocationService not initialized. Make sure you're using Hilt injection or wait for initialization."
            )
        }

        fun isInitialized(): Boolean {
            return instance != null
        }
    }

    /**
     * Update background permission status
     */
    fun updateBackgroundPermissionStatus(granted: Boolean) {
        hasBackgroundPermission = granted
        Timber.tag(tag).d("Background location permission status updated: %s", if (granted) "granted" else "denied")
    }

    /**
     * Check if location permissions are currently granted
     */
    fun checkLocationPermission(): Boolean {
        // Use PermissionManager to check permission
        return permissionManager.isLocationPermissionGranted().also {
            _locationPermissionGranted.value = it
            Timber.tag(tag).d("Location permission check: %s", it)
        }
    }

    /**
     * Request location permission using unified permission handling
     */
    fun requestLocationPermission(
        activity: Activity,
        permissionLauncher: ActivityResultLauncher<String>,
        onPermissionGranted: () -> Unit = {}
    ) {
        Timber.tag(tag).i("Requesting location permission")

        permissionManager.requestLocationPermission(
            activity = activity,
            permissionLauncher = permissionLauncher
        ) { granted ->
            if (granted == true) {
                _locationPermissionGranted.value = true
                onPermissionGranted()
            }
        }
    }

    // In the LocationService class, add this protected method for testing

    /**
     * Update location permission status and associated state
     *
     * @param granted Whether the permission is granted
     */
    fun updateLocationPermission(granted: Boolean) {
        _locationPermissionGranted.value = granted
        Timber.tag(tag).d("Location permission status updated: %s", if (granted) "granted" else "denied")

        // If permission was granted, we can try to initialize location services
        if (granted == true) {
            // This is optional, but helps ensure location services start right after permission is granted
            startLocationUpdates()
        }
    }

    /**
     * Set user identity information for location updates
     */
    fun setUserInfo(userId: String, userType: String, destination: String) {
        Timber.tag(tag).d("Setting user info: userId=%s, userType=%s, destination=%s", userId, userType, destination)
        _userId.value = userId
        _userType.value = userType
        _destination.value = destination

        // Update Firebase reference
        updateFirebaseReference()
    }

    /**
     * Update only the destination without changing other user info
     */
    fun updateDestination(destination: String) {
        Timber.tag(tag).d("Updating destination to: %s", destination)
        if (_destination.value != destination) {
            val oldDestination = _destination.value
            val userId = _userId.value
            val userType = _userType.value

            // Use coroutine to properly await removal before proceeding
            serviceScope.launch {
                // Remove user from old destination path first and wait for completion
                if (oldDestination != null && userId != null && userType != null) {
                    val oldRef = database.reference.child("${userType}s/$oldDestination/$userId")
                    try {
                        oldRef.removeValue().await()
                        Timber.tag(tag).d("Successfully removed user from old destination: %s", oldDestination)
                    } catch (e: Exception) {
                        Timber.tag(tag).e("Failed to remove user from old destination: %s", e.message)
                    }
                }

                // Update the destination value
                _destination.value = destination

                // Update Firebase reference with new destination
                updateFirebaseReference()

                // Update location in Firebase with new destination
                _currentLocation.value?.let { location ->
                    updateLocationInFirebase(location.latitude, location.longitude)
                }
            }
        }
    }

    /**
     * Update only the user type without changing other user info
     */
    fun updateUserType(userType: String) {
        Timber.tag(tag).d("Updating user type to: %s", userType)
        if (_userType.value != userType) {
            val oldUserType = _userType.value
            val userId = _userId.value
            val destination = _destination.value

            // Use coroutine to properly await removal before proceeding
            serviceScope.launch {
                // Remove user from old userType path first and wait for completion
                if (oldUserType != null && userId != null && destination != null) {
                    val oldRef = database.reference.child("${oldUserType}s/$destination/$userId")
                    try {
                        oldRef.removeValue().await()
                        Timber.tag(tag).d("Successfully removed user from old userType: %s", oldUserType)
                    } catch (e: Exception) {
                        Timber.tag(tag).e("Failed to remove user from old userType: %s", e.message)
                    }
                }

                // Update the userType value
                _userType.value = userType

                // Update Firebase reference with new user type
                updateFirebaseReference()

                // Update location in Firebase with new user type
                _currentLocation.value?.let { location ->
                    updateLocationInFirebase(location.latitude, location.longitude)
                }
            }
        }
    }

    /**
     * Update Firebase reference based on current user info
     */
    private fun updateFirebaseReference() {
        val userId = _userId.value
        val userType = _userType.value
        val destination = _destination.value

        if (userId != null && userType != null && destination != null) {
            userRef = database.reference.child("${userType}s/$destination/$userId")
            // Set up onDisconnect to remove user when connection is lost
            userRef?.onDisconnect()?.removeValue()
            Timber.tag(tag).d("Firebase reference updated: %s/%s/%s", "${userType}s", destination, userId)
        } else {
            Timber.tag(tag).w("Cannot update Firebase reference - missing user info")
        }
    }

    /**
     * Get last known location as a one-time operation
     *
     * @param onSuccess Callback when location is successfully retrieved
     * @param onFailure Callback when location retrieval fails
     */
    fun getLastLocation(
        onSuccess: (LatLng) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Timber.tag(tag).d("Getting last known location")
        if (!isFullyInitialized) {
            val error = AppError.ValidationError(message = "LocationService not fully initialized")
            errorHandlingService.logError(error, tag)
            onFailure(IllegalStateException("LocationService not fully initialized"))
            return
        }

        if (!checkLocationPermission()) {
            val error = AppError.PermissionError(
                "LOCATION",
                "Location permission not granted"
            )
            errorHandlingService.logError(error, tag)
            onFailure(SecurityException("Location permission not granted"))
            return
        }

        serviceScope.launchSafely(tag = tag) {
            try {
                val location = executeWithRetry(
                    operation = {
                        fusedLocationClient.lastLocation.await()
                    },
                    maxRetries = maxRetryAttempts,
                    initialDelayMillis = baseRetryDelayMs
                ).getOrNull()

                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    _currentLocation.value = latLng
                    _lastUpdateTime.value = System.currentTimeMillis()
                    Timber.tag(tag).d("Last location retrieved: %s, %s", location.latitude.toString(), location.longitude.toString())
                    onSuccess(latLng)

                    // Also update Firebase if user info is available
                    if (_userId.value != null) {
                        updateLocationInFirebase(location.latitude, location.longitude)
                    }
                } else {
                    Timber.tag(tag).w("Last known location is null, attempting Firebase fallback")
                    getLocationFromFirebase { firebaseLocation ->
                        if (firebaseLocation != null) {
                            Timber.tag(tag).d("Retrieved location from Firebase as fallback")
                            onSuccess(firebaseLocation)
                        } else {
                            Timber.tag(tag).w("No location available from any source")
                            onFailure(Exception("No location available from device or Firebase"))
                        }
                    }
                }
            } catch (e: Exception) {
                // This will be caught by launchSafely and processed by the error handler
                val error = if (e is SecurityException) {
                    AppError.PermissionError("LOCATION", e.message)
                } else {
                    AppError.LocationError(e, "Error getting last location")
                }
                errorHandlingService.logError(error, tag)
                onFailure(e)

                // Try to get location from Firebase as fallback
                getLocationFromFirebase { firebaseLocation ->
                    if (firebaseLocation != null) {
                        onSuccess(firebaseLocation)
                    } else {
                        onFailure(e)
                    }
                }
            }
        }
    }

    /**
     * Get last known location as a suspending function
     *
     * @return LatLng of the last known location or null if not available
     */
    suspend fun getLastLocationSuspend(): LatLng? {
        Timber.tag(tag).d("Getting last known location (suspend)")
        if (!isFullyInitialized) {
            Timber.tag(tag).e("LocationService not fully initialized")
            return null
        }

        if (!checkLocationPermission()) {
            Timber.tag(tag).e("Location permission not granted")
            return null
        }

        return try {
            val location = fusedLocationClient.lastLocation.await()
            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                _currentLocation.value = latLng
                _lastUpdateTime.value = System.currentTimeMillis()
                Timber.tag(tag).d("Last location retrieved: %s, %s", location.latitude.toString(), location.longitude.toString())

                // Also update Firebase if user info is available
                if (_userId.value != null) {
                    updateLocationInFirebase(location.latitude, location.longitude)
                }
                latLng
            } else {
                Timber.tag(tag).w("Last known location is null")
                getLocationFromFirebaseSuspend()
            }
        } catch (e: Exception) {
            Timber.tag(tag).e("Error getting last location: %s", e.message)
            getLocationFromFirebaseSuspend()
        }
    }

    /**
     * Try to get user location from Firebase as fallback
     */
    private fun getLocationFromFirebase(callback: (LatLng?) -> Unit) {
        val userIdValue = _userId.value
        if (userIdValue == null) {
            Timber.tag(tag).e("Cannot get location from Firebase - userId is null")
            callback(null)
            return
        }
        val userId = userIdValue

        // Try possible paths for this user
        val paths = arrayOf(
            "passengers/town/$userId",
            "passengers/local/$userId",
            "drivers/town/$userId",
            "drivers/local/$userId"
        )

        checkNextPath(database, paths, 0, userId, callback)
    }

    /**
     * Recursively check Firebase paths for location data
     */
    private fun checkNextPath(
        database: FirebaseDatabase,
        paths: Array<String>,
        index: Int,
        userId: String,
        callback: (LatLng?) -> Unit
    ) {
        // If we've checked all paths, return null
        if (index >= paths.size) {
            Timber.tag(tag).w("No location found in any Firebase path for user %s", userId)
            callback(null)
            return
        }

        val path = paths[index]
        Timber.tag(tag).d("Checking Firebase path: %s", path)

        try {
            database.reference.child(path).get()
                .addOnSuccessListener { snapshot ->
                    try {
                        if (snapshot.exists() && snapshot.hasChild("latitude") && snapshot.hasChild("longitude")) {
                            val lat = snapshot.child("latitude").getValue(Double::class.java)
                            val lng = snapshot.child("longitude").getValue(Double::class.java)

                            if (lat != null && lng != null) {
                                val location = LatLng(lat, lng)
                                _currentLocation.value = location
                                Timber.tag(tag).d("Location found in Firebase: %s, %s", lat.toString(), lng.toString())
                                callback(location)
                            } else {
                                // Try next path
                                checkNextPath(database, paths, index + 1, userId, callback)
                            }
                        } else {
                            // Try next path
                            checkNextPath(database, paths, index + 1, userId, callback)
                        }
                    } catch (e: Exception) {
                        Timber.tag(tag).e("Error parsing location data: %s", e.message)
                        checkNextPath(database, paths, index + 1, userId, callback)
                    }
                }
                .addOnFailureListener { e ->
                    Timber.tag(tag).e("Failed to get data from path %s: %s", path, e.message)
                    checkNextPath(database, paths, index + 1, userId, callback)
                }
        } catch (e: Exception) {
            Timber.tag(tag).e("Error accessing Firebase: %s", e.message)
            checkNextPath(database, paths, index + 1, userId, callback)
        }
    }

    /**
     * Try to get user location from Firebase as fallback (suspend version)
     */
    private suspend fun getLocationFromFirebaseSuspend(): LatLng? {
        val userIdValue = _userId.value
        if (userIdValue == null) {
            Timber.tag(tag).e("Cannot get location from Firebase - userId is null")
            return null
        }
        val userId = userIdValue

        // Try possible paths for this user
        val paths = arrayOf(
            "passengers/town/$userId",
            "passengers/local/$userId",
            "drivers/town/$userId",
            "drivers/local/$userId"
        )

        for (path in paths) {
            try {
                val snapshot = database.reference.child(path).get().await()
                if (snapshot.exists() && snapshot.hasChild("latitude") && snapshot.hasChild("longitude")) {
                    val lat = snapshot.child("latitude").getValue(Double::class.java)
                    val lng = snapshot.child("longitude").getValue(Double::class.java)

                    if (lat != null && lng != null) {
                        val location = LatLng(lat, lng)
                        _currentLocation.value = location
                        Timber.tag(tag).d("Location found in Firebase: %s, %s", lat.toString(), lng.toString())
                        return location
                    }
                }
            } catch (e: Exception) {
                Timber.tag(tag).e("Error accessing Firebase path %s: %s", path, e.message)
                // Continue to next path
            }
        }

        Timber.tag(tag).w("No location found in any Firebase path for user %s", userId)
        return null
    }

    /**
     * Prepares location service components without starting updates
     * Called during initialization to set up required components
     */
    fun prepareLocationUpdates() {
        Timber.tag(tag).d("Preparing location service components")
        try {
            // Ensure the client is initialized
            _fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)

            // Check current permission state
            _locationPermissionGranted.value = permissionManager.isLocationPermissionGranted()

            // Nothing more to do until permission is granted and updates are requested
            Timber.tag(tag).d("Location service components prepared")
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Error preparing location updates: %s", e.message)
        }
    }

    /**
     * Start continuous location updates
     *
     * @param lifecycleOwner LifecycleOwner to manage update lifecycle
     * @param interval Update interval in milliseconds
     * @param onUpdate Optional callback for location updates
     */
    fun startLocationUpdates(
        lifecycleOwner: LifecycleOwner? = null,
        interval: Long = defaultUpdateIntervalMs,
        onUpdate: ((LatLng) -> Unit)? = null
    ) {
        if (!isFullyInitialized) {
            Timber.tag(tag).w("Cannot start location updates - LocationService not fully initialized")
            return
        }

        // Generate consumer ID for tracking
        val consumerId = generateConsumerId(lifecycleOwner, interval)
        
        // Use state manager to check if service should start
        val priority = determinePriority()
        val shouldStart = locationStateManager.requestLocationUpdates(consumerId, interval, priority)
        
        if (!shouldStart) {
            if (BuildConfig.DEBUG) {
                Timber.tag(tag).i("Location updates already active with same parameters, not starting again (consumer: $consumerId)")
                Timber.tag(tag).d("Current state: ${locationStateManager.getDebugInfo()}")
            }
            return
        }

        if (!checkLocationPermission()) {
            Timber.tag(tag).e("Cannot start location updates - permission not granted")
            // Remove consumer from state manager since we can't provide updates
            locationStateManager.releaseLocationUpdates(consumerId)
            return
        }

        try {
            // Performance Optimization: Use cached battery status to avoid expensive system calls
            val (batteryLevel, isCharging) = getCachedBatteryStatus()

            // Determine priority based on app state and battery
            val priority = when {
                isCharging -> Priority.PRIORITY_HIGH_ACCURACY
                isInBackground && batteryLevel < 20 -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
                isInBackground -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
                else -> Priority.PRIORITY_HIGH_ACCURACY
            }

            // Adjust intervals based on battery and app state
            val actualInterval = when {
                isCharging -> interval // Use requested interval when charging
                isInBackground && batteryLevel < 20 -> lowBatteryUpdateIntervalMs // Save battery
                isInBackground -> backgroundUpdateIntervalMs
                else -> interval
            }

            val minUpdateInterval = when {
                isCharging -> fastUpdateIntervalMs
                isInBackground && batteryLevel < 20 -> lowBatteryUpdateIntervalMs
                isInBackground -> backgroundMinUpdateIntervalMs
                else -> fastUpdateIntervalMs
            }

            // Add distance filter to prevent unnecessary updates
            val minDisplacement = when {
                isCharging -> chargingMinDisplacement // 10 meters when charging
                isInBackground && batteryLevel < 20 -> lowBatteryMinDisplacement // 100 meters on low battery
                isInBackground -> backgroundMinDisplacement // 50 meters in background
                else -> defaultMinDisplacement // 20 meters in foreground
            }

            // Location request with both time and distance filtering
            val locationRequest = LocationRequest.Builder(actualInterval)
                .setPriority(priority)
                .setMinUpdateIntervalMillis(minUpdateInterval)
                .setMinUpdateDistanceMeters(minDisplacement) // Only update if moved this far
                .setWaitForAccurateLocation(!isInBackground) // Don't wait for high accuracy when in background
                .setMaxUpdateDelayMillis(actualInterval * 2) // Allow system to batch updates
                .build()

            // Create location callback
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        val latLng = LatLng(location.latitude, location.longitude)

                        // Update internal state
                        _currentLocation.value = latLng
                        _lastUpdateTime.value = System.currentTimeMillis()
                        Timber.tag(tag).v("Location update: %s, %s, accuracy: %sm",
                            location.latitude.toString(), location.longitude.toString(), location.accuracy.toString())

                        // Call optional callback
                        onUpdate?.invoke(latLng)

                        // Update Firebase if needed
                        handleLocationUpdate(location)
                    }
                }
            }

            // Register for updates
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )

            _isUpdating.value = true
            Timber.tag(tag).i(
                "Location updates started with interval: %sms, priority: %s, " +
                        "minUpdateInterval: %sms, minDisplacement: %sm",
                actualInterval.toString(), priority.toString(), minUpdateInterval.toString(), minDisplacement.toString()
            )

            // Safely add lifecycle observer only if lifecycle is in appropriate state
            lifecycleOwner?.let { owner ->
                try {
                    // Check if lifecycle is in an appropriate state for adding observers
                    if (owner.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.INITIALIZED) &&
                        !owner.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.DESTROYED)
                    ) {
                        owner.lifecycleScope.launch {
                            try {
                                owner.lifecycle.addObserver(androidx.lifecycle.LifecycleEventObserver { _, event ->
                                    if (event == androidx.lifecycle.Lifecycle.Event.ON_DESTROY) {
                                        stopLocationUpdates()
                                    }
                                })
                            } catch (e: Exception) {
                                Timber.tag(tag).e(e, "Error adding lifecycle observer: %s", e.message)
                            }
                        }
                    } else {
                        Timber.tag(tag).w("Lifecycle owner in inappropriate state for observer registration: %s",
                            owner.lifecycle.currentState)
                    }
                } catch (e: Exception) {
                    Timber.tag(tag).e(e, "Error accessing lifecycle: %s", e.message)
                }
            }
        } catch (e: SecurityException) {
            Timber.tag(tag).e("Security exception when starting location updates: %s", e.message)
            // Remove consumer from state manager on error
            locationStateManager.releaseLocationUpdates(consumerId)
        } catch (e: Exception) {
            Timber.tag(tag).e("Error starting location updates: %s", e.message)
            // Remove consumer from state manager on error
            locationStateManager.releaseLocationUpdates(consumerId)
        }
    }
    
    /**
     * Generates a unique consumer ID for tracking
     */
    private fun generateConsumerId(lifecycleOwner: LifecycleOwner?, interval: Long): String {
        val ownerName = lifecycleOwner?.javaClass?.simpleName ?: "unknown"
        return "${ownerName}_${interval}_${System.currentTimeMillis()}"
    }
    
    /**
     * Determines location priority based on current conditions
     */
    private fun determinePriority(): Int {
        val (batteryLevel, isCharging) = getCachedBatteryStatus()
        
        return when {
            isCharging -> Priority.PRIORITY_HIGH_ACCURACY
            isInBackground && batteryLevel < 20 -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
            isInBackground -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
            else -> Priority.PRIORITY_HIGH_ACCURACY
        }
    }

    /**
     * Process location update with optimized handling to reduce redundant operations
     */
    private fun handleLocationUpdate(location: Location) {
        val currentTime = System.currentTimeMillis()
        val newLatLng = LatLng(location.latitude, location.longitude)
        
        // Performance optimization: check if location has changed significantly
        val lastLoc = _currentLocation.value
        if (lastLoc != null) {
            val distance = calculateDistance(lastLoc, newLatLng) * 1000 // Convert to meters
            if (distance < defaultMinDisplacement) {
                Timber.tag(tag).d("Location update skipped - distance too small: ${distance}m")
                return
            }
        }
        
        // Update local state immediately (Main thread)
        _currentLocation.value = newLatLng
        _lastUpdateTime.value = currentTime
        
        // Performance optimization: batch all background operations
        serviceScope.launch(Dispatchers.IO) {
            // Handle background update throttling
            if (isInBackground) {
                if (currentTime - lastBackgroundUpdateTime < backgroundUpdateIntervalMs) {
                    Timber.tag(tag).d("Background update throttled")
                    return@launch
                }
                lastBackgroundUpdateTime = currentTime
            }
            
            // Firebase update (already optimized with debouncing)
            updateLocationInFirebase(location.latitude, location.longitude)
            
            // Update application location (lower priority)
            try {
                val modelLocation = ModelLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    timestamp = location.time
                )
                // Use reflection to avoid circular dependency
                try {
                    val taxiAppClass = Class.forName("com.tecvo.taxi.TaxiApplication")
                    val updateMethod = taxiAppClass.getDeclaredMethod("updateUserLocation", ModelLocation::class.java)
                    if (taxiAppClass.isInstance(applicationContext)) {
                        updateMethod.invoke(applicationContext, modelLocation)
                    }
                } catch (reflectionError: Exception) {
                    Timber.tag(tag).d("TaxiApplication not available or method not found: %s", reflectionError.message)
                }
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "Error updating user location in app: %s", e.message)
            }
        }
    }

    /**
     * Centralized throttling decision method that can be used by other services
     *
     * @param currentTime Current time in milliseconds
     * @param lastUpdateTime Last update time in milliseconds
     * @param minInterval Minimum interval between updates in milliseconds
     * @return True if update should be throttled (skipped), false otherwise
     */
    fun shouldThrottleUpdate(currentTime: Long, lastUpdateTime: Long, minInterval: Long): Boolean {
        // Get battery level for more aggressive throttling on low battery
        val batteryManager = applicationContext.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val batteryLevel = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
        val isCharging = batteryManager?.isCharging == true

        // Don't throttle when charging
        if (isCharging) return false

        // More aggressive throttling on low battery
        val effectiveMinInterval = when {
            batteryLevel < 20 -> minInterval * 2
            isInBackground -> minInterval
            else -> minInterval
        }

        return currentTime - lastUpdateTime < effectiveMinInterval
    }

    /**
     * Update location in Firebase with optimized debouncing and distance filtering
     * Only updates Firebase if enough time has passed and location has changed significantly
     */
    private fun updateLocationInFirebase(latitude: Double, longitude: Double) {
        if (!isFullyInitialized) {
            Timber.tag(tag).w("Cannot update Firebase - LocationService not fully initialized")
            return
        }

        val userId = _userId.value
        val userType = _userType.value
        val destination = _destination.value

        if (userId == null || userType == null || destination == null) {
            Timber.tag(tag).w("Cannot update Firebase - missing user info")
            val error = AppError.ValidationError(message = "Cannot update location in Firebase - missing user info")
            errorHandlingService.logError(error, tag)
            return
        }

        val currentTime = System.currentTimeMillis()
        val newLocation = LatLng(latitude, longitude)
        
        // Performance optimization: debounce Firebase updates
        if (shouldThrottleFirebaseUpdate(currentTime, newLocation)) {
            Timber.tag(tag).d("Firebase update throttled - too recent or location unchanged")
            return
        }

        // Cancel any pending update to avoid duplicate writes
        pendingLocationUpdate?.cancel()
        
        // Create location data
        val locationData = mapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "timestamp" to ServerValue.TIMESTAMP,
            "type" to userType,
            "destination" to destination
        )

        // Update Firebase with debounced, batched operation
        pendingLocationUpdate = serviceScope.launch {
            // Additional delay for batching multiple rapid updates
            delay(1000) // 1 second batching delay
            
            val primaryRef = database.reference.child("${userType}s/$destination/$userId")
            
            executeWithRetry(
                operation = {
                    primaryRef.setValue(locationData).await()
                    Unit
                },
                maxRetries = maxRetryAttempts
            ).fold(
                onSuccess = { _: Unit ->
                    lastFirebaseUpdateTime = currentTime
                    lastLocationUpdate = newLocation
                    Timber.tag(tag).i("Firebase: Location update successful")
                },
                onFailure = { e: Throwable ->
                    val error = AppError.DatabaseError(e, "Firebase: Failed to update location")
                    errorHandlingService.logError(error, tag)

                    // Add retry logic for critical updates, but only if not in background
                    if (!isInBackground) {
                        retryLocationUpdate(primaryRef, locationData, 1)
                    }
                }
            )
        }
    }
    
    /**
     * Performance optimization: check if Firebase update should be throttled
     */
    private fun shouldThrottleFirebaseUpdate(currentTime: Long, newLocation: LatLng): Boolean {
        // Don't throttle if enough time has passed
        if (currentTime - lastFirebaseUpdateTime >= firebaseUpdateDebounceMs) {
            return false
        }
        
        // Don't throttle if location has changed significantly
        lastLocationUpdate?.let { lastLoc ->
            val distance = calculateDistance(lastLoc, newLocation) * 1000 // Convert to meters
            if (distance >= minimumDistanceForUpdate) {
                return false
            }
        }
        
        // Throttle if recent update and location hasn't changed much
        return true
    }

    /**
     * Retry failed location update with exponential backoff
     * Only used for critical foreground updates when regular retry fails
     */
    private suspend fun retryLocationUpdate(
        ref: DatabaseReference,
        locationData: Map<String, Any>,
        attempt: Int
    ) {
        if (attempt > maxRetryAttempts) {
            Timber.tag(tag).e("Firebase: Maximum retry attempts reached for location update")
            return
        }

        Timber.tag(tag).i("Firebase: Retrying location update (attempt %d of %d)",
            attempt, maxRetryAttempts)

        // Exponential backoff
        val delayMillis = baseRetryDelayMs * (2.0.pow(attempt - 1)).toLong()

        // Use coroutine delay instead of Handler
        delay(delayMillis)

        try {
            ref.setValue(locationData).await()
            Timber.tag(tag).i("Firebase: Location update succeeded on retry %d", attempt)
        } catch (e: Exception) {
            Timber.tag(tag).e("Firebase: Retry %d failed - %s", attempt, e.message)

            // Continue retrying if not in background
            if (!isInBackground) {
                retryLocationUpdate(ref, locationData, attempt + 1)
            }
        }
    }

    /**
     * Retry failed batch location update with exponential backoff
     */
    private fun retryBatchLocationUpdate(
        updates: Map<String, Any>,
        attempt: Int
    ) {
        // Cancel existing job if any
        updateRetryJob?.cancel()

        // Create new retry job using the utility function
        updateRetryJob = serviceScope.launch {
            executeWithRetry(
                operation = {
                    // Convert Firebase's callback-based API to a suspend function
                    suspendCoroutine<Unit> { continuation ->
                        database.reference.updateChildren(updates)
                            .addOnSuccessListener {
                                Timber.tag(tag).i("Firebase: Batch location update succeeded on retry")
                                continuation.resume(Unit)
                            }
                            .addOnFailureListener { e ->
                                continuation.resumeWithException(e)
                            }
                    }
                },
                maxRetries = maxRetryAttempts,
                initialDelayMillis = baseRetryDelayMs,
                shouldRetryPredicate = { throwable: Throwable ->
                    // Only retry network-related errors
                    errorHandlingService.isNetworkError(throwable)
                }
            ).fold(
                onSuccess = { _: Unit ->
                    Timber.tag(tag).i("Firebase: Batch location update completed successfully after retry")
                },
                onFailure = { e: Throwable ->
                    val error = AppError.DatabaseError(e, "Firebase: All retry attempts failed")
                    errorHandlingService.logError(error, tag)
                }
            )
        }
    }

    /**
     * Stop continuous location updates
     */
    fun stopLocationUpdates() {
        // Guard against being called before initialization
        if (!isFullyInitialized) {
            Timber.tag(tag).w("Cannot stop location updates - LocationService not fully initialized")
            return
        }

        locationCallback?.let {
            try {
                fusedLocationClient.removeLocationUpdates(it)
                _isUpdating.value = false
                Timber.tag(tag).i("Location updates stopped")
            } catch (e: Exception) {
                Timber.tag(tag).e("Error stopping location updates: %s", e.message)
            }
        }
    }

    /**
     * Pause location updates (typically when app goes to background)
     */
    fun pauseLocationUpdates() {
        // Guard against being called before initialization or when already in desired state
        if (!isFullyInitialized) {
            Timber.tag(tag).w("Cannot pause location updates - LocationService not fully initialized")
            return
        }

        if (isInBackground) return // Already paused

        isInBackground = true
        Timber.tag(tag).i("Location updates paused for background operation")

        // If we don't have background permission, stop updates entirely
        if (!hasBackgroundPermission) {
            Timber.tag(tag).i("No background location permission - stopping updates completely")
            stopLocationUpdates()
            return
        }

        // Continue with background-optimized updates if we have permission
        if (_isUpdating.value) {
            restartLocationUpdatesWithCurrentSettings()
        }
    }

    /**
     * Resume normal location updates (typically when app comes to foreground)
     */
    fun resumeLocationUpdates() {
        // Guard against being called before initialization or when already in desired state
        if (!isFullyInitialized) {
            Timber.tag(tag).w("Cannot resume location updates - LocationService not fully initialized")
            return
        }

        if (!isInBackground) return // Already resumed

        isInBackground = false
        lastBackgroundUpdateTime = 0L
        Timber.tag(tag).i("Location updates resumed for foreground operation")

        // Restart location updates with foreground-optimized settings
        if (_isUpdating.value) {
            restartLocationUpdatesWithCurrentSettings()
        }

        // Also update location immediately to get fresh data
        if (_isUpdating.value && checkLocationPermission()) {
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            val latLng = LatLng(location.latitude, location.longitude)
                            _currentLocation.value = latLng
                            _lastUpdateTime.value = System.currentTimeMillis()
                            handleLocationUpdate(location)
                        }
                    }
            } catch (e: Exception) {
                Timber.tag(tag).e("Error getting location after resume: %s", e.message)
            }
        }
    }

    /**
     * Restart location updates with settings appropriate for current app state
     * Called after app transitions between foreground and background
     */
    private fun restartLocationUpdatesWithCurrentSettings() {
        if (!_isUpdating.value || !checkLocationPermission()) {
            return
        }

        try {
            // Save the current callback reference
            val currentCallback = locationCallback

            // Stop current updates first
            if (currentCallback != null) {
                fusedLocationClient.removeLocationUpdates(currentCallback)
            }

            // Get battery information
            val batteryManager = applicationContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val isCharging = batteryManager.isCharging

            // Determine appropriate settings for current state
            val priority = when {
                isCharging -> Priority.PRIORITY_HIGH_ACCURACY
                isInBackground && batteryLevel < 20 -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
                isInBackground -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
                else -> Priority.PRIORITY_HIGH_ACCURACY
            }

            // Adjust intervals based on battery and app state
            val actualInterval = when {
                isCharging -> defaultUpdateIntervalMs
                isInBackground && batteryLevel < 20 -> lowBatteryUpdateIntervalMs
                isInBackground -> backgroundUpdateIntervalMs
                else -> defaultUpdateIntervalMs
            }

            val minUpdateInterval = when {
                isCharging -> fastUpdateIntervalMs
                isInBackground && batteryLevel < 20 -> lowBatteryUpdateIntervalMs
                isInBackground -> backgroundMinUpdateIntervalMs
                else -> fastUpdateIntervalMs
            }

            // Add distance filter to prevent unnecessary updates
            val minDisplacement = when {
                isCharging -> chargingMinDisplacement
                isInBackground && batteryLevel < 20 -> lowBatteryMinDisplacement
                isInBackground -> backgroundMinDisplacement
                else -> defaultMinDisplacement
            }

            // Location request with both time and distance filtering
            val locationRequest = LocationRequest.Builder(actualInterval)
                .setPriority(priority)
                .setMinUpdateIntervalMillis(minUpdateInterval)
                .setMinUpdateDistanceMeters(minDisplacement)
                .setWaitForAccurateLocation(!isInBackground)
                .setMaxUpdateDelayMillis(actualInterval * 2)
                .build()

            // Register for updates with the same callback
            if (currentCallback != null) {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    currentCallback,
                    Looper.getMainLooper()
                )

                Timber.tag(tag).i(
                    "Location updates restarted with new settings - priority: %s, interval: %sms, minUpdateInterval: %sms, minDisplacement: %sm",
                    priority.toString(), actualInterval.toString(), minUpdateInterval.toString(), minDisplacement.toString()
                )
            }
        } catch (e: Exception) {
            Timber.tag(tag).e("Error restarting location updates: %s", e.message)
        }
    }

    /**
     * Update user location manually
     *
     * @param latLng Location to update to
     */
    fun updateLocation(latLng: LatLng) {
        if (!isFullyInitialized) {
            Timber.tag(tag).w("Cannot update location - LocationService not fully initialized")
            return
        }

        _currentLocation.value = latLng
        _lastUpdateTime.value = System.currentTimeMillis()
        updateLocationInFirebase(latLng.latitude, latLng.longitude)
    }

    /**
     * Update user location manually from Android Location
     *
     * @param location Location to update to
     */
    fun updateLocation(location: Location) {
        if (!isFullyInitialized) {
            Timber.tag(tag).w("Cannot update location - LocationService not fully initialized")
            return
        }

        val latLng = LatLng(location.latitude, location.longitude)
        _currentLocation.value = latLng
        _lastUpdateTime.value = System.currentTimeMillis()
        handleLocationUpdate(location)
    }

    /**
     * Calculate distance between two locations using Haversine formula
     *
     * @param point1 First location
     * @param point2 Second location
     * @return Distance in kilometers
     */
    fun calculateDistance(point1: LatLng, point2: LatLng): Double {
        val r = 6371.0 // Earth radius in kilometers
        val lat1Rad = Math.toRadians(point1.latitude)
        val lon1Rad = Math.toRadians(point1.longitude)
        val lat2Rad = Math.toRadians(point2.latitude)
        val lon2Rad = Math.toRadians(point2.longitude)
        val dLat = lat2Rad - lat1Rad
        val dLon = lon2Rad - lon1Rad
        val a = sin(dLat / 2) * sin(dLat / 2) + cos(lat1Rad) * cos(lat2Rad) * sin(dLon / 2) * sin(
            dLon / 2
        )
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /**
     * Clean up resources when service is no longer needed
     */
    fun cleanup() {
        // Guard against being called before initialization
        if (!isFullyInitialized) {
            Timber.tag(tag).w("Cannot cleanup - LocationService not fully initialized")
            return
        }

        Timber.tag(tag).i("Cleaning up LocationService")

        // Stop location updates
        stopLocationUpdates()

        // Cancel any pending operations for performance optimization
        updateRetryJob?.cancel()
        updateRetryJob = null
        
        // Cancel pending location updates to prevent unnecessary Firebase writes
        pendingLocationUpdate?.cancel()
        pendingLocationUpdate = null
        
        // Cancel permission flow collection jobs to prevent memory leaks
        locationPermissionFlowJob?.cancel()
        locationPermissionFlowJob = null
        backgroundPermissionFlowJob?.cancel()
        backgroundPermissionFlowJob = null

        // Remove user from Firebase directly
        userRef?.removeValue()
        userRef = null
        
        // Reset performance optimization variables
        lastFirebaseUpdateTime = 0L
        lastLocationUpdate = null
        lastBackgroundUpdateTime = 0L
    }
}