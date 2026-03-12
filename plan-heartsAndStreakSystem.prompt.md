## Plan: Locate hearts refill touchpoints

Map the existing user-progress lifecycle first, then answer the hearts/lives question by tying `UserRepository.refillHearts` to the places where user state is loaded or consumed. In this repo, the likely answer will center on session entry, lesson/quiz entry, and quiz state refresh—not deep inside composables—because the current architecture already keeps heart mutation in repositories and ViewModels.

### Steps
1. Inspect `UserRepository` in `app/src/main/java/com/codeprep/app/data/repository/UserRepository.kt` to confirm the refill contract around `refillHearts`, `loseHeart`, and `getUserProgress`.
2. Trace user record creation and session bootstrap in `app/src/main/java/com/codeprep/app/data/repository/AuthRepositoryImpl.kt`, `app/src/main/java/com/codeprep/app/ui/auth/AuthViewModel.kt`, and `app/src/main/java/com/codeprep/app/MainActivity.kt`.
3. Review lesson list entry flow in `app/src/main/java/com/codeprep/app/ui/navigation/MainNavGraph.kt`, `app/src/main/java/com/codeprep/app/ui/lesson/LessonListViewModel.kt`, and `app/src/main/java/com/codeprep/app/ui/lesson/LessonListScreen.kt` to judge whether refill belongs on screen entry or only when heart-gated actions exist.
4. Review lesson opening flow in `app/src/main/java/com/codeprep/app/ui/lesson/LessonViewModel.kt` and `app/src/main/java/com/codeprep/app/ui/lesson/LessonDetailScreen.kt`, especially around `onStartQuiz`, as a likely pre-quiz refresh point.
5. Review quiz lifecycle in `app/src/main/java/com/codeprep/app/ui/quiz/QuizViewModel.kt` since it already owns `userRepository`, observes `getUserProgress`, and calls `loseHeart`/`addXp`; this is the strongest current touchpoint for invoking `refillHearts` before loading questions or before answer submission.
6. Draft the final guidance around recommended invocation layers: session-level refresh at app/login entry, feature-level refresh at lesson/quiz entry, and avoiding direct repository calls from composables unless there’s no ViewModel for that flow.

### Required Changes
1. **`UserRepository.kt`**
   - Keep `refillHearts(userId)` as the central refill entry point, but call it from session/feature entry instead of UI composables.
   - Fix refill math so `lastHeartLostAt` advances when only part of the 30-minute windows has elapsed; otherwise hearts can refill incorrectly after the first partial refill.
   - Add a dedicated streak update method, e.g. `registerLessonActivity(userId)`, that updates `lastActiveDate` and increments/resets `streak` once per calendar day.
   - Optionally add a helper like `canStartNewLesson(userId)` only if you want gating logic centralized outside the UI layer.
2. **`AuthRepositoryImpl.kt`**
   - Align registration/login with the current `UserProgressEntity` fields (`lastHeartLostAt`, `Instant` values) because the file still uses old `heartsLockedUntil`/`Long`-style data.
   - On login, actually persist the fetched user progress into Room with `userDao.upsert(...)`; right now the entity is constructed but not saved.
   - After hydrating the user, invoke or enable an early refill path so the app does not show stale hearts after returning to the app.
3. **`MainActivity.kt` or root session bootstrap**
   - Add a session bootstrap point that refreshes hearts once when an authenticated user enters the app.
   - Keep this as a consistency refresh, not the only refresh point; lesson/quiz entry should still be allowed to re-check.
4. **`LessonProgressEntity.kt`**
   - Redesign lesson progress so it can represent unlock state per user, not globally per lesson.
   - Add at least: `userId`, `lessonId`, `completed`, `perfectRun` (or `unlockedNextLesson`), `score`, `mistakeCount`, `lastAttemptAt`.
   - Consider using a composite primary key or a synthetic id; current `lessonId` alone is not enough for multi-user progress.
5. **Room wiring (`CodePrepDatabase.kt` + new DAO/repository files)**
   - Add a `LessonProgressDao` and expose it from `CodePrepDatabase`.
   - Create a repository for lesson progress/unlock queries if you want `LessonListViewModel` and `QuizViewModel` to stay thin.
   - Because entity shape will change, expect a Room version bump and migration/reset strategy.
6. **`LessonListViewModel.kt`**
   - Stop exposing only raw `List<CachedLessonEntity>`; produce UI models that include `isUnlocked`, `isCompleted`, `isPerfect`, and possibly `isBlockedByHearts`.
   - Combine ordered lessons with lesson-progress data and current user progress (`hearts`) to compute which lesson cards are tappable.
   - First lesson should always be unlocked; each next lesson unlocks only if the previous quiz was passed with zero mistakes.
   - Call `refillHearts(userId)` when the lesson list is entered or refreshed so gating uses up-to-date hearts.
7. **`LessonListScreen.kt`**
   - Render locked vs unlocked lessons clearly.
   - Prevent navigation when a lesson is locked or when opening a new locked lesson is forbidden because hearts are `0`.
   - Show a small explanation message for blocked states instead of silently ignoring taps.
8. **`LessonViewModel.kt` / `LessonDetailScreen.kt`**
   - Record lesson activity for streak purposes when a lesson is opened the first time that day.
   - If quiz start should be heart-gated, do the check in the ViewModel before navigation, not directly inside `LessonDetailScreen`.
   - Optionally refresh hearts here as a pre-quiz consistency check if you want fresher state than lesson-list entry alone.
9. **`QuizViewModel.kt`**
   - Call `refillHearts(userId)` in `init` or before questions load so the quiz sees the latest heart count.
   - Keep `loseHeart(userId)` on wrong answers, but also track `mistakeCount` or `hadMistake` across the whole attempt.
   - Do **not** auto-close the quiz if hearts reach `0`; the user must be allowed to finish the active quiz session.
   - On finish, persist lesson attempt/progress and unlock the next lesson only if `hadMistake == false`.
   - XP awarding can stay independent from perfect unlock logic unless you want stricter progression rules.
10. **Navigation / course flow (`MainNavGraph.kt` and related screens)**
    - Keep the "0 hearts" restriction at lesson-opening time, not at in-progress quiz time.
    - Ensure revisiting already unlocked/completed lessons stays allowed even when hearts are `0`, if that matches the product rule.
11. **Optional UI surfacing**
    - Expose `hearts` and `streak` on a top bar, home, or profile screen so users can understand why a lesson is blocked and see motivation feedback.

### Concrete Invocation Recommendation for `refillHearts`
1. **On app/session entry**: after login or app start for an already authenticated user.
2. **On lesson list entry**: so lesson availability reflects real-time hearts.
3. **On quiz entry**: as a final consistency check before consuming/updating hearts.
4. **Not inside composables directly**: call it from `ViewModel`/repository-triggered flows.

### Refill Math Bug Explanation and Suggestions
1. **Why the current math is wrong**
   - Current logic computes how many 30-minute windows passed since `lastHeartLostAt`, adds that many hearts, but usually keeps the same `lastHeartLostAt` value.
   - That means already-consumed refill time is counted again on the next call.
   - Example: if the user has `0` hearts and `65` minutes passed, they get `+2` hearts. If `lastHeartLostAt` stays unchanged, then `10` minutes later the code sees `75` minutes from the original timestamp and can grant progress too early because it did not “spend” the first `60` minutes.
2. **Preferred fix**
   - After a partial refill, move `lastHeartLostAt` forward by the number of fully consumed 30-minute windows.
   - In practice: calculate `heartsToAdd`, then if the user is still below `5`, set `lastHeartLostAt = oldLastHeartLostAt + heartsToAdd * 30 minutes`.
   - If hearts reach `5`, set `lastHeartLostAt = null`.
   - This preserves the remainder correctly. Example: `65` minutes => `+2` hearts and anchor moves forward by `60` minutes, so the next heart still needs `25` more minutes.
3. **Suggestion to add to implementation notes**
   - Re-check `loseHeart(userId)` as well: right now it starts the refill timer only when hearts drop to `0`.
   - If the intended product behavior is Duolingo-like refill from any value below `5`, then the timer should start on the first loss from `5 -> 4`, and stay anchored until hearts refill back to `5`.
4. **Alternative fixes**
   - **Alternative A:** rename the field conceptually to `heartRefillAnchorAt` and keep the same behavior, but document that it is the refill anchor, not strictly the last loss time.
   - **Alternative B:** replace `lastHeartLostAt` with `nextHeartRefillAt`, which can make the math easier to reason about in the UI and repository.
   - **Alternative C:** store per-missing-heart timestamps for the most precise model, but this is heavier than needed for the current app.

### Further Considerations
1. Preferred policy: A) refresh only on app/login entry, B) refresh on every lesson/quiz entry, or C) both for strongest consistency?
2. If hearts will block quiz start, `LessonViewModel` or `QuizViewModel` is a cleaner owner than `LessonDetailScreen`.
3. Current auth/session flow appears incomplete for user-progress hydration, so the final answer should call out `app/src/main/java/com/codeprep/app/MainActivity.kt` and `app/src/main/java/com/codeprep/app/data/repository/AuthRepositoryImpl.kt` as likely gaps to verify.
4. There is already a visible model mismatch in the repo (`UserProgressEntity` uses `Instant`/`lastHeartLostAt`, while `AuthRepositoryImpl.kt` still references `heartsLockedUntil`/timestamp longs), so part of the work is basic model alignment before feature logic can be reliable.
5. `LessonProgressEntity` is currently present but unused; that is the main missing persistence piece for unlock rules.
