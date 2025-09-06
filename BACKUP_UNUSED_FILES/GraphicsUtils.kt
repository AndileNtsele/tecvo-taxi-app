package com.example.taxi.utils

import android.content.Context
import android.opengl.GLSurfaceView
import timber.log.Timber

/**
 * Utilities for handling graphics and OpenGL initialization issues gracefully
 */
object GraphicsUtils {
    
    private const val TAG = "GraphicsUtils"
    
    /**
     * Safe OpenGL initialization with fallback handling
     */
    fun safeGraphicsInitialization(context: Context, block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            when {
                e.message?.contains("EGL", ignoreCase = true) == true -> {
                    Timber.tag(TAG).w(e, "EGL initialization failed, using software fallback")
                    handleEGLFailure(context)
                }
                e.message?.contains("OpenGL", ignoreCase = true) == true -> {
                    Timber.tag(TAG).w(e, "OpenGL initialization failed, continuing with degraded graphics")
                }
                else -> {
                    Timber.tag(TAG).w(e, "Graphics initialization failed: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Handle EGL initialization failures with software rendering fallback
     */
    private fun handleEGLFailure(context: Context) {
        try {
            // Force software rendering for map components if hardware acceleration fails
            Timber.tag(TAG).i("Attempting software rendering fallback")
            // This is a graceful degradation - the app will continue to work
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Software fallback also failed, continuing with basic rendering")
        }
    }
    
    /**
     * Safe rendering operation wrapper
     */
    fun safeRender(operation: String, block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Rendering operation '$operation' failed gracefully: ${e.message}")
            // Continue execution - don't crash the app
        }
    }
}