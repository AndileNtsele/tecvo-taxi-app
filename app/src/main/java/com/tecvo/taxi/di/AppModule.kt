package com.tecvo.taxi.di

import android.content.Context
import android.content.SharedPreferences
import com.tecvo.taxi.permissions.PermissionManager
import com.tecvo.taxi.services.AnalyticsManager
import com.tecvo.taxi.services.AppInitManager
import com.tecvo.taxi.services.AppInitService
import com.tecvo.taxi.services.CityBasedOverviewService
import com.tecvo.taxi.services.CrashReportingManager
import com.tecvo.taxi.services.ErrorHandlingService
import com.tecvo.taxi.services.GeocodingService
import com.tecvo.taxi.services.LocationService
import com.tecvo.taxi.services.NotificationService
import com.tecvo.taxi.services.NotificationStateManager
import com.tecvo.taxi.services.MapsInitializationManager
import com.tecvo.taxi.services.LocationServiceStateManager
import com.tecvo.taxi.utils.MemoryOptimizationManager
import com.tecvo.taxi.utils.ConnectivityManager
import com.tecvo.taxi.utils.FavoritesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.tecvo.taxi.repository.UserPreferencesRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("taxi_app_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideConnectivityManager(@ApplicationContext context: Context): ConnectivityManager {
        return ConnectivityManager(context)
    }

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return FirebaseDatabase.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        @ApplicationContext context: Context,
        sharedPreferences: SharedPreferences
    ): UserPreferencesRepository {
        return UserPreferencesRepository(context, sharedPreferences)
    }

    @Provides
    @Singleton
    fun provideErrorHandlingService(
        @ApplicationContext context: Context,
        crashReportingManager: CrashReportingManager
    ): ErrorHandlingService {
        return ErrorHandlingService(context, crashReportingManager)
    }

    @Provides
    @Singleton
    fun provideLocationServiceStateManager(): LocationServiceStateManager {
        return LocationServiceStateManager()
    }

    @Provides
    @Singleton
    fun provideLocationService(
        @ApplicationContext context: Context,
        errorHandlingService: ErrorHandlingService,
        permissionManager: PermissionManager,
        locationServiceStateManager: LocationServiceStateManager
    ): LocationService {
        return LocationService(context, errorHandlingService, permissionManager, locationServiceStateManager)
    }

    @Provides
    @Singleton
    fun provideNotificationStateManager(): NotificationStateManager {
        return NotificationStateManager()
    }


    @Provides
    @Singleton
    fun provideMemoryOptimizationManager(): MemoryOptimizationManager {
        return MemoryOptimizationManager()
    }

    @Provides
    @Singleton
    fun provideNotificationService(
        @ApplicationContext context: Context,
        locationService: LocationService,
        notificationStateManager: NotificationStateManager
    ): NotificationService {
        return NotificationService(context, locationService, notificationStateManager)
    }

    @Provides
    @Singleton
    fun provideFavoritesManager(@ApplicationContext context: Context): FavoritesManager {
        return FavoritesManager(context)
    }

    @Provides
    @Singleton
    fun provideGeocodingService(@ApplicationContext context: Context): GeocodingService {
        return GeocodingService(context)
    }

    @Provides
    @Singleton
    fun provideCityBasedOverviewService(
        @ApplicationContext context: Context,
        geocodingService: GeocodingService
    ): CityBasedOverviewService {
        return CityBasedOverviewService(context, geocodingService)
    }

    @Provides
    @Singleton
    fun provideAnalyticsManager(@ApplicationContext context: Context): AnalyticsManager {
        return AnalyticsManager(context)
    }

    @Provides
    @Singleton
    fun provideCrashReportingManager(@ApplicationContext context: Context): CrashReportingManager {
        return CrashReportingManager(context)
    }

    @Provides
    @Singleton
    fun provideAppInitService(@ApplicationContext context: Context): AppInitService {
        return AppInitService(context)
    }


    @Provides
    @Singleton
    fun provideAppInitManager(
        locationService: LocationService,
        notificationService: NotificationService,
        analyticsManager: AnalyticsManager
    ): AppInitManager {
        return AppInitManager(locationService, notificationService, analyticsManager)
    }
}