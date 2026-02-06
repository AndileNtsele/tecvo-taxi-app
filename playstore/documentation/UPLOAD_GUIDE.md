# 🚀 PLAY STORE UPLOAD GUIDE

## STEP-BY-STEP UPLOAD PROCESS

---

## 📦 **PHASE 1: BUILD RELEASE AAB** (15 minutes)

### **1.1 Clean Previous Builds**
```bash
cd "C:\Users\ntsel\AndroidStudioProjects\TAXI - 03"
gradlew.bat clean
```

### **1.2 Build Release Bundle**
```bash
gradlew.bat bundleRelease
```

**Output location:**
```
app\build\outputs\bundle\release\app-release.aab
```

### **1.3 Verify Build Success**
Check terminal output for:
```
BUILD SUCCESSFUL in Xs
```

### **1.4 Archive Build**
```bash
copy "app\build\outputs\bundle\release\app-release.aab" "playstore\builds\app-release-v1.0.1.aab"
```

---

## 🌐 **PHASE 2: PLAY CONSOLE SETUP** (First time only)

### **2.1 Access Play Console**
Visit: https://play.google.com/console

### **2.2 Create App**
1. Click **"Create app"**
2. App details:
   - **App name:** TAXI - SA Taxi Visibility
   - **Default language:** English (United States)
   - **App or game:** App
   - **Free or paid:** Free

3. Declarations:
   - [ ] App follows Developer Program Policies
   - [ ] App complies with US export laws

4. Click **"Create app"**

---

## 📝 **PHASE 3: STORE LISTING** (30 minutes)

### **3.1 Navigate to Store Listing**
Dashboard → Store presence → Main store listing

### **3.2 App Details**
Copy from: `playstore/descriptions/`

**App name:**
```
TAXI - SA Taxi Visibility
```

**Short description:** (80 characters max)
```
Real-time taxi visibility for SA commuters - see available taxis & passengers
```

**Full description:** (Copy entire content from full-description.txt)

### **3.3 Graphics**
Upload from: `playstore/graphics/`

**App icon:**
- File: `icon-512.png`
- Size: 512 x 512 pixels
- Format: PNG

**Feature graphic:**
- File: `feature-graphic.png`
- Size: 1024 x 500 pixels
- Format: PNG

**Phone screenshots:**
Upload all 7 from `playstore/graphics/screenshots/`:
1. 01-login-screen.png
2. 02-role-selection.png
3. 03-direction-choice.png
4. 04-map-driver-view.png
5. 05-map-passenger-view.png
6. 06-settings.png
7. 07-additional-view.png

### **3.4 Categorization**
- **App category:** Maps & Navigation
- **Tags:** (Optional) taxi, transport, South Africa

### **3.5 Contact Details**
- **Email:** privacy@tecvo.com (or your support email)
- **Website:** (Optional) Your company website
- **Phone:** (Optional)

### **3.6 Privacy Policy**
From: `playstore/legal/privacy-policy-url.txt`

**Privacy policy URL:**
```
https://andilentsele.github.io/tecvo-taxi-app/
```

Click **"Save"**

---

## 🔒 **PHASE 4: DATA SAFETY** (15 minutes)

### **4.1 Navigate to Data Safety**
Dashboard → App content → Data safety

### **4.2 Data Collection Questions**

**Does your app collect or share user data?**
- Answer: **Yes**

**Data types collected:**

**Location:**
- [x] Approximate location
- [x] Precise location
- **Purpose:** App functionality (real-time taxi visibility)
- **Collection:** Required
- **Sharing:** Not shared with third parties
- **Ephemeral:** YES - temporary only while on map

**Personal info:**
- [x] Phone number
- **Purpose:** Account management (authentication)
- **Collection:** Required
- **Sharing:** Not shared with third parties

### **4.3 Security Practices**
- [x] Data is encrypted in transit
- [x] Users can request data deletion
- [x] Committed to Google Play Families Policy

### **4.4 Data Retention**
- **Location data:** Deleted automatically when user leaves map screen
- **Phone number:** Retained for authentication, can be deleted on request

Click **"Save"** and **"Submit"**

---

## 🎯 **PHASE 5: CONTENT RATING** (10 minutes)

### **5.1 Navigate to Content Rating**
Dashboard → App content → Content rating

### **5.2 Start Questionnaire**
Select: **IARC questionnaire**

### **5.3 Answer Questions**
**Category:** Apps

**Violence:**
- Realistic violence? **No**
- Unrealistic violence? **No**

**Sexual Content:**
- Sexual themes? **No**
- Nudity? **No**

**Language:**
- Profanity? **No**

**Controlled Substances:**
- Alcohol/tobacco/drugs? **No**

**Gambling:**
- Simulated gambling? **No**

**Other:**
- Social features? **No** (temporary location sharing is not social networking)
- Users can interact? **Yes** (can see each other on map - minimal interaction)

### **5.4 Review Rating**
Expected rating: **Everyone**

Click **"Save"** and **"Apply rating"**

---

## 📱 **PHASE 6: UPLOAD AAB** (10 minutes)

### **6.1 Choose Release Track**

**Option A: Internal Testing** (Recommended first time)
- Dashboard → Release → Testing → Internal testing
- Benefit: Test before public release, get Pre-launch report

**Option B: Production** (If confident)
- Dashboard → Release → Production
- Goes live after review

### **6.2 Create Release**
1. Click **"Create new release"**
2. Click **"Upload"**
3. Select: `playstore/builds/app-release-v1.0.1.aab`
4. Wait for upload (~2 minutes)

### **6.3 Release Details**

**Release name:**
```
1.0.1 (2)
```

**Release notes:**
Copy from: `playstore/descriptions/release-notes.txt`

```
Initial release of TAXI - SA Taxi Visibility app

Features:
• Real-time visibility of taxis and passengers
• TOWN/LOCAL direction selection
• Simple phone verification
• Privacy-focused design
• Optimized for SA networks
```

### **6.4 Review Release**
Check for:
- [x] No errors
- [x] No warnings (or justified)
- [x] Correct version code

Click **"Save"** → **"Review release"**

---

## 🔍 **PHASE 7: PRE-LAUNCH REPORT** (Automatic - Wait 1 hour)

### **7.1 What Happens**
Google automatically tests your app on ~10 devices:
- Robo test crawls your app
- Tests basic functionality
- Checks for crashes
- Scans for security issues

### **7.2 Review Report**
Dashboard → Release → [Your track] → Pre-launch report

**Check for:**
- Crashes: Should be 0%
- ANRs: Should be 0%
- Security issues: Should be 0%

**If issues found:**
- Fix issues
- Build new AAB
- Upload again

---

## ✅ **PHASE 8: FINAL REVIEW & SUBMIT** (5 minutes)

### **8.1 Complete All Sections**
Dashboard → Check for incomplete items:
- [ ] Store listing ✅
- [ ] Data safety ✅
- [ ] Content rating ✅
- [ ] App access (if app requires login)
- [ ] Ads declaration
- [ ] Target audience
- [ ] News apps declaration (if applicable)
- [ ] COVID-19 contact tracing/status declaration (if applicable)

### **8.2 App Access**
**Does your app require login?**
- Answer: **No** (users can use app after phone verification, but it's not a traditional login)

If Google requires test credentials:
- Phone: 072 858 8857
- OTP: 123456 (test account)

### **8.3 Ads Declaration**
**Does your app contain ads?**
- Answer: **No**

### **8.4 Submit for Review**
1. Review all sections
2. Click **"Send for review"** or **"Start rollout to [track]"**
3. Confirm submission

---

## ⏱️ **PHASE 9: WAIT FOR REVIEW** (1-3 days)

### **9.1 Review Process**
Google will:
1. Run automated tests (minutes)
2. Manual review by Google team (hours to days)
3. Notify you via email

### **9.2 Possible Outcomes**

**Approved ✅**
- Email: "Your app is published"
- App goes live on Play Store
- Users can download

**Changes Requested ⚠️**
- Email: "Action required for your app"
- Review feedback
- Make requested changes
- Resubmit

**Rejected ❌**
- Email: "Your app was not approved"
- Review rejection reasons
- Fix issues
- Appeal or resubmit

---

## 🎉 **PHASE 10: GO LIVE!**

### **10.1 Verify App is Live**
Search Play Store:
```
TAXI - SA Taxi Visibility
```

Or visit:
```
https://play.google.com/store/apps/details?id=com.tecvo.taxi
```

### **10.2 Share Your App**
Share link with users, friends, family, beta testers!

### **10.3 Monitor Performance**
Dashboard → Statistics:
- Installs
- Uninstalls
- Ratings
- Reviews
- Crashes

---

## 📊 **TESTING REQUIREMENT DETAILS**

### **If New Personal Account (Post-Nov 13, 2023)**

#### **Set Up Closed Test:**
1. Dashboard → Release → Testing → Closed testing
2. Create new closed test
3. Create email list of testers
4. Share test link with testers
5. Wait 14 days with minimum 12 active testers
6. Apply for production access

#### **Recruittings Testers:**
- Friends, family, colleagues
- Local taxi drivers (your target users!)
- South African Android users
- Share test link via WhatsApp, email

#### **After 14 Days:**
1. Dashboard → Apply for production
2. Answer questions about testing
3. Submit application
4. Wait 1-2 days for approval
5. Then proceed to production release

---

## 🆘 **TROUBLESHOOTING**

### **AAB Upload Failed**
- Check AAB is signed with release keystore
- Verify version code is incremented
- Try re-building: `gradlew.bat clean bundleRelease`

### **Privacy Policy URL Not Accepted**
- Verify URL is accessible: https://andilentsele.github.io/tecvo-taxi-app/
- Ensure HTTPS (not HTTP)
- Check URL doesn't require login

### **Pre-launch Report Shows Crashes**
- Review crash logs in Pre-launch report
- Fix issues in code
- Rebuild and re-upload

### **Feature Graphic Wrong Size**
- Use: `playstore/graphics/feature-graphic.png`
- Verified: 1024 x 500 pixels
- Format: PNG

---

## 📞 **SUPPORT RESOURCES**

**Official Google Help:**
- Play Console Help: https://support.google.com/googleplay/android-developer
- Developer Policy: https://play.google.com/about/developer-content-policy/

**Your Documentation:**
- Submission Checklist: SUBMISSION_CHECKLIST.md
- Store Content: STORE_LISTING_CONTENT.md

---

**Good luck with your launch! 🚀**

**Last Updated:** January 2025