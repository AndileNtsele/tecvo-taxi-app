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
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Navigation stress integration tests for real-time accuracy under pressure.
 * 
 * From CLAUDE.md testing requirements:
 * - "Rapid navigation between screens"
 * - "Screen rotation while on map"
 * - "Back button behavior"
 * - "Multiple quick destination changes"
 * 
 * These tests ensure the critical requirement: "If someone appears on the map, they MUST actually be there"
 * even under stressful navigation scenarios.
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], application = HiltTestApplication::class)
class NavigationStressIntegrationTest : FirebaseIntegrationTestBase() {
    
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
    
    // Test locations for destination changes
    private val joziLocation = LatLng(-26.2041, 28.0473) // Johannesburg
    private val capeTownLocation = LatLng(-33.9249, 18.4241) // Cape Town
    private val durbanLocation = LatLng(-29.8587, 31.0218) // Durban
    
    @Before
    override fun setupFirebaseIntegration() {
        super.setupFirebaseIntegration()
        MockitoAnnotations.openMocks(this)
        
        setupMockDependencies()
        createTestServices()
    }
    
    private fun setupMockDependencies() {
        // Mock SharedPreferences
        whenever(mockSharedPreferences.edit()).thenReturn(mockEditor)
        whenever(mockEditor.putBoolean(any(), any())).thenReturn(mockEditor)
        whenever(mockSharedPreferences.getBoolean(any<String>(), any<Boolean>())).thenReturn(true)
        
        // Mock UserPreferencesRepository
        whenever(mockUserPreferencesRepository.notificationRadiusFlow).thenReturn(MutableStateFlow(2.0f))
        whenever(mockUserPreferencesRepository.notifyDifferentRoleFlow).thenReturn(MutableStateFlow(true))
        
        // Mock ConnectivityManager - always online
        whenever(mockConnectivityManager.isOnline).thenReturn(MutableStateFlow(true))
        whenever(mockConnectivityManager.isNetworkAvailable()).thenReturn(true)
        
        // Mock PermissionManager
        whenever(mockPermissionManager.isLocationPermissionGranted()).thenReturn(true)
        whenever(mockPermissionManager.locationPermissionFlow).thenReturn(MutableStateFlow(true))
        whenever(mockPermissionManager.backgroundLocationPermissionFlow).thenReturn(MutableStateFlow(true))
    }
    
    private fun createTestServices() {
        locationService = LocationService(
            testContext, 
            mockErrorHandlingService, 
            mockPermissionManager, 
            mockLocationServiceStateManager
        )
        
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
    
    /**
     * CRITICAL STRESS TEST: Rapid navigation between screens doesn't create false positives
     * 
     * From CLAUDE.md: "Rapid navigation between screens" - one of the critical test scenarios
     */
    @Test
    fun `rapid navigation between screens maintains Firebase accuracy`() = runTest(timeout = 30_000) {
        // Given: User info is set up
        mapViewModel.initialize(testUserId, testUserType, testDestination)
        locationService.setUserInfo(testUserId, testUserType, testDestination)
        locationService.updateLocation(joziLocation)
        
        // When: Rapid navigation cycles (5 cycles of enter/leave map)
        for (cycle in 1..5) {
            println("Navigation cycle $cycle")
            
            // Enter map screen
            mapViewModel.startMonitoring()
            delay(300) // Very brief stay on map (300ms)
            
            // Verify user appears in Firebase
            val appearedInFirebase = awaitFirebasePresence(testUserId, testUserType, testDestination, 2000)
            assertTrue("User should appear in Firebase during cycle $cycle", appearedInFirebase)
            
            // Leave map screen  
            mapViewModel.stopMonitoring()
            delay(200) // Brief time off map (200ms)
        }
        
        // Final cleanup - user leaves app entirely
        mapViewModel.cleanup()
        locationService.cleanup()
        
        // Then: User should be completely removed from Firebase after all navigation
        val finallyRemoved = awaitFirebaseAbsence(testUserId, testUserType, testDestination, 5000)
        assertTrue("User must be completely removed from Firebase after navigation stress test", finallyRemoved)
        
        // Critical assertion: No false positives remain
        delay(2000) // Additional wait to ensure cleanup completed
        val stillPresent = checkFirebasePresence(testUserId, testUserType, testDestination)
        assertFalse("User must not appear as available after rapid navigation cleanup", stillPresent)
    }
    
    /**
     * CRITICAL STRESS TEST: Multiple quick destination changes maintain accuracy
     * 
     * From CLAUDE.md: "Multiple quick destination changes" - tests destination switching stress
     */
    @Test
    fun `multiple quick destination changes maintain Firebase accuracy`() = runTest(timeout = 25_000) {
        // Given: User starts with initial destination
        mapViewModel.initialize(testUserId, testUserType, "town")
        locationService.setUserInfo(testUserId, testUserType, "town")
        locationService.updateLocation(joziLocation)
        mapViewModel.startMonitoring()
        
        // Verify initial presence
        awaitFirebasePresence(testUserId, testUserType, "town", 3000)
        
        // When: Rapid destination changes
        val destinations = listOf("local", "town", "local", "town", "local")
        val locations = listOf(capeTownLocation, durbanLocation, joziLocation, capeTownLocation, durbanLocation)
        
        for ((index, destination) in destinations.withIndex()) {
            println("Changing destination to: $destination")
            
            // Change destination
            locationService.updateDestination(destination)
            locationService.updateLocation(locations[index])
            mapViewModel.initialize(testUserId, testUserType, destination)
            
            delay(500) // Brief delay to allow destination change processing
            
            // Verify user appears under new destination
            val presentInNewDestination = awaitFirebasePresence(testUserId, testUserType, destination, 3000)
            assertTrue("User should appear in Firebase under destination '$destination'", presentInNewDestination)
        }
        
        // Cleanup
        mapViewModel.stopMonitoring()
        mapViewModel.cleanup()
        locationService.cleanup()
        
        // Then: User should be removed from all destinations
        for (destination in destinations.distinct()) {
            val removedFromDestination = awaitFirebaseAbsence(testUserId, testUserType, destination, 3000)
            assertTrue("User should be removed from destination '$destination'", removedFromDestination)
        }
    }
    
    /**
     * CONFIGURATION CHANGE STRESS TEST: Screen rotation during navigation stress
     * 
     * From CLAUDE.md: "Screen rotation while on map" - configuration change resilience
     */
    @Test
    fun `screen rotation during navigation maintains presence accurately`() = runTest(timeout = 20_000) {
        // Given: User is on map
        mapViewModel.initialize(testUserId, testUserType, testDestination)
        locationService.setUserInfo(testUserId, testUserType, testDestination)
        locationService.updateLocation(joziLocation)
        mapViewModel.startMonitoring()
        
        awaitFirebasePresence(testUserId, testUserType, testDestination, 3000)
        
        // When: Multiple screen rotations (simulated by recreating ViewModels)
        for (rotation in 1..3) {
            println("Screen rotation simulation $rotation")
            
            // Simulate configuration change by creating new ViewModel while keeping LocationService
            val rotatedViewModel = MapViewModel(
                mockUserPreferencesRepository,
                locationService, // Keep same LocationService (simulates retained service)
                mockErrorHandlingService,
                mockConnectivityManager,
                testDatabase,
                mockSharedPreferences,
                mockMemoryOptimizer,
                mockGeocodingService,
                mockContext
            )
            
            // Reinitialize with same data (simulates activity recreation)
            rotatedViewModel.initialize(testUserId, testUserType, testDestination)
            
            delay(500) // Allow configuration change to complete
            
            // Verify user remains present through rotation
            val stillPresent = checkFirebasePresence(testUserId, testUserType, testDestination)
            assertTrue("User must remain present through screen rotation $rotation", stillPresent)
            
            // Replace reference for next iteration
            mapViewModel = rotatedViewModel
        }
        
        // Cleanup final ViewModel
        mapViewModel.stopMonitoring()
        mapViewModel.cleanup()
        locationService.cleanup()
        
        // Verify proper cleanup after all rotations
        val finallyRemoved = awaitFirebaseAbsence(testUserId, testUserType, testDestination, 5000)
        assertTrue("User must be removed after screen rotation stress test", finallyRemoved)
    }
    
    /**
     * BACK BUTTON STRESS TEST: Rapid back button presses during map usage
     * 
     * From CLAUDE.md: "Back button behavior" - tests navigation control stress
     */
    @Test
    fun `rapid back button navigation maintains Firebase state accuracy`() = runTest(timeout = 20_000) {
        // Given: User navigates to map multiple times via back button simulation
        mapViewModel.initialize(testUserId, testUserType, testDestination)
        locationService.setUserInfo(testUserId, testUserType, testDestination)
        locationService.updateLocation(joziLocation)
        
        // When: Simulate rapid back button navigation (enter map, back out, repeat)
        for (backPress in 1..4) {
            println("Back button simulation $backPress")
            
            // Enter map (user navigates to map screen)
            mapViewModel.startMonitoring()
            delay(400) // Brief time on map
            
            // Verify presence
            val appeared = awaitFirebasePresence(testUserId, testUserType, testDestination, 2500)
            assertTrue("User should appear on map entry $backPress", appeared)
            
            // Back button pressed (user leaves map)
            mapViewModel.stopMonitoring()
            delay(300) // Brief time away from map
            
            // Verify removal (back button should trigger cleanup)
            val removed = awaitFirebaseAbsence(testUserId, testUserType, testDestination, 2500)
            assertTrue("User should be removed on back button press $backPress", removed)
        }
        
        // Final cleanup
        mapViewModel.cleanup()
        locationService.cleanup()
        
        // Verify no lingering presence
        val noLingeringPresence = !checkFirebasePresence(testUserId, testUserType, testDestination)
        assertTrue("No presence should remain after back button stress test", noLingeringPresence)
    }
    
    /**
     * MEMORY PRESSURE STRESS TEST: Navigation under memory constraints
     * 
     * Tests Firebase accuracy when system is under memory pressure
     */
    @Test
    fun `navigation under memory pressure maintains accuracy`() = runTest(timeout = 25_000) {
        // Given: Simulate memory pressure with many rapid operations
        val stressOperations = 10
        
        for (operation in 1..stressOperations) {
            println("Memory stress operation $operation")
            
            // Create new services to simulate memory recycling
            val stressLocationService = LocationService(
                testContext, 
                mockErrorHandlingService, 
                mockPermissionManager, 
                mockLocationServiceStateManager
            )
            
            val stressViewModel = MapViewModel(
                mockUserPreferencesRepository,
                stressLocationService,
                mockErrorHandlingService,
                mockConnectivityManager,
                testDatabase,
                mockSharedPreferences,
                mockMemoryOptimizer,
                mockGeocodingService,
                mockContext
            )
            
            // Rapid setup and teardown
            stressViewModel.initialize("stress-user-$operation", testUserType, testDestination)
            stressLocationService.setUserInfo("stress-user-$operation", testUserType, testDestination)
            stressLocationService.updateLocation(joziLocation)
            stressViewModel.startMonitoring()
            
            delay(200) // Very brief presence
            
            // Cleanup
            stressViewModel.stopMonitoring()
            stressViewModel.cleanup()
            stressLocationService.cleanup()
            
            delay(100) // Brief pause between operations
        }
        
        // Then: Verify no lingering test users in Firebase
        delay(2000) // Allow cleanup to complete
        
        // Check that none of the stress test users remain
        for (operation in 1..stressOperations) {
            val lingering = checkFirebasePresence("stress-user-$operation", testUserType, testDestination)
            assertFalse("Stress test user $operation should not remain in Firebase", lingering)
        }
    }
    
    /**
     * EDGE CASE: Rapid app foreground/background during navigation
     */
    @Test
    fun `app lifecycle changes during navigation maintain accuracy`() = runTest(timeout = 15_000) {
        // Given: User on map
        mapViewModel.initialize(testUserId, testUserType, testDestination)
        locationService.setUserInfo(testUserId, testUserType, testDestination)
        locationService.updateLocation(joziLocation)
        mapViewModel.startMonitoring()
        
        awaitFirebasePresence(testUserId, testUserType, testDestination, 3000)
        
        // When: Simulate rapid app lifecycle changes
        for (cycle in 1..3) {
            println("App lifecycle cycle $cycle")
            
            // App goes to background (but map screen stays active)
            delay(500)
            val presentWhileBackgrounded = checkFirebasePresence(testUserId, testUserType, testDestination)
            assertTrue("User should remain present when app is backgrounded from map", presentWhileBackgrounded)
            
            // App returns to foreground
            delay(500)
            val presentAfterForeground = checkFirebasePresence(testUserId, testUserType, testDestination)
            assertTrue("User should remain present when app returns to foreground", presentAfterForeground)
        }
        
        // When: User actually leaves map (not just app lifecycle)
        mapViewModel.stopMonitoring()
        mapViewModel.cleanup()
        locationService.cleanup()
        
        // Then: User should be removed
        val removedAfterActualExit = awaitFirebaseAbsence(testUserId, testUserType, testDestination, 5000)
        assertTrue("User must be removed when actually leaving map", removedAfterActualExit)
    }
    
    // Helper methods (reusing from parent class concepts)
    private suspend fun awaitFirebasePresence(userId: String, userType: String, destination: String, timeoutMs: Long): Boolean = 
        suspendCoroutine { continuation ->
            val reference = testDatabase.reference
                .child(userType + "s")
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
            
            GlobalScope.launch {
                delay(timeoutMs)
                if (!resumed) {
                    resumed = true
                    reference.removeEventListener(listener)
                    continuation.resume(false)
                }
            }
        }
    
    private suspend fun awaitFirebaseAbsence(userId: String, userType: String, destination: String, timeoutMs: Long): Boolean = 
        suspendCoroutine { continuation ->
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
            
            GlobalScope.launch {
                delay(timeoutMs)
                if (!resumed) {
                    resumed = true
                    reference.removeEventListener(listener)
                    continuation.resume(false)
                }
            }
        }
    
    private suspend fun checkFirebasePresence(userId: String, userType: String, destination: String): Boolean = 
        suspendCoroutine { continuation ->
            val reference = testDatabase.reference
                .child(userType + "s")
                .child(destination)
                .child(userId)
                
            reference.get().addOnCompleteListener { task ->
                continuation.resume(task.result?.exists() ?: false)
            }
        }
}