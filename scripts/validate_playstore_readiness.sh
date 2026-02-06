#!/bin/bash

# Play Store Release Validation Script
# This script checks if your app is ready for Play Store submission

echo "========================================="
echo "TAXI APP - PLAY STORE READINESS CHECK"
echo "========================================="
echo ""

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Track if all checks pass
ALL_PASS=true

# Function to check if file exists
check_file() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} $2 found"
        return 0
    else
        echo -e "${RED}✗${NC} $2 NOT FOUND - $3"
        ALL_PASS=false
        return 1
    fi
}

# Function to check if directory exists
check_dir() {
    if [ -d "$1" ]; then
        echo -e "${GREEN}✓${NC} $2 found"
        return 0
    else
        echo -e "${RED}✗${NC} $2 NOT FOUND - $3"
        ALL_PASS=false
        return 1
    fi
}

# Function to check file content
check_content() {
    if grep -q "$2" "$1" 2>/dev/null; then
        echo -e "${RED}✗${NC} $3"
        ALL_PASS=false
        return 1
    else
        echo -e "${GREEN}✓${NC} $4"
        return 0
    fi
}

echo "1. CHECKING CRITICAL CONFIGURATIONS"
echo "-----------------------------------"

# Check if package name has been changed
check_content "app/build.gradle.kts" "com.example.taxi" \
    "Package name still contains 'com.example' - MUST BE CHANGED!" \
    "Package name check (ensure you've changed from com.example.taxi)"

# Check for keystore configuration
check_file "keystore.properties" "Keystore properties" \
    "Create keystore.properties from template"

check_file "release-keystore.jks" "Release keystore" \
    "Generate with: keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias taxi-release-key"

echo ""
echo "2. CHECKING FIREBASE CONFIGURATION"
echo "-----------------------------------"

check_file "app/google-services.json" "Firebase configuration" \
    "Download from Firebase Console"

echo ""
echo "3. CHECKING BUILD FILES"
echo "------------------------"

check_file "app/proguard-rules.pro" "ProGuard rules" \
    "ProGuard rules needed for release build"

check_file "local.properties" "Local properties" \
    "Copy from local.properties.template and add your API keys"

echo ""
echo "4. CHECKING APP RESOURCES"
echo "--------------------------"

# Check for app icons
check_file "app/src/main/res/mipmap-hdpi/ic_launcher.png" "App icon (hdpi)" \
    "Generate icons using Android Studio's Image Asset tool"

check_file "app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" "App icon (xxxhdpi)" \
    "Generate icons using Android Studio's Image Asset tool"

echo ""
echo "5. CHECKING LEGAL DOCUMENTS"
echo "----------------------------"

check_file "PRIVACY_POLICY_TEMPLATE.md" "Privacy Policy template" \
    "Customize and host on your website"

echo ""
echo "6. CHECKING BUILD OUTPUT"
echo "-------------------------"

# Check if release AAB exists
if [ -f "app/build/outputs/bundle/release/app-release.aab" ]; then
    echo -e "${GREEN}✓${NC} Release AAB found"
    # Get file size
    AAB_SIZE=$(du -h app/build/outputs/bundle/release/app-release.aab | cut -f1)
    echo "  Size: $AAB_SIZE"
    
    # Check if it's signed
    if command -v jarsigner &> /dev/null; then
        if jarsigner -verify app/build/outputs/bundle/release/app-release.aab &> /dev/null; then
            echo -e "${GREEN}✓${NC} AAB is signed"
        else
            echo -e "${YELLOW}⚠${NC} AAB may not be properly signed"
        fi
    fi
else
    echo -e "${YELLOW}⚠${NC} Release AAB not found - Build with: ./gradlew bundleRelease"
fi

echo ""
echo "7. RUNNING GRADLE CHECKS"
echo "-------------------------"

# Check if gradlew exists and is executable
if [ -x "./gradlew" ]; then
    echo -e "${GREEN}✓${NC} Gradle wrapper found"
    
    # Try to run a simple gradle task
    if ./gradlew tasks --quiet &> /dev/null; then
        echo -e "${GREEN}✓${NC} Gradle configuration valid"
    else
        echo -e "${RED}✗${NC} Gradle configuration has issues"
        ALL_PASS=false
    fi
else
    echo -e "${RED}✗${NC} Gradle wrapper not found or not executable"
    ALL_PASS=false
fi

echo ""
echo "8. API KEYS CHECK"
echo "-----------------"

# Check for API keys in local.properties
if [ -f "local.properties" ]; then
    if grep -q "MAPS_API_KEY=" local.properties && grep -q "GEOCODING_API_KEY=" local.properties; then
        echo -e "${GREEN}✓${NC} API keys configured in local.properties"
    else
        echo -e "${YELLOW}⚠${NC} Some API keys may be missing in local.properties"
    fi
else
    echo -e "${RED}✗${NC} local.properties not found"
    ALL_PASS=false
fi

echo ""
echo "========================================="
echo "VALIDATION SUMMARY"
echo "========================================="

if [ "$ALL_PASS" = true ]; then
    echo -e "${GREEN}✓ ALL CHECKS PASSED!${NC}"
    echo ""
    echo "Your app appears to be ready for Play Store submission!"
    echo ""
    echo "NEXT STEPS:"
    echo "1. Take screenshots of your app (2-8 screenshots)"
    echo "2. Create a feature graphic (1024x500 px)"
    echo "3. Write app description (short: 80 chars, full: 4000 chars)"
    echo "4. Create a Google Play Developer account ($25 one-time fee)"
    echo "5. Upload your AAB to Internal Testing first"
    echo "6. Test thoroughly before promoting to Production"
else
    echo -e "${RED}✗ SOME CHECKS FAILED${NC}"
    echo ""
    echo "Please fix the issues above before submitting to Play Store."
    echo "The most critical issue is changing the package name from com.example.taxi!"
fi

echo ""
echo "For detailed instructions, see:"
echo "- PLAYSTORE_RELEASE_SETUP.md"
echo "- The Play Store Release Checklist artifact"

# Additional helpful commands
echo ""
echo "========================================="
echo "HELPFUL COMMANDS"
echo "========================================="
echo "Build release AAB:    ./gradlew bundleRelease"
echo "Clean project:        ./gradlew clean"
echo "Run tests:           ./gradlew test"
echo "Check dependencies:  ./gradlew dependencies"