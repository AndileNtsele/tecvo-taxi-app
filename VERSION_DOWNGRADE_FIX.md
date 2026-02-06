# VERSION DOWNGRADE + KSP REMOVAL FIX - COMPLETED

## Root Cause
1. Kotlin 2.0.21 produces metadata version 2.2.0, but Hilt 2.50/2.51 only supports up to 2.0.0
2. Switched to KAPT, so KSP plugin is no longer needed

## Solution
Downgraded to working versions + removed KSP plugin

## Changes Made

### 1. gradle/libs.versions.toml
```kotlin
// VERSION CHANGES
agp = "8.13.2"        (was "9.0.0")
kotlin = "2.0.0"      (was "2.0.21")
hilt = "2.49"         (was "2.50")
hiltCompiler = "2.48" (was "2.50")

// REMOVED (no longer needed with KAPT)
ksp = "2.0.0-1.0.28"  (deleted)

// REMOVED from [plugins] section
ksp = { id = "com.google.devtools.ksp", ... } (deleted)
```

### 2. build.gradle.kts (root)
```kotlin
// VERSION CHANGES
id("com.google.dagger.hilt.android") version "2.48" apply false  (was "2.51")
classpath("com.google.dagger:hilt-android-gradle-plugin:2.48")   (was "2.51")

// REMOVED (no longer needed with KAPT)
alias(libs.plugins.ksp) apply false  (deleted)
```

### 3. app/build.gradle.kts
Already configured with KAPT from previous migration.

## Next Steps

1. **Sync Gradle:**
   File → Sync Project with Gradle Files

2. **Clean Build:**
   ```bash
   ./gradlew clean
   ./gradlew build
   ```

3. **Or in Android Studio:**
   Build → Clean Project
   Build → Rebuild Project

## Why This Works
- Kotlin 2.0.0 + Hilt 2.48/2.49 = compatible metadata versions
- KAPT is more stable than KSP for Hilt
- No KSP plugin = no version conflicts

These are the exact versions from your working backup.
