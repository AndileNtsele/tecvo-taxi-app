// Create this file at: com/example/taxi/di/HiltEntryPoints.kt
package com.tecvo.taxi.di

import android.content.Context
import com.tecvo.taxi.permissions.PermissionManager
import com.tecvo.taxi.services.AnalyticsManager
import com.tecvo.taxi.services.ErrorHandlingService
import com.tecvo.taxi.services.GeocodingService
import com.tecvo.taxi.services.LocationService
import com.tecvo.taxi.services.MapsInitializationManager
import com.tecvo.taxi.services.NotificationService
import com.tecvo.taxi.utils.ConnectivityManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MapScreenEntryPoint {
    fun errorHandlingService(): ErrorHandlingService
    fun locationService(): LocationService
    fun notificationService(): NotificationService
    fun connectivityManager(): ConnectivityManager
    fun analyticsManager(): AnalyticsManager
    fun permissionManager(): PermissionManager
    fun mapsInitializationManager(): MapsInitializationManager
    fun geocodingService(): GeocodingService
}

// Helper function to access the entry point
object HiltEntryPoints {
    fun getMapScreenEntryPoint(context: Context): MapScreenEntryPoint {
        return EntryPointAccessors.fromApplication(
            context.applicationContext,
            MapScreenEntryPoint::class.java
        )
    }
}