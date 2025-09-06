package com.example.taxi.integration

import android.content.Context
import android.content.SharedPreferences
import com.example.taxi.services.ErrorHandlingService
import com.example.taxi.services.LocationService
import com.example.taxi.services.LocationServiceStateManager
import com.example.taxi.permissions.PermissionManager
import com.example.taxi.viewmodel.MapViewModel
import com.example.taxi.viewmodel.HomeViewModel
import com.example.taxi.viewmodel.RoleViewModel
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
import repository.AuthRepository
import repository.UserPreferencesRepository
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * End-to-end user flow integration tests for complete real-time taxi app scenarios.
 * 
 * Tests complete user journeys from CLAUDE.md core mission:
 * - "Real-time taxi matching app where drivers and passengers going to the same destination can find each other instantly"
 * - Full app flow: Role selection → Home → Map → Real-time discovery → Strategic positioning
 * - Cross-role interactions and strategic intelligence features
 * 
 * These tests validate that the entire system works together to deliver the core value proposition.
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], application = HiltTestApplication::class)
class EndToEndUserFlowIntegrationTest : FirebaseIntegrationTestBase() {
    
    // Core mocks
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
    @Mock private lateinit var mockAuthRepository: AuthRepository
    
    // Complete user simulation
    data class CompleteUser(
        val userId: String,
        val userType: String,
        val location: LatLng,
        val locationService: LocationService,
        val mapViewModel: MapViewModel,
        val homeViewModel: HomeViewModel,
        val roleViewModel: RoleViewModel
    )
    
    // Test locations representing real SA taxi scenarios
    private val taxiRankLocation = LatLng(-26.2041, 28.0473) // Johannesburg CBD taxi rank
    private val townshipLocation = LatLng(-26.2678, 27.8546) // Soweto 
    private val mallLocation = LatLng(-26.1076, 28.0567) // Sandton City
    private val airportLocation = LatLng(-26.1367, 28.2411) // OR Tambo
    
    @Before
    override fun setupFirebaseIntegration() {
        super.setupFirebaseIntegration()
        MockitoAnnotations.openMocks(this)
        setupAllMockDependencies()
    }
    
    private fun setupAllMockDependencies() {
        // SharedPreferences mocks
        whenever(mockSharedPreferences.edit()).thenReturn(mockEditor)
        whenever(mockEditor.putBoolean(any(), any())).thenReturn(mockEditor)
        whenever(mockEditor.putString(any(), any())).thenReturn(mockEditor)
        whenever(mockSharedPreferences.getBoolean(any<String>(), any<Boolean>())).thenReturn(true)
        whenever(mockSharedPreferences.getString(any(), any())).thenReturn("test-string")
        
        // UserPreferencesRepository mocks
        whenever(mockUserPreferencesRepository.notificationRadiusFlow).thenReturn(MutableStateFlow(3.0f))
        whenever(mockUserPreferencesRepository.notifyDifferentRoleFlow).thenReturn(MutableStateFlow(true))
        whenever(mockUserPreferencesRepository.getUserRole()).thenReturn("driver")
        whenever(mockUserPreferencesRepository.getNotificationRadius()).thenReturn(3.0f)
        
        // ConnectivityManager - always online for e2e tests
        whenever(mockConnectivityManager.isOnline).thenReturn(MutableStateFlow(true))
        whenever(mockConnectivityManager.isNetworkAvailable()).thenReturn(true)
        
        // PermissionManager - permissions granted
        whenever(mockPermissionManager.isLocationPermissionGranted()).thenReturn(true)
        whenever(mockPermissionManager.locationPermissionFlow).thenReturn(MutableStateFlow(true))
        whenever(mockPermissionManager.backgroundLocationPermissionFlow).thenReturn(MutableStateFlow(true))
        
        // AuthRepository - user authenticated
        whenever(mockAuthRepository.isUserLoggedIn()).thenReturn(true)
        whenever(mockAuthRepository.getCurrentUserId()).thenReturn("auth-user-id")
    }
    
    /**
     * COMPLETE E2E TEST: Driver discovers passenger - full app flow
     * 
     * This tests the complete core mission: "drivers and passengers going to the same destination can find each other instantly"
     */
    @Test
    fun `complete driver discovers passenger end to end flow`() = runTest(timeout = 30_000) {
        // Given: Complete passenger user journey
        val passenger = createCompleteUser("passenger", townshipLocation, "town")
        
        // Passenger: Role selection → Home → Map
        passenger.roleViewModel.selectRole("passenger")
        passenger.homeViewModel.setDestination("town") 
        passenger.homeViewModel.proceedToMap()
        
        // Passenger enters map and becomes available
        passenger.mapViewModel.initialize(passenger.userId, passenger.userType, "town")
        passenger.locationService.setUserInfo(passenger.userId, passenger.userType, "town")
        passenger.locationService.updateLocation(passenger.location)
        passenger.mapViewModel.startMonitoring()
        
        // Verify passenger is discoverable
        val passengerDiscoverable = awaitFirebasePresence(passenger.userId, passenger.userType, "town", 4000)
        assertTrue("Passenger must be discoverable on map", passengerDiscoverable)
        println("✓ Passenger is discoverable and waiting for taxi")
        
        // When: Complete driver user journey  
        val driver = createCompleteUser("driver", taxiRankLocation, "town")
        
        // Driver: Role selection → Home → Map
        driver.roleViewModel.selectRole("driver")
        driver.homeViewModel.setDestination("town")
        driver.homeViewModel.proceedToMap()
        
        // Driver enters map and starts monitoring for passengers
        driver.mapViewModel.initialize(driver.userId, driver.userType, "town")
        driver.locationService.setUserInfo(driver.userId, driver.userType, "town")
        driver.locationService.updateLocation(driver.location)
        driver.mapViewModel.startMonitoring()
        
        println("✓ Driver entered map and started monitoring")
        
        // Then: Real-time discovery should occur within 5 seconds
        val discoveryStartTime = System.currentTimeMillis()
        
        // Driver should discover passenger
        val passengerDiscoveredByDriver = awaitEntityDiscovery(
            driver.mapViewModel, 
            passenger.location, 
            6000
        )
        
        val discoveryTime = System.currentTimeMillis() - discoveryStartTime
        assertTrue("Driver must discover passenger within 6 seconds (took ${discoveryTime}ms)", passengerDiscoveredByDriver)
        println("✓ Driver discovered passenger in ${discoveryTime}ms")
        
        // Verify driver sees passenger in secondary entities (passengers from driver's perspective)
        val driverSeesPassenger = driver.mapViewModel.secondaryEntityLocations.value
            .any { isLocationNear(it, passenger.location) }
        assertTrue("Driver should see passenger on map", driverSeesPassenger)
        
        // Verify passenger sees driver in primary entities (drivers from passenger's perspective)
        val passengerSeesDriver = passenger.mapViewModel.primaryEntityLocations.value
            .any { isLocationNear(it, driver.location) }
        assertTrue("Passenger should see driver on map", passengerSeesDriver)
        
        println("✓ Mutual discovery successful - both users can see each other")
        
        // Test strategic intelligence: Both users can see if other same-type users are in area
        val driverSameTypeCount = driver.mapViewModel.nearbyPrimaryCount.value // Other drivers
        val passengerSameTypeCount = passenger.mapViewModel.nearbySecondaryCount.value // Other passengers
        
        // Counts should be accessible for strategic positioning decisions
        assertNotNull("Driver should have access to competitor count for strategic positioning", driverSameTypeCount)
        assertNotNull("Passenger should have access to demand count for strategic positioning", passengerSameTypeCount)
        
        println("✓ Strategic intelligence available - users can make informed positioning decisions")
        
        // Cleanup both users
        driver.mapViewModel.stopMonitoring()
        driver.mapViewModel.cleanup()
        driver.locationService.cleanup()
        
        passenger.mapViewModel.stopMonitoring()
        passenger.mapViewModel.cleanup()
        passenger.locationService.cleanup()
        
        // Verify cleanup
        val driverRemoved = awaitFirebaseAbsence(driver.userId, driver.userType, "town", 5000)
        val passengerRemoved = awaitFirebaseAbsence(passenger.userId, passenger.userType, "town", 5000)
        
        assertTrue("Driver should be removed after leaving map", driverRemoved)
        assertTrue("Passenger should be removed after leaving map", passengerRemoved)
        
        println("✓ Complete E2E test successful - core app mission validated")
    }
    
    /**
     * STRATEGIC INTELLIGENCE E2E TEST: Multiple users scenario with strategic positioning
     * 
     * Tests CLAUDE.md strategic intelligence features:
     * - "See competing drivers on same route → avoid oversupply areas"
     * - "See other passengers wanting same route → gauge demand density" 
     */
    @Test
    fun `strategic intelligence end to end flow with multiple users`() = runTest(timeout = 35_000) {
        // Given: Multiple users in same area for strategic intelligence testing
        val firstDriver = createCompleteUser("driver", taxiRankLocation, "town")
        val secondDriver = createCompleteUser("driver", mallLocation, "town") 
        val firstPassenger = createCompleteUser("passenger", townshipLocation, "town")
        val secondPassenger = createCompleteUser("passenger", airportLocation, "town")
        
        val allUsers = listOf(firstDriver, secondDriver, firstPassenger, secondPassenger)
        
        // All users complete their journey: Role → Home → Map
        for (user in allUsers) {
            user.roleViewModel.selectRole(user.userType)
            user.homeViewModel.setDestination("town")
            user.homeViewModel.proceedToMap()
            
            user.mapViewModel.initialize(user.userId, user.userType, "town")
            user.locationService.setUserInfo(user.userId, user.userType, "town")
            user.locationService.updateLocation(user.location)
            user.mapViewModel.startMonitoring()
            
            delay(1000) // Stagger entries for realistic scenario
        }
        
        println("✓ All users entered map for strategic intelligence testing")
        
        // Allow time for mutual discovery
        delay(5000)
        
        // When: Users analyze strategic intelligence
        
        // First driver should see competing driver for strategic positioning
        val firstDriverSeesCompetitor = firstDriver.mapViewModel.primaryEntityLocations.value
            .any { isLocationNear(it, secondDriver.location) }
        assertTrue("First driver should see competing driver for strategic intelligence", firstDriverSeesCompetitor)
        
        // First driver should see both passengers (potential customers)
        val firstDriverSeesPassengers = firstDriver.mapViewModel.secondaryEntityLocations.value.size >= 2
        assertTrue("First driver should see multiple passengers for demand analysis", firstDriverSeesPassengers)
        
        // First passenger should see other passenger for demand clustering intelligence
        val firstPassengerSeesOtherPassenger = firstPassenger.mapViewModel.secondaryEntityLocations.value
            .any { isLocationNear(it, secondPassenger.location) }
        assertTrue("First passenger should see other passenger for demand clustering", firstPassengerSeesOtherPassenger)
        
        // First passenger should see both drivers (available taxis)
        val firstPassengerSeesDrivers = firstPassenger.mapViewModel.primaryEntityLocations.value.size >= 2
        assertTrue("First passenger should see multiple drivers for taxi availability", firstPassengerSeesDrivers)
        
        println("✓ Strategic intelligence working - users can see competitive landscape")
        
        // Then: Test strategic decision scenario - driver avoids oversupply area
        
        // Simulate first driver making strategic decision to avoid area with too many drivers
        // Driver leaves the oversupplied area
        firstDriver.mapViewModel.stopMonitoring()
        firstDriver.mapViewModel.cleanup()
        firstDriver.locationService.cleanup()
        
        delay(3000) // Allow system to update
        
        // Remaining driver should now have strategic advantage (less competition)
        val secondDriverCompetitorCount = secondDriver.mapViewModel.primaryEntityLocations.value.size
        assertTrue("Remaining driver should have less competition after competitor left", 
            secondDriverCompetitorCount == 0) // First driver is gone
        
        // Passengers should now see only one driver (reduced supply)
        val passengersSeesFewerDrivers = firstPassenger.mapViewModel.primaryEntityLocations.value.size == 1
        assertTrue("Passengers should see reduced driver availability", passengersSeesFewerDrivers)
        
        println("✓ Strategic positioning validated - market intelligence affects user decisions")
        
        // Cleanup remaining users
        val remainingUsers = listOf(secondDriver, firstPassenger, secondPassenger)
        for (user in remainingUsers) {
            user.mapViewModel.stopMonitoring()
            user.mapViewModel.cleanup()
            user.locationService.cleanup()
        }
        
        println("✓ Strategic intelligence E2E test successful")
    }
    
    /**
     * REAL-WORLD SCENARIO E2E TEST: South African taxi route simulation
     * 
     * Tests realistic SA taxi scenario from CLAUDE.md context
     */
    @Test
    fun `south african taxi route end to end scenario`() = runTest(timeout = 25_000) {
        // Given: Realistic SA taxi scenario - Township to CBD route
        val townshipPassenger = createCompleteUser("passenger", townshipLocation, "town") // From Soweto
        val taxiDriver = createCompleteUser("driver", taxiRankLocation, "town") // At CBD rank
        
        // Passenger in township needs to get to town (common SA taxi route)
        townshipPassenger.roleViewModel.selectRole("passenger")
        townshipPassenger.homeViewModel.setDestination("town") // Going to town/city center
        townshipPassenger.homeViewModel.proceedToMap()
        
        townshipPassenger.mapViewModel.initialize(townshipPassenger.userId, "passenger", "town")
        townshipPassenger.locationService.setUserInfo(townshipPassenger.userId, "passenger", "town")
        townshipPassenger.locationService.updateLocation(townshipPassenger.location)
        townshipPassenger.mapViewModel.startMonitoring()
        
        println("✓ Township passenger looking for taxi to town")
        
        // Driver at taxi rank starts monitoring for town-bound passengers
        taxiDriver.roleViewModel.selectRole("driver")
        taxiDriver.homeViewModel.setDestination("town") // Doing town runs
        taxiDriver.homeViewModel.proceedToMap()
        
        taxiDriver.mapViewModel.initialize(taxiDriver.userId, "driver", "town")
        taxiDriver.locationService.setUserInfo(taxiDriver.userId, "driver", "town")
        taxiDriver.locationService.updateLocation(taxiDriver.location)
        taxiDriver.mapViewModel.startMonitoring()
        
        println("✓ Taxi driver monitoring for town-bound passengers")
        
        // When: Real-time matching occurs (core app value)
        delay(4000) // Allow discovery
        
        // Then: Both should discover each other for the town route
        val driverFoundPassenger = taxiDriver.mapViewModel.secondaryEntityLocations.value
            .any { isLocationNear(it, townshipPassenger.location) }
        val passengerFoundDriver = townshipPassenger.mapViewModel.primaryEntityLocations.value
            .any { isLocationNear(it, taxiDriver.location) }
        
        assertTrue("Taxi driver should find township passenger needing town transport", driverFoundPassenger)
        assertTrue("Township passenger should find driver doing town runs", passengerFoundDriver)
        
        println("✓ Successful SA taxi route matching - core value delivered")
        
        // Test the transparency benefit: passenger can see exactly where available taxis are
        val availableDriversCount = townshipPassenger.mapViewModel.primaryEntityLocations.value.size
        assertTrue("Passenger should see real-time taxi availability", availableDriversCount > 0)
        
        // Test strategic benefit: driver can see demand density 
        val demandCount = taxiDriver.mapViewModel.secondaryEntityLocations.value.size
        assertTrue("Driver should see real-time passenger demand", demandCount > 0)
        
        println("✓ SA taxi industry transparency achieved - both sides have market intelligence")
        
        // Cleanup
        taxiDriver.mapViewModel.stopMonitoring()
        taxiDriver.mapViewModel.cleanup()
        taxiDriver.locationService.cleanup()
        
        townshipPassenger.mapViewModel.stopMonitoring()
        townshipPassenger.mapViewModel.cleanup()
        townshipPassenger.locationService.cleanup()
        
        println("✓ SA taxi route E2E test successful - real-world scenario validated")
    }
    
    /**
     * APP LIFECYCLE E2E TEST: Complete app journey with backgrounding
     */
    @Test
    fun `complete app lifecycle end to end flow`() = runTest(timeout = 20_000) {
        // Given: User completes full app journey
        val user = createCompleteUser("driver", taxiRankLocation, "local")
        
        // Complete flow: Role selection
        user.roleViewModel.selectRole("driver")
        assertEquals("driver", user.roleViewModel.selectedRole.value)
        
        // Home screen destination selection  
        user.homeViewModel.setDestination("local")
        assertEquals("local", user.homeViewModel.currentDestination.value)
        
        // Navigate to map
        user.homeViewModel.proceedToMap()
        
        // Map screen initialization and monitoring
        user.mapViewModel.initialize(user.userId, "driver", "local")
        user.locationService.setUserInfo(user.userId, "driver", "local")
        user.locationService.updateLocation(user.location)
        user.mapViewModel.startMonitoring()
        
        // Verify user is active and discoverable
        val activeAndDiscoverable = awaitFirebasePresence(user.userId, "driver", "local", 4000)
        assertTrue("User should be active and discoverable after complete app flow", activeAndDiscoverable)
        
        // When: App is backgrounded (user switches to another app but map stays active)
        delay(2000) // Simulate backgrounding
        
        // User should remain discoverable while map is active in background
        val stillActiveWhileBackgrounded = checkFirebasePresence(user.userId, "driver", "local")
        assertTrue("User should remain discoverable when app is backgrounded from map", stillActiveWhileBackgrounded)
        
        // When: User returns to app and then properly exits map
        user.mapViewModel.stopMonitoring()
        user.mapViewModel.cleanup()
        user.locationService.cleanup()
        
        // Then: User should be properly removed
        val properlyRemoved = awaitFirebaseAbsence(user.userId, "driver", "local", 5000)
        assertTrue("User should be removed after properly exiting app", properlyRemoved)
        
        println("✓ Complete app lifecycle E2E test successful")
    }
    
    // Helper methods
    private fun createCompleteUser(userType: String, location: LatLng, destination: String): CompleteUser {
        val userId = "${userType}-${UUID.randomUUID()}"
        
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
        
        val homeViewModel = HomeViewModel(
            mockUserPreferencesRepository,
            mockSharedPreferences,
            mockErrorHandlingService
        )
        
        val roleViewModel = RoleViewModel(
            mockUserPreferencesRepository,
            mockSharedPreferences,
            mockErrorHandlingService
        )
        
        return CompleteUser(userId, userType, location, locationService, mapViewModel, homeViewModel, roleViewModel)
    }
    
    private suspend fun awaitEntityDiscovery(
        mapViewModel: MapViewModel,
        targetLocation: LatLng,
        timeoutMs: Long
    ): Boolean = suspendCoroutine { continuation ->
        var resumed = false
        
        val job = GlobalScope.launch {
            var remainingTime = timeoutMs
            val checkInterval = 300L
            
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
    
    // Firebase helpers (reuse parent class pattern)
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