package com.tecvo.taxi.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch

/**
 * A notification bell button that handles notification permission and toggles notifications.
 * Requests permission on first click, then toggles notifications on subsequent clicks.
 *
 * @param userType The current user type ("driver" or "passenger")
 * @param isNotificationEnabled Whether notifications are currently enabled
 * @param onToggle Callback function when the notification setting is toggled
 * @param currentRadius The current search radius in kilometers
 * @param permissionLauncher Launcher for requesting notification permission
 */
@Composable
fun NotificationBellButton(
    userType: String,
    isNotificationEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    currentRadius: Float,
    permissionLauncher: ActivityResultLauncher<String>
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // The opposite role type for notifications
    val oppositeRole = if (userType == "driver") "passenger" else "driver"

    // Define a strong blue color for enabled state
    val enabledBlueColor = Color(0xFF1976D2) // Strong blue color

    // Get shared preferences to check if permission was previously granted
    val sharedPrefs = context.getSharedPreferences("taxi_app_prefs", Context.MODE_PRIVATE)

    // Function to check if notification permission is granted
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // For versions below Android 13, notification permission is not required
            true
        }
    }

    // Function to check if we should request permission (hasn't been asked before)
    fun shouldRequestPermission(): Boolean {
        return !sharedPrefs.getBoolean("notification_permission_requested", false)
    }

    // Function to mark permission as requested
    fun markPermissionRequested() {
        sharedPrefs.edit().putBoolean("notification_permission_requested", true).apply()
    }

    // FIXED: Determine visual state - bell should be blue if notifications are enabled AND permission is granted
    val shouldShowAsEnabled = isNotificationEnabled && hasNotificationPermission()

    // Bell button click handler
    Surface(
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
            .clickable {
                // Check if we need to request notification permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (!hasNotificationPermission() && shouldRequestPermission()) {
                        // Request notification permission and enable notifications
                        markPermissionRequested()
                        // FIXED: Enable notifications when requesting permission
                        onToggle(true)
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        return@clickable
                    }
                }

                // If permission is granted or not needed, toggle notification state
                val newState = !isNotificationEnabled
                onToggle(newState)

                // Show a quick toast message when toggled
                coroutineScope.launch {
                    val message = if (newState) {
                        if (hasNotificationPermission()) {
                            "Notifications for available ${oppositeRole}s enabled"
                        } else {
                            "Notifications disabled - permission not granted"
                        }
                    } else {
                        "Notifications for available ${oppositeRole}s disabled"
                    }

                    android.widget.Toast.makeText(
                        context,
                        message,
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            },
        color = Color.White.copy(alpha = 0.7f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (shouldShowAsEnabled)
                    Icons.Default.NotificationsActive
                else
                    Icons.Default.Notifications,
                contentDescription = if (shouldShowAsEnabled)
                    "Disable notifications"
                else
                    "Enable notifications",
                tint = if (shouldShowAsEnabled) enabledBlueColor else Color(0xFF333333)
            )
        }
    }
}