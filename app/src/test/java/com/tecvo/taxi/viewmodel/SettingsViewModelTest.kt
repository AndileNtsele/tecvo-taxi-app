package com.tecvo.taxi.viewmodel

import android.app.Activity
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.tecvo.taxi.models.Country
import com.tecvo.taxi.repository.AuthRepository
import com.tecvo.taxi.repository.UserPreferencesRepository
import com.tecvo.taxi.utils.CountryUtils
import com.tecvo.taxi.utils.TestFirebaseUtil
import com.google.firebase.FirebaseException
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import java.lang.reflect.Field

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], application = HiltTestApplication::class)
class SettingsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var mockAuthRepository: AuthRepository

    @Mock
    private lateinit var mockUserPreferencesRepository: UserPreferencesRepository

    @Mock
    private lateinit var mockActivity: Activity

    @get:Rule
    val firebaseRule = TestFirebaseUtil.FirebaseTestRule()

    // Class under test
    private lateinit var viewModel: SettingsViewModel

    // Test constants
    private val testPhoneNumber = "0123456789"
    private val testVerificationId = "test-verification-id"
    private val testOtpCode = "123456"

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // Initialize Firebase for testing
        val context = ApplicationProvider.getApplicationContext<Context>()
        TestFirebaseUtil.initializeTestFirebase(context)

        // Configure mock StateFlows with non-null initial values
        setupMockStateFlows()

        // Mock suspend functions responses
        setupMockSuspendFunctions()
    }

    private fun setupMockStateFlows() {
        // Create state flows with initial values to prevent NPEs
        whenever(mockUserPreferencesRepository.notificationRadiusFlow)
            .thenReturn(MutableStateFlow(1.0f))
        whenever(mockUserPreferencesRepository.notificationsEnabledFlow)
            .thenReturn(MutableStateFlow(true))
        whenever(mockUserPreferencesRepository.notifyDifferentRoleFlow)
            .thenReturn(MutableStateFlow(true))
        whenever(mockUserPreferencesRepository.notifySameRoleFlow)
            .thenReturn(MutableStateFlow(false))
        whenever(mockUserPreferencesRepository.notifyProximityFlow)
            .thenReturn(MutableStateFlow(false))
    }

    private fun setupMockSuspendFunctions() = runTest {
        // Mock repository suspend functions with direct responses
        whenever(mockUserPreferencesRepository.areNotificationsEnabled())
            .thenReturn(true)
        whenever(mockUserPreferencesRepository.areDifferentRoleNotificationsEnabled())
            .thenReturn(true)
        whenever(mockUserPreferencesRepository.areSameRoleNotificationsEnabled())
            .thenReturn(false)
        whenever(mockUserPreferencesRepository.areProximityNotificationsEnabled())
            .thenReturn(false)
        whenever(mockUserPreferencesRepository.getNotificationRadius())
            .thenReturn(1.0f)
    }

    private fun createViewModel(): SettingsViewModel {
        // Initialize new viewModel
        return SettingsViewModel(mockAuthRepository, mockUserPreferencesRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `updatePhoneNumber updates state and clears error`() {
        // Create viewModel after all mocks are set up
        viewModel = createViewModel()

        // Given
        setStateFlowValue(viewModel, "_phoneError", "Previous error")

        // When
        viewModel.updatePhoneNumber(testPhoneNumber)

        // Then
        assertEquals(testPhoneNumber, viewModel.phoneNumber.value)
        assertEquals("", viewModel.phoneError.value)
    }

    @Test
    fun `updateOtpCode updates state and clears error when valid`() {
        // Create viewModel after all mocks are set up
        viewModel = createViewModel()

        // Given
        setStateFlowValue(viewModel, "_otpError", "Previous error")

        // When
        viewModel.updateOtpCode(testOtpCode)

        // Then
        assertEquals(testOtpCode, viewModel.otpCode.value)
        assertEquals("", viewModel.otpError.value)
    }

    @Test
    fun `updateOtpCode ignores input longer than 6 digits`() {
        // Create viewModel after all mocks are set up
        viewModel = createViewModel()

        // Given
        setStateFlowValue(viewModel, "_otpCode", testOtpCode)

        // When
        viewModel.updateOtpCode("1234567")

        // Then
        assertEquals(testOtpCode, viewModel.otpCode.value)
    }

    @Test
    fun `updateSelectedCountry updates country state`() {
        // Create viewModel after all mocks are set up
        viewModel = createViewModel()

        // Given
        val testCountry = Country(
            name = "United States",
            code = "US",
            dialCode = "+1",
            flagEmoji = "🇺🇸"
        )

        // When
        viewModel.updateSelectedCountry(testCountry)

        // Then
        assertEquals(testCountry, viewModel.selectedCountry.value)
    }

    @Test
    fun `startAccountDeletion sets deletion step to 1`() {
        // Create viewModel after all mocks are set up
        viewModel = createViewModel()

        // When
        viewModel.startAccountDeletion()

        // Then
        assertEquals(1, viewModel.accountDeletionStep.value)
    }

    @Test
    fun `nextAccountDeletionStep increments deletion step`() {
        // Create viewModel after all mocks are set up
        viewModel = createViewModel()

        // Given
        setStateFlowValue(viewModel, "_accountDeletionStep", 1)

        // When
        viewModel.nextAccountDeletionStep()

        // Then
        assertEquals(2, viewModel.accountDeletionStep.value)
    }

    @Test
    fun `cancelAccountDeletion resets deletion process`() {
        // Create viewModel after all mocks are set up
        viewModel = createViewModel()

        // Given
        setStateFlowValue(viewModel, "_accountDeletionStep", 2)
        setStateFlowValue(viewModel, "_phoneNumber", testPhoneNumber)
        setStateFlowValue(viewModel, "_otpCode", testOtpCode)
        setStateFlowValue(viewModel, "_phoneError", "Some error")
        setStateFlowValue(viewModel, "_otpError", "Some error")

        // When
        viewModel.cancelAccountDeletion()

        // Then
        assertEquals(0, viewModel.accountDeletionStep.value)
        assertEquals("", viewModel.phoneNumber.value)
        assertEquals("", viewModel.otpCode.value)
        assertEquals("", viewModel.phoneError.value)
        assertEquals("", viewModel.otpError.value)
    }

    @Test
    fun `sendVerificationCode shows error for empty phone number`() {
        // Create viewModel after all mocks are set up
        viewModel = createViewModel()

        // Given
        setStateFlowValue(viewModel, "_phoneNumber", "")

        // When
        viewModel.sendVerificationCode(mockActivity)

        // Then
        assertEquals("Please enter a phone number", viewModel.phoneError.value)
    }

    @Test
    fun `sendVerificationCode calls repository and advances step on success`() = runTest {
        // Create viewModel after all mocks are set up
        viewModel = createViewModel()

        // Given
        setStateFlowValue(viewModel, "_phoneNumber", testPhoneNumber)
        setStateFlowValue(viewModel, "_selectedCountry", CountryUtils.DEFAULT_COUNTRY)

        // Mock verification success
        doAnswer { invocation ->
            val onVerificationIdReceived = invocation.getArgument<(String) -> Unit>(2)
            onVerificationIdReceived(testVerificationId)
        }.whenever(mockAuthRepository).verifyPhoneNumber(
            any(),
            eq(mockActivity),
            any(),
            any(),
            any()
        )

        // When
        viewModel.sendVerificationCode(mockActivity)

        // Process pending tasks
        advanceUntilIdle()
        ShadowLooper.idleMainLooper()

        // Then
        assertEquals(testVerificationId, viewModel.verificationId.value)
        assertEquals(2, viewModel.accountDeletionStep.value)
        assertEquals("", viewModel.phoneError.value)
    }

    @Test
    fun `sendVerificationCode shows error on verification failure`() = runTest {
        // Create viewModel after all mocks are set up
        viewModel = createViewModel()

        // Given
        setStateFlowValue(viewModel, "_phoneNumber", testPhoneNumber)
        setStateFlowValue(viewModel, "_selectedCountry", CountryUtils.DEFAULT_COUNTRY)

        // Mock verification failure
        val firebaseException = FirebaseException("Verification failed")
        doAnswer { invocation ->
            val onVerificationFailed = invocation.getArgument<(FirebaseException) -> Unit>(4)
            onVerificationFailed(firebaseException)
        }.whenever(mockAuthRepository).verifyPhoneNumber(
            any(),
            eq(mockActivity),
            any(),
            any(),
            any()
        )

        // When
        viewModel.sendVerificationCode(mockActivity)

        // Process pending tasks
        advanceUntilIdle()
        ShadowLooper.idleMainLooper()

        // Then
        assertEquals("Verification failed", viewModel.phoneError.value)
    }

    @Test
    fun `verifyOtpCode shows error for invalid code length`() {
        // Create viewModel after all mocks are set up
        viewModel = createViewModel()

        // Given
        setStateFlowValue(viewModel, "_otpCode", "12345") // 5 digits instead of 6

        // When
        viewModel.verifyOtpCode()

        // Then
        assertEquals("Please enter a valid 6-digit code", viewModel.otpError.value)
        assertFalse(viewModel.isProcessingDeletion.value)
    }

    @Test
    fun `verifyOtpCode calls repository with valid code`() = runTest {
        // Create viewModel after all mocks are set up
        viewModel = createViewModel()

        // Given
        setStateFlowValue(viewModel, "_otpCode", testOtpCode)
        setStateFlowValue(viewModel, "_verificationId", testVerificationId)

        // Mock repository response
        whenever(mockAuthRepository.verifyOtpCode(eq(testVerificationId), eq(testOtpCode)))
            .thenReturn(Result.success(Unit))

        whenever(mockAuthRepository.deleteUserAccount())
            .thenReturn(Result.success(Unit))

        // When
        viewModel.verifyOtpCode()

        // Process pending tasks
        advanceUntilIdle()

        // Then
        verify(mockAuthRepository).verifyOtpCode(testVerificationId, testOtpCode)
        verify(mockAuthRepository).deleteUserAccount()
        assertEquals(0, viewModel.accountDeletionStep.value)
        assertFalse(viewModel.isProcessingDeletion.value)
    }

    @Test
    fun `verifyOtpCode handles verification failure`() = runTest {
        // Create viewModel after all mocks are set up
        viewModel = createViewModel()

        // Given
        setStateFlowValue(viewModel, "_otpCode", testOtpCode)
        setStateFlowValue(viewModel, "_verificationId", testVerificationId)
        setStateFlowValue(viewModel, "_isProcessingDeletion", true)

        // Set up direct exception handling - bypass the Result.failure mechanism
        // which appears to be not working correctly with mocks in tests
        whenever(mockAuthRepository.verifyOtpCode(any(), any()))
            .thenAnswer { throw Exception("Invalid code") }

        // When
        viewModel.verifyOtpCode()

        // Process pending tasks
        advanceUntilIdle()

        // Idle the main looper to catch any pending UI updates
        ShadowLooper.idleMainLooper()

        // Then
        assertEquals("Invalid code", viewModel.otpError.value)
        assertFalse(viewModel.isProcessingDeletion.value)
    }

    @Test
    fun `verifyOtpCode handles account deletion failure`() = runTest {
        // Create viewModel after all mocks are set up
        viewModel = createViewModel()

        // Given
        setStateFlowValue(viewModel, "_otpCode", testOtpCode)
        setStateFlowValue(viewModel, "_verificationId", testVerificationId)

        // Mock repository responses
        whenever(mockAuthRepository.verifyOtpCode(eq(testVerificationId), eq(testOtpCode)))
            .thenReturn(Result.success(Unit))

        val deleteException = Exception("Delete failed")
        whenever(mockAuthRepository.deleteUserAccount())
            .thenReturn(Result.failure(deleteException))

        // When
        viewModel.verifyOtpCode()

        // Process pending tasks
        advanceUntilIdle()

        // Then
        verify(mockAuthRepository).verifyOtpCode(testVerificationId, testOtpCode)
        verify(mockAuthRepository).deleteUserAccount()
        assertEquals("Failed to delete account: Delete failed", viewModel.otpError.value)
        assertFalse(viewModel.isProcessingDeletion.value)
    }

    @Test
    fun `loadNotificationSettings loads settings from repository`() = runTest {
        // Mock suspend function results
        whenever(mockUserPreferencesRepository.areNotificationsEnabled())
            .thenReturn(true)
        whenever(mockUserPreferencesRepository.areDifferentRoleNotificationsEnabled())
            .thenReturn(true)
        whenever(mockUserPreferencesRepository.areSameRoleNotificationsEnabled())
            .thenReturn(false)
        whenever(mockUserPreferencesRepository.areProximityNotificationsEnabled())
            .thenReturn(false)
        whenever(mockUserPreferencesRepository.getNotificationRadius())
            .thenReturn(1.0f)

        // Create a new viewModel to trigger loadNotificationSettings
        viewModel = createViewModel()

        // Advance the dispatcher to let all coroutines complete
        advanceUntilIdle()

        // Then - verify settings were loaded from repository
        assertEquals(true, viewModel.notificationsEnabled.value)
        assertEquals(true, viewModel.notifyDifferentRole.value)
        assertEquals(false, viewModel.notifySameRole.value)
        assertEquals(false, viewModel.notifyProximity.value)
        assertEquals(1.0f, viewModel.notificationRadius.value)
    }

    @Test
    fun `saveNotificationSettings saves settings to repository`() = runTest {
        // Given
        viewModel = createViewModel()

        // Set up the mock to properly handle saveNotificationSettings
        whenever(mockUserPreferencesRepository.saveNotificationSettings(
            enabled = false,
            notifyDifferentRole = false,
            notifySameRole = true,
            proximityEnabled = true,
            radiusKm = 3.0f
        )).thenReturn(Unit)  // Changed from thenReturn(1)

        // When
        viewModel.saveNotificationSettings(
            enabled = false,
            notifyDifferentRole = false,
            notifySameRole = true,
            proximityEnabled = true,
            radiusKm = 3.0f
        )

        // Process pending tasks
        advanceUntilIdle()

        // Then - verify repository was called with correct parameters
        verify(mockUserPreferencesRepository).saveNotificationSettings(
            enabled = false,
            notifyDifferentRole = false,
            notifySameRole = true,
            proximityEnabled = true,
            radiusKm = 3.0f
        )
    }

    @Test
    fun `updateNotificationRadius calls repository with valid radius`() = runTest {
        // Given
        viewModel = createViewModel()

        // Mock the suspend function
        whenever(mockUserPreferencesRepository.setNotificationRadius(3.5f))
            .thenReturn(Unit)  // Changed from thenReturn(1)

        // When
        viewModel.updateNotificationRadius("3.5")

        // Process pending tasks
        advanceUntilIdle()

        // Then
        verify(mockUserPreferencesRepository).setNotificationRadius(3.5f)
    }

    @Test
    fun `updateNotificationRadius ignores invalid input`() = runTest {
        // Given
        viewModel = createViewModel()

        // When
        viewModel.updateNotificationRadius("invalid")

        // Process pending tasks
        advanceUntilIdle()

        // Then - verify repository was not called
        verify(mockUserPreferencesRepository, never()).setNotificationRadius(any())
    }

    // Helper method to inject values via reflection (for testing private fields)
    @Suppress("UNCHECKED_CAST")
    private fun <T> setStateFlowValue(instance: Any, fieldName: String, value: T) {
        val field = findField(instance.javaClass, fieldName)
        field.isAccessible = true
        val stateFlow = field.get(instance) as MutableStateFlow<T>
        stateFlow.value = value
    }

    private fun findField(clazz: Class<*>, fieldName: String): Field {
        return try {
            clazz.getDeclaredField(fieldName)
        } catch (e: NoSuchFieldException) {
            val superclass = clazz.superclass
            if (superclass != null) {
                findField(superclass, fieldName)
            } else {
                throw e
            }
        }
    }
}