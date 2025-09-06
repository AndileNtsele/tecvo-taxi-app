package com.tecvo.taxi.viewmodel

import android.location.Location
import androidx.test.core.app.ApplicationProvider
import com.tecvo.taxi.repository.UserPreferencesRepository
import com.tecvo.taxi.utils.TestFirebaseUtil
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], application = HiltTestApplication::class)
class RoleViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var mockUserPreferencesRepository: UserPreferencesRepository

    @Mock
    private lateinit var mockLocation: Location

    @get:Rule
    val firebaseRule = TestFirebaseUtil.FirebaseTestRule()

    // Class under test
    private lateinit var viewModel: RoleViewModel

    // Test constants
    private val testRole = "driver"
    private val testDestination = "town"
    private val testLatitude = 37.7749
    private val testLongitude = -122.4194
    private val testLatLng = LatLng(testLatitude, testLongitude)

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // Initialize Firebase for testing
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        TestFirebaseUtil.initializeTestFirebase(context)

        // Configure mock behavior
        setupMockDependencies()
    }

    private fun setupMockDependencies() {
        // Mock UserPreferencesRepository behavior
        runTest {
            `when`(mockUserPreferencesRepository.getLastSelectedDestination()).thenReturn(testDestination)
            `when`(mockUserPreferencesRepository.getLastSelectedRole()).thenReturn(testRole)
        }

        // Mock Location behavior
        `when`(mockLocation.latitude).thenReturn(testLatitude)
        `when`(mockLocation.longitude).thenReturn(testLongitude)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads last destination from repository`() = runTest {
        // Given - repository returns a specific destination
        `when`(mockUserPreferencesRepository.getLastSelectedDestination()).thenReturn(testDestination)

        // When - create the viewModel which will call init
        viewModel = RoleViewModel(mockUserPreferencesRepository)

        // Process pending tasks
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - check that lastDestination was updated and loading flag was set to false
        assertEquals(testDestination, viewModel.lastDestination.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `setUserRole updates role and saves to repository`() = runTest {
        // Given
        viewModel = RoleViewModel(mockUserPreferencesRepository)

        // When
        viewModel.setUserRole(testRole)

        // Process pending tasks
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(testRole, viewModel.userRole.value)
        verify(mockUserPreferencesRepository).saveLastSelectedRole(testRole)
    }

    @Test
    fun `saveDestination updates destination and saves to repository`() = runTest {
        // Given
        viewModel = RoleViewModel(mockUserPreferencesRepository)

        // When
        viewModel.saveDestination(testDestination)

        // Process pending tasks
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(testDestination, viewModel.lastDestination.value)
        verify(mockUserPreferencesRepository).saveLastSelectedDestination(testDestination)
    }

    @Test
    fun `updateLocation with Location updates currentLocation StateFlow`() {
        // Given
        viewModel = RoleViewModel(mockUserPreferencesRepository)

        // When
        viewModel.updateLocation(mockLocation)

        // Then
        val currentLocation = viewModel.currentLocation.value
        assertEquals(testLatitude, currentLocation?.latitude)
        assertEquals(testLongitude, currentLocation?.longitude)
    }

    @Test
    fun `updateLocation with LatLng updates currentLocation StateFlow`() {
        // Given
        viewModel = RoleViewModel(mockUserPreferencesRepository)

        // When
        viewModel.updateLocation(testLatLng)

        // Then
        assertEquals(testLatLng, viewModel.currentLocation.value)
    }

    @Test
    fun `handles exception when loadLastDestination fails`() = runTest {
        // Given - repository throws exception
        `when`(mockUserPreferencesRepository.getLastSelectedDestination()).thenThrow(RuntimeException("Test exception"))

        // When - create the viewModel which will call init
        viewModel = RoleViewModel(mockUserPreferencesRepository)

        // Process pending tasks
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - loading should be set to false despite the exception
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `handles exception when setUserRole fails`() = runTest {
        // Given
        viewModel = RoleViewModel(mockUserPreferencesRepository)
        `when`(mockUserPreferencesRepository.saveLastSelectedRole(any())).thenThrow(RuntimeException("Test exception"))

        // When
        viewModel.setUserRole(testRole)

        // Process pending tasks
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - role should still be updated despite repository failure
        assertEquals(testRole, viewModel.userRole.value)
    }

    @Test
    fun `handles exception when saveDestination fails`() = runTest {
        // Given
        viewModel = RoleViewModel(mockUserPreferencesRepository)
        `when`(mockUserPreferencesRepository.saveLastSelectedDestination(any())).thenThrow(RuntimeException("Test exception"))

        // When
        viewModel.saveDestination(testDestination)

        // Process pending tasks
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - destination should still be updated despite repository failure
        assertEquals(testDestination, viewModel.lastDestination.value)
    }
}