package com.tecvo.taxi.utils

import android.database.Cursor
import com.tecvo.taxi.services.ErrorHandlingService.AppError
import timber.log.Timber
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.database.DatabaseError
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.math.pow

/**
 * Maps a throwable to a typed AppError for standardized error handling
 */
fun mapThrowableToAppError(throwable: Throwable, tag: String): AppError {
    // Handle network errors
    if (throwable is UnknownHostException || throwable is ConnectException || throwable is SocketTimeoutException) {
        return AppError.NetworkError(throwable, "Network connectivity issue")
    }

    // Handle HTTP errors
    if (throwable is HttpException) {
        val code = throwable.code()
        return when (code) {
            401, 403 -> AppError.AuthError(throwable, "Authentication failed ($code)")
            in 400..499 -> AppError.ValidationError(message = "Request error: ${throwable.message()}")
            in 500..599 -> AppError.NetworkError(throwable, "Server error ($code)")
            else -> AppError.UnexpectedError(throwable, tag)
        }
    }

    // Handle Firebase auth errors
    if (throwable is FirebaseAuthException) {
        return AppError.AuthError(throwable, "Authentication error: ${throwable.message}")
    }

    // Handle Firebase database errors
    if (throwable is DatabaseError) {
        return AppError.DatabaseError(throwable, "Database error: ${throwable.message}")
    }

    // Handle general Firebase errors
    if (throwable is FirebaseException) {
        return if (throwable.message?.contains("permission", ignoreCase = true) == true ||
            throwable.message?.contains("auth", ignoreCase = true) == true) {
            AppError.AuthError(throwable, throwable.message)
        } else {
            AppError.DatabaseError(throwable, throwable.message)
        }
    }

    // Handle security exceptions
    if (throwable is SecurityException) {
        return AppError.PermissionError("LOCATION", throwable.message ?: "Permission denied")
    }

    // Handle all other exceptions
    return when {
        throwable.message?.contains("location", ignoreCase = true) == true ->
            AppError.LocationError(throwable, throwable.message)
        throwable.message?.contains("permission", ignoreCase = true) == true ->
            AppError.PermissionError("UNKNOWN", throwable.message ?: "Permission denied")
        else ->
            AppError.UnexpectedError(throwable, tag)
    }
}

/**
 * Safe cursor extension to ensure proper resource cleanup
 */
inline fun <T> Cursor?.use(block: (Cursor) -> T): T? {
    return try {
        this?.let { cursor ->
            block(cursor).also {
                cursor.close()
            }
        }
    } catch (e: Exception) {
        Timber.w(e, "Error using cursor, ensuring cleanup")
        this?.close()
        null
    }
}

/**
 * Safe resource cleanup extension
 */
inline fun <T : AutoCloseable?, R> T.useSafely(block: (T) -> R): R? {
    return try {
        use(block)
    } catch (e: Exception) {
        Timber.w(e, "Error in resource usage, ensuring cleanup")
        null
    }
}

/**
 * Execute operation with retry mechanism and exponential backoff
 */
suspend fun <T> executeWithRetry(
    operation: suspend () -> T,
    maxRetries: Int = 3,
    initialDelayMillis: Long = 500L,
    shouldRetryPredicate: (Throwable) -> Boolean = { true }
): Result<T> {
    var lastException: Throwable? = null
    
    repeat(maxRetries) { attempt ->
        try {
            return Result.success(operation())
        } catch (e: Throwable) {
            lastException = e
            
            // Check if we should retry this exception
            if (!shouldRetryPredicate(e) || attempt == maxRetries - 1) {
                return@repeat
            }
            
            // Exponential backoff with jitter
            val delayMs = (initialDelayMillis * (2.0.pow(attempt))).toLong()
            delay(delayMs)
        }
    }
    
    return Result.failure(lastException ?: Exception("Unknown error in executeWithRetry"))
}

/**
 * Safe coroutine launch with error handling
 */
fun CoroutineScope.launchSafely(
    tag: String = "CoroutineScope",
    onError: (Throwable) -> Unit = { e -> 
        Timber.tag(tag).e(e, "Error in coroutine: ${e.message}")
    },
    block: suspend CoroutineScope.() -> Unit
) {
    launch {
        try {
            block()
        } catch (e: Throwable) {
            onError(e)
        }
    }
}