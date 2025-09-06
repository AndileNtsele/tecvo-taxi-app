package com.tecvo.taxi.services

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.tecvo.taxi.utils.TestFirebaseUtil
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O], application = HiltTestApplication::class)
class NotificationServiceTest {
    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockNotificationManager: NotificationManager

    @Mock
    private lateinit var mockLocationService: LocationService

    @Mock
    private lateinit var mockNotificationStateManager: NotificationStateManager

    @Mock
    private lateinit var mockSharedPrefs: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    @Mock
    private lateinit var mockFirebaseDatabase: FirebaseDatabase

    @Mock
    private lateinit var mockDatabaseReference: DatabaseReference

    private lateinit var firebaseDatabaseStaticMock: MockedStatic<FirebaseDatabase>

    @get:Rule
    val firebaseRule = TestFirebaseUtil.FirebaseTestRule()

    // Class under test
    private lateinit var notificationService: NotificationService

    // Test constants
    private val testUserId = "test-user-id"
    private val testUserType = "driver"
    private val testDestination = "town"
    private val testLocation = LatLng(37.7749, -122.4194)

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // Initialize Firebase for testing
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        TestFirebaseUtil.initializeTestFirebase(appContext)

        // Setup static mock for FirebaseDatabase
        firebaseDatabaseStaticMock = Mockito.mockStatic(FirebaseDatabase::class.java)
        firebaseDatabaseStaticMock.`when`<FirebaseDatabase> { FirebaseDatabase.getInstance() }.thenReturn(mockFirebaseDatabase)

        // Configure mock behavior
        setupMockDependencies()

        // Create the service with mocked dependencies
        notificationService = NotificationService(mockContext, mockLocationService, mockNotificationStateManager)
    }

    private fun setupMockDependencies() {
        // Mock context behavior
        `when`(mockContext.getSystemService(Context.NOTIFICATION_SERVICE)).thenReturn(mockNotificationManager)
        `when`(mockContext.getSharedPreferences("taxi_app_prefs", Context.MODE_PRIVATE)).thenReturn(mockSharedPrefs)

        // Mock SharedPreferences behavior
        `when`(mockSharedPrefs.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putBoolean(any(), any())).thenReturn(mockEditor)
        `when`(mockEditor.putFloat(any(), any())).thenReturn(mockEditor)
        `when`(mockEditor.apply()).thenAnswer { }

        // Default preference values
        `when`(mockSharedPrefs.getBoolean("notifications_enabled", true)).thenReturn(true)
        `when`(mockSharedPrefs.getBoolean("notify_passengers", true)).thenReturn(true)
        `when`(mockSharedPrefs.getBoolean("notify_drivers", true)).thenReturn(true)
        `when`(mockSharedPrefs.getFloat("notification_radius_km", 1.0f)).thenReturn(2.0f)

        // Mock LocationService behavior
        `when`(mockLocationService.calculateDistance(any(), any())).thenReturn(1.5)

        // Mock Firebase behavior
        `when`(mockFirebaseDatabase.reference).thenReturn(mockDatabaseReference)
        `when`(mockDatabaseReference.child(any())).thenReturn(mockDatabaseReference)
        
        // Mock NotificationStateManager behavior
        `when`(mockNotificationStateManager.requestStartMonitoring(any(), any(), any())).thenReturn(true)
        `when`(mockNotificationStateManager.requestStopMonitoring()).thenReturn(true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        // Close the static mock
        firebaseDatabaseStaticMock.close()

        try {
            notificationService.cleanup()
        } catch (_: Exception) {
            // Ignore cleanup errors in tests
        }
    }

    @Test
    fun `createNotificationChannel creates channel with correct settings on Android O and above`() {
        // Verify channel creation
        verify(mockNotificationManager).createNotificationChannel(any())
    }

    @Test
    fun `startMonitoring initializes state correctly`() {
        // When
        notificationService.startMonitoring(testUserId, testLocation, testDestination, testUserType)

        // Then
        // Verify monitoring was initialized (indirectly verify that Firebase listener was set up)
        verify(mockDatabaseReference).child("passengers/town")
    }

    @Test
    fun `startMonitoring clears previous notifications`() {
        // Setup a helper method to add entity to the notified set
        val setField = NotificationService::class.java.getDeclaredField("notifiedEntities")
        setField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val notifiedEntities = setField.get(notificationService) as MutableSet<String>
        notifiedEntities.add("previous-entity")

        // When
        notificationService.startMonitoring(testUserId, testLocation, testDestination, testUserType)

        // Then
        assertTrue("Notified entities should be cleared", notifiedEntities.isEmpty())
    }

    @Test
    fun `toggleNotifications updates preferences and monitoring state`() {
        // When enabling
        notificationService.toggleNotifications(true)

        // Then verify preferences updated
        verify(mockEditor).putBoolean("notifications_enabled", true)
        verify(mockEditor).apply()

        // When disabling
        notificationService.toggleNotifications(false)

        // Then verify preferences updated again
        verify(mockEditor).putBoolean("notifications_enabled", false)
        verify(mockEditor, times(2)).apply()
    }

    @Test
    fun `updateDestination sets new destination and restarts monitoring`() {
        // First initialize with a starting destination
        notificationService.startMonitoring(testUserId, testLocation, "local", testUserType)

        // Clear invocations to reset the verification count
        Mockito.clearInvocations(mockDatabaseReference)

        // When
        notificationService.updateDestination(testDestination)

        // Then
        // Verify new monitoring was started with the new destination
        verify(mockDatabaseReference).child("passengers/town")
    }

    @Test
    fun `updateUserType sets new user type and restarts monitoring`() {
        // First initialize with a starting user type
        notificationService.startMonitoring(testUserId, testLocation, testDestination, "passenger")

        // Clear invocations to reset the verification count
        Mockito.clearInvocations(mockDatabaseReference)

        // When
        notificationService.updateUserType(testUserType)

        // Then
        // Verify new monitoring was started with the new user type
        verify(mockDatabaseReference).child("passengers/town")
    }

    @Test
    fun `updateNotificationPreferences updates from SharedPreferences`() {
        // When
        notificationService.updateNotificationPreferences()

        // Then - verify preferences were read
        verify(mockSharedPrefs).getBoolean("notifications_enabled", true)
        verify(mockSharedPrefs).getBoolean("notify_different_role", true)
        verify(mockSharedPrefs).getBoolean("notify_same_role", false)
        verify(mockSharedPrefs).getBoolean("notify_proximity", true)
        verify(mockSharedPrefs).getFloat("notification_radius_km", 1.0f)
    }

    @Test
    fun `shouldShowNotifications checks if notifications are enabled`() {
        // When notifications are enabled
        `when`(mockSharedPrefs.getBoolean("notifications_enabled", true)).thenReturn(true)

        // Setup user ID
        val userIdField = NotificationService::class.java.getDeclaredField("userId")
        userIdField.isAccessible = true
        userIdField.set(notificationService, testUserId)

        // Then
        assertTrue(notificationService.shouldShowNotifications())

        // When notifications are disabled
        `when`(mockSharedPrefs.getBoolean("notifications_enabled", true)).thenReturn(false)

        // Then
        assertFalse(notificationService.shouldShowNotifications())
    }

    @Test
    fun `hasNotificationPermission returns correct permission status`() {
        // For API 33+ (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Mock permission denied
            `when`(mockContext.checkSelfPermission(any())).thenReturn(android.content.pm.PackageManager.PERMISSION_DENIED)
            assertFalse(notificationService.hasNotificationPermission())

            // Mock permission granted
            `when`(mockContext.checkSelfPermission(any())).thenReturn(android.content.pm.PackageManager.PERMISSION_GRANTED)
            assertTrue(notificationService.hasNotificationPermission())
        } else {
            // For API < 33, permission is always granted
            assertTrue(notificationService.hasNotificationPermission())
        }
    }

    @Test
    fun `saveNotificationPreferences updates shared preferences`() {
        // When
        notificationService.saveNotificationPreferences(
            enabled = true,
            notifyDifferentRole = true,
            notifySameRole = false,
            notifyProximity = true,
            radiusKm = 3.0f
        )

        // Then
        verify(mockEditor).putBoolean("notifications_enabled", true)
        verify(mockEditor).putBoolean("notify_different_role", true)
        verify(mockEditor).putBoolean("notify_same_role", false)
        verify(mockEditor).putBoolean("notify_proximity", true)
        verify(mockEditor).putFloat("notification_radius_km", 3.0f)
        verify(mockEditor).apply()
    }

    @Test
    fun `saveNotificationPreferences with partial parameters only updates specified values`() {
        // When
        notificationService.saveNotificationPreferences(
            enabled = true,
            radiusKm = 3.0f
        )

        // Then
        verify(mockEditor).putBoolean("notifications_enabled", true)
        verify(mockEditor).putFloat("notification_radius_km", 3.0f)
        verify(mockEditor, never()).putBoolean("notify_different_role", true)
        verify(mockEditor, never()).putBoolean("notify_same_role", false)
        verify(mockEditor, never()).putBoolean("notify_proximity", true)
        verify(mockEditor).apply()
    }

    @Test
    fun `cleanup stops monitoring`() {
        // Initialize service
        notificationService.startMonitoring(testUserId, testLocation, testDestination, testUserType)

        // Create a specific mock for the entity path that will be used in stopMonitoring
        val oppositeType = "passenger" // Since testUserType is "driver"
        val path = "${oppositeType}s/$testDestination"
        val mockEntityRef = mock(DatabaseReference::class.java)

        // Configure the reference chain
        `when`(mockFirebaseDatabase.reference).thenReturn(mockDatabaseReference)
        `when`(mockDatabaseReference.child(path)).thenReturn(mockEntityRef)

        // Set a mock entity listener via reflection
        val entityField = NotificationService::class.java.getDeclaredField("entityListener")
        entityField.isAccessible = true
        val mockListener = mock(ValueEventListener::class.java)
        entityField.set(notificationService, mockListener)

        // When
        notificationService.cleanup()

        // Then verify the listener was removed
        verify(mockEntityRef).removeEventListener(mockListener)
    }

    @Test
    fun `updateUserLocation updates location tracking`() {
        // When
        notificationService.updateUserLocation(testLocation)

        // Then
        val locationField = NotificationService::class.java.getDeclaredField("currentUserLocation")
        locationField.isAccessible = true
        assertEquals(testLocation, locationField.get(notificationService))
    }

    @Test
    fun `enterMapScreen and exitMapScreen toggle map state`() {
        // When
        notificationService.enterMapScreen("TestScreen")

        // Then
        val inMapScreenField = NotificationService::class.java.getDeclaredField("isInMapScreen")
        inMapScreenField.isAccessible = true
        assertTrue(inMapScreenField.getBoolean(notificationService))

        // When
        notificationService.exitMapScreen("TestScreen")

        // Then
        assertFalse(inMapScreenField.getBoolean(notificationService))
    }
}