# RunTracker Android App

RunTracker is an Android app for logging running sessions and tracking progress over time using simple, date-based entries and visual trends.

Design principles:

- Local-first
- No authentication
- No cloud sync
- No ads
- No social features
- No gamification

## Core Functionality

- Create and update a personal profile with name, height, and gender.
- Add, edit, and review one run entry per date.
- Track run type (Outdoor or Treadmill), weight, distance, duration, pace/speed, and notes.
- View monthly calendar-based run history from the home screen.
- Open detailed run view for any logged date.
- Choose unit preferences from Settings:
   - Metric: cm, kg, km
   - Imperial: ft/in, lb, miles
   - Custom: per-field unit selection
- Keep all stored values in metric internally (heightCm, weightKg, distanceKm); convert only at UI level.
- See progression charts for:
  - Distance
  - Speed
  - Weight
  - BMI
- Statistics chart improvements:
  - X-axis dates shown in dd-MM format
  - Sparse/even date labels to avoid clutter (left, right, and a few intermediate labels)
  - Point tooltip showing date and value
  - High/Low values shown at top-right of each chart card
- Backup and restore run/profile data using JSON import/export via Android Storage Access Framework.
- Backup reminder dialog (gentle): shown only when 100 runs or 6 months pass since last reminder handling.
- In-app rating prompt using Google Play In-App Review API with local eligibility checks.
- About section in Settings with app version and Powered by Upstead email action.
- In-app light/dark mode toggle in Settings, stored locally on device (default mode is light).

## Prompt Behavior

- Backup reminder actions:
   - Later
   - Export Backup
- Rating prompt appears only when all are true:
   - App installed for at least 7 days
   - User has at least 5 runs
   - User was not already prompted successfully
   - User did not choose Don't Ask Again
- Rating prompt actions:
   - Rate App (In-App Review)
   - Maybe Later (re-eligible after 30 days or 25 additional runs)
   - Don't Ask Again

## Data Compatibility

- JSON backup/import remains metric-based internally.
- Run type is included in exports.
- Older JSON backups without run type are still importable.

## Tech Stack

- Kotlin
- Jetpack Compose (Material 3)
- Room (local persistence)
- Google Play In-App Review API
- Android Architecture Components (ViewModel, StateFlow)
- Gradle Kotlin DSL

## Project Structure

- app/ : Android application module
- docs/ : requirement and vision documents
- gradle/ : Gradle wrapper and version catalog files

## Prerequisites (Linux)

Install the following:

1. Java Development Kit (JDK) 17 or newer
2. Android Studio (latest stable) with Android SDK installed
3. Android SDK Build-Tools and platform matching project compileSdk
4. adb (usually included with Android SDK platform-tools)
5. Git (for source control)

Environment setup recommendations:

- Ensure JAVA_HOME points to your JDK installation.
- Ensure Android SDK is installed and recognized by Android Studio.
- Keep local.properties out of version control (already ignored).

## Build and Run (Linux)

From the project root:

1. Make Gradle wrapper executable (if needed):

   chmod +x gradlew

2. Compile debug Kotlin sources:

   ./gradlew :app:compileDebugKotlin

3. Build debug APK:

   ./gradlew :app:assembleDebug

4. Optional: Build release APK:

   ./gradlew :app:assembleRelease

Output locations:

- Debug APK: app/build/outputs/apk/debug/
- Release APK: app/build/outputs/apk/release/

## Install on Device (optional)

With USB debugging enabled and device connected:

adb install -r app/build/outputs/apk/debug/app-debug.apk

## Notes

- This repository intentionally excludes build outputs, IDE metadata, local machine settings, and signing keys.
- Keep signing artifacts and credentials private.