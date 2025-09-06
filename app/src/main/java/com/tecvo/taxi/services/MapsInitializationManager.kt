// MapsInitializationManager.kt
package com.tecvo.taxi.services

import android.content.Context
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Google Maps initialization to prevent multiple retry attempts
 * and ensure reliable map functionality across the application.
 */
@Singleton
class MapsInitializationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val tag = "MapsInitializationManager"
    private val initScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val initMutex = Mutex()
    
    /**
     * Represents the state of Maps initialization
     */
    sealed class InitializationState {
        object NotStarted : InitializationState()
        object InProgress : InitializationState()
        object Completed : InitializationState()
        data class Failed(val error: String, val retryCount: Int) : InitializationState()
        
        override fun toString(): String {
            return when (this) {
                is NotStarted -> "NotStarted"
                is InProgress -> "InProgress"
                is Completed -> "Completed"
                is Failed -> "Failed(error=$error, retries=$retryCount)"
            }
        }
    }
    
    // Current initialization state
    private val _initializationState = MutableStateFlow<InitializationState>(InitializationState.NotStarted)
    val initializationState: StateFlow<InitializationState> = _initializationState.asStateFlow()
    
    // Track initialization attempts
    private var initializationAttempts = 0
    private val maxInitializationAttempts = 5
    
    /**
     * Ensures Google Maps is properly initialized with robust error handling
     * Returns true if initialization is successful, false otherwise
     */
    suspend fun ensureInitialized(): Boolean {
        return initMutex.withLock {
            when (val currentState = _initializationState.value) {
                is InitializationState.Completed -> {
                    Timber.tag(tag).d("Maps already initialized")
                    true
                }
                is InitializationState.InProgress -> {
                    Timber.tag(tag).d("Maps initialization already in progress, waiting...")
                    waitForInitialization()
                }
                is InitializationState.NotStarted, is InitializationState.Failed -> {
                    performInitialization()
                }
            }
        }
    }
    
    /**
     * Performs the actual initialization with retry logic
     */
    private suspend fun performInitialization(): Boolean {
        if (initializationAttempts >= maxInitializationAttempts) {
            Timber.tag(tag).e("Maximum initialization attempts reached ($maxInitializationAttempts), giving up")
            _initializationState.value = InitializationState.Failed(
                "Max attempts reached", 
                initializationAttempts
            )
            return false
        }
        
        _initializationState.value = InitializationState.InProgress
        initializationAttempts++
        
        Timber.tag(tag).d("Starting Maps initialization attempt $initializationAttempts/$maxInitializationAttempts")
        
        return try {
            // Initialize with timeout to prevent hanging
            val success = withTimeoutOrNull(10000L) { // 10 second timeout
                initializeGoogleMaps()
            } ?: false
            
            if (success) {
                _initializationState.value = InitializationState.Completed
                Timber.tag(tag).i("Google Maps initialization completed successfully")
                true
            } else {
                val errorMsg = "Initialization timeout or failure"
                _initializationState.value = InitializationState.Failed(errorMsg, initializationAttempts)
                Timber.tag(tag).w("Maps initialization failed: $errorMsg")
                false
            }
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Unknown error"
            _initializationState.value = InitializationState.Failed(errorMsg, initializationAttempts)
            Timber.tag(tag).e("Maps initialization error: $errorMsg")
            false
        }
    }
    
    /**
     * Core initialization logic
     */
    private suspend fun initializeGoogleMaps(): Boolean {
        return withContext(Dispatchers.Main) {
            try {
                // Initialize MapsInitializer first
                MapsInitializer.initialize(context.applicationContext)
                
                // Test CameraUpdateFactory with exponential backoff
                var retryDelay = 100L
                var testAttempts = 0
                val maxTestAttempts = 10
                
                while (testAttempts < maxTestAttempts) {
                    if (testCameraUpdateFactory()) {
                        Timber.tag(tag).d("CameraUpdateFactory ready after $testAttempts test attempts")
                        return@withContext true
                    }
                    
                    testAttempts++
                    delay(retryDelay)
                    retryDelay = minOf(retryDelay * 2, 2000L) // Cap at 2 seconds
                    
                    Timber.tag(tag).d("CameraUpdateFactory test attempt $testAttempts/$maxTestAttempts")
                }
                
                Timber.tag(tag).w("CameraUpdateFactory not ready after $maxTestAttempts test attempts")
                false
                
            } catch (e: Exception) {
                Timber.tag(tag).e("Error during Maps initialization: ${e.message}")
                false
            }
        }
    }
    
    /**
     * Tests if CameraUpdateFactory is working properly
     */
    private fun testCameraUpdateFactory(): Boolean {
        return try {
            // Test with a simple operation
            val testUpdate = CameraUpdateFactory.newLatLng(LatLng(0.0, 0.0))
            testUpdate != null
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Waits for ongoing initialization to complete
     */
    private suspend fun waitForInitialization(): Boolean {
        var waitTime = 0L
        val maxWaitTime = 15000L // 15 seconds max wait
        val checkInterval = 100L
        
        while (waitTime < maxWaitTime) {
            when (val state = _initializationState.value) {
                is InitializationState.Completed -> return true
                is InitializationState.Failed -> return false
                is InitializationState.InProgress -> {
                    delay(checkInterval)
                    waitTime += checkInterval
                }
                is InitializationState.NotStarted -> {
                    // Shouldn't happen, but handle gracefully
                    Timber.tag(tag).w("Unexpected state transition to NotStarted while waiting")
                    return false
                }
            }
        }
        
        Timber.tag(tag).w("Timeout waiting for Maps initialization")
        return false
    }
    
    /**
     * Resets initialization state (for error recovery)
     */
    fun resetInitialization() {
        initScope.launch {
            initMutex.withLock {
                _initializationState.value = InitializationState.NotStarted
                initializationAttempts = 0
                Timber.tag(tag).i("Maps initialization state reset")
            }
        }
    }
    
    /**
     * Checks if Maps is currently initialized
     */
    fun isInitialized(): Boolean {
        return _initializationState.value is InitializationState.Completed
    }
    
    /**
     * Preemptively initialize Maps (call during app startup)
     */
    fun preInitialize() {
        initScope.launch {
            if (_initializationState.value is InitializationState.NotStarted) {
                Timber.tag(tag).d("Starting preemptive Maps initialization")
                ensureInitialized()
            }
        }
    }
    
    /**
     * Get initialization statistics for debugging
     */
    fun getInitializationStats(): String {
        val state = _initializationState.value
        return "State: $state, Attempts: $initializationAttempts/$maxInitializationAttempts"
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        try {
            initScope.cancel("Service stopped")
            Timber.tag(tag).d("MapsInitializationManager cleaned up")
        } catch (e: Exception) {
            Timber.tag(tag).e("Error during cleanup: ${e.message}")
        }
    }
}