package com.tecvo.taxi.utils

import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import timber.log.Timber

object FirebaseCleanupUtil {
    private const val TAG = "FirebaseCleanupUtil"
    private const val CLEANUP_TIMEOUT_MS = 3000L // 3 seconds timeout

    fun removeUserData(
        reference: DatabaseReference?,
        scope: CoroutineScope,
        userId: String,
        userType: String,
        destination: String
    ) {
        if (reference == null) return

        scope.launch(Dispatchers.IO) {
            try {
                withTimeout(CLEANUP_TIMEOUT_MS) {
                    reference.removeValue().await()
                    Timber.tag(TAG)
                        .i("Successfully removed user data for $userId ($userType/$destination)")
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e("Error removing user data: ${e.message}")
            }
        }
    }
}