package com.tecvo.taxi.utils
import android.content.Context
import android.widget.Toast
import com.tecvo.taxi.BuildConfig
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Utility class to validate the Google Maps API key at runtime
 * to catch invalid keys early and provide helpful error messages.
 */
object ApiKeyValidator {
    private const val TAG = "ApiKeyValidator"
    /**
     * Validates the Maps API key format and initialization
     * @param context Application context
     * @return True if the key appears valid, false otherwise
     */
    suspend fun validateMapsApiKey(context: Context): Boolean = withContext(Dispatchers.Main) {
        val apiKey = BuildConfig.MAPS_API_KEY
        // Basic format validation
        if (apiKey.isBlank()) {
            Timber.tag(TAG).e("Maps API key is missing or empty")
            Toast.makeText(
                context,
                "Maps functionality unavailable: API key not configured",
                Toast.LENGTH_LONG
            ).show()
            return@withContext false
        }
        // API key format check (simple regex for typical Google API key format)
        if (!apiKey.matches(Regex("^AIza[0-9A-Za-z\\-_]{35}$"))) {
            Timber.tag(TAG).e("Maps API key has invalid format")
            Toast.makeText(
                context,
                "Maps functionality may be unavailable: API key format appears invalid",
                Toast.LENGTH_LONG
            ).show()
            return@withContext false
        }
        // MapsInitializer would throw an exception for completely invalid keys
        try {
            // Initialize with callback to detect issues
            MapsInitializer.initialize(
                context.applicationContext, MapsInitializer.Renderer.LATEST,
                OnMapsSdkInitializedCallback { renderer ->
                    Timber.tag(TAG).d("Maps SDK initialized with renderer: $renderer")
                }
            )
            return@withContext true
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Maps initialization failed, possible API key issue")
            Toast.makeText(
                context,
                "Maps functionality unavailable: API key may be invalid or restricted",
                Toast.LENGTH_LONG
            ).show()
            return@withContext false
        }
    }
}