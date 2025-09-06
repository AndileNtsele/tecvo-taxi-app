package com.tecvo.taxi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecvo.taxi.services.CityBasedOverviewService
import com.tecvo.taxi.services.CityInfo
import com.tecvo.taxi.services.CityFilteredEntities
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class CityOverviewState(
    val isActive: Boolean = false,
    val isLoading: Boolean = false,
    val currentUserCity: CityInfo? = null,
    val filteredEntities: CityFilteredEntities? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class CityBasedOverviewViewModel @Inject constructor(
    private val cityBasedOverviewService: CityBasedOverviewService
) : ViewModel() {
    
    private val TAG = "CityBasedOverviewVM"
    
    // Combined state for UI
    private val _state = MutableStateFlow(CityOverviewState())
    val state: StateFlow<CityOverviewState> = _state.asStateFlow()
    
    // Job for filtering operations
    private var filteringJob: Job? = null
    
    init {
        // Observe service state changes
        viewModelScope.launch {
            combine(
                cityBasedOverviewService.isCityOverviewActive,
                cityBasedOverviewService.currentUserCity,
                cityBasedOverviewService.filteredEntities
            ) { isActive, userCity, filtered ->
                _state.value = _state.value.copy(
                    isActive = isActive,
                    currentUserCity = userCity,
                    filteredEntities = filtered
                )
            }
        }
        
        Timber.tag(TAG).d("CityBasedOverviewViewModel initialized")
    }
    
    /**
     * Toggle city-based overview mode
     */
    fun toggleCityOverview() {
        Timber.tag(TAG).d("toggleCityOverview called")
        cityBasedOverviewService.toggleCityOverviewMode()
        
        // Immediately update ViewModel state to reflect the change
        val newActiveState = cityBasedOverviewService.isCityOverviewActive.value
        _state.value = _state.value.copy(isActive = newActiveState)
        
        Timber.tag(TAG).d("City overview state updated to: $newActiveState")
    }
    
    /**
     * Initialize city detection for current user
     */
    fun initializeCityDetection(currentUserLocation: LatLng) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, errorMessage = null)
                
                Timber.tag(TAG).d("Initializing city detection for location: ${currentUserLocation.latitude}, ${currentUserLocation.longitude}")
                
                val cityInfo = cityBasedOverviewService.detectCurrentUserCity(currentUserLocation)
                
                if (cityInfo != null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        currentUserCity = cityInfo
                    )
                    
                    Timber.tag(TAG).i("City detection successful: ${cityInfo.cityName}")
                } else {
                    val errorMsg = "Could not detect city from location"
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = errorMsg
                    )
                    
                    Timber.tag(TAG).w(errorMsg)
                }
                
            } catch (e: Exception) {
                val errorMsg = "Error detecting city: ${e.message}"
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = errorMsg
                )
                
                Timber.tag(TAG).e(e, errorMsg)
            }
        }
    }
    
    /**
     * Filter entities by city and destination
     */
    fun filterEntitiesByCityAndDestination(
        allPassengerLocations: List<LatLng>,
        allDriverLocations: List<LatLng>,
        currentUserLocation: LatLng,
        destination: String
    ) {
        // Cancel any existing filtering job
        filteringJob?.cancel()
        
        filteringJob = viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, errorMessage = null)
                
                Timber.tag(TAG).d("Starting entity filtering by city and destination: $destination - passengers: ${allPassengerLocations.size}, drivers: ${allDriverLocations.size}")
                
                val filteredEntities = cityBasedOverviewService.filterEntitiesByCityAndDestination(
                    allPassengerLocations = allPassengerLocations,
                    allDriverLocations = allDriverLocations,
                    currentUserLocation = currentUserLocation,
                    destination = destination
                )
                
                if (filteredEntities != null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        filteredEntities = filteredEntities
                    )
                    
                    Timber.tag(TAG).i("Entity filtering successful for ${filteredEntities.cityName} ($destination): ${filteredEntities.totalPassengers} passengers, ${filteredEntities.totalDrivers} drivers")
                } else {
                    val errorMsg = "Could not filter entities by city and destination"
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = errorMsg
                    )
                    
                    Timber.tag(TAG).w(errorMsg)
                }
                
            } catch (e: Exception) {
                val errorMsg = "Error filtering entities: ${e.message}"
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = errorMsg
                )
                
                Timber.tag(TAG).e(e, errorMsg)
            }
        }
    }
    
    /**
     * Filter entities by city and destination with visibility logic (respects eye button setting)
     */
    fun filterEntitiesByCityAndDestinationWithVisibility(
        allPassengerLocations: List<LatLng>,
        allDriverLocations: List<LatLng>,
        currentUserLocation: LatLng,
        destination: String,
        userType: String,
        showSameTypeEntities: Boolean
    ) {
        // Cancel any existing filtering job
        filteringJob?.cancel()
        
        filteringJob = viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, errorMessage = null)
                
                Timber.tag(TAG).d("Starting entity filtering with visibility - city+destination: $destination, userType: $userType, showSameType: $showSameTypeEntities")
                Timber.tag(TAG).d("Input entities - passengers: ${allPassengerLocations.size}, drivers: ${allDriverLocations.size}")
                
                // Determine which entities to include based on visibility setting
                val passengersToFilter = if (userType == "passenger" && !showSameTypeEntities) {
                    // If user is passenger and doesn't want to see other passengers, exclude passenger locations
                    emptyList()
                } else {
                    allPassengerLocations
                }
                
                val driversToFilter = if (userType == "driver" && !showSameTypeEntities) {
                    // If user is driver and doesn't want to see other drivers, exclude driver locations
                    emptyList()
                } else {
                    allDriverLocations
                }
                
                Timber.tag(TAG).d("After visibility filtering - passengers: ${passengersToFilter.size}, drivers: ${driversToFilter.size}")
                
                val filteredEntities = cityBasedOverviewService.filterEntitiesByCityAndDestination(
                    allPassengerLocations = passengersToFilter,
                    allDriverLocations = driversToFilter,
                    currentUserLocation = currentUserLocation,
                    destination = destination
                )
                
                if (filteredEntities != null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        filteredEntities = filteredEntities
                    )
                    
                    Timber.tag(TAG).i("Entity filtering with visibility successful for ${filteredEntities.cityName} ($destination): ${filteredEntities.totalPassengers} passengers, ${filteredEntities.totalDrivers} drivers")
                } else {
                    val errorMsg = "Could not filter entities by city and destination"
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = errorMsg
                    )
                    
                    Timber.tag(TAG).w(errorMsg)
                }
                
            } catch (e: Exception) {
                val errorMsg = "Error filtering entities with visibility: ${e.message}"
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = errorMsg
                )
                
                Timber.tag(TAG).e(e, errorMsg)
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
    
    /**
     * Clear cached data
     */
    fun clearCache() {
        cityBasedOverviewService.clearCache()
        _state.value = CityOverviewState()
        Timber.tag(TAG).d("Cache and state cleared")
    }
    
    /**
     * Get cache statistics
     */
    fun getCacheStats(): Map<String, Any> {
        return cityBasedOverviewService.getCacheStats()
    }
    
    override fun onCleared() {
        super.onCleared()
        filteringJob?.cancel()
        Timber.tag(TAG).d("CityBasedOverviewViewModel cleared")
    }
}