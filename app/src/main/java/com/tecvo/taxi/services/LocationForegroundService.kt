package com.tecvo.taxi.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.tecvo.taxi.MainActivity
import com.tecvo.taxi.R
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * Foreground service that keeps location tracking active when app is backgrounded.
 * Required for continuous location updates for both passengers and drivers.
 * 
 * This service:
 * - Shows a persistent notification to inform users location tracking is active
 * - Keeps LocationService running in the background
 * - Updates Firebase with user location continuously
 * - Enables notifications about nearby taxis/passengers
 */
@AndroidEntryPoint
class LocationForegroundService : Service() {

    @Inject
    lateinit var locationService: LocationService
    
    @Inject
    lateinit var notificationService: NotificationService

    companion object {
        private const val TAG = "LocationForegroundService"
        private const val CHANNEL_ID = "location_tracking_channel"
        private const val NOTIFICATION_ID = 1001
        
        // Intent extras
        const val EXTRA_USER_ID = "user_id"
        const val EXTRA_USER_TYPE = "user_type"
        const val EXTRA_DESTINATION = "destination"
        
        /**
         * Start the foreground service
         */
        fun start(context: Context, userId: String, userType: String, destination: String) {
            val intent = Intent(context, LocationForegroundService::class.java).apply {
                putExtra(EXTRA_USER_ID, userId)
                putExtra(EXTRA_USER_TYPE, userType)
                putExtra(EXTRA_DESTINATION, destination)
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            
            Timber.tag(TAG).i("Starting location foreground service for $userType: $userId")
        }
        
        /**
         * Stop the foreground service
         */
        fun stop(context: Context) {
            val intent = Intent(context, LocationForegroundService::class.java)
            context.stopService(intent)
            Timber.tag(TAG).i("Stopping location foreground service")
        }
    }

    private var userId: String? = null
    private var userType: String? = null
    private var destination: String? = null
    private var foregroundStarted = false

    override fun onCreate() {
        super.onCreate()
        
        // Create channel if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        
        // Create and show notification IMMEDIATELY
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Taxi")
            .setContentText("Location tracking active")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
            
        startForeground(NOTIFICATION_ID, notification)
        foregroundStarted = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.tag(TAG).i("onStartCommand called")
        
        if (!foregroundStarted) {
            Timber.tag(TAG).e("Foreground not started in onCreate(), cannot continue")
            return START_NOT_STICKY
        }
        
        // Extract user info from intent
        userId = intent?.getStringExtra(EXTRA_USER_ID)
        userType = intent?.getStringExtra(EXTRA_USER_TYPE)
        destination = intent?.getStringExtra(EXTRA_DESTINATION)
        
        Timber.tag(TAG).d("Parameters: userId=$userId, userType=$userType, destination=$destination")
        
        if (userId == null || userType == null || destination == null) {
            Timber.tag(TAG).e("Missing required parameters")
            return START_NOT_STICKY
        }
        
        try {
            // Update notification with actual user info
            val notification = createNotification()
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.notify(NOTIFICATION_ID, notification)
            
            // Set user info in LocationService
            locationService.setUserInfo(userId!!, userType!!, destination!!)
            
            // Start location updates
            locationService.startLocationUpdates()
            
            // Start monitoring for nearby entities
            locationService.currentLocation.value?.let { location ->
                notificationService.startMonitoring(
                    userId = userId!!,
                    location = location,
                    destination = destination!!,
                    userType = userType!!
                )
            }
            
            Timber.tag(TAG).i("Location tracking started for $userType: $userId")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error in onStartCommand")
            return START_NOT_STICKY
        }
        
        Timber.tag(TAG).i("Location tracking started for $userType: $userId in $destination")
        
        // If service is killed, recreate it with the same intent
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // Not a bound service
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.tag(TAG).i("Service destroyed, stopping location updates")
        
        // Stop location updates
        locationService.stopLocationUpdates()
        
        // Stop notification monitoring
        notificationService.stopMonitoring()
    }

    /**
     * Creates the notification channel for Android O and above
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW // Low importance = no sound/vibration
            ).apply {
                description = "Keeps location tracking active when app is in background"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            
            Timber.tag(TAG).d("Notification channel created")
        }
    }

    /**
     * Creates the persistent notification shown while service is running
     */
    private fun createNotification(): Notification {
        // Intent to open the app when notification is tapped
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Get notification text based on user type
        val notificationText = when (userType) {
            "driver" -> "Passengers can see your location on the map"
            "passenger" -> "Drivers can see your location on the map"
            else -> "Location tracking is active"
        }
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Taxi - Active")
            .setContentText(notificationText)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // Can't be dismissed by user
            .setPriority(NotificationCompat.PRIORITY_LOW) // Low priority = no sound
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
    
    /**
     * Creates a placeholder notification for immediate startForeground() call
     */
    private fun createPlaceholderNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Taxi - Starting...")
            .setContentText("Initializing location tracking")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
}
