package com.tecvo.taxi.services

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

data class CityInfo(
    val cityName: String,
    val fullLocationName: String, // e.g., "Madadeni M"
    val province: String? = null
)

data class CityFilteredEntities(
    val cityName: String,
    val passengerLocations: List<LatLng> = emptyList(),
    val driverLocations: List<LatLng> = emptyList(),
    val totalPassengers: Int = 0,
    val totalDrivers: Int = 0
)

@Singleton
class CityBasedOverviewService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val geocodingService: GeocodingService
) {
    private val TAG = "CityBasedOverviewService"
    
    // Cache for location to city mappings to avoid repeated geocoding
    private val locationToCityCache = mutableMapOf<String, CityInfo>()
    
    // Current user's city info
    private val _currentUserCity = MutableStateFlow<CityInfo?>(null)
    val currentUserCity: StateFlow<CityInfo?> = _currentUserCity.asStateFlow()
    
    // Filtered entities state
    private val _filteredEntities = MutableStateFlow<CityFilteredEntities?>(null)
    val filteredEntities: StateFlow<CityFilteredEntities?> = _filteredEntities.asStateFlow()
    
    // City-based overview mode state
    private val _isCityOverviewActive = MutableStateFlow(false)
    val isCityOverviewActive: StateFlow<Boolean> = _isCityOverviewActive.asStateFlow()
    
    /**
     * Detect the city for the current user's location
     */
    suspend fun detectCurrentUserCity(userLocation: LatLng): CityInfo? = withContext(Dispatchers.IO) {
        val cacheKey = "${userLocation.latitude},${userLocation.longitude}"
        
        // Check cache first
        locationToCityCache[cacheKey]?.let { cachedCity ->
            Timber.tag(TAG).d("Using cached city info for current user: ${cachedCity.cityName}")
            _currentUserCity.value = cachedCity
            return@withContext cachedCity
        }
        
        try {
            Timber.tag(TAG).d("Detecting city for user location: ${userLocation.latitude}, ${userLocation.longitude}")
            
            // Get detailed location information from geocoding
            val locationInfo = getCityInfoFromCoordinates(userLocation.latitude, userLocation.longitude)
            
            // Cache the result
            locationToCityCache[cacheKey] = locationInfo
            _currentUserCity.value = locationInfo
            
            Timber.tag(TAG).i("Current user city detected: ${locationInfo.cityName} (${locationInfo.fullLocationName})")
            return@withContext locationInfo
            
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to detect city for user location")
            return@withContext null
        }
    }
    
    /**
     * Get detailed city information from coordinates using the existing geocoding service
     */
    private suspend fun getCityInfoFromCoordinates(latitude: Double, longitude: Double): CityInfo {
        // Use the existing geocoding service to get the place name (area level)
        val fullLocationName = geocodingService.getPlaceNameFromCoordinates(latitude, longitude)
        
        // Now we need to extract the city name from the geocoding response
        // We'll make another call to get structured address components
        val cityName = extractCityNameFromGeocoding(latitude, longitude)
        val provinceName = extractProvinceFromGeocoding(latitude, longitude)
        
        return CityInfo(
            cityName = cityName ?: "Unknown City",
            fullLocationName = fullLocationName,
            province = provinceName
        )
    }
    
    /**
     * Extract city name from geocoding response with robust South African address handling
     */
    private suspend fun extractCityNameFromGeocoding(latitude: Double, longitude: Double): String? = withContext(Dispatchers.IO) {
        try {
            val url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=$latitude,$longitude&key=${com.tecvo.taxi.BuildConfig.GEOCODING_API_KEY}"
            val request = okhttp3.Request.Builder().url(url).build()
            val client = okhttp3.OkHttpClient()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Timber.tag(TAG).w("Geocoding API request failed with code: ${response.code}")
                    return@withContext null
                }

                val jsonStr = response.body?.string() ?: return@withContext null
                val json = org.json.JSONObject(jsonStr)
                
                if (json.getString("status") != "OK") {
                    Timber.tag(TAG).w("Geocoding API returned status: ${json.getString("status")}")
                    return@withContext null
                }

                val results = json.getJSONArray("results")
                if (results.length() == 0) {
                    Timber.tag(TAG).w("No geocoding results found for coordinates: $latitude, $longitude")
                    return@withContext null
                }

                Timber.tag(TAG).d("Processing ${results.length()} geocoding results for coordinates: $latitude, $longitude")
                
                // Priority order for city detection (highest to lowest priority)
                val cityTypePriorities = listOf(
                    "locality",                    // Standard city field
                    "administrative_area_level_2", // District/metropolitan area  
                    "administrative_area_level_1", // Province (last resort)
                    "sublocality_level_1"          // Large neighborhood (very last resort)
                )
                
                val foundComponents = mutableMapOf<String, String>()
                
                // Collect all relevant components first
                for (i in 0 until results.length()) {
                    val result = results.getJSONObject(i)
                    val addressComponents = result.getJSONArray("address_components")
                    
                    Timber.tag(TAG).d("Examining result $i with ${addressComponents.length()} components")
                    
                    for (j in 0 until addressComponents.length()) {
                        val component = addressComponents.getJSONObject(j)
                        val types = component.getJSONArray("types")
                        val longName = component.getString("long_name")
                        
                        for (k in 0 until types.length()) {
                            val type = types.getString(k)
                            if (type in cityTypePriorities) {
                                foundComponents[type] = longName
                                Timber.tag(TAG).d("Found component - Type: $type, Name: $longName")
                            }
                        }
                    }
                }
                
                // Now select the best city name based on priority and South African specifics
                for (priorityType in cityTypePriorities) {
                    val componentName = foundComponents[priorityType]
                    if (componentName != null) {
                        val cityName = processSouthAfricanCityName(componentName, priorityType)
                        if (cityName != null) {
                            Timber.tag(TAG).i("Selected city name '$cityName' from type '$priorityType' (original: '$componentName')")
                            return@withContext cityName
                        }
                    }
                }
                
                Timber.tag(TAG).w("Could not extract city name from any geocoding components. Found components: $foundComponents")
                return@withContext null
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error extracting city name from geocoding for coordinates: $latitude, $longitude")
            return@withContext null
        }
    }
    
    /**
     * Process South African city names to extract the actual city from administrative areas
     */
    private fun processSouthAfricanCityName(componentName: String, componentType: String): String? {
        return when (componentType) {
            "locality" -> {
                // Direct city name - use as is
                componentName
            }
            "administrative_area_level_2" -> {
                // For South Africa, this might be "eThekwini Metropolitan Municipality" -> "Durban"
                // or "City of Cape Town" -> "Cape Town", etc.
                when {
                    componentName.contains("eThekwini", ignoreCase = true) -> "Durban"
                    componentName.contains("City of Cape Town", ignoreCase = true) -> "Cape Town"
                    componentName.contains("City of Johannesburg", ignoreCase = true) -> "Johannesburg"
                    componentName.contains("City of Tshwane", ignoreCase = true) -> "Pretoria"
                    componentName.contains("Ekurhuleni", ignoreCase = true) -> "Germiston"
                    componentName.contains("Nelson Mandela Bay", ignoreCase = true) -> "Port Elizabeth"
                    componentName.contains("Buffalo City", ignoreCase = true) -> "East London"
                    componentName.contains("Mangaung", ignoreCase = true) -> "Bloemfontein"
                    // Handle other patterns
                    componentName.contains("Metropolitan", ignoreCase = true) -> {
                        // Extract city name from "X Metropolitan Municipality"
                        val parts = componentName.split(" ")
                        if (parts.isNotEmpty()) parts[0] else componentName
                    }
                    componentName.contains("Municipality", ignoreCase = true) -> {
                        // Extract from "X Local Municipality" or "X Municipality"
                        componentName.replace(" Local Municipality", "")
                                   .replace(" Metropolitan Municipality", "")
                                   .replace(" Municipality", "")
                                   .trim()
                    }
                    else -> componentName
                }
            }
            "administrative_area_level_1" -> {
                // Province level - only use if no better option
                when (componentName) {
                    "Gauteng" -> "Johannesburg" // Default major city
                    "Western Cape" -> "Cape Town"
                    "KwaZulu-Natal" -> "Durban"
                    "Eastern Cape" -> "Port Elizabeth"
                    "Free State" -> "Bloemfontein"
                    else -> null // Don't use provinces we don't recognize
                }
            }
            "sublocality_level_1" -> {
                // Large neighborhood - only use as absolute last resort
                // and only if it looks like a major area
                if (componentName.length > 3 && !componentName.contains("suburb", ignoreCase = true)) {
                    componentName
                } else {
                    null
                }
            }
            else -> componentName
        }
    }
    
    /**
     * Extract province name from geocoding response
     */
    private suspend fun extractProvinceFromGeocoding(latitude: Double, longitude: Double): String? = withContext(Dispatchers.IO) {
        try {
            val url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=$latitude,$longitude&key=${com.tecvo.taxi.BuildConfig.GEOCODING_API_KEY}"
            val request = okhttp3.Request.Builder().url(url).build()
            val client = okhttp3.OkHttpClient()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null

                val jsonStr = response.body?.string() ?: return@withContext null
                val json = org.json.JSONObject(jsonStr)
                
                if (json.getString("status") != "OK") return@withContext null

                val results = json.getJSONArray("results")
                if (results.length() == 0) return@withContext null

                // Look for administrative_area_level_1 (province)
                for (i in 0 until results.length()) {
                    val result = results.getJSONObject(i)
                    val addressComponents = result.getJSONArray("address_components")
                    
                    for (j in 0 until addressComponents.length()) {
                        val component = addressComponents.getJSONObject(j)
                        val types = component.getJSONArray("types")
                        
                        for (k in 0 until types.length()) {
                            val type = types.getString(k)
                            if (type == "administrative_area_level_1") {
                                val provinceName = component.getString("long_name")
                                Timber.tag(TAG).d("Found province name from geocoding: $provinceName")
                                return@withContext provinceName
                            }
                        }
                    }
                }
                
                return@withContext null
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error extracting province from geocoding")
            return@withContext null
        }
    }
    
    /**
     * Toggle city-based overview mode
     */
    fun toggleCityOverviewMode() {
        val newState = !_isCityOverviewActive.value
        _isCityOverviewActive.value = newState
        
        if (newState) {
            Timber.tag(TAG).i("City-based overview mode activated")
        } else {
            Timber.tag(TAG).i("City-based overview mode deactivated")
            // Clear filtered data when deactivating
            _filteredEntities.value = null
        }
    }
    
    /**
     * Filter entities by city and destination - only show users in the same city AND same destination as current user
     */
    suspend fun filterEntitiesByCityAndDestination(
        allPassengerLocations: List<LatLng>,
        allDriverLocations: List<LatLng>,
        currentUserLocation: LatLng,
        destination: String
    ): CityFilteredEntities? = withContext(Dispatchers.IO) {
        
        // First ensure we know the current user's city
        val userCityInfo = _currentUserCity.value ?: detectCurrentUserCity(currentUserLocation)
        
        if (userCityInfo == null) {
            Timber.tag(TAG).w("Cannot filter entities - user city unknown")
            return@withContext null
        }
        
        Timber.tag(TAG).d("Filtering entities for city: ${userCityInfo.cityName} and destination: $destination")
        
        try {
            // NOTE: The entities passed to this method are assumed to already be filtered by destination
            // at the MapScreen/ViewModel level since they are routed based on destination selection
            
            // Filter passengers in the same city (already destination-filtered)
            val sameCityPassengers = filterLocationsByCity(allPassengerLocations, userCityInfo.cityName)
            
            // Filter drivers in the same city (already destination-filtered)  
            val sameCityDrivers = filterLocationsByCity(allDriverLocations, userCityInfo.cityName)
            
            val result = CityFilteredEntities(
                cityName = userCityInfo.cityName,
                passengerLocations = sameCityPassengers,
                driverLocations = sameCityDrivers,
                totalPassengers = sameCityPassengers.size,
                totalDrivers = sameCityDrivers.size
            )
            
            _filteredEntities.value = result
            
            Timber.tag(TAG).i("City + destination filtering complete for ${userCityInfo.cityName} ($destination): ${sameCityPassengers.size} passengers, ${sameCityDrivers.size} drivers")
            return@withContext result
            
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error filtering entities by city and destination")
            return@withContext null
        }
    }
    
    /**
     * Filter entities by city - only show users in the same city as current user
     * @deprecated Use filterEntitiesByCityAndDestination for proper destination-aware filtering
     */
    suspend fun filterEntitiesByCity(
        allPassengerLocations: List<LatLng>,
        allDriverLocations: List<LatLng>,
        currentUserLocation: LatLng
    ): CityFilteredEntities? = withContext(Dispatchers.IO) {
        
        // First ensure we know the current user's city
        val userCityInfo = _currentUserCity.value ?: detectCurrentUserCity(currentUserLocation)
        
        if (userCityInfo == null) {
            Timber.tag(TAG).w("Cannot filter entities - user city unknown")
            return@withContext null
        }
        
        Timber.tag(TAG).d("Filtering entities for city: ${userCityInfo.cityName}")
        
        try {
            // Filter passengers in the same city
            val sameCityPassengers = filterLocationsByCity(allPassengerLocations, userCityInfo.cityName)
            
            // Filter drivers in the same city
            val sameCityDrivers = filterLocationsByCity(allDriverLocations, userCityInfo.cityName)
            
            val result = CityFilteredEntities(
                cityName = userCityInfo.cityName,
                passengerLocations = sameCityPassengers,
                driverLocations = sameCityDrivers,
                totalPassengers = sameCityPassengers.size,
                totalDrivers = sameCityDrivers.size
            )
            
            _filteredEntities.value = result
            
            Timber.tag(TAG).i("City filtering complete for ${userCityInfo.cityName}: ${sameCityPassengers.size} passengers, ${sameCityDrivers.size} drivers")
            return@withContext result
            
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error filtering entities by city")
            return@withContext null
        }
    }
    
    /**
     * Filter a list of locations to only include those in the specified city
     */
    private suspend fun filterLocationsByCity(locations: List<LatLng>, targetCityName: String): List<LatLng> = withContext(Dispatchers.IO) {
        val filteredLocations = mutableListOf<LatLng>()
        
        // Process locations in batches to avoid overwhelming the geocoding API
        val batchSize = 10
        val batches = locations.chunked(batchSize)
        
        for (batch in batches) {
            for (location in batch) {
                try {
                    val cacheKey = "${location.latitude},${location.longitude}"
                    
                    // Check cache first
                    val cachedCityInfo = locationToCityCache[cacheKey]
                    if (cachedCityInfo != null) {
                        if (cachedCityInfo.cityName.equals(targetCityName, ignoreCase = true)) {
                            filteredLocations.add(location)
                        }
                        continue
                    }
                    
                    // Get city for this location
                    val cityName = extractCityNameFromGeocoding(location.latitude, location.longitude)
                    if (cityName != null) {
                        // Cache the result
                        val cityInfo = CityInfo(cityName = cityName, fullLocationName = "")
                        locationToCityCache[cacheKey] = cityInfo
                        
                        // Check if it matches target city
                        if (cityName.equals(targetCityName, ignoreCase = true)) {
                            filteredLocations.add(location)
                        }
                    }
                    
                    // Small delay to respect geocoding API rate limits
                    kotlinx.coroutines.delay(50)
                    
                } catch (e: Exception) {
                    Timber.tag(TAG).w(e, "Failed to get city for location ${location.latitude}, ${location.longitude}")
                }
            }
        }
        
        return@withContext filteredLocations
    }
    
    /**
     * Clear all cached data
     */
    fun clearCache() {
        locationToCityCache.clear()
        _currentUserCity.value = null
        _filteredEntities.value = null
        Timber.tag(TAG).d("Cache cleared")
    }
    
    /**
     * Get cache statistics for monitoring
     */
    fun getCacheStats(): Map<String, Any> {
        return mapOf(
            "cacheSize" to locationToCityCache.size,
            "currentUserCity" to (_currentUserCity.value?.cityName ?: "Unknown"),
            "isOverviewActive" to _isCityOverviewActive.value
        )
    }
}