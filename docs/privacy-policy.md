# TAXI App Privacy Policy - Privacy-by-Design Real-Time Visibility Service

**Last Updated: January 2025**

## WHAT THE TAXI APP IS

The TAXI app (by TECVO) is a **real-time visibility service** for South African taxi users, not a data collection app. Like Google Maps showing your location while navigating, our app temporarily shows your location only while actively using the taxi visibility service.

**Core Service**: Provides "birds eye view" of available taxis and passengers at pickup points - just like having eyes in the sky to see what's coming.

---

## OUR PRIVACY-BY-DESIGN APPROACH

### **WE ARE NOT A DATA COLLECTION APP**

Unlike social media, e-commerce, or analytics apps that collect and store your data permanently, the TAXI app operates on a **service-based model**:

✅ **Real-time visibility service** (temporary operational data)
❌ **Not data collection** (permanent storage for business purposes)
❌ **Not user profiling** (no behavioral tracking)
❌ **Not data monetization** (no selling or advertising use)

---

## WHAT INFORMATION WE PROCESS (TEMPORARILY)

### **1. Real-Time Location Data (Temporary Service Operation)**
- **What**: Your GPS coordinates (latitude, longitude)
- **When**: Only while actively using the map screen
- **Why**: To show your position to other users going the same direction
- **Duration**: **Automatically deleted when you leave the map screen**
- **Like**: Similar to showing your location during a WhatsApp live location share or Uber ride

### **2. Service Selection Data (Temporary)**
- **What**: Your role (driver/passenger) and destination (town/local)
- **When**: Only while using the visibility service
- **Why**: To filter map view (passengers see taxis, drivers see passengers)
- **Duration**: **Automatically deleted when service ends**

### **3. Phone Authentication (Standard Login)**
- **What**: Phone number for login verification
- **Why**: Standard app authentication (like WhatsApp, Uber, banking apps)
- **Storage**: Firebase Auth service (Google's secure authentication system)
- **Not used for**: Marketing, profiling, or data collection

### **4. App Preferences (Standard App Functionality)**
- **What**: Notification radius, map display preferences
- **Why**: Basic app settings (like notification preferences in any app)
- **Storage**: Local device storage only
- **Not used for**: Tracking or profiling

### **5. Device and Network Compatibility**
- **Device Restrictions**: App is exclusively for smartphones; tablets are blocked from access
- **Network Optimization**: Designed for South African mobile conditions including 2G/EDGE networks
- **Purpose**: Ensures optimal performance for target users (taxi drivers and commuters)

---

## HOW OUR SERVICE WORKS (Technical)

1. **You open app at pickup point** → Select role and destination
2. **Real-time visibility starts** → Your location appears on Firebase at `drivers/town/{userId}`
3. **Other users see you** → Only users going same direction see your location
4. **You leave map screen** → **Location automatically deleted from Firebase**
5. **Service ends** → No permanent data remains

**Key Point**: Data exists only for immediate service delivery, not permanent storage.

---

## DATA RETENTION (Minimal by Design)

### **Real-Time Location Data**
- **Retention Period**: **0 seconds after leaving map**
- **Cleanup**: Automatic via `FirebaseCleanupUtil.removeUserData()`
- **Purpose**: Maintains visibility accuracy - if you're not there, you shouldn't appear on map

### **Phone Authentication**
- **Retention Period**: Until account deletion
- **Purpose**: Standard app login (like any authenticated app)
- **Control**: Delete via Firebase Auth account deletion

### **App Preferences**
- **Retention Period**: Until app uninstall or preference reset
- **Storage**: Local device only
- **Purpose**: Basic app functionality

### **No Permanent History**
- ❌ **No location history stored**
- ❌ **No trip records kept**
- ❌ **No behavioral data collected**
- ❌ **No user profiling data**

---

## THIRD-PARTY SERVICES (Required for App Function)

### **Firebase (Google)**
- **Purpose**: Real-time database for temporary location visibility
- **Data**: Temporary location coordinates, phone authentication
- **Privacy**: Google Firebase Privacy Policy applies
- **Alternative**: No equivalent service exists for real-time taxi visibility

### **Google Maps Platform**
- **Purpose**: Map display and location services
- **Data**: Map tiles, geocoding requests
- **Privacy**: Google Maps Privacy Policy applies
- **Alternative**: Essential for mapping functionality

**Important**: These services are **operationally necessary** for real-time visibility, not optional data collection.

---

## YOUR CONTROL AND RIGHTS

### **Immediate Control**
- **Start/Stop Service**: Use or don't use the app
- **Leave Map**: Instantly removes your location from system
- **Location Permissions**: Control via device settings

### **Account Control**
- **Delete Account**: Removes phone auth record
- **Reset Preferences**: Clear app settings anytime
- **Uninstall App**: Removes all local data

### **No Data Deletion Request Needed**
Since we don't store permanent personal data (only temporary service data), traditional "data deletion requests" don't apply to our service model.

---

## LEGAL COMPLIANCE

### **South African POPIA Compliance**
- **Lawful Processing**: Real-time visibility service with user consent
- **Purpose Limitation**: Data used only for taxi visibility, not other purposes
- **Data Minimization**: Only location data needed for immediate service
- **Retention Minimization**: Automatic deletion when service not in use
- **Security**: Industry-standard Firebase and HTTPS encryption

### **No Cross-Border Data Issues**
- **Service Area**: South Africa only
- **User Base**: SA taxi users at traditional pickup points
- **Data Processing**: Temporary operational processing only

---

## CHILDREN'S PRIVACY

Our app is designed for **adults using SA taxi services**. Not intended for children under 18.

---

## SECURITY MEASURES

- **Encrypted transmission** (HTTPS/TLS)
- **Firebase security rules** prevent unauthorized access
- **Automatic data cleanup** maintains system integrity
- **No permanent data storage** reduces security risks

---

## CHANGES TO THIS POLICY

We may update this policy to:
- Clarify our privacy-by-design approach
- Reflect technical improvements to temporary data handling
- Ensure continued legal compliance

Changes posted in app with updated date.

---

## UNDERSTANDING THE DIFFERENCE

**TAXI App (Privacy-by-Design Service)**:
- ✅ Temporary operational data for real-time visibility
- ✅ Automatic cleanup when service not in use
- ✅ No permanent user profiles or history
- ✅ No data monetization or advertising

**Traditional Apps (Data Collection Model)**:
- ❌ Permanent data storage for business purposes
- ❌ User profiling and behavioral tracking
- ❌ Data sharing for advertising/marketing
- ❌ Complex deletion processes required

---

## CONTACT US

**TECVO (Pty) Ltd**
**Email**: privacy@tecvo.com
**Purpose**: Real-time taxi visibility service for South African taxi industry
**Mission**: Technology that extends natural abilities without changing behavior

---

## CONSENT

By using the TAXI app, you consent to this **real-time visibility service** and understand that:
- Your location is shared temporarily while actively using the service
- Data is automatically deleted when you stop using the service
- This is operational service data, not permanent data collection
- You maintain full control through standard app usage patterns

---

*This policy reflects our commitment to privacy-by-design principles in providing real-time taxi visibility services to the South African taxi industry.*
