package com.tecvo.taxi.integration

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.tecvo.taxi.components.CityOverviewErrorCard
import com.tecvo.taxi.components.CityOverviewInfoCard
import com.tecvo.taxi.components.CityOverviewToggleButton
import com.tecvo.taxi.viewmodel.CityBasedOverviewViewModel
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber

/**
 * Integration wrapper for city-based overview functionality
 * This provides a clean interface for MapScreen to use city-based filtering
 * without breaking existing overview functionality
 */
@Composable
fun WithCityBasedOverview(
    // Map data
    currentUserLocation: LatLng?,
    allPassengerLocations: List<LatLng>,
    allDriverLocations: List<LatLng>,
    userType: String,
    destination: String, // Add destination parameter for filtering
    showSameTypeEntities: Boolean, // Add visibility state from eye button
    
    // UI configuration
    showToggleButton: Boolean = true,
    showInfoCard: Boolean = true,
    
    // Callback when filtered entities change
    onFilteredEntitiesChange: (passengerLocations: List<LatLng>, driverLocations: List<LatLng>) -> Unit = { _, _ -> },
    
    // Content
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    
    // ViewModel
    cityOverviewViewModel: CityBasedOverviewViewModel = hiltViewModel()
) {
    val state by cityOverviewViewModel.state.collectAsState()
    
    // Auto-detect city when user location changes
    LaunchedEffect(currentUserLocation) {
        currentUserLocation?.let { location ->
            if (state.currentUserCity == null) {
                Timber.d("CityBasedOverview: Auto-detecting city for user location")
                cityOverviewViewModel.initializeCityDetection(location)
            }
        }
    }
    
    // Perform filtering when overview is active and we have all required data
    LaunchedEffect(
        state.isActive, 
        currentUserLocation, 
        allPassengerLocations, 
        allDriverLocations,
        state.currentUserCity,
        destination, // Also trigger when destination changes
        showSameTypeEntities // Also trigger when visibility setting changes
    ) {
        if (state.isActive && currentUserLocation != null && state.currentUserCity != null) {
            Timber.d("CityBasedOverview: Filtering entities for city: ${state.currentUserCity?.cityName}, destination: $destination, showSameType: $showSameTypeEntities")
            cityOverviewViewModel.filterEntitiesByCityAndDestinationWithVisibility(
                allPassengerLocations = allPassengerLocations,
                allDriverLocations = allDriverLocations,
                currentUserLocation = currentUserLocation,
                destination = destination,
                userType = userType,
                showSameTypeEntities = showSameTypeEntities
            )
        }
    }
    
    // Notify parent when filtered entities change
    LaunchedEffect(state.filteredEntities) {
        state.filteredEntities?.let { filtered ->
            if (state.isActive) {
                onFilteredEntitiesChange(filtered.passengerLocations, filtered.driverLocations)
            } else {
                // When not active, pass through all entities
                onFilteredEntitiesChange(allPassengerLocations, allDriverLocations)
            }
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Main content (map)
        content()
        
        
        // Info card when overview is active
        if (showInfoCard && state.isActive && state.currentUserCity != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 80.dp, end = 16.dp)
                    .zIndex(12f)
            ) {
                CityOverviewInfoCard(
                    cityName = state.currentUserCity!!.cityName,
                    filteredEntities = state.filteredEntities,
                    userType = userType,
                    destination = destination, // Pass destination for display
                    onDismiss = {
                        cityOverviewViewModel.toggleCityOverview()
                    }
                )
            }
        }
        
        // Error card
        state.errorMessage?.let { errorMsg ->
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
                    .zIndex(20f)
            ) {
                CityOverviewErrorCard(
                    errorMessage = errorMsg,
                    onRetry = {
                        currentUserLocation?.let { location ->
                            cityOverviewViewModel.initializeCityDetection(location)
                        }
                    },
                    onDismiss = {
                        cityOverviewViewModel.clearError()
                    }
                )
            }
        }
    }
}

/**
 * Utility function to check if city-based overview should filter entities
 */
@Composable
fun rememberCityBasedFiltering(
    cityOverviewViewModel: CityBasedOverviewViewModel = hiltViewModel()
): Boolean {
    val state by cityOverviewViewModel.state.collectAsState()
    return state.isActive && state.filteredEntities != null
}

/**
 * Utility function to get filtered entities for use in map rendering
 */
@Composable
fun rememberFilteredEntities(
    originalPassengers: List<LatLng>,
    originalDrivers: List<LatLng>,
    cityOverviewViewModel: CityBasedOverviewViewModel = hiltViewModel()
): Pair<List<LatLng>, List<LatLng>> {
    val state by cityOverviewViewModel.state.collectAsState()
    
    return if (state.isActive && state.filteredEntities != null) {
        Pair(
            state.filteredEntities!!.passengerLocations,
            state.filteredEntities!!.driverLocations
        )
    } else {
        Pair(originalPassengers, originalDrivers)
    }
}