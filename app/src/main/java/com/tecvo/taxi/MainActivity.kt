@file:OptIn(ExperimentalMaterial3Api::class)
package com.tecvo.taxi

import com.tecvo.taxi.Routes
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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
import com.tecvo.taxi.utils.DeviceTypeUtil
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
    @Inject lateinit var permissionManager: PermissionManager
    @Inject lateinit var navHostManager: NavHostManager

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.tag(TAG).i("Application startup: Initializing main activity")
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Check for tablet device and block if detected
        if (DeviceTypeUtil.isTablet(this)) {
            Timber.tag(TAG).w("Tablet device detected - blocking app access")
            showTabletRestrictionDialog()
            return // Stop further initialization
        }

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
                // Track app open event and user after initialization
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        analyticsManager.logEvent(FirebaseAnalytics.Event.APP_OPEN)
                        // Track current user if available
                        FirebaseAuth.getInstance().currentUser?.let { user ->
                            analyticsManager.setUserId(user.uid)
                        }
                        Timber.tag(TAG).i("Application startup: Initialization complete")
                    } catch (e: Exception) {
                        Timber.tag(TAG).e("Error logging analytics events: ${e.message}")
                    }
                }
            })
        }
    }

    /**
     * Handles the permission flow after user registration
     */
    fun handlePostRegistrationPermissions(navController: NavController) {
        Timber.tag(TAG).i("User Flow: Starting post-registration permission flow")

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

        // Optimized post-registration flow with guaranteed cleanup
        lifecycleScope.launch(Dispatchers.IO) {
            var dialogDismissed = false

            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                Timber.tag(TAG).d("Post-registration: Current user ${if (currentUser != null) "found" else "null"}")

                if (currentUser != null) {
                    val userId = currentUser.uid

                    // Run analytics and service preparation in parallel
                    val analyticsJob = async {
                        try {
                            analyticsManager.setUserId(userId)
                            Timber.tag(TAG).d("Post-registration: Analytics setup complete")
                        } catch (e: Exception) {
                            Timber.tag(TAG).e("Analytics setup failed: ${e.message}")
                        }
                    }
                    val servicesJob = async {
                        try {
                            locationServiceManager.prepareBasicServices(userId)
                            Timber.tag(TAG).d("Post-registration: Services setup complete")
                        } catch (e: Exception) {
                            Timber.tag(TAG).e("Services setup failed: ${e.message}")
                        }
                    }

                    // Wait for both to complete
                    analyticsJob.await()
                    servicesJob.await()
                }

                // Always navigate to home on main thread and dismiss dialog
                withContext(Dispatchers.Main) {
                    Timber.tag(TAG).i("Post-registration: Completing setup and navigating to home")
                    dialogManager.hideLoadingOverlay(this@MainActivity)
                    dialogDismissed = true
                    safeNavigateToHome(navController)
                }

            } catch (e: Exception) {
                Timber.tag(TAG).e("Error in post-registration flow: ${e.message}")
                withContext(Dispatchers.Main) {
                    if (!dialogDismissed) {
                        dialogManager.hideLoadingOverlay(this@MainActivity)
                        dialogDismissed = true
                    }
                    safeNavigateToHome(navController)
                }
            } finally {
                // Final safety check to ensure dialog is always dismissed
                if (!dialogDismissed) {
                    withContext(Dispatchers.Main) {
                        Timber.tag(TAG).w("Post-registration: Final cleanup - force dismissing dialog")
                        dialogManager.hideLoadingOverlay(this@MainActivity)
                    }
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

    /**
     * Shows a dialog informing the user that the app is only available for mobile phones
     * and closes the app after the user acknowledges.
     */
    private fun showTabletRestrictionDialog() {
        val deviceInfo = DeviceTypeUtil.getDeviceInfo(this)
        Timber.tag(TAG).w("Device info for blocked tablet: $deviceInfo")

        AlertDialog.Builder(this)
            .setTitle("Phone-Only App")
            .setMessage(
                "This app is designed specifically for mobile phones to provide " +
                "real-time taxi visibility for SA drivers and commuters.\n\n" +
                "Please install and use this app on a mobile phone for the best experience.\n\n" +
                "Tablets are not supported due to the mobile-first nature of taxi operations."
            )
            .setPositiveButton("OK") { _, _ ->
                Timber.tag(TAG).i("User acknowledged tablet restriction, closing app")
                finishAffinity() // Close the app completely
            }
            .setCancelable(false) // Prevent dismissing without action
            .show()
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