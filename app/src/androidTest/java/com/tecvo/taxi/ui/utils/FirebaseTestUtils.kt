package com.tecvo.taxi.ui.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.mockito.Mockito.*
import javax.inject.Singleton

/**
 * Firebase testing utilities for UI tests.
 * Provides mocked Firebase services and test data setup.
 */
@Singleton
class FirebaseTestUtils {

    companion object {
        private const val TEST_USER_ID = "test_user_123"
        private const val TEST_PHONE_NUMBER = "+27728588857" // 072 858 8857 in international format
        private const val TEST_PHONE_DISPLAY = "072 858 8857" // SA display format
        private const val TEST_OTP_CODE = "123456"
        private const val TEST_EMAIL = "test@example.com"
        
        /**
         * Gets the test phone number in display format (SA format)
         */
        fun getTestPhoneDisplayFormat(): String = TEST_PHONE_DISPLAY

        /**
         * Gets the test phone number in international format
         */
        fun getTestPhoneInternationalFormat(): String = TEST_PHONE_NUMBER

        /**
         * Gets the test OTP code
         */
        fun getTestOtpCode(): String = TEST_OTP_CODE

        /**
         * Creates a mocked FirebaseAuth instance with a test user
         */
        fun createMockedFirebaseAuth(isSignedIn: Boolean = true): FirebaseAuth {
            val mockAuth = mock(FirebaseAuth::class.java)
            
            if (isSignedIn) {
                val mockUser = createMockedFirebaseUser()
                `when`(mockAuth.currentUser).thenReturn(mockUser)
            } else {
                `when`(mockAuth.currentUser).thenReturn(null)
            }
            
            return mockAuth
        }
        
        /**
         * Creates a mocked FirebaseUser for testing
         */
        fun createMockedFirebaseUser(): FirebaseUser {
            val mockUser = mock(FirebaseUser::class.java)
            `when`(mockUser.uid).thenReturn(TEST_USER_ID)
            `when`(mockUser.phoneNumber).thenReturn(TEST_PHONE_NUMBER)
            `when`(mockUser.email).thenReturn(TEST_EMAIL)
            `when`(mockUser.isEmailVerified).thenReturn(true)
            return mockUser
        }
        
        /**
         * Creates a mocked Firebase Database reference
         */
        fun createMockedFirebaseDatabase(): FirebaseDatabase {
            val mockDatabase = mock(FirebaseDatabase::class.java)
            val mockReference = mock(DatabaseReference::class.java)
            
            `when`(mockDatabase.reference).thenReturn(mockReference)
            `when`(mockDatabase.getReference(anyString())).thenReturn(mockReference)
            
            return mockDatabase
        }
        
        /**
         * Sets up test data for user presence in Firebase
         */
        fun setupTestUserPresence(
            database: FirebaseDatabase,
            role: String,
            destination: String,
            location: TestLocation = TestLocation.JOHANNESBURG
        ) {
            val mockReference = mock(DatabaseReference::class.java)
            val path = "${role}s/$destination/$TEST_USER_ID"
            
            `when`(database.getReference(path)).thenReturn(mockReference)
            
            // Mock successful write operations
            `when`(mockReference.setValue(any())).thenReturn(mock())
            `when`(mockReference.removeValue()).thenReturn(mock())
        }
        
        /**
         * Sets up test data for other users on the map
         */
        fun setupTestOtherUsers(
            database: FirebaseDatabase,
            role: String,
            destination: String,
            userCount: Int = 3
        ): List<TestUser> {
            val testUsers = mutableListOf<TestUser>()
            
            repeat(userCount) { index ->
                val testUser = TestUser(
                    id = "test_user_$index",
                    role = role,
                    destination = destination,
                    location = TestLocation.values()[index % TestLocation.values().size]
                )
                testUsers.add(testUser)
            }
            
            return testUsers
        }
        
        /**
         * Simulates Firebase connection state changes
         */
        fun simulateFirebaseConnectionState(
            database: FirebaseDatabase,
            isConnected: Boolean
        ) {
            val mockReference = mock(DatabaseReference::class.java)
            `when`(database.getReference(".info/connected")).thenReturn(mockReference)
            
            // Mock connection state listeners would be set up here
            // This is simplified for the UI test context
        }
    }
    
    /**
     * Test user data class
     */
    data class TestUser(
        val id: String,
        val role: String,
        val destination: String,
        val location: TestLocation,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * Predefined test locations
     */
    enum class TestLocation(
        val latitude: Double,
        val longitude: Double,
        val locationName: String
    ) {
        JOHANNESBURG(-26.2041, 28.0473, "Johannesburg"),
        CAPE_TOWN(-33.9249, 18.4241, "Cape Town"),
        DURBAN(-29.8587, 31.0218, "Durban"),
        PRETORIA(-25.7479, 28.2293, "Pretoria"),
        BLOEMFONTEIN(-29.0852, 26.1596, "Bloemfontein")
    }
}