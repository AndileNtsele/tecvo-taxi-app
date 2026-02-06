# Quick Robo Test Commands

## Current Build
```bash
# APK Location
app\build\outputs\apk\debug\app-debug.apk
# Size: 28 MB
```

## Option 1: Firebase Test Lab (Web UI) - EASIEST
1. Go to: https://console.firebase.google.com
2. Select your TAXI project
3. Click "Test Lab" → "Run a test"
4. Upload `app\build\outputs\apk\debug\app-debug.apk`
5. Select devices (phones only):
   - Pixel 5 (Android 12)
   - Samsung Galaxy S21 (Android 11)
6. Optional: Upload `robo-script.json`
7. Click "Run"
8. Wait 15 minutes for results

## Option 2: gcloud CLI (After Installation)
```bash
# Install gcloud first
# Download from: https://cloud.google.com/sdk/docs/install

# Initialize
gcloud init
gcloud auth login
gcloud config set project YOUR_FIREBASE_PROJECT_ID

# Run Robo test on Pixel 5
gcloud firebase test android run \
  --type robo \
  --app app\build\outputs\apk\debug\app-debug.apk \
  --device model=Pixel5,version=30,locale=en_ZA,orientation=portrait \
  --timeout 5m \
  --robo-script robo-script.json

# Run on multiple devices
gcloud firebase test android run \
  --type robo \
  --app app\build\outputs\apk\debug\app-debug.apk \
  --device model=Pixel5,version=30 \
  --device model=Galaxy S21,version=30 \
  --device model=Pixel3,version=28 \
  --timeout 5m \
  --robo-script robo-script.json
```

## Option 3: Play Console Pre-launch Report - MOST COMPREHENSIVE
```bash
# 1. Build release APK
gradlew.bat assembleRelease

# 2. Upload to Play Console
# - Go to: https://play.google.com/console
# - Internal Testing → Upload APK
# - Wait 30 minutes for automatic Robo testing on 20+ devices

# 3. View results
# - Release → Testing → Pre-launch report
```

## Test Credentials
```
Phone: 072 858 8857
OTP: 123456
```

## Rebuild APK if Needed
```bash
# Debug build
gradlew.bat assembleDebug

# Release build
gradlew.bat assembleRelease

# Clean build
gradlew.bat clean assembleDebug
```

## Recommended Devices (Phone-only)
- Pixel 5 (Android 12) - Modern reference device
- Samsung Galaxy S21 (Android 11) - Popular SA device
- Samsung Galaxy Z Fold 3 (Android 12) - Foldable support test
- Pixel 3 (Android 10) - Older Android version

## What to Check in Results
✅ No crashes during normal flows
✅ Authentication completes successfully
✅ Role selection works (Driver/Passenger)
✅ Direction selection works (TOWN/LOCAL)
✅ Map loads without errors
✅ No tablet blocking dialogs on phones
✅ Foldable devices show constrained UI
✅ No ANRs (Application Not Responding)
✅ No security vulnerabilities
✅ Memory usage within normal bounds

## Next Steps
1. Choose your testing method (Web UI recommended for first time)
2. Run Robo test on 2-3 devices
3. Review results (crashes, screenshots, logs)
4. Fix any issues found
5. Re-test until clean
6. Proceed to Play Console upload for comprehensive testing
