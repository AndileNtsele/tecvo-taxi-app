package com.tecvo.taxi.di

import android.content.Context
import com.tecvo.taxi.screens.mapscreens.MapScreenEntityTracker
import com.tecvo.taxi.screens.mapscreens.MapScreenErrorHandler
import com.tecvo.taxi.screens.mapscreens.MapScreenLocationTracker
import com.tecvo.taxi.screens.mapscreens.MapScreenNotificationManager
import com.tecvo.taxi.screens.mapscreens.MapScreenState
import com.tecvo.taxi.services.ErrorHandlingService
import com.tecvo.taxi.services.LocationService
import com.tecvo.taxi.services.NotificationService
import com.tecvo.taxi.utils.ConnectivityManager
import com.tecvo.taxi.viewmodel.MapViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object ScreenComponentsModule {

    @Provides
    @ViewModelScoped
    fun provideMapScreenState(
        viewModel: MapViewModel
    ): MapScreenState {
        return MapScreenState(viewModel)
    }

    @Provides
    @ViewModelScoped
    fun provideMapScreenLocationTracker(
        locationService: LocationService
    ): MapScreenLocationTracker {
        return MapScreenLocationTracker(locationService)
    }

    @Provides
    @ViewModelScoped
    fun provideMapScreenEntityTracker(
        @ApplicationContext context: Context,
        locationTracker: MapScreenLocationTracker
    ): MapScreenEntityTracker {
        return MapScreenEntityTracker(context, locationTracker)
    }

    @Provides
    @ViewModelScoped
    fun provideMapScreenNotificationManager(
        connectivityManager: ConnectivityManager,
        notificationService: NotificationService,
        locationService: LocationService  // ADD THIS PARAMETER
    ): MapScreenNotificationManager {
        return MapScreenNotificationManager(
            connectivityManager,
            notificationService,
            locationService  // ADD THIS PARAMETER
        )
    }

    @Provides
    @ViewModelScoped
    fun provideMapScreenErrorHandler(
        errorHandlingService: ErrorHandlingService
    ): MapScreenErrorHandler {
        // Note: SnackbarHostState and coroutineScope will be set in composition
        return MapScreenErrorHandler(errorHandlingService)
    }
}