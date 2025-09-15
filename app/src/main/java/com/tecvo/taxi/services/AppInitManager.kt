package com.tecvo.taxi.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AppInitManager @Inject constructor(
    private val locationService: LocationService,
    private val notificationService: NotificationService,
    private val analyticsManager: AnalyticsManager
) {
    private val tag = "AppInitManager"
    private val initScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun initialize(onComplete: () -> Unit) {
        initScope.launch {
            Timber.tag(tag).i("Starting app initialization sequence")
            
            try {
                // Log analytics event in separate coroutine to prevent blocking
                launch { 
                    try {
                        analyticsManager.logEvent("app_initialization_started")
                    } catch (e: Exception) {
                        Timber.tag(tag).w("Failed to log initialization start: ${e.message}")
                    }
                }

                // Initialize Firebase first (critical) - but with timeout
                val firebaseJob = async { initializeFirebase() }
                
                // Initialize other services in parallel with Firebase
                val serviceJobs = listOf(
                    async { initializeLocationServices() },
                    async { initializeNotifications() },
                    async { initializeAnalytics() }
                )

                // Wait for Firebase first, then other services
                firebaseJob.await()
                serviceJobs.awaitAll()

                // Complete initialization on main thread
                withContext(Dispatchers.Main) {
                    Timber.tag(tag).i("App initialization completed successfully")
                    onComplete()
                }
                
                // Log completion event after callback to prevent blocking
                launch {
                    try {
                        analyticsManager.logEvent("app_initialization_completed")
                    } catch (e: Exception) {
                        Timber.tag(tag).w("Failed to log initialization completion: ${e.message}")
                    }
                }
                
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "Error during app initialization: ${e.message}")
                
                // Log error in background to prevent blocking
                launch {
                    try {
                        analyticsManager.logEvent("app_initialization_failed", mapOf(
                            "error" to (e.message ?: "Unknown error")
                        ))
                    } catch (analyticsError: Exception) {
                        Timber.tag(tag).w("Failed to log initialization error: ${analyticsError.message}")
                    }
                }
                
                withContext(Dispatchers.Main) {
                    onComplete() // Still call complete to prevent app hanging
                }
            }
        }
    }

    private suspend fun initializeFirebase() {
        // Firebase is already initialized in TaxiApplication
        // Reduced delay to improve startup performance
        delay(50) // Reduced from 100ms to 50ms
    }

    private suspend fun initializeLocationServices() {
        try {
            // Pre-initialize any needed location components
            locationService.prepareLocationUpdates()
        } catch (e: Exception) {
            Timber.tag(tag).e("Error initializing location services: ${e.message}")
            // Don't rethrow - allow other services to continue initializing
        }
        delay(50) // Reduced from 100ms to 50ms
    }

    private suspend fun initializeNotifications() {
        try {
            notificationService.createNotificationChannels()
        } catch (e: Exception) {
            Timber.tag(tag).e("Error initializing notifications: ${e.message}")
            // Don't rethrow - allow other services to continue initializing
        }
        delay(50) // Reduced from 100ms to 50ms
    }

    private suspend fun initializeAnalytics() {
        try {
            // Set common user properties if needed
            analyticsManager.setCommonProperties()
        } catch (e: Exception) {
            Timber.tag(tag).e("Error initializing analytics: ${e.message}")
            // Don't rethrow - allow other services to continue initializing
        }
        delay(50) // Reduced from 100ms to 50ms
    }

    fun cleanup() {
        initScope.cancel()
    }
}