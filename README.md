# 🚖 TAXI - SA Taxi Visibility App

**Real-time visibility platform for South African taxi drivers and passengers**

[![Target SDK](https://img.shields.io/badge/Target%20SDK-35-green)](https://developer.android.com)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26-blue)](https://developer.android.com)
[![Version](https://img.shields.io/badge/Version-1.0.1-orange)](https://play.google.com/store)

---

## 📱 What is TAXI?

TAXI is a privacy-first, real-time visibility app designed specifically for South Africa's taxi industry. It allows drivers and passengers to see each other in real-time, helping:

- **Drivers:** Find passengers during off-peak hours (save R200-500/day in fuel)
- **Passengers:** See when taxis are coming to their area
- **Everyone:** Make the SA taxi system more efficient without changing it

### Key Features
- ✅ Real-time map showing taxis & passengers
- ✅ TOWN/LOCAL direction selection (authentic SA terminology)
- ✅ Privacy-by-design (temporary location sharing only)
- ✅ Phone-only app (optimized for mobile use)
- ✅ Works on 2G/EDGE networks (rural-friendly)
- ✅ Battery efficient
- ✅ Completely free

---

## 🏗️ Project Structure

```
TAXI - 03/
├── app/                    # Android app source code
├── playstore/              # Play Store submission assets
├── documentation/          # Project documentation
│   ├── development/       # Development guides
│   ├── architecture/      # Architecture & design docs
│   ├── security/          # Security implementation
│   ├── testing/           # Testing guides & reports
│   ├── playstore-prep/    # Play Store preparation docs
│   └── archive/           # Old reports & historical docs
├── scripts/               # Build & utility scripts
├── assets/                # Project assets (fonts, graphics)
├── docs/                  # GitHub Pages (privacy policy)
├── gradle/                # Gradle wrapper
└── archive/               # Old backups & temp files
```

---

## 🚀 Quick Start

### **Prerequisites**
- Android Studio Hedgehog or newer
- JDK 17+
- Android SDK 35
- Google Maps API key (see setup guide)

### **Setup**
1. Clone repository
2. Copy `secrets.properties.template` to `secrets.properties`
3. Add your Google Maps API key to `secrets.properties`
4. Sync Gradle
5. Run on emulator or device

### **Build Commands**
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew testDebugUnitTest

# Build Play Store bundle
./gradlew bundleRelease
```

---

## 📚 Documentation

### **Getting Started**
- [CLAUDE.md](./CLAUDE.md) - Project instructions for Claude Code
- [Development Guide](./documentation/development/) - Setup & development workflow
- [Architecture Overview](./documentation/architecture/) - Technical architecture

### **Security**
- [Security Guide](./documentation/security/) - API key security & best practices
- Network security config (HTTPS-only)
- ProGuard rules for code obfuscation

### **Testing**
- [Testing Guide](./documentation/testing/) - Running tests
- 193 unit tests (100% pass rate)
- 242 UI tests infrastructure
- Firebase Test Lab integration

### **Play Store**
- [Play Store Submission](./playstore/) - Complete submission package
- All graphics, descriptions, and documentation ready
- Privacy policy live at: https://andilentsele.github.io/tecvo-taxi-app/

---

## 🎯 Tech Stack

### **Core**
- **Language:** Kotlin 100%
- **UI:** Jetpack Compose
- **Architecture:** MVVM with Repository pattern
- **Dependency Injection:** Hilt/Dagger

### **Firebase**
- Authentication (Phone)
- Realtime Database
- Crashlytics
- Analytics

### **Google Services**
- Maps SDK
- Geocoding API
- Location Services

### **Libraries**
- Coroutines & Flow
- Navigation Compose
- Room (if local storage needed)
- Timber (logging)

---

## 📦 App Details

- **Package Name:** `com.tecvo.taxi`
- **Version:** 1.0.1 (versionCode: 2)
- **Target SDK:** 35 (Android 15)
- **Min SDK:** 26 (Android 8.0)
- **Build Tools:** Gradle 8.7

### **Supported Devices**
- ✅ Phones (320dp - 600dp)
- ✅ Foldable phones (constrained UI)
- ❌ Tablets (explicitly blocked)

---

## 🔒 Privacy & Security

### **Privacy-by-Design**
- Temporary location data only (deleted on map exit)
- No permanent user profiles or tracking
- Minimal data collection (phone + location)
- POPIA compliant (South Africa)

### **Security Features**
- Google's Secrets Gradle Plugin for API keys
- HTTPS-only network traffic
- Certificate pinning for Google services
- ProGuard code obfuscation
- No hardcoded credentials

### **Data Handling**
- Location: Temporary (auto-cleanup)
- Phone number: Authentication only
- No data selling or monetization
- No third-party data sharing

---

## 🧪 Testing

### **Test Coverage**
- **Unit Tests:** 193 tests (100% pass)
- **UI Tests:** 242 tests (infrastructure ready)
- **Test Credentials:** Phone: 072 858 8857, OTP: 123456

### **Run Tests**
```bash
# Unit tests
./gradlew testDebugUnitTest

# UI tests
./gradlew connectedAndroidTest

# Security validation
./scripts/validate_security.bat
```

---

## 📱 Play Store

### **Submission Status**
✅ **100% Ready for Play Store**

- All assets prepared
- Privacy policy live
- Target SDK 35 compliant
- Security hardened
- Testing complete

**Submission Package:** `playstore/`

---

## 🌍 Target Market

### **Primary**
- South Africa 🇿🇦
- Taxi drivers & commuters
- Urban & rural areas
- Age: 18-65
- Android 8.0+

### **Languages**
- English (primary)
- Afrikaans (configured)

---

## 👥 Team

- **Company:** TECVO (Pty) Ltd
- **Contact:** privacy@tecvo.com
- **Privacy Policy:** https://andilentsele.github.io/tecvo-taxi-app/

---

## 📄 License

**Copyright © 2025 TECVO (Pty) Ltd. All rights reserved.**

---

## 🔗 Links

- **Privacy Policy:** https://andilentsele.github.io/tecvo-taxi-app/
- **Play Store:** (Coming soon)
- **Support:** privacy@tecvo.com

---

## 🛠️ Development

### **Active Development**
- Phone-only enforcement
- Foldable phone support
- Privacy-by-design architecture
- SA network optimization (2G/EDGE support)

### **Recent Updates**
- Target SDK 35 (Android 15)
- Security hardening (Secrets Plugin)
- ANR fixes (async initialization)
- Comprehensive foldable support
- OTP back button UX improvement

---

## 📊 Project Stats

- **82 Kotlin files**
- **33 test files**
- **193 unit tests** (100% pass)
- **242 UI tests** (infrastructure)
- **Target SDK 35** (exceeds 2025 requirements)
- **100% Play Store ready**

---

**Built with ❤️ for South Africa's taxi industry**

🚖 Enhancing the existing taxi system, not replacing it.