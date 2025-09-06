package com.tecvo.taxi.repository

import android.app.Activity
import com.tecvo.taxi.services.ErrorHandlingService
import com.tecvo.taxi.services.ErrorHandlingService.AppError
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AuthRepository"

/**
 * Repository that handles all authentication-related operations
 */
@Singleton
open class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val errorHandlingService: ErrorHandlingService
) {
    /**
     * Check if a user is currently logged in
     * @return true if user is logged in, false otherwise
     */
    open suspend fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Get current user ID or null if no user is logged in
     */
    open fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Sign out the current user
     */
    open suspend fun signOut(): Int {
        withContext(Dispatchers.IO) {
            auth.signOut()
        }
        return 1 // Return success code
    }

    /**
     * Verify phone number to start authentication process
     */
    open fun verifyPhoneNumber(
        phoneNumber: String,
        activity: Activity,
        onVerificationIdReceived: (String) -> Unit,
        onVerificationCompleted: (PhoneAuthCredential) -> Unit,
        onVerificationFailed: (FirebaseException) -> Unit
    ) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Timber.tag(TAG).d("Phone verification completed automatically")
                onVerificationCompleted(credential)
            }
            override fun onVerificationFailed(e: FirebaseException) {
                Timber.tag(TAG).e("Phone verification failed: ${e.message}")
                onVerificationFailed(e)
            }
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Timber.tag(TAG).d("Verification code sent")
                onVerificationIdReceived(verificationId)
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    /**
     * Verify OTP code after it's been sent
     */
    open suspend fun verifyOtpCode(verificationId: String, code: String): Result<Unit> {
        return errorHandlingService.executeFirebaseOperation(
            tag = TAG,
            operation = {
                val credential = PhoneAuthProvider.getCredential(verificationId, code)
                auth.signInWithCredential(credential).await()
                Unit
            },
            onError = { error ->
                // Optional: Add analytics tracking for auth failures
                if (error is AppError.AuthError) {
                    // Track auth failures in analytics
                }
            }
        )
    }

    /**
     * Sign in with phone auth credential (typically from auto-verification)
     */
    open suspend fun signInWithCredential(credential: PhoneAuthCredential): Result<Unit> {
        return errorHandlingService.executeFirebaseOperation(
            tag = TAG,
            operation = {
                auth.signInWithCredential(credential).await()
                Unit
            },
            onError = { error ->
                // Optional: Add analytics tracking for auth failures
                if (error is AppError.AuthError) {
                    // Track auth failures in analytics
                }
            }
        )
    }

    /**
     * Delete user account
     */
    open suspend fun deleteUserAccount(): Result<Unit> {
        return errorHandlingService.executeFirebaseOperation(
            tag = TAG,
            operation = {
                auth.currentUser?.delete()?.await()
                Unit
            },
            onError = { error ->
                // Optional: Add analytics tracking for account deletion failures
            }
        )
    }
}