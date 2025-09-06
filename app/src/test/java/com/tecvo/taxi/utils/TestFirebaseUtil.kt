package com.tecvo.taxi.utils

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.mockito.Mockito

object TestFirebaseUtil {
    // Consolidated mock credentials
    const val MOCK_API_KEY = "AIzaSyBdVl-cTICSwYKrZ95SuvNw7dbMuDt1KG0"
    const val MOCK_APP_ID = "1:123456789012:android:1234567890123456"
    const val MOCK_PROJECT_ID = "test-project"
    const val MOCK_DATABASE_URL = "https://test-project.firebaseio.com"

    fun initializeTestFirebase(context: Context) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            try {
                val options = FirebaseOptions.Builder()
                    .setApplicationId(MOCK_APP_ID)
                    .setApiKey(MOCK_API_KEY)
                    .setProjectId(MOCK_PROJECT_ID)
                    .setDatabaseUrl(MOCK_DATABASE_URL)
                    .build()

                FirebaseApp.initializeApp(context, options)

                // Disable Crashlytics for testing
                try {
                    FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = false
                } catch (e: Exception) {
                    println("Crashlytics disabled for testing: ${e.message}")
                }
            } catch (e: Exception) {
                println("Firebase initialization failed: ${e.message}")
                val mockApp = Mockito.mock(FirebaseApp::class.java)
                Mockito.`when`(FirebaseApp.getInstance()).thenReturn(mockApp)
            }
        }
    }

    // JUnit Rule for Firebase initialization
    class FirebaseTestRule : TestRule {
        override fun apply(base: Statement, description: Description): Statement {
            return object : Statement() {
                override fun evaluate() {
                    val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<Context>()
                    initializeTestFirebase(context)
                    base.evaluate()
                }
            }
        }
    }
}