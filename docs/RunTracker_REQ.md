# RunTracker - Requirements Specification

## Overview

RunTracker is a simple, local-first Android running journal for a single user.

Core principles:

- No authentication
- No cloud sync
- No ads
- No social features
- No gamification
- Simple and uncluttered UI

All data is stored locally on device.

---

## Goals

- Record runs by date
- Track consistency and personal history
- View trends through clear charts
- Export and import local backups

---

## Non-Goals

- Multi-user support
- Login/authentication
- Cloud sync
- Wearables/GPS/routes
- Challenges, streak pressure, badges
- AI coaching
- Social networking

---

## Platform

- Android only
- Kotlin + Jetpack Compose (Material 3)
- Room (SQLite)

---

## First Launch

On first launch, user sets profile:

- Name (required)
- Height (required)
- Gender (required)

Profile is stored locally and used for BMI.

---

## Main Experience

- Monthly calendar home view
- One run entry per date
- Quick add/edit flow
- Run detail view
- Statistics charts for distance, speed, weight, BMI

---

## Run Entry

Fields:

- Date (required)
- Weight (required)
- Distance (required)
- Duration (required)
- Run Type (optional, defaults to Outdoor)
- Notes (optional)

Run Type values:

- Outdoor
- Treadmill

---

## Units

Settings includes a Units section with:

- Metric: cm, kg, km
- Imperial: ft/in, lb, miles
- Custom: independent selection per metric

Storage rule:

- Internal values remain metric only: heightCm, weightKg, distanceKm
- Conversion happens at UI layer only

---

## Settings

Settings includes:

- Profile: name, height, gender
- Units: Metric/Imperial/Custom
- Appearance: dark mode toggle (default app mode is light)
- Data: Export/Import JSON backup
- About: app name, version, Powered by Upstead email action

Powered by Upstead behavior:

- Opens Android email chooser
- To: contact@upstead.ai
- Subject: RunTracker Feedback
- Gracefully handles no email app case

---

## Backup Reminder

Show gentle reminder only when either condition is met:

- 100 runs since last reminder handling
- 6 months since last reminder handling

Reminder text:

"Consider exporting a backup of your RunTracker data."

Actions:

- Later
- Export Backup

State is stored locally to avoid repeated nagging.

---

## Rating Prompt (Google In-App Review)

Use Google Play In-App Review API only (no manual Play Store redirect).

Show only when all are true:

- App installed at least 7 days
- User recorded at least 5 runs
- User has not already been prompted successfully
- User has not selected Don't Ask Again

Dialog:

- Title: Enjoying RunTracker?
- Message about usefulness and rating request
- Actions: Rate App, Maybe Later, Don't Ask Again

Behavior:

- Rate App: launch In-App Review flow; if launch succeeds, mark prompted
- Maybe Later: defer eligibility until 30 additional days OR 25 additional runs
- Don't Ask Again: permanently disable future prompts

No analytics, incentives, or feature gating.

---

## Data and Compatibility

Room data model:

- Profile: id, name, heightCm, gender
- RunEntry: id, date, weightKg, distanceKm, durationSeconds, notes, runType

Constraint:

- One run per date

Backup JSON:

- Uses metric values internally
- Includes runType
- Backward compatible import for older exports where runType is missing
