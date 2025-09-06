package com.tecvo.taxi.services

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

/**
 * Centralized error handling service that standardizes logging, user feedback,
 * retry mechanisms, and crash reporting integration.
 */
@Singleton
open class ErrorHandlingService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val crashReportingManager: CrashReportingManager
) {
    // Constants for error handling
    private val errorTagConstant = "TaxiErrorHandler"
    private val maxRetryAttempts = 3
    private val baseRetryDelayMs = 500L // 500ms base delay for retries

    // Coroutine scope for handling async operations
    @Suppress("unused")
    private val errorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Standard error types with enhanced crash reporting capabilities
    sealed class AppError {
        data class NetworkError(
            val throwable: Throwable,
            val message: String? = null,
            val isConnectionError: Boolean = true
        ) : AppError()

        data class AuthError(
            val throwable: Throwable? = null,
            val message: String? = null,
            val authErrorCode: String? = null
        ) : AppError()

        data class DatabaseError(
            val throwable: Throwable,
            val message: String? = null,
            val path: String? = null
        ) : AppError()

        data class LocationError(
            val throwable: Throwable? = null,
            val message: String? = null,
            val errorCode: Int? = null
        ) : AppError()

        data class PermissionError(
            val permission: String,
            val message: String? = null,
            val isPermanentlyDenied: Boolean = false
        ) : AppError()

        data class ValidationError(
            val field: String? = null,
            val message: String
        ) : AppError()

        data class UserFeedbackError(
            val message: String,
            val isRecoverable: Boolean = true,
            val actionLabel: String? = null
        ) : AppError()

        data class UnexpectedError(
            val throwable: Throwable,
            val where: String? = null,
            val errorCode: String? = null
        ) : AppError()
    }

    /**
     * Maps a throwable to a typed AppError for standardized error handling.
     * Enhanced with phone authentication specific error mapping.
     */
    open fun mapThrowableToAppError(throwable: Throwable, tag: String): AppError {
        return when (throwable) {
            // Network errors
            is UnknownHostException,
            is ConnectException,
            is SocketTimeoutException -> {
                AppError.NetworkError(throwable, "Network connectivity issue")
            }
            // HTTP errors
            is retrofit2.HttpException -> {
                when (throwable.code()) {
                    in 400..499 -> {
                        if (throwable.code() == 401 || throwable.code() == 403) {
                            AppError.AuthError(throwable, "Authentication failed (${throwable.code()})")
                        } else {
                            AppError.ValidationError(message = "Request error: ${throwable.message()}")
                        }
                    }
                    in 500..599 -> {
                        AppError.NetworkError(throwable, "Server error (${throwable.code()})")
                    }
                    else -> {
                        AppError.UnexpectedError(throwable, tag)
                    }
                }
            }
            // Firebase auth errors - Enhanced for phone authentication
            is com.google.firebase.auth.FirebaseAuthException -> {
                val userMessage = when (throwable.errorCode) {
                    "ERROR_INVALID_PHONE_NUMBER" -> "The phone number format is incorrect"
                    "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Please try again later"
                    "ERROR_SESSION_EXPIRED" -> "Verification session expired. Please try again"
                    "ERROR_QUOTA_EXCEEDED" -> "SMS quota exceeded. Please try again tomorrow"
                    "ERROR_INVALID_VERIFICATION_CODE" -> "Invalid verification code. Please check and try again"
                    "ERROR_CAPTCHA_CHECK_FAILED" -> "Verification check failed. Please try again"
                    else -> "Authentication error: ${throwable.message}"
                }
                AppError.AuthError(throwable, userMessage, throwable.errorCode)
            }
            is com.google.firebase.database.DatabaseException -> {
                AppError.DatabaseError(throwable, "Database error: ${throwable.message}")
            }
            // General Firebase errors
            is com.google.firebase.FirebaseException -> {
                if (throwable.message?.contains("permission", ignoreCase = true) == true ||
                    throwable.message?.contains("auth", ignoreCase = true) == true
                ) {
                    AppError.AuthError(throwable, throwable.message)
                } else {
                    AppError.DatabaseError(throwable, throwable.message)
                }
            }
            // Security exceptions (often for location)
            is SecurityException -> {
                AppError.PermissionError(
                    "LOCATION",
                    throwable.message ?: "Permission denied"
                )
            }
            // Generic exception handling
            else -> {
                when {
                    throwable.message?.contains("location", ignoreCase = true) == true -> {
                        AppError.LocationError(throwable, throwable.message)
                    }
                    throwable.message?.contains("permission", ignoreCase = true) == true -> {
                        AppError.PermissionError(
                            "UNKNOWN",
                            throwable.message ?: "Permission denied"
                        )
                    }
                    else -> {
                        AppError.UnexpectedError(throwable, tag)
                    }
                }
            }
        }
    }

    /**
     * Logs an error with standardized format and crash reporting integration
     */
    open fun logError(error: AppError, tag: String = errorTagConstant) {
        // Local logging
        when (error) {
            is AppError.NetworkError -> {
                Timber.tag(tag)
                    .e(error.throwable, "Network Error: ${error.message ?: "No message"}")
            }
            is AppError.AuthError -> {
                Timber.tag(tag)
                    .e(error.throwable, "Authentication Error: ${error.message ?: "No message"}")
            }
            is AppError.DatabaseError -> {
                Timber.tag(tag)
                    .e(error.throwable, "Database Error: ${error.message ?: "No message"}")
            }
            is AppError.LocationError -> {
                Timber.tag(tag)
                    .e(error.throwable, "Location Error: ${error.message ?: "No message"}")
            }
            is AppError.PermissionError -> {
                Timber.tag(tag)
                    .e("Permission Error for ${error.permission}: ${error.message ?: "Permission denied"}")
            }
            is AppError.ValidationError -> {
                val fieldInfo = if (error.field != null) " for field ${error.field}" else ""
                Timber.tag(tag).e("Validation Error${fieldInfo}: ${error.message}")
            }
            is AppError.UserFeedbackError -> {
                Timber.tag(tag).i("User Feedback: ${error.message}")
            }
            is AppError.UnexpectedError -> {
                val whereInfo = if (error.where != null) " in ${error.where}" else ""
                Timber.tag(tag).e(error.throwable, "Unexpected Error${whereInfo}")
            }
        }

        // Crash reporting integration
        try {
            when (error) {
                is AppError.NetworkError -> {
                    crashReportingManager.logNonFatalError(
                        tag,
                        "Network Error: ${error.message}",
                        error.throwable
                    )
                }
                is AppError.AuthError -> {
                    crashReportingManager.logNonFatalError(
                        tag,
                        "Authentication Error: ${error.message}",
                        error.throwable
                    )
                }
                is AppError.DatabaseError -> {
                    crashReportingManager.logNonFatalError(
                        tag,
                        "Database Error: ${error.message}",
                        error.throwable
                    )
                }
                is AppError.LocationError -> {
                    crashReportingManager.logNonFatalError(
                        tag,
                        "Location Error: ${error.message}",
                        error.throwable
                    )
                }
                is AppError.PermissionError -> {
                    crashReportingManager.logNonFatalError(
                        tag,
                        "Permission Error: ${error.message}"
                    )
                }
                is AppError.ValidationError -> {
                    crashReportingManager.logNonFatalError(
                        tag,
                        "Validation Error: ${error.message}"
                    )
                }
                is AppError.UnexpectedError -> {
                    crashReportingManager.logException(
                        error.throwable,
                        "Unexpected Error in ${error.where}"
                    )
                }
                is AppError.UserFeedbackError -> {
                    crashReportingManager.logBreadcrumb("User Feedback: ${error.message}")
                }
            }
        } catch (e: Exception) {
            Timber.tag("ErrorHandlingService").e("Error logging to crash reporting: ${e.message}")
        }
    }

    /**
     * Shows a toast message for user feedback
     */
    open fun showToast(message: String, isLongDuration: Boolean = false) {
        val duration = if (isLongDuration) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        Toast.makeText(context, message, duration).show()
        // Log the user-facing message
        crashReportingManager.logBreadcrumb("Toast: $message")
    }

    /**
     * Helper method to determine if an error is a network connectivity issue
     */
    open fun isNetworkError(throwable: Throwable): Boolean {
        return throwable is ConnectException ||
                throwable is SocketTimeoutException ||
                throwable is UnknownHostException ||
                throwable.message?.contains("Failed to connect", ignoreCase = true) == true ||
                throwable.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                throwable.message?.contains("Network", ignoreCase = true) == true
    }

    /**
     * Wrapper for Firebase operations with standardized error handling and retry mechanism
     */
    open suspend fun <T> executeFirebaseOperation(
        tag: String,
        operation: suspend () -> T,
        onError: ((AppError) -> Unit)? = null,
        maxRetries: Int = maxRetryAttempts
    ): Result<T> = withContext(Dispatchers.IO) {
        var lastException: Exception? = null
        var currentRetry = 0

        while (currentRetry < maxRetries) {
            try {
                // Exponential backoff for retries
                if (currentRetry > 0) {
                    val delayTime = baseRetryDelayMs * (2.0.pow(currentRetry - 1)).toLong()
                    Timber.tag(tag)
                        .d("Retrying operation (attempt ${currentRetry + 1}/$maxRetries) after $delayTime ms")
                    delay(delayTime)
                }

                // Perform the operation
                val result = operation()
                return@withContext Result.success(result)
            } catch (e: Exception) {
                lastException = e
                val error = when {
                    e.message?.contains("permission_denied", ignoreCase = true) == true ->
                        AppError.AuthError(e, "Firebase operation not authorized")
                    isNetworkError(e) ->
                        AppError.NetworkError(e, "Network error during Firebase operation")
                    else ->
                        AppError.DatabaseError(e, "Error during Firebase operation")
                }

                // Log the error
                logError(error, tag)

                // Call custom error handler if provided
                onError?.invoke(error)

                // Only retry network errors automatically
                if (error !is AppError.NetworkError) {
                    break
                }
                currentRetry++
            }
        }

        // If we've exhausted all retries, return failure
        val finalError = lastException ?: Exception("Unknown error in Firebase operation")
        return@withContext Result.failure(finalError)
    }

    /**
     * Show a snackbar error message in Compose UI
     */
    open suspend fun showSnackbarError(
        snackbarHostState: SnackbarHostState,
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        // Log the snackbar message as a breadcrumb
        crashReportingManager.logBreadcrumb("Snackbar: $message")
        snackbarHostState.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = duration
        )
    }

    /**
     * Get a user-friendly message from an AppError
     */
    open fun getUserFriendlyMessage(error: AppError): String {
        return when (error) {
            is AppError.NetworkError ->
                if (error.isConnectionError)
                    "Network issue. Please check your connection and try again."
                else
                    "Could not access server. Please try again later."
            is AppError.AuthError ->
                error.message ?: "Authentication error. Please try again."
            is AppError.DatabaseError ->
                "Could not access data. Please try again later."
            is AppError.LocationError ->
                error.message ?: "Could not determine your location. Please check your settings."
            is AppError.PermissionError ->
                "Permission required: ${error.message ?: error.permission}"
            is AppError.ValidationError ->
                error.message
            is AppError.UserFeedbackError ->
                error.message
            is AppError.UnexpectedError ->
                "Something went wrong. Please try again."
        }
    }
}

/**
 * Composable helper that provides a standardized way to handle errors in Compose UI
 */
@Composable
@Suppress("unused")
fun rememberErrorHandler(
    snackbarHostState: SnackbarHostState? = null
): ErrorHandler {
    val coroutineScope = rememberCoroutineScope()
    val service = dagger.hilt.android.EntryPointAccessors.fromApplication(
        LocalContext.current,
        ErrorHandlingEntryPoint::class.java
    ).errorHandlingService()

    return remember(service, coroutineScope, snackbarHostState) {
        ErrorHandler(service, coroutineScope, snackbarHostState)
    }
}

/**
 * Entry point for accessing ErrorHandlingService from Compose UI components
 */
@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface ErrorHandlingEntryPoint {
    fun errorHandlingService(): ErrorHandlingService
}

/**
 * Helper class for handling errors in Compose UI
 */
class ErrorHandler(
    private val service: ErrorHandlingService,
    private val coroutineScope: CoroutineScope,
    private val snackbarHostState: SnackbarHostState?
) {
    @Suppress("unused")
    fun handleError(
        error: ErrorHandlingService.AppError,
        tag: String = "ComposeUI",
        showToast: Boolean = true,
        onErrorHandled: ((ErrorHandlingService.AppError) -> Unit)? = null
    ) {
        coroutineScope.launch {
            // Log the error
            service.logError(error, tag)

            // Get user-friendly message
            val userMessage = service.getUserFriendlyMessage(error)

            // Show toast if needed
            if (showToast) {
                service.showToast(userMessage)
            }

            // Show snackbar if provided
            snackbarHostState?.let {
                service.showSnackbarError(it, userMessage)
            }

            // Call custom handler if provided
            onErrorHandled?.invoke(error)
        }
    }
}