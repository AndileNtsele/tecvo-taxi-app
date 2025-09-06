package com.example.taxi.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.pow

/**
 * Execute an operation with exponential backoff retry
 * @param operation The suspend function to execute
 * @param maxRetries Maximum number of retry attempts
 * @param initialDelayMillis Base delay for retry in milliseconds
 * @param shouldRetryPredicate Optional predicate to determine if retry should happen
 * @return Result containing either success value or exception
 */
suspend fun <T> executeWithRetry(
    operation: suspend () -> T,
    maxRetries: Int = 3,
    initialDelayMillis: Long = 500,
    shouldRetryPredicate: (Throwable) -> Boolean = { true }
): Result<T> {
    var currentDelay = initialDelayMillis
    repeat(maxRetries) { attempt ->
        try {
            return Result.success(operation())
        } catch (e: Exception) {
            // Check if we should retry
            if (!shouldRetryPredicate(e)) {
                return Result.failure(e)
            }

            // If this is the last attempt, don't delay just return failure
            if (attempt == maxRetries - 1) {
                return Result.failure(e)
            }

            // Wait with exponential backoff before next attempt
            kotlinx.coroutines.delay(currentDelay)
            currentDelay = (currentDelay * 2.0.pow(1)).toLong()
        }
    }
    return Result.failure(IllegalStateException("Should not reach here"))
}

/**
 * Extension to safely launch coroutines with error handling
 */
fun CoroutineScope.launchSafely(
    tag: String,
    dispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.Default,
    block: suspend () -> Unit
) = this.launch(dispatcher) {
    try {
        block()
    } catch (e: Exception) {
        Timber.tag(tag).e(e, "Error in coroutine: ${e.message}")
    }
}