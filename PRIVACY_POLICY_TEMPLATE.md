# Privacy Policy Template for [YOUR TAXI APP NAME]

**Last Updated: [DATE]**

## Introduction

[Your Company Name] ("we," "our," or "us") operates the [Your App Name] mobile application (the "Service"). This Privacy Policy informs you of our policies regarding the collection, use, and disclosure of personal data when you use our Service.

## Information We Collect

### Personal Information
- **Account Information**: Name, email address, phone number (via Firebase Authentication)
- **Profile Information**: User role (Driver/Passenger), profile picture (if provided)

### Location Information
- **Precise Location**: We collect real-time GPS location data to:
  - Show nearby drivers to passengers
  - Navigate drivers to pickup/dropoff points
  - Calculate trip distances and fares
  - Provide safety features
- **Background Location** (Drivers only): To show availability when app is in background

### Usage Data
- **App Analytics**: Screen views, feature usage, crash reports (via Firebase Analytics)
- **Performance Data**: App load times, API response times (via Firebase Performance)

### Device Information
- Device type and model
- Operating system version
- Unique device identifiers
- Network information

## How We Use Your Information

We use the collected data for:
1. **Service Provision**: Connecting passengers with drivers
2. **Navigation**: Providing turn-by-turn directions
3. **Safety**: Tracking trips for user safety
4. **Communication**: Sending notifications about trip status
5. **Improvement**: Analyzing usage patterns to improve the app
6. **Support**: Responding to user inquiries
7. **Legal Compliance**: Meeting legal obligations

## Data Sharing

We share your information with:

### Service Providers
- **Google Maps**: For mapping and navigation services
- **Firebase Services**: For authentication, database, and analytics
- **Google Cloud Platform**: For geocoding services

### Other Users
- Drivers see passenger pickup location and name
- Passengers see driver name, vehicle info, and real-time location during trips

### Legal Requirements
We may disclose information if required by law or to protect rights and safety.

## Data Security

We implement appropriate security measures including:
- Encrypted data transmission (HTTPS/TLS)
- Firebase Security Rules for database access
- Regular security updates

## Data Retention

- **Account Data**: Retained until account deletion
- **Trip History**: Retained for [X] years for legal/dispute resolution
- **Location Data**: Real-time data deleted after trip completion
- **Analytics Data**: Aggregated and anonymized after 14 months

## Your Rights

You have the right to:
- **Access**: Request a copy of your personal data
- **Correction**: Update incorrect information
- **Deletion**: Request deletion of your account and data
- **Portability**: Receive your data in a portable format
- **Opt-out**: Disable certain data collection (may limit app functionality)

## Children's Privacy

Our Service is not intended for children under 18. We do not knowingly collect data from children.

## Location Services

### How to Control Location Sharing:
1. **In-App**: Toggle location sharing in Settings
2. **Device Settings**: Manage app permissions in your device settings
3. **Background Location**: Can be disabled (drivers will appear offline)

## Third-Party Services

Our app uses third-party services that may collect information:
- Google Play Services
- Firebase Analytics
- Firebase Crashlytics
- Google Maps Platform

Review their privacy policies:
- [Google Privacy Policy](https://policies.google.com/privacy)
- [Firebase Privacy Policy](https://firebase.google.com/support/privacy)

## Data Protection (GDPR/POPIA Compliance)

### For South African Users (POPIA)
We comply with the Protection of Personal Information Act (POPIA):
- **Lawful Processing**: Data processed only for legitimate purposes
- **Consent**: Obtained before collecting personal information
- **Data Minimization**: Collect only necessary information
- **Security Safeguards**: Implement appropriate security measures

### For EU Users (GDPR)
If applicable, we comply with GDPR requirements including:
- Legal basis for processing
- Data protection rights
- Data breach notifications

## Changes to This Policy

We may update this Privacy Policy. Changes will be posted in the app with the "Last Updated" date.

## Contact Us

For questions about this Privacy Policy or data practices:

**Email**: [support@yourcompany.com]  
**Address**: [Your Company Address]  
**Phone**: [Your Phone Number]

## Consent

By using our app, you consent to this Privacy Policy and agree to its terms.

---

## IMPORTANT NOTES FOR DEVELOPER:

1. **Customize all [bracketed] placeholders**
2. **Review with a legal professional** familiar with South African POPIA laws
3. **Host this on a website** and provide the URL in Play Console
4. **Update the PrivacyPolicyScreen.kt** to link to your hosted version
5. **Consider adding sections for**:
   - Payment processing (if applicable)
   - Marketing communications
   - Cookies/tracking technologies
   - International data transfers
6. **Ensure compliance with**:
   - South African POPIA
   - Google Play Developer Policy
   - Any other applicable laws in your operating regions