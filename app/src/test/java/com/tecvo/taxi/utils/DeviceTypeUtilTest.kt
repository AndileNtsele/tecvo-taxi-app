package com.tecvo.taxi.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DeviceTypeUtilTest {

    private val mockContext = mockk<Context>()
    private val mockResources = mockk<Resources>()
    private val mockConfiguration = mockk<Configuration>()
    private val mockDisplayMetrics = mockk<DisplayMetrics>()

    @Before
    fun setup() {
        every { mockContext.resources } returns mockResources
        every { mockResources.configuration } returns mockConfiguration
        every { mockResources.displayMetrics } returns mockDisplayMetrics

        // Default phone configuration
        setupDefaultPhoneConfig()
    }

    @After
    fun tearDown() {
        // Clean up static mocks if any
    }

    private fun setupDefaultPhoneConfig() {
        every { mockConfiguration.screenLayout } returns Configuration.SCREENLAYOUT_SIZE_NORMAL
        every { mockConfiguration.smallestScreenWidthDp } returns 360
        every { mockDisplayMetrics.widthPixels } returns 1080
        every { mockDisplayMetrics.heightPixels } returns 1920
        every { mockDisplayMetrics.density } returns 3.0f
        every { mockDisplayMetrics.xdpi } returns 432.0f
        every { mockDisplayMetrics.ydpi } returns 432.0f
    }

    private fun setupTabletConfig() {
        every { mockConfiguration.screenLayout } returns Configuration.SCREENLAYOUT_SIZE_XLARGE
        every { mockConfiguration.smallestScreenWidthDp } returns 800
        every { mockDisplayMetrics.widthPixels } returns 2048
        every { mockDisplayMetrics.heightPixels } returns 1536
        every { mockDisplayMetrics.density } returns 2.0f
        every { mockDisplayMetrics.xdpi } returns 264.0f
        every { mockDisplayMetrics.ydpi } returns 264.0f
    }

    @Test
    fun `isFoldablePhone should detect Samsung Galaxy Z Fold`() {
        mockkStatic(Build::class) {
            every { Build.MANUFACTURER } returns "Samsung"
            every { Build.MODEL } returns "SM-F936B"
            every { Build.DEVICE } returns "q2q"

            assertTrue("Should detect Samsung Galaxy Z Fold", DeviceTypeUtil.isFoldablePhone(mockContext))
        }
    }

    @Test
    fun `isFoldablePhone should detect Samsung Galaxy Z Flip`() {
        mockkStatic(Build::class) {
            every { Build.MANUFACTURER } returns "Samsung"
            every { Build.MODEL } returns "SM-F711B"
            every { Build.DEVICE } returns "b2q"

            assertTrue("Should detect Samsung Galaxy Z Flip", DeviceTypeUtil.isFoldablePhone(mockContext))
        }
    }

    @Test
    fun `isFoldablePhone should detect Google Pixel Fold`() {
        mockkStatic(Build::class) {
            every { Build.MANUFACTURER } returns "Google"
            every { Build.MODEL } returns "Pixel Fold"
            every { Build.DEVICE } returns "felix"

            assertTrue("Should detect Google Pixel Fold", DeviceTypeUtil.isFoldablePhone(mockContext))
        }
    }

    @Test
    fun `isFoldablePhone should detect OnePlus Open`() {
        mockkStatic(Build::class) {
            every { Build.MANUFACTURER } returns "OnePlus"
            every { Build.MODEL } returns "CPH2449"
            every { Build.DEVICE } returns "OP594DL1"

            assertTrue("Should detect OnePlus Open", DeviceTypeUtil.isFoldablePhone(mockContext))
        }
    }

    @Test
    fun `isFoldablePhone should detect Motorola Razr`() {
        mockkStatic(Build::class) {
            every { Build.MANUFACTURER } returns "Motorola"
            every { Build.MODEL } returns "Moto Razr"
            every { Build.DEVICE } returns "razr"

            assertTrue("Should detect Motorola Razr", DeviceTypeUtil.isFoldablePhone(mockContext))
        }
    }

    @Test
    fun `isFoldablePhone should detect Xiaomi Mix Fold`() {
        mockkStatic(Build::class) {
            every { Build.MANUFACTURER } returns "Xiaomi"
            every { Build.MODEL } returns "Mi Mix Fold"
            every { Build.DEVICE } returns "cetus"

            assertTrue("Should detect Xiaomi Mix Fold", DeviceTypeUtil.isFoldablePhone(mockContext))
        }
    }

    @Test
    fun `isFoldablePhone should detect Oppo Find N`() {
        mockkStatic(Build::class) {
            every { Build.MANUFACTURER } returns "OPPO"
            every { Build.MODEL } returns "OPPO Find N"
            every { Build.DEVICE } returns "OP4E75L1"

            assertTrue("Should detect Oppo Find N", DeviceTypeUtil.isFoldablePhone(mockContext))
        }
    }

    @Test
    fun `isFoldablePhone should detect Huawei Mate X`() {
        mockkStatic(Build::class) {
            every { Build.MANUFACTURER } returns "Huawei"
            every { Build.MODEL } returns "Mate X"
            every { Build.DEVICE } returns "mate_x"

            assertTrue("Should detect Huawei Mate X", DeviceTypeUtil.isFoldablePhone(mockContext))
        }
    }

    @Test
    fun `isFoldablePhone should detect Honor Magic V`() {
        mockkStatic(Build::class) {
            every { Build.MANUFACTURER } returns "Honor"
            every { Build.MODEL } returns "Magic V"
            every { Build.DEVICE } returns "honor_magic_v"

            assertTrue("Should detect Honor Magic V", DeviceTypeUtil.isFoldablePhone(mockContext))
        }
    }

    @Test
    fun `isFoldablePhone should detect Vivo X Fold`() {
        mockkStatic(Build::class) {
            every { Build.MANUFACTURER } returns "Vivo"
            every { Build.MODEL } returns "X Fold"
            every { Build.DEVICE } returns "vivo_x_fold"

            assertTrue("Should detect Vivo X Fold", DeviceTypeUtil.isFoldablePhone(mockContext))
        }
    }

    @Test
    fun `isFoldablePhone should return false for regular Samsung phone`() {
        mockkStatic(Build::class) {
            every { Build.MANUFACTURER } returns "Samsung"
            every { Build.MODEL } returns "SM-G998B"
            every { Build.DEVICE } returns "o1s"

            assertFalse("Should not detect regular Samsung phone as foldable", DeviceTypeUtil.isFoldablePhone(mockContext))
        }
    }

    @Test
    fun `isFoldablePhone should return false for regular Google phone`() {
        mockkStatic(Build::class) {
            every { Build.MANUFACTURER } returns "Google"
            every { Build.MODEL } returns "Pixel 7"
            every { Build.DEVICE } returns "panther"

            assertFalse("Should not detect regular Google phone as foldable", DeviceTypeUtil.isFoldablePhone(mockContext))
        }
    }

    @Test
    fun `isTablet should return true for large screen device`() {
        setupTabletConfig()

        mockkStatic(Build::class) {
            every { Build.MANUFACTURER } returns "Samsung"
            every { Build.MODEL } returns "SM-T870"
            every { Build.DEVICE } returns "tablet"

            assertTrue("Should detect tablet", DeviceTypeUtil.isTablet(mockContext))
        }
    }

    @Test
    fun `isTablet should return false for phone`() {
        setupDefaultPhoneConfig()

        mockkStatic(Build::class) {
            every { Build.MANUFACTURER } returns "Samsung"
            every { Build.MODEL } returns "SM-G998B"
            every { Build.DEVICE } returns "o1s"

            assertFalse("Should not detect phone as tablet", DeviceTypeUtil.isTablet(mockContext))
        }
    }

    @Test
    fun `isTablet should return false for foldable phone even with large screen`() {
        // Set up large screen dimensions (tablet-like)
        setupTabletConfig()

        // But it's a foldable phone
        mockkStatic(Build::class) {
            every { Build.MANUFACTURER } returns "Samsung"
            every { Build.MODEL } returns "SM-F936B"
            every { Build.DEVICE } returns "q2q"

            assertFalse("Should not detect foldable phone as tablet even with large screen", DeviceTypeUtil.isTablet(mockContext))
        }
    }

    @Test
    fun `isTablet should detect based on smallestScreenWidthDp`() {
        every { mockConfiguration.screenLayout } returns Configuration.SCREENLAYOUT_SIZE_NORMAL
        every { mockConfiguration.smallestScreenWidthDp } returns 700 // Tablet threshold
        every { mockDisplayMetrics.widthPixels } returns 1400
        every { mockDisplayMetrics.heightPixels } returns 1800
        every { mockDisplayMetrics.density } returns 2.0f
        every { mockDisplayMetrics.xdpi } returns 264.0f
        every { mockDisplayMetrics.ydpi } returns 264.0f

        mockkStatic(Build::class) {
            every { Build.MANUFACTURER } returns "Samsung"
            every { Build.MODEL } returns "SM-T870"
            every { Build.DEVICE } returns "tablet"

            assertTrue("Should detect tablet based on smallestScreenWidthDp", DeviceTypeUtil.isTablet(mockContext))
        }
    }

    @Test
    fun `isTablet should detect based on diagonal size`() {
        every { mockConfiguration.screenLayout } returns Configuration.SCREENLAYOUT_SIZE_NORMAL
        every { mockConfiguration.smallestScreenWidthDp } returns 500
        every { mockDisplayMetrics.widthPixels } returns 1536
        every { mockDisplayMetrics.heightPixels } returns 2048
        every { mockDisplayMetrics.density } returns 2.0f
        every { mockDisplayMetrics.xdpi } returns 216.0f  // Lower DPI = larger physical size
        every { mockDisplayMetrics.ydpi } returns 216.0f

        mockkStatic(Build::class) {
            every { Build.MANUFACTURER } returns "Samsung"
            every { Build.MODEL } returns "SM-T870"
            every { Build.DEVICE } returns "tablet"

            assertTrue("Should detect tablet based on diagonal size", DeviceTypeUtil.isTablet(mockContext))
        }
    }

    @Test
    fun `getDeviceTypeDescription should return correct descriptions`() {
        // Test phone description
        setupDefaultPhoneConfig()
        mockkStatic(Build::class) {
            every { Build.MANUFACTURER } returns "Samsung"
            every { Build.MODEL } returns "SM-G998B"
            every { Build.DEVICE } returns "o1s"

            assertEquals("Phone", DeviceTypeUtil.getDeviceTypeDescription(mockContext))
        }

        // Test tablet description
        setupTabletConfig()
        mockkStatic(Build::class) {
            every { Build.MANUFACTURER } returns "Samsung"
            every { Build.MODEL } returns "SM-T870"
            every { Build.DEVICE } returns "tablet"

            assertEquals("Tablet", DeviceTypeUtil.getDeviceTypeDescription(mockContext))
        }
    }

    @Test
    fun `getDeviceInfo should return formatted device information`() {
        setupDefaultPhoneConfig()

        mockkStatic(Build::class) {
            every { Build.MANUFACTURER } returns "Samsung"
            every { Build.MODEL } returns "SM-G998B"
            every { Build.DEVICE } returns "o1s"

            val deviceInfo = DeviceTypeUtil.getDeviceInfo(mockContext)

            assertTrue("Should contain device type", deviceInfo.contains("Device Type: Phone"))
            assertTrue("Should contain screen layout", deviceInfo.contains("Screen Layout:"))
            assertTrue("Should contain smallest width", deviceInfo.contains("Smallest Width:"))
            assertTrue("Should contain dimensions", deviceInfo.contains("Current Dimensions:"))
            assertTrue("Should contain pixel dimensions", deviceInfo.contains("Pixel Dimensions:"))
            assertTrue("Should contain density", deviceInfo.contains("Density:"))
            assertTrue("Should contain diagonal size", deviceInfo.contains("Diagonal Size:"))
        }
    }

    @Test
    fun `case insensitive manufacturer and model detection`() {
        mockkStatic(Build::class) {
            every { Build.MANUFACTURER } returns "SAMSUNG"  // Uppercase
            every { Build.MODEL } returns "SM-F936b"        // Mixed case
            every { Build.DEVICE } returns "Q2Q"            // Uppercase

            assertTrue("Should detect foldable with case insensitive matching", DeviceTypeUtil.isFoldablePhone(mockContext))
        }
    }

    @Test
    fun `generic foldable detection should work`() {
        mockkStatic(Build::class) {
            every { Build.MANUFACTURER } returns "UnknownBrand"
            every { Build.MODEL } returns "New Fold Device"
            every { Build.DEVICE } returns "unknown_fold"

            assertTrue("Should detect generic foldable device", DeviceTypeUtil.isFoldablePhone(mockContext))
        }
    }

    @Test
    fun `edge case - very small screen should be phone`() {
        every { mockConfiguration.screenLayout } returns Configuration.SCREENLAYOUT_SIZE_SMALL
        every { mockConfiguration.smallestScreenWidthDp } returns 240
        every { mockDisplayMetrics.widthPixels } returns 480
        every { mockDisplayMetrics.heightPixels } returns 800
        every { mockDisplayMetrics.density } returns 2.0f
        every { mockDisplayMetrics.xdpi } returns 320.0f
        every { mockDisplayMetrics.ydpi } returns 320.0f

        mockkStatic(Build::class) {
            every { Build.MANUFACTURER } returns "Generic"
            every { Build.MODEL } returns "Small Phone"
            every { Build.DEVICE } returns "small"

            assertFalse("Should detect very small screen as phone, not tablet", DeviceTypeUtil.isTablet(mockContext))
        }
    }
}