package com.example.taxi.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import model.User
import com.example.taxi.services.ErrorHandlingService
import com.example.taxi.services.LocationService
import com.example.taxi.utils.TestFirebaseUtil
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.capture
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import repository.FirebaseRepository

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], application = HiltTestApplication::class)
class FirebaseRepositoryTest {
    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var mockDatabase: FirebaseDatabase

    @Mock
    private lateinit var mockDatabaseReference: DatabaseReference

    @Mock
    private lateinit var mockUsersReference: DatabaseReference

    @Mock
    private lateinit var mockUserIdReference: DatabaseReference

    @Mock
    private lateinit var mockEntitiesReference: DatabaseReference

    @Mock
    private lateinit var mockLocationService: LocationService

    @Mock
    private lateinit var mockErrorHandlingService: ErrorHandlingService

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    private lateinit var mockContext: Context

    private lateinit var firebaseRepository: FirebaseRepository

    // Test constants
    private val testUserId = "test-user-id"
    private val testUserType = "driver"
    private val testDestination = "available"

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // Initialize Firebase for testing
        val context = ApplicationProvider.getApplicationContext<Context>()
        TestFirebaseUtil.initializeTestFirebase(context)

        // Configure mock behavior for database references chain
        `when`(mockDatabase.reference).thenReturn(mockDatabaseReference)

        // Mock users reference path
        `when`(mockDatabaseReference.child("users")).thenReturn(mockUsersReference)
        `when`(mockUsersReference.child(testUserId)).thenReturn(mockUserIdReference)

        // Mock entities reference path
        val entitiesPath = "${testUserType}s/$testDestination"
        `when`(mockDatabaseReference.child(entitiesPath)).thenReturn(mockEntitiesReference)

        // Configure mock behavior for shared preferences
        `when`(mockContext.getSharedPreferences(eq("taxi_app_prefs"), eq(Context.MODE_PRIVATE)))
            .thenReturn(mockSharedPreferences)
        `when`(mockSharedPreferences.getFloat(eq("notification_radius_km"), eq(2.0f)))
            .thenReturn(3.5f)

        // Create the repository with mocked dependencies
        firebaseRepository = FirebaseRepository(
            mockDatabase,
            mockErrorHandlingService,
            mockContext
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getNotificationRadius returns value from shared preferences`() {
        // When
        val result = firebaseRepository.getNotificationRadius()

        // Then
        assertEquals(3.5, result, 0.001)
    }

    @Test
    fun `listenForEntities adds value event listener to correct reference`() = runTest {
        // Given
        val listenerCaptor = ArgumentCaptor.forClass(ValueEventListener::class.java)

        // When
        val returnedListener = firebaseRepository.listenForEntities(
            testUserType,
            testDestination,
            testUserId,
            onEntitiesUpdate = { },
            onError = { }
        )

        // Then
        val entitiesPath = "${testUserType}s/$testDestination"
        verify(mockDatabaseReference).child(entitiesPath)
        verify(mockEntitiesReference).addValueEventListener(capture(listenerCaptor))
        assertEquals(returnedListener, listenerCaptor.value)
    }

    @Test
    fun `listenForEntities handles data change correctly`() = runTest {
        // Given
        val listenerCaptor = ArgumentCaptor.forClass(ValueEventListener::class.java)
        val locationsList = mutableListOf<LatLng>()

        // Mock data snapshot for entity
        val mockDataSnapshot = mock<DataSnapshot>()
        val childSnapshot = mock<DataSnapshot>()
        val latSnapshot = mock<DataSnapshot>()
        val lngSnapshot = mock<DataSnapshot>()

        `when`(mockDataSnapshot.children).thenReturn(listOf(childSnapshot))
        `when`(childSnapshot.key).thenReturn("entity1")
        `when`(childSnapshot.child("latitude")).thenReturn(latSnapshot)
        `when`(childSnapshot.child("longitude")).thenReturn(lngSnapshot)
        `when`(latSnapshot.getValue(Double::class.java)).thenReturn(1.0)
        `when`(lngSnapshot.getValue(Double::class.java)).thenReturn(2.0)

        // When
        firebaseRepository.listenForEntities(
            testUserType,
            testDestination,
            null,
            onEntitiesUpdate = { locations -> locationsList.addAll(locations) },
            onError = { }
        )

        // Capture the listener and simulate data change
        verify(mockEntitiesReference).addValueEventListener(capture(listenerCaptor))
        listenerCaptor.value.onDataChange(mockDataSnapshot)

        // Then
        assertEquals(1, locationsList.size)
        assertEquals(1.0, locationsList[0].latitude, 0.001)
        assertEquals(2.0, locationsList[0].longitude, 0.001)
    }

    @Test
    fun `listenForEntities handles errors correctly`() = runTest {
        // Given
        val listenerCaptor = ArgumentCaptor.forClass(ValueEventListener::class.java)
        val errorMessages = mutableListOf<String>()
        val testErrorMessage = "Test database error"
        val mockError = mock<DatabaseError>()
        `when`(mockError.message).thenReturn(testErrorMessage)

        // When
        firebaseRepository.listenForEntities(
            testUserType,
            testDestination,
            null,
            onEntitiesUpdate = { },
            onError = { errorMessage -> errorMessages.add(errorMessage) }
        )

        // Capture the listener and simulate error
        verify(mockEntitiesReference).addValueEventListener(capture(listenerCaptor))
        listenerCaptor.value.onCancelled(mockError)

        // Then
        assertEquals(1, errorMessages.size)
        assertEquals(testErrorMessage, errorMessages[0])
    }

    @Test
    fun `writeUser calls setValue on correct reference`() = runTest {
        // Given
        val testUser = User(
            uid = testUserId,
            name = "Test User",
            phone = "+123456789"
        )

        // When
        firebaseRepository.writeUser(testUser)

        // Then
        verify(mockDatabaseReference).child("users")
        verify(mockUsersReference).child(testUserId)
        verify(mockUserIdReference).setValue(testUser)
    }

    @Test
    fun `readUser calls get on correct reference`() = runTest {
        // When
        firebaseRepository.readUser(testUserId)

        // Then
        verify(mockDatabaseReference).child("users")
        verify(mockUsersReference).child(testUserId)
        verify(mockUserIdReference).get()
    }

    @Test
    fun `updateUser calls updateChildren on correct reference`() = runTest {
        // Given
        val updateData = mapOf("name" to "Updated Name", "status" to "available")

        // When
        firebaseRepository.updateUser(testUserId, updateData)

        // Then
        verify(mockDatabaseReference).child("users")
        verify(mockUsersReference).child(testUserId)
        verify(mockUserIdReference).updateChildren(updateData)
    }

    @Test
    fun `deleteUser calls removeValue on correct reference`() = runTest {
        // When
        firebaseRepository.deleteUser(testUserId)

        // Then
        verify(mockDatabaseReference).child("users")
        verify(mockUsersReference).child(testUserId)
        verify(mockUserIdReference).removeValue()
    }
}