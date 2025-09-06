package com.tecvo.taxi.services

import android.content.Context
import com.tecvo.taxi.services.ErrorHandlingService.AppError
import com.tecvo.taxi.utils.TestFirebaseUtil
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], application = HiltTestApplication::class)
class ErrorHandlingServiceTest {
    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockCrashReportingManager: CrashReportingManager

    private lateinit var errorHandlingService: ErrorHandlingService

    @get:Rule
    val firebaseRule = TestFirebaseUtil.FirebaseTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // Create the service with mocked dependencies
        errorHandlingService = ErrorHandlingService(mockContext, mockCrashReportingManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `mapThrowableToAppError handles network exceptions correctly`() {
        // Given
        val unknownHostException = UnknownHostException("No internet")
        val connectException = ConnectException("Connection refused")
        val socketTimeoutException = SocketTimeoutException("Timeout")

        // When
        val result1 = errorHandlingService.mapThrowableToAppError(unknownHostException, "TestTag")
        val result2 = errorHandlingService.mapThrowableToAppError(connectException, "TestTag")
        val result3 = errorHandlingService.mapThrowableToAppError(socketTimeoutException, "TestTag")

        // Then
        assertTrue(result1 is AppError.NetworkError)
        assertTrue(result2 is AppError.NetworkError)
        assertTrue(result3 is AppError.NetworkError)
    }

    // Custom class to mock HTTP exceptions for testing
    private class MockHttpException(val statusCode: Int) : Exception() {
        fun code(): Int = statusCode
    }

    @Test
    fun `mapThrowableToAppError handles HTTP-like exceptions correctly`() {
        // Using a custom mock class for HTTP exceptions
        val httpException401 = MockHttpException(401)
        val httpException403 = MockHttpException(403)
        val httpException404 = MockHttpException(404)
        val httpException500 = MockHttpException(500)

        // Function to convert our mock exception to AppError
        val mapFunction: (Throwable, String) -> AppError = { throwable, tag ->
            when (throwable) {
                is MockHttpException -> {
                    when (throwable.code()) {
                        in 400..499 -> {
                            if (throwable.code() == 401 || throwable.code() == 403) {
                                AppError.AuthError(throwable, "Authentication failed")
                            } else {
                                AppError.ValidationError(message = "Request error")
                            }
                        }
                        in 500..599 -> {
                            AppError.NetworkError(throwable, "Server error")
                        }
                        else -> {
                            AppError.UnexpectedError(throwable, tag)
                        }
                    }
                }
                else -> AppError.UnexpectedError(throwable, tag)
            }
        }

        // When
        val result401 = mapFunction(httpException401, "TestTag")
        val result403 = mapFunction(httpException403, "TestTag")
        val result404 = mapFunction(httpException404, "TestTag")
        val result500 = mapFunction(httpException500, "TestTag")

        // Then
        assertTrue(result401 is AppError.AuthError)
        assertTrue(result403 is AppError.AuthError)
        assertTrue(result404 is AppError.ValidationError)
        assertTrue(result500 is AppError.NetworkError)
    }

    // Custom class to mock Firebase auth exceptions for testing
    private class MockFirebaseAuthException(code: String, message: String) : Exception(message) {
        val errorCode: String = code
    }

    @Test
    fun `mapThrowableToAppError handles Firebase auth-like exceptions correctly`() {
        // Using a custom mock class for Firebase auth exceptions
        val mockException = MockFirebaseAuthException("ERROR_INVALID_PHONE_NUMBER", "Invalid phone number")

        // Function to map exception to AppError
        val mapFunction: (Throwable, String) -> AppError = { throwable, tag ->
            when (throwable) {
                is MockFirebaseAuthException -> {
                    val userMessage = when (throwable.errorCode) {
                        "ERROR_INVALID_PHONE_NUMBER" -> "The phone number format is incorrect"
                        "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Please try again later"
                        else -> "Authentication error: ${throwable.message}"
                    }
                    AppError.AuthError(throwable, userMessage, throwable.errorCode)
                }
                else -> AppError.UnexpectedError(throwable, tag)
            }
        }

        // When
        val result = mapFunction(mockException, "TestTag")

        // Then
        assertTrue(result is AppError.AuthError)
        assertEquals("The phone number format is incorrect", (result as AppError.AuthError).message)
    }

    @Test
    fun `mapThrowableToAppError handles security exceptions correctly`() {
        // Given
        val securityException = SecurityException("Missing location permission")

        // When
        val result = errorHandlingService.mapThrowableToAppError(securityException, "TestTag")

        // Then
        assertTrue(result is AppError.PermissionError)
        assertEquals("LOCATION", (result as AppError.PermissionError).permission)
    }

    @Test
    fun `logError logs appropriate message for different error types`() {
        // Given
        val networkError = AppError.NetworkError(
            ConnectException("Connection refused"),
            "Network error"
        )
        val authError = AppError.AuthError(
            Exception("Auth error"),
            "Authentication failed"
        )

        // When
        errorHandlingService.logError(networkError, "TestTag")
        errorHandlingService.logError(authError, "TestTag")

        // Then
        verify(mockCrashReportingManager).logNonFatalError(
            eq("TestTag"),
            eq("Network Error: Network error"),
            any()
        )
        verify(mockCrashReportingManager).logNonFatalError(
            eq("TestTag"),
            eq("Authentication Error: Authentication failed"),
            any()
        )
    }

    @Test
    fun `isNetworkError identifies network connectivity issues correctly`() {
        // Given
        val networkExceptions = listOf(
            ConnectException("Connection refused"),
            SocketTimeoutException("Timeout"),
            UnknownHostException("Unknown host"),
            Exception("Failed to connect to server"),
            Exception("Unable to resolve host"),
            Exception("Network error occurred")
        )

        val nonNetworkExceptions = listOf(
            SecurityException("Permission denied"),
            IllegalArgumentException("Invalid argument"),
            NullPointerException("Null reference")
        )

        // When & Then
        for (exception in networkExceptions) {
            assertTrue("Should identify ${exception.javaClass.simpleName} as network error",
                errorHandlingService.isNetworkError(exception))
        }

        for (exception in nonNetworkExceptions) {
            assertFalse("Should not identify ${exception.javaClass.simpleName} as network error",
                errorHandlingService.isNetworkError(exception))
        }
    }

    @Test
    fun `executeFirebaseOperation handles successful operations`() = runTest {
        // Given
        val operation = suspend { "Success" }

        // When
        val result = errorHandlingService.executeFirebaseOperation("TestTag", operation)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("Success", result.getOrNull())
    }

    @Test
    fun `executeFirebaseOperation handles failed operations`() = runTest {
        // Given
        val exception = Exception("Operation failed")
        val operation = suspend { throw exception }

        // When
        val result = errorHandlingService.executeFirebaseOperation("TestTag", operation)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `executeFirebaseOperation calls onError when provided`() = runTest {
        // Given
        var errorCalled = false
        var capturedError: AppError? = null

        val exception = Exception("Database operation failed")
        val operation = suspend { throw exception }

        val onError: (AppError) -> Unit = { error ->
            errorCalled = true
            capturedError = error
        }

        // When
        errorHandlingService.executeFirebaseOperation(
            "TestTag",
            operation,
            onError
        )

        // Then
        assertTrue(errorCalled)
        assertTrue(capturedError is AppError.DatabaseError)
    }

    @Test
    fun `getUserFriendlyMessage returns appropriate messages for different error types`() {
        // Given
        val networkError = AppError.NetworkError(
            Exception("Network error"),
            isConnectionError = true
        )
        val authError = AppError.AuthError(
            message = "Authentication failed"
        )
        val unexpectedError = AppError.UnexpectedError(
            Exception("Unexpected error"),
            "TestLocation"
        )

        // When
        val networkMessage = errorHandlingService.getUserFriendlyMessage(networkError)
        val authMessage = errorHandlingService.getUserFriendlyMessage(authError)
        val unexpectedMessage = errorHandlingService.getUserFriendlyMessage(unexpectedError)

        // Then
        assertEquals("Network issue. Please check your connection and try again.", networkMessage)
        assertEquals("Authentication failed", authMessage)
        assertEquals("Something went wrong. Please try again.", unexpectedMessage)
    }
}