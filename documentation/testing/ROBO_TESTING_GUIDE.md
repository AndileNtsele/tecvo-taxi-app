# Robo Testing Guide for TAXI App

## Current Status
- **APK Location**: `app\build\outputs\apk\debug\app-debug.apk`
- **APK Size**: 28 MB
- **Build Date**: November 13, 2025
- **gcloud CLI**: Not installed

## Robo Testing Options

### Option 1: Play Console Pre-launch Report (RECOMMENDED)
**Best for Play Store preparation - Free and automatic**

#### Why This Option?
- Completely FREE
- Tests on 20+ real devices automatically
- Required for Play Store anyway
- Most comprehensive coverage
- Official Google Play testing

#### Steps:
1. **Build Release APK**:
   ```bash
   gradlew.bat assembleRelease
   ```

2. **Sign APK** (if not already configured in `build.gradle.kts`)

3. **Upload to Play Console**:
   - Go to https://play.google.com/console
   - Navigate to your TAXI app
   - Create Internal Testing track (if not exists)
   - Upload the release APK
   - Promote to Internal Testing

4. **Access Pre-launch Report**:
   - Wait 15-30 minutes after upload
   - Go to "Release" → "Testing" → "Pre-launch report"
   - View Robo test results on 20+ devices

#### What You'll Get:
- Crash reports
- Screenshots of tested flows
- Performance metrics (ANRs, crashes)
- Accessibility issues
- Security vulnerabilities
- Test coverage visualization

---

### Option 2: Firebase Test Lab (Web UI)
**Good for quick testing before Play Store submission**

#### Free Tier Limits:
- 5 tests/day on virtual devices
- 10 tests/day on physical devices
- Limited to 1 hour/test

#### Steps:
1. **Access Firebase Console**:
   - Go to https://console.firebase.google.com
   - Select your TAXI app project
   - Navigate to "Test Lab" in left menu

2. **Run Robo Test**:
   - Click "Run a test"
   - Select "Robo test"
   - Upload: `app\build\outputs\apk\debug\app-debug.apk`

3. **Configure Test**:
   - **Device Selection** (Phone-only for TAXI app):
     - Pixel 5 (Android 12)
     - Samsung Galaxy S21 (Android 11)
     - Pixel 3 (Android 10)
   - **Avoid tablets** - App blocks them
   - **Test timeout**: 5 minutes (default)

4. **Optional: Add Robo Script** (see below)

5. **Run Test** and wait 10-15 minutes for results

#### What You'll Get:
- Activity map showing tested flows
- Screenshots at each step
- Crash logs and stack traces
- Video recording of test execution
- Logcat output

---

### Option 3: Install gcloud CLI (Command Line)
**Best for automation and CI/CD**

#### Installation:
1. Download from: https://cloud.google.com/sdk/docs/install
2. Run installer
3. Initialize: `gcloud init`
4. Authenticate: `gcloud auth login`
5. Set project: `gcloud config set project YOUR_PROJECT_ID`

#### Run Robo Test:
```bash
gcloud firebase test android run \
  --type robo \
  --app app\build\outputs\apk\debug\app-debug.apk \
  --device model=Pixel5,version=30,locale=en_ZA,orientation=portrait \
  --timeout 5m \
  --results-bucket=gs://your-bucket-name \
  --robo-script=robo-script.json
```

---

## TAXI App-Specific Test Configuration

### Test Credentials
```
Phone: 072 858 8857
OTP: 123456
```

### Robo Script for Authentication
Create `robo-script.json` to guide Robo through OTP flow:

```json
{
  "crawl": {
    "maxSteps": 100,
    "maxDepth": 50,
    "maxScreenshots": 200,
    "loginCredentials": {
      "inputFields": [
        {
          "resourceId": "com.tecvo.taxi:id/phoneNumberInput",
          "text": "0728588857"
        },
        {
          "resourceId": "com.tecvo.taxi:id/otpInput",
          "text": "123456"
        }
      ]
    }
  }
}
```

### Critical Test Scenarios for TAXI App

#### 1. Phone-Only Validation
- **Expected**: App should work on phones
- **Expected**: Should NOT see tablet blocking dialog on phones
- **Devices to test**: Pixel 5, Samsung Galaxy S21, Samsung Galaxy Z Fold (foldable)

#### 2. Authentication Flow
- **Flow**: Phone entry → OTP screen → Verification → Home screen
- **Challenge**: Robo may struggle with OTP auto-fill
- **Solution**: Use robo-script.json with test credentials

#### 3. Permission Handling
- **Required**: Location permissions
- **Expected**: Graceful permission request handling
- **Test**: Both granted and denied scenarios

#### 4. Core User Flows
1. **Driver Flow**: Login → Driver role → TOWN direction → Map screen
2. **Passenger Flow**: Login → Passenger role → LOCAL direction → Map screen
3. **Firebase Cleanup**: Leave map → Data cleanup verification

#### 5. Offline Handling
- **Test**: Network disconnect scenarios
- **Expected**: Offline banner display
- **Expected**: Graceful reconnection

---

## Recommended Testing Strategy

### Phase 1: Quick Validation (Firebase Test Lab Web UI)
1. Upload debug APK
2. Test on 2-3 phone devices (Pixel, Samsung)
3. Check for crashes and major issues
4. **Time**: 15-20 minutes

### Phase 2: Comprehensive Testing (Play Console Pre-launch)
1. Build release APK
2. Upload to Internal Testing track
3. Wait for automatic Robo testing (20+ devices)
4. Review comprehensive report
5. **Time**: 30-60 minutes (mostly waiting)

### Phase 3: Iterative Fixes
1. Address crashes and issues from reports
2. Re-run tests
3. Validate fixes
4. **Repeat until clean**

---

## Expected Robo Test Results for TAXI App

### ✅ What Should Pass:
- App launches successfully
- Login screen loads
- Phone number input works
- Role selection (Driver/Passenger) works
- Direction selection (TOWN/LOCAL) works
- Map screen loads (may not show markers without real Firebase data)
- Phone-only restrictions work (no tablet blocking on phones)
- Foldable phone support (constrained UI on large screens)
- Back navigation works
- App doesn't crash on common user flows

### ⚠️ Expected Challenges:
- **OTP verification**: Robo may get stuck at OTP screen (no real SMS)
  - **Solution**: Use robo-script.json with test OTP
- **Firebase data**: May not see real taxi/passenger markers
  - **Not critical**: Robo tests app stability, not live data
- **Location permissions**: Robo may not grant all permissions
  - **Review manually**: Check permission dialogs in screenshots

### ❌ What Should NOT Happen:
- Crashes during normal navigation
- ANRs (Application Not Responding)
- Security vulnerabilities detected
- Memory leaks
- Tablet blocking dialogs on phone devices

---

## Device Recommendations for Testing

### Priority 1: Most Common SA Devices
1. **Samsung Galaxy S21** - Very popular in SA
2. **Pixel 5** - Clean Android reference
3. **Samsung Galaxy A52** - Mid-range popular model

### Priority 2: Foldable Testing
4. **Samsung Galaxy Z Fold 3/4** - Test foldable support
   - Should show phone-constrained UI when unfolded

### Priority 3: Different Android Versions
5. **Android 10** (Pixel 3)
6. **Android 11** (Samsung S21)
7. **Android 12** (Pixel 5)
8. **Android 13** (Pixel 7)

### ❌ Avoid Tablets:
- Nexus 9, Pixel C, etc. - App will block them

---

## Next Steps

1. **Choose your testing method** based on timeline and budget:
   - **Fast & Free**: Firebase Test Lab Web UI (2-3 devices)
   - **Comprehensive**: Play Console Pre-launch Report (20+ devices)
   - **Advanced**: Install gcloud CLI for automation

2. **Prepare test configuration**:
   - Copy `robo-script.json` template above
   - Update resource IDs if needed (check your layout files)

3. **Run tests** and wait for results

4. **Review reports** for:
   - Crashes and exceptions
   - UI/UX issues
   - Performance problems
   - Security vulnerabilities

5. **Fix issues** and re-test until clean

---

## Support Resources

- **Firebase Test Lab Docs**: https://firebase.google.com/docs/test-lab
- **Play Console Testing**: https://support.google.com/googleplay/android-developer/answer/9844679
- **gcloud CLI Install**: https://cloud.google.com/sdk/docs/install
- **Robo Test Guide**: https://firebase.google.com/docs/test-lab/android/robo-ux-test

---

## Questions?

If you need help with any step, let me know! I can assist with:
- Setting up Firebase Test Lab
- Creating custom Robo scripts
- Interpreting test results
- Fixing issues discovered during testing
