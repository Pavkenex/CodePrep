# Sound Feedback Design

## Goal

Add a balanced feedback layer to CodePrep that makes the app feel responsive and rewarding without turning routine navigation into noise.

The system should support:

- short in-app sound effects
- short in-app haptics
- separate user controls for sound and haptics
- Android-managed notification sound and vibration for background reminders

## Product Direction

The feedback style is `balanced`.

- Primary controls should feel responsive.
- Progress moments should feel more rewarding than ordinary taps.
- Errors should be clear but restrained.
- Background reminders should use normal Android notification behavior instead of custom app playback.

This feature should make the app feel more alive, not louder.

## Scope

Include:

- in-app sound effects for key interactions and outcomes
- in-app haptic feedback for key interactions and outcomes
- separate persisted toggles for sound effects and haptics
- notification-first handling for worker-driven reminder events such as heart refills and streak reminders

Do not include:

- background music
- ambient loops
- voice lines
- keyboard click sounds for every character
- per-event volume sliders
- custom sound packs
- custom worker playback while the app is in the background

## Architecture

Use one app-level feedback layer with a semantic API.

### Core Pieces

`AppFeedbackManager`

- Owns in-app feedback playback decisions.
- Exposes semantic event entry points such as `emit(FeedbackEvent.TapPrimary)` and `emit(FeedbackEvent.Reward)`.
- Reads the current sound and haptic settings before playing anything.
- Enforces suppression rules so the same action chain does not feel duplicated.

`FeedbackEvent`

Use a small semantic event set instead of screen-specific event names.

Initial set:

- `TapPrimary`
- `Confirm`
- `Success`
- `Reward`
- `Milestone`
- `Error`
- `HeartLoss`
- `HeartRefill`
- `NotificationReminder`

`SoundEngine`

- Internal part of the manager responsible for in-app audio playback.
- Use `SoundPool` to preload a very small set of short audio assets.
- Fail silently if assets do not load or playback is unavailable.

`HapticsEngine`

- Internal part of the manager responsible for in-app tactile feedback.
- Map semantic events to short Android haptic patterns.
- Prefer built-in behavior and degrade gracefully on devices with limited haptic support.

`AppSettingsStore`

- Stores `soundEffectsEnabled`.
- Stores `hapticsEnabled`.
- Remains the single source of truth for app-level feedback preferences.

### Boundary Rules

- Shared UI components trigger direct interaction events such as `TapPrimary`.
- View models trigger confirmed outcome events such as `Success`, `Reward`, `Milestone`, and `Error`.
- Workers do not call `AppFeedbackManager` for background playback.
- Compose recomposition and passive state observation must never trigger feedback on their own.

This keeps the design simple: UI emits taps, business logic emits outcomes, and the manager enforces consistency.

## Event Map

The feedback language should stay small and predictable.

### Direct Interaction Events

`TapPrimary`

Use for primary controls only:

- `GamifiedButton`
- lesson path nodes
- bottom navigation items
- submit buttons
- continue buttons
- key settings rows
- avatar selection rows
- language selection rows

Feedback style:

- short, light click
- light tap haptic

`Confirm`

Use when the user commits an action that starts or advances a flow:

- sending an AI prompt
- confirming import
- confirming export
- starting a quiz
- continuing after an answered question
- confirming an alert or dialog

Feedback style:

- slightly fuller than `TapPrimary`
- clean confirmation haptic

### Positive Outcome Events

`Success`

Use for immediate positive outcomes that deserve acknowledgment but not celebration:

- correct quiz answer
- successful save
- successful import
- successful export
- successful avatar change

Feedback style:

- short upward sound
- crisp light success haptic

`Reward`

Use when the app advances player state:

- XP gain
- lesson completion
- streak continuation
- lesson unlock
- daily challenge completion

Feedback style:

- brighter and more celebratory than `Success`
- short stronger haptic than routine success

`Milestone`

Use for rarer and more memorable moments:

- perfect run
- major streak milestone
- module unlock
- major progression threshold

Feedback style:

- strongest celebratory cue in the set
- richer success haptic, still short

### Negative And Recovery Events

`Error`

Use for rejected or failed actions:

- wrong quiz answer
- invalid form submit
- failed import or export
- unavailable AI request
- blocked action caused by validation or current app state

Feedback style:

- short restrained low cue
- brief rejection haptic

`HeartLoss`

Use when gameplay state worsens because an incorrect answer costs a heart.

Feedback style:

- heavier negative cue than `Error`
- firmer warning haptic

`HeartRefill`

Use when the app is open and hearts become available again after an explicit refresh or foreground-confirmed state change.

Feedback style:

- short recovery cue
- soft restoring haptic

### Notification And Background Events

`NotificationReminder`

This is a logical event category, not an in-app playback cue.

Use it for worker-driven reminders such as:

- hearts refilled while the app is backgrounded
- streak reminder notifications

Behavior:

- rely on Android notification channel sound and vibration
- do not trigger custom in-app sound or haptics from workers

## Silence Rules

Do not play feedback for:

- scrolling
- passive data refresh
- Compose recomposition
- screen restoration after process or navigation restoration
- screen entry and exit by default

Avoid duplicate bursts:

- do not stack multiple cues for the same action chain unless they communicate clearly different stages
- if a tap and an outcome happen almost back to back, the manager should suppress or space them so the result still feels intentional

## Settings And Control

Add two separate toggles to the profile settings sheet.

`Sound effects`

- controls in-app feedback audio only
- defaults to enabled
- persists across app restarts

Suggested helper text:
`Short sounds for taps, rewards, and errors in the app.`

`Haptics`

- controls in-app tactile feedback only
- defaults to enabled
- persists across app restarts

Suggested helper text:
`Vibration feedback for taps, rewards, and errors in the app.`

### Rules

- If sound is off and haptics are on, the app still uses haptics.
- If haptics are off and sound is on, the app still uses sound.
- If both are off, the feedback manager becomes silent but the UI still works normally.
- These toggles control in-app feedback only.
- Notification sound and vibration remain controlled by Android notification settings and channel behavior.

This wording matters. It avoids promising that in-app toggles control background reminders.

## Integration Points

Phase the integration to keep tuning manageable.

### Phase 1

Wire the core manager into the highest-value surfaces:

- `GamifiedButton`
- lesson path nodes
- bottom navigation
- quiz answer submission
- quiz result states
- profile settings toggles

### Phase 2

Add progress and gameplay outcomes:

- lesson completion
- XP gain
- unlocks
- streak continuation
- heart loss
- heart refill while the app is open

### Phase 3

Polish secondary flows:

- AI send, confirm, and error events
- import and export success or failure
- avatar change confirmation
- other important settings actions

## Failure Handling

- If sound assets fail to load, the app continues silently.
- If haptics are unsupported, the app skips them without warning.
- If playback fails, the feature must not block the user flow.
- Workers must never attempt custom background playback.

## Testing

### Unit Tests

- `AppSettingsStore` persists `soundEffectsEnabled`.
- `AppSettingsStore` persists `hapticsEnabled`.
- `AppFeedbackManager` suppresses sound playback when sound is disabled.
- `AppFeedbackManager` suppresses haptics when haptics are disabled.
- semantic events map to the correct sound and haptic categories
- duplicate suppression rules behave correctly for quick tap-followed-by-result flows

### UI Tests

Use Jetpack Compose UI testing as the primary approach, not raw Espresso.

Why:

- the app UI is Compose-first
- Compose tests understand semantics, state, and Compose nodes directly
- tests will be easier to read and less brittle than view-based Espresso selectors

Use Espresso only where needed for hybrid edges such as:

- permission flows
- system dialogs
- non-Compose surfaces
- notification-related instrumentation gaps

UI verification should cover:

- primary controls still work after feedback wrappers are added
- profile settings sheet shows both toggles
- toggling settings updates visible UI state correctly
- quiz flows still behave correctly while feedback events are emitted

### Manual Verification

- taps feel light and limited to primary controls
- correct answers feel clearly better than ordinary taps
- heart loss feels more serious than a generic error
- milestone moments stand out more than routine rewards
- turning off sound silences foreground playback
- turning off haptics stops foreground vibration
- background reminder sound and vibration follow Android notification behavior, not custom app playback

## Acceptance Criteria

- Primary controls produce a short shared tap cue when sound effects are enabled.
- Primary controls produce a light haptic when haptics are enabled.
- Correct answers, rewards, unlocks, and milestones produce clearer positive feedback than simple taps.
- Wrong answers and blocked actions produce restrained negative feedback.
- Heart loss produces a stronger negative cue than a generic error.
- The user can toggle sound effects and haptics separately from profile settings.
- The toggles persist across app restarts.
- Passive updates and recomposition do not trigger feedback.
- The app remains stable and usable if sound or haptics fail.
- Worker-driven reminder events rely on Android notification channel behavior rather than custom in-app playback.

## Out Of Scope

- background music
- long ambient audio
- voice narration
- per-event sliders
- custom sound pack selection
- complex reduced-motion controls in this pass
- custom background playback from workers
