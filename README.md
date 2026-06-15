# RunTracker Android App

RunTracker is an Android app for logging running sessions and tracking progress over time using simple, date-based entries and visual trends.

## Core Functionality

- Create and update a personal profile with name, height, and gender.
- Add, edit, and review run entries by date.
- Track weight, distance, duration, pace/speed, and notes.
- View monthly calendar-based run history from the home screen.
- Open detailed run view for any logged date.
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
- In-app light/dark mode toggle in Settings, stored locally on device.

## Tech Stack

- Kotlin
- Jetpack Compose (Material 3)
- Room (local persistence)
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