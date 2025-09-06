package com.tecvo.taxi.services
import android.content.Context
import android.os.Build
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class AnalyticsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val tag = "AnalyticsManager"
    private val firebaseAnalytics: FirebaseAnalytics by lazy { FirebaseAnalytics.getInstance(context) }

    open fun logEvent(eventName: String, params: Map<String, Any> = emptyMap()) {
        try {
            val bundle = Bundle()
            params.forEach { (key, value) ->
                when (value) {
                    is String -> bundle.putString(key, value)
                    is Int -> bundle.putLong(key, value.toLong())
                    is Long -> bundle.putLong(key, value)
                    is Double -> bundle.putDouble(key, value)
                    is Boolean -> bundle.putString(key, value.toString())
                    else -> bundle.putString(key, value.toString())
                }
            }
            firebaseAnalytics.logEvent(eventName, bundle)
            Timber.tag(tag).d("Logged event: $eventName with params: $params")
        } catch (e: Exception) {
            Timber.tag(tag).e("Error logging event $eventName: ${e.message}")
        }
    }

    open fun setUserProperty(name: String, value: String?) {
        try {
            firebaseAnalytics.setUserProperty(name, value)
            Timber.tag(tag).d("Set user property: $name = $value")
        } catch (e: Exception) {
            Timber.tag(tag).e("Error setting user property $name: ${e.message}")
        }
    }

    open fun logScreenView(screenName: String, screenClass: String? = null) {
        val params = mutableMapOf<String, Any>(
            FirebaseAnalytics.Param.SCREEN_NAME to screenName
        )
        if (screenClass != null) {
            params[FirebaseAnalytics.Param.SCREEN_CLASS] = screenClass
        }
        logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params)
    }

    open fun setUserId(userId: String?) {
        try {
            firebaseAnalytics.setUserId(userId)
            Timber.tag(tag).d("Set user ID: $userId")
        } catch (e: Exception) {
            Timber.tag(tag).e("Error setting user ID: ${e.message}")
        }
    }

    /**
     * Sets common analytics properties used throughout the app
     */
    open fun setCommonProperties() {
        try {
            // Get app version
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val appVersion = packageInfo.versionName
            val appVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }

            // Set common properties
            setUserProperty("app_version", appVersion)
            setUserProperty("app_version_code", appVersionCode.toString())
            setUserProperty("device_model", Build.MODEL)
            setUserProperty("android_version", Build.VERSION.RELEASE)

            Timber.tag(tag).d("Common analytics properties set")
        } catch (e: Exception) {
            Timber.tag(tag).e("Error setting common properties: ${e.message}")
        }
    }

    open fun setAnalyticsEnabled(enabled: Boolean) {
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
        Timber.tag(tag).i("Analytics collection ${if (enabled) "enabled" else "disabled"}")
    }

    open fun resetAnalyticsData() {
        try {
            Firebase.analytics.resetAnalyticsData()
            Timber.tag(tag).i("Analytics data reset")
        } catch (e: Exception) {
            Timber.tag(tag).e("Error resetting analytics data: ${e.message}")
        }
    }
}