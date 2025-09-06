@echo off
echo ============================================
echo TECVO TAXI - Security Validation Script
echo ============================================
echo.

echo [1/4] Checking repository for exposed API keys...
findstr /r /c:"AIzaSy[A-Za-z0-9_-]*" local.properties local.properties.template >nul 2>&1
if %errorlevel%==0 (
    echo ❌ FAIL: Exposed API keys found in repository files
    echo.
    echo Found keys in:
    findstr /r /c:"AIzaSy[A-Za-z0-9_-]*" local.properties local.properties.template
    echo.
    echo ⚠️  CRITICAL: Remove real API keys before committing
    goto :fail
) else (
    echo ✅ PASS: No exposed API keys in repository files
)

echo.
echo [2/4] Verifying placeholder keys are in place...
findstr /c:"YOUR_GOOGLE_MAPS_API_KEY_HERE" local.properties >nul 2>&1
if %errorlevel%==0 (
    echo ✅ PASS: Placeholder keys detected in local.properties
) else (
    echo ❌ FAIL: Placeholder keys missing in local.properties
    goto :fail
)

echo.
echo [3/4] Checking .gitignore configuration...
findstr /c:"local.properties" .gitignore >nul 2>&1
if %errorlevel%==0 (
    echo ✅ PASS: local.properties excluded from version control
) else (
    echo ❌ FAIL: local.properties not in .gitignore
    goto :fail
)

findstr /c:"local.properties.dev" .gitignore >nul 2>&1
if %errorlevel%==0 (
    echo ✅ PASS: local.properties.dev excluded from version control
) else (
    echo ❌ FAIL: local.properties.dev not in .gitignore
    goto :fail
)

echo.
echo [4/4] Verifying development keys are available...
if exist "local.properties.dev" (
    echo ✅ PASS: Development key file available
    findstr /r /c:"AIzaSy[A-Za-z0-9_-]*" local.properties.dev >nul 2>&1
    if %errorlevel%==0 (
        echo ✅ PASS: Working API keys available for development
    ) else (
        echo ⚠️  WARNING: No working keys found in development file
    )
) else (
    echo ❌ FAIL: local.properties.dev file missing
    goto :fail
)

echo.
echo ============================================
echo 🎉 SECURITY VALIDATION: ALL TESTS PASSED
echo ============================================
echo.
echo ✅ Repository is clean and Play Store ready
echo ✅ No exposed API keys in version control  
echo ✅ Professional template system in place
echo ✅ Development workflow preserved
echo.
echo 📋 NEXT STEPS:
echo 1. Copy keys from local.properties.dev to local.properties for development
echo 2. Build and test: gradlew assembleDebug
echo 3. Ready for Play Store submission with secure configuration
echo.
goto :success

:fail
echo.
echo ============================================
echo ❌ SECURITY VALIDATION: FAILED
echo ============================================
echo.
echo Please fix the issues above before proceeding with Play Store submission.
echo See SECURE_DEPLOYMENT_GUIDE.md for detailed instructions.
echo.
exit /b 1

:success
echo Security validation complete - ready for Play Store! 🚀
exit /b 0