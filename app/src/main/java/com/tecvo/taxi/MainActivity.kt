@file:OptIn(ExperimentalMaterial3Api::class)
package com.tecvo.taxi

import com.tecvo.taxi.Routes
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.tecvo.taxi.constants.EventConstants
import com.tecvo.taxi.navigation.AppNavigation
import com.tecvo.taxi.navigation.NavHostManager
import com.tecvo.taxi.navigation.safeNavigateToHome
import com.tecvo.taxi.permissions.PermissionManager
import com.tecvo.taxi.services.AnalyticsManager
import com.tecvo.taxi.services.CrashReportingManager
import com.tecvo.taxi.services.LocationServiceManager
import com.tecvo.taxi.services.NotificationService
import com.tecvo.taxi.services.ServiceInitializationManager
import com.tecvo.taxi.ui.dialog.DialogManager
import com.tecvo.taxi.ui.theme.TaxiTheme
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * MainActivity serves as the entry point of the Taxi application.
 * It focuses on coordinating between services rather than implementing details directly.
 * Uses managers for specific functionality domains like location, notifications, and initialization.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // All dependencies are properly injected using Hilt
    @Inject lateinit var dialogManager: DialogManager
    @Inject lateinit var serviceInitManager: ServiceInitializationManager
    @Inject lateinit var locationServiceManager: LocationServiceManager
    @Inject lateinit var notificationService: NotificationService
    @Inject lateinit var analyticsManager: AnalyticsManager
    @Inject lateinit var crashReportingManager: CrashReportingManager
    //@Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var permissionManager: PermissionManager
    @Inject lateinit var navHostManager: NavHostManager

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.tag(TAG).i("Application startup: Initializing main activity")
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Set the content of the activity first to show UI quickly
        setContent {
            Timber.tag(TAG).i("UI: Setting up navigation structure")
            TaxiTheme {
                AppNavigation(
                    ::handlePostRegistrationPermissions,
                    analyticsManager,
                    crashReportingManager,
                    navHostManager
                )
            }
        }

        // Start application initialization in background to prevent UI blocking
        lifecycleScope.launch(Dispatchers.IO) {
            serviceInitManager.startInitialization(onComplete = {
                // This will run when initialization is complete
                lifecycleScope.launch(Dispatchers.Main) {
                    // Track app open event after initialization - moved to background dispatcher
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            analyticsManager.logEvent(FirebaseAnalytics.Event.APP_OPEN)
                            // Track current user if available
                            FirebaseAuth.getInstance().currentUser?.let { user ->
                                analyticsManager.setUserId(user.uid)
                            }
                        } catch (e: Exception) {
                            Timber.tag(TAG).e("Error logging analytics events: ${e.message}")
                        }
                    }
                    Timber.tag(TAG).i("Application startup: Initialization complete")
                }
            })
        }
    }

    /**
     * Handles the permission flow after user registration
     */
    fun handlePostRegistrationPermissions(navController: NavController) {
        // Show loading indicator immediately on main thread
        dialogManager.showLoadingOverlay(this, "Setting up your account...")

        // Move analytics tracking to background to prevent main thread blocking
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                analyticsManager.logEvent("registration_completed")
            } catch (e: Exception) {
                Timber.tag(TAG).e("Error tracking registration completion: ${e.message}")
            }
        }

        // Safety timeout to prevent UI freeze - reduced timeout for better UX
        lifecycleScope.launch(Dispatchers.Main) {
            kotlinx.coroutines.delay(10000) // Reduced from 15 to 10 seconds
            dialogManager.hideLoadingOverlay(this@MainActivity)
            try {
                // Use safe navigation method
                safeNavigateToHome(navController)
            } catch (e: Exception) {
                Timber.tag(TAG).e("Navigation error in safety timeout: ${e.message}")
                dialogManager.showToast(this@MainActivity, "Something went wrong. Please try again.")
                // Track navigation error in background
                lifecycleScope.launch(Dispatchers.IO) {
                    analyticsManager.logEvent("navigation_error", mapOf(
                        "destination" to "home",
                        "error" to (e.message ?: "Unknown error")
                    ))
                }
            }
        }

        // Optimized post-registration flow
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    val userId = currentUser.uid
                    
                    // Run analytics and service preparation in parallel
                    val analyticsJob = async { analyticsManager.setUserId(userId) }
                    val servicesJob = async { locationServiceManager.prepareBasicServices(userId) }
                    
                    // Wait for both to complete
                    analyticsJob.await()
                    servicesJob.await()

                    withContext(Dispatchers.Main) {
                        dialogManager.hideLoadingOverlay(this@MainActivity)
                        safeNavigateToHome(navController)
                    }
                } else {
                    // Handle case where user is null
                    withContext(Dispatchers.Main) {
                        dialogManager.hideLoadingOverlay(this@MainActivity)
                        safeNavigateToHome(navController)
                    }
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e("Error in post-registration flow: ${e.message}")
                withContext(Dispatchers.Main) {
                    dialogManager.hideLoadingOverlay(this@MainActivity)
                    safeNavigateToHome(navController)
                }
            }
        }
    }

    /**
     * Safe navigation helper to prevent navigation stack issues
     * Now uses the centralized safe navigation extension
     */
    private fun safeNavigateToHome(navController: NavController) {
        navController.safeNavigateToHome()
    }

    override fun onPause() {
        super.onPause()
        Timber.tag(TAG).i("Lifecycle: MainActivity is being paused")
        // Track app background
        analyticsManager.logEvent("app_background")

        try {
            (application as TaxiApplication).setAppInBackground(true)
        } catch (e: Exception) {
            Timber.tag(TAG).e("Error in onPause: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        Timber.tag(TAG).i("Lifecycle: MainActivity is being resumed")
        // Track app foreground
        analyticsManager.logEvent("app_foreground")

        try {
            (application as TaxiApplication).setAppInBackground(false)
        } catch (e: Exception) {
            Timber.tag(TAG).e("Error in onResume: ${e.message}")
        }
    }

    override fun onDestroy() {
        Timber.tag(TAG).i("Lifecycle: MainActivity is being destroyed, cleaning up resources")
        // Track app close event
        analyticsManager.logEvent(EventConstants.APP_CLOSE)

        // Clean up resources
        locationServiceManager.cleanup()
        serviceInitManager.cleanup()

        super.onDestroy()

        // Clean up notification services
        try {
            (application as TaxiApplication).cleanupNotificationService()
        } catch (e: Exception) {
            Timber.tag(TAG).e("Error during cleanup: ${e.message}")
        }
    }
}