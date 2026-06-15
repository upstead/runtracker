# RunTracker - Requirements Specification (V1)

## Overview

RunTracker is a simple offline-first Android application for tracking running progress.

The application is designed for a single user and focuses on simplicity rather than advanced fitness analytics.

No user accounts, cloud sync, authentication, advertisements, social features, or online services will be included.

All data will be stored locally on the device.

---

# Goals

The application should allow the user to:

- Track daily running activity.
- Track body weight over time.
- View running history using a calendar interface.
- Visualize progress using charts.
- Export and import data for backup and migration.

---

# Non-Goals

The following features are explicitly excluded from V1:

- Multi-user support
- Login or authentication
- Cloud synchronization
- Wearable integrations
- GPS tracking
- Route mapping
- Heart rate monitoring
- Calories calculation
- Achievement systems
- Streak systems
- Notifications
- AI coaching
- Social features

---

# Platform

- Android only
- Kotlin
- Jetpack Compose
- Room Database (SQLite)
- Material 3 UI

---

# First Launch Experience

On first launch, the application should display a profile setup screen.

## Profile Fields

### Required

- Name
- Height (cm)
- Gender

### Storage

Profile data should be stored locally.

Profile information is used for BMI calculations.

---

# Main Screen

The main screen should display a monthly calendar view.

## Top App Bar

Title:

RunTracker

Actions:

- Statistics Icon
- Settings Icon

---

# Calendar View

The calendar is the primary navigation component.

## Calendar Behavior

### Date without Run Entry

Selecting a date should show:

- No run recorded
- Add Run action

### Date with Run Entry

Dates containing a run entry should display an indicator (dot or marker).

Selecting the date should display run details.

---

# Floating Action Button

A single global Floating Action Button (FAB) should be displayed.

Purpose:

Add a new run entry.

Default selected date:

Current date.

---

# Run Entry

Each date can contain a maximum of one run record.

## Fields

### Required

- Date
- Weight (kg)
- Distance (km)
- Duration

### Optional

- Notes

---

# View Run Screen

Selecting a date with a run should display:

- Date
- Weight
- Distance
- Duration
- Average Speed
- BMI
- Notes

---

# Edit Run

Existing run entries must be editable.

The same screen should be reused for both Add and Edit operations.

---

# Statistics Screen

Displays four charts:

1. Distance Progression
2. Speed Progression
3. Weight Progression
4. BMI Progression

---

# Settings Screen

- Name
- Height
- Gender
- Export Data
- Import Data

---

# Database Design

## Profile Table

- id
- name
- heightCm
- gender

## RunEntry Table

- id
- date
- weightKg
- distanceKm
- durationSeconds
- notes

Constraint:

- One run per date
