@echo off
REM Play Store Release Validation Script for Windows
REM This script checks if your app is ready for Play Store submission

echo =========================================
echo TAXI APP - PLAY STORE READINESS CHECK
echo =========================================
echo.

setlocal enabledelayedexpansion
set ALL_PASS=1

echo 1. CHECKING CRITICAL CONFIGURATIONS
echo -----------------------------------

REM Check if package name has been changed
findstr /C:"com.example.taxi" app\build.gradle.kts >nul 2>&1
if %errorlevel% equ 0 (
    echo [X] Package name still contains 'com.example' - MUST BE CHANGED!
    set ALL_PASS=0
) else (
    echo [OK] Package name check passed
)

REM Check for keystore configuration
if exist "keystore.properties" (
    echo [OK] Keystore properties found
) else (
    echo [X] Keystore properties NOT FOUND - Create from template
    set ALL_PASS=0
)

if exist "release-keystore.jks" (
    echo [OK] Release keystore found
) else (
    echo [X] Release keystore NOT FOUND
    echo     Generate with: keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias taxi-release-key
    set ALL_PASS=0
)

echo.
echo 2. CHECKING FIREBASE CONFIGURATION
echo -----------------------------------

if exist "app\google-services.json" (
    echo [OK] Firebase configuration found
) else (
    echo [X] google-services.json NOT FOUND - Download from Firebase Console
    set ALL_PASS=0
)

echo.
echo 3. CHECKING BUILD FILES
echo ------------------------

if exist "app\proguard-rules.pro" (
    echo [OK] ProGuard rules found
) else (
    echo [X] ProGuard rules NOT FOUND
    set ALL_PASS=0
)

if exist "local.properties" (
    echo [OK] Local properties found
) else (
    echo [X] local.properties NOT FOUND - Copy from template and add API keys
    set ALL_PASS=0
)

echo.
echo 4. CHECKING APP RESOURCES
echo --------------------------

if exist "app\src\main\res\mipmap-hdpi\ic_launcher.png" (
    echo [OK] App icon (hdpi) found
) else (
    echo [X] App icons NOT FOUND - Generate using Android Studio's Image Asset tool
    set ALL_PASS=0
)

if exist "app\src\main\res\mipmap-xxxhdpi\ic_launcher.png" (
    echo [OK] App icon (xxxhdpi) found
) else (
    echo [X] High-res app icon NOT FOUND
    set ALL_PASS=0
)

echo.
echo 5. CHECKING BUILD OUTPUT
echo -------------------------

if exist "app\build\outputs\bundle\release\app-release.aab" (
    echo [OK] Release AAB found
    for %%I in ("app\build\outputs\bundle\release\app-release.aab") do echo      Size: %%~zI bytes
) else (
    echo [!] Release AAB not found - Build with: gradlew.bat bundleRelease
)

echo.
echo 6. CHECKING GRADLE
echo ------------------

if exist "gradlew.bat" (
    echo [OK] Gradle wrapper found
    
    REM Try to run a simple gradle task
    call gradlew.bat tasks >nul 2>&1
    if %errorlevel% equ 0 (
        echo [OK] Gradle configuration valid
    ) else (
        echo [X] Gradle configuration has issues
        set ALL_PASS=0
    )
) else (
    echo [X] Gradle wrapper not found
    set ALL_PASS=0
)

echo.
echo 7. API KEYS CHECK
echo -----------------

if exist "local.properties" (
    findstr /C:"MAPS_API_KEY=" local.properties >nul 2>&1
    if %errorlevel% equ 0 (
        findstr /C:"GEOCODING_API_KEY=" local.properties >nul 2>&1
        if %errorlevel% equ 0 (
            echo [OK] API keys configured
        ) else (
            echo [!] Some API keys may be missing
        )
    ) else (
        echo [!] API keys missing in local.properties
    )
)

echo.
echo =========================================
echo VALIDATION SUMMARY
echo =========================================

if %ALL_PASS% equ 1 (
    echo.
    echo SUCCESS: Most checks passed!
    echo.
    echo CRITICAL: Make sure you have changed the package name from com.example.taxi!
    echo.
    echo NEXT STEPS:
    echo 1. Take screenshots of your app (2-8 screenshots^)
    echo 2. Create a feature graphic (1024x500 px^)
    echo 3. Write app descriptions
    echo 4. Create Google Play Developer account ($25 fee^)
    echo 5. Upload AAB to Internal Testing first
    echo 6. Test thoroughly before Production release
) else (
    echo.
    echo FAILED: Some critical checks failed!
    echo.
    echo Please fix the issues above before submitting to Play Store.
    echo The most critical issue is changing the package name from com.example.taxi!
)

echo.
echo For detailed instructions, see:
echo - PLAYSTORE_RELEASE_SETUP.md
echo - The Play Store Release Checklist

echo.
echo =========================================
echo HELPFUL COMMANDS
echo =========================================
echo Build release AAB:    gradlew.bat bundleRelease
echo Clean project:        gradlew.bat clean
echo Run tests:           gradlew.bat test
echo Check dependencies:  gradlew.bat dependencies
echo.

pause