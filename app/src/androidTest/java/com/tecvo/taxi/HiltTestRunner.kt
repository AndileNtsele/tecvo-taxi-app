package com.tecvo.taxi

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import com.tecvo.taxi.ui.utils.TestCleanupUtils
import dagger.hilt.android.testing.HiltTestApplication
import timber.log.Timber

/**
 * Enhanced Test Runner for Hilt-enabled UI Tests with automatic cleanup
 * TECVO TAXI - South African Taxi Matching App
 */
class HiltTestRunner : AndroidJUnitRunner() {

    override fun onCreate(arguments: Bundle) {
        super.onCreate(arguments)
        
        // Initialize Timber for test logging
        Timber.plant(Timber.DebugTree())
        
        // Clear all app data before tests run
        TestCleanupUtils.clearAllAppData()
        
        Timber.i("HiltTestRunner: TECVO TAXI test environment initialized")
    }

    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
    
    override fun onDestroy() {
        // Clean up after all tests
        TestCleanupUtils.clearAllAppData()
        super.onDestroy()
    }
}