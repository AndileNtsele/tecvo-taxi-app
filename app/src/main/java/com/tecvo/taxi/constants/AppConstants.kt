package com.tecvo.taxi.constants

object AppConstants {
    // Notification constants
    const val NOTIFICATION_CHANNEL_ID = "taxi_app_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Taxi App Notifications"
    const val NOTIFICATION_CHANNEL_DESC = "Notifications for nearby passengers and drivers"

    // Map UI Feature Flags - Easy toggle for visual improvements
    object MapFeatures {
        const val ENABLE_CUSTOM_MARKERS = true
        const val ENABLE_CUSTOM_MAP_THEME = true
        const val ENABLE_MARKER_ANIMATIONS = true
        const val ENABLE_FLOATING_ACTION_BUTTONS = true
        const val ENABLE_RADIUS_VISUALIZATION = true
        const val ENABLE_SMOOTH_TRANSITIONS = true
    }
}