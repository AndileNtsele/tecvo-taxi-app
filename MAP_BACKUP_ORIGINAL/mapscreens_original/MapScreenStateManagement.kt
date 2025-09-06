package com.example.taxi.screens.mapscreens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import com.example.taxi.viewmodel.MapViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapType
import javax.inject.Inject

class MapScreenState @Inject constructor(
    private val viewModel: MapViewModel
) {
    // Expose states as composable states
    @Composable
    fun currentLocation(): State<LatLng?> = viewModel.currentLocation.collectAsState()

    @Composable
    fun primaryEntityLocations(): State<List<LatLng>> = viewModel.primaryEntityLocations.collectAsState()

    @Composable
    fun secondaryEntityLocations(): State<List<LatLng>> = viewModel.secondaryEntityLocations.collectAsState()

    @Composable
    fun nearbyPrimaryCount(): State<Int> = viewModel.nearbyPrimaryCount.collectAsState()

    @Composable
    fun nearbySecondaryCount(): State<Int> = viewModel.nearbySecondaryCount.collectAsState()

    @Composable
    fun isLoading(): State<Boolean> = viewModel.isLoading.collectAsState()

    @Composable
    fun showSameTypeEntities(): State<Boolean> = viewModel.showSameTypeEntities.collectAsState()

    @Composable
    fun mapType(): State<MapType> = viewModel.mapType.collectAsState()

    @Composable
    fun searchRadius(): State<Float> = viewModel.searchRadius.collectAsState()

    @Composable
    fun isOffline(): State<Boolean> = viewModel.isOffline.collectAsState()

    // Actions
    fun toggleSameTypeEntitiesVisibility() = viewModel.toggleSameTypeEntitiesVisibility()
    fun toggleMapType() = viewModel.toggleMapType()
    fun updateSearchRadius(radius: Float) = viewModel.updateSearchRadius(radius)
    fun updateLocationPermission(granted: Boolean) = viewModel.updateLocationPermission(granted)
    fun cleanup() = viewModel.cleanup()
}

// Helper function to get MapScreenState via Hilt
@Composable
fun rememberMapScreenState(
    viewModel: MapViewModel
): MapScreenState = MapScreenState(viewModel)