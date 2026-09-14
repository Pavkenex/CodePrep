# Copilot Instructions for CodePrep

## Build, test, and lint commands

Run commands from the repository root (Windows):

- `.\gradlew.bat :app:assembleDebug` - build debug APK
- `.\gradlew.bat :app:build` - full module build/check pipeline
- `.\gradlew.bat :app:testDebugUnitTest` - run JVM unit tests
- `.\gradlew.bat :app:testDebugUnitTest --tests "com.codeprep.app.ExampleUnitTest.addition_isCorrect"` - run one JVM test
- `.\gradlew.bat :app:connectedDebugAndroidTest` - run instrumentation tests on connected emulator/device
- `.\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.codeprep.app.ExampleInstrumentedTest` - run one instrumentation test class
- `.\gradlew.bat :app:lintDebug` (or `.\gradlew.bat :app:lint`) - run Android lint

## High-level architecture

- Single-module Android app (`:app`) using Jetpack Compose, Hilt DI, Room, Firebase Auth/Firestore, and Retrofit/OkHttp.
- App bootstrap is `CodePrepApp` (`@HiltAndroidApp`) + `MainActivity` (`RootNavGraph`). Navigation is split into nested `"auth"` and `"main"` graphs in `ui/navigation/`.
- UI layer is Compose screens backed by Hilt ViewModels (`AuthViewModel`, `CourseViewModel`, `LessonListViewModel`, `LessonViewModel`, `QuizViewModel`, `SessionBootstrapViewModel`) exposing `StateFlow`/`Flow`.
- Data layer centers on repositories:
  - `AuthRepositoryImpl`: Firebase Auth register/login and local/remote user profile bootstrap.
  - `CourseRepository`: Firestore read (`modules -> lessons -> questions`) with Room caching for course/lesson lists.
  - `UserRepository`: XP/level/hearts/streak rules and Room + Firestore synchronization/hydration.
  - `LessonProgressRepository`: per-user lesson attempt/completion/perfect-run tracking.
- Room DB (`CodePrepDatabase`) persists `user_progress`, `lesson_progress`, `cached_courses`, `cached_lessons`, and `ai_explanations`; `Converters` handles `Instant`, `List<String>`, and `List<CodeSnippet>`.
- AI API wiring is in `di/NetworkModule.kt` and `data/remote/api/OpenRouterApi.kt`; the API key, model ID, and base URL are user-configured and stored encrypted in `data/settings/AiSettingsStore.kt` (`EncryptedSharedPreferences`). `NetworkModule` reads them per request via the `EndpointBuilder` interceptor; no build-time key exists.

## Key conventions in this repository

- Add/modify routes in `ui/navigation/Screen.kt` and keep route-string construction in `createRoute(...)` helpers.
- Keep screens thin and state-driven; business logic stays in ViewModels/repositories.
- Follow the existing offline-first pattern: repositories return DAO `Flow` first, then refresh cache from Firestore in background coroutines.
- Before quiz/session-gated actions, hydrate/check local user state via `UserRepository.ensureLocalUserProgress(...)`; session startup refresh is handled by `SessionBootstrapViewModel.refreshHeartsOnSessionStart()`.
- Hearts/streak logic is centralized in `UserRepository`:
  - hearts cap is 5
  - refill interval is 30 minutes using `lastHeartLostAt`
  - lesson unlock progression depends on previous lesson `perfectRun`
  - completed lessons may be replayed even when hearts are 0
- Keep DI wiring in Hilt modules under `di/` (`DatabaseModule`, `FirebaseModule`, `NetworkModule`, `RepositoryModule`) and extend those instead of manual service construction in UI code.

