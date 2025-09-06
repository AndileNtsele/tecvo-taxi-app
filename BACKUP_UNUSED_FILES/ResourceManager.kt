// ResourceManager.kt
package com.example.taxi.utils

import android.content.Context
import android.content.res.Resources
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class to handle resource management and resolve resource ID issues
 * that can cause crashes or warnings in the application
 */
@Singleton
class ResourceManager @Inject constructor() {
    
    private val tag = "ResourceManager"
    
    /**
     * Safely get a string resource with fallback handling
     */
    fun getString(context: Context, resId: Int, fallback: String = ""): String {
        return try {
            context.getString(resId)
        } catch (e: Resources.NotFoundException) {
            Timber.tag(tag).w("Resource not found for ID: 0x${Integer.toHexString(resId)}, using fallback: $fallback")
            fallback
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Error getting string resource: 0x${Integer.toHexString(resId)}")
            fallback
        }
    }
    
    /**
     * Safely get a string resource with format arguments and fallback handling
     */
    fun getString(context: Context, resId: Int, fallback: String = "", vararg formatArgs: Any): String {
        return try {
            context.getString(resId, *formatArgs)
        } catch (e: Resources.NotFoundException) {
            Timber.tag(tag).w("Resource not found for ID: 0x${Integer.toHexString(resId)}, using fallback: $fallback")
            fallback
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Error getting formatted string resource: 0x${Integer.toHexString(resId)}")
            fallback
        }
    }
    
    /**
     * Safely get a drawable resource
     */
    fun getDrawable(context: Context, resId: Int): android.graphics.drawable.Drawable? {
        return try {
            androidx.core.content.ContextCompat.getDrawable(context, resId)
        } catch (e: Resources.NotFoundException) {
            Timber.tag(tag).w("Drawable resource not found for ID: 0x${Integer.toHexString(resId)}")
            null
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Error getting drawable resource: 0x${Integer.toHexString(resId)}")
            null
        }
    }
    
    /**
     * Safely get a color resource
     */
    fun getColor(context: Context, resId: Int, fallback: Int = android.graphics.Color.TRANSPARENT): Int {
        return try {
            androidx.core.content.ContextCompat.getColor(context, resId)
        } catch (e: Resources.NotFoundException) {
            Timber.tag(tag).w("Color resource not found for ID: 0x${Integer.toHexString(resId)}, using fallback")
            fallback
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Error getting color resource: 0x${Integer.toHexString(resId)}")
            fallback
        }
    }
    
    /**
     * Safely get a dimension resource
     */
    fun getDimension(context: Context, resId: Int, fallback: Float = 0f): Float {
        return try {
            context.resources.getDimension(resId)
        } catch (e: Resources.NotFoundException) {
            Timber.tag(tag).w("Dimension resource not found for ID: 0x${Integer.toHexString(resId)}, using fallback: $fallback")
            fallback
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Error getting dimension resource: 0x${Integer.toHexString(resId)}")
            fallback
        }
    }
    
    /**
     * Safely get an integer resource
     */
    fun getInteger(context: Context, resId: Int, fallback: Int = 0): Int {
        return try {
            context.resources.getInteger(resId)
        } catch (e: Resources.NotFoundException) {
            Timber.tag(tag).w("Integer resource not found for ID: 0x${Integer.toHexString(resId)}, using fallback: $fallback")
            fallback
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Error getting integer resource: 0x${Integer.toHexString(resId)}")
            fallback
        }
    }
    
    /**
     * Safely get a boolean resource
     */
    fun getBoolean(context: Context, resId: Int, fallback: Boolean = false): Boolean {
        return try {
            context.resources.getBoolean(resId)
        } catch (e: Resources.NotFoundException) {
            Timber.tag(tag).w("Boolean resource not found for ID: 0x${Integer.toHexString(resId)}, using fallback: $fallback")
            fallback
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Error getting boolean resource: 0x${Integer.toHexString(resId)}")
            fallback
        }
    }
    
    /**
     * Check if a resource exists without throwing exceptions
     */
    fun resourceExists(context: Context, resId: Int): Boolean {
        return try {
            context.resources.getResourceName(resId)
            true
        } catch (e: Resources.NotFoundException) {
            false
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Error checking resource existence: 0x${Integer.toHexString(resId)}")
            false
        }
    }
    
    /**
     * Get resource name safely
     */
    fun getResourceName(context: Context, resId: Int): String? {
        return try {
            context.resources.getResourceName(resId)
        } catch (e: Resources.NotFoundException) {
            Timber.tag(tag).w("Resource name not found for ID: 0x${Integer.toHexString(resId)}")
            null
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Error getting resource name: 0x${Integer.toHexString(resId)}")
            null
        }
    }
    
    /**
     * Log resource information for debugging
     */
    fun logResourceInfo(context: Context, resId: Int) {
        try {
            val resourceName = context.resources.getResourceName(resId)
            val resourceType = context.resources.getResourceTypeName(resId)
            val resourceEntry = context.resources.getResourceEntryName(resId)
            
            Timber.tag(tag).d("Resource info - ID: 0x${Integer.toHexString(resId)}, " +
                    "Name: $resourceName, Type: $resourceType, Entry: $resourceEntry")
        } catch (e: Exception) {
            Timber.tag(tag).w("Cannot get info for resource ID: 0x${Integer.toHexString(resId)}")
        }
    }
}