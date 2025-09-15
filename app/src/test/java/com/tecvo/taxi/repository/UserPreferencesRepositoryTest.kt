package com.tecvo.taxi.repository

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.tecvo.taxi.repository.UserPreferencesRepository
import com.tecvo.taxi.utils.TestSharedPreferencesUtil


@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], application = HiltTestApplication::class)
class UserPreferencesRepositoryTest {
    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    private lateinit var mockEditor: Editor

    private lateinit var repository: UserPreferencesRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // Create properly configured SharedPreferences mock with test data
        val testData = TestSharedPreferencesUtil.createUserPreferencesTestData() + 
                      TestSharedPreferencesUtil.createNotificationRadiusTestData()
        mockSharedPreferences = TestSharedPreferencesUtil.createMockSharedPreferences(testData)

        // Override the editor to use our test mock
        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString(Mockito.anyString(), Mockito.anyString())).thenReturn(mockEditor)
        `when`(mockEditor.putBoolean(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(mockEditor)
        `when`(mockEditor.putFloat(Mockito.anyString(), Mockito.anyFloat())).thenReturn(mockEditor)
        `when`(mockEditor.apply()).then { /* no-op */ }

        // Setup mock context
        `when`(mockContext.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)).thenReturn(mockSharedPreferences)

        // Create the repository with mocked dependencies
        repository = UserPreferencesRepository(mockContext, mockSharedPreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        repository.cleanup()
    }

    @Test
    fun `getLastSelectedRole returns value from shared preferences`() = runTest {
        // When
        val result = repository.getLastSelectedRole()

        // Then
        assertEquals("driver", result)
        verify(mockSharedPreferences, atLeastOnce()).getString("last_selected_role", null)
    }

    @Test
    fun `saveLastSelectedRole calls sharedPreferences edit`() = runTest {
        // When
        val result = repository.saveLastSelectedRole("passenger")

        // Then
        assertEquals(1, result)
        verify(mockEditor).putString("last_selected_role", "passenger")
        verify(mockEditor).apply()
    }

    @Test
    fun `getLastSelectedDestination returns value from shared preferences`() = runTest {
        // When
        val result = repository.getLastSelectedDestination()

        // Then
        assertEquals("town", result)
        verify(mockSharedPreferences, atLeastOnce()).getString("last_selected_destination", "town")
    }

    @Test
    fun `getNotificationRadius returns value from shared preferences`() = runTest {
        // When
        val result = repository.getNotificationRadius()

        // Then
        assertEquals(3.5f, result, 0.001f)
        verify(mockSharedPreferences, atLeastOnce()).getFloat("notification_radius_km", 0.5f)
    }

    @Test
    fun `areNotificationsEnabled returns value from shared preferences`() = runTest {
        // When
        val result = repository.areNotificationsEnabled()

        // Then
        assertEquals(true, result)
        verify(mockSharedPreferences, atLeastOnce()).getBoolean("notifications_enabled", true)
    }

    @Test
    fun `areDifferentRoleNotificationsEnabled returns value from shared preferences`() = runTest {
        // When
        val result = repository.areDifferentRoleNotificationsEnabled()

        // Then
        assertEquals(true, result)
        verify(mockSharedPreferences, atLeastOnce()).getBoolean("notify_different_role", true)
    }

    @Test
    fun `areSameRoleNotificationsEnabled returns value from shared preferences`() = runTest {
        // When
        val result = repository.areSameRoleNotificationsEnabled()

        // Then
        assertEquals(false, result)
        verify(mockSharedPreferences, atLeastOnce()).getBoolean("notify_same_role", false)
    }

    @Test
    fun `areProximityNotificationsEnabled returns value from shared preferences`() = runTest {
        // When
        val result = repository.areProximityNotificationsEnabled()

        // Then
        assertEquals(false, result)
        verify(mockSharedPreferences, atLeastOnce()).getBoolean("notify_proximity", false)
    }

    @Test
    fun `setNotificationsEnabled updates shared preferences`() = runTest {
        // When
        repository.setNotificationsEnabled(false)

        // Then
        verify(mockEditor).putBoolean("notifications_enabled", false)
        verify(mockEditor).apply()
    }

    @Test
    fun `setSameRoleNotificationsEnabled updates shared preferences`() = runTest {
        // When
        repository.setSameRoleNotificationsEnabled(true)

        // Then
        verify(mockEditor).putBoolean("notify_same_role", true)
        verify(mockEditor).apply()
    }

    @Test
    fun `setProximityNotificationsEnabled updates shared preferences`() = runTest {
        // When
        repository.setProximityNotificationsEnabled(true)

        // Then
        verify(mockEditor).putBoolean("notify_proximity", true)
        verify(mockEditor).apply()
    }

    @Test
    fun `setNotificationRadius updates shared preferences`() = runTest {
        // When
        repository.setNotificationRadius(5.0f)

        // Then
        verify(mockEditor).putFloat("notification_radius_km", 5.0f)
        verify(mockEditor).apply()
    }
}