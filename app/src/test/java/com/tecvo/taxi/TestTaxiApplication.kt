package com.tecvo.taxi

import android.app.Application
import com.tecvo.taxi.utils.TestFirebaseUtil
import timber.log.Timber

/**
 * Test-only Application class that prevents Firebase initialization issues during unit testing.
 * 
 * This class replaces TaxiApplication during Robolectric tests to prevent the automatic
 * Firebase initialization that occurs in TaxiApplication.onCreate() from conflicting
 * with test-specific Firebase setup.
 * 
 * Key differences from TaxiApplication:
 * - No @HiltAndroidApp annotation (prevents Hilt from trying to inject dependencies)
 * - No Firebase initialization (handled by TestFirebaseUtil instead)
 * - Minimal setup to support basic Android component testing
 */
class TestTaxiApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for test logging (safe operation)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.d("TestTaxiApplication initialized for testing")
        }
        
        // Store instance for any tests that might need it
        instance = this
        
        // Initialize Firebase using the test utility instead of real Firebase
        try {
            TestFirebaseUtil.initializeTestFirebase(this, TestFirebaseUtil.InitMode.FIREBASE_MOCK)
            Timber.d("Test Firebase initialization completed")
        } catch (e: Exception) {
            Timber.w("Test Firebase initialization failed: ${e.message}")
            // Continue with tests even if Firebase mock setup fails
        }
        
        Timber.d("TestTaxiApplication onCreate completed")
    }

    override fun onTerminate() {
        super.onTerminate()
        
        // Clean up test Firebase state
        try {
            TestFirebaseUtil.reset()
            Timber.d("Test Firebase cleanup completed")
        } catch (e: Exception) {
            Timber.w("Test Firebase cleanup warning: ${e.message}")
        }
        
        Timber.d("TestTaxiApplication terminating")
    }

    companion object {
        @Volatile 
        private var instance: TestTaxiApplication? = null
        
        fun getInstance(): TestTaxiApplication {
            return instance ?: throw IllegalStateException("TestTaxiApplication not initialized")
        }
    }
}