# Zinwa Dialer

A native Android dialer app built for the **Zinwa Q25**, optimized for square displays (720x720) with full physical keyboard and D-pad support.

> **Early Development** — This project is under active development. Expect breaking changes between releases.

## Features

- **3-tab navigation** — Favorites, Home (call log), and Keypad
- **Adaptive dialpad** — Responsive layout that fits any square screen size
- **Haptic feedback** — Light vibration on all button presses
- **Inline call details** — Single tap expands call log entries with quick actions (Call, Message, History)
- **Bottom sheet actions** — Long press for edit contact, add to contacts, favorites, block, copy number, view history, delete
- **Call history detail** — Full history per contact with date grouping
- **Favorites** — Pin/unpin contacts, suggestions from recent calls
- **Hardware key support** — Physical call/end buttons, D-pad navigation, QWERTY keyboard input
- **Default dialer management** — Role-based (ROLE_DIALER) with setup prompt on launch
- **AccessibilityService** — Intercepts hardware call/end buttons for reliable key routing
- **Voice search** — Speech-to-text contact search
- **Blocked numbers** — Manage blocked contacts

## Technical Details

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Min SDK:** 34 (Android 14)
- **Target SDK:** 34
- **Build system:** Gradle 8.6, AGP 8.2.2
- **Image loading:** Coil 2.6.0

## Permissions

| Permission | Purpose |
|---|---|
| `READ_CALL_LOG` / `WRITE_CALL_LOG` | Display and manage call history |
| `READ_CONTACTS` | Contact name resolution and search |
| `CALL_PHONE` | Place calls |
| `READ_PHONE_STATE` | Monitor call state |
| `MANAGE_OWN_CALLS` | In-call management |
| `BIND_INCALL_SERVICE` | Handle active calls |
| `BIND_SCREENING_SERVICE` | Call screening support |

## Build

### Debug

```bash
./gradlew assembleDebug
```

### Release (signed)

1. Create a `keystore.properties` file in the project root (see `keystore.properties.example`):

```properties
storeFile=../zinwa_dialer.jks
storePassword=your_store_password
keyAlias=zinwa_dialer
keyPassword=your_key_password
```

2. Generate a keystore if you don't have one:

```bash
keytool -genkeypair -alias zinwa_dialer -keyalg RSA -keysize 2048 \
  -validity 10000 -keystore zinwa_dialer.jks
```

3. Build:

```bash
./gradlew assembleRelease
```

The signed APK will be at `app/build/outputs/apk/release/app-release.apk`.

## Project Structure

```
zinwa_dialer/
├── app/src/main/java/com/zinwa/dialer/
│   ├── MainActivity.kt              # Entry point, key routing, role management
│   ├── DialerViewModel.kt           # UI state, search, favorites, call logic
│   ├── KeyHandler.kt                # Physical keyboard & D-pad event handling
│   ├── CallHistoryDetailActivity.kt # Per-contact call history screen
│   ├── InCallActivity.kt            # In-call UI
│   ├── InCallViewModel.kt           # In-call state management
│   ├── CallStateHolder.kt           # Active call state singleton
│   ├── FavoritesScrollController.kt # D-pad scroll bridge for favorites
│   ├── data/
│   │   ├── Contact.kt               # Data model
│   │   ├── ContactsRepo.kt          # System contacts provider
│   │   ├── RecentsRepo.kt           # Call log provider
│   │   ├── SearchEngine.kt          # Unified search across contacts & recents
│   │   ├── FavoritesRepo.kt         # Pinned favorites (SharedPreferences)
│   │   ├── BlockedNumbersRepo.kt    # Blocked numbers management
│   │   └── FuzzySearch.kt           # Fuzzy matching for search
│   ├── service/
│   │   ├── ButtonInterceptService.kt # AccessibilityService for hardware keys
│   │   ├── ToolbarButtonHandler.kt   # Bridge between service and activity
│   │   ├── MyInCallService.kt        # InCallService implementation
│   │   ├── ScreeningService.kt       # Call screening
│   │   └── CallActionReceiver.kt     # Notification call actions
│   └── ui/
│       ├── DialerScreen.kt           # Main UI (tabs, home, favorites, keypad)
│       ├── ResultsList.kt            # Call log list with expand & bottom sheet
│       └── theme/Theme.kt            # Material 3 theme
├── app/src/main/res/                 # Resources (drawables, layouts, values)
├── .gitignore
├── build.gradle.kts
├── keystore.properties.example
└── README.md
```

## Tested Devices

| Device | Display | Status |
|---|---|---|
| Zinwa Q25 | 720x720 | Primary target |

## License

All rights reserved.
