package com.tecvo.taxi.services

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.reflect.Method

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29], application = HiltTestApplication::class)
class CityBasedOverviewServiceTest {
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Mock
    private lateinit var mockContext: Context
    
    @Mock  
    private lateinit var mockGeocodingService: GeocodingService
    
    private lateinit var cityBasedOverviewService: CityBasedOverviewService
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        // Setup is simplified - network calls will be mocked at service level
        
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        cityBasedOverviewService = CityBasedOverviewService(appContext, mockGeocodingService)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        // No cleanup needed for simplified test
    }
    
    @Test
    fun `processSouthAfricanCityName handles eThekwini correctly`() {
        // Given
        val method = getPrivateMethod("processSouthAfricanCityName")
        
        // When
        val result = method.invoke(cityBasedOverviewService, "eThekwini Metropolitan Municipality", "administrative_area_level_2") as String?
        
        // Then
        assertEquals("Should convert eThekwini to Durban", "Durban", result)
    }
    
    @Test
    fun `processSouthAfricanCityName handles City of Cape Town correctly`() {
        // Given
        val method = getPrivateMethod("processSouthAfricanCityName")
        
        // When
        val result = method.invoke(cityBasedOverviewService, "City of Cape Town", "administrative_area_level_2") as String?
        
        // Then
        assertEquals("Should convert City of Cape Town to Cape Town", "Cape Town", result)
    }
    
    @Test
    fun `processSouthAfricanCityName handles City of Johannesburg correctly`() {
        // Given
        val method = getPrivateMethod("processSouthAfricanCityName")
        
        // When
        val result =
            method.invoke(cityBasedOverviewService, "City of Johannesburg Metropolitan Municipality", "administrative_area_level_2") as String?
        
        // Then
        assertEquals("Should convert City of Johannesburg to Johannesburg", "Johannesburg", result)
    }
    
    @Test
    fun `processSouthAfricanCityName handles locality type directly`() {
        // Given
        val method = getPrivateMethod("processSouthAfricanCityName")
        
        // When
        val result = method.invoke(cityBasedOverviewService, "Durban", "locality") as String?
        
        // Then
        assertEquals("Should use locality name directly", "Durban", result)
    }
    
    @Test
    fun `processSouthAfricanCityName handles KwaZulu-Natal province fallback`() {
        // Given
        val method = getPrivateMethod("processSouthAfricanCityName")
        
        // When
        val result = method.invoke(cityBasedOverviewService, "KwaZulu-Natal", "administrative_area_level_1") as String?
        
        // Then
        assertEquals("Should convert KwaZulu-Natal province to Durban", "Durban", result)
    }
    
    @Test
    fun `processSouthAfricanCityName handles Gauteng province fallback`() {
        // Given
        val method = getPrivateMethod("processSouthAfricanCityName")
        
        // When
        val result = method.invoke(cityBasedOverviewService, "Gauteng", "administrative_area_level_1") as String?
        
        // Then
        assertEquals("Should convert Gauteng province to Johannesburg", "Johannesburg", result)
    }
    
    @Test
    fun `processSouthAfricanCityName handles Western Cape province fallback`() {
        // Given
        val method = getPrivateMethod("processSouthAfricanCityName")
        
        // When
        val result = method.invoke(cityBasedOverviewService, "Western Cape", "administrative_area_level_1") as String?
        
        // Then
        assertEquals("Should convert Western Cape province to Cape Town", "Cape Town", result)
    }
    
    @Test
    fun `processSouthAfricanCityName handles generic Municipality pattern`() {
        // Given
        val method = getPrivateMethod("processSouthAfricanCityName")
        
        // When
        val result = method.invoke(cityBasedOverviewService, "Stellenbosch Local Municipality", "administrative_area_level_2") as String?
        
        // Then
        assertEquals("Should extract city name from municipality", "Stellenbosch", result)
    }
    
    @Test
    fun `processSouthAfricanCityName handles Metropolitan Municipality pattern`() {
        // Given
        val method = getPrivateMethod("processSouthAfricanCityName")
        
        // When
        val result = method.invoke(cityBasedOverviewService, "Buffalo City Metropolitan Municipality", "administrative_area_level_2") as String?
        
        // Then
        assertEquals("Should convert Buffalo City Metropolitan Municipality to East London", "East London", result)
    }
    
    @Test
    fun `processSouthAfricanCityName rejects short sublocality names`() {
        // Given
        val method = getPrivateMethod("processSouthAfricanCityName")
        
        // When
        val result = method.invoke(cityBasedOverviewService, "A", "sublocality_level_1") as String?
        
        // Then
        assertNull("Should reject very short sublocality names", result)
    }
    
    @Test
    fun `processSouthAfricanCityName rejects suburb sublocality names`() {
        // Given
        val method = getPrivateMethod("processSouthAfricanCityName")
        
        // When
        val result = method.invoke(cityBasedOverviewService, "Rosebank Suburb", "sublocality_level_1") as String?
        
        // Then
        assertNull("Should reject suburb-named sublocalities", result)
    }
    
    @Test
    fun `processSouthAfricanCityName accepts valid sublocality names`() {
        // Given
        val method = getPrivateMethod("processSouthAfricanCityName")
        
        // When
        val result = method.invoke(cityBasedOverviewService, "Sandton", "sublocality_level_1") as String?
        
        // Then
        assertEquals("Should accept valid sublocality names", "Sandton", result)
    }
    
    @Test
    fun `detectCurrentUserCity handles network failure gracefully`() = runTest {
        // Given
        val testLocation = LatLng(-29.8587, 31.0218) // Durban coordinates
        `when`(mockGeocodingService.getPlaceNameFromCoordinates(-29.8587, 31.0218))
            .thenThrow(RuntimeException("Network error"))
        
        // Network errors will be handled by the service's error handling
        
        // When
        val result = cityBasedOverviewService.detectCurrentUserCity(testLocation)
        
        // Then - Should handle errors gracefully and return null
        assertNull("Should return null on API failure", result)
    }
    
    @Test
    fun `toggleCityOverviewMode changes state correctly`() {
        // Given - initial state should be false
        assertFalse("Initial overview mode should be false", cityBasedOverviewService.isCityOverviewActive.value)
        
        // When - toggle once
        cityBasedOverviewService.toggleCityOverviewMode()
        
        // Then - should be true
        assertTrue("Overview mode should be true after first toggle", cityBasedOverviewService.isCityOverviewActive.value)
        
        // When - toggle again
        cityBasedOverviewService.toggleCityOverviewMode()
        
        // Then - should be false again
        assertFalse("Overview mode should be false after second toggle", cityBasedOverviewService.isCityOverviewActive.value)
    }
    
    @Test
    fun `clearCache clears cached data but preserves overview mode`() {
        // Given - some state exists
        cityBasedOverviewService.toggleCityOverviewMode() // Enable overview mode
        
        // When
        cityBasedOverviewService.clearCache()
        
        // Then
        assertTrue("Overview mode should still be active", cityBasedOverviewService.isCityOverviewActive.value)
        assertNull("Current user city should be cleared", cityBasedOverviewService.currentUserCity.value)
        assertNull("Filtered entities should be cleared", cityBasedOverviewService.filteredEntities.value)
    }
    
    @Test
    fun `getCacheStats returns correct information`() {
        // Given
        cityBasedOverviewService.toggleCityOverviewMode() // Enable overview mode
        
        // When  
        val stats = cityBasedOverviewService.getCacheStats()
        
        // Then
        assertNotNull("Cache stats should not be null", stats)
        assertTrue("Should contain cache size", stats.containsKey("cacheSize"))
        assertTrue("Should contain current user city", stats.containsKey("currentUserCity"))
        assertTrue("Should contain overview active state", stats.containsKey("isOverviewActive"))
        assertEquals("Overview active should be true", true, stats["isOverviewActive"])
    }
    
    // Helper method to access private methods for testing
    private fun getPrivateMethod(methodName: String): Method {
        val method = CityBasedOverviewService::class.java.getDeclaredMethod(
            methodName, 
            String::class.java, 
            String::class.java
        )
        method.isAccessible = true
        return method
    }
    
    companion object {
        // Real South African coordinates for integration testing
        val NTUZUMA_COORDINATES = LatLng(-29.7292, 30.8648)        // Ntuzuma, should return Durban
        val SANDTON_COORDINATES = LatLng(-26.1076, 28.0567)       // Sandton, should return Johannesburg
        val CAPE_TOWN_COORDINATES = LatLng(-33.9249, 18.4241)     // Cape Town CBD
        val PRETORIA_COORDINATES = LatLng(-25.7479, 28.2293)      // Pretoria CBD
        val PORT_ELIZABETH_COORDINATES = LatLng(-33.9608, 25.6022) // Port Elizabeth CBD
    }
}