# CodePrep

CodePrep is a single-module Android learning app that helps users prepare for coding interviews through structured lessons, quizzes, and gamified progression.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Technical Details](#technical-details)
- [Project Structure](#project-structure)
- [Setup and Installation](#setup-and-installation)
- [Build and Development](#build-and-development)
- [Key Conventions](#key-conventions)
- [Database Schema](#database-schema)
- [Header Shortcuts Check](#header-shortcuts-check)
- [Contributing](#contributing)

## Features

- Gamified progress system (XP, levels, hearts, streak)
- Lessons and quizzes with unlock progression
- Firebase Authentication and Firestore-backed content
- Offline-first local cache with Room
- AI explanation support through OpenRouter API
- Friend system and profile views
- Home screen fun-fact widget

## Tech Stack

- **UI:** Jetpack Compose, Material 3, Navigation Compose
- **State:** ViewModel, Kotlin Coroutines, Flow
- **Dependency Injection:** Hilt
- **Local storage:** Room
- **Backend services:** Firebase Auth, Firestore
- **Networking:** Retrofit, OkHttp, Gson
- **Background work:** WorkManager

## Architecture

CodePrep uses a layered architecture inside one Android module (`:app`):

- **UI layer:** Compose screens and Hilt ViewModels (`ui/**`)
- **Data layer:** repositories and data sources (`data/**`)
- **Infrastructure layer:** DI wiring, workers, notifications, widget support (`di/**`, `work/**`, `notifications/**`, `widget/**`)

App startup is driven by:

- `CodePrepApp` (`@HiltAndroidApp`) for DI + app-wide setup
- `MainActivity` + `RootNavGraph()` for navigation bootstrap and session UI state

## Technical Details

- Navigation is split into nested auth and main graphs (`authNavGraph`, `mainNavGraph`) with route definitions in `ui/navigation/Screen.kt`.
- Session bootstrap (`SessionBootstrapViewModel`) refreshes user hearts and top-bar stats when a session starts.
- `UserRepository` centralizes progression rules, including:
  - max hearts = 5
  - refill interval = 30 minutes
  - streak updates and reset checks
- `CourseRepository` follows an offline-first read pattern (Room `Flow` first, background Firestore refresh).
- `LessonProgressRepository` and `LessonProgressRules` track completion, perfect runs, and XP reward logic.
- AI integration is configured in `di/NetworkModule.kt`, with the API key read from `BuildConfig.OPENROUTER_API_KEY`.

## Project Structure

```text
app/src/main/java/com/codeprep/app/
├── data/          # repositories, room, firestore/network adapters
├── di/            # Hilt modules (database, firebase, network, repositories)
├── domain/        # domain models/contracts
├── ui/            # compose screens, viewmodels, navigation
├── notifications/ # notification setup
├── widget/        # home screen widget updates
└── work/          # WorkManager jobs/scheduling
```

## Setup and Installation

### Prerequisites

- Android Studio (latest stable)
- JDK 21 toolchain
- Firebase project with valid Android config

### Local setup

1. Clone the repository.
2. Add Firebase config (`app/google-services.json`).
3. Add your OpenRouter key to `local.properties`:

   ```properties
   OPENROUTER_API_KEY=your_api_key_here
   ```

4. Sync Gradle and build.

## Build and Development

Run from repository root:

- `./gradlew :app:assembleDebug`
- `./gradlew :app:build`
- `./gradlew :app:testDebugUnitTest`
- `./gradlew :app:connectedDebugAndroidTest`
- `./gradlew :app:lintDebug`

Windows equivalents:

- `.\gradlew.bat :app:assembleDebug`
- `.\gradlew.bat :app:build`
- `.\gradlew.bat :app:testDebugUnitTest`
- `.\gradlew.bat :app:connectedDebugAndroidTest`
- `.\gradlew.bat :app:lintDebug`

## Key Conventions

- Keep route declarations and `createRoute(...)` helpers in `ui/navigation/Screen.kt`.
- Keep UI screens state-driven; put business logic in ViewModels and repositories.
- Preserve offline-first data access (DAO flow first, remote refresh second).
- Use Hilt modules in `di/` for service wiring; avoid manual dependency construction in UI.

## Database Schema

`CodePrepDatabase` currently includes:

- `user_progress`
- `lesson_progress`
- `cached_courses`
- `cached_lessons`
- `ai_conversations`
- `ai_conversation_messages`
- `ai_response_cache`
- `cached_public_users`
- `friends`
- `friend_requests`

## Header Shortcuts Check

All table-of-contents header shortcuts above point to existing sections in this README.

## Contributing

1. Create a feature branch.
2. Keep changes focused and small.
3. Run build, tests, and lint before opening a PR.
4. Submit a PR with a clear summary of behavior and validation steps.
