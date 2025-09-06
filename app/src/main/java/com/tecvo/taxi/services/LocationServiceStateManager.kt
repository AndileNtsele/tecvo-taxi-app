// LocationServiceStateManager.kt
package com.tecvo.taxi.services

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * State management for location service to prevent redundant operations
 * and coordinate between multiple location consumers.
 */
@Singleton
class LocationServiceStateManager @Inject constructor() {
    
    private val tag = "LocationServiceStateManager"
    
    /**
     * Represents location service operational state
     */
    data class LocationServiceState(
        val isActive: Boolean = false,
        val currentInterval: Long = 0L,
        val currentPriority: Int = 0,
        val activeConsumers: Set<String> = emptySet(),
        val lastStartTime: Long = 0L
    )
    
    // Current location service state
    private val _serviceState = MutableStateFlow(LocationServiceState())
    val serviceState: StateFlow<LocationServiceState> = _serviceState.asStateFlow()
    
    // Consumer tracking
    private val activeConsumers = mutableSetOf<String>()
    
    /**
     * Registers a consumer for location updates
     * Returns true if location service should start, false if already running
     */
    fun requestLocationUpdates(
        consumerId: String, 
        requestedInterval: Long, 
        requestedPriority: Int
    ): Boolean {
        val currentState = _serviceState.value
        
        // Add consumer to active set
        val wasEmpty = activeConsumers.isEmpty()
        activeConsumers.add(consumerId)
        
        return when {
            // Service not active and this is first consumer - start service
            !currentState.isActive && wasEmpty -> {
                updateState(
                    isActive = true,
                    currentInterval = requestedInterval,
                    currentPriority = requestedPriority,
                    activeConsumers = activeConsumers.toSet(),
                    lastStartTime = System.currentTimeMillis()
                )
                Timber.tag(tag).d("Starting location service for first consumer: $consumerId")
                true
            }
            
            // Service active but parameters different - restart needed
            currentState.isActive && (
                currentState.currentInterval != requestedInterval || 
                currentState.currentPriority != requestedPriority
            ) -> {
                updateState(
                    isActive = true,
                    currentInterval = requestedInterval,
                    currentPriority = requestedPriority,
                    activeConsumers = activeConsumers.toSet(),
                    lastStartTime = System.currentTimeMillis()
                )
                Timber.tag(tag).d("Restarting location service with new parameters for consumer: $consumerId")
                true
            }
            
            // Service already active with same parameters - no action needed
            currentState.isActive -> {
                updateState(activeConsumers = activeConsumers.toSet())
                Timber.tag(tag).d("Location service already active with same parameters, adding consumer: $consumerId")
                false
            }
            
            // Edge case - service should be active but isn't
            else -> {
                updateState(
                    isActive = true,
                    currentInterval = requestedInterval,
                    currentPriority = requestedPriority,
                    activeConsumers = activeConsumers.toSet(),
                    lastStartTime = System.currentTimeMillis()
                )
                Timber.tag(tag).w("Service was in unexpected state, starting for consumer: $consumerId")
                true
            }
        }
    }
    
    /**
     * Unregisters a consumer from location updates
     * Returns true if location service should stop, false if other consumers still active
     */
    fun releaseLocationUpdates(consumerId: String): Boolean {
        val removed = activeConsumers.remove(consumerId)
        
        if (!removed) {
            Timber.tag(tag).w("Attempted to remove non-existent consumer: $consumerId")
            return false
        }
        
        val shouldStop = activeConsumers.isEmpty()
        
        if (shouldStop) {
            updateState(
                isActive = false,
                currentInterval = 0L,
                currentPriority = 0,
                activeConsumers = emptySet(),
                lastStartTime = 0L
            )
            Timber.tag(tag).d("Stopping location service - no active consumers (removed: $consumerId)")
        } else {
            updateState(activeConsumers = activeConsumers.toSet())
            Timber.tag(tag).d("Removed consumer $consumerId, ${activeConsumers.size} consumers still active")
        }
        
        return shouldStop
    }
    
    /**
     * Checks if location service should be active
     */
    fun shouldServiceBeActive(): Boolean {
        return activeConsumers.isNotEmpty()
    }
    
    /**
     * Gets current active consumers
     */
    fun getActiveConsumers(): Set<String> {
        return activeConsumers.toSet()
    }
    
    /**
     * Forces service to stopped state (for error recovery)
     */
    fun forceStop() {
        activeConsumers.clear()
        updateState(
            isActive = false,
            currentInterval = 0L,
            currentPriority = 0,
            activeConsumers = emptySet(),
            lastStartTime = 0L
        )
        Timber.tag(tag).w("Location service state forcefully reset")
    }
    
    /**
     * Gets current service configuration
     */
    fun getCurrentConfiguration(): Triple<Long, Int, Set<String>>? {
        val state = _serviceState.value
        return if (state.isActive) {
            Triple(state.currentInterval, state.currentPriority, state.activeConsumers)
        } else {
            null
        }
    }
    
    /**
     * Checks if service is currently active
     */
    fun isServiceActive(): Boolean {
        return _serviceState.value.isActive
    }
    
    /**
     * Updates the internal state
     */
    private fun updateState(
        isActive: Boolean = _serviceState.value.isActive,
        currentInterval: Long = _serviceState.value.currentInterval,
        currentPriority: Int = _serviceState.value.currentPriority,
        activeConsumers: Set<String> = _serviceState.value.activeConsumers,
        lastStartTime: Long = _serviceState.value.lastStartTime
    ) {
        _serviceState.value = LocationServiceState(
            isActive = isActive,
            currentInterval = currentInterval,
            currentPriority = currentPriority,
            activeConsumers = activeConsumers,
            lastStartTime = lastStartTime
        )
    }
    
    /**
     * Gets debug information about current state
     */
    fun getDebugInfo(): String {
        val state = _serviceState.value
        return "LocationService State: active=${state.isActive}, " +
                "interval=${state.currentInterval}ms, priority=${state.currentPriority}, " +
                "consumers=[${state.activeConsumers.joinToString()}], " +
                "startTime=${state.lastStartTime}"
    }
}