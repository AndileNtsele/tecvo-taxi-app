package com.tecvo.taxi.services

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for controlling the LocationForegroundService lifecycle.
 * Provides centralized control for starting/stopping background location tracking.
 */
@Singleton
class LocationForegroundServiceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "LocationFgServiceMgr"
        private const val RESTART_DELAY_MS = 200L
    }
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isServiceRunning = false
    private var currentUserId: String? = null
    private var currentUserType: String? = null
    private var currentDestination: String? = null
    
    /**
     * Start the foreground service for continuous location tracking
     * 
     * @param userId User's unique identifier
     * @param userType User type (driver or passenger)
     * @param destination User's destination (town or local)
     */
    fun startService(userId: String, userType: String, destination: String) {
        // Don't restart if already running with same parameters
        if (isServiceRunning && 
            currentUserId == userId && 
            currentUserType == userType && 
            currentDestination == destination) {
            Timber.tag(TAG).d("Service already running with same parameters")
            return
        }
        
        // Stop existing service if parameters changed
        if (isServiceRunning && 
            (currentUserId != userId || 
             currentUserType != userType || 
             currentDestination != destination)) {
            Timber.tag(TAG).i("Parameters changed, restarting service")
            stopService()
            // Give system time to fully stop the service before restarting
            serviceScope.launch {
                delay(RESTART_DELAY_MS)
                startServiceInternal(userId, userType, destination)
            }
            return
        }
        
        startServiceInternal(userId, userType, destination)
    }
    
    private fun startServiceInternal(userId: String, userType: String, destination: String) {
        try {
            LocationForegroundService.start(context, userId, userType, destination)
            isServiceRunning = true
            currentUserId = userId
            currentUserType = userType
            currentDestination = destination
            Timber.tag(TAG).i("Started foreground service for $userType: $userId")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to start foreground service")
            isServiceRunning = false
        }
    }
    
    /**
     * Stop the foreground service
     */
    fun stopService() {
        if (!isServiceRunning) {
            Timber.tag(TAG).d("Service not running, nothing to stop")
            return
        }
        
        try {
            LocationForegroundService.stop(context)
            isServiceRunning = false
            currentUserId = null
            currentUserType = null
            currentDestination = null
            Timber.tag(TAG).i("Stopped foreground service")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to stop foreground service")
        }
    }
    
    /**
     * Check if service is currently running
     */
    fun isRunning(): Boolean = isServiceRunning
    
    /**
     * Update destination if service is running
     */
    fun updateDestination(destination: String) {
        if (isServiceRunning && currentUserId != null && currentUserType != null) {
            currentDestination = destination
            startService(currentUserId!!, currentUserType!!, destination)
        }
    }
    
    /**
     * Update user type if service is running
     */
    fun updateUserType(userType: String) {
        if (isServiceRunning && currentUserId != null && currentDestination != null) {
            currentUserType = userType
            startService(currentUserId!!, userType, currentDestination!!)
        }
    }
}
