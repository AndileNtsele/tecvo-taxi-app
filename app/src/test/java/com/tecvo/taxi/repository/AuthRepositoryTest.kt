package com.tecvo.taxi.repository

import androidx.test.core.app.ApplicationProvider
import com.tecvo.taxi.services.ErrorHandlingService
import com.tecvo.taxi.utils.TestFirebaseUtil
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.tecvo.taxi.repository.AuthRepository

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], application = HiltTestApplication::class)
class AuthRepositoryTest {
    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var mockAuth: FirebaseAuth

    @Mock
    private lateinit var mockErrorHandlingService: ErrorHandlingService

    @Mock
    private lateinit var mockUser: FirebaseUser

    @Mock
    private lateinit var mockCredential: PhoneAuthCredential

    @Mock
    private lateinit var mockAuthTask: Task<Void>

    private lateinit var authRepository: AuthRepository

    // Constants for testing
    private val testUserId = "test-user-id"
    private val testVerificationId = "test-verification-id"
    private val testOtpCode = "123456"

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        // Initialize dispatcher properly
        Dispatchers.setMain(testDispatcher)

        // Initialize Firebase for testing
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        TestFirebaseUtil.initializeTestFirebase(context)

        // Set up user ID for tests that need it
        `when`(mockUser.uid).thenReturn(testUserId)

        // Create the repository with mocked dependencies
        authRepository = AuthRepository(mockAuth, mockErrorHandlingService)
    }

    @After
    fun tearDown() {
        try {
            testDispatcher.scheduler.advanceUntilIdle() // Process any pending coroutines
            Dispatchers.resetMain()
        } catch (e: Exception) {
            // Log but don't fail the test during cleanup
            println("Error during test cleanup: ${e.message}")
        }
    }

    @Test
    fun `isUserLoggedIn returns true when user is logged in`() = runTest(testDispatcher) {
        // Given
        `when`(mockAuth.currentUser).thenReturn(mockUser)

        // When
        val result = authRepository.isUserLoggedIn()

        // Then
        assertTrue(result)
    }

    @Test
    fun `isUserLoggedIn returns false when user is not logged in`() = runTest(testDispatcher) {
        // Given
        `when`(mockAuth.currentUser).thenReturn(null)

        // When
        val result = authRepository.isUserLoggedIn()

        // Then
        assertFalse(result)
    }

    @Test
    fun `getCurrentUserId returns user ID when logged in`() {
        // Given
        `when`(mockAuth.currentUser).thenReturn(mockUser)

        // When
        val result = authRepository.getCurrentUserId()

        // Then
        assertEquals(testUserId, result)
    }

    @Test
    fun `getCurrentUserId returns null when not logged in`() {
        // Given
        `when`(mockAuth.currentUser).thenReturn(null)

        // When
        val result = authRepository.getCurrentUserId()

        // Then
        assertNull(result)
    }

    @Test
    fun `signOut calls Firebase auth signOut`() = runTest(testDispatcher) {
        // When
        val result = authRepository.signOut()

        // Then
        verify(mockAuth).signOut()
        assertEquals(1, result) // Check success code
    }

    @Test
    fun `verifyOtpCode uses ErrorHandlingService to handle Firebase operation`() = runTest(testDispatcher) {
        // Setup
        whenever(mockErrorHandlingService.executeFirebaseOperation<Unit>(
            eq("AuthRepository"),
            any(),
            any(),
            anyOrNull()
        )).thenReturn(Result.success(Unit))

        // When
        val result = authRepository.verifyOtpCode(testVerificationId, testOtpCode)

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `verifyOtpCode handles error case`() = runTest(testDispatcher) {
        // Setup - error case
        val testException = Exception("Auth failed")
        whenever(mockErrorHandlingService.executeFirebaseOperation<Unit>(
            eq("AuthRepository"),
            any(),
            any(),
            anyOrNull()
        )).thenReturn(Result.failure(testException))

        // When
        val result = authRepository.verifyOtpCode(testVerificationId, testOtpCode)

        // Then
        assertTrue(result.isFailure)
        assertEquals(testException, result.exceptionOrNull())
    }

    @Test
    fun `signInWithCredential uses ErrorHandlingService to handle Firebase operation`() = runTest(testDispatcher) {
        // Setup
        whenever(mockErrorHandlingService.executeFirebaseOperation<Unit>(
            eq("AuthRepository"),
            any(),
            any(),
            anyOrNull()
        )).thenReturn(Result.success(Unit))

        // When
        val result = authRepository.signInWithCredential(mockCredential)

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `signInWithCredential handles error case`() = runTest(testDispatcher) {
        // Setup - error case
        val testException = Exception("Auth failed")
        whenever(mockErrorHandlingService.executeFirebaseOperation<Unit>(
            eq("AuthRepository"),
            any(),
            any(),
            anyOrNull()
        )).thenReturn(Result.failure(testException))

        // When
        val result = authRepository.signInWithCredential(mockCredential)

        // Then
        assertTrue(result.isFailure)
        assertEquals(testException, result.exceptionOrNull())
    }

    @Test
    fun `deleteUserAccount uses ErrorHandlingService to handle Firebase operation`() = runTest(testDispatcher) {
        // Setup
        `when`(mockAuth.currentUser).thenReturn(mockUser)
        whenever(mockUser.delete()).thenReturn(mockAuthTask)
        whenever(mockErrorHandlingService.executeFirebaseOperation<Unit>(
            eq("AuthRepository"),
            any(),
            any(),
            anyOrNull()
        )).thenReturn(Result.success(Unit))

        // When
        val result = authRepository.deleteUserAccount()

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `deleteUserAccount handles error case`() = runTest(testDispatcher) {
        // Setup - error case
        `when`(mockAuth.currentUser).thenReturn(mockUser)
        val testException = Exception("Delete failed")
        whenever(mockErrorHandlingService.executeFirebaseOperation<Unit>(
            eq("AuthRepository"),
            any(),
            any(),
            anyOrNull()
        )).thenReturn(Result.failure(testException))

        // When
        val result = authRepository.deleteUserAccount()

        // Then
        assertTrue(result.isFailure)
        assertEquals(testException, result.exceptionOrNull())
    }
}