package com.tecvo.taxi.services

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppInitService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "AppInitService"
        @Volatile private var instance: AppInitService? = null

        fun getInstance(): AppInitService {
            return instance ?: synchronized(this) {
                instance ?: throw IllegalStateException(
                    "AppInitService has not been initialized via Hilt injection."
                )
            }
        }
    }

    init {
        // Store instance for backward compatibility
        instance = this
    }

    suspend fun configureWebView() = withContext(Dispatchers.IO) {
        try {
            val webViewPackage = android.webkit.WebView.getCurrentWebViewPackage()
            Timber.tag(TAG).i("WebView version: ${webViewPackage?.versionName ?: "unknown"}")

            // WebView configuration must happen on main thread, but we can minimize work
            withContext(Dispatchers.Main) {
                android.webkit.WebView.setWebContentsDebuggingEnabled(
                    context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0
                )
            }

            // Success - WebView configured
            Timber.tag(TAG).i("WebView successfully configured")
        } catch (e: Exception) {
            Timber.tag(TAG).e("Error configuring WebView: ${e.message}")
            throw e
        }
    }

    suspend fun createAppDirectories() = withContext(Dispatchers.IO) {
        try {
            // Create OBB directory if needed
            val obbDir = context.obbDir
            if (!obbDir.exists()) {
                val success = obbDir.mkdirs()
                if (success) {
                    Timber.tag(TAG).i("OBB directory created successfully")
                } else {
                    Timber.tag(TAG).w("Could not create OBB directory")
                }
            } else {
                Timber.tag(TAG).i("OBB directory already exists")
            }

            // Create cache directories if needed
            createCacheDirs()
        } catch (e: Exception) {
            Timber.tag(TAG).e("Failed to create directories: ${e.message}")
            throw e
        }
    }

    private suspend fun createCacheDirs() = withContext(Dispatchers.IO) {
        try {
            // Create image cache directory
            val imageCacheDir = File(context.cacheDir, "image_cache")
            if (!imageCacheDir.exists()) {
                val success = imageCacheDir.mkdirs()
                if (success) {
                    Timber.tag(TAG).i("Image cache directory created successfully")
                } else {
                    Timber.tag(TAG).w("Could not create image cache directory")
                }
            }

            // Create map tiles cache directory
            val mapTilesCacheDir = File(context.cacheDir, "map_tiles")
            if (!mapTilesCacheDir.exists()) {
                val success = mapTilesCacheDir.mkdirs()
                if (success) {
                    Timber.tag(TAG).i("Map tiles cache directory created successfully")
                } else {
                    Timber.tag(TAG).w("Could not create map tiles cache directory")
                }
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e("Failed to create cache directories: ${e.message}")
            // Not throwing here as these are not critical directories
        }
    }

    fun cleanup() {
        Timber.tag(TAG).i("Cleaning up AppInitService resources")
        // Any cleanup logic needed
    }
}