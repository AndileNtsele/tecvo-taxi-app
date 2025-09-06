package com.tecvo.taxi.viewmodel

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.tecvo.taxi.repository.AuthRepository
import com.tecvo.taxi.repository.UserPreferencesRepository
import com.tecvo.taxi.utils.TestFirebaseUtil
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import java.lang.reflect.Field

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], application = HiltTestApplication::class)
class HomeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var mockAuthRepository: AuthRepository

    @Mock
    private lateinit var mockPreferencesRepository: UserPreferencesRepository

    @get:Rule
    val firebaseRule = TestFirebaseUtil.FirebaseTestRule()

    // Class under test
    private lateinit var viewModel: HomeViewModel

    // Test constants
    private val testRole = "driver"

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // Initialize Firebase for testing
        val context = ApplicationProvider.getApplicationContext<Context>()
        TestFirebaseUtil.initializeTestFirebase(context)

        // Configure mock behavior
        setupMockDependencies()
    }

    private fun setupMockDependencies() = runTest {
        // Mock AuthRepository behavior
        `when`(mockAuthRepository.isUserLoggedIn()).thenReturn(true)

        // Mock UserPreferencesRepository behavior
        `when`(mockPreferencesRepository.getLastSelectedRole()).thenReturn(testRole)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init sets login status from repository`() = runTest {
        // Given
        `when`(mockAuthRepository.isUserLoggedIn()).thenReturn(true)

        // When - create viewModel which will call init
        viewModel = HomeViewModel(mockAuthRepository, mockPreferencesRepository)

        // Process pending tasks
        testDispatcher.scheduler.advanceUntilIdle()
        ShadowLooper.idleMainLooper()

        // Then
        assertTrue(viewModel.isUserLoggedIn.value)
        verify(mockAuthRepository).isUserLoggedIn()
    }

    @Test
    fun `init loads last selected role from repository`() = runTest {
        // Given
        `when`(mockPreferencesRepository.getLastSelectedRole()).thenReturn(testRole)

        // When - create viewModel which will call init
        viewModel = HomeViewModel(mockAuthRepository, mockPreferencesRepository)

        // Process pending tasks
        testDispatcher.scheduler.advanceUntilIdle()
        ShadowLooper.idleMainLooper()

        // Then
        assertEquals(testRole, viewModel.lastSelectedRole.value)
        verify(mockPreferencesRepository).getLastSelectedRole()
    }

    @Test
    fun `saveSelectedRole updates role and saves to repository`() = runTest {
        // Given
        viewModel = HomeViewModel(mockAuthRepository, mockPreferencesRepository)

        // When
        viewModel.saveSelectedRole(testRole)

        // Process pending tasks
        testDispatcher.scheduler.advanceUntilIdle()
        ShadowLooper.idleMainLooper()

        // Then
        assertEquals(testRole, viewModel.lastSelectedRole.value)
        verify(mockPreferencesRepository).saveLastSelectedRole(testRole)
    }

    @Test
    fun `signOut updates login status and calls repository`() = runTest {
        // Given
        viewModel = HomeViewModel(mockAuthRepository, mockPreferencesRepository)
        prepareViewModelForSignOut(viewModel)

        // When
        viewModel.signOut()

        // Process pending tasks
        testDispatcher.scheduler.advanceUntilIdle()
        ShadowLooper.idleMainLooper()

        // Then
        assertFalse(viewModel.isUserLoggedIn.value)
        verify(mockAuthRepository).signOut()
    }

    @Test
    fun `checkLoginStatus handles exception`() = runTest {
        // Given
        `when`(mockAuthRepository.isUserLoggedIn()).thenThrow(RuntimeException("Test exception"))

        // When - create viewModel which will call init containing checkLoginStatus
        viewModel = HomeViewModel(mockAuthRepository, mockPreferencesRepository)

        // Process pending tasks
        testDispatcher.scheduler.advanceUntilIdle()
        ShadowLooper.idleMainLooper()

        // Then - should not crash and loading should be set to false
        assertFalse(viewModel.isUserLoggedIn.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loadLastSelectedRole handles exception`() = runTest {
        // Given
        `when`(mockPreferencesRepository.getLastSelectedRole()).thenThrow(RuntimeException("Test exception"))

        // When - create viewModel which will call init containing loadLastSelectedRole
        viewModel = HomeViewModel(mockAuthRepository, mockPreferencesRepository)

        // Process pending tasks
        testDispatcher.scheduler.advanceUntilIdle()
        ShadowLooper.idleMainLooper()

        // Then - should not crash and role should remain null
        assertEquals(null, viewModel.lastSelectedRole.value)
    }

    @Test
    fun `saveSelectedRole handles exception`() = runTest {
        // Given
        `when`(mockPreferencesRepository.saveLastSelectedRole(testRole)).thenThrow(RuntimeException("Test exception"))
        viewModel = HomeViewModel(mockAuthRepository, mockPreferencesRepository)

        // When
        viewModel.saveSelectedRole(testRole)

        // Process pending tasks
        testDispatcher.scheduler.advanceUntilIdle()
        ShadowLooper.idleMainLooper()

        // Then - should not crash
        // Role should still be updated in memory even if saving to repository fails
        assertEquals(testRole, viewModel.lastSelectedRole.value)
    }

    @Test
    fun `signOut handles exception`() = runTest {
        // Given
        whenever(mockAuthRepository.signOut()).thenThrow(RuntimeException("Test exception"))
        viewModel = HomeViewModel(mockAuthRepository, mockPreferencesRepository)
        prepareViewModelForSignOut(viewModel)

        // When
        viewModel.signOut()

        // Process pending tasks
        testDispatcher.scheduler.advanceUntilIdle()
        ShadowLooper.idleMainLooper()

        // Then - should not crash and login status should remain true since the operation failed
        assertTrue(viewModel.isUserLoggedIn.value)
        verify(mockAuthRepository).signOut()
    }

    // Helper method for preparing the viewModel for signOut tests
    private fun prepareViewModelForSignOut(viewModel: HomeViewModel) {
        setUserLoggedInTrue(viewModel)
    }

    // Specific helper method without parameters that have fixed values
    private fun setUserLoggedInTrue(viewModel: HomeViewModel) {
        val field = findField(viewModel.javaClass, "_isUserLoggedIn")
        field.isAccessible = true
        val fieldValue = field.get(viewModel)

        if (fieldValue is MutableStateFlow<*>) {
            @Suppress("UNCHECKED_CAST")
            val stateFlow = fieldValue as? MutableStateFlow<Boolean>
            stateFlow?.value = true
        } else {
            throw IllegalStateException("Field _isUserLoggedIn is not of type MutableStateFlow<Boolean>")
        }
    }

    private fun findField(clazz: Class<*>, fieldName: String): Field {
        try {
            return clazz.getDeclaredField(fieldName)
        } catch (e: NoSuchFieldException) {
            val superClass = clazz.superclass ?: throw e
            return findField(superClass, fieldName)
        }
    }
}