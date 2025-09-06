@echo off
setlocal enabledelayedexpansion

REM Interactive Package Name Change Assistant
cls
color 0A

echo =========================================
echo    PACKAGE NAME CHANGE ASSISTANT
echo =========================================
echo.
echo This will help you change from com.example.taxi
echo to your own unique package name.
echo.
echo IMPORTANT: Your Firebase and Google Cloud are
echo already configured with com.example.taxi
echo.
echo =========================================
echo.

REM Check if backup exists
dir /b BACKUP_BEFORE_PACKAGE_CHANGE_* >nul 2>&1
if %errorlevel% neq 0 (
    echo [!] No backup found. Creating backup first...
    echo.
    call backup_before_package_change.bat
    echo.
)

echo What would you like to do?
echo.
echo [1] RECOMMENDED: Change package name (30 minutes)
echo [2] RISKY: Keep com.example.taxi (might be rejected)
echo [3] Read more about the implications
echo [4] Exit
echo.
set /p choice="Enter your choice (1-4): "

if "%choice%"=="1" goto :change_package
if "%choice%"=="2" goto :keep_package
if "%choice%"=="3" goto :read_more
if "%choice%"=="4" goto :exit

:change_package
cls
echo =========================================
echo    CHANGING PACKAGE NAME - STEP BY STEP
echo =========================================
echo.
echo Let's do this! Follow each step carefully.
echo.
echo STEP 1: Choose Your New Package Name
echo -------------------------------------
echo Format: com.yourcompany.appname
echo.
echo Examples:
echo - com.ridetaxi.app
echo - com.safetaxi.rider  
echo - za.co.yourcompany.taxi (South African format)
echo - com.yourname.taxiride
echo.
echo Rules:
echo - Must be globally unique
echo - Use only lowercase letters
echo - No spaces or special characters
echo - Don't use 'example' or 'test'
echo.
set /p newpackage="Enter your new package name: "
echo.
echo You entered: %newpackage%
set /p confirm="Is this correct? (y/n): "
if /i not "%confirm%"=="y" goto :change_package

echo.
echo =========================================
echo STEP 2: Update Firebase Console
echo =========================================
echo.
echo 1. Open your browser and go to:
echo    https://console.firebase.google.com
echo.
echo 2. Select your project
echo.
echo 3. Click the gear icon - Project Settings
echo.
echo 4. Scroll to "Your apps" section
echo.
echo 5. Click "Add app" - Android icon
echo.
echo 6. Enter package name: %newpackage%
echo.
echo 7. Download the NEW google-services.json
echo.
echo 8. Save it to your Desktop (for now)
echo.
pause
echo.

echo =========================================
echo STEP 3: Update Google Cloud Console
echo =========================================
echo.
echo 1. Open: https://console.cloud.google.com
echo.
echo 2. Select your project
echo.
echo 3. Go to: APIs and Services - Credentials
echo.
echo 4. Click on your Android API key
echo.
echo 5. Under "Application restrictions" ADD:
echo    - Package name: %newpackage%
echo    - Keep the old com.example.taxi for now
echo.
echo 6. Save the changes
echo.
pause
echo.

echo =========================================
echo STEP 4: Update Your Project Files
echo =========================================
echo.
echo Now let's update your Android Studio project:
echo.
echo 1. Open app\build.gradle.kts in a text editor
echo.
echo 2. Change these lines:
echo    namespace = "%newpackage%"
echo    applicationId = "%newpackage%"
echo.
echo 3. Save the file
echo.
echo 4. Copy the NEW google-services.json from Desktop
echo    to your app\ folder (replace the old one)
echo.
pause
echo.

echo =========================================
echo STEP 5: Update ProGuard Rules
echo =========================================
echo.
echo 1. Open app\proguard-rules.pro
echo.
echo 2. Find and replace com.example.taxi with %newpackage%
echo    (There should be about 5 occurrences)
echo.
echo 3. Save the file
echo.
pause
echo.

echo =========================================
echo STEP 6: Refactor in Android Studio
echo =========================================
echo.
echo 1. Open your project in Android Studio
echo.
echo 2. In Project view (left panel), switch to "Android" view
echo.
echo 3. Right-click on com.example.taxi package
echo.
echo 4. Select Refactor - Rename
echo.
echo 5. Rename each part to match: %newpackage%
echo.
echo 6. Click "Refactor" when prompted
echo.
echo 7. Wait for Android Studio to update all files
echo.
echo 8. File - Sync Project with Gradle Files
echo.
pause
echo.

echo =========================================
echo STEP 7: Clean and Test
echo =========================================
echo.
echo Let's make sure everything works!
echo.
pause

echo Cleaning project...
call gradlew.bat clean
echo.

echo Building debug version...
call gradlew.bat assembleDebug
echo.

if %errorlevel% equ 0 (
    echo.
    echo [OK] Build successful!
    echo.
    echo =========================================
    echo STEP 8: Test Your App
    echo =========================================
    echo.
    echo Install and test these features:
    echo - [ ] App installs successfully
    echo - [ ] Google Sign-In works
    echo - [ ] Maps display correctly
    echo - [ ] Location tracking works
    echo - [ ] Firebase Database works
    echo.
    echo APK location: app\build\outputs\apk\debug\app-debug.apk
    echo.
) else (
    echo.
    echo [!] Build failed. Check for errors and try again.
    echo Common issues:
    echo - Did you update build.gradle.kts?
    echo - Did you copy the new google-services.json?
    echo - Try: File - Invalidate Caches and Restart in Android Studio
    echo.
)

pause
goto :menu

:keep_package
cls
echo =========================================
echo    KEEPING com.example.taxi
echo =========================================
echo.
echo WARNING: This is risky and not recommended!
echo.
echo Risks:
echo - Google Play might reject your app
echo - Looks unprofessional to users
echo - Cannot transfer app ownership later
echo - Some features might be restricted
echo.
echo Some developers report success with com.example.*
echo but Google officially discourages it.
echo.
set /p confirm="Are you sure you want to keep com.example.taxi? (y/n): "
if /i not "%confirm%"=="y" goto :menu

echo.
echo OK, keeping com.example.taxi
echo.
echo Building release bundle...
call gradlew.bat bundleRelease
echo.
if %errorlevel% equ 0 (
    echo [OK] Release bundle created!
    echo Location: app\build\outputs\bundle\release\app-release.aab
    echo.
    echo You can now upload this to Play Store.
    echo If it gets rejected, you'll need to change the package name.
) else (
    echo [!] Build failed. Check the errors above.
)
echo.
pause
goto :menu

:read_more
cls
type CHECK_FIREBASE_CONFIG.md | more
echo.
pause
goto :menu

:menu
cls
echo =========================================
echo What would you like to do next?
echo =========================================
echo.
echo [1] Build release AAB
echo [2] Run validation check
echo [3] View migration guide
echo [4] Exit
echo.
set /p next="Enter your choice (1-4): "

if "%next%"=="1" (
    echo Building release bundle...
    call gradlew.bat bundleRelease
    pause
    goto :menu
)
if "%next%"=="2" (
    call validate_playstore_readiness.bat
    pause
    goto :menu
)
if "%next%"=="3" (
    start "" "CHECK_FIREBASE_CONFIG.md"
    goto :menu
)

:exit
echo.
echo Good luck with your Play Store submission!
echo.