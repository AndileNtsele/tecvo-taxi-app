package com.tecvo.taxi.viewmodel
import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecvo.taxi.models.Country
import com.tecvo.taxi.repository.AuthRepository
import com.tecvo.taxi.utils.CountryUtils
import com.google.firebase.auth.PhoneAuthCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val TAG = "LoginViewModel"
/**
 * LoginViewModel handles all authentication logic and state management
 * for the login/registration process.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    // Login state
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()
    // Phone verification
    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()
    private val _otp = MutableStateFlow("")
    val otp: StateFlow<String> = _otp.asStateFlow()
    private val _isOtpSent = MutableStateFlow(false)
    val isOtpSent: StateFlow<Boolean> = _isOtpSent.asStateFlow()
    private val _verificationId = MutableStateFlow<String?>(null)

    // Country selection
    private val _selectedCountry = MutableStateFlow(CountryUtils.DEFAULT_COUNTRY)
    val selectedCountry: StateFlow<Country> = _selectedCountry.asStateFlow()
    // Terms and conditions
    private val _termsAccepted = MutableStateFlow(false)
    val termsAccepted: StateFlow<Boolean> = _termsAccepted.asStateFlow()
    // Error handling
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    // Login process
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    // UI Animation state
    private val _showLoginForm = MutableStateFlow(false)
    val showLoginForm: StateFlow<Boolean> = _showLoginForm.asStateFlow()
    init {
// Check if user is already logged in
        viewModelScope.launch {
            _isLoggedIn.value = repository.isUserLoggedIn()
            Timber.tag(TAG).d("User login status initialized: ${_isLoggedIn.value}")
        }
    }
    /**
     * Check if network connectivity is available
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        return actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    /**
     * Update phone number state
     */
    fun updatePhoneNumber(newValue: String) {
        _phoneNumber.value = newValue
        _error.value = null
    }
    /**
     * Update OTP state
     */
    fun updateOtp(newValue: String) {
        if (newValue.length <= 6) {
            _otp.value = newValue
            _error.value = null
        }
    }
    /**
     * Update selected country
     */
    fun updateSelectedCountry(country: Country) {
        _selectedCountry.value = country
    }
    /**
     * Toggle terms acceptance state
     */
    fun toggleTerms(accepted: Boolean) {
        _termsAccepted.value = accepted
    }
    /**
     * Show the login form with animation
     */
    fun showLoginForm() {
        _showLoginForm.value = true
    }
    /**
     * Initiate phone verification process
     */
    fun verifyPhoneNumber(activity: Activity) {
        if (!isNetworkAvailable()) {
            _error.value = "Internet connection required to register. Please connect to the internet and try again."
            return
        }
        if (!isPhoneNumberValid()) {
            _error.value = "Please enter a valid phone number"
            return
        }
        if (!_termsAccepted.value) {
            _error.value = "Please accept the terms and conditions"
            return
        }
        _loginState.value = LoginState.Loading
        val formattedNumber = formatPhoneNumberForAuth(_phoneNumber.value, _selectedCountry.value)
        Timber.tag(TAG).i("Initiating phone verification for $formattedNumber")
        viewModelScope.launch {
            repository.verifyPhoneNumber(
                phoneNumber = formattedNumber,
                activity = activity,
                onVerificationIdReceived = { id ->
                    _verificationId.value = id
                    _isOtpSent.value = true
                    _loginState.value = LoginState.Idle
                    _error.value = null
                    Timber.tag(TAG).i("Verification code sent successfully")
                },
                onVerificationCompleted = { credential ->
                    handleSignInWithCredential(credential)
                },
                onVerificationFailed = { e ->
                    _error.value = e.message ?: "Verification failed"
                    _loginState.value = LoginState.Error(e.message ?: "Verification failed")
                    Timber.tag(TAG).e("Phone verification error: ${e.message}")
// For session storage errors, provide a more user-friendly message
                    if (e.message?.contains("missing initial state") == true ||
                        e.message?.contains("Session storage error") == true) {
                        _error.value = "Authentication error: Please try closing your browser completely and reopening the app"
                    }
                }
            )
        }
    }
    /**
     * Handle sign-in with credential
     */
    private fun handleSignInWithCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val result = repository.signInWithCredential(credential)
                if (result.isSuccess) {
                    _isLoggedIn.value = true
                    _loginState.value = LoginState.Success
                    Timber.tag(TAG).i("Auto-verification completed successfully")
                } else {
                    val exception = result.exceptionOrNull()
                    _error.value = exception?.message ?: "Authentication failed"
                    _loginState.value = LoginState.Error(exception?.message ?: "Authentication failed")
                    Timber.tag(TAG).e("Auto-verification failed: ${exception?.message}")
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Authentication failed"
                _loginState.value = LoginState.Error(e.message ?: "Authentication failed")
                Timber.tag(TAG).e("Exception during authentication: ${e.message}")
            }
        }
    }
    /**
     * Verify OTP code after it's sent
     */
    fun verifyOtpCode() {
        if (!isNetworkAvailable()) {
            _error.value = "Internet connection required to verify code. Please connect to the internet and try again."
            return
        }
        if (_otp.value.length != 6) {
            _error.value = "Please enter a valid 6-digit code"
            return
        }
        val verificationId = _verificationId.value
        if (verificationId == null) {
            _error.value = "Verification process error. Please try again."
            return
        }
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            try {
                val result = repository.verifyOtpCode(verificationId, _otp.value)
                if (result.isSuccess) {
                    _isLoggedIn.value = true
                    _loginState.value = LoginState.Success
                    Timber.tag(TAG).i("OTP verification successful")
                } else {
                    val exception = result.exceptionOrNull()
                    _error.value = exception?.message ?: "Invalid verification code"
                    _loginState.value = LoginState.Error(exception?.message ?: "Invalid verification code")
                    Timber.tag(TAG).e("OTP verification failed: ${exception?.message}")
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Invalid verification code"
                _loginState.value = LoginState.Error(e.message ?: "Error during verification")
                Timber.tag(TAG).e("Exception during OTP verification: ${e.message}")
            }
        }
    }
    /**
     * Check if the phone number is valid
     */
    private fun isPhoneNumberValid(): Boolean {
        return CountryUtils.isValidLocalPhoneNumber(_phoneNumber.value, _selectedCountry.value)
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
/**
 * States for the login process
 */
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}