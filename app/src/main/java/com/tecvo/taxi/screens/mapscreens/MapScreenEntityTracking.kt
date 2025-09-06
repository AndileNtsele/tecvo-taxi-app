package com.tecvo.taxi.screens.mapscreens
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber
import javax.inject.Inject
import com.tecvo.taxi.BuildConfig
import com.tecvo.taxi.constants.AppConstants
import com.tecvo.taxi.R
import java.lang.ref.WeakReference

private const val TAG = "MapScreenEntity"
// Update MapScreenEntityTracking.kt
class MapScreenEntityTracker @Inject constructor(
    private val context: Context,
    private val locationTracker: MapScreenLocationTracker
) {
    // Performance optimization: Use WeakReference for bitmap cache to allow GC when needed
    private val bitmapCache = mutableMapOf<String, WeakReference<BitmapDescriptor>>()
    private val maxCacheSize = 15 // Reduced cache size to prevent memory issues
    /**
     * Creates a bitmap for marker clusters
     */
    fun createClusterBitmap(count: Int, size: Dp, density: Float): BitmapDescriptor {
        val cacheKey = "cluster_${count}_${size.value}_${density}"
        
        // Performance optimization: return cached cluster bitmap if available and not GC'd
        bitmapCache[cacheKey]?.get()?.let { cachedBitmap ->
            return cachedBitmap
        }
        // Clean up null references
        bitmapCache[cacheKey]?.get() ?: bitmapCache.remove(cacheKey)
        
        val sizePx = (size.value * density).toInt()
        val bitmap = createBitmap(sizePx, sizePx)
        val canvas = Canvas(bitmap)
// Create a paint for the circle background
        val backgroundPaint = Paint().apply {
            color = Color.rgb(33, 150, 243) // Blue color
            isAntiAlias = true
            style = Paint.Style.FILL
        }
// Create a paint for the border
        val borderPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = sizePx * 0.08f
        }
// Create a paint for the text
        val textPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            textSize = sizePx * 0.4f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
// Calculate center and radius
        val centerX = sizePx / 2f
        val centerY = sizePx / 2f
        val radius = sizePx / 2f * 0.85f
// Draw circle background
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint)
// Draw border
        canvas.drawCircle(centerX, centerY, radius, borderPaint)
// Draw text
        val countText = count.toString()
        val textHeight = textPaint.descent() - textPaint.ascent()
        val textOffset = textHeight / 2 - textPaint.descent()
        canvas.drawText(countText, centerX, centerY + textOffset, textPaint)
        
        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
        
        // Cache the cluster bitmap for reuse with WeakReference
        if (bitmapCache.size >= maxCacheSize) {
            // Remove entries with null references first
            val nullKeys = bitmapCache.entries.filter { it.value.get() == null }.map { it.key }
            nullKeys.forEach { bitmapCache.remove(it) }
            
            // If still over limit, remove oldest
            if (bitmapCache.size >= maxCacheSize) {
                val firstKey = bitmapCache.keys.first()
                bitmapCache.remove(firstKey)
            }
        }
        bitmapCache[cacheKey] = WeakReference(bitmapDescriptor)
        
        return bitmapDescriptor
    }
    /**
     * Creates a marker bitmap from a resource with caching for performance
     */
    fun createMarkerBitmap(resourceId: Int, size: Dp, density: Float): BitmapDescriptor {
        val cacheKey = "${resourceId}_${size.value}_${density}"
        
        // Performance optimization: return cached bitmap if available and not GC'd
        bitmapCache[cacheKey]?.get()?.let { cachedBitmap ->
            return cachedBitmap
        }
        // Clean up null references
        bitmapCache[cacheKey]?.get() ?: bitmapCache.remove(cacheKey)
        
        try {
            val sizePx = (density * size.value).toInt()
            val bitmap = AppCompatResources.getDrawable(context, resourceId)?.let { drawable ->
                BitmapDescriptorFactory.fromBitmap(drawable.toBitmap(sizePx, sizePx))
            } ?: run {
                Timber.tag(TAG).e("Error creating marker bitmap: drawable is null")
                BitmapDescriptorFactory.defaultMarker()
            }
            
            // Cache the result but manage cache size with WeakReference
            if (bitmapCache.size >= maxCacheSize) {
                // Remove entries with null references first
                val nullKeys = bitmapCache.entries.filter { it.value.get() == null }.map { it.key }
                nullKeys.forEach { bitmapCache.remove(it) }
                
                // If still over limit, remove oldest entry (simple FIFO)
                if (bitmapCache.size >= maxCacheSize) {
                    val firstKey = bitmapCache.keys.first()
                    bitmapCache.remove(firstKey)
                }
            }
            bitmapCache[cacheKey] = WeakReference(bitmap)
            
            return bitmap
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error creating marker bitmap: ${e.message}")
            return BitmapDescriptorFactory.defaultMarker()
        }
    }
    /**
     * Performance Optimization: Clear bitmap cache to free memory
     */
    fun clearBitmapCache() {
        if (BuildConfig.DEBUG && bitmapCache.isNotEmpty()) {
            Timber.tag(TAG).d("Clearing bitmap cache, size: ${bitmapCache.size}")
        }
        bitmapCache.clear()
    }
    
    /**
     * Performance Optimization: Get cache status for monitoring (active references only)
     */
    fun getCacheSize(): Int = bitmapCache.values.count { it.get() != null }
    
    /**
     * Performance Optimization: Clean up null references from cache
     */
    fun cleanupCache() {
        val nullKeys = bitmapCache.entries.filter { it.value.get() == null }.map { it.key }
        nullKeys.forEach { bitmapCache.remove(it) }
        if (BuildConfig.DEBUG && nullKeys.isNotEmpty()) {
            Timber.tag(TAG).d("Cleaned up ${nullKeys.size} null references from bitmap cache")
        }
    }

    /**
     * Creates taxi marker based on feature flags
     */
    fun createTaxiMarker(size: Dp, density: Float): BitmapDescriptor {
        return if (AppConstants.MapFeatures.ENABLE_CUSTOM_MARKERS) {
            createMarkerBitmap(R.drawable.ic_taxi_marker, size, density)
        } else {
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
        }
    }
    
    /**
     * Creates passenger marker based on feature flags
     */
    fun createPassengerMarker(size: Dp, density: Float): BitmapDescriptor {
        return if (AppConstants.MapFeatures.ENABLE_CUSTOM_MARKERS) {
            createMarkerBitmap(R.drawable.ic_passenger_marker, size, density)
        } else {
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
        }
    }
    
    /**
     * Creates user location marker based on feature flags
     */
    fun createUserLocationMarker(size: Dp, density: Float): BitmapDescriptor {
        return if (AppConstants.MapFeatures.ENABLE_CUSTOM_MARKERS) {
            createMarkerBitmap(R.drawable.ic_user_location_marker, size, density)
        } else {
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
        }
    }

    /**
     * Check if entity is within radius
     */
    fun isEntityWithinRadius(entityLocation: LatLng,
                             currentLocation: LatLng,
                             radiusKm: Double): Boolean {
        val distance = locationTracker.calculateDistance(currentLocation, entityLocation)
        return distance <= radiusKm
    }
}
@Composable
fun rememberMapScreenEntityTracker(
    locationTracker: MapScreenLocationTracker,
    context: Context
): MapScreenEntityTracker {
    return remember(locationTracker) {
        MapScreenEntityTracker(context, locationTracker)
    }
}