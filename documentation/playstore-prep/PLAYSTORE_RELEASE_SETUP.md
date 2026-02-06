# IMMEDIATE SETUP STEPS FOR PLAY STORE RELEASE

## Step 1: Generate Your Release Keystore

Open Terminal/Command Prompt in your project directory and run:

```bash
keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias taxi-release-key
```

**IMPORTANT:** Save the passwords you enter! You'll need them forever. If you lose them, you can't update your app.

## Step 2: Create keystore.properties file

Create a new file `keystore.properties` in your project root (same level as local.properties):

```properties
storePassword=YOUR_KEYSTORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
keyAlias=taxi-release-key
storeFile=../release-keystore.jks
```

## Step 3: Update .gitignore

Add these lines to your .gitignore file:

```
release-keystore.jks
keystore.properties
*.aab
*.apk
```

## Step 4: Package Name Change Instructions

### CRITICAL: Change from com.example.taxi

1. **In Android Studio:**
   - Open `app/build.gradle.kts`
   - Change line 11: `namespace = "com.example.taxi"` 
   - Change line 14: `applicationId = "com.example.taxi"`
   - To something unique like: `com.yourname.ridetaxi` or `com.yourcompany.taxiapp`

2. **Refactor Package Structure:**
   - In Project view, switch to "Android" view
   - Right-click on `com.example.taxi` package
   - Select Refactor → Rename
   - Rename "example" to your company name
   - Rename "taxi" to your app name if desired
   - Click "Refactor" and let it update all files

3. **Update Firebase:**
   - Go to Firebase Console
   - Update your app's package name
   - Download new `google-services.json`
   - Replace the one in `app/` directory

## Step 5: Build Your Release AAB

```bash
# Clean everything first
./gradlew clean

# Build the release bundle
./gradlew bundleRelease
```

Your AAB file will be at: `app/build/outputs/bundle/release/app-release.aab`

## Step 6: Test Your Release Build

1. Download bundletool:
```bash
curl -L -o bundletool.jar https://github.com/google/bundletool/releases/latest/download/bundletool-all.jar
```

2. Generate APKs from your AAB:
```bash
java -jar bundletool.jar build-apks --bundle=app/build/outputs/bundle/release/app-release.aab --output=test.apks --mode=universal
```

3. Install on your device:
```bash
java -jar bundletool.jar install-apks --apks=test.apks
```

## CRITICAL WARNINGS

⚠️ **DO NOT PROCEED TO PLAY STORE WITH:**
- Package name containing "com.example"
- Missing keystore configuration
- Untested release build

⚠️ **KEEP YOUR KEYSTORE SAFE:**
- Back up `release-keystore.jks` in multiple secure locations
- Never commit it to Git
- If you lose it, you can NEVER update your app

## Next Steps After These Are Done:

1. Create app icons (use Android Studio's Image Asset tool)
2. Take professional screenshots
3. Write compelling app descriptions
4. Set up Privacy Policy website
5. Complete Play Console setup

---

**Need Help?** Common issues:

- Build fails after package rename: Do a full clean and rebuild
- Firebase stops working: Make sure you updated google-services.json
- Can't find AAB file: Look in app/build/outputs/bundle/release/
