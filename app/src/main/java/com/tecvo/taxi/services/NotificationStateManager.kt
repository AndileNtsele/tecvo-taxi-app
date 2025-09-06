// NotificationStateManager.kt
package com.tecvo.taxi.services

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * State management for notification service to prevent redundant operations
 * and ensure consistent state transitions.
 */
@Singleton
class NotificationStateManager @Inject constructor() {
    
    private val tag = "NotificationStateManager"
    
    /**
     * Represents the current state of notification monitoring
     */
    sealed class NotificationState {
        object Stopped : NotificationState()
        data class Starting(val userId: String, val userType: String, val destination: String) : NotificationState()
        data class Active(val userId: String, val userType: String, val destination: String) : NotificationState()
        object Stopping : NotificationState()
        
        override fun toString(): String {
            return when (this) {
                is Stopped -> "Stopped"
                is Starting -> "Starting(user=$userId, type=$userType, dest=$destination)"
                is Active -> "Active(user=$userId, type=$userType, dest=$destination)"
                is Stopping -> "Stopping"
            }
        }
    }
    
    // Current notification state
    private val _currentState = MutableStateFlow<NotificationState>(NotificationState.Stopped)
    val currentState: StateFlow<NotificationState> = _currentState.asStateFlow()
    
    /**
     * Attempts to start monitoring if not already started with same parameters
     * Returns true if monitoring should proceed, false if already active or starting
     */
    fun requestStartMonitoring(userId: String, userType: String, destination: String): Boolean {
        val currentState = _currentState.value
        
        return when (currentState) {
            is NotificationState.Stopped -> {
                _currentState.value = NotificationState.Starting(userId, userType, destination)
                Timber.tag(tag).d("State transition: Stopped -> Starting($userId, $userType, $destination)")
                true
            }
            is NotificationState.Starting -> {
                // Check if it's the same request
                if (currentState.userId == userId && 
                    currentState.userType == userType && 
                    currentState.destination == destination) {
                    Timber.tag(tag).d("Ignoring duplicate start request for same parameters")
                    false
                } else {
                    // Different parameters, need to restart
                    _currentState.value = NotificationState.Starting(userId, userType, destination)
                    Timber.tag(tag).d("State transition: Starting (different params) -> Starting($userId, $userType, $destination)")
                    true
                }
            }
            is NotificationState.Active -> {
                // Check if parameters are different
                if (currentState.userId == userId && 
                    currentState.userType == userType && 
                    currentState.destination == destination) {
                    Timber.tag(tag).d("Monitoring already active with same parameters, ignoring request")
                    false
                } else {
                    // Parameters changed, need to restart
                    _currentState.value = NotificationState.Starting(userId, userType, destination)
                    Timber.tag(tag).d("State transition: Active (different params) -> Starting($userId, $userType, $destination)")
                    true
                }
            }
            is NotificationState.Stopping -> {
                // Wait for stop to complete, then start with new parameters
                _currentState.value = NotificationState.Starting(userId, userType, destination)
                Timber.tag(tag).d("State transition: Stopping -> Starting($userId, $userType, $destination)")
                true
            }
        }
    }
    
    /**
     * Marks monitoring as successfully started
     */
    fun markMonitoringStarted(userId: String, userType: String, destination: String) {
        val currentState = _currentState.value
        if (currentState is NotificationState.Starting &&
            currentState.userId == userId &&
            currentState.userType == userType &&
            currentState.destination == destination) {
            
            _currentState.value = NotificationState.Active(userId, userType, destination)
            Timber.tag(tag).d("State transition: Starting -> Active($userId, $userType, $destination)")
        } else {
            Timber.tag(tag).w("Unexpected state when marking started: $currentState")
        }
    }
    
    /**
     * Attempts to stop monitoring
     * Returns true if stopping should proceed, false if already stopped or stopping
     */
    fun requestStopMonitoring(): Boolean {
        val currentState = _currentState.value
        
        return when (currentState) {
            is NotificationState.Stopped -> {
                Timber.tag(tag).d("Already stopped, ignoring stop request")
                false
            }
            is NotificationState.Starting -> {
                _currentState.value = NotificationState.Stopping
                Timber.tag(tag).d("State transition: Starting -> Stopping")
                true
            }
            is NotificationState.Active -> {
                _currentState.value = NotificationState.Stopping
                Timber.tag(tag).d("State transition: Active -> Stopping")
                true
            }
            is NotificationState.Stopping -> {
                Timber.tag(tag).d("Already stopping, ignoring stop request")
                false
            }
        }
    }
    
    /**
     * Marks monitoring as successfully stopped
     */
    fun markMonitoringStopped() {
        _currentState.value = NotificationState.Stopped
        Timber.tag(tag).d("State transition: -> Stopped")
    }
    
    /**
     * Force reset state (for error recovery)
     */
    fun resetState() {
        _currentState.value = NotificationState.Stopped
        Timber.tag(tag).w("State forcefully reset to Stopped")
    }
    
    /**
     * Get current monitoring parameters if active
     */
    fun getCurrentMonitoringInfo(): Triple<String, String, String>? {
        return when (val state = _currentState.value) {
            is NotificationState.Active -> Triple(state.userId, state.userType, state.destination)
            is NotificationState.Starting -> Triple(state.userId, state.userType, state.destination)
            else -> null
        }
    }
    
    /**
     * Check if monitoring is currently active
     */
    fun isMonitoringActive(): Boolean {
        return _currentState.value is NotificationState.Active
    }
    
    /**
     * Check if monitoring is in any active state (starting or active)
     */
    fun isMonitoringActiveOrStarting(): Boolean {
        val state = _currentState.value
        return state is NotificationState.Active || state is NotificationState.Starting
    }
}