package com.tecvo.taxi.services

import android.content.Context
import android.os.Build
import com.tecvo.taxi.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class CrashReportingManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val tag = "CrashReportingManager"
    private var crashlytics: FirebaseCrashlytics? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    open fun initialize() {
        try {
            // Get Crashlytics instance after Firebase is initialized
            crashlytics = FirebaseCrashlytics.getInstance()

            // Enable Crashlytics collection
            crashlytics?.isCrashlyticsCollectionEnabled = true

            // Set app start-up metadata
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            crashlytics?.setCustomKey("app_version_name", packageInfo.versionName.toString())

            // Handle API level compatibility for versionCode
            @Suppress("DEPRECATION")
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toString()
            } else {
                packageInfo.versionCode.toString()
            }
            crashlytics?.setCustomKey("app_version_code", versionCode)

            crashlytics?.setCustomKey("device_model", Build.MODEL)
            crashlytics?.setCustomKey("android_version", Build.VERSION.RELEASE)
            
            // Add version control information to fix Firebase warnings
            try {
                crashlytics?.setCustomKey("build_time", BuildConfig.BUILD_TIME)
                crashlytics?.setCustomKey("git_commit", BuildConfig.GIT_COMMIT)
                crashlytics?.setCustomKey("build_type", BuildConfig.BUILD_TYPE)
                crashlytics?.setCustomKey("debug_build", BuildConfig.DEBUG)
                crashlytics?.setCustomKey("application_id", BuildConfig.APPLICATION_ID)
            } catch (e: Exception) {
                Timber.tag(tag).w("Error setting version control info: ${e.message}")
            }
            
            Timber.tag(tag).i("Crashlytics initialized successfully with version control info")
        } catch (e: Exception) {
            Timber.tag(tag).e("Failed to initialize Crashlytics: ${e.message}")
        }
    }

    open fun logException(throwable: Throwable, message: String? = null) {
        serviceScope.launch {
            try {
                val crashlyticsInstance = crashlytics
                if (crashlyticsInstance != null) {
                    message?.let {
                        crashlyticsInstance.log("EXCEPTION: $it")
                        crashlyticsInstance.setCustomKey("error_context", it)
                    }
                    
                    // Add thread and timing information
                    crashlyticsInstance.setCustomKey("thread_name", Thread.currentThread().name)
                    crashlyticsInstance.setCustomKey("error_timestamp", System.currentTimeMillis().toString())
                    
                    crashlyticsInstance.recordException(throwable)
                    
                    // Only log in debug to avoid duplicate logging in production
                    if (BuildConfig.DEBUG) {
                        Timber.tag(tag)
                            .e(throwable, "Logged exception to Crashlytics: ${throwable.message}")
                    }
                } else {
                    Timber.tag(tag).w(throwable, "Crashlytics not initialized, logging exception locally only")
                }
            } catch (e: Exception) {
                Timber.tag(tag).e("Failed to log exception to Crashlytics: ${e.message}")
                // Fallback: at least log the original exception locally
                Timber.tag(tag).e(throwable, "Original exception (Crashlytics failed): $message")
            }
        }
    }

    open fun logNonFatalError(tag: String, message: String, exception: Throwable? = null) {
        serviceScope.launch {
            try {
                val crashlyticsInstance = crashlytics
                if (crashlyticsInstance != null) {
                    val logMessage = "NON-FATAL[$tag]: $message"
                    crashlyticsInstance.log(logMessage)
                    
                    // Add context information
                    crashlyticsInstance.setCustomKey("error_tag", tag)
                    crashlyticsInstance.setCustomKey("error_type", "non_fatal")
                    crashlyticsInstance.setCustomKey("thread_name", Thread.currentThread().name)
                    crashlyticsInstance.setCustomKey("error_timestamp", System.currentTimeMillis().toString())
                    
                    if (exception != null) {
                        crashlyticsInstance.recordException(exception)
                    } else {
                        // Create a non-fatal exception to record the error
                        crashlyticsInstance.recordException(Exception("Non-fatal error: $logMessage"))
                    }
                    
                    if (BuildConfig.DEBUG) {
                        Timber.tag(tag).w(exception, logMessage)
                    }
                } else {
                    Timber.tag(tag).w(exception, "Crashlytics not initialized, logging non-fatal error locally only: $message")
                }
            } catch (e: Exception) {
                Timber.tag(tag).e("Failed to log non-fatal error to Crashlytics: ${e.message}")
                // Fallback: log locally
                Timber.tag(tag).w(exception, "NON-FATAL[$tag]: $message (Crashlytics failed)")
            }
        }
    }

    @Suppress("unused")
    open fun setCustomKey(key: String, value: String) {
        try {
            crashlytics?.setCustomKey(key, value)
        } catch (e: Exception) {
            Timber.tag(tag).e("Failed to set custom key: ${e.message}")
        }
    }

    @Suppress("unused")
    fun setCustomKey(key: String, value: Boolean) {
        try {
            crashlytics?.setCustomKey(key, value)
        } catch (e: Exception) {
            Timber.tag(tag).e("Failed to set custom key: ${e.message}")
        }
    }

    @Suppress("unused")
    fun setCustomKey(key: String, value: Int) {
        try {
            crashlytics?.setCustomKey(key, value)
        } catch (e: Exception) {
            Timber.tag(tag).e("Failed to set custom key: ${e.message}")
        }
    }

    @Suppress("unused")
    fun setCustomKey(key: String, value: Long) {
        try {
            crashlytics?.setCustomKey(key, value)
        } catch (e: Exception) {
            Timber.tag(tag).e("Failed to set custom key: ${e.message}")
        }
    }

    @Suppress("unused")
    fun setCustomKey(key: String, value: Float) {
        try {
            crashlytics?.setCustomKey(key, value)
        } catch (e: Exception) {
            Timber.tag(tag).e("Failed to set custom key: ${e.message}")
        }
    }

    @Suppress("unused")
    fun setCustomKey(key: String, value: Double) {
        try {
            crashlytics?.setCustomKey(key, value)
        } catch (e: Exception) {
            Timber.tag(tag).e("Failed to set custom key: ${e.message}")
        }
    }

    @Suppress("unused")
    open fun setUserId(userId: String?) {
        try {
            userId?.let {
                crashlytics?.setUserId(it)
                Timber.tag(tag).d("Set Crashlytics user ID: $it")
            }
        } catch (e: Exception) {
            Timber.tag(tag).e("Failed to set user ID: ${e.message}")
        }
    }

    @Suppress("unused")
    open fun logUser(userId: String, email: String? = null, username: String? = null) {
        try {
            crashlytics?.setUserId(userId)
            setCustomKey("user_email", email ?: "not_set")
            setCustomKey("user_name", username ?: "not_set")
        } catch (e: Exception) {
            Timber.tag(tag).e("Failed to log user info: ${e.message}")
        }
    }

    open fun logScreen(screenName: String) {
        try {
            crashlytics?.log("Screen view: $screenName")
            setCustomKey("last_screen", screenName)
        } catch (e: Exception) {
            Timber.tag(tag).e("Failed to log screen: ${e.message}")
        }
    }

    open fun logBreadcrumb(message: String) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                crashlytics?.log(message)
            } catch (e: Exception) {
                Timber.tag(tag).e("Failed to log breadcrumb: ${e.message}")
            }
        }
    }

    open fun clearUserData() {
        try {
            // Clear user-specific data but keep crashlytics enabled
            Firebase.crashlytics.setUserId("")
            Timber.tag(tag).i("Cleared user data from Crashlytics")
        } catch (e: Exception) {
            Timber.tag(tag).e("Failed to clear user data: ${e.message}")
        }
    }

    @Suppress("unused")
    open fun setCrashlyticsEnabled(enabled: Boolean) {
        try {
            crashlytics?.isCrashlyticsCollectionEnabled = enabled
            Timber.tag(tag).i("Crashlytics collection ${if (enabled) "enabled" else "disabled"}")
        } catch (e: Exception) {
            Timber.tag(tag).e("Failed to set Crashlytics enabled state: ${e.message}")
        }
    }

    open fun cleanup() {
        try {
            // Cancel coroutine scope
            serviceScope.cancel()
            // Clear user data
            clearUserData()
            Timber.tag(tag).i("CrashReportingManager cleaned up")
        } catch (e: Exception) {
            Timber.tag(tag).e("Error during cleanup: ${e.message}")
        }
    }
}