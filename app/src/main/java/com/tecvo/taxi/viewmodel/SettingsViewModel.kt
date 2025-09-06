package com.tecvo.taxi.viewmodel
import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecvo.taxi.models.Country
import com.tecvo.taxi.repository.AuthRepository
import com.tecvo.taxi.repository.UserPreferencesRepository
import com.tecvo.taxi.utils.CountryUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val TAG = "SettingsViewModel"
/**
 * ViewModel for the Settings screen that handles user preferences and account management
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {
    // Notification settings
    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()
    private val _notifyDifferentRole = MutableStateFlow(true)
    val notifyDifferentRole: StateFlow<Boolean> = _notifyDifferentRole.asStateFlow()
    private val _notifySameRole = MutableStateFlow(false)
    val notifySameRole: StateFlow<Boolean> = _notifySameRole.asStateFlow()
    private val _notifyProximity = MutableStateFlow(false)
    val notifyProximity: StateFlow<Boolean> = _notifyProximity.asStateFlow()
    private val _notificationRadius = MutableStateFlow(1.0f)
    val notificationRadius: StateFlow<Float> = _notificationRadius.asStateFlow()
    // Account deletion
    private val _accountDeletionStep = MutableStateFlow(0)
    val accountDeletionStep: StateFlow<Int> = _accountDeletionStep.asStateFlow()
    private val _isProcessingDeletion = MutableStateFlow(false)
    val isProcessingDeletion: StateFlow<Boolean> = _isProcessingDeletion.asStateFlow()
    // Phone verification for account deletion
    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()
    private val _otpCode = MutableStateFlow("")
    val otpCode: StateFlow<String> = _otpCode.asStateFlow()
    private val _selectedCountry = MutableStateFlow(CountryUtils.DEFAULT_COUNTRY)
    val selectedCountry: StateFlow<Country> = _selectedCountry.asStateFlow()
    private val _verificationId = MutableStateFlow("")
    val verificationId: StateFlow<String> = _verificationId.asStateFlow()
    // Error states
    private val _phoneError = MutableStateFlow("")
    val phoneError: StateFlow<String> = _phoneError.asStateFlow()
    private val _otpError = MutableStateFlow("")
    val otpError: StateFlow<String> = _otpError.asStateFlow()
    init {
// Load initial notification settings
        loadNotificationSettings()
// Set up observers for preference changes
        observePreferenceChanges()
    }
    /**
     * Set up observers for all preference changes
     */
    private fun observePreferenceChanges() {
// Observe notification radius changes
        viewModelScope.launch {
            preferencesRepository.notificationRadiusFlow.collect { radius ->
                if (_notificationRadius.value != radius) {
                    Timber.tag(TAG).d("Notification radius changed from preferences: %s", radius)
                    _notificationRadius.value = radius
                }
            }
        }
// Observe notifications enabled setting
        viewModelScope.launch {
            preferencesRepository.notificationsEnabledFlow.collect { enabled ->
                if (_notificationsEnabled.value != enabled) {
                    Timber.tag(TAG).d("Notifications enabled setting changed: %s", enabled)
                    _notificationsEnabled.value = enabled
                }
            }
        }
// Observe different role notifications setting
        viewModelScope.launch {
            preferencesRepository.notifyDifferentRoleFlow.collect { enabled ->
                if (_notifyDifferentRole.value != enabled) {
                    Timber.tag(TAG).d("Notify different role setting changed: %s", enabled)
                    _notifyDifferentRole.value = enabled
                }
            }
        }
// Observe same role notifications setting
        viewModelScope.launch {
            preferencesRepository.notifySameRoleFlow.collect { enabled ->
                if (_notifySameRole.value != enabled) {
                    Timber.tag(TAG).d("Notify same role setting changed: %s", enabled)
                    _notifySameRole.value = enabled
                }
            }
        }
// Observe proximity notifications setting
        viewModelScope.launch {
            preferencesRepository.notifyProximityFlow.collect { enabled ->
                if (_notifyProximity.value != enabled) {
                    Timber.tag(TAG).d("Notify proximity setting changed: %s", enabled)
                    _notifyProximity.value = enabled
                }
            }
        }
    }
    /**
     * Load notification settings from preferences
     */
    private fun loadNotificationSettings() {
        viewModelScope.launch {
            try {
                _notificationsEnabled.value = preferencesRepository.areNotificationsEnabled()
                _notifyDifferentRole.value = preferencesRepository.areDifferentRoleNotificationsEnabled()
                _notifySameRole.value = preferencesRepository.areSameRoleNotificationsEnabled()
                _notifyProximity.value = preferencesRepository.areProximityNotificationsEnabled()
                _notificationRadius.value = preferencesRepository.getNotificationRadius()
                Timber.tag(TAG).d(
                    "Loaded notification settings: enabled=%s, differentRole=%s, sameRole=%s, proximity=%s, radius=%s",
                    _notificationsEnabled.value,
                    _notifyDifferentRole.value,
                    _notifySameRole.value,
                    _notifyProximity.value,
                    _notificationRadius.value
                )
            } catch (e: Exception) {
                Timber.tag(TAG).e("Error loading notification settings: %s", e.message)
            }
        }
    }
    /**
     * Save all notification settings
     */
    fun saveNotificationSettings(
        enabled: Boolean,
        notifyDifferentRole: Boolean,
        notifySameRole: Boolean,
        proximityEnabled: Boolean,
        radiusKm: Float
    ) {
        viewModelScope.launch {
            try {
                preferencesRepository.saveNotificationSettings(
                    enabled = enabled,
                    notifyDifferentRole = notifyDifferentRole,
                    notifySameRole = notifySameRole,
                    proximityEnabled = proximityEnabled,
                    radiusKm = radiusKm
                )
// The values will be updated via flow collection, no need to manually update
                Timber.tag(TAG).d("Saved notification settings")
            } catch (e: Exception) {
                Timber.tag(TAG).e("Error saving notification settings: %s", e.message)
            }
        }
    }
    /**
     * Update notification radius
     */
    fun updateNotificationRadius(radius: String) {
        val radiusValue = radius.toFloatOrNull() ?: return
        viewModelScope.launch {
            preferencesRepository.setNotificationRadius(radiusValue)
// The value will be updated via flow collection
        }
    }
    /**
     * Update phone number for verification
     */
    fun updatePhoneNumber(number: String) {
        _phoneNumber.value = number
        _phoneError.value = ""
    }
    /**
     * Update OTP code
     */
    fun updateOtpCode(code: String) {
        if (code.length <= 6) {
            _otpCode.value = code
            _otpError.value = ""
        }
    }
    /**
     * Update selected country
     */
    fun updateSelectedCountry(country: Country) {
        _selectedCountry.value = country
    }
    /**
     * Start account deletion process
     */
    fun startAccountDeletion() {
        _accountDeletionStep.value = 1
    }
    /**
     * Move to next step in account deletion
     */
    fun nextAccountDeletionStep() {
        _accountDeletionStep.value += 1
    }
    /**
     * Cancel account deletion
     */
    fun cancelAccountDeletion() {
        _accountDeletionStep.value = 0
        _phoneNumber.value = ""
        _otpCode.value = ""
        _phoneError.value = ""
        _otpError.value = ""
    }
    /**
     * Send phone verification code
     */
    fun sendVerificationCode(activity: Activity) {
        if (_phoneNumber.value.isEmpty()) {
            _phoneError.value = "Please enter a phone number"
            return
        }
        val formattedPhoneNumber = formatPhoneNumberForAuth(_phoneNumber.value, _selectedCountry.value)
        viewModelScope.launch {
            try {
                _phoneError.value = ""
                authRepository.verifyPhoneNumber(
                    phoneNumber = formattedPhoneNumber,
                    activity = activity,
                    onVerificationIdReceived = { id ->
                        _verificationId.value = id
                        _accountDeletionStep.value = 2
                        Timber.tag(TAG).d("Verification code sent successfully")
                    },
                    onVerificationCompleted = { credential ->
// Auto verification completed
                        viewModelScope.launch {
                            try {
                                authRepository.signInWithCredential(credential)
                                deleteUserAccount()
                            } catch (e: Exception) {
                                _phoneError.value = e.message ?: "Authentication failed"
                                Timber.tag(TAG).e("Auto-verification failed: %s", e.message)
                            }
                        }
                    },
                    onVerificationFailed = { e ->
                        _phoneError.value = e.message ?: "Verification failed"
                        Timber.tag(TAG).e("Phone verification failed: %s", e.message)
                    }
                )
            } catch (e: Exception) {
                _phoneError.value = "Error: ${e.message}"
                Timber.tag(TAG).e("Error sending verification code: %s", e.message)
            }
        }
    }
    /**
     * Verify OTP code
     */
    fun verifyOtpCode() {
        if (_otpCode.value.length != 6) {
            _otpError.value = "Please enter a valid 6-digit code"
            return
        }
        _isProcessingDeletion.value = true
        _otpError.value = ""
        viewModelScope.launch {
            try {
                authRepository.verifyOtpCode(_verificationId.value, _otpCode.value)
                deleteUserAccount()
            } catch (e: Exception) {
                _isProcessingDeletion.value = false
                _otpError.value = e.message ?: "Invalid verification code"
                Timber.tag(TAG).e("OTP verification failed: %s", e.message)
            }
        }
    }
    /**
     * Sign out the current user (for development/testing)
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                Timber.tag(TAG).d("User signed out successfully")
            } catch (e: Exception) {
                Timber.tag(TAG).e("Error signing out: %s", e.message)
            }
        }
    }

    /**
     * Delete user account after verification
     */
    private suspend fun deleteUserAccount() {
        try {
            authRepository.deleteUserAccount().fold(
                onSuccess = {
                    _isProcessingDeletion.value = false
                    _accountDeletionStep.value = 0
                    Timber.tag(TAG).d("User account deleted successfully")
                },
                onFailure = { e ->
                    _isProcessingDeletion.value = false
                    _otpError.value = "Failed to delete account: ${e.message}"
                    Timber.tag(TAG).e("Failed to delete account: %s", e.message)
                }
            )
        } catch (e: Exception) {
            _isProcessingDeletion.value = false
            _otpError.value = "Error: ${e.message}"
            Timber.tag(TAG).e("Error deleting account: %s", e.message)
        }
    }
    /**
     * Format phone number for authentication
     */
    private fun formatPhoneNumberForAuth(localNumber: String, country: Country): String {
// Remove spaces, hyphens, and parentheses
        val cleanNumber = localNumber.replace(Regex("[\\s-()]"), "")
// If the number starts with a zero, remove it before adding the country code
        val trimmedNumber = if (cleanNumber.startsWith("0")) {
            cleanNumber.substring(1)
        } else {
            cleanNumber
        }
// Return the full international format
        return "${country.dialCode}$trimmedNumber"
    }
}