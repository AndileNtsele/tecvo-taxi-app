import java.io.FileInputStream
import java.util.Properties

// Load keystore properties for release signing - prioritize environment variables for security
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()

// Try to load from environment variables first (secure for CI/CD and production)
val keystoreStorePassword = System.getenv("KEYSTORE_STORE_PASSWORD")
val keystoreKeyPassword = System.getenv("KEYSTORE_KEY_PASSWORD") 
val keystoreKeyAlias = System.getenv("KEYSTORE_KEY_ALIAS")
val keystoreStoreFile = System.getenv("KEYSTORE_STORE_FILE")

// Fallback to keystore.properties file for local development only
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}
android {
    namespace = "com.tecvo.taxi" // TECVO registered company package name
    compileSdk = 35
    
    // Signing configurations for release builds - secure environment variable approach
    signingConfigs {
        create("release") {
            // Prioritize environment variables for security (CI/CD and production builds)
            keyAlias = keystoreKeyAlias 
                ?: keystoreProperties["keyAlias"] as String? 
                ?: "taxi-release-key"
            keyPassword = keystoreKeyPassword 
                ?: keystoreProperties["keyPassword"] as String? 
                ?: ""
            storePassword = keystoreStorePassword 
                ?: keystoreProperties["storePassword"] as String? 
                ?: ""
            storeFile = if (keystoreStoreFile != null) {
                file(keystoreStoreFile)
            } else {
                keystoreProperties["storeFile"]?.let { file(it as String) } 
                    ?: file("../release-keystore.jks")
            }
            
            // Security validation - ensure we have credentials before signing
            val storePassCheck = storePassword ?: ""
            val keyPassCheck = keyPassword ?: ""
            if (storePassCheck.isEmpty() || keyPassCheck.isEmpty()) {
                logger.warn("⚠️  WARNING: Missing keystore credentials! Release signing will fail.")
                logger.warn("🔐 SECURITY: Set environment variables KEYSTORE_STORE_PASSWORD, KEYSTORE_KEY_PASSWORD, KEYSTORE_KEY_ALIAS, KEYSTORE_STORE_FILE")
                logger.warn("📁 LOCAL DEV: Or ensure keystore.properties exists (NOT in version control)")
            } else {
                logger.info("✅ Release keystore credentials configured securely")
            }
        }
    }
    defaultConfig {
        applicationId = "com.tecvo.taxi" // TAXI app application ID (by TECVO)
        minSdk = 26
        targetSdk = 35
        versionCode = 2 // Incremented for Play Store release
        versionName = "1.0.1" // Play Store release version
        testInstrumentationRunner = "com.tecvo.taxi.HiltTestRunner"
        
        // Enable vector drawable support for older API levels
        vectorDrawables {
            useSupportLibrary = true
        }

        // Resource optimization for smaller APK size (2025 syntax)
        androidResources {
            localeFilters += listOf("en", "af") // English and Afrikaans for South Africa
        }
        // COMMENTED OUT: Manual API key loading - now handled by Secrets Gradle Plugin
        // The Secrets Gradle Plugin automatically:
        // 1. Loads keys from secrets.properties (real keys) or local.defaults.properties (placeholders)
        // 2. Generates BuildConfig fields (MAPS_API_KEY, GEOCODING_API_KEY, etc.)
        // 3. Creates manifest placeholders for AndroidManifest.xml
        //
        // Original manual loading code preserved below for reference/rollback if needed:
        /*
        // Load properties from local.properties file
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }
        // Get the Maps API key with fallback to project properties and then empty string
        val mapsApiKey = localProperties.getProperty("MAPS_API_KEY")
            ?: project.findProperty("MAPS_API_KEY")?.toString()
            ?: ""
        // Get the Geocoding API key with fallback to project properties and then empty string
        val geocodingApiKey = localProperties.getProperty("GEOCODING_API_KEY")
            ?: project.findProperty("GEOCODING_API_KEY")?.toString()
            ?: ""
        // Get the Secondary Geocoding API key with fallback
        val geocodingApiKeySecondary = localProperties.getProperty("GEOCODING_API_KEY_SECONDARY")
            ?: project.findProperty("GEOCODING_API_KEY_SECONDARY")?.toString()
            ?: ""
        // Get Firebase Database URL with fallback
        val firebaseDatabaseUrl = localProperties.getProperty("FIREBASE_DATABASE_URL")
            ?: project.findProperty("FIREBASE_DATABASE_URL")?.toString()
            ?: ""

        // Add error check
        if (firebaseDatabaseUrl.isEmpty()) {
            throw GradleException("Firebase Database URL not found! Add FIREBASE_DATABASE_URL to local.properties")
        }
        // Enhanced API key validation with development guidance
        if (mapsApiKey.isEmpty() || mapsApiKey.contains("YOUR_")) {
            logger.warn("⚠️  WARNING: No Google Maps API key found! Map functionality will not work.")
            logger.warn("📋 SETUP: Copy keys from local.properties.dev to local.properties for development")
        } else {
            logger.info("✅ Google Maps API key configured")
        }

        if (geocodingApiKey.isEmpty() || geocodingApiKey.contains("YOUR_")) {
            logger.warn("⚠️  WARNING: No Geocoding API key found! Address search will not work.")
            logger.warn("📋 SETUP: Copy keys from local.properties.dev to local.properties for development")
        } else {
            logger.info("✅ Primary Geocoding API key configured")
        }

        if (geocodingApiKeySecondary.isEmpty() || geocodingApiKeySecondary.contains("YOUR_")) {
            logger.warn("⚠️  WARNING: No secondary Geocoding API key found! Fallback disabled.")
        } else {
            logger.info("✅ Secondary Geocoding API key configured")
        }
        // Log a message about Firebase URL
        logger.info("Using Firebase Database URL: $firebaseDatabaseUrl")
        // Set BuildConfig fields and manifest placeholders
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")
        buildConfigField("String", "GEOCODING_API_KEY", "\"$geocodingApiKey\"")
        buildConfigField("String", "GEOCODING_API_KEY_SECONDARY", "\"$geocodingApiKeySecondary\"")
        buildConfigField("String", "FIREBASE_DATABASE_URL", "\"$firebaseDatabaseUrl\"")
        manifestPlaceholders.putAll(mapOf("MAPS_API_KEY" to mapsApiKey))
        */
    }
    buildTypes {
        release {
            // Security: Disable debugging for production builds
            isDebuggable = false

            // Signing configuration
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }

            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Enable Crashlytics for release builds
            manifestPlaceholders["crashlyticsCollectionEnabled"] = true
            
            // Add build metadata for version control
            buildConfigField("String", "BUILD_TIME", "\"${System.currentTimeMillis()}\"")
            buildConfigField("String", "GIT_COMMIT", "\"unknown\"")
            
            // Disable debug features in release
            buildConfigField("Boolean", "ENABLE_DEBUG_FEATURES", "false")

            // Android App Bundle optimization
            buildConfigField("Boolean", "USE_APP_BUNDLE", "true")
        }
        debug {
            // Disable Crashlytics for debug builds to speed up build time
            manifestPlaceholders["crashlyticsCollectionEnabled"] = false
            
            // Add debug build metadata
            buildConfigField("String", "BUILD_TIME", "\"${System.currentTimeMillis()}\"")
            buildConfigField("String", "GIT_COMMIT", "\"unknown\"")
            
            // Enable multidex for debug builds to handle large dependency count
            multiDexEnabled = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        // Kotlin compilation optimizations (2025)
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all"
        )
    }

    // Use available Java installation instead of strict toolchain
    // kotlin {
    //     jvmToolchain(17)
    // }

    // Build performance optimizations (2025 CRITICAL)
    // dexOptions removed - deprecated in AGP 8.x, dexing is optimized automatically
    buildFeatures {
        compose = true
        buildConfig = true
    }
    // AAPT Optimization: Fix processDebugResources timeout (2025 CRITICAL FIX)
    androidResources {
        additionalParameters += listOf("--allow-reserved-package-id", "--no-version-vectors")
        noCompress += listOf("tflite") // Minimal noCompress items for faster processing
        ignoreAssetsPattern = "!.svn:!.git:!.ds_store:!*.scc:.*:!CVS:!thumbs.db:!picasa.ini:!*~"
    }

    // Configure test options for performance and reliability (2025 optimization)
    testOptions {
        // Disable animations for UI tests - critical for reliability
        animationsDisabled = true

        // Use Test Orchestrator for test isolation
        execution = "ANDROIDX_TEST_ORCHESTRATOR"

        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true

            all {
                it.systemProperty("robolectric.logging", "stdout")
                it.systemProperty("robolectric.dependency.repo.url", "https://repo1.maven.org/maven2")

                // Fix ClassLoader issues
                it.jvmArgs("-noverify")
                it.jvmArgs("-XX:+EnableDynamicAgentLoading")
                it.jvmArgs("-XX:+AllowRedefinitionToAddDeleteMethods")
                it.jvmArgs("-Djdk.instrument.traceUsage=false")

                // Increase memory for tests to prevent OutOfMemoryError
                it.maxHeapSize = "2g"

                // Enable incremental compilation
                it.systemProperty("org.gradle.daemon.idletimeout", "10800000")

                // Set reasonable timeout for individual tests
                it.systemProperty("junit.jupiter.execution.timeout.default", "120s")
            }
        }

        // Configure instrumented test timeouts
        managedDevices {
            localDevices {
                create("pixel2Api30") {
                    device = "Pixel 2"
                    apiLevel = 30
                    systemImageSource = "google"
                }
            }
        }
    }
    
    // Configure packaging options to resolve resource conflicts
    packaging {
        resources {
            excludes.addAll(listOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/DEPENDENCIES",
                "/META-INF/LICENSE",
                "/META-INF/LICENSE.txt",
                "/META-INF/NOTICE",
                "/META-INF/NOTICE.txt",
                "/META-INF/*.kotlin_module"
            ))
        }
    }

    // Android App Bundle Configuration (2025 Play Store Standard)
    bundle {
        language {
            // Enable language-based APK splits
            enableSplit = true
        }
        density {
            // Enable density-based APK splits for smaller downloads
            enableSplit = true
        }
        abi {
            // Enable ABI-based APK splits
            enableSplit = true
        }
    }
}
// Fix for Java agent dynamic loading warning and ClassLoader issues
tasks.withType<Test> {
    jvmArgs(
        "-XX:+EnableDynamicAgentLoading",
        "-XX:+AllowRedefinitionToAddDeleteMethods",
        "-Djdk.instrument.traceUsage=false"
    )
    maxHeapSize = "2g"
}

// Git commit hash removed to fix configuration cache compatibility
// If you need git commit info, consider using build metadata or version control at deployment time

// Configure KAPT for better performance and stability
kapt {
    correctErrorTypes = true
    useBuildCache = true
    includeCompileClasspath = false

    // Arguments for Hilt to improve build performance
    arguments {
        arg("dagger.fastInit", "enabled")
        arg("dagger.formatGeneratedSource", "disabled")
        arg("dagger.hilt.android.internal.disableAndroidSuperclassValidation", "true")
    }
}

dependencies {
    
    // AndroidX Core & App Compat
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // Lifecycle Components
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Jetpack Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation(libs.androidx.material3.window.size)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Firebase (using BOM for version consistency)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.crashlytics.ktx)
    // Temporarily disabled to fix protobuf conflicts in tests
    // implementation(libs.firebase.perf.ktx)

    // Google Play Services
    implementation(libs.play.services.auth)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    // Maps Compose
    implementation(libs.maps.compose)
    implementation(libs.maps.utils)

    // Navigation
    implementation(libs.navigation.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // DataStore
    implementation(libs.datastore.preferences)

    // Accompanist
    implementation(libs.accompanist.systemuicontroller)

    // Image loading
    implementation(libs.coil.compose)

    // Window metrics
    implementation(libs.window)

    // Phone number validation
    implementation(libs.libphonenumber)

    // Material Design
    implementation(libs.material)

    // Retrofit & Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Gson for JSON parsing
    implementation(libs.gson)

    // Timber for better logging
    implementation(libs.timber)

    // LeakCanary for memory leak detection
    debugImplementation(libs.leakcanary)

    // Hilt dependencies with KAPT (optimized configuration)
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)

    // Firebase Core and Common
    implementation(libs.firebase.core)
    implementation(libs.firebase.common.ktx)

    // ===== TESTING DEPENDENCIES =====
    testImplementation(libs.junit)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.core.ktx)
    testImplementation(libs.androidx.test.rules)
    testImplementation(libs.androidx.test.runner)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.ui.test.junit4)
    testImplementation(libs.navigation.testing)
    testImplementation(libs.hilt.android.testing)
    kaptTest(libs.hilt.android.compiler)

    // ===== ANDROID INSTRUMENTED TESTING DEPENDENCIES =====
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.navigation.testing)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.androidx.espresso.contrib)
    androidTestImplementation(libs.androidx.espresso.intents)
    
    // Hilt testing dependencies with KAPT
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.android.compiler)

    // Test Orchestrator for better test isolation
    androidTestUtil("androidx.test:orchestrator:1.5.1")
}

// KAPT configuration for Hilt with performance optimizations
// Key optimizations applied: fastInit, useBuildCache, disabled formatGeneratedSource

// CRITICAL FIX: Configure KAPT worker process memory to prevent timeouts (2025)
tasks.withType<org.jetbrains.kotlin.gradle.internal.KaptWithoutKotlincTask>().configureEach {
    kaptProcessJvmArgs.addAll(listOf(
        "-Xmx2048m",
        "-XX:+UseParallelGC",
        "-XX:MaxMetaspaceSize=512m"
    ))
}

// Secrets Gradle Plugin Configuration
// This plugin automatically:
// 1. Loads API keys from secrets.properties (real keys, git-ignored)
// 2. Falls back to local.defaults.properties (placeholders, git-tracked)
// 3. Generates BuildConfig fields: MAPS_API_KEY, GEOCODING_API_KEY, etc.
// 4. Creates AndroidManifest placeholders: ${MAPS_API_KEY}
secrets {
    // Properties file with real API keys (excluded from git)
    propertiesFileName = "secrets.properties"

    // Default properties file with placeholder values (included in git)
    defaultPropertiesFileName = "local.defaults.properties"

    // Keys to ignore (optional)
    ignoreList.add("sdk.dir") // Ignore Android SDK path
}