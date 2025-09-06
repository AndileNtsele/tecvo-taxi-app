// MemoryOptimizationManager.kt
package com.tecvo.taxi.utils

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Memory optimization manager to reduce object allocations and GC pressure
 */
@Singleton
class MemoryOptimizationManager @Inject constructor() {
    
    private val tag = "MemoryOptimizationManager"
    private val cleanupScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Object pools for frequently created objects
    private val latLngPool = LinkedBlockingQueue<LatLng>()
    private val stringBuilderPool = LinkedBlockingQueue<StringBuilder>()
    
    // Cache for string operations
    private val stringCache = ConcurrentHashMap<String, String>()
    private val maxCacheSize = 500
    
    // Memory monitoring
    private var lastGcTime = 0L
    private var gcCount = 0
    
    init {
        // Pre-populate pools
        repeat(20) {
            latLngPool.offer(LatLng(0.0, 0.0))
            stringBuilderPool.offer(StringBuilder())
        }
        
        // Start periodic cleanup
        startPeriodicCleanup()
        
        Timber.tag(tag).d("MemoryOptimizationManager initialized with object pools")
    }
    
    /**
     * Get a LatLng object from the pool, or create a new one
     */
    fun getLatLng(latitude: Double, longitude: Double): LatLng {
        return latLngPool.poll()?.let { recycled ->
            // Unfortunately, LatLng is immutable, so we can't reuse it efficiently
            // This is a design limitation of the Google Maps API
            LatLng(latitude, longitude)
        } ?: LatLng(latitude, longitude)
    }
    
    /**
     * Return a LatLng object to the pool (limited effectiveness due to immutability)
     */
    fun recycleLatLng(latLng: LatLng) {
        if (latLngPool.size < 50) { // Limit pool size
            // Can't actually recycle immutable LatLng effectively
            // Keep method for future optimization opportunities
        }
    }
    
    /**
     * Get a StringBuilder from the pool
     */
    fun getStringBuilder(): StringBuilder {
        return stringBuilderPool.poll()?.apply {
            clear()
        } ?: StringBuilder()
    }
    
    /**
     * Return a StringBuilder to the pool
     */
    fun recycleStringBuilder(stringBuilder: StringBuilder) {
        if (stringBuilderPool.size < 50) { // Limit pool size
            stringBuilder.clear()
            stringBuilderPool.offer(stringBuilder)
        }
    }
    
    /**
     * Cached string formatting to reduce allocations
     */
    fun getCachedString(template: String, vararg args: Any): String {
        val key = buildCacheKey(template, args)
        
        return stringCache.getOrPut(key) {
            val sb = getStringBuilder()
            try {
                String.format(template, *args)
            } finally {
                recycleStringBuilder(sb)
            }
        }
    }
    
    /**
     * Build cache key for string operations
     */
    private fun buildCacheKey(template: String, args: Array<out Any>): String {
        val sb = getStringBuilder()
        try {
            sb.append(template)
            for (arg in args) {
                sb.append("|").append(arg.toString())
            }
            return sb.toString()
        } finally {
            recycleStringBuilder(sb)
        }
    }
    
    /**
     * Clear string cache when it gets too large
     */
    private fun clearStringCacheIfNeeded() {
        if (stringCache.size > maxCacheSize) {
            val entriesToRemove = stringCache.size - (maxCacheSize * 3 / 4)
            val iterator = stringCache.entries.iterator()
            repeat(entriesToRemove) {
                if (iterator.hasNext()) {
                    iterator.next()
                    iterator.remove()
                }
            }
            Timber.tag(tag).d("Cleared %d entries from string cache", entriesToRemove)
        }
    }
    
    /**
     * Optimize collections by removing empty or unused entries
     */
    fun <K, V> optimizeMap(map: MutableMap<K, V>, maxSize: Int) {
        if (map.size > maxSize) {
            val entriesToRemove = map.size - (maxSize * 3 / 4)
            val iterator = map.entries.iterator()
            repeat(entriesToRemove) {
                if (iterator.hasNext()) {
                    iterator.next()
                    iterator.remove()
                }
            }
        }
    }
    
    /**
     * Monitor memory usage and GC activity
     */
    fun logMemoryStats() {
        try {
            val runtime = Runtime.getRuntime()
            val totalMemory = runtime.totalMemory()
            val freeMemory = runtime.freeMemory()
            val usedMemory = totalMemory - freeMemory
            val maxMemory = runtime.maxMemory()
            
            val usedPercentage = (usedMemory.toDouble() / maxMemory * 100).toInt()
            
            Timber.tag(tag).d("Memory: %d%% used (%d/%d MB), GCs: %d",
                usedPercentage,
                usedMemory / 1024 / 1024,
                maxMemory / 1024 / 1024,
                gcCount
            )
            
        } catch (e: Exception) {
            Timber.tag(tag).w("Error logging memory stats: ${e.message}")
        }
    }
    
    /**
     * Suggest garbage collection if memory usage is high
     */
    fun suggestGcIfNeeded() {
        try {
            val runtime = Runtime.getRuntime()
            val totalMemory = runtime.totalMemory()
            val freeMemory = runtime.freeMemory()
            val usedMemory = totalMemory - freeMemory
            val maxMemory = runtime.maxMemory()
            
            val usedPercentage = usedMemory.toDouble() / maxMemory
            
            // Suggest GC if using more than 85% of available memory
            if (usedPercentage > 0.85) {
                val currentTime = System.currentTimeMillis()
                // Don't suggest GC more than once every 30 seconds
                if (currentTime - lastGcTime > 30000) {
                    System.gc()
                    lastGcTime = currentTime
                    gcCount++
                    Timber.tag(tag).d("Suggested GC due to high memory usage: %.1f%%", usedPercentage * 100)
                }
            }
        } catch (e: Exception) {
            Timber.tag(tag).w("Error checking memory for GC: ${e.message}")
        }
    }
    
    /**
     * Periodic cleanup of caches and pools
     */
    private fun startPeriodicCleanup() {
        cleanupScope.launch {
            while (isActive) {
                try {
                    delay(60000) // Run every minute
                    
                    clearStringCacheIfNeeded()
                    
                    // Log memory stats periodically
                    if (gcCount % 10 == 0) { // Every 10th cleanup cycle
                        logMemoryStats()
                    }
                    
                    // Suggest GC if needed
                    suggestGcIfNeeded()
                    
                } catch (e: Exception) {
                    Timber.tag(tag).e("Error in periodic cleanup: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Manual cleanup for immediate memory pressure relief
     */
    fun performImmediateCleanup() {
        try {
            // Clear caches
            stringCache.clear()
            
            // Limit pool sizes
            while (latLngPool.size > 10) {
                latLngPool.poll()
            }
            while (stringBuilderPool.size > 10) {
                stringBuilderPool.poll()
            }
            
            // Suggest GC
            System.gc()
            gcCount++
            
            Timber.tag(tag).i("Performed immediate memory cleanup")
            
        } catch (e: Exception) {
            Timber.tag(tag).e("Error in immediate cleanup: ${e.message}")
        }
    }
    
    /**
     * Get memory optimization statistics
     */
    fun getStats(): String {
        return try {
            val runtime = Runtime.getRuntime()
            val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
            val maxMemory = runtime.maxMemory() / 1024 / 1024
            
            "Memory: ${usedMemory}/${maxMemory}MB, " +
                    "String cache: ${stringCache.size}, " +
                    "LatLng pool: ${latLngPool.size}, " +
                    "StringBuilder pool: ${stringBuilderPool.size}, " +
                    "GC count: $gcCount"
        } catch (e: Exception) {
            "Stats unavailable: ${e.message}"
        }
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        try {
            cleanupScope.cancel("Service stopped")
            stringCache.clear()
            latLngPool.clear()
            stringBuilderPool.clear()
            Timber.tag(tag).d("MemoryOptimizationManager cleaned up")
        } catch (e: Exception) {
            Timber.tag(tag).e("Error during cleanup: ${e.message}")
        }
    }
}