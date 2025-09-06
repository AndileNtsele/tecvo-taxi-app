package com.tecvo.taxi.services

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.tecvo.taxi.utils.ApiKeyValidator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceInitializationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appInitService: AppInitService // Directly inject AppInitService
) : DefaultLifecycleObserver {

    companion object {
        private const val TAG = "ServiceInitManager"
        @Volatile private var instance: ServiceInitializationManager? = null
        fun getInstance(): ServiceInitializationManager {
            return instance ?: synchronized(this) {
                instance ?: throw IllegalStateException(
                    "ServiceInitializationManager has not been initialized via Hilt injection."
                )
            }
        }
    }

    init {
        // Store instance for backward compatibility
        instance = this
    }

    // Service status tracking
    private val serviceStatus = ConcurrentHashMap<String, InitStatus>()
    // Coroutine scope for initialization tasks
    private val initScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    // Track if initialization is already in progress
    private val isInitializationInProgress = AtomicBoolean(false)
    // Job tracking for cancellation
    private var currentInitJob: Job? = null

    // Initialization states
    enum class InitStatus {
        @Suppress("unused") NOT_STARTED, IN_PROGRESS, COMPLETED, FAILED
    }

    // Service names for tracking
    object ServiceNames {
        const val WEB_VIEW = "WebView Configuration"
        const val FIREBASE = "Firebase"
        const val DIRECTORIES = "Application Directories"
        const val MAPS_API = "Maps API Validation"
        @Suppress("unused") const val NOTIFICATION = "Notification Service"
        const val LOCATION = "Location Service"
    }

    // Initialize tracking progress class
    object InitializationProgress {
        fun reset() {
            // Reset initialization progress
        }
    }

    init {
        // Register for lifecycle events if context is a LifecycleOwner
        if (context is LifecycleOwner) {
            context.lifecycle.addObserver(this)
            Timber.tag(TAG).d("Registered for lifecycle events")
        }
    }

    @Suppress("unused")
    fun startInitialization(
        onProgress: ((String, String) -> Unit)? = null,
        onComplete: (() -> Unit)? = null,
        onError: ((String, String) -> Unit)? = null
    ) {
        // Only start if not already in progress
        if (!isInitializationInProgress.compareAndSet(false, true)) {
            Timber.tag(TAG).d("Initialization already in progress")
            onComplete?.invoke() // Still notify completion to prevent hanging
            return
        }

        // Use a timeout to prevent infinite waiting
        currentInitJob = initScope.launch {
            try {
                Timber.tag(TAG).i("Starting service initialization sequence")
                InitializationProgress.reset()

                // Use a timeout for the entire initialization process
                withTimeoutOrNull(30000) { // 30 seconds max
                    // Track initialization state for each service
                    serviceStatus.clear()

                    // Set a timeout for each stage
                    val criticalTimeout = async {
                        delay(10000) // 10s timeout for critical services
                        Timber.tag(TAG).w("Critical services initialization timed out")
                    }

                    // Step 1: Initialize critical services first
                    val criticalJob = async {
                        initializeCriticalServices(onProgress, onError)
                    }

                    // Wait for critical services or timeout
                    try {
                        criticalJob.await()
                        criticalTimeout.cancel()
                    } catch (e: Exception) {
                        Timber.tag(TAG).e("Critical services initialization failed: ${e.message}")
                        // Continue anyway to non-critical services
                    }

                    // Set timeout for non-critical services
                    val nonCriticalTimeout = async {
                        delay(20000) // 20s timeout for non-critical services
                        Timber.tag(TAG).w("Non-critical services initialization timed out")
                    }

                    // Step 2: Initialize non-critical services
                    val nonCriticalJob = async {
                        initializeNonCriticalServices(onProgress, onError)
                    }

                    // Wait for non-critical services or timeout
                    try {
                        nonCriticalJob.await()
                        nonCriticalTimeout.cancel()
                    } catch (e: Exception) {
                        Timber.tag(TAG)
                            .e("Non-critical services initialization failed: ${e.message}")
                        // Continue to completion
                    }
                }

                // Signal completion regardless of individual service status
                Timber.tag(TAG).i("Service initialization sequence completed")
                withContext(Dispatchers.Main) {
                    onComplete?.invoke()
                }
            } catch (e: CancellationException) {
                Timber.tag(TAG).w("Service initialization was cancelled")
                throw e // Re-throw cancellation exceptions
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error during service initialization sequence: ${e.message}")
                onError?.invoke("Initialization Sequence", "Failed to complete service initialization: ${e.message}")
                // Still call completion to avoid hanging
                withContext(Dispatchers.Main) {
                    onComplete?.invoke()
                }
            } finally {
                isInitializationInProgress.set(false)
            }
        }
    }

    private suspend fun initializeCriticalServices(
        onProgress: ((String, String) -> Unit)?,
        onError: ((String, String) -> Unit)?
    ) {
        // Configure WebView (critical for Firebase Auth)
        if (serviceStatus[ServiceNames.WEB_VIEW] != InitStatus.COMPLETED) {
            serviceStatus[ServiceNames.WEB_VIEW] = InitStatus.IN_PROGRESS
            onProgress?.invoke(ServiceNames.WEB_VIEW, "Starting")
            try {
                Timber.tag(TAG).i("Configuring WebView for Firebase Auth")
                appInitService.configureWebView()
                serviceStatus[ServiceNames.WEB_VIEW] = InitStatus.COMPLETED
                onProgress?.invoke(ServiceNames.WEB_VIEW, "Completed")
                Timber.tag(TAG).i("WebView configured successfully")
            } catch (e: Exception) {
                serviceStatus[ServiceNames.WEB_VIEW] = InitStatus.FAILED
                Timber.tag(TAG).e(e, "WebView configuration failed: ${e.message}")
                onError?.invoke(ServiceNames.WEB_VIEW, "Failed to configure WebView: ${e.message}")
            }
        }

        // Firebase is already initialized in TaxiApplication
        serviceStatus[ServiceNames.FIREBASE] = InitStatus.COMPLETED
        onProgress?.invoke(ServiceNames.FIREBASE, "Already initialized by TaxiApplication")
        Timber.tag(TAG).i("Firebase already initialized by TaxiApplication")
    }

    private suspend fun initializeNonCriticalServices(
        onProgress: ((String, String) -> Unit)?,
        onError: ((String, String) -> Unit)?
    ) {
        // Create directories in the background
        val directoriesDeferred = initScope.async(Dispatchers.IO) {
            try {
                serviceStatus[ServiceNames.DIRECTORIES] = InitStatus.IN_PROGRESS
                onProgress?.invoke(ServiceNames.DIRECTORIES, "Starting")
                Timber.tag(TAG).i("Creating application directories")
                // Add a small delay to avoid resource contention
                delay(500)
                appInitService.createAppDirectories()
                serviceStatus[ServiceNames.DIRECTORIES] = InitStatus.COMPLETED
                onProgress?.invoke(ServiceNames.DIRECTORIES, "Completed")
                Timber.tag(TAG).i("Application directories created successfully")
                true
            } catch (e: Exception) {
                serviceStatus[ServiceNames.DIRECTORIES] = InitStatus.FAILED
                Timber.tag(TAG).e(e, "Failed to create application directories: ${e.message}")
                onError?.invoke(ServiceNames.DIRECTORIES, "Failed to create directories: ${e.message}")
                false
            }
        }

        // Validate Maps API key in parallel
        val mapsValidationDeferred = initScope.async(Dispatchers.IO) {
            try {
                serviceStatus[ServiceNames.MAPS_API] = InitStatus.IN_PROGRESS
                onProgress?.invoke(ServiceNames.MAPS_API, "Starting")
                Timber.tag(TAG).i("Validating Maps API key")
                // Add delay to prevent resource contention
                delay(800)
                val isValid = ApiKeyValidator.validateMapsApiKey(context)
                if (isValid) {
                    serviceStatus[ServiceNames.MAPS_API] = InitStatus.COMPLETED
                    onProgress?.invoke(ServiceNames.MAPS_API, "Completed")
                    Timber.tag(TAG).i("Maps API key validated successfully")
                } else {
                    serviceStatus[ServiceNames.MAPS_API] = InitStatus.FAILED
                    Timber.tag(TAG).w("Maps API key validation failed, but continuing")
                    onError?.invoke(ServiceNames.MAPS_API, "Maps API key validation failed, map features may be limited")
                }
                isValid
            } catch (e: Exception) {
                serviceStatus[ServiceNames.MAPS_API] = InitStatus.FAILED
                Timber.tag(TAG).e(e, "Error validating Maps API key: ${e.message}")
                onError?.invoke(ServiceNames.MAPS_API, "Error validating Maps API key: ${e.message}")
                false
            }
        }

        // Wait for all async operations to complete
        directoriesDeferred.await()
        mapsValidationDeferred.await()
        Timber.tag(TAG).i("Non-critical service initialization completed")
    }

    fun cleanup() {
        Timber.tag(TAG).i("Cleaning up ServiceInitializationManager")
        // Cancel any ongoing initialization
        currentInitJob?.cancel()
        initScope.coroutineContext.cancelChildren()
        // Clear status tracking
        serviceStatus.clear()
        isInitializationInProgress.set(false)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        Timber.tag(TAG).d("Lifecycle: onDestroy called, cleaning up")
        cleanup()
        super.onDestroy(owner)
    }
}