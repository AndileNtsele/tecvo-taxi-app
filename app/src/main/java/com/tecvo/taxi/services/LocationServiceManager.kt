package com.tecvo.taxi.services
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.tecvo.taxi.permissions.PermissionManager
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationServiceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    val locationService: LocationService,
    private val notificationService: NotificationService,
    private val permissionManager: PermissionManager
) {
    private val tag = "LocationServiceManager"
    // Coroutine scope for background operations
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    // State management
    private val isInitialized = AtomicBoolean(false)
    private var userId: String? = null
    private var userType: String = "passenger" // Default user type
    private var currentDestination: String = "town" // Default destination
    /**
     * Callback interfaces
     */
    interface InitializationCallback {
        fun onInitialized()
        fun onInitializationFailed(error: Exception)
    }
    companion object {
        @Volatile
        private var instance: LocationServiceManager? = null
        // For backward compatibility during migration
        fun getInstance(): LocationServiceManager {
            return instance ?: synchronized(this) {
                instance ?: throw IllegalStateException(
                    "LocationServiceManager has not been initialized via Hilt injection."
                )
            }
        }
    }
    init {
        // Store instance for backward compatibility
        instance = this
    }
    /**
     * Initializes the LocationServiceManager and configures the LocationService
     */
    fun initialize(callback: InitializationCallback? = null) {
        if (isInitialized.get()) {
            Timber.tag(tag).i("LocationServiceManager already initialized")
            callback?.onInitialized()
            return
        }
        serviceScope.launch {
            try {
                // Check if LocationService is initialized
                if (!LocationService.isInitialized()) {
                    // Wait for LocationService to initialize with timeout
                    var retryCount = 0
                    val maxRetries = 5
                    var initialized = false

                    while (!initialized && retryCount < maxRetries) {
                        kotlinx.coroutines.delay(500)
                        initialized = LocationService.isInitialized()
                        retryCount++
                    }
                    if (!initialized) {
                        throw IllegalStateException("LocationService failed to initialize")
                    }
                }
                // Check and get current user ID if available
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    userId = currentUser.uid
                }
                isInitialized.set(true)
                Timber.tag(tag).i("LocationServiceManager initialized successfully")
                withContext(Dispatchers.Main) {
                    callback?.onInitialized()
                }
            } catch (e: Exception) {
                Timber.tag(tag).e("Error initializing LocationServiceManager: ${e.message}")
                withContext(Dispatchers.Main) {
                    callback?.onInitializationFailed(e)
                }
            }
        }
    }
    /**
     * Check if location permission is granted
     */
    fun checkLocationPermission(): Boolean {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        Timber.tag(tag)
            .d("Location permission status: ${if (hasPermission) "granted" else "not granted"}")
        return hasPermission
    }

    /**
     * Request location permission using the provided launcher
     */
    fun requestLocationPermission(
        activity: Activity,
        permissionLauncher: ActivityResultLauncher<String>,
        onPermissionGranted: () -> Unit = {},
        onPermissionDenied: () -> Unit = {}
    ) {
        // Use basic permission manager
        permissionManager.requestLocationPermission(
            activity = activity,
            permissionLauncher = permissionLauncher,
            onResult = { granted ->
                if (granted) {
                    onPermissionGranted()
                } else {
                    onPermissionDenied()
                }
            }
        )
    }
    /**
     * Initialize location service with permission
     */
    suspend fun initializeLocationServiceWithPermission(userId: String): Pair<LatLng, String> {
        Timber.tag(tag).i("Initializing location service with permission for user $userId")
        if (!checkLocationPermission()) {
            throw SecurityException("Location permission not granted")
        }
        // Fetch user destination
        val (destination, userType) = fetchUserDestinationSuspend(userId)
        // Set user info in LocationService
        setUserInfo(userId, userType, destination)
        // Get location (with timeout)
        val location = withTimeoutOrNull(5000) { // 5 second timeout
            locationService.getLastLocationSuspend()
        } ?: LatLng(0.0, 0.0) // Default location if timeout
        Timber.tag(tag)
            .i("Location service initialized with location: ${location.latitude}, ${location.longitude}")
        return Pair(location, destination)
    }
    /**
     * Fetch user destination from Firebase as a suspend function
     */
    private suspend fun fetchUserDestinationSuspend(userId: String): Pair<String, String> {
        Timber.tag(tag).i("Fetching user destination from Firebase for user $userId")
        val paths = arrayOf(
            "passengers/town/$userId",
            "passengers/local/$userId",
            "drivers/town/$userId",
            "drivers/local/$userId"
        )
        val database = FirebaseDatabase.getInstance()
        for (path in paths) {
            try {
                val snapshot = database.reference.child(path).get().await()
                if (snapshot.exists()) {
                    val destination = if (path.contains("/town/")) "town" else "local"
                    val userType = if (path.contains("/drivers/")) "driver" else "passenger"
                    Timber.tag(tag)
                        .i("Found user destination in Firebase: $destination, userType: $userType")
                    return Pair(destination, userType)
                }
            } catch (e: Exception) {
                Timber.tag(tag).e("Error accessing Firebase path $path: ${e.message}")
                // Continue to next path
            }
        }
        Timber.tag(tag).w("No destination found for user $userId, defaulting to 'town'")
        return Pair("town", "passenger")
    }
    /**
     * Set user info for location updates
     */
    fun setUserInfo(userId: String, userType: String, destination: String) {
        this.userId = userId
        this.userType = userType
        this.currentDestination = destination
        // Also update LocationService
        locationService.setUserInfo(userId, userType, destination)
        Timber.tag(tag)
            .i("User info set: userId=$userId, userType=$userType, destination=$destination")
    }
    /**
     * Prepare basic services for post-registration
     */
    fun prepareBasicServices(userId: String) {
        try {
            // Fetch minimal required data
            val defaultDestination = "town"
            // Set basic configuration in services
            if (LocationService.isInitialized()) {
                LocationService.getInstance().setUserInfo(userId, "passenger", defaultDestination)
            }
            Timber.tag(tag).i("Basic services prepared for post-registration")
        } catch (e: Exception) {
            Timber.tag(tag).e("Error preparing basic services: ${e.message}")
        }
    }
    /**
     * Start location and notification monitoring
     */
    fun startLocationAndMonitoring(
        userId: String,
        location: LatLng,
        destination: String
    ) {
        Timber.tag(tag)
            .i("Starting location monitoring for user $userId at ${location.latitude}, ${location.longitude}, destination: $destination")
        // Update current user info
        this.userId = userId
        this.currentDestination = destination
        // Initialize app-level notification service in background
        serviceScope.launch {
            try {
                // Start notification monitoring
                notificationService.startMonitoring(userId, location, destination)
                Timber.tag(tag).i("Location monitoring initialized successfully")
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "Error initializing notification service: ${e.message}")
            }
        }
    }
    /**
     * Clean up resources
     */
    fun cleanup() {
        Timber.tag(tag).i("Cleaning up LocationServiceManager")
        // Stop location updates
        locationService.stopLocationUpdates()
        // Clean up notification services
        notificationService.cleanup()
    }
}