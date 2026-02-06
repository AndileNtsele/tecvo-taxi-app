PLAY STORE RELEASE BUILDS
=========================

This folder contains production-ready AAB files for Play Store submission.

HOW TO BUILD RELEASE AAB:
-------------------------
1. Open terminal in project root
2. Run: gradlew.bat clean
3. Run: gradlew.bat bundleRelease
4. Find AAB at: app\build\outputs\bundle\release\app-release.aab
5. Copy to this folder with version number: app-release-v1.0.1.aab

NAMING CONVENTION:
------------------
app-release-v[version].aab

Example:
- app-release-v1.0.1.aab (First release)
- app-release-v1.0.2.aab (Bug fix update)
- app-release-v1.1.0.aab (Feature update)

IMPORTANT:
----------
- Keep ALL release builds for version history
- Never delete old AAB files
- Include release notes for each version
- Verify AAB is signed before uploading