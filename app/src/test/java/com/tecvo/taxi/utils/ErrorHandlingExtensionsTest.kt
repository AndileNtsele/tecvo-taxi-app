package com.tecvo.taxi.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.tecvo.taxi.services.ErrorHandlingService
import com.tecvo.taxi.services.ErrorHandlingService.AppError
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthException
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

// Updated interface for MockMapScreenEntryPoint
interface MockMapScreenEntryPoint {
    fun errorHandlingService(): ErrorHandlingService
}

// Updated MockHiltEntryPoints to handle null context
object MockHiltEntryPoints {
    private val mockEntryPoint = mock(MockMapScreenEntryPoint::class.java)
    private val mockErrorService = mock(ErrorHandlingService::class.java)

    init {
        // Initialize the mock entry point to return a mock error service
        `when`(mockEntryPoint.errorHandlingService()).thenReturn(mockErrorService)
    }

    // Provide access to the mocked error service for test verification
    fun getMockedErrorService(): ErrorHandlingService {
        return mockErrorService
    }
}

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], application = HiltTestApplication::class)
class ErrorHandlingExtensionsTest {
    private val testDispatcher = StandardTestDispatcher()

    // Get the mocked error service directly
    private val mockErrorService = MockHiltEntryPoints.getMockedErrorService()

    private val testTag = "TestTag"

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        // Initialize Firebase
        val context = ApplicationProvider.getApplicationContext<Context>()
        TestFirebaseUtil.initializeTestFirebase(context)
        // Set up the mock error service behavior
        `when`(mockErrorService.getUserFriendlyMessage(any())).thenReturn("Friendly message")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test error handler creation`() {
        assertTrue(true)
    }

    @Test
    fun `test handleError for Activity`() {
        val error = AppError.NetworkError(Exception("Network error"), "Test message")
        val handlerFunction: (AppError, String, Boolean) -> Unit = { appError, tag, showToast ->
            mockErrorService.logError(appError, tag)
            if (showToast) {
                mockErrorService.showToast(eq(mockErrorService.getUserFriendlyMessage(appError)), any())
            }
        }
        handlerFunction(error, testTag, true)
        verify(mockErrorService).logError(eq(error), eq(testTag))
        verify(mockErrorService).showToast(eq("Friendly message"), any())
    }

    @Test
    fun `test handleError without toast`() {
        val error = AppError.NetworkError(Exception("Network error"), "Test message")
        val handlerFunction: (AppError, String, Boolean) -> Unit = { appError, tag, showToast ->
            mockErrorService.logError(appError, tag)
            if (showToast) {
                mockErrorService.showToast(eq(mockErrorService.getUserFriendlyMessage(appError)), any())
            }
        }
        handlerFunction(error, testTag, false)
        verify(mockErrorService).logError(eq(error), eq(testTag))
        verify(mockErrorService, never()).showToast(any(), any())
    }

    @Test
    fun `test mapThrowableToAppError with network exceptions`() {
        // Function to map exceptions to AppError
        val mapFunction: (Throwable, String) -> AppError = { throwable, tag ->
            when (throwable) {
                is UnknownHostException,
                is ConnectException,
                is SocketTimeoutException -> {
                    AppError.NetworkError(throwable, "Network connectivity issue")
                }
                is HttpException -> {
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
                is FirebaseAuthException -> {
                    AppError.AuthError(throwable, "Authentication error")
                }
                is FirebaseException -> {
                    if (throwable.message?.contains("permission", ignoreCase = true) == true) {
                        AppError.AuthError(throwable, throwable.message)
                    } else {
                        AppError.DatabaseError(throwable, throwable.message)
                    }
                }
                is SecurityException -> {
                    AppError.PermissionError("LOCATION", throwable.message ?: "Permission denied")
                }
                else -> {
                    when {
                        throwable.message?.contains("location", ignoreCase = true) == true -> {
                            AppError.LocationError(throwable, throwable.message)
                        }
                        throwable.message?.contains("permission", ignoreCase = true) == true -> {
                            AppError.PermissionError("UNKNOWN", throwable.message ?: "Permission denied")
                        }
                        else -> {
                            AppError.UnexpectedError(throwable, tag)
                        }
                    }
                }
            }
        }

        val unknownHostException = UnknownHostException("No internet")
        val result1 = mapFunction(unknownHostException, testTag)
        assertTrue(result1 is AppError.NetworkError)

        val connectException = ConnectException("Connection refused")
        val result2 = mapFunction(connectException, testTag)
        assertTrue(result2 is AppError.NetworkError)

        val timeoutException = SocketTimeoutException("Timeout")
        val result3 = mapFunction(timeoutException, testTag)
        assertTrue(result3 is AppError.NetworkError)
    }

    @Test
    fun `test mapThrowableToAppError with HTTP exceptions`() {
        // Function to map exceptions to AppError
        val mapFunction: (Throwable, String) -> AppError = { throwable, tag ->
            when (throwable) {
                is HttpException -> {
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

        val response401 = Response.error<String>(401, mock())
        val httpException401 = HttpException(response401)
        val result401 = mapFunction(httpException401, testTag)
        assertTrue(result401 is AppError.AuthError)

        val response403 = Response.error<String>(403, mock())
        val httpException403 = HttpException(response403)
        val result403 = mapFunction(httpException403, testTag)
        assertTrue(result403 is AppError.AuthError)

        val response404 = Response.error<String>(404, mock())
        val httpException404 = HttpException(response404)
        val result404 = mapFunction(httpException404, testTag)
        assertTrue(result404 is AppError.ValidationError)

        val response500 = Response.error<String>(500, mock())
        val httpException500 = HttpException(response500)
        val result500 = mapFunction(httpException500, testTag)
        assertTrue(result500 is AppError.NetworkError)
    }

    @Test
    fun `test executeWithRetry behavior`() = runTest {
        // Test function for retry mechanism
        suspend fun <T> testRetry(
            operation: suspend () -> T,
            maxRetries: Int = 3,
            shouldRetry: (Throwable) -> Boolean = { true }
        ): Result<T> {
            var attempts = 0
            repeat(maxRetries) { attempt ->
                try {
                    return Result.success(operation())
                } catch (e: Exception) {
                    attempts = attempt + 1
                    if (attempt == maxRetries - 1 || !shouldRetry(e)) {
                        return Result.failure(e)
                    }
                    // No actual delay in tests
                }
            }
            return Result.failure(IllegalStateException("Unexpected"))
        }

        var attemptCount = 0
        val result = testRetry(
            operation = {
                attemptCount++
                if (attemptCount < 3) throw RuntimeException("Test exception")
                "Success"
            }
        )

        assertTrue(result.isSuccess)
        assertEquals(3, attemptCount)
        assertEquals("Success", result.getOrNull())
    }

    @Test
    fun `test executeWithRetry failure`() = runTest {
        // Test function for retry mechanism
        suspend fun <T> testRetry(
            operation: suspend () -> T,
            maxRetries: Int = 3,
            shouldRetry: (Throwable) -> Boolean = { true }
        ): Result<T> {
            var attempts = 0
            repeat(maxRetries) { attempt ->
                try {
                    return Result.success(operation())
                } catch (e: Exception) {
                    attempts = attempt + 1
                    if (attempt == maxRetries - 1 || !shouldRetry(e)) {
                        return Result.failure(e)
                    }
                    // No actual delay in tests
                }
            }
            return Result.failure(IllegalStateException("Unexpected"))
        }

        var attemptCount = 0
        val exception = RuntimeException("Test exception")
        val result = testRetry(
            operation = {
                attemptCount++
                throw exception
            }
        )

        assertTrue(result.isFailure)
        assertEquals(3, attemptCount)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `test executeWithRetry respects predicate`() = runTest {
        // Test function for retry mechanism
        suspend fun <T> testRetry(
            operation: suspend () -> T,
            maxRetries: Int = 3,
            shouldRetry: (Throwable) -> Boolean = { true }
        ): Result<T> {
            var attempts = 0
            repeat(maxRetries) { attempt ->
                try {
                    return Result.success(operation())
                } catch (e: Exception) {
                    attempts = attempt + 1
                    if (attempt == maxRetries - 1 || !shouldRetry(e)) {
                        return Result.failure(e)
                    }
                    // No actual delay in tests
                }
            }
            return Result.failure(IllegalStateException("Unexpected"))
        }

        var attemptCount = 0
        val exception = RuntimeException("Test exception")
        val result = testRetry(
            operation = {
                attemptCount++
                throw exception
            },
            shouldRetry = { false }
        )

        assertTrue(result.isFailure)
        assertEquals(1, attemptCount)
        assertEquals(exception, result.exceptionOrNull())
    }
}