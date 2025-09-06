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
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Integration tests for real-time user discovery - the CORE functionality of the taxi app.
 * 
 * Tests the mission from CLAUDE.md:
 * - "Real-time taxi matching app where drivers and passengers going to the same destination can find each other instantly"
 * - Multi-user discovery scenarios
 * - Cross-role visibility (drivers see passengers, passengers see drivers)
 * - Same-role strategic intelligence features
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], application = HiltTestApplication::class)
class RealTimeUserDiscoveryIntegrationTest : FirebaseIntegrationTestBase() {
    
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
    
    // Multiple test users for discovery scenarios
    private data class TestUser(
        val id: String,
        val type: String,
        val location: LatLng,
        val locationService: LocationService,
        val mapViewModel: MapViewModel
    )
    
    private lateinit var driverUser: TestUser
    private lateinit var passengerUser: TestUser
    private lateinit var secondDriverUser: TestUser
    private lateinit var secondPassengerUser: TestUser
    
    // Test locations in Johannesburg area
    private val joziCenterLocation = LatLng(-26.2041, 28.0473) // Johannesburg CBD
    private val sandownLocation = LatLng(-26.1076, 28.0567) // Sandton
    private val rosebankLocation = LatLng(-26.1486, 28.0490) // Rosebank
    private val sowetoLocation = LatLng(-26.2678, 27.8546) // Soweto
    
    @Before
    override fun setupFirebaseIntegration() {
        super.setupFirebaseIntegration()
        MockitoAnnotations.openMocks(this)
        
        setupMockDependencies()
        createTestUsers()
    }
    
    private fun setupMockDependencies() {
        // Mock SharedPreferences for all users
        whenever(mockSharedPreferences.edit()).thenReturn(mockEditor)
        whenever(mockEditor.putBoolean(any(), any())).thenReturn(mockEditor)
        whenever(mockSharedPreferences.getBoolean(any<String>(), any<Boolean>())).thenReturn(true)
        
        // Mock UserPreferencesRepository
        whenever(mockUserPreferencesRepository.notificationRadiusFlow).thenReturn(MutableStateFlow(5.0f)) // Wider radius for testing
        whenever(mockUserPreferencesRepository.notifyDifferentRoleFlow).thenReturn(MutableStateFlow(true))
        
        // Mock ConnectivityManager - always online
        whenever(mockConnectivityManager.isOnline).thenReturn(MutableStateFlow(true))
        whenever(mockConnectivityManager.isNetworkAvailable()).thenReturn(true)
        
        // Mock PermissionManager
        whenever(mockPermissionManager.isLocationPermissionGranted()).thenReturn(true)
        whenever(mockPermissionManager.locationPermissionFlow).thenReturn(MutableStateFlow(true))
        whenever(mockPermissionManager.backgroundLocationPermissionFlow).thenReturn(MutableStateFlow(true))
    }
    
    private fun createTestUsers() {
        // Create driver user
        driverUser = createTestUser("driver", joziCenterLocation)
        
        // Create passenger user  
        passengerUser = createTestUser("passenger", sandownLocation)
        
        // Create second driver for same-role testing
        secondDriverUser = createTestUser("driver", rosebankLocation)
        
        // Create second passenger for same-role testing
        secondPassengerUser = createTestUser("passenger", sowetoLocation)
    }
    
    private fun createTestUser(userType: String, location: LatLng): TestUser {
        val userId = "test-${userType}-${UUID.randomUUID()}"
        
        val locationService = LocationService(
            testContext, 
            mockErrorHandlingService, 
            mockPermissionManager, 
            mockLocationServiceStateManager
        )
        
        val mapViewModel = MapViewModel(
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
        
        return TestUser(userId, userType, location, locationService, mapViewModel)
    }
    
    /**
     * CRITICAL TEST: Driver discovers passenger within 5 seconds
     * 
     * This tests the core app functionality: "drivers and passengers going to the same destination can find each other instantly"
     */
    @Test
    fun `driver discovers passenger within 5 seconds of both being on map`() = runTest(timeout = 15_000) {
        // Given: Passenger enters map first
        passengerUser.locationService.setUserInfo(passengerUser.id, passengerUser.type, testDestination)
        passengerUser.locationService.updateLocation(passengerUser.location)
        passengerUser.mapViewModel.initialize(passengerUser.id, passengerUser.type, testDestination)
        passengerUser.mapViewModel.startMonitoring()
        
        // Wait for passenger to be established in Firebase
        awaitFirebasePresence(passengerUser.id, passengerUser.type, testDestination, 3000)
        
        // When: Driver enters map
        val driverStartTime = System.currentTimeMillis()
        driverUser.locationService.setUserInfo(driverUser.id, driverUser.type, testDestination)
        driverUser.locationService.updateLocation(driverUser.location)
        driverUser.mapViewModel.initialize(driverUser.id, driverUser.type, testDestination)
        driverUser.mapViewModel.startMonitoring()
        
        // Then: Driver should discover passenger within 5 seconds
        val passengerDiscovered = awaitEntityDiscovery(
            driverUser.mapViewModel,
            passengerUser.location,
            5000
        )
        val discoveryTime = System.currentTimeMillis() - driverStartTime
        
        assertTrue("Driver must discover passenger within 5 seconds (took ${discoveryTime}ms)", passengerDiscovered)
        assertTrue("Discovery time must be under 5000ms", discoveryTime < 5000)
        
        // Verify the passenger appears in driver's secondary entities (passengers)
        val secondaryEntities = driverUser.mapViewModel.secondaryEntityLocations.value
        assertTrue("Driver should see passenger in secondary entities", 
            secondaryEntities.any { isLocationNear(it, passengerUser.location) })
    }
    
    /**
     * CRITICAL TEST: Passenger discovers driver within 5 seconds
     * 
     * Reverse scenario - passenger finding driver
     */
    @Test
    fun `passenger discovers driver within 5 seconds of both being on map`() = runTest(timeout = 15_000) {
        // Given: Driver enters map first
        driverUser.locationService.setUserInfo(driverUser.id, driverUser.type, testDestination)
        driverUser.locationService.updateLocation(driverUser.location)
        driverUser.mapViewModel.initialize(driverUser.id, driverUser.type, testDestination)
        driverUser.mapViewModel.startMonitoring()
        
        // Wait for driver to be established
        awaitFirebasePresence(driverUser.id, driverUser.type, testDestination, 3000)
        
        // When: Passenger enters map
        val passengerStartTime = System.currentTimeMillis()
        passengerUser.locationService.setUserInfo(passengerUser.id, passengerUser.type, testDestination)
        passengerUser.locationService.updateLocation(passengerUser.location)
        passengerUser.mapViewModel.initialize(passengerUser.id, passengerUser.type, testDestination)
        passengerUser.mapViewModel.startMonitoring()
        
        // Then: Passenger should discover driver within 5 seconds
        val driverDiscovered = awaitEntityDiscovery(
            passengerUser.mapViewModel,
            driverUser.location,
            5000
        )
        val discoveryTime = System.currentTimeMillis() - passengerStartTime
        
        assertTrue("Passenger must discover driver within 5 seconds (took ${discoveryTime}ms)", driverDiscovered)
        assertTrue("Discovery time must be under 5000ms", discoveryTime < 5000)
        
        // Verify the driver appears in passenger's primary entities (drivers from passenger's perspective)
        val primaryEntities = passengerUser.mapViewModel.primaryEntityLocations.value
        assertTrue("Passenger should see driver in primary entities", 
            primaryEntities.any { isLocationNear(it, driverUser.location) })
    }
    
    /**
     * STRATEGIC INTELLIGENCE TEST: Driver sees competing drivers for strategic positioning
     * 
     * From CLAUDE.md: "See competing drivers on same route → avoid oversupply areas"
     */
    @Test
    fun `driver discovers competing drivers for strategic intelligence`() = runTest(timeout = 15_000) {
        // Given: First driver is on map
        driverUser.locationService.setUserInfo(driverUser.id, driverUser.type, testDestination)
        driverUser.locationService.updateLocation(driverUser.location)
        driverUser.mapViewModel.initialize(driverUser.id, driverUser.type, testDestination)
        driverUser.mapViewModel.startMonitoring()
        
        awaitFirebasePresence(driverUser.id, driverUser.type, testDestination, 3000)
        
        // When: Second driver enters same route
        secondDriverUser.locationService.setUserInfo(secondDriverUser.id, secondDriverUser.type, testDestination)
        secondDriverUser.locationService.updateLocation(secondDriverUser.location)
        secondDriverUser.mapViewModel.initialize(secondDriverUser.id, secondDriverUser.type, testDestination)
        secondDriverUser.mapViewModel.startMonitoring()
        
        // Then: First driver should discover competing driver
        val competitorDiscovered = awaitEntityDiscovery(
            driverUser.mapViewModel,
            secondDriverUser.location,
            5000
        )
        
        assertTrue("Driver must discover competing drivers for strategic positioning", competitorDiscovered)
        
        // Verify same-type entities are visible (strategic intelligence feature)
        val sameTypeEntities = driverUser.mapViewModel.primaryEntityLocations.value // Same type as self
        assertTrue("Driver should see competing driver in same-type entities", 
            sameTypeEntities.any { isLocationNear(it, secondDriverUser.location) })
    }
    
    /**
     * STRATEGIC INTELLIGENCE TEST: Passenger sees other passengers for demand clustering
     * 
     * From CLAUDE.md: "See other passengers wanting same route → gauge demand density"
     */
    @Test
    fun `passenger discovers other passengers for demand intelligence`() = runTest(timeout = 15_000) {
        // Given: First passenger is on map
        passengerUser.locationService.setUserInfo(passengerUser.id, passengerUser.type, testDestination)
        passengerUser.locationService.updateLocation(passengerUser.location)
        passengerUser.mapViewModel.initialize(passengerUser.id, passengerUser.type, testDestination)
        passengerUser.mapViewModel.startMonitoring()
        
        awaitFirebasePresence(passengerUser.id, passengerUser.type, testDestination, 3000)
        
        // When: Second passenger enters same destination
        secondPassengerUser.locationService.setUserInfo(secondPassengerUser.id, secondPassengerUser.type, testDestination)
        secondPassengerUser.locationService.updateLocation(secondPassengerUser.location)
        secondPassengerUser.mapViewModel.initialize(secondPassengerUser.id, secondPassengerUser.type, testDestination)
        secondPassengerUser.mapViewModel.startMonitoring()
        
        // Then: First passenger should discover other passenger for demand clustering
        val otherPassengerDiscovered = awaitEntityDiscovery(
            passengerUser.mapViewModel,
            secondPassengerUser.location,
            5000
        )
        
        assertTrue("Passenger must discover other passengers for demand intelligence", otherPassengerDiscovered)
        
        // Verify same-type entities are visible
        val sameTypeEntities = passengerUser.mapViewModel.secondaryEntityLocations.value // Same type from passenger's perspective
        assertTrue("Passenger should see other passenger in same-type entities", 
            sameTypeEntities.any { isLocationNear(it, secondPassengerUser.location) })
    }
    
    /**
     * REAL-TIME TEST: User immediately disappears when leaving map
     * 
     * Tests that discoveries are accurate and don't show unavailable users
     */
    @Test
    fun `user immediately disappears from other users maps when leaving`() = runTest(timeout = 15_000) {
        // Given: Both driver and passenger are on map and can see each other
        setupDiscoveredUsers()
        
        // Verify they can see each other
        assertTrue("Driver should see passenger initially", 
            driverUser.mapViewModel.secondaryEntityLocations.value.isNotEmpty())
        assertTrue("Passenger should see driver initially", 
            passengerUser.mapViewModel.primaryEntityLocations.value.isNotEmpty())
        
        // When: Driver leaves map
        driverUser.mapViewModel.stopMonitoring()
        driverUser.mapViewModel.cleanup()
        
        // Then: Passenger should no longer see driver within 5 seconds
        val driverDisappearedFromPassengerMap = awaitEntityDisappearance(
            passengerUser.mapViewModel,
            driverUser.location,
            5000
        )
        
        assertTrue("Driver must disappear from passenger's map when leaving", driverDisappearedFromPassengerMap)
    }
    
    /**
     * MULTI-USER SCALE TEST: Multiple users discover each other efficiently
     */
    @Test
    fun `multiple users discover each other in real-time`() = runTest(timeout = 20_000) {
        // Given: Multiple users enter map in sequence
        val allUsers = listOf(driverUser, passengerUser, secondDriverUser, secondPassengerUser)
        
        for (user in allUsers) {
            user.locationService.setUserInfo(user.id, user.type, testDestination)
            user.locationService.updateLocation(user.location)
            user.mapViewModel.initialize(user.id, user.type, testDestination)
            user.mapViewModel.startMonitoring()
            
            delay(1000) // Stagger entries
        }
        
        // When: All users are on map
        delay(3000) // Allow discovery to complete
        
        // Then: Each user should discover relevant entities
        // Drivers should see passengers
        assertTrue("First driver should see passengers", 
            driverUser.mapViewModel.secondaryEntityLocations.value.isNotEmpty())
        assertTrue("Second driver should see passengers", 
            secondDriverUser.mapViewModel.secondaryEntityLocations.value.isNotEmpty())
            
        // Passengers should see drivers
        assertTrue("First passenger should see drivers", 
            passengerUser.mapViewModel.primaryEntityLocations.value.isNotEmpty())
        assertTrue("Second passenger should see drivers", 
            secondPassengerUser.mapViewModel.primaryEntityLocations.value.isNotEmpty())
    }
    
    // Helper methods
    private suspend fun setupDiscoveredUsers() {
        // Setup both users on map
        driverUser.locationService.setUserInfo(driverUser.id, driverUser.type, testDestination)
        driverUser.locationService.updateLocation(driverUser.location)
        driverUser.mapViewModel.initialize(driverUser.id, driverUser.type, testDestination)
        driverUser.mapViewModel.startMonitoring()
        
        passengerUser.locationService.setUserInfo(passengerUser.id, passengerUser.type, testDestination)
        passengerUser.locationService.updateLocation(passengerUser.location)
        passengerUser.mapViewModel.initialize(passengerUser.id, passengerUser.type, testDestination)
        passengerUser.mapViewModel.startMonitoring()
        
        // Wait for mutual discovery
        delay(3000)
    }
    
    private suspend fun awaitEntityDiscovery(
        mapViewModel: MapViewModel,
        targetLocation: LatLng,
        timeoutMs: Long
    ): Boolean = suspendCoroutine { continuation ->
        var resumed = false
        
        // Monitor both primary and secondary entity lists
        val job = GlobalScope.launch {
            var remainingTime = timeoutMs
            val checkInterval = 200L
            
            while (remainingTime > 0 && !resumed) {
                val primaryEntities = mapViewModel.primaryEntityLocations.value
                val secondaryEntities = mapViewModel.secondaryEntityLocations.value
                
                val foundInPrimary = primaryEntities.any { isLocationNear(it, targetLocation) }
                val foundInSecondary = secondaryEntities.any { isLocationNear(it, targetLocation) }
                
                if (foundInPrimary || foundInSecondary) {
                    if (!resumed) {
                        resumed = true
                        continuation.resume(true)
                    }
                    break
                }
                
                delay(checkInterval)
                remainingTime -= checkInterval
            }
            
            if (!resumed) {
                resumed = true
                continuation.resume(false)
            }
        }
    }
    
    private suspend fun awaitEntityDisappearance(
        mapViewModel: MapViewModel,
        targetLocation: LatLng,
        timeoutMs: Long
    ): Boolean = suspendCoroutine { continuation ->
        var resumed = false
        
        val job = GlobalScope.launch {
            var remainingTime = timeoutMs
            val checkInterval = 200L
            
            while (remainingTime > 0 && !resumed) {
                val primaryEntities = mapViewModel.primaryEntityLocations.value
                val secondaryEntities = mapViewModel.secondaryEntityLocations.value
                
                val stillInPrimary = primaryEntities.any { isLocationNear(it, targetLocation) }
                val stillInSecondary = secondaryEntities.any { isLocationNear(it, targetLocation) }
                
                if (!stillInPrimary && !stillInSecondary) {
                    if (!resumed) {
                        resumed = true
                        continuation.resume(true)
                    }
                    break
                }
                
                delay(checkInterval)
                remainingTime -= checkInterval
            }
            
            if (!resumed) {
                resumed = true
                continuation.resume(false)
            }
        }
    }
    
    private fun isLocationNear(location1: LatLng, location2: LatLng, thresholdKm: Double = 0.1): Boolean {
        val distance = calculateDistance(location1, location2)
        return distance < thresholdKm
    }
    
    private fun calculateDistance(location1: LatLng, location2: LatLng): Double {
        val earthRadius = 6371.0 // km
        val dLat = Math.toRadians(location2.latitude - location1.latitude)
        val dLon = Math.toRadians(location2.longitude - location1.longitude)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(location1.latitude)) * Math.cos(Math.toRadians(location2.latitude)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }
    
    // Reuse helper from parent class
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
}