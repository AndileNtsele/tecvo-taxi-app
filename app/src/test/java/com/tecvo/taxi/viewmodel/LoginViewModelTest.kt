package com.tecvo.taxi.viewmodel

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.test.core.app.ApplicationProvider
import com.tecvo.taxi.repository.AuthRepository
import com.tecvo.taxi.utils.TestFirebaseUtil
import com.google.firebase.FirebaseException
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
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import java.lang.reflect.Field

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], application = HiltTestApplication::class)
class LoginViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var mockAuthRepository: AuthRepository

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockConnectivityManager: ConnectivityManager

    @Mock
    private lateinit var mockNetwork: Network

    @Mock
    private lateinit var mockNetworkCapabilities: NetworkCapabilities

    @Mock
    private lateinit var mockActivity: Activity

    @get:Rule
    val firebaseRule = TestFirebaseUtil.FirebaseTestRule()

    private lateinit var viewModel: LoginViewModel

    private val testPhoneNumber = "0123456789"
    private val testVerificationId = "test-verification-id"
    private val testOtp = "123456"

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        val context = ApplicationProvider.getApplicationContext<Context>()
        TestFirebaseUtil.initializeTestFirebase(context)

        setupMockDependencies()

        runTest {
            `when`(mockAuthRepository.isUserLoggedIn()).thenReturn(false)
        }

        viewModel = LoginViewModel(mockAuthRepository, mockContext)

        // Initialize default StateFlow values to prevent null errors
        setStateFlowValue(viewModel, "_isLoggedIn", false)
    }

    private fun setupMockDependencies() {
        `when`(mockContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockConnectivityManager)
        `when`(mockConnectivityManager.activeNetwork).thenReturn(mockNetwork)
        `when`(mockConnectivityManager.getNetworkCapabilities(mockNetwork)).thenReturn(mockNetworkCapabilities)
        `when`(mockNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `verifyPhoneNumber handles verification failure`() = runTest {
        // Given
        viewModel.updatePhoneNumber(testPhoneNumber)
        viewModel.toggleTerms(true)

        // Use FirebaseException instead of regular Exception
        val firebaseException = mock(FirebaseException::class.java)
        `when`(firebaseException.message).thenReturn("Verification failed")

        // Mock verification failure with the right exception type
        doAnswer { invocation ->
            val onVerificationFailed = invocation.getArgument<(FirebaseException) -> Unit>(4)
            onVerificationFailed(firebaseException)
            null
        }.`when`(mockAuthRepository).verifyPhoneNumber(
            any(),
            eq(mockActivity),
            any(),
            any(),
            any()
        )

        // When
        viewModel.verifyPhoneNumber(mockActivity)

        // Process pending tasks and idle the main looper
        testDispatcher.scheduler.advanceUntilIdle()
        ShadowLooper.idleMainLooper()

        // Then - Wait for state to be updated
        assertEquals("Verification failed", viewModel.error.value)
        assertTrue(viewModel.loginState.value is LoginState.Error)
    }

    @Test
    fun `verifyPhoneNumber handles session storage error with user-friendly message`() = runTest {
        // Given
        viewModel.updatePhoneNumber(testPhoneNumber)
        viewModel.toggleTerms(true)

        // Use FirebaseException instead of regular Exception
        val firebaseException = mock(FirebaseException::class.java)
        `when`(firebaseException.message).thenReturn("Session storage error occurred")

        // Mock session storage error
        doAnswer { invocation ->
            val onVerificationFailed = invocation.getArgument<(FirebaseException) -> Unit>(4)
            onVerificationFailed(firebaseException)
            null
        }.`when`(mockAuthRepository).verifyPhoneNumber(
            any(),
            eq(mockActivity),
            any(),
            any(),
            any()
        )

        // When
        viewModel.verifyPhoneNumber(mockActivity)

        // Process pending tasks and idle the main looper
        testDispatcher.scheduler.advanceUntilIdle()
        ShadowLooper.idleMainLooper()

        // Then
        assertEquals("Authentication error: Please try closing your browser completely and reopening the app",
            viewModel.error.value)
    }

    @Test
    fun `verifyOtpCode handles verification failure`() = runTest {
        // Given
        viewModel.updateOtp(testOtp)
        setStateFlowValue(viewModel, "_verificationId", testVerificationId)

        // Explicitly set isLoggedIn to false to prevent NPE
        setStateFlowValue(viewModel, "_isLoggedIn", false)

        // Mock repository error response
        val exception = Exception("Invalid code")
        `when`(mockAuthRepository.verifyOtpCode(eq(testVerificationId), eq(testOtp)))
            .thenReturn(Result.failure(exception))

        // When
        viewModel.verifyOtpCode()

        // Process pending tasks
        testDispatcher.scheduler.advanceUntilIdle()
        ShadowLooper.idleMainLooper()

        // Then - Use null-safe assertions
        assertEquals("Invalid code", viewModel.error.value)
        assertTrue(viewModel.loginState.value is LoginState.Error)
        assertFalse(viewModel.isLoggedIn.value)
    }

    @Test
    fun `resetToPhoneEntry cleans up verification state and resets all values`() = runTest {
        // Given - Set up initial state as if OTP was sent
        setStateFlowValue(viewModel, "_otp", "12345")
        setStateFlowValue(viewModel, "_isOtpSent", true)
        setStateFlowValue(viewModel, "_verificationId", testVerificationId)
        setStateFlowValue(viewModel, "_error", "Some error")
        setStateFlowValue(viewModel, "_loginState", LoginState.Error("Test error"))

        // Mock the repository to return success for cleanup
        `when`(mockAuthRepository.cancelOngoingVerification()).thenReturn(Unit)

        // When
        viewModel.resetToPhoneEntry()

        // Process pending coroutines
        testDispatcher.scheduler.advanceUntilIdle()
        ShadowLooper.idleMainLooper()

        // Then - Verify repository cleanup was called
        verify(mockAuthRepository).cancelOngoingVerification()

        // And verify all state values are reset
        assertEquals("", viewModel.otp.value)
        assertFalse(viewModel.isOtpSent.value)
        assertEquals(null, getPrivateFieldValue(viewModel, "_verificationId"))
        assertEquals(null, viewModel.error.value)
        assertTrue(viewModel.loginState.value is LoginState.Idle)
    }

    @Test
    fun `resetToPhoneEntry handles cleanup error gracefully`() = runTest {
        // Given - Set up initial state
        setStateFlowValue(viewModel, "_otp", "12345")
        setStateFlowValue(viewModel, "_isOtpSent", true)
        setStateFlowValue(viewModel, "_verificationId", testVerificationId)

        // Mock the repository to throw an exception during cleanup
        `when`(mockAuthRepository.cancelOngoingVerification()).thenAnswer {
            throw Exception("Cleanup failed")
        }

        // When
        viewModel.resetToPhoneEntry()

        // Process pending coroutines
        testDispatcher.scheduler.advanceUntilIdle()
        ShadowLooper.idleMainLooper()

        // Then - Verify cleanup was attempted
        verify(mockAuthRepository).cancelOngoingVerification()

        // And verify state is still reset despite the error
        assertEquals("", viewModel.otp.value)
        assertFalse(viewModel.isOtpSent.value)
        assertEquals(null, getPrivateFieldValue(viewModel, "_verificationId"))
        assertEquals(null, viewModel.error.value)
        assertTrue(viewModel.loginState.value is LoginState.Idle)
    }

    // Add the rest of the test methods from your original code

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
            val superClass = clazz.superclass
            if (superClass != null) findField(superClass, fieldName)
            else throw e
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getPrivateFieldValue(instance: Any, fieldName: String): T? {
        val field = findField(instance.javaClass, fieldName)
        field.isAccessible = true
        val stateFlow = field.get(instance) as MutableStateFlow<T?>
        return stateFlow.value
    }
}