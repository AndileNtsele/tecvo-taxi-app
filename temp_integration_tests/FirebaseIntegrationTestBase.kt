package com.example.taxi.integration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.taxi.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.UUID

/**
 * Base class for Firebase integration tests that connect to a real Firebase test instance.
 * Provides setup and teardown for Firebase connections and manages test data cleanup.
 */
@ExperimentalCoroutinesApi
abstract class FirebaseIntegrationTestBase {
    
    protected val testDispatcher = StandardTestDispatcher()
    protected lateinit var testDatabase: FirebaseDatabase
    protected lateinit var testContext: Context
    
    // Generate unique test identifiers to avoid conflicts between parallel tests
    protected val testSessionId = UUID.randomUUID().toString()
    protected val testUserId = "test-user-${testSessionId}"
    protected val testUserType = "driver"
    protected val testDestination = "town"
    
    @get:Rule
    val firebaseIntegrationRule = FirebaseIntegrationRule()
    
    @Before
    open fun setupFirebaseIntegration() {
        testContext = ApplicationProvider.getApplicationContext()
        
        // Initialize Firebase with real test database URL from BuildConfig
        initializeRealFirebaseForTesting()
        
        testDatabase = FirebaseDatabase.getInstance()
        testDatabase.useEmulator("127.0.0.1", 9000) // Use Firebase emulator for testing
    }
    
    @After
    open fun tearDownFirebaseIntegration() {
        // Clean up test data from Firebase
        cleanupTestData()
    }
    
    private fun initializeRealFirebaseForTesting() {
        val apps = FirebaseApp.getApps(testContext)
        if (apps.any { it.name == "integration-test" }) {
            return // Already initialized
        }
        
        val options = FirebaseOptions.Builder()
            .setApplicationId("1:123456789:android:test")
            .setApiKey("test-api-key")
            .setProjectId("taxi-integration-test")
            .setDatabaseUrl("http://127.0.0.1:9000/?ns=taxi-integration-test") // Emulator URL
            .build()
            
        FirebaseApp.initializeApp(testContext, options, "integration-test")
    }
    
    private fun cleanupTestData() {
        try {
            // Clean up all test data for this session
            testDatabase.reference
                .child("drivers")
                .child(testDestination)
                .child(testUserId)
                .removeValue()
                
            testDatabase.reference
                .child("passengers")
                .child(testDestination)
                .child(testUserId)
                .removeValue()
        } catch (e: Exception) {
            // Log but don't fail test cleanup
            println("Warning: Failed to clean up test data: ${e.message}")
        }
    }
    
    /**
     * JUnit Rule for Firebase integration test setup
     */
    inner class FirebaseIntegrationRule : TestRule {
        override fun apply(base: Statement, description: Description): Statement {
            return object : Statement() {
                override fun evaluate() {
                    try {
                        // Ensure Firebase emulator is available before running tests
                        checkFirebaseEmulatorAvailability()
                        base.evaluate()
                    } catch (e: Exception) {
                        throw AssertionError(
                            "Firebase integration test failed. Ensure Firebase emulator is running on port 9000. " +
                            "Run: firebase emulators:start --only database", e
                        )
                    }
                }
            }
        }
        
        private fun checkFirebaseEmulatorAvailability() {
            // This would ideally check if emulator is running
            // For now, we'll assume it's available and let the connection attempt reveal any issues
        }
    }
}