package repository

import android.content.Context
import com.example.taxi.services.ErrorHandlingService
import com.example.taxi.services.ErrorHandlingService.AppError
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.User
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Repository class that handles all Firebase database operations,
 * providing a clean API for ViewModels to interact with Firebase.
 */
@Singleton
class FirebaseRepository @Inject constructor(
    private val database: FirebaseDatabase,
    private val errorHandlingService: ErrorHandlingService,
    @ApplicationContext private val context: Context
) {
    private val tag = "FirebaseRepository"
    
    // Performance optimization: cache frequently accessed data
    private var cachedNotificationRadius: Double? = null
    private var lastRadiusFetch: Long = 0L
    private val radiusCacheTimeoutMs = 60000L // 1 minute cache

    /**
     * Get the notification radius from shared preferences with caching optimization
     */
    fun getNotificationRadius(): Double {
        val currentTime = System.currentTimeMillis()
        
        // Return cached value if it's still valid
        if (cachedNotificationRadius != null && currentTime - lastRadiusFetch < radiusCacheTimeoutMs) {
            return cachedNotificationRadius!!
        }
        
        val sharedPrefs = context.getSharedPreferences("taxi_app_prefs", Context.MODE_PRIVATE)
        val radius = sharedPrefs.getFloat("notification_radius_km", 2.0f).toDouble()
        
        // Cache the result
        cachedNotificationRadius = radius
        lastRadiusFetch = currentTime
        
        return radius
    }

    /**
     * Listen for entity location updates with performance optimizations
     */
    fun listenForEntities(
        userType: String,
        destination: String,
        excludeUserId: String?,
        onEntitiesUpdate: (List<LatLng>) -> Unit,
        onError: (String) -> Unit
    ): ValueEventListener {
        Timber.tag(tag).d("Setting up listener for $userType entities at $destination")
        val entitiesRef = database.reference.child("${userType}s/$destination")
        
        // Performance optimization: cache last known locations to detect changes
        var lastLocations: List<LatLng>? = null
        var lastUpdateTime = 0L
        val updateThrottleMs = 2000L // 2 second throttle for updates
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val currentTime = System.currentTimeMillis()
                    
                    // Performance optimization: throttle rapid updates
                    if (currentTime - lastUpdateTime < updateThrottleMs) {
                        Timber.tag(tag).d("Firebase update throttled - too frequent")
                        return
                    }
                    
                    Timber.tag(tag)
                        .v("Received data update for ${userType}s: ${snapshot.childrenCount} records")
                    
                    // Process each entity with better error handling
                    val locations = mutableListOf<LatLng>()
                    var errorCount = 0
                    
                    for (child in snapshot.children) {
                        // Skip self if ID is provided
                        if (excludeUserId != null && child.key == excludeUserId) {
                            continue
                        }
                        
                        try {
                            val lat = child.child("latitude").getValue(Double::class.java)
                            val lng = child.child("longitude").getValue(Double::class.java)
                            
                            if (lat != null && lng != null && 
                                lat >= -90.0 && lat <= 90.0 && 
                                lng >= -180.0 && lng <= 180.0) {
                                locations.add(LatLng(lat, lng))
                            }
                        } catch (e: Exception) {
                            errorCount++
                            if (errorCount <= 3) { // Only log first 3 errors to avoid spam
                                val error = AppError.DatabaseError(
                                    e,
                                    "Error parsing location for ${child.key}"
                                )
                                errorHandlingService.logError(error, tag)
                            }
                        }
                    }
                    
                    // Performance optimization: only call callback if data has actually changed
                    if (lastLocations != locations) {
                        lastLocations = locations.toList() // Create defensive copy
                        lastUpdateTime = currentTime
                        onEntitiesUpdate(locations)
                    } else {
                        Timber.tag(tag).d("Firebase data unchanged - skipping callback")
                    }
                    
                } catch (e: Exception) {
                    val error = AppError.DatabaseError(
                        e,
                        "Error processing entity data"
                    )
                    errorHandlingService.logError(error, tag)
                    onError(e.message ?: "Error processing data")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.tag(tag).e("Firebase: Error fetching $userType data - ${error.message}")
                onError(error.message)
            }
        }
        // Add the listener to Firebase
        entitiesRef.addValueEventListener(listener)
        return listener
    }

    /**
     * Writes a User object to the Realtime Database under the "users" node.
     */
    suspend fun writeUser(user: User): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            suspendCoroutine { continuation ->
                database.reference.child("users").child(user.uid).setValue(user)
                    .addOnSuccessListener {
                        continuation.resume(Result.success(Unit))
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(Result.failure(exception))
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Reads a User object from the Realtime Database based on UID.
     */
    suspend fun readUser(uid: String): Result<User?> = withContext(Dispatchers.IO) {
        return@withContext try {
            suspendCoroutine { continuation ->
                database.reference.child("users").child(uid).get()
                    .addOnSuccessListener { snapshot ->
                        val user = snapshot.getValue(User::class.java)
                        continuation.resume(Result.success(user))
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(Result.failure(exception))
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates a User's information in the Realtime Database.
     */
    suspend fun updateUser(uid: String, updatedData: Map<String, Any>): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            suspendCoroutine { continuation ->
                database.reference.child("users").child(uid).updateChildren(updatedData)
                    .addOnSuccessListener {
                        continuation.resume(Result.success(Unit))
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(Result.failure(exception))
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a User from the Realtime Database.
     */
    suspend fun deleteUser(uid: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            suspendCoroutine { continuation ->
                database.reference.child("users").child(uid).removeValue()
                    .addOnSuccessListener {
                        continuation.resume(Result.success(Unit))
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(Result.failure(exception))
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}