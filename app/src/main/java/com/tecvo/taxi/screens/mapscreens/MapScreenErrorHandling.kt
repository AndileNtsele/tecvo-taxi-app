package com.tecvo.taxi.screens.mapscreens

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.tecvo.taxi.services.ErrorHandlingService
import com.tecvo.taxi.services.ErrorHandlingService.AppError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MapScreenError"

class MapScreenErrorHandler @Inject constructor(
    private val errorHandlingService: ErrorHandlingService
) {
    private lateinit var snackbarHostState: SnackbarHostState
    private lateinit var coroutineScope: CoroutineScope

    fun initialize(snackbarHostState: SnackbarHostState, coroutineScope: CoroutineScope) {
        this.snackbarHostState = snackbarHostState
        this.coroutineScope = coroutineScope
    }
    fun handlePermissionDenial() {
        val error = AppError.PermissionError(
            "LOCATION",
            "Location permission denied by user"
        )
        handleError(error)
    }

    fun handleError(error: AppError, showToast: Boolean = true) {
        coroutineScope.launch {
            errorHandlingService.logError(error, TAG)

            if (showToast) {
                val userMessage = errorHandlingService.getUserFriendlyMessage(error)
                errorHandlingService.showToast(userMessage)
            }

            val snackbarMessage = errorHandlingService.getUserFriendlyMessage(error)
            snackbarHostState.showSnackbar(snackbarMessage)
        }
    }
}

@Composable
fun rememberMapScreenErrorHandler(
    snackbarHostState: SnackbarHostState,
    errorHandlingService: ErrorHandlingService
): MapScreenErrorHandler {
    val coroutineScope = rememberCoroutineScope()
    val errorHandler = remember(snackbarHostState, coroutineScope) {
        val handler = MapScreenErrorHandler(errorHandlingService)
        handler.initialize(snackbarHostState, coroutineScope)
        handler
    }
    return errorHandler
}