@echo off
REM Backup current configuration before package name change

echo =========================================
echo BACKING UP CURRENT CONFIGURATION
echo =========================================
echo.

REM Create backup directory with timestamp
set BACKUP_DIR=BACKUP_BEFORE_PACKAGE_CHANGE_%date:~-4%%date:~3,2%%date:~0,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set BACKUP_DIR=%BACKUP_DIR: =0%
mkdir %BACKUP_DIR% 2>nul

echo Creating backup in: %BACKUP_DIR%
echo.

REM Backup critical files
echo Backing up Firebase configuration...
copy "app\google-services.json" "%BACKUP_DIR%\google-services.json.backup" >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] google-services.json backed up
) else (
    echo [!] Could not backup google-services.json
)

echo Backing up build configuration...
copy "app\build.gradle.kts" "%BACKUP_DIR%\build.gradle.kts.backup" >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] build.gradle.kts backed up
) else (
    echo [!] Could not backup build.gradle.kts
)

echo Backing up local properties...
copy "local.properties" "%BACKUP_DIR%\local.properties.backup" >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] local.properties backed up
) else (
    echo [!] Could not backup local.properties
)

echo Backing up AndroidManifest...
copy "app\src\main\AndroidManifest.xml" "%BACKUP_DIR%\AndroidManifest.xml.backup" >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] AndroidManifest.xml backed up
) else (
    echo [!] Could not backup AndroidManifest.xml
)

echo Backing up ProGuard rules...
copy "app\proguard-rules.pro" "%BACKUP_DIR%\proguard-rules.pro.backup" >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] proguard-rules.pro backed up
) else (
    echo [!] Could not backup proguard-rules.pro
)

REM Create a configuration record
echo Creating configuration record...
(
echo Configuration Backup Record
echo ===========================
echo Date: %date% %time%
echo.
echo Current Package Name: com.example.taxi
echo Current Namespace: com.example.taxi
echo.
echo Files Backed Up:
echo - google-services.json
echo - build.gradle.kts
echo - local.properties
echo - AndroidManifest.xml
echo - proguard-rules.pro
echo.
echo Firebase Project Info:
echo ----------------------
echo Check your Firebase Console for:
echo - Project ID
echo - Web API Key
echo - Database URL
echo.
echo Google Cloud Info:
echo ------------------
echo Check Google Cloud Console for:
echo - API Keys and their restrictions
echo - Enabled APIs
echo - OAuth consent screen settings
echo.
echo RESTORE INSTRUCTIONS:
echo ---------------------
echo If you need to restore, copy files from this backup folder
echo back to their original locations.
echo.
echo NEXT STEPS:
echo -----------
echo 1. Change package name in build.gradle.kts
echo 2. Refactor package in Android Studio
echo 3. Add new app to Firebase Console
echo 4. Download new google-services.json
echo 5. Update Google Cloud API restrictions
echo 6. Test everything thoroughly
) > "%BACKUP_DIR%\BACKUP_INFO.txt"

echo [OK] Configuration record created
echo.

REM Display current configuration
echo =========================================
echo CURRENT CONFIGURATION SUMMARY
echo =========================================
echo.
echo Package Name: com.example.taxi

findstr /C:"MAPS_API_KEY" local.properties >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Maps API Key configured
) else (
    echo [!] Maps API Key not found
)

findstr /C:"GEOCODING_API_KEY" local.properties >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Geocoding API Key configured
) else (
    echo [!] Geocoding API Key not found
)

findstr /C:"FIREBASE_DATABASE_URL" local.properties >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Firebase Database URL configured
) else (
    echo [!] Firebase Database URL not found
)

echo.
echo =========================================
echo BACKUP COMPLETE!
echo =========================================
echo.
echo Backup saved to: %BACKUP_DIR%
echo.
echo You can now safely proceed with package name change.
echo If anything goes wrong, restore from this backup.
echo.
echo RECOMMENDED NEXT STEPS:
echo ------------------------
echo 1. Open Firebase Console in your browser
echo 2. Go to Project Settings
echo 3. Add new Android app with new package name
echo 4. Follow the Firebase Migration Guide
echo.
echo Press any key to open the migration guide...
pause >nul

REM Try to open the migration guide
if exist "CHECK_FIREBASE_CONFIG.md" (
    start "" "CHECK_FIREBASE_CONFIG.md"
)