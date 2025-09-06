package com.tecvo.taxi.ui.utils

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import timber.log.Timber

/**
 * Utility class for cleaning up test state between test runs.
 * Ensures each test starts with a clean slate.
 */
object TestCleanupUtils {
    
    private const val TAG = "TestCleanup"
    
    /**
     * Clear all app data to ensure clean test state
     */
    fun clearAllAppData() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        
        // Clear SharedPreferences
        clearSharedPreferences(context)
        
        // Clear app cache
        clearCache(context)
        
        // Clear databases
        clearDatabases(context)
        
        // Sign out from Firebase
        signOutFirebase()
    }
    
    /**
     * Clear all SharedPreferences
     */
    private fun clearSharedPreferences(context: Context) {
        try {
            val prefsDir = context.filesDir.parentFile?.resolve("shared_prefs")
            prefsDir?.listFiles()?.forEach { file ->
                if (file.name.endsWith(".xml")) {
                    val prefName = file.nameWithoutExtension
                    context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                        .edit()
                        .clear()
                        .commit()
                }
            }
            Timber.tag(TAG).d("SharedPreferences cleared")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to clear SharedPreferences")
        }
    }
    
    /**
     * Clear app cache
     */
    private fun clearCache(context: Context) {
        try {
            context.cacheDir.deleteRecursively()
            Timber.tag(TAG).d("Cache cleared")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to clear cache")
        }
    }
    
    /**
     * Clear all databases
     */
    private fun clearDatabases(context: Context) {
        try {
            val dbDir = context.filesDir.parentFile?.resolve("databases")
            dbDir?.listFiles()?.forEach { it.delete() }
            Timber.tag(TAG).d("Databases cleared")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to clear databases")
        }
    }
    
    /**
     * Sign out from Firebase Auth
     */
    private fun signOutFirebase() {
        try {
            FirebaseAuth.getInstance().signOut()
            Timber.tag(TAG).d("Firebase Auth signed out")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to sign out from Firebase")
        }
    }
    
    /**
     * Reset test environment to initial state
     */
    suspend fun resetTestEnvironment() {
        // Clear all data
        clearAllAppData()
        
        // Wait for cleanup to complete
        kotlinx.coroutines.delay(500)
        
        // Verify cleanup
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            Timber.tag(TAG).w("Firebase user still present after cleanup, forcing sign out")
            auth.signOut()
        }
    }
    
    /**
     * Clear specific test data without full reset
     */
    fun clearTestData(context: Context, vararg prefNames: String) {
        prefNames.forEach { prefName ->
            context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit()
        }
    }
}