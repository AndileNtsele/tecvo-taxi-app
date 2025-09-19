package com.tecvo.taxi
import android.app.Application
import com.tecvo.taxi.BuildConfig
import com.tecvo.taxi.services.AnalyticsManager
import com.tecvo.taxi.services.AppInitManager
import com.tecvo.taxi.services.CrashReportingManager
import com.tecvo.taxi.services.ErrorHandlingService
import com.tecvo.taxi.services.LocationService
import com.tecvo.taxi.services.NotificationService
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import com.tecvo.taxi.model.Location
import com.tecvo.taxi.repository.UserPreferencesRepository
import com.tecvo.taxi.utils.DeviceTypeUtil
import timber.log.Timber
import javax.inject.Inject

/**
 * Main Application class that initializes app-wide components and services.
 *
 * Responsibilities:
 * - Initialize Firebase
 * - Set up crash reporting
 * - Configure analytics
 * - Manage application-level services and resources
 */
@HiltAndroidApp
class TaxiApplication : Application() {
    // Application-level coroutine scope
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    // Service references injected by Hilt
    @Inject
    lateinit var notificationService: NotificationService
    @Inject
    lateinit var locationService: LocationService
    @Inject
    lateinit var errorHandlingService: ErrorHandlingService
    @Inject
    lateinit var analyticsManager: AnalyticsManager
    @Inject
    lateinit var crashReportingManager: CrashReportingManager
    @Inject
    lateinit var appInitManager: AppInitManager
    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    // Track if app is in background
    private var isAppInBackground = false
    
    // Public method to check app background state
    fun isAppInBackground(): Boolean = isAppInBackground

    override fun onCreate() {
        super.onCreate()

        // Configure Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Early tablet detection at application level
        if (DeviceTypeUtil.isTablet(this)) {
            Timber.tag("TaxiApplication").w("TABLET DETECTED: Application started on tablet device - restricting access")
            // Note: MainActivity will handle the user-facing dialog and app closure
            // This is just for comprehensive logging and potential future restrictions
        }

        // Store the instance reference
        instance = this

        // Only initialize Firebase here (critical for Hilt to work)
        try {
            Timber.tag("TaxiApplication").i("Initializing Firebase at application startup")
            FirebaseApp.initializeApp(this)
            Timber.tag("TaxiApplication").i("Firebase initialized successfully")
        } catch (e: Exception) {
            Timber.tag("TaxiApplication").e("Firebase initialization failed: ${e.message}")
        }

        // Everything else will be lazy-initialized later
        Timber.tag("TaxiApplication").d("Application instance created")
    }

    // Add a method to start the initialization sequence - optimized for performance
    fun startInitialization(onComplete: () -> Unit = {}) {
        // Start initialization immediately in background to prevent main thread blocking
        appInitManager.initialize(onComplete)
    }

    override fun onTerminate() {
        super.onTerminate()
        // Cancel all coroutines
        applicationScope.cancel()
        // Clean up repositories to avoid memory leaks
        userPreferencesRepository.cleanup()
        // Clean up service resources
        locationService.cleanup()
        notificationService.cleanup()
        crashReportingManager.cleanup()
        Timber.tag("TaxiApplication").d("Application terminating, cleaning up resources")
    }

    /**
     * Method to update notification preferences - optimized for performance
     */
    fun updateNotificationServicePreferences() {
        // Move heavy operations to background thread to prevent UI blocking
        applicationScope.launch(Dispatchers.IO) {
            try {
                notificationService.updateNotificationPreferences()
                // Track notification preference changes in background
                analyticsManager.logEvent("notification_preferences_updated")
            } catch (e: Exception) {
                Timber.tag("TaxiApplication").e("Error updating notification preferences: ${e.message}")
            }
        }
    }

    /**
     * Method to cleanup when app is closing
     */
    fun cleanupNotificationService() {
        notificationService.cleanup()
        Timber.tag("TaxiApplication").d("Notification service cleaned up")
    }

    /**
     * Called when app goes to background - optimized for performance
     */
    fun setAppInBackground(inBackground: Boolean) {
        Timber.tag("TaxiApplication")
            .d("App ${if (inBackground) "entered background" else "came to foreground"}")
        
        isAppInBackground = inBackground
        
        // Move heavy operations to background thread to prevent main thread blocking
        applicationScope.launch(Dispatchers.IO) {
            try {
                // Track app state in analytics in background
                analyticsManager.logEvent(if(inBackground) "app_background" else "app_foreground")
                
                // Handle location updates based on app state
                if (inBackground) {
                    locationService.pauseLocationUpdates()
                } else {
                    locationService.resumeLocationUpdates()
                }
            } catch (e: Exception) {
                Timber.tag("TaxiApplication").e("Error handling app state change: ${e.message}")
            }
        }
    }

    /**
     * Companion object for singleton instance management
     */
    companion object {
        @Volatile private var instance: TaxiApplication? = null
        fun getInstance(): TaxiApplication {
            return instance ?: throw IllegalStateException("TaxiApplication not initialized")
        }
    }

    /**
     * Update user location from a Location object - optimized for performance
     */
    fun updateUserLocation(location: Location) {
        Timber.tag("TaxiApplication")
            .d("Updating user location: lat=${location.latitude}, lng=${location.longitude}")
        
        // Move analytics tracking to background to prevent main thread blocking
        applicationScope.launch(Dispatchers.IO) {
            try {
                analyticsManager.logEvent("location_updated", mapOf(
                    "latitude" to location.latitude,
                    "longitude" to location.longitude
                ))
            } catch (e: Exception) {
                Timber.tag("TaxiApplication").e("Error tracking location update: ${e.message}")
            }
        }
    }
}