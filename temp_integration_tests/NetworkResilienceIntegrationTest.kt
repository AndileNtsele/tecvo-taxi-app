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
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Network resilience integration tests for real-time accuracy under network stress.
 * 
 * From CLAUDE.md testing requirements:
 * - "WiFi to mobile data switching"
 * - "Airplane mode cycles" 
 * - "Poor connection scenarios"
 * - "Firebase reconnection handling"
 * 
 * These tests ensure the app maintains real-time accuracy even when network conditions are challenging.
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], application = HiltTestApplication::class)
class NetworkResilienceIntegrationTest : FirebaseIntegrationTestBase() {
    
    @Mock private lateinit var mockUserPreferencesRepository: UserPreferencesRepository
    @Mock private lateinit var mockErrorHandlingService: ErrorHandlingService
    @Mock private lateinit var mockSharedPreferences: SharedPreferences
    @Mock private lateinit var mockEditor: SharedPreferences.Editor
    @Mock private lateinit var mockMemoryOptimizer: MemoryOptimizationManager
    @Mock private lateinit var mockGeocodingService: GeocodingService
    @Mock private lateinit var mockContext: Context
    @Mock private lateinit var mockPermissionManager: PermissionManager
    @Mock private lateinit var mockLocationServiceStateManager: LocationServiceStateManager
    
    // Mock ConnectivityManager for network state simulation
    private lateinit var mockConnectivityManager: ConnectivityManager
    private lateinit var networkStateFlow: MutableStateFlow<Boolean>
    
    private lateinit var locationService: LocationService
    private lateinit var mapViewModel: MapViewModel
    
    // Test location
    private val testLocation = LatLng(-26.2041, 28.0473) // Johannesburg
    
    @Before
    override fun setupFirebaseIntegration() {
        super.setupFirebaseIntegration()
        MockitoAnnotations.openMocks(this)
        
        setupNetworkMockDependencies()
        createTestServices()
    }
    
    private fun setupNetworkMockDependencies() {
        // Create controllable network state
        networkStateFlow = MutableStateFlow(true) // Start online
        mockConnectivityManager = mock(ConnectivityManager::class.java)
        
        whenever(mockConnectivityManager.isOnline).thenReturn(networkStateFlow)
        whenever(mockConnectivityManager.isNetworkAvailable()).thenAnswer { networkStateFlow.value }
        
        // Other mocks
        whenever(mockSharedPreferences.edit()).thenReturn(mockEditor)
        whenever(mockEditor.putBoolean(any(), any())).thenReturn(mockEditor)
        whenever(mockSharedPreferences.getBoolean(any<String>(), any<Boolean>())).thenReturn(true)
        
        whenever(mockUserPreferencesRepository.notificationRadiusFlow).thenReturn(MutableStateFlow(2.0f))
        whenever(mockUserPreferencesRepository.notifyDifferentRoleFlow).thenReturn(MutableStateFlow(true))
        
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
     * CRITICAL NETWORK TEST: WiFi to mobile data switching maintains Firebase presence
     * 
     * From CLAUDE.md: "WiFi to mobile data switching" - core network resilience requirement
     */
    @Test
    fun `wifi to mobile data switching maintains Firebase presence`() = runTest(timeout = 20_000) {
        // Given: User is on map with WiFi
        mapViewModel.initialize(testUserId, testUserType, testDestination)
        locationService.setUserInfo(testUserId, testUserType, testDestination)
        locationService.updateLocation(testLocation)
        mapViewModel.startMonitoring()
        
        // Verify initial presence on WiFi
        awaitFirebasePresence(testUserId, testUserType, testDestination, 3000)
        println("User established on WiFi")
        
        // When: Network switches to mobile data (brief disconnection)
        networkStateFlow.value = false // Simulate brief disconnection
        delay(500) // Brief network switch delay
        networkStateFlow.value = true // Back online on mobile data
        
        println("Simulated WiFi -> mobile data switch")
        
        // Then: User should remain present after network switch
        delay(2000) // Allow Firebase reconnection time
        val stillPresentAfterSwitch = checkFirebasePresence(testUserId, testUserType, testDestination)
        assertTrue("User must remain present after WiFi to mobile data switch", stillPresentAfterSwitch)
        
        // Cleanup
        mapViewModel.stopMonitoring()
        mapViewModel.cleanup()
        locationService.cleanup()
        
        val properlyRemoved = awaitFirebaseAbsence(testUserId, testUserType, testDestination, 5000)
        assertTrue("User should be properly removed after network resilience test", properlyRemoved)
    }
    
    /**
     * CRITICAL NETWORK TEST: Airplane mode cycle recovery
     * 
     * From CLAUDE.md: "Airplane mode cycles" - tests complete network loss recovery
     */
    @Test
    fun `airplane mode cycle maintains Firebase accuracy after reconnection`() = runTest(timeout = 25_000) {
        // Given: User is on map and present in Firebase
        mapViewModel.initialize(testUserId, testUserType, testDestination)
        locationService.setUserInfo(testUserId, testUserType, testDestination)
        locationService.updateLocation(testLocation)
        mapViewModel.startMonitoring()
        
        awaitFirebasePresence(testUserId, testUserType, testDestination, 3000)
        println("User established before airplane mode")
        
        // When: Airplane mode ON (complete network loss)
        networkStateFlow.value = false
        println("Airplane mode ON - network disconnected")
        
        delay(3000) // Extended offline period
        
        // During offline period, user should still be trying to maintain presence locally
        // but won't be able to write to Firebase
        
        // Airplane mode OFF (network restored)
        networkStateFlow.value = true
        println("Airplane mode OFF - network restored")
        
        // Then: Firebase presence should be restored within reasonable time
        delay(3000) // Allow reconnection and presence restoration
        
        val presentAfterReconnection = checkFirebasePresence(testUserId, testUserType, testDestination)
        assertTrue("User must be restored in Firebase after airplane mode cycle", presentAfterReconnection)
        
        // Cleanup
        mapViewModel.stopMonitoring()
        mapViewModel.cleanup()
        locationService.cleanup()
    }
    
    /**
     * NETWORK STRESS TEST: Poor connection scenarios with intermittent connectivity
     * 
     * From CLAUDE.md: "Poor connection scenarios" - tests unstable network handling
     */
    @Test
    fun `poor connection with intermittent connectivity maintains accuracy`() = runTest(timeout = 30_000) {
        // Given: User on map
        mapViewModel.initialize(testUserId, testUserType, testDestination)
        locationService.setUserInfo(testUserId, testUserType, testDestination)
        locationService.updateLocation(testLocation)
        mapViewModel.startMonitoring()
        
        awaitFirebasePresence(testUserId, testUserType, testDestination, 3000)
        
        // When: Simulate poor, intermittent connectivity
        for (cycle in 1..5) {
            println("Poor connectivity cycle $cycle")
            
            // Brief disconnection
            networkStateFlow.value = false
            delay(300) // Short offline period
            
            // Brief reconnection
            networkStateFlow.value = true
            delay(400) // Short online period
        }
        
        // Final stable connection
        networkStateFlow.value = true
        delay(2000) // Allow stabilization
        
        // Then: User should be present after connectivity stabilizes
        val presentAfterPoorConnection = checkFirebasePresence(testUserId, testUserType, testDestination)
        assertTrue("User must be present after poor connectivity resolves", presentAfterPoorConnection)
        
        // When: User properly leaves map
        mapViewModel.stopMonitoring()
        mapViewModel.cleanup()
        locationService.cleanup()
        
        // Then: Should be properly removed despite previous network issues
        val removedAfterNetworkStress = awaitFirebaseAbsence(testUserId, testUserType, testDestination, 5000)
        assertTrue("User must be properly removed after network stress", removedAfterNetworkStress)
    }
    
    /**
     * FIREBASE RECONNECTION TEST: Firebase-specific connection handling
     * 
     * From CLAUDE.md: "Firebase reconnection handling" - tests Firebase connection resilience
     */
    @Test
    fun `firebase reconnection maintains real-time accuracy`() = runTest(timeout = 20_000) {
        // Given: Two users can see each other (testing real-time discovery)
        val secondUser = createSecondTestUser()
        
        // Both users on map
        mapViewModel.initialize(testUserId, testUserType, testDestination)
        locationService.setUserInfo(testUserId, testUserType, testDestination)
        locationService.updateLocation(testLocation)
        mapViewModel.startMonitoring()
        
        secondUser.mapViewModel.initialize(secondUser.userId, "passenger", testDestination)
        secondUser.locationService.setUserInfo(secondUser.userId, "passenger", testDestination)
        secondUser.locationService.updateLocation(LatLng(-26.1076, 28.0567))
        secondUser.mapViewModel.startMonitoring()
        
        // Verify mutual discovery
        delay(3000)
        val initialSecondaryEntities = mapViewModel.secondaryEntityLocations.value.size
        assertTrue("Users should discover each other initially", initialSecondaryEntities > 0)
        
        // When: Network disconnection affects both users
        networkStateFlow.value = false
        delay(2000) // Offline period
        
        // Network reconnection
        networkStateFlow.value = true
        delay(4000) // Allow Firebase reconnection and sync
        
        // Then: Users should rediscover each other after reconnection
        val postReconnectionEntities = mapViewModel.secondaryEntityLocations.value.size
        assertTrue("Users should rediscover each other after Firebase reconnection", postReconnectionEntities > 0)
        
        // Cleanup both users
        mapViewModel.stopMonitoring()
        mapViewModel.cleanup()
        locationService.cleanup()
        
        secondUser.mapViewModel.stopMonitoring()
        secondUser.mapViewModel.cleanup()
        secondUser.locationService.cleanup()
    }
    
    /**
     * EDGE CASE: Network loss during critical operations
     */
    @Test
    fun `network loss during user entry and exit maintains accuracy`() = runTest(timeout = 25_000) {
        // Given: User attempts to enter map during network issues
        mapViewModel.initialize(testUserId, testUserType, testDestination)
        locationService.setUserInfo(testUserId, testUserType, testDestination)
        locationService.updateLocation(testLocation)
        
        // When: Network is offline when user enters map
        networkStateFlow.value = false
        mapViewModel.startMonitoring()
        
        delay(2000) // User tries to be present while offline
        
        // Network comes back online
        networkStateFlow.value = true
        delay(3000) // Allow presence to be established
        
        // Then: User should appear in Firebase once network is restored
        val presentAfterDelayedEntry = checkFirebasePresence(testUserId, testUserType, testDestination)
        assertTrue("User should appear in Firebase after delayed network entry", presentAfterDelayedEntry)
        
        // When: Network goes offline again during user exit
        networkStateFlow.value = false
        mapViewModel.stopMonitoring()
        mapViewModel.cleanup()
        locationService.cleanup()
        
        delay(1000) // Offline cleanup attempt
        
        // Network comes back online
        networkStateFlow.value = true
        delay(3000) // Allow cleanup to complete
        
        // Then: User should be removed even with delayed network cleanup
        val removedAfterDelayedExit = !checkFirebasePresence(testUserId, testUserType, testDestination)
        assertTrue("User should be removed after delayed network cleanup", removedAfterDelayedExit)
    }
    
    /**
     * NETWORK QUALITY TEST: Degraded network performance
     */
    @Test
    fun `degraded network performance maintains eventual consistency`() = runTest(timeout = 20_000) {
        // Given: User on map with good connection initially
        mapViewModel.initialize(testUserId, testUserType, testDestination)
        locationService.setUserInfo(testUserId, testUserType, testDestination)
        locationService.updateLocation(testLocation)
        mapViewModel.startMonitoring()
        
        awaitFirebasePresence(testUserId, testUserType, testDestination, 3000)
        
        // When: Network becomes very slow (simulate with delayed operations)
        for (slowCycle in 1..3) {
            // Simulate slow network with rapid on/off cycles
            networkStateFlow.value = false
            delay(100) // Very brief disconnection
            networkStateFlow.value = true
            delay(500) // Slower reconnection
        }
        
        // Allow time for eventual consistency
        delay(3000)
        
        // Then: User should still be accurately present
        val eventuallyConsistent = checkFirebasePresence(testUserId, testUserType, testDestination)
        assertTrue("User presence should be eventually consistent despite network degradation", eventuallyConsistent)
        
        // Cleanup should also work despite network issues
        mapViewModel.stopMonitoring()
        mapViewModel.cleanup()
        locationService.cleanup()
        
        delay(3000) // Allow cleanup despite network issues
        val eventuallyRemoved = !checkFirebasePresence(testUserId, testUserType, testDestination)
        assertTrue("User should be eventually removed despite network degradation", eventuallyRemoved)
    }
    
    // Helper to create second test user for multi-user network scenarios
    private fun createSecondTestUser(): TestUserData {
        val secondUserId = "second-${testSessionId}"
        
        val secondLocationService = LocationService(
            testContext, 
            mockErrorHandlingService, 
            mockPermissionManager, 
            mockLocationServiceStateManager
        )
        
        val secondMapViewModel = MapViewModel(
            mockUserPreferencesRepository,
            secondLocationService,
            mockErrorHandlingService,
            mockConnectivityManager,
            testDatabase,
            mockSharedPreferences,
            mockMemoryOptimizer,
            mockGeocodingService,
            mockContext
        )
        
        return TestUserData(secondUserId, secondLocationService, secondMapViewModel)
    }
    
    data class TestUserData(
        val userId: String,
        val locationService: LocationService,
        val mapViewModel: MapViewModel
    )
    
    // Helper methods (similar to parent class)
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