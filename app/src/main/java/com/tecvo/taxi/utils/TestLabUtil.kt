package com.tecvo.taxi.utils

import android.content.Context
import android.provider.Settings
import timber.log.Timber

/**
 * Utility to detect Firebase Test Lab environment
 * Industry standard approach: https://stackoverflow.com/questions/43598250
 */
object TestLabUtil {

    private const val TAG = "TestLabUtil"

    /**
     * Check if app is running in Firebase Test Lab
     * @return true if running in Firebase Test Lab, false otherwise
     */
    fun isRunningInTestLab(context: Context): Boolean {
        val testLabSetting = try {
            Settings.System.getString(context.contentResolver, "firebase.test.lab")
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Error checking Test Lab setting")
            null
        }

        val isTestLab = "true" == testLabSetting
        if (isTestLab) {
            Timber.tag(TAG).i("🧪 DETECTED: Running in Firebase Test Lab - Auth bypass enabled")
        }
        return isTestLab
    }
}
