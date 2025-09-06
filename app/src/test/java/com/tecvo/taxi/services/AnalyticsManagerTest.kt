package com.tecvo.taxi.services

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.tecvo.taxi.utils.TestFirebaseUtil
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], application = HiltTestApplication::class)
class AnalyticsManagerTest {
    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockFirebaseAnalytics: FirebaseAnalytics

    private lateinit var firebaseAnalyticsStaticMock: MockedStatic<FirebaseAnalytics>

    @get:Rule
    val firebaseRule = TestFirebaseUtil.FirebaseTestRule()

    // Class under test
    private lateinit var analyticsManager: AnalyticsManager

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // Initialize Firebase for testing
        val context = ApplicationProvider.getApplicationContext<Context>()
        TestFirebaseUtil.initializeTestFirebase(context)

        // Setup static mock for FirebaseAnalytics
        firebaseAnalyticsStaticMock = Mockito.mockStatic(FirebaseAnalytics::class.java)
        firebaseAnalyticsStaticMock.`when`<FirebaseAnalytics> {
            FirebaseAnalytics.getInstance(any())
        }.thenReturn(mockFirebaseAnalytics)

        // Create the manager with mocked dependencies
        analyticsManager = AnalyticsManager(mockContext)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        firebaseAnalyticsStaticMock.close()
    }

    @Test
    fun `logEvent logs event with correct name`() {
        // Given
        val eventName = "test_event"
        val params = mapOf("string_param" to "value")

        // When
        analyticsManager.logEvent(eventName, params)

        // Then
        verify(mockFirebaseAnalytics).logEvent(eq(eventName), any())
    }

    @Test
    fun `logEvent handles different parameter types correctly`() {
        // Given
        val eventName = "test_event"
        val params = mapOf(
            "string_param" to "value",
            "int_param" to 123,
            "long_param" to 123L,
            "double_param" to 123.45,
            "boolean_param" to true
        )

        // When
        analyticsManager.logEvent(eventName, params)

        // Then
        verify(mockFirebaseAnalytics).logEvent(eq(eventName), any())
    }

    @Test
    fun `setUserProperty sets property with correct values`() {
        // Given
        val propertyName = "user_type"
        val propertyValue = "driver"

        // When
        analyticsManager.setUserProperty(propertyName, propertyValue)

        // Then
        verify(mockFirebaseAnalytics).setUserProperty(propertyName, propertyValue)
    }

    @Test
    fun `logScreenView logs screen view event with correct name`() {
        // Given
        val screenName = "MapScreen"
        val screenClass = "MapActivity"

        // When
        analyticsManager.logScreenView(screenName, screenClass)

        // Then
        verify(mockFirebaseAnalytics).logEvent(eq(FirebaseAnalytics.Event.SCREEN_VIEW), any())
    }

    @Test
    fun `logScreenView works without screenClass parameter`() {
        // Given
        val screenName = "MapScreen"

        // When
        analyticsManager.logScreenView(screenName)

        // Then
        verify(mockFirebaseAnalytics).logEvent(eq(FirebaseAnalytics.Event.SCREEN_VIEW), any())
    }

    @Test
    fun `setUserId sets user ID correctly`() {
        // Given
        val userId = "test-user-123"

        // When
        analyticsManager.setUserId(userId)

        // Then
        verify(mockFirebaseAnalytics).setUserId(userId)
    }

    @Test
    fun `setAnalyticsEnabled enables analytics collection`() {
        // Given
        val isEnabled = true

        // When
        analyticsManager.setAnalyticsEnabled(isEnabled)

        // Then
        verify(mockFirebaseAnalytics).setAnalyticsCollectionEnabled(isEnabled)
    }

    @Test
    fun `setAnalyticsEnabled disables analytics collection`() {
        // Given
        val isEnabled = false

        // When
        analyticsManager.setAnalyticsEnabled(isEnabled)

        // Then
        verify(mockFirebaseAnalytics).setAnalyticsCollectionEnabled(isEnabled)
    }

    // Note: We're not fully testing resetAnalyticsData as it involves static Firebase.analytics.resetAnalyticsData()
    // which requires a different mocking approach. In a real-world scenario, we might refactor for testability.
    @Test
    fun `resetAnalyticsData does not throw exception`() {
        // Just verify that calling this method doesn't cause a crash
        analyticsManager.resetAnalyticsData()
        // No assertion needed - we're just checking that no exception is thrown
    }
}