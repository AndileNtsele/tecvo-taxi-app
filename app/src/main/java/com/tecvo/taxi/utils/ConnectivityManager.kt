package com.tecvo.taxi.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Manages network connectivity state and provides observable flows
 * for network status changes.
 */
class ConnectivityManager(context: Context) {
    private val tag = "ConnectivityManager"

    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _showOfflineMessage = MutableStateFlow(false)
    val showOfflineMessage: StateFlow<Boolean> = _showOfflineMessage.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Timber.tag(tag).d("Network available")
            _isOnline.value = true
        }

        override fun onLost(network: Network) {
            Timber.tag(tag).d("Network lost")
            _isOnline.value = false
            _showOfflineMessage.value = true

            // Auto-hide offline message after 5 seconds
            scope.launch {
                delay(5000)
                _showOfflineMessage.value = false
            }
        }
    }

    init {
        // Initialize current status
        _isOnline.value = isNetworkAvailable()
        Timber.tag(tag).d("Initial network status: ${if (_isOnline.value) "online" else "offline"}")

        // Register for network callbacks
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    fun isNetworkAvailable(): Boolean {
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        return actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun cleanup() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
            Timber.tag(tag).d("Network callback unregistered")
        } catch (e: Exception) {
            Timber.tag(tag).e("Error unregistering network callback: ${e.message}")
        }
    }
}