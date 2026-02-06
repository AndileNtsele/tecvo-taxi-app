# 📋 PLAY STORE SUBMISSION CHECKLIST

## ✅ PRE-SUBMISSION CHECKLIST

### **Graphics & Media**
- [ ] Feature graphic uploaded (playstore/graphics/feature-graphic.png)
- [ ] Hi-res app icon uploaded (playstore/graphics/icon-512.png)
- [ ] All 7 screenshots uploaded from playstore/graphics/screenshots/
- [ ] Screenshots are in correct order (01-07)

### **Store Listing Content**
- [ ] App title entered (playstore/descriptions/title.txt)
- [ ] Short description entered (80 chars max)
- [ ] Full description entered (4000 chars max)
- [ ] Release notes entered for first version

### **Legal & Privacy**
- [ ] Privacy policy URL added: https://andilentsele.github.io/tecvo-taxi-app/
- [ ] Privacy policy URL verified as accessible
- [ ] Data safety section completed in Play Console
- [ ] Content rating questionnaire completed

### **App Build**
- [ ] Release AAB built (gradlew.bat bundleRelease)
- [ ] AAB signed with release keystore
- [ ] AAB uploaded to Play Console
- [ ] Version code matches (currently: 2)
- [ ] Version name matches (currently: 1.0.1)

### **App Configuration**
- [ ] Target SDK: 35 ✅
- [ ] Min SDK: 26 ✅
- [ ] Package name: com.tecvo.taxi ✅
- [ ] App permissions reviewed and justified ✅

### **Testing** (Account Type Dependent)
- [ ] Determine account type (Business/Personal/Date created)
- [ ] If new personal account: Set up 12 testers for 14 days
- [ ] If business/old personal: Self-test on 2-3 devices minimum
- [ ] Pre-launch report reviewed (after upload)

### **Categories & Distribution**
- [ ] Primary category: Maps & Navigation
- [ ] Target countries: South Africa (primary)
- [ ] Pricing: Free
- [ ] Age rating: Everyone

---

## 🚀 SUBMISSION STEPS

### **Step 1: Build Release AAB** (15 min)
```bash
gradlew.bat clean
gradlew.bat bundleRelease
```
Output: `app\build\outputs\bundle\release\app-release.aab`

Copy to: `playstore/builds/app-release-v1.0.1.aab`

### **Step 2: Upload to Play Console** (30 min)
1. Go to: https://play.google.com/console
2. Create new app or select existing
3. Upload AAB to Internal Testing track (recommended) or Production
4. Wait for processing (~5 minutes)

### **Step 3: Complete Store Listing** (30 min)
Use files from `playstore/descriptions/` and `playstore/graphics/`

**Required fields:**
- App name
- Short description
- Full description
- App icon
- Feature graphic
- Screenshots (minimum 2, you have 7)
- Category
- Content rating
- Privacy policy URL

### **Step 4: Fill Data Safety Section** (15 min)
**Your app collects:**
- Location data (approximate/precise) - TEMPORARY ONLY
- Phone number - For authentication

**Data handling:**
- Data is encrypted in transit ✅
- Users can request deletion ✅ (automatic on exit)
- Data not shared with third parties ✅
- Data not used for advertising ✅

### **Step 5: Content Rating** (10 min)
Answer questionnaire honestly:
- No violence
- No sexual content
- No gambling
- No controlled substances
- **Result: Everyone rating**

### **Step 6: Review & Submit** (5 min)
- Review all sections for completeness
- Check for warnings or errors
- Submit for review
- Wait 1-3 days for Google review

---

## ⏱️ TIMELINE ESTIMATES

### **If Business Account or Old Personal Account:**
- Today: Build & upload (2 hours)
- Days 1-3: Google review
- Day 4-7: **GO LIVE!** 🎉

### **If New Personal Account (Post-Nov 13, 2023):**
- Today: Build & upload to Internal Testing (2 hours)
- Days 1-14: Recruit 12 testers, run closed test
- Day 15: Apply for production access
- Days 16-18: Google review
- Day 19-20: **GO LIVE!** 🎉

---

## 📞 SUPPORT

**Issues during submission?**
- Check: UPLOAD_GUIDE.md (step-by-step)
- Review: STORE_LISTING_CONTENT.md (all content ready)
- Google Help: https://support.google.com/googleplay/android-developer

**Common issues:**
- "Privacy policy URL required" → Use playstore/legal/privacy-policy-url.txt
- "Feature graphic wrong size" → Use playstore/graphics/feature-graphic.png (verified 1024x500)
- "AAB upload failed" → Verify signing with release keystore

---

**Last Updated:** January 2025
**App Version:** 1.0.1 (versionCode: 2)
**Target SDK:** 35