package com.tecvo.taxi.utils

import android.content.SharedPreferences
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

/**
 * Utility for creating properly configured SharedPreferences mocks for testing.
 * This solves the common issue where mocks return default values instead of configured ones.
 */
object TestSharedPreferencesUtil {
    
    /**
     * Create a SharedPreferences mock with all standard TECVO TAXI app preferences
     * properly configured with sensible test defaults.
     */
    fun createMockSharedPreferences(): SharedPreferences {
        val mockPrefs = Mockito.mock(SharedPreferences::class.java)
        val mockEditor = Mockito.mock(SharedPreferences.Editor::class.java)
        
        // Setup editor chain - using doReturn for chaining instead of any() matchers
        whenever(mockPrefs.edit()).thenReturn(mockEditor)
        whenever(mockEditor.putString(Mockito.anyString(), Mockito.anyString())).thenReturn(mockEditor)
        whenever(mockEditor.putBoolean(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(mockEditor)
        whenever(mockEditor.putFloat(Mockito.anyString(), Mockito.anyFloat())).thenReturn(mockEditor)
        whenever(mockEditor.putInt(Mockito.anyString(), Mockito.anyInt())).thenReturn(mockEditor)
        whenever(mockEditor.putLong(Mockito.anyString(), Mockito.anyLong())).thenReturn(mockEditor)
        whenever(mockEditor.apply()).then { /* no-op */ }
        whenever(mockEditor.commit()).thenReturn(true)
        
        // Configure all TECVO TAXI preferences with test defaults
        configureDefaultPreferences(mockPrefs)
        
        return mockPrefs
    }
    
    /**
     * Configure a SharedPreferences mock with all default values used in the TECVO TAXI app
     */
    private fun configureDefaultPreferences(mockPrefs: SharedPreferences) {
        // User preferences
        whenever(mockPrefs.getString("last_selected_role", null)).thenReturn(null)
        whenever(mockPrefs.getString("last_selected_destination", "town")).thenReturn("town")
        whenever(mockPrefs.getString("user_phone", "")).thenReturn("")
        
        // Notification preferences
        whenever(mockPrefs.getBoolean("notifications_enabled", true)).thenReturn(true)
        whenever(mockPrefs.getBoolean("notify_different_role", true)).thenReturn(true) 
        whenever(mockPrefs.getBoolean("notify_same_role", false)).thenReturn(false)
        whenever(mockPrefs.getBoolean("notify_proximity", true)).thenReturn(true)
        whenever(mockPrefs.getBoolean("notify_passengers", true)).thenReturn(true)
        whenever(mockPrefs.getBoolean("notify_drivers", true)).thenReturn(true)
        whenever(mockPrefs.getFloat("notification_radius_km", 0.5f)).thenReturn(3.5f)
        
        // Location preferences  
        whenever(mockPrefs.getBoolean("location_enabled", false)).thenReturn(false)
        whenever(mockPrefs.getFloat("last_latitude", 0.0f)).thenReturn(0.0f)
        whenever(mockPrefs.getFloat("last_longitude", 0.0f)).thenReturn(0.0f)
        
        // App state preferences
        whenever(mockPrefs.getBoolean("first_launch", true)).thenReturn(true)
        whenever(mockPrefs.getLong("last_app_update", 0L)).thenReturn(0L)
        whenever(mockPrefs.getString("app_version", "")).thenReturn("1.0")
    }
    
    /**
     * Create a SharedPreferences mock with custom values for specific keys
     */
    fun createMockSharedPreferences(customValues: Map<String, Any>): SharedPreferences {
        val mockPrefs = createMockSharedPreferences()
        
        // Override with custom values using specific defaults
        customValues.forEach { (key, value) ->
            when (value) {
                is String -> {
                    // Use specific defaults for known keys
                    val default = when (key) {
                        "last_selected_role" -> null
                        "last_selected_destination" -> "town"
                        "user_phone" -> ""
                        "app_version" -> ""
                        else -> ""
                    }
                    whenever(mockPrefs.getString(key, default)).thenReturn(value)
                }
                is Boolean -> {
                    // Use specific defaults for known boolean keys
                    val default = when (key) {
                        "notifications_enabled" -> true
                        "notify_different_role" -> true
                        "notify_same_role" -> false
                        "notify_proximity" -> true
                        "notify_passengers" -> true
                        "notify_drivers" -> true
                        "location_enabled" -> false
                        "first_launch" -> true
                        else -> false
                    }
                    whenever(mockPrefs.getBoolean(key, default)).thenReturn(value)
                }
                is Float -> {
                    val default = when (key) {
                        "notification_radius_km" -> 0.5f
                        "last_latitude" -> 0.0f
                        "last_longitude" -> 0.0f
                        else -> 0.0f
                    }
                    whenever(mockPrefs.getFloat(key, default)).thenReturn(value)
                }
                is Int -> whenever(mockPrefs.getInt(key, 0)).thenReturn(value)
                is Long -> whenever(mockPrefs.getLong(key, 0L)).thenReturn(value)
            }
        }
        
        return mockPrefs
    }
    
    /**
     * Create test data set for notification radius testing
     */
    fun createNotificationRadiusTestData(): Map<String, Any> {
        return mapOf(
            "notification_radius_km" to 3.5f,
            "notifications_enabled" to true,
            "notify_different_role" to true
        )
    }
    
    /**
     * Create test data set for user preferences testing
     */
    fun createUserPreferencesTestData(): Map<String, Any> {
        return mapOf(
            "last_selected_role" to "driver",
            "last_selected_destination" to "town",
            "notifications_enabled" to true,
            "user_phone" to "+27821234567"
        )
    }
}