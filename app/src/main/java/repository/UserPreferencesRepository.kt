package com.tecvo.taxi.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.tecvo.taxi.TaxiApplication
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import com.tecvo.taxi.BuildConfig


private const val TAG = "UserPreferencesRepository"
private const val KEY_LAST_ROLE = "last_selected_role"
private const val KEY_LAST_DESTINATION = "last_selected_destination"
private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
private const val KEY_NOTIFY_DIFFERENT_ROLE = "notify_different_role"
private const val KEY_NOTIFY_SAME_ROLE = "notify_same_role"
private const val KEY_NOTIFY_PROXIMITY = "notify_proximity"
private const val KEY_NOTIFICATION_RADIUS = "notification_radius_km"

/**
 * Repository for accessing and modifying user preferences with reactive updates
 */
@Singleton
open class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPreferences: SharedPreferences
) {
    // StateFlows for reactive updates
    private val _notificationRadiusFlow = MutableStateFlow(0.5f)
    val notificationRadiusFlow: StateFlow<Float> = _notificationRadiusFlow.asStateFlow()

    private val _notificationsEnabledFlow = MutableStateFlow(true)
    val notificationsEnabledFlow: StateFlow<Boolean> = _notificationsEnabledFlow.asStateFlow()

    private val _notifyDifferentRoleFlow = MutableStateFlow(true)
    val notifyDifferentRoleFlow: StateFlow<Boolean> = _notifyDifferentRoleFlow.asStateFlow()

    private val _notifySameRoleFlow = MutableStateFlow(false)
    val notifySameRoleFlow: StateFlow<Boolean> = _notifySameRoleFlow.asStateFlow()

    private val _notifyProximityFlow = MutableStateFlow(false)
    val notifyProximityFlow: StateFlow<Boolean> = _notifyProximityFlow.asStateFlow()

    // Performance Optimization: Cache frequently accessed values
    private var cachedLastRole: String? = null
    private var cachedLastDestination: String? = null
    private var cacheValidTime = 0L
    private val cacheExpiryMs = 5000L // Cache for 5 seconds to reduce I/O

    // SharedPreferences change listener
    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
        when (key) {
            KEY_NOTIFICATION_RADIUS -> {
                val radius = prefs.getFloat(KEY_NOTIFICATION_RADIUS, 0.5f)
                _notificationRadiusFlow.value = radius
                if (BuildConfig.DEBUG) {
                    Timber.tag(TAG).d("Preference changed: notification radius = $radius")
                }
            }
            KEY_NOTIFICATIONS_ENABLED -> {
                val enabled = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
                _notificationsEnabledFlow.value = enabled
                if (BuildConfig.DEBUG) {
                    Timber.tag(TAG).d("Preference changed: notifications enabled = $enabled")
                }
            }
            KEY_NOTIFY_DIFFERENT_ROLE -> {
                val enabled = prefs.getBoolean(KEY_NOTIFY_DIFFERENT_ROLE, true)
                _notifyDifferentRoleFlow.value = enabled
                if (BuildConfig.DEBUG) {
                    Timber.tag(TAG).d("Preference changed: notify different role = $enabled")
                }
            }
            KEY_NOTIFY_SAME_ROLE -> {
                val enabled = prefs.getBoolean(KEY_NOTIFY_SAME_ROLE, false)
                _notifySameRoleFlow.value = enabled
                if (BuildConfig.DEBUG) {
                    Timber.tag(TAG).d("Preference changed: notify same role = $enabled")
                }
            }
            KEY_NOTIFY_PROXIMITY -> {
                val enabled = prefs.getBoolean(KEY_NOTIFY_PROXIMITY, false)
                _notifyProximityFlow.value = enabled
                if (BuildConfig.DEBUG) {
                    Timber.tag(TAG).d("Preference changed: notify proximity = $enabled")
                }
            }
            // Performance Optimization: Invalidate cache when role/destination changes
            KEY_LAST_ROLE, KEY_LAST_DESTINATION -> {
                cacheValidTime = 0L // Invalidate cache
            }
        }
    }

    init {
        // Initialize flow values with current preferences
        _notificationRadiusFlow.value = sharedPreferences.getFloat(KEY_NOTIFICATION_RADIUS, 0.5f)
        _notificationsEnabledFlow.value = sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        _notifyDifferentRoleFlow.value = sharedPreferences.getBoolean(KEY_NOTIFY_DIFFERENT_ROLE, true)
        _notifySameRoleFlow.value = sharedPreferences.getBoolean(KEY_NOTIFY_SAME_ROLE, false)
        _notifyProximityFlow.value = sharedPreferences.getBoolean(KEY_NOTIFY_PROXIMITY, false)

        // Register the listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(prefsListener)
        if (BuildConfig.DEBUG) {
            Timber.tag(TAG).d("UserPreferencesRepository initialized with reactive flows")
        }
    }

    /**
     * Performance Optimized: Get the last selected role (driver or passenger)
     */
    open suspend fun getLastSelectedRole(): String? = withContext(Dispatchers.IO) {
        // Check cache first
        val currentTime = System.currentTimeMillis()
        if (cachedLastRole != null && (currentTime - cacheValidTime) < cacheExpiryMs) {
            return@withContext cachedLastRole
        }
        
        // Cache miss - fetch from preferences
        val role = sharedPreferences.getString(KEY_LAST_ROLE, null)
        
        // Update cache
        cachedLastRole = role
        cacheValidTime = currentTime
        
        if (BuildConfig.DEBUG) {
            Timber.tag(TAG).d("Retrieved last selected role: $role")
        }
        return@withContext role
    }

    /**
     * Save the last selected role
     */
    open suspend fun saveLastSelectedRole(role: String): Int = withContext(Dispatchers.IO) {
        sharedPreferences.edit {
            putString(KEY_LAST_ROLE, role)
        }
        // Performance Optimization: Update cache immediately
        cachedLastRole = role
        cacheValidTime = System.currentTimeMillis()
        
        if (BuildConfig.DEBUG) {
            Timber.tag(TAG).d("Saved last selected role: $role")
        }
        return@withContext 1  // Return success code
    }

    /**
     * Get the last selected destination (town or local)
     */
    open suspend fun saveLastSelectedDestination(destination: String): Int = withContext(Dispatchers.IO) {
        sharedPreferences.edit {
            putString(KEY_LAST_DESTINATION, destination)
        }
        // Performance Optimization: Update cache immediately
        cachedLastDestination = destination
        cacheValidTime = System.currentTimeMillis()
        
        if (BuildConfig.DEBUG) {
            Timber.tag(TAG).d("Saved last selected destination: $destination")
        }
        return@withContext 1  // Return success code
    }

    /**
     * Performance Optimized: Get the last selected destination (town or local)
     */
    open suspend fun getLastSelectedDestination(): String = withContext(Dispatchers.IO) {
        // Check cache first
        val currentTime = System.currentTimeMillis()
        if (cachedLastDestination != null && (currentTime - cacheValidTime) < cacheExpiryMs) {
            return@withContext cachedLastDestination!!
        }
        
        // Cache miss - fetch from preferences
        val destination = sharedPreferences.getString(KEY_LAST_DESTINATION, "town") ?: "town"
        
        // Update cache
        cachedLastDestination = destination
        cacheValidTime = currentTime
        
        if (BuildConfig.DEBUG) {
            Timber.tag(TAG).d("Retrieved last selected destination: $destination")
        }
        return@withContext destination
    }


    /**
     * Check if notifications are enabled
     */
    open suspend fun areNotificationsEnabled(): Boolean = withContext(Dispatchers.IO) {
        return@withContext sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    /**
     * Set notifications enabled/disabled
     */
    open suspend fun setNotificationsEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        sharedPreferences.edit {
            putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled)
        }
        Timber.tag(TAG).d("Set notifications enabled: $enabled")
    }

    /**
     * Check if notifications for different role are enabled
     */
    open suspend fun areDifferentRoleNotificationsEnabled(): Boolean = withContext(Dispatchers.IO) {
        return@withContext sharedPreferences.getBoolean(KEY_NOTIFY_DIFFERENT_ROLE, true)
    }

    /**
     * Check if notifications for same role are enabled
     */
    open suspend fun areSameRoleNotificationsEnabled(): Boolean = withContext(Dispatchers.IO) {
        return@withContext sharedPreferences.getBoolean(KEY_NOTIFY_SAME_ROLE, false)
    }

    /**
     * Set same role notifications enabled/disabled
     */
    open suspend fun setSameRoleNotificationsEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        sharedPreferences.edit {
            putBoolean(KEY_NOTIFY_SAME_ROLE, enabled)
        }
        Timber.tag(TAG).d("Set same role notifications enabled: $enabled")
    }

    /**
     * Check if proximity notifications are enabled
     */
    open suspend fun areProximityNotificationsEnabled(): Boolean = withContext(Dispatchers.IO) {
        return@withContext sharedPreferences.getBoolean(KEY_NOTIFY_PROXIMITY, false)
    }

    /**
     * Set proximity notifications enabled/disabled
     */
    open suspend fun setProximityNotificationsEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        sharedPreferences.edit {
            putBoolean(KEY_NOTIFY_PROXIMITY, enabled)
        }
        Timber.tag(TAG).d("Set proximity notifications enabled: $enabled")
    }

    /**
     * Get notification radius in kilometers
     */
    open suspend fun getNotificationRadius(): Float = withContext(Dispatchers.IO) {
        val radius = sharedPreferences.getFloat(KEY_NOTIFICATION_RADIUS, 0.5f)
        Timber.tag(TAG).d("Retrieved notification radius: $radius km")
        return@withContext radius
    }

    /**
     * Set notification radius in kilometers
     */
    open suspend fun setNotificationRadius(radiusKm: Float) = withContext(Dispatchers.IO) {
        sharedPreferences.edit {
            putFloat(KEY_NOTIFICATION_RADIUS, radiusKm)
        }
        Timber.tag(TAG).d("Set notification radius: $radiusKm km")
    }

    /**
     * Save all notification settings at once
     */
    open suspend fun saveNotificationSettings(
        enabled: Boolean,
        notifyDifferentRole: Boolean,
        notifySameRole: Boolean,
        proximityEnabled: Boolean,
        radiusKm: Float
    ) = withContext(Dispatchers.IO) {
        sharedPreferences.edit {
            putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled)
            putBoolean(KEY_NOTIFY_DIFFERENT_ROLE, notifyDifferentRole)
            putBoolean(KEY_NOTIFY_SAME_ROLE, notifySameRole)
            putBoolean(KEY_NOTIFY_PROXIMITY, proximityEnabled)
            putFloat(KEY_NOTIFICATION_RADIUS, radiusKm)
        }

        // Notify the application of preference changes
        (context as? TaxiApplication)?.updateNotificationServicePreferences()
        Timber.tag(TAG).d("Saved all notification settings")
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(prefsListener)
        Timber.tag(TAG).d("UserPreferencesRepository cleaned up")
    }
}