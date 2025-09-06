package com.example.taxi.integration

import android.content.Context
import android.content.SharedPreferences
import com.example.taxi.services.ErrorHandlingService
import com.example.taxi.services.LocationService
import com.example.taxi.services.LocationServiceStateManager
import com.example.taxi.permissions.PermissionManager
import com.example.taxi.viewmodel.MapViewModel
import com.example.taxi.utils.ConnectivityManager
import com.example.taxi.utils.MemoryOptimizationManager
import com.example.taxi.services.GeocodingService
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import repository.UserPreferencesRepository
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Integration tests for Firebase presence lifecycle - the CRITICAL functionality for real-time accuracy.
 * 
 * Tests the core mission from CLAUDE.md:
 * - "If someone appears on the map, they MUST actually be there and available"
 * - Firebase write/remove timing requirements (2-5 seconds)
 * - Real-time user presence accuracy
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], application = HiltTestApplication::class)
class FirebasePresenceLifecycleIntegrationTest : FirebaseIntegrationTestBase() {
    
    @Mock private lateinit var mockUserPreferencesRepository: UserPreferencesRepository
    @Mock private lateinit var mockErrorHandlingService: ErrorHandlingService
    @Mock private lateinit var mockConnectivityManager: ConnectivityManager
    @Mock private lateinit var mockSharedPreferences: SharedPreferences
    @Mock private lateinit var mockEditor: SharedPreferences.Editor
    @Mock private lateinit var mockMemoryOptimizer: MemoryOptimizationManager
    @Mock private lateinit var mockGeocodingService: GeocodingService
    @Mock private lateinit var mockContext: Context
    @Mock private lateinit var mockPermissionManager: PermissionManager
    @Mock private lateinit var mockLocationServiceStateManager: LocationServiceStateManager
    
    private lateinit var locationService: LocationService
    private lateinit var mapViewModel: MapViewModel
    
    // Test location data
    private val testLocation = LatLng(-26.2041, 28.0473) // Johannesburg
    private val testSecondaryLocation = LatLng(-33.9249, 18.4241) // Cape Town
    
    @Before
    override fun setupFirebaseIntegration() {
        super.setupFirebaseIntegration()
        MockitoAnnotations.openMocks(this)
        
        setupMockDependencies()
        
        // Create services with real Firebase backend
        locationService = LocationService(testContext, mockErrorHandlingService, mockPermissionManager, mockLocationServiceStateManager)
        mapViewModel = MapViewModel(
            mockUserPreferencesRepository,
            locationService, 
            mockErrorHandlingService,
            mockConnectivityManager,
            testDatabase,
            mockSharedPreferences,
            mockMemoryOptimizer,
            mockGeocodingService,
            mockContext
        )
    }
    
    private fun setupMockDependencies() {
        // Mock SharedPreferences
        whenever(mockSharedPreferences.edit()).thenReturn(mockEditor)
        whenever(mockEditor.putBoolean(any(), any())).thenReturn(mockEditor)
        whenever(mockSharedPreferences.getBoolean(any<String>(), any<Boolean>())).thenReturn(true)
        
        // Mock UserPreferencesRepository
        whenever(mockUserPreferencesRepository.notificationRadiusFlow).thenReturn(MutableStateFlow(2.0f))
        whenever(mockUserPreferencesRepository.notifyDifferentRoleFlow).thenReturn(MutableStateFlow(true))
        
        // Mock ConnectivityManager - always online for these tests
        whenever(mockConnectivityManager.isOnline).thenReturn(MutableStateFlow(true))
        whenever(mockConnectivityManager.isNetworkAvailable()).thenReturn(true)
        
        // Mock PermissionManager
        whenever(mockPermissionManager.isLocationPermissionGranted()).thenReturn(true)
        whenever(mockPermissionManager.locationPermissionFlow).thenReturn(MutableStateFlow(true))
        whenever(mockPermissionManager.backgroundLocationPermissionFlow).thenReturn(MutableStateFlow(true))
    }
    
    /**
     * CRITICAL TEST: User appears in Firebase within 2 seconds of entering map
     * 
     * This tests the core requirement: "User enters map → appears in Firebase within 2 seconds"
     */
    @Test
    fun `user appears in Firebase within 2 seconds of entering map screen`() = runTest(timeout = 10_000) {
        // Given: User initializes and enters map screen
        mapViewModel.initialize(testUserId, testUserType, testDestination)
        locationService.setUserInfo(testUserId, testUserType, testDestination)
        locationService.updateLocation(testLocation)
        
        // When: User enters map screen (start monitoring)
        val startTime = System.currentTimeMillis()
        mapViewModel.startMonitoring()
        
        // Then: User should appear in Firebase within 2 seconds
        val userAppearedInFirebase = awaitFirebasePresence(testUserId, testUserType, testDestination, 2000)
        val elapsedTime = System.currentTimeMillis() - startTime
        
        assertTrue("User must appear in Firebase within 2 seconds (took ${elapsedTime}ms)", userAppearedInFirebase)
        assertTrue("Response time must be under 2000ms", elapsedTime < 2000)
    }
    
    /**
     * CRITICAL TEST: User disappears from Firebase within 5 seconds of leaving map
     * 
     * This tests: "User leaves map → disappears from Firebase within 5 seconds"
     */
    @Test
    fun `user disappears from Firebase within 5 seconds of leaving map screen`() = runTest(timeout = 15_000) {
        // Given: User is present on map
        mapViewModel.initialize(testUserId, testUserType, testDestination)
        locationService.setUserInfo(testUserId, testUserType, testDestination)
        locationService.updateLocation(testLocation)
        mapViewModel.startMonitoring()
        
        // Verify user is present
        awaitFirebasePresence(testUserId, testUserType, testDestination, 3000)
        
        // When: User leaves map screen
        val startTime = System.currentTimeMillis()
        mapViewModel.stopMonitoring()
        mapViewModel.cleanup()
        
        // Then: User should disappear from Firebase within 5 seconds
        val userRemovedFromFirebase = awaitFirebaseAbsence(testUserId, testUserType, testDestination, 5000)
        val elapsedTime = System.currentTimeMillis() - startTime
        
        assertTrue("User must disappear from Firebase within 5 seconds (took ${elapsedTime}ms)", userRemovedFromFirebase)
        assertTrue("Cleanup time must be under 5000ms", elapsedTime < 5000)
    }
    
    /**
     * CRITICAL TEST: User persists in Firebase during app backgrounding from map
     * 
     * Tests: "User backgrounds app from map → stays in Firebase"
     */
    @Test
    fun `user persists in Firebase when app is backgrounded from map screen`() = runTest(timeout = 10_000) {
        // Given: User is on map screen
        mapViewModel.initialize(testUserId, testUserType, testDestination)
        locationService.setUserInfo(testUserId, testUserType, testDestination)
        locationService.updateLocation(testLocation)
        mapViewModel.startMonitoring()
        
        // Verify user is present
        awaitFirebasePresence(testUserId, testUserType, testDestination, 3000)
        
        // When: App is backgrounded (but map screen is still active)
        // This simulates the user switching to another app while map is still the active screen
        // Note: We DON'T call stopMonitoring() or cleanup() as the map screen is still active
        
        // Then: User should still be present in Firebase after backgrounding
        delay(2000) // Wait 2 seconds to simulate background state
        val stillPresent = checkFirebasePresence(testUserId, testUserType, testDestination)
        
        assertTrue("User must persist in Firebase when app is backgrounded from map screen", stillPresent)
    }
    
    /**
     * CRITICAL TEST: Screen rotation doesn't affect Firebase presence
     * 
     * Tests stability during configuration changes
     */
    @Test
    fun `user persists in Firebase during screen rotation on map`() = runTest(timeout = 10_000) {
        // Given: User is on map screen
        mapViewModel.initialize(testUserId, testUserType, testDestination)
        locationService.setUserInfo(testUserId, testUserType, testDestination)
        locationService.updateLocation(testLocation)
        mapViewModel.startMonitoring()
        
        // Verify user is present
        awaitFirebasePresence(testUserId, testUserType, testDestination, 3000)
        
        // When: Screen rotation occurs (configuration change)
        // In real app, this would cause ViewModel to be retained but activity to be recreated
        // We simulate this by creating a new MapViewModel with same data
        val rotatedViewModel = MapViewModel(
            mockUserPreferencesRepository,
            locationService, 
            mockErrorHandlingService,
            mockConnectivityManager,
            testDatabase,
            mockSharedPreferences,
            mockMemoryOptimizer,
            mockGeocodingService,
            mockContext
        )
        rotatedViewModel.initialize(testUserId, testUserType, testDestination)
        
        // Then: User should remain present in Firebase throughout rotation
        delay(1000) // Allow rotation to complete
        val stillPresent = checkFirebasePresence(testUserId, testUserType, testDestination)
        
        assertTrue("User must persist in Firebase during screen rotation", stillPresent)
    }
    
    /**
     * CRITICAL TEST: Rapid navigation doesn't create false positives
     * 
     * Tests: Navigation stress scenario from CLAUDE.md testing requirements
     */
    @Test
    fun `rapid navigation between screens maintains accurate Firebase state`() = runTest(timeout = 15_000) {
        // Given: User rapidly navigates in and out of map screen
        mapViewModel.initialize(testUserId, testUserType, testDestination)
        locationService.setUserInfo(testUserId, testUserType, testDestination)
        locationService.updateLocation(testLocation)
        
        // When: Rapid navigation cycles
        for (cycle in 1..3) {
            // Enter map
            mapViewModel.startMonitoring()
            delay(500) // Brief stay on map
            
            // Leave map
            mapViewModel.stopMonitoring()
            delay(300) // Brief time off map
        }
        
        // Final cleanup
        mapViewModel.cleanup()
        
        // Then: User should be absent from Firebase after all cycles
        val finallyRemoved = awaitFirebaseAbsence(testUserId, testUserType, testDestination, 5000)
        
        assertTrue("User must be properly cleaned up after rapid navigation", finallyRemoved)
    }
    
    /**
     * Helper function to wait for user presence in Firebase
     */
    private suspend fun awaitFirebasePresence(
        userId: String, 
        userType: String, 
        destination: String, 
        timeoutMs: Long
    ): Boolean = suspendCoroutine { continuation ->
        val reference = testDatabase.reference
            .child(userType + "s") // "drivers" or "passengers"
            .child(destination)
            .child(userId)
            
        var resumed = false
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!resumed && snapshot.exists()) {
                    resumed = true
                    continuation.resume(true)
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                if (!resumed) {
                    resumed = true
                    continuation.resume(false)
                }
            }
        }
        
        reference.addValueEventListener(listener)
        
        // Timeout mechanism
        GlobalScope.launch {
            delay(timeoutMs)
            if (!resumed) {
                resumed = true
                reference.removeEventListener(listener)
                continuation.resume(false)
            }
        }
    }
    
    /**
     * Helper function to wait for user absence from Firebase
     */
    private suspend fun awaitFirebaseAbsence(
        userId: String, 
        userType: String, 
        destination: String, 
        timeoutMs: Long
    ): Boolean = suspendCoroutine { continuation ->
        val reference = testDatabase.reference
            .child(userType + "s")
            .child(destination)
            .child(userId)
            
        var resumed = false
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!resumed && !snapshot.exists()) {
                    resumed = true
                    continuation.resume(true)
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                if (!resumed) {
                    resumed = true
                    continuation.resume(false)
                }
            }
        }
        
        reference.addValueEventListener(listener)
        
        // Timeout mechanism
        GlobalScope.launch {
            delay(timeoutMs)
            if (!resumed) {
                resumed = true
                reference.removeEventListener(listener)
                continuation.resume(false)
            }
        }
    }
    
    /**
     * Helper function to check current Firebase presence
     */
    private suspend fun checkFirebasePresence(
        userId: String, 
        userType: String, 
        destination: String
    ): Boolean = suspendCoroutine { continuation ->
        val reference = testDatabase.reference
            .child(userType + "s")
            .child(destination)
            .child(userId)
            
        reference.get().addOnCompleteListener { task ->
            continuation.resume(task.result?.exists() ?: false)
        }
    }
}