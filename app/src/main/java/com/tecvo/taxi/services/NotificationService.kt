package com.tecvo.taxi.services
import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import com.tecvo.taxi.R
import com.tecvo.taxi.constants.AppConstants
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import com.tecvo.taxi.BuildConfig

/**
 * Unified service for handling all notification-related functionality.
 * Manages notification permissions, preferences, and all types of app notifications.
 *
 * Notifications will only be triggered once for each entity that enters the radius.
 */
@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationService: LocationService, // Inject LocationService directly
    private val notificationStateManager: NotificationStateManager // Add state manager
) {
    private val tag = "NotificationService"
    // Channel ID for notifications
    private val channelId = AppConstants.NOTIFICATION_CHANNEL_ID

    // Default radius for notifications
    private val defaultNotificationRadiusKm = 0.5f
    // State tracking
    private var isInMapScreen = false
    private var currentUserLocation: LatLng? = null
    private var userId: String? = null
    private var currentDestination: String = "town" // Default destination
    private var userType: String = "passenger" // Default role
    // Track already notified entities to avoid duplicate notifications
    private val notifiedEntities = mutableSetOf<String>()
    // Firebase listener for entity monitoring
    private var entityListener: ValueEventListener? = null
    
    // Performance Optimization: Cache notification channel creation
    private var isChannelCreated = false
    private var lastMonitoringPath: String? = null
    // Coroutine scope for background operations
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    // Shared preferences reference
    private val sharedPrefs = context.getSharedPreferences("taxi_app_prefs", Context.MODE_PRIVATE)
    companion object {
        @Volatile
        private var instance: NotificationService? = null
        // This method provides backwards compatibility during migration
        fun getInstance():
                NotificationService {
            return instance ?: synchronized(this) {
// During migration, we'll return the instance if it's already created via dependency injection
                instance ?: throw IllegalStateException(
                    "NotificationService has not been initialized. Make sure you're using Hilt injection."
                )
            }
        }
    }
    init {
        if (BuildConfig.DEBUG) {
            Timber.tag(tag).d("Initializing NotificationService")
        }
        createNotificationChannel()
        // Store instance reference for backward compatibility
        instance = this
    }
    /**
     * Creates the notification channel required for Android 8.0 (Oreo) and higher
     */
    private fun createNotificationChannel() {
        // Performance Optimization: Only create channel once
        if (isChannelCreated) return
        
        val name = AppConstants.NOTIFICATION_CHANNEL_NAME
        val descriptionText = AppConstants.NOTIFICATION_CHANNEL_DESC
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 250, 500)
            setShowBadge(true)
        }
        try {
            // Register the channel with the system
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            isChannelCreated = true
            
            if (BuildConfig.DEBUG) {
                Timber.tag(tag).d("Notification channel created with importance: %d", importance)
            }
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Failed to create notification channel")
        }
    }

    /**
     * Creates all notification channels needed by the app
     * This is called during app initialization
     */
    fun createNotificationChannels() {
        Timber.tag(tag).d("Creating notification channels")
        // Call the existing createNotificationChannel() method
        createNotificationChannel()
        // If you need more channels in the future, create them here
    }

    /**
     * Starts monitoring for nearby entities
     * Will only notify once for each entity that enters the user's radius
     * Uses state management to prevent redundant operations
     *
     * @param userId User's unique identifier
     * @param location Initial user location
     * @param destination User's destination ("town" or "local")
     * @param userType Optional user type, defaults to "passenger"
     */
    fun startMonitoring(
        userId: String,
        location: LatLng,
        destination: String,
        userType: String = "passenger"
    ) {
        // Use state manager to prevent redundant operations
        if (!notificationStateManager.requestStartMonitoring(userId, userType, destination)) {
            Timber.tag(tag).d("Skipping monitoring start - already active or duplicate request")
            return
        }
        
        try {
            this.userId = userId
            this.currentUserLocation = location
            this.currentDestination = destination
            this.userType = userType
            
            // Clear previous notifications for a fresh start
            notifiedEntities.clear()
            
            // Start monitoring if notifications are enabled
            if (shouldShowNotifications()) {
                monitorSecondaryEntities()
                // Mark as successfully started
                notificationStateManager.markMonitoringStarted(userId, userType, destination)
            } else {
                // If notifications disabled, mark as stopped
                notificationStateManager.markMonitoringStopped()
            }
            
            if (BuildConfig.DEBUG) {
                Timber.tag(tag).d("Started monitoring for user %s (%s) with destination: %s", userId, userType, destination)
            }
        } catch (e: Exception) {
            Timber.tag(tag).e("Error starting monitoring: ${e.message}")
            notificationStateManager.resetState()
        }
    }
    /**
     * Primary monitoring function that watches for entities entering the radius
     * Notifies only once per entity when they enter the radius
     * Enhanced with state management to prevent redundant operations
     */
    private fun monitorSecondaryEntities() {
        val userId = this.userId ?: return
        val oppositeType = if (userType == "driver") "passenger" else "driver"
        val path = "${oppositeType}s/$currentDestination"
        
        // Enhanced state checking: Avoid redundant listener setup
        if (lastMonitoringPath == path && entityListener != null && 
            notificationStateManager.isMonitoringActiveOrStarting()) {
            if (BuildConfig.DEBUG) {
                Timber.tag(tag).d("Already monitoring path: %s, skipping setup", path)
            }
            return
        }
        
        // Clean up any existing listener first
        entityListener?.let {
            val oldPath = lastMonitoringPath ?: path
            try {
                FirebaseDatabase.getInstance().reference.child(oldPath).removeEventListener(it)
                Timber.tag(tag).d("Cleaned up previous listener for path: %s", oldPath)
            } catch (e: Exception) {
                Timber.tag(tag).e("Error cleaning up previous listener: ${e.message}")
            }
        }
        
        // If notifications are disabled, don't set up the listener
        if (!shouldShowNotifications()) {
            if (BuildConfig.DEBUG) {
                Timber.tag(tag).d("Notifications disabled, not monitoring for nearby entities")
            }
            notificationStateManager.markMonitoringStopped()
            return
        }
        // Reference to the opposite entity type
        val entitiesRef = FirebaseDatabase.getInstance().reference.child(path)
        // Set up the listener that triggers notifications
        entityListener = entitiesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Skip if we don't have a location or notifications are disabled
                val userLocation = currentUserLocation ?: return
                if (!shouldShowNotifications()) return
                // Get current notification radius setting
                val radius = sharedPrefs.getFloat("notification_radius_km", defaultNotificationRadiusKm)
                // Get setting for notifying about opposite role
                val notifyOppositeRole = sharedPrefs.getBoolean(
                    if (userType == "driver") "notify_passengers" else "notify_drivers",
                    true
                )
                // Skip if opposite role notifications are disabled
                if (!notifyOppositeRole) return
                snapshot.children.forEach { entitySnapshot ->
                    // Skip ourselves
                    if (entitySnapshot.key == userId) return@forEach
                    val entityId = entitySnapshot.key ?: return@forEach
                    val lat = entitySnapshot.child("latitude").getValue(Double::class.java)
                    val lng = entitySnapshot.child("longitude").getValue(Double::class.java)
                    if (lat != null && lng != null) {
                        val entityLocation = LatLng(lat, lng)
                        val distance = calculateDistance(userLocation, entityLocation)
                        // Only notify if:
                        // 1. Entity is within radius
                        // 2. We haven't notified about this entity before
                        if (distance <= radius && !notifiedEntities.contains(entityId)) {
                            // Add to notified set so we don't notify again
                            notifiedEntities.add(entityId)
                            // Create and show notification
                            showNewEntityNotification(entityId, oppositeType, distance)
                            Timber.tag(tag).d("Notified about %s %s at distance %.1f km", oppositeType, entityId, distance)
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Timber.tag(tag).e("Error monitoring secondary entities: %s", error.message)
            }
        })
        
        // Performance Optimization: Cache the monitoring path
        lastMonitoringPath = path
        
        if (BuildConfig.DEBUG) {
            Timber.tag(tag).d("Set up entity monitoring for path: %s", path)
        }
        
        // Mark monitoring as successfully started in state manager
        try {
            notificationStateManager.markMonitoringStarted(userId, userType, currentDestination)
        } catch (e: Exception) {
            Timber.tag(tag).e("Error marking monitoring as started: ${e.message}")
        }
    }
    /**
     * Display a notification for a new entity that entered the radius
     */
    private fun showNewEntityNotification(entityId: String, entityType: String, distanceKm: Float) {
        val notificationTitle = context.getString(R.string.nearby_entity_title, entityType.capitalize())
        val notificationText = context.getString(R.string.entity_distance_away, entityType, String.format(Locale.US, "%.1f", distanceKm))
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Create intent to open the app when notification is tapped
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Build notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        // Show the notification
        notificationManager.notify(entityId.hashCode(), notification)
    }
    /**
     * Calculate distance between two points
     */
    private fun calculateDistance(point1: LatLng, point2: LatLng): Float {
        return locationService.calculateDistance(point1, point2).toFloat()
    }
    /**
     * Updates the user's destination
     *
     * @param destination New destination value
     */
    fun updateDestination(destination: String) {
        if (this.currentDestination == destination) return
        this.currentDestination = destination
        Timber.tag(tag).d("Updated destination to: %s", destination)
        // Restart monitoring with new destination
        if (shouldShowNotifications()) {
            // Clear notified entities when destination changes
            notifiedEntities.clear()
            monitorSecondaryEntities()
        }
    }
    /**
     * Updates user type (driver or passenger)
     *
     * @param userType The user type to update to
     */
    fun updateUserType(userType: String) {
        if (this.userType == userType) return
        this.userType = userType
        Timber.tag(tag).d("Updated user type to: %s", userType)
        // Restart monitoring with new user type
        if (shouldShowNotifications()) {
            // Clear notified entities when user type changes
            notifiedEntities.clear()
            monitorSecondaryEntities()
        }
    }
    /**
     * Refreshes notification settings from shared preferences
     * and restarts monitoring if needed
     */
    fun updateNotificationPreferences() {
        // Load notification settings from shared preferences
        val notificationsEnabled = sharedPrefs.getBoolean("notifications_enabled", true)
        val notifyDifferentRole = sharedPrefs.getBoolean("notify_different_role", true)
        val notifySameRole = sharedPrefs.getBoolean("notify_same_role", false)
        val notifyProximity = sharedPrefs.getBoolean("notify_proximity", true)
        val radiusKm = sharedPrefs.getFloat("notification_radius_km", defaultNotificationRadiusKm)
        Timber.tag(tag).d(
            "Updated notification preferences - enabled: %b, differentRole: %b, sameRole: %b, proximity: %b, radius: %.1f km",
            notificationsEnabled, notifyDifferentRole, notifySameRole, notifyProximity, radiusKm
        )
        // Update monitoring based on new settings
        if (notificationsEnabled && notifyDifferentRole) {
            monitorSecondaryEntities()
        } else {
            // Stop monitoring if notifications are disabled
            entityListener?.let {
                val oppositeType = if (userType == "driver") "passenger" else "driver"
                val path = "${oppositeType}s/$currentDestination"
                FirebaseDatabase.getInstance().reference.child(path).removeEventListener(it)
                entityListener = null
            }
        }
    }
    /**
     * Check if notifications should be shown based on current settings
     */
    fun shouldShowNotifications(): Boolean {
        val notificationsEnabled = sharedPrefs.getBoolean("notifications_enabled", true)
        return notificationsEnabled && userId != null
    }
    /**
     * Toggle notification state
     */
    fun toggleNotifications(enabled: Boolean) {
        sharedPrefs.edit {
            putBoolean("notifications_enabled", enabled)
        }
        if (enabled) {
            // Start monitoring when enabling notifications
            notifiedEntities.clear() // Clear previous notifications
            monitorSecondaryEntities()
        } else {
            // Stop monitoring when disabling notifications
            entityListener?.let {
                val oppositeType = if (userType == "driver") "passenger" else "driver"
                val path = "${oppositeType}s/$currentDestination"
                FirebaseDatabase.getInstance().reference.child(path).removeEventListener(it)
                entityListener = null
            }
        }
        Timber.tag(tag).d("Notifications %s", if (enabled) "enabled" else "disabled")
    }
    /**
     * Checks if notification permission is granted
     * @return true if permission is granted, false otherwise
     */
    fun hasNotificationPermission(): Boolean {
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // For versions below Android 13, we don't need explicit permission
        }
        Timber.tag(tag).d("Notification permission check: %b", hasPermission)
        return hasPermission
    }
    /**
     * Request notification permission
     */
    fun requestNotificationPermission(permissionLauncher: ActivityResultLauncher<String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    /**
     * Request notification permission if needed
     */
    fun requestNotificationPermissionIfNeeded(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission()) {
                showNotificationPermissionExplanationDialog(activity)
                return false
            }
        }
        return true
    }
    /**
     * Shows an explanation dialog when notification permission is denied
     * @param activity The activity context to show the dialog in
     */
    fun showNotificationPermissionExplanationDialog(activity: Activity) {
        // Create a custom dialog
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_permission)
        // Set window properties to match system dialogs
        dialog.window?.apply {
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            attributes.windowAnimations = android.R.style.Animation_Dialog
            setGravity(Gravity.CENTER)
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }
        // Set dialog content
        val iconView = dialog.findViewById<ImageView>(R.id.icon)
        val titleView = dialog.findViewById<TextView>(R.id.dialog_title)
        val messageView = dialog.findViewById<TextView>(R.id.dialog_message)
        val buttonAllow = dialog.findViewById<Button>(R.id.btn_allow)
        val buttonDeny = dialog.findViewById<Button>(R.id.btn_deny)

        iconView.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.ic_notification, null))
        titleView.setText(R.string.notification_permission_title)
        messageView.setText(R.string.notification_permission_message)
        buttonAllow.setText(R.string.enable_notifications)
        buttonDeny.setText(R.string.not_now)

        // Set button click listeners
        buttonAllow.setOnClickListener {
            dialog.dismiss()
            // Open app settings
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", activity.packageName, null)
            intent.data = uri
            activity.startActivity(intent)
        }
        buttonDeny.setOnClickListener {
            dialog.dismiss()
            Toast.makeText(
                activity,
                R.string.notifications_disabled_message,
                Toast.LENGTH_LONG
            ).show()
        }
        dialog.show()
    }
    /**
     * Update user location
     */
    fun updateUserLocation(newLocation: LatLng) {
        currentUserLocation = newLocation
        Timber.tag(tag).d("Updated user location: %.6f, %.6f", newLocation.latitude, newLocation.longitude)
    }
    /**
     * Update user location from Android Location
     */
    fun updateUserLocation(location: android.location.Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        updateUserLocation(latLng)
    }
    /**
     * Track when user enters a map screen
     */
    fun enterMapScreen(screenName: String) {
        Timber.tag(tag).d("User entered map screen: %s", screenName)
        isInMapScreen = true
    }
    /**
     * Track when user exits a map screen
     */
    fun exitMapScreen(screenName: String) {
        Timber.tag(tag).d("User exited map screen: %s", screenName)
        isInMapScreen = false
    }
    /**
     * Save notification preferences
     */
    fun saveNotificationPreferences(
        enabled: Boolean? = null,
        notifyDifferentRole: Boolean? = null,
        notifySameRole: Boolean? = null,
        notifyProximity: Boolean? = null,
        radiusKm: Float? = null
    ) {
        sharedPrefs.edit {
            enabled?.let { putBoolean("notifications_enabled", it) }
            notifyDifferentRole?.let { putBoolean("notify_different_role", it) }
            notifySameRole?.let { putBoolean("notify_same_role", it) }
            notifyProximity?.let { putBoolean("notify_proximity", it) }
            radiusKm?.let { putFloat("notification_radius_km", it) }
        }
        Timber.tag(tag).d("Saved notification preferences to SharedPreferences")
        // Update monitoring based on new settings
        updateNotificationPreferences()
    }
    /**
     * Stops monitoring with state management
     */
    fun stopMonitoring() {
        // Use state manager to prevent redundant operations
        if (!notificationStateManager.requestStopMonitoring()) {
            Timber.tag(tag).d("Skipping monitoring stop - already stopped or stopping")
            return
        }
        
        try {
            Timber.tag(tag).d("Stopped monitoring for user %s", userId)
            
            // Remove Firebase listeners
            entityListener?.let {
                val oppositeType = if (userType == "driver") "passenger" else "driver"
                val path = "${oppositeType}s/$currentDestination"
                FirebaseDatabase.getInstance().reference.child(path).removeEventListener(it)
                entityListener = null
                lastMonitoringPath = null
            }
            
            // Mark as successfully stopped
            notificationStateManager.markMonitoringStopped()
            
        } catch (e: Exception) {
            Timber.tag(tag).e("Error stopping monitoring: ${e.message}")
            notificationStateManager.resetState()
        }
    }
    /**
     * Clean up resources when service is no longer needed
     */
    fun cleanup() {
        Timber.tag(tag).d("Performing complete service cleanup")
        // Stop monitoring
        stopMonitoring()
        // Clear notified entities
        notifiedEntities.clear()
        try {
            scope.cancel("Service stopped")
            Timber.tag(tag).d("Coroutine scope canceled")
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Error canceling coroutine scope")
        }
        Timber.tag(tag).d("Service cleaned up")
    }
    /**
     * Extension function to capitalize first letter of a string
     */
    private fun String.capitalize(): String {
        return if (isNotEmpty()) {
            this[0].uppercase() + substring(1)
        } else {
            this
        }
    }
}