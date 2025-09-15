package com.tecvo.taxi.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.mockito.Mockito
import org.robolectric.shadows.ShadowLog

/**
 * Enhanced Firebase test utility with comprehensive initialization handling.
 * Supports both full Firebase initialization and selective mocking for different test scenarios.
 */
object TestFirebaseUtil {
    // Mock credentials for testing
    const val MOCK_API_KEY = "AIzaSyBdVl-cTICSwYKrZ95SuvNw7dbMuDt1KG0"
    const val MOCK_APP_ID = "1:123456789012:android:1234567890123456"
    const val MOCK_PROJECT_ID = "test-project"
    const val MOCK_DATABASE_URL = "https://test-project-default-rtdb.firebaseio.com/"
    
    private var isInitialized = false
    private var initializationMode = InitMode.FULL_MOCK

    enum class InitMode {
        FULL_MOCK,          // Mock everything - fastest for unit tests
        FIREBASE_MOCK,      // Initialize Firebase but mock specific services
        FULL_FIREBASE       // Full Firebase initialization (for integration tests)
    }

    /**
     * Initialize Firebase for testing with different modes
     */
    fun initializeTestFirebase(context: Context? = null, mode: InitMode = InitMode.FULL_MOCK) {
        if (isInitialized) return
        
        val testContext = context ?: ApplicationProvider.getApplicationContext<Context>()
        initializationMode = mode
        
        // Enable Robolectric logging for debugging if needed
        ShadowLog.stream = System.out
        
        when (mode) {
            InitMode.FULL_MOCK -> initializeWithFullMocking()
            InitMode.FIREBASE_MOCK -> initializeWithFirebaseMocking(testContext)
            InitMode.FULL_FIREBASE -> initializeWithFullFirebase(testContext)
        }
        
        isInitialized = true
        println("TestFirebaseUtil initialized with mode: $mode")
    }

    private fun initializeWithFullMocking() {
        // Mock static FirebaseApp methods to prevent initialization errors
        try {
            // This handles cases where Firebase dependencies are unavoidable
            System.setProperty("firebase.test.lab", "true")
        } catch (e: Exception) {
            println("Full mocking setup completed: ${e.message}")
        }
    }

    private fun initializeWithFirebaseMocking(context: Context) {
        try {
            // First clean up any existing Firebase apps
            FirebaseApp.getApps(context).forEach { app ->
                try {
                    app.delete()
                } catch (e: Exception) {
                    // Ignore cleanup errors during test setup
                }
            }
            
            // Initialize with minimal mock configuration
            val options = FirebaseOptions.Builder()
                .setApplicationId(MOCK_APP_ID)
                .setApiKey(MOCK_API_KEY)
                .setProjectId(MOCK_PROJECT_ID)
                .setDatabaseUrl(MOCK_DATABASE_URL)
                .build()

            // Initialize the default app
            FirebaseApp.initializeApp(context, options)
            println("Firebase initialized with mock configuration")

            // Disable services that shouldn't run in tests
            try {
                FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = false
            } catch (e: Exception) {
                println("Crashlytics setup completed: ${e.message}")
            }
        } catch (e: Exception) {
            println("Firebase mock initialization failed: ${e.message}")
            // For unit tests, we can continue with system properties
            System.setProperty("firebase.test.lab", "true")
        }
    }

    private fun initializeWithFullFirebase(context: Context) {
        // This would be used for integration tests that need real Firebase
        // For now, falls back to mock initialization
        initializeWithFirebaseMocking(context)
    }

    /**
     * Reset Firebase state for testing
     */
    fun reset() {
        isInitialized = false
        try {
            val context = ApplicationProvider.getApplicationContext<Context>()
            FirebaseApp.getApps(context).forEach { app ->
                try {
                    app.delete()
                } catch (e: Exception) {
                    // Ignore cleanup errors
                }
            }
        } catch (e: Exception) {
            // Context might not be available
        }
    }

    /**
     * Enhanced JUnit Rule for Firebase initialization with proper cleanup
     */
    class FirebaseTestRule(private val mode: InitMode = InitMode.FULL_MOCK) : TestRule {
        override fun apply(base: Statement, description: Description): Statement {
            return object : Statement() {
                override fun evaluate() {
                    var testException: Exception? = null
                    try {
                        // Always initialize Firebase before each test
                        val context = ApplicationProvider.getApplicationContext<Context>()
                        initializeTestFirebase(context, mode)
                        
                        // Run the test
                        base.evaluate()
                    } catch (e: Exception) {
                        testException = e
                        println("Test execution failed: ${e.message}")
                        
                        // If it's a Firebase initialization issue, try different approaches
                        if (e.message?.contains("FirebaseApp") == true || e.message?.contains("Default FirebaseApp") == true) {
                            try {
                                println("Attempting Firebase recovery...")
                                initializeEmergencyFirebase()
                                base.evaluate() // Retry the test
                                testException = null // Clear the exception if retry succeeds
                            } catch (retryException: Exception) {
                                println("Firebase recovery failed: ${retryException.message}")
                                testException = retryException
                            }
                        }
                    } finally {
                        // Cleanup is important for test isolation
                        try {
                            // Don't reset between tests in the same class to avoid re-initialization overhead
                            // reset() 
                        } catch (e: Exception) {
                            println("Cleanup warning: ${e.message}")
                        }
                    }
                    
                    // Re-throw the exception if the test still failed
                    testException?.let { throw it }
                }
            }
        }
        
        private fun initializeEmergencyFirebase() {
            try {
                val context = ApplicationProvider.getApplicationContext<Context>()
                System.setProperty("firebase.test.lab", "true")
                System.setProperty("com.google.firebase.testing", "true")
                
                // Force initialization with minimal configuration
                if (FirebaseApp.getApps(context).isEmpty()) {
                    val emergencyOptions = FirebaseOptions.Builder()
                        .setApiKey("emergency-key-for-testing")
                        .setApplicationId("1:000000:android:emergency")
                        .setProjectId("emergency-test-project")
                        .build()
                    FirebaseApp.initializeApp(context, emergencyOptions)
                    println("Emergency Firebase initialization successful")
                }
            } catch (e: Exception) {
                println("Emergency Firebase initialization failed: ${e.message}")
                throw e
            }
        }
    }

    /**
     * Create mock instances for Firebase services when needed
     */
    object Mocks {
        fun createMockFirebaseAuth(): FirebaseAuth = Mockito.mock(FirebaseAuth::class.java)
        fun createMockFirebaseDatabase(): FirebaseDatabase = Mockito.mock(FirebaseDatabase::class.java)
        fun createMockFirebaseApp(): FirebaseApp = Mockito.mock(FirebaseApp::class.java)
    }
}