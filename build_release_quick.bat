@echo off
color 0A
cls

echo ==========================================
echo    QUICK RELEASE BUILD AND TEST
echo ==========================================
echo.

REM Check if keystore exists
if not exist "release-keystore.jks" (
    echo [ERROR] release-keystore.jks not found!
    echo.
    echo Please create it first with:
    echo keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias taxi-release-key
    echo.
    pause
    exit /b 1
)

REM Check if keystore.properties has been updated
findstr /C:"REPLACE_WITH_YOUR_PASSWORD" keystore.properties >nul 2>&1
if %errorlevel% equ 0 (
    echo [ERROR] You haven't updated keystore.properties!
    echo.
    echo Please edit keystore.properties and replace:
    echo   REPLACE_WITH_YOUR_PASSWORD
    echo with your actual keystore password.
    echo.
    pause
    exit /b 1
)

echo [1/4] Cleaning project...
call gradlew.bat clean
if %errorlevel% neq 0 (
    echo [ERROR] Clean failed!
    pause
    exit /b 1
)

echo.
echo [2/4] Building Release AAB...
echo This may take a few minutes...
call gradlew.bat bundleRelease
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Build failed!
    echo.
    echo Common issues:
    echo - Check keystore.properties has correct passwords
    echo - Check release-keystore.jks exists
    echo - Check for compilation errors above
    echo.
    pause
    exit /b 1
)

echo.
echo ==========================================
echo    BUILD SUCCESSFUL!
echo ==========================================
echo.

REM Check if AAB was created
if exist "app\build\outputs\bundle\release\app-release.aab" (
    echo [SUCCESS] Release AAB created!
    echo.
    
    REM Get file size
    for %%F in ("app\build\outputs\bundle\release\app-release.aab") do (
        set /a size=%%~zF/1048576
        echo File: app\build\outputs\bundle\release\app-release.aab
        echo Size: !size! MB
    )
    echo.
    
    echo [3/4] Extracting APK for testing...
    
    REM Check if bundletool exists
    if not exist "bundletool.jar" (
        echo.
        echo Downloading bundletool...
        powershell -Command "Invoke-WebRequest -Uri 'https://github.com/google/bundletool/releases/latest/download/bundletool-all.jar' -OutFile 'bundletool.jar'"
    )
    
    if exist "bundletool.jar" (
        echo Generating universal APK from AAB...
        java -jar bundletool.jar build-apks --bundle=app\build\outputs\bundle\release\app-release.aab --output=test.apks --mode=universal --ks=release-keystore.jks --ks-key-alias=taxi-release-key
        
        if %errorlevel% equ 0 (
            echo [4/4] APK generated successfully!
            echo.
            echo ==========================================
            echo    NEXT STEPS
            echo ==========================================
            echo.
            echo 1. TEST YOUR APP:
            echo    - Install test APK on a real device
            echo    - Test all critical features:
            echo      * Login with Google
            echo      * Maps display
            echo      * Location tracking
            echo      * Role selection (Driver/Passenger)
            echo      * Firebase connection
            echo.
            echo 2. PREPARE STORE LISTING:
            echo    - Take 2-8 screenshots
            echo    - Create feature graphic (1024x500)
            echo    - Write descriptions
            echo.
            echo 3. UPLOAD TO PLAY CONSOLE:
            echo    File ready: app\build\outputs\bundle\release\app-release.aab
            echo.
        ) else (
            echo [WARNING] Could not generate test APK, but AAB is ready!
            echo You'll need to enter your keystore password when prompted.
        )
    )
) else (
    echo [ERROR] AAB file not found!
    echo Expected at: app\build\outputs\bundle\release\app-release.aab
)

echo.
echo Press any key to open the output folder...
pause >nul
explorer "app\build\outputs\bundle\release"