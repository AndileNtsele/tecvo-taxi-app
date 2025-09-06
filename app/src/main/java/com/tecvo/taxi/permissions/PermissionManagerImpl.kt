package com.tecvo.taxi.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tecvo.taxi.R
import com.tecvo.taxi.services.AnalyticsManager
import com.tecvo.taxi.services.ErrorHandlingService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

// Define interface first for better testing
interface PermissionManager {
    val locationPermissionFlow: StateFlow<Boolean>
    val backgroundLocationPermissionFlow: StateFlow<Boolean>
    val notificationPermissionFlow: StateFlow<Boolean>

    fun isLocationPermissionGranted(): Boolean
    fun isBackgroundLocationPermissionGranted(): Boolean
    fun isNotificationPermissionGranted(): Boolean
    fun requestLocationPermission(activity: Activity, permissionLauncher: ActivityResultLauncher<String>, onResult: (Boolean) -> Unit)
    fun requestNotificationPermission(activity: Activity, permissionLauncher: ActivityResultLauncher<String>, onResult: (Boolean) -> Unit)
    fun requestBackgroundLocationPermission(activity: Activity, permissionLauncher: ActivityResultLauncher<String>, onResult: (Boolean) -> Unit)
    fun openAppSettings(activity: Activity)
    fun updatePermissionState(permission: String, isGranted: Boolean)
}

@Singleton
class PermissionManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analyticsManager: AnalyticsManager,
    private val errorHandlingService: ErrorHandlingService
) : PermissionManager {

    private val tag = "PermissionManager"

    // State flows for reactive state management
    private val _locationPermissionFlow = MutableStateFlow(false)
    override val locationPermissionFlow = _locationPermissionFlow.asStateFlow()

    private val _backgroundLocationPermissionFlow = MutableStateFlow(false)
    override val backgroundLocationPermissionFlow = _backgroundLocationPermissionFlow.asStateFlow()

    private val _notificationPermissionFlow = MutableStateFlow(false)
    override val notificationPermissionFlow = _notificationPermissionFlow.asStateFlow()

    init {
        // Initialize permission states
        _locationPermissionFlow.value = isLocationPermissionGranted()
        _backgroundLocationPermissionFlow.value = isBackgroundLocationPermissionGranted()
        _notificationPermissionFlow.value = isNotificationPermissionGranted()

        Timber.tag(tag).d(
            "📋 App permissions status: Location tracking ${if (_locationPermissionFlow.value) "enabled" else "disabled"}, Background tracking ${if (_backgroundLocationPermissionFlow.value) "enabled" else "disabled"}, Notifications ${if (_notificationPermissionFlow.value) "enabled" else "disabled"}"
        )
    }

    override fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    override fun isBackgroundLocationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // On older Android versions, location permission implies background
            isLocationPermissionGranted()
        }
    }

    override fun isNotificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Not required on older Android versions
            true
        }
    }

    override fun requestLocationPermission(
        activity: Activity,
        permissionLauncher: ActivityResultLauncher<String>,
        onResult: (Boolean) -> Unit
    ) {
        // If already granted, just return
        if (isLocationPermissionGranted()) {
            Timber.tag(tag).d("✅ Location permission already granted")
            onResult(true)
            return
        }

        // Check if we should show rationale
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            Timber.tag(tag).d("💬 Showing location permission explanation to user")
            // Show rationale dialog
            showRationaleDialog(
                activity = activity,
                title = activity.getString(R.string.location_permission_title),
                message = activity.getString(R.string.location_permission_rationale),
                onProceed = {
                    Timber.tag(tag).d("✅ User agreed to grant location permission")
                    analyticsManager.logEvent("permission_rationale_accepted", mapOf(
                        "permission" to "location"
                    ))
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                },
                onDeny = {
                    Timber.tag(tag).d("❌ User declined location permission after explanation")
                    analyticsManager.logEvent("permission_rationale_declined", mapOf(
                        "permission" to "location"
                    ))
                    onResult(false)
                }
            )
        } else {
            Timber.tag(tag).d("📱 Requesting location permission from user")
            // Request directly
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun requestNotificationPermission(
        activity: Activity,
        permissionLauncher: ActivityResultLauncher<String>,
        onResult: (Boolean) -> Unit
    ) {
        // Only relevant for Android 13+
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Timber.tag(tag).d("✅ Notification permission not needed on this Android version")
            onResult(true)
            return
        }

        // If already granted, just return
        if (isNotificationPermissionGranted()) {
            Timber.tag(tag).d("✅ Notification permission already granted")
            onResult(true)
            return
        }

        // Check if we should show rationale
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            )
        ) {
            Timber.tag(tag).d("💬 Showing notification permission explanation to user")
            // Show rationale dialog
            showRationaleDialog(
                activity = activity,
                title = "Notification Permission",
                message = "This app needs notification permission to alert you when drivers or passengers are nearby.",
                onProceed = {
                    Timber.tag(tag).d("✅ User agreed to grant notification permission")
                    analyticsManager.logEvent("permission_rationale_accepted", mapOf(
                        "permission" to "notification"
                    ))
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                },
                onDeny = {
                    Timber.tag(tag).d("❌ User declined notification permission after explanation")
                    analyticsManager.logEvent("permission_rationale_declined", mapOf(
                        "permission" to "notification"
                    ))
                    onResult(false)
                }
            )
        } else {
            Timber.tag(tag).d("📱 Requesting notification permission from user")
            // Request directly
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun requestBackgroundLocationPermission(
        activity: Activity,
        permissionLauncher: ActivityResultLauncher<String>,
        onResult: (Boolean) -> Unit
    ) {
        // Only relevant for Android 10+
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Timber.tag(tag).d("✅ Background location permission automatically granted on this Android version")
            onResult(true) // Granted automatically on older versions
            return
        }

        // First check if foreground permission is granted
        if (!isLocationPermissionGranted()) {
            Timber.tag(tag).w("⚠️ Cannot enable background location tracking - basic location permission is required first")
            onResult(false)
            return
        }

        // If already granted, just return
        if (isBackgroundLocationPermissionGranted()) {
            Timber.tag(tag).d("✅ Background location permission already granted")
            onResult(true)
            return
        }

        Timber.tag(tag).d("🔄 Redirecting user to settings to enable background location tracking")
        // For background location, we need to redirect to settings
        showBackgroundLocationSettingsDialog(activity, onResult)
    }

    private fun showBackgroundLocationSettingsDialog(
        activity: Activity,
        onResult: (Boolean) -> Unit
    ) {
        val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(activity)
        dialogBuilder
            .setTitle("Background Location Access")
            .setMessage("To track your location when the app is closed, please:\n\n1. Tap 'Open Settings'\n2. Select 'Allow all the time'\n3. Return to the app")
            .setPositiveButton("Open Settings") { dialog, _ ->
                dialog.dismiss()
                try {
                    Timber.tag(tag).d("⚙️ Opening app settings for background location permission")
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", activity.packageName, null)
                    }
                    activity.startActivity(intent)

                    // Check permission status after a delay
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        val isGranted = isBackgroundLocationPermissionGranted()
                        onResult(isGranted)

                        if (isGranted) {
                            Timber.tag(tag).d("🎉 Background location tracking successfully enabled")
                        } else {
                            Timber.tag(tag).d("ℹ️ Background location tracking remains disabled - user chose not to enable it")
                        }

                        val message = if (isGranted) {
                            "Background location enabled! We can now track your location when the app is closed."
                        } else {
                            "Background location not enabled. Location tracking will stop when app is closed."
                        }

                        android.widget.Toast.makeText(activity, message, android.widget.Toast.LENGTH_LONG).show()
                    }, 3000)

                } catch (e: Exception) {
                    Timber.tag(tag).e("❌ Failed to open app settings: ${e.message}")
                    onResult(false)
                }
            }
            .setNegativeButton("Skip") { dialog, _ ->
                dialog.dismiss()
                Timber.tag(tag).d("ℹ️ User chose to skip background location permission")
                onResult(false)
                android.widget.Toast.makeText(
                    activity,
                    "Background location denied. Location tracking will stop when app is closed.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    override fun openAppSettings(activity: Activity) {
        try {
            Timber.tag(tag).d("⚙️ Opening app settings page for user")
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", activity.packageName, null)
            }
            activity.startActivity(intent)
            analyticsManager.logEvent("open_app_settings")
        } catch (e: Exception) {
            Timber.tag(tag).e("❌ Could not open app settings page: %s", e.message)
            errorHandlingService.logError(
                ErrorHandlingService.AppError.ValidationError(
                    message = "Could not open app settings: ${e.message}"
                )
            )
        }
    }

    override fun updatePermissionState(permission: String, isGranted: Boolean) {
        when (permission) {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION -> {
                _locationPermissionFlow.value = isGranted

                // Update background flow for pre-Android Q
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    _backgroundLocationPermissionFlow.value = isGranted
                }

                if (isGranted) {
                    Timber.tag(tag).d("✅ Location tracking permission granted - app can now access user location")
                } else {
                    Timber.tag(tag).d("❌ Location tracking permission denied - app cannot access user location")
                }

                analyticsManager.logEvent(
                    if (isGranted) "permission_granted" else "permission_denied",
                    mapOf("permission" to "location")
                )
            }
            Manifest.permission.ACCESS_BACKGROUND_LOCATION -> {
                _backgroundLocationPermissionFlow.value = isGranted

                if (isGranted) {
                    Timber.tag(tag).d("✅ Background location tracking enabled - app can track location when closed")
                } else {
                    Timber.tag(tag).d("❌ Background location tracking disabled - location stops when app is closed")
                }

                analyticsManager.logEvent(
                    if (isGranted) "permission_granted" else "permission_denied",
                    mapOf("permission" to "background_location")
                )
            }
            Manifest.permission.POST_NOTIFICATIONS -> {
                _notificationPermissionFlow.value = isGranted

                if (isGranted) {
                    Timber.tag(tag).d("✅ Notification permission granted - app can send alerts to user")
                } else {
                    Timber.tag(tag).d("❌ Notification permission denied - app cannot send alerts to user")
                }

                analyticsManager.logEvent(
                    if (isGranted) "permission_granted" else "permission_denied",
                    mapOf("permission" to "notification")
                )
            }
        }
    }

    private fun showRationaleDialog(
        activity: Activity,
        title: String,
        message: String,
        onProceed: () -> Unit,
        onDeny: () -> Unit
    ) {
        // Remove the problematic theme reference
        val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(activity)
        dialogBuilder
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.proceed) { dialog, _ ->
                dialog.dismiss()
                onProceed()
            }
            .setNegativeButton(R.string.not_now) { dialog, _ ->
                dialog.dismiss()
                onDeny()
            }
            .setCancelable(false)
            .create()
            .show()
    }
}