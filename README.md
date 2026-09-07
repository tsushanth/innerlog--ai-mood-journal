# InnerLog: AI Mood Journal

InnerLog is a private, on-device journaling app for Android. Write daily entries, track how
you feel, build habits, and work toward personal goals — with lightweight on-device mood
analysis that suggests a mood and surfaces a short insight from what you wrote, no data ever
leaving the device unless you choose to back it up.

## Features

- **Journal** — Create, edit, and revisit dated journal entries with a title and free-form
  text.
- **Mood tracking** — Tag each entry with a mood (Awful → Great) and view mood trends over
  time on the Insights/Stats screen.
- **On-device mood suggestions** — `MoodAnalyzer` scans entry text for sentiment cues and
  suggests a mood and short insight as you write — fully offline, no network calls.
- **Habits** — Create habits and check them off day to day.
- **Goals** — Track personal goals alongside your journal and habits.
- **App lock** — Optional PIN and biometric (fingerprint/face) lock via `LockManager` and
  `BiometricAuthHelper`, backed by `androidx.security` encrypted storage.
- **Premium / Paywall** — Optional subscription and one-time purchase tiers (weekly, monthly,
  yearly, lifetime, and a small tip) via Google Play Billing, unlocking premium insights.
- **Material 3 theming** — Dynamic color (Android 12+) and full light/dark mode support.

## Requirements

- **Android Studio** Ladybug (2024.2) or newer
- **JDK 17**
- **Android SDK**
  - `compileSdk` 34
  - `minSdk` 24 (Android 7.0 Nougat)
  - `targetSdk` 34
- Gradle 8.9 (via the included Gradle Wrapper — no local Gradle install required)
- A device or emulator running Android 7.0+ (dynamic color requires Android 12+)

## Build instructions

Clone or open the project directory, then from the project root:

```bash
# Build a debug APK
./gradlew assembleDebug

# Install and run on a connected device/emulator
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented (on-device) tests
./gradlew connectedAndroidTest
```

Or open the project root in Android Studio and let it sync Gradle, then run the `app`
configuration on a device or emulator.

Google Play Billing requires a device signed into a Google account with the app's billing
products configured in the Play Console to test real purchases; without that, the paywall
falls back to the prices declared in `BillingProducts.kt`.

## Project structure

```
app/src/main/java/com/factory/innerlogaimoodjournal/
├── MainActivity.kt              # Single-activity host, edge-to-edge + theme setup
├── InnerLogApplication.kt       # App-wide DI container (repositories, managers)
├── billing/                     # Google Play Billing integration and premium entitlement state
├── data/
│   ├── local/                   # Room database, DAOs, entities, type converters
│   ├── model/                   # Domain models (e.g. MoodLevel)
│   ├── mood/                    # On-device mood/sentiment analysis
│   └── repository/              # Repositories bridging DAOs to the UI layer
├── security/                    # PIN lock manager and biometric authentication
└── ui/
    ├── components/               # Reusable composables (cards, selectors, badges)
    ├── navigation/                # Navigation graph and route definitions
    ├── screens/                   # One package per feature screen (journal, habits,
    │                               goals, home, lock, paywall, settings, stats)
    └── theme/                     # Material 3 color scheme, typography, dynamic theming
```

Data flows one way: Room `entity` → `dao` → `repository` → screen `ViewModel` → Composable
`Screen`, with navigation routes declared in `ui/navigation/Screen.kt` and wired together in
`ui/navigation/NavGraph.kt`.
