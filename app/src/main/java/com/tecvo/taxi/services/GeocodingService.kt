// Update the existing GeocodingService.kt
package com.tecvo.taxi.services

import android.content.Context
import com.tecvo.taxi.BuildConfig
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import timber.log.Timber
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeocodingService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client = OkHttpClient()
    private val TAG = "GeocodingService"

    // Cache boundary results to avoid repeated API calls
    private val boundaryCache = mutableMapOf<String, List<LatLng>>()
    private val localAreasCache = mutableMapOf<String, List<String>>()

    /** Get boundary points for an area by name */
    suspend fun getBoundaryPoints(areaName: String, cityName: String? = null): List<LatLng> = withContext(Dispatchers.IO) {
        // Check cache first
        val cacheKey = if (cityName != null) "$areaName,$cityName" else areaName
        boundaryCache[cacheKey]?.let {
            Timber.d("Using cached boundary for $cacheKey")
            return@withContext it
        }

        try {
            // Build the search query
            val query = if (cityName != null) "$areaName, $cityName, South Africa" else "$areaName, South Africa"
            val encodedQuery = URLEncoder.encode(query, "UTF-8")

            // Geocoding API request
            val url = "https://maps.googleapis.com/maps/api/geocode/json?address=$encodedQuery&key=${BuildConfig.GEOCODING_API_KEY}"
            val request = Request.Builder().url(url).build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw Exception("Geocoding request failed: ${response.message}")

                val jsonStr = response.body?.string() ?: throw Exception("Empty response body")
                val json = JSONObject(jsonStr)

                if (json.getString("status") != "OK") {
                    throw Exception("Geocoding API error: ${json.getString("status")}")
                }

                val results = json.getJSONArray("results")
                if (results.length() == 0) throw Exception("No results found for $query")

                val result = results.getJSONObject(0)

                // Try to get boundary from result
                val bounds = try {
                    result.getJSONObject("geometry").getJSONObject("bounds")
                } catch (e: Exception) {
                    // If no bounds, use viewport
                    result.getJSONObject("geometry").getJSONObject("viewport")
                }

                val northeast = bounds.getJSONObject("northeast")
                val southwest = bounds.getJSONObject("southwest")

                // Calculate center
                val centerLat = (northeast.getDouble("lat") + southwest.getDouble("lat")) / 2
                val centerLng = (northeast.getDouble("lng") + southwest.getDouble("lng")) / 2

                // Get width and height
                val latSpan = northeast.getDouble("lat") - southwest.getDouble("lat")
                val lngSpan = northeast.getDouble("lng") - southwest.getDouble("lng")

                // Create a more detailed boundary (octagon shape)
                val boundaryPoints = mutableListOf<LatLng>()

                // Add 8 points around the boundary
                boundaryPoints.add(LatLng(northeast.getDouble("lat"), northeast.getDouble("lng")))
                boundaryPoints.add(LatLng(centerLat + latSpan/2, centerLng + lngSpan*0.2))
                boundaryPoints.add(LatLng(centerLat + latSpan*0.2, centerLng + lngSpan/2))
                boundaryPoints.add(LatLng(southwest.getDouble("lat"), northeast.getDouble("lng")))
                boundaryPoints.add(LatLng(southwest.getDouble("lat"), southwest.getDouble("lng")))
                boundaryPoints.add(LatLng(centerLat - latSpan/2, centerLng - lngSpan*0.2))
                boundaryPoints.add(LatLng(centerLat - latSpan*0.2, centerLng - lngSpan/2))
                boundaryPoints.add(LatLng(northeast.getDouble("lat"), southwest.getDouble("lng")))
                boundaryPoints.add(LatLng(northeast.getDouble("lat"), northeast.getDouble("lng"))) // Close the polygon

                // Cache the result
                boundaryCache[cacheKey] = boundaryPoints

                return@withContext boundaryPoints
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching boundaries for $areaName")
            // Return fallback boundaries centered at Johannesburg
            return@withContext createFallbackBoundary()
        }
    }

    /** Get local areas within a city */
    suspend fun getLocalAreas(cityName: String): List<String> = withContext(Dispatchers.IO) {
        // Check cache first
        localAreasCache[cityName]?.let {
            Timber.d("Using cached local areas for $cityName")
            return@withContext it
        }

        try {
            // For this implementation, we'll use a simplified approach with Geocoding API
            // In a real app, you would use Places API for better results
            val encodedQuery = URLEncoder.encode("neighborhoods in $cityName, South Africa", "UTF-8")
            val url = "https://maps.googleapis.com/maps/api/geocode/json?address=$encodedQuery&key=${BuildConfig.GEOCODING_API_KEY}"

            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw Exception("Local areas request failed: ${response.message}")

                val jsonStr = response.body?.string() ?: throw Exception("Empty response body")
                val json = JSONObject(jsonStr)

                // For demonstration, we'll create mock local areas based on city name
                // In a real app, you would parse the actual API response
                val mockLocalAreas = mutableListOf<String>()
                val suffixes = listOf("North", "South", "East", "West", "Central", "Heights", "Gardens", "Park")

                suffixes.forEach { suffix ->
                    mockLocalAreas.add("$cityName $suffix")
                }

                // Cache the result
                localAreasCache[cityName] = mockLocalAreas

                return@withContext mockLocalAreas
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching local areas for $cityName")
            return@withContext emptyList()
        }
    }

    /** Check if a location is within a boundary */
    fun isLocationInBoundary(location: LatLng, boundary: List<LatLng>): Boolean {
        return PolyUtil.containsLocation(location, boundary, true)
    }

    /** Count entities within a boundary */
    fun countEntitiesInBoundary(entities: List<LatLng>, boundary: List<LatLng>): Int {
        return entities.count { isLocationInBoundary(it, boundary) }
    }

    /** Get place name from coordinates using reverse geocoding with dual API key fallback */
    suspend fun getPlaceNameFromCoordinates(latitude: Double, longitude: Double): String = withContext(Dispatchers.IO) {
        // Use real geocoding API to get actual area names from coordinates
        Timber.d("Getting real area name for coordinates: $latitude, $longitude")
        
        // Try primary API key first
        try {
            return@withContext performGeocoding(latitude, longitude, BuildConfig.GEOCODING_API_KEY, "primary")
        } catch (primaryException: Exception) {
            Timber.w(primaryException, "Primary geocoding API key failed, trying secondary key")
            
            // Try secondary API key if primary fails
            try {
                return@withContext performGeocoding(latitude, longitude, BuildConfig.GEOCODING_API_KEY_SECONDARY, "secondary")
            } catch (secondaryException: Exception) {
                Timber.e(secondaryException, "Both geocoding API keys failed for coordinates: $latitude, $longitude")
                
                // Log both errors for debugging
                Timber.e("Primary API error: ${primaryException.message}")
                Timber.e("Secondary API error: ${secondaryException.message}")
                
                // Return original error message when geocoding fails
                return@withContext "Unable to determine area (API error)"
            }
        }
    }
    
    /** Helper method to perform geocoding with a specific API key */
    private suspend fun performGeocoding(latitude: Double, longitude: Double, apiKey: String, keyType: String): String {
        val url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=$latitude,$longitude&key=$apiKey"
        Timber.d("Making $keyType geocoding request for: $latitude, $longitude")
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorMessage = "Geocoding request failed with status: ${response.code} - ${response.message}"
                Timber.e(errorMessage)
                throw Exception("$keyType geocoding request failed: ${response.message}")
            }

            val jsonStr = response.body?.string() ?: throw Exception("Empty response body")
            val json = JSONObject(jsonStr)
            val status = json.getString("status")
            
            Timber.d("$keyType Geocoding API status: $status")
            
            // Handle different error statuses
            when (status) {
                "OK" -> {
                    // Success - continue processing
                }
                "OVER_QUERY_LIMIT" -> {
                    throw Exception("$keyType API key has exceeded quota limit")
                }
                "REQUEST_DENIED" -> {
                    throw Exception("$keyType API key request denied - check key validity")
                }
                "INVALID_REQUEST" -> {
                    throw Exception("$keyType API invalid request parameters")
                }
                "ZERO_RESULTS" -> {
                    throw Exception("$keyType API found no results for these coordinates")
                }
                else -> {
                    throw Exception("$keyType API returned error status: $status")
                }
            }

            val results = json.getJSONArray("results")
            if (results.length() == 0) throw Exception("No results found for coordinates")

            // Look for the most specific address component with priority order
            for (i in 0 until results.length()) {
                val result = results.getJSONObject(i)
                val addressComponents = result.getJSONArray("address_components")
                
                // DEBUG: Log all available address components (only for first few requests to avoid spam)
                if (i == 0) {
                    Timber.d("=== $keyType API ADDRESS COMPONENTS for $latitude, $longitude ===")
                    for (j in 0 until addressComponents.length()) {
                        val component = addressComponents.getJSONObject(j)
                        val types = component.getJSONArray("types")
                        val longName = component.getString("long_name")
                        val shortName = component.getString("short_name")
                        val typesStr = (0 until types.length()).map { types.getString(it) }.joinToString(", ")
                        Timber.d("Component: '$longName' ($shortName) - Types: [$typesStr]")
                    }
                    Timber.d("=== END $keyType API ADDRESS COMPONENTS ===")
                }
                
                // First priority: Try to find neighborhood (most specific)
                for (j in 0 until addressComponents.length()) {
                    val component = addressComponents.getJSONObject(j)
                    val types = component.getJSONArray("types")
                    
                    for (k in 0 until types.length()) {
                        val type = types.getString(k)
                        if (type == "neighborhood") {
                            val placeName = component.getString("long_name")
                            Timber.d("Found neighborhood via $keyType API: $placeName for coordinates: $latitude, $longitude")
                            return placeName
                        }
                    }
                }
                
                // Second priority: Try to find sublocality_level_1 (section level)
                for (j in 0 until addressComponents.length()) {
                    val component = addressComponents.getJSONObject(j)
                    val types = component.getJSONArray("types")
                    
                    for (k in 0 until types.length()) {
                        val type = types.getString(k)
                        if (type == "sublocality_level_1") {
                            val placeName = component.getString("long_name")
                            Timber.d("Found sublocality_level_1 via $keyType API: $placeName for coordinates: $latitude, $longitude")
                            return placeName
                        }
                    }
                }
                
                // Third priority: Try to find sublocality (broader area)
                for (j in 0 until addressComponents.length()) {
                    val component = addressComponents.getJSONObject(j)
                    val types = component.getJSONArray("types")
                    
                    for (k in 0 until types.length()) {
                        val type = types.getString(k)
                        if (type == "sublocality") {
                            val placeName = component.getString("long_name")
                            Timber.d("Found sublocality via $keyType API: $placeName for coordinates: $latitude, $longitude")
                            return placeName
                        }
                    }
                }
                
                // Final fallback: Try to find locality (city level)
                for (j in 0 until addressComponents.length()) {
                    val component = addressComponents.getJSONObject(j)
                    val types = component.getJSONArray("types")
                    
                    for (k in 0 until types.length()) {
                        val type = types.getString(k)
                        if (type == "locality") {
                            val placeName = component.getString("long_name")
                            Timber.d("Found locality via $keyType API: $placeName for coordinates: $latitude, $longitude")
                            return placeName
                        }
                    }
                }
            }
            
            // Fallback to formatted address or first result
            val firstResult = results.getJSONObject(0)
            val formattedAddress = firstResult.getString("formatted_address")
            
            // Extract the first part of the address (usually the most specific location)
            val addressParts = formattedAddress.split(",")
            val specificLocation = if (addressParts.isNotEmpty()) {
                addressParts[0].trim()
            } else {
                "Area not found"
            }
            
            Timber.d("Using formatted address part via $keyType API: $specificLocation for coordinates: $latitude, $longitude")
            return specificLocation
        }
    }

    /** Create a fallback boundary if API request fails */
    private fun createFallbackBoundary(): List<LatLng> {
        // Default to Johannesburg center
        val centerLat = -26.2041
        val centerLng = 28.0473
        val offset = 0.05

        return listOf(
            LatLng(centerLat + offset, centerLng + offset),
            LatLng(centerLat + offset, centerLng - offset),
            LatLng(centerLat - offset, centerLng - offset),
            LatLng(centerLat - offset, centerLng + offset),
            LatLng(centerLat + offset, centerLng + offset)
        )
    }
}