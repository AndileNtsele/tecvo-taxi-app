package com.tecvo.taxi.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecvo.taxi.repository.AuthRepository
import com.tecvo.taxi.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val TAG = "HomeViewModel"
/**
 * ViewModel for the Home screen that handles user role selection and user state
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {
    // User login status
    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()
    // Last selected role (can be used to restore state if user returns to screen)
    private val _lastSelectedRole = MutableStateFlow<String?>(null)
    val lastSelectedRole: StateFlow<String?> = _lastSelectedRole.asStateFlow()
    // Loading states
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    init {
        checkLoginStatus()
        loadLastSelectedRole()
    }
    /**
     * Check if user is logged in
     */
    private fun checkLoginStatus() {
        viewModelScope.launch {
            try {
                _isUserLoggedIn.value = authRepository.isUserLoggedIn()
                Timber.tag(TAG).d("User logged in status: ${_isUserLoggedIn.value}")
            } catch (e: Exception) {
                Timber.tag(TAG).e("Error checking login status: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    /**
     * Load the user's last selected role from preferences
     */
    private fun loadLastSelectedRole() {
        viewModelScope.launch {
            try {
                val role = preferencesRepository.getLastSelectedRole()
                _lastSelectedRole.value = role
                Timber.tag(TAG).d("Last selected role: $role")
            } catch (e: Exception) {
                Timber.tag(TAG).e("Error loading last selected role: ${e.message}")
            }
        }
    }
    /**
     * Save selected role when user makes a selection
     */
    fun saveSelectedRole(role: String) {
        viewModelScope.launch {
            try {
                preferencesRepository.saveLastSelectedRole(role)
                _lastSelectedRole.value = role
                Timber.tag(TAG).d("Saved selected role: $role")
            } catch (e: Exception) {
                Timber.tag(TAG).e("Error saving selected role: ${e.message}")
            }
        }
    }
    /**

    File - D:\TAXI\app\src\main\java\com\example\taxi\viewmodel\HomeViewModel.kt
    Page 2 of 2
     * Sign out the current user
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                _isUserLoggedIn.value = false
                Timber.tag(TAG).d("User signed out successfully")
            } catch (e: Exception) {
                Timber.tag(TAG).e("Error signing out: ${e.message}")
            }
        }
    }
}