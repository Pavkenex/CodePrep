# Heart Refill Timer Design

Date: 2026-03-16

## Goal

Show a countdown in the shared top bar when the user has fewer than 5 hearts. The countdown must show the time until the next heart refill. Hide the countdown when hearts are full.

## Current State

- `UserRepository` owns heart rules, including `loseHeart(userId)`, `refillHearts(userId)`, and `calculateTimeUntilFullHearts(progress, now)`.
- `SessionBootstrapViewModel` already exposes `currentUserProgress` and refreshes hearts at session start.
- `TopBarStats` is rendered once from `MainActivity`, so it is the correct place for a shared heart timer.
- Quiz and lesson flows already call `refillHearts(userId)` before heart-gated actions.

## Scope

In scope:

- Add a shared countdown for the next heart refill.
- Show it only while `hearts < 5`.
- Feed the timer from session-level user progress.
- Refresh heart state when the countdown reaches zero.

Out of scope:

- Notifications or background-only timer UX changes.
- New persistence fields.
- Changing heart refill rules or heart cap.
- Redesigning the top bar beyond the timer addition.

## Recommended Approach

Keep the countdown as a session-level UI concern.

`SessionBootstrapViewModel` should derive timer state from `currentUserProgress`. `MainActivity` should pass that derived state into `TopBarStats`. `TopBarStats` should render a secondary heart label such as `Next in 12:43` only when hearts are below the cap.

This keeps business rules in `UserRepository`, keeps the timer close to the shared UI that needs it, and avoids duplicating countdown logic across screens.

## Data Flow

1. `SessionBootstrapViewModel` observes `currentUserProgress`.
2. When `hearts >= 5` or `lastHeartLostAt == null`, timer state is `null`.
3. When `hearts < 5`, the view model starts a lightweight ticker.
4. On each tick, the view model computes the remaining time until the next refill boundary.
5. `MainActivity` collects the derived timer label and passes it to `TopBarStats`.
6. When the timer reaches zero, the view model calls `userRepository.refillHearts(userId)`.
7. If hearts are still below 5 after refill, the timer continues from the updated progress state.
8. If hearts reach 5, the timer stops and the label disappears.

## Countdown Logic

The timer must show time until the next single heart refill, not time until full hearts.

Rules:

- If `lastHeartLostAt == null`, show no timer.
- If `hearts >= 5`, show no timer.
- The next refill time is `lastHeartLostAt + 30 minutes`.
- Remaining time is `nextRefillAt - now`, clamped at zero.
- When remaining time hits zero, trigger `refillHearts(userId)` and recompute from the new state.

Formatting:

- Prefer `MM:SS` when under one hour.
- Prefer `H:MM:SS` if the remaining time can exceed one hour.
- Prefix with short copy such as `Next in`.

## UI Behavior

`TopBarStats` should keep the current heart count as the primary value.

When `hearts < 5`:

- Show a smaller secondary text line near the heart badge.
- Example: `Next in 12:43`.
- Keep the streak badge unchanged.

When `hearts == 5`:

- Hide the secondary timer text.
- Keep the existing layout stable so the top bar does not jump excessively.

## Lifecycle And Performance

- The timer should live in `SessionBootstrapViewModel`, not in each screen.
- The ticker should run only while the user is logged in and hearts are below 5.
- The ticker should stop automatically when the shared top bar is no longer shown.
- The timer should derive display state in memory only. It should not write to Room every second.

## Error Handling

- If user progress is unavailable, `TopBarStats` should fall back to the current behavior and show only the heart count.
- If `refillHearts(userId)` fails when the timer reaches zero, retry on the next tick or the next repository-driven refresh.
- If local progress is stale after app resume, rely on the existing session-start refresh path before showing long-lived countdown state.

## Testing

Unit tests:

- Timer is hidden at 5 hearts.
- Timer is hidden when `lastHeartLostAt` is `null`.
- Timer shows the correct remaining time when hearts are below 5.
- Timer rolls over to the next cycle after a refill when hearts remain below 5.
- Timer stops after hearts reach 5.

UI tests:

- Shared top bar shows only the heart count at 5 hearts.
- Shared top bar shows countdown text when hearts are below 5.
- Countdown text updates without requiring screen navigation.

Integration checks:

- Session start refresh updates heart count before countdown display.
- Lesson and quiz entry still honor refreshed heart state.

## Risks

- If `lastHeartLostAt` is treated inconsistently as both "last loss time" and "refill anchor," countdown drift can appear after repeated refills.
- If the ticker is owned by Compose instead of the session view model, multiple screens can start duplicate timers.
- If the top bar layout is not constrained, adding a second line can cause clipping on small screens.

## Implementation Notes

- Reuse the existing `UserProgressEntity` fields.
- Prefer adding a small helper in `UserRepository` for "time until next heart" if the countdown math would otherwise be duplicated.
- Keep `TopBarStats` API simple: `hearts`, `streak`, and nullable timer text.

## Acceptance Criteria

- The shared top bar displays a countdown only when the user has fewer than 5 hearts.
- The countdown shows the time until the next heart refill.
- The countdown disappears when the user returns to 5 hearts.
- The countdown updates without requiring navigation or manual refresh.
- The feature does not introduce a second source of truth for heart state.
