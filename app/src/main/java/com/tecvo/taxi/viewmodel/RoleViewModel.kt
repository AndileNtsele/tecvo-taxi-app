package com.tecvo.taxi.viewmodel
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecvo.taxi.repository.UserPreferencesRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val TAG = "RoleViewModel"
/**
 * ViewModel for the Role selection screen that handles user selection
 * between town and local destinations.
 */
@HiltViewModel
class RoleViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    // Current role (driver or passenger)
    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole.asStateFlow()
    // Last selected destination
    private val _lastDestination = MutableStateFlow<String?>(null)
    val lastDestination: StateFlow<String?> = _lastDestination.asStateFlow()
    // Current location state
    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation.asStateFlow()
    // Loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    init {
        loadLastDestination()
    }
    /**
     * Set the user's role (driver or passenger)
     */
    fun setUserRole(role: String) {
        viewModelScope.launch {
            try {
                _userRole.value = role
                userPreferencesRepository.saveLastSelectedRole(role)
                Timber.tag(TAG).d("User role set to: $role")
            } catch (e: Exception) {
                Timber.tag(TAG).e("Error setting user role: ${e.message}")
            }
        }
    }
    /**
     * Load the last selected destination from preferences
     */
    private fun loadLastDestination() {
        viewModelScope.launch {
            try {
                val destination = userPreferencesRepository.getLastSelectedDestination()
                _lastDestination.value = destination
                _isLoading.value = false
                Timber.tag(TAG).d("Loaded last destination: $destination")
            } catch (e: Exception) {
                Timber.tag(TAG).e("Error loading last destination: ${e.message}")
                _isLoading.value = false
            }
        }
    }
    /**
     * Save the selected destination (town or local)
     */
    fun saveDestination(destination: String) {
        viewModelScope.launch {
            try {
                userPreferencesRepository.saveLastSelectedDestination(destination)
                _lastDestination.value = destination
                Timber.tag(TAG).d("Saved destination: $destination")
            } catch (e: Exception) {
                Timber.tag(TAG).e("Error saving destination: ${e.message}")
            }
        }
    }
    /**
     * Update current location
     */
    fun updateLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        _currentLocation.value = latLng
        Timber.tag(TAG)
            .d("Updated current location to: (${location.latitude}, ${location.longitude})")
    }
    fun updateLocation(latLng: LatLng) {
        _currentLocation.value = latLng
        Timber.tag(TAG).d("Updated current location to: (${latLng.latitude}, ${latLng.longitude})")
    }
}
