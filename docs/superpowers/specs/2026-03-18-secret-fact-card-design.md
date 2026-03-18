# Secret Fact Card

## Goal

Add a hidden sensor-driven easter egg that feels playful but still educational. On safe screens, shaking the device arms a short-lived secret state. Rotating to landscape within that window reveals a polished fact card that reuses the existing `funFacts` content.

## Current App Context

- CodePrep is an Android app built with Compose.
- Recent work has focused on lesson and module UI redesigns, but this feature should stay outside disruptive study flows.
- The app already has a `FunFactRepository` that loads localized fun facts from Firestore and falls back to cached content when needed.
- The app already has a `FunFactActivity`, widget support, and shared fun-fact strings, so the new easter egg should reuse those content and fallback paths instead of creating a parallel source.

## Product Intent

- Keep the interaction hidden and delightful.
- Give the gesture a real learning payoff, not a pure joke.
- Make the sequence feel intentional:
  - shake arms the easter egg
  - rotation reveals the fact
- Limit activation to safe surfaces so the feature never interrupts lessons, quizzes, auth, or other focused flows.

## Supported Surfaces

Enable the feature only on approved low-risk surfaces, such as:

- home
- dashboard
- profile
- other non-critical discovery screens

Do not enable it on:

- auth flows
- quizzes or tests
- active lesson tasks
- modal flows where rotation or overlays would feel disruptive

The allowlist should be explicit and owned by navigation or screen-level metadata, not inferred from UI state.

## User Flow

1. The user is on a supported screen in portrait.
2. The app detects a valid shake gesture.
3. The app enters an `armed` state for a short window, such as 4 to 6 seconds.
4. A subtle visual hint confirms that something is ready.
5. While armed, the app prepares one fun fact from the existing repository.
6. If the user rotates to landscape before the timeout expires, the `Secret Fact Card` reveal plays.
7. If the user does nothing, the armed state expires quietly and the app returns to idle.

## Interaction Model

### Shake

- Shake does not open the card immediately.
- Shake arms the easter egg and starts the timeout window.
- Shake should feel intentional, so the threshold must reject normal handling noise and small motion.

### Armed Hint

The app should show a brief effect that hints that the ritual is active without exposing the whole feature. Good candidates:

- a soft glow pulse near the top app bar
- a short shimmer across the screen
- a small floating line such as `Secret fact ready...`
- a tiny icon or accent flash tied to CodePrep branding

Avoid a standard snackbar or heavy dialog. The cue should feel secret, not procedural.

### Rotation Reveal

- Rotation to landscape only works while the feature is armed.
- The app should require a fresh portrait-to-landscape change after arming.
- If the phone is already in landscape, do not reveal until the user returns to portrait and performs the ritual again.

## Content Strategy

Reuse the existing `funFacts` pipeline.

- Use `FunFactRepository` as the source of fact content.
- Preserve the existing language resolution behavior.
- Preserve existing cache fallback behavior where possible.
- Do not introduce a second fun-fact collection or a separate easter-egg-only repository.
- Support both app languages explicitly:
  - Serbian (`sr`)
  - English (`en`)

The reveal should prefer a random fun fact rather than today's fact. That keeps the easter egg surprising and makes repeated discoveries more fun. If the current repository only supports the daily fact path, extend it with a small random-selection method rather than duplicating logic elsewhere.

All visible text in the feature should respect the currently selected app language, not just the fact body itself. This includes:

- armed hint copy
- reveal title
- dismiss action
- any fallback or empty-state text used by the card

Language behavior should follow the same pattern as the existing fun-fact flow:

1. use the selected app language
2. fall back to English when needed
3. fall back to Serbian when needed

## Architecture

### Sensor Gesture Controller

Listens for shake and orientation signals only while a supported screen is active. It should stay isolated from presentation code and emit high-level events such as:

- `ShakeDetected`
- `LandscapeRevealTriggered`
- `ArmingExpired`

### Secret Fact Coordinator

Owns the feature state machine:

- `idle`
- `armed`
- `revealed`

Responsibilities:

- gate the feature by current screen
- start and cancel the arming timeout
- request a fact when arming starts
- reveal only after a valid shake-first sequence
- reset cleanly after dismissal or timeout

### Fun Fact Loader

Reuse the existing repository. The fact should be selected as soon as the feature arms so the reveal feels instant.

### Secret Fact Card UI

A dedicated composable or overlay that presents:

- playful title
- localized fact text
- optional small badge or category if the data model supports it
- dismiss action
- optional secondary action for `another fact` in a later iteration

All user-facing strings in this UI must ship in both Serbian and English.

## Visual Direction

The reveal should feel like the current screen is sliding or flipping into a hidden layer of knowledge.

- Use a polished slide-in or card-reveal transition.
- Keep the card readable and educational first.
- Add personality through title, motion, and subtle visual effects rather than noisy decoration.
- Aim for magical with a wink, not chaotic parody.

Possible titles:

- `Secret Fact Unlocked`
- `Forbidden Knowledge`
- `You Shook Loose a Fact`

Final UI copy can be tuned during implementation.

## State and Timing Rules

- Default state is `idle`.
- Arming timeout should be short, around 4 to 6 seconds.
- A new shake while already armed can either refresh the timer or be ignored. Refreshing is the better default because it feels forgiving.
- Dismissing the card returns the feature to `idle`.
- Leaving the supported screen should immediately cancel the armed state.
- App backgrounding should cancel the armed state.

## Error Handling

- Ignore all sensor events on unsupported screens.
- Ignore weak or noisy shake events.
- If fact loading fails, fall back to the existing cached fun-fact path when available.
- If no fact is available at all, cancel the reveal quietly rather than showing a broken card.
- If rotation happens after timeout expiry, do nothing.

## Accessibility and UX Guardrails

- The card must remain readable in landscape.
- Motion should be fast and clean, not disorienting.
- Dismissal must be obvious.
- The feature should never trap the user in landscape-only UI.
- The hint should be visible enough to teach the sequence once discovered, but subtle enough to preserve the easter-egg feel.

## Testing

### Unit Tests

- state transitions: `idle -> armed -> idle`
- state transitions: `idle -> armed -> revealed -> idle`
- supported-screen gating
- timeout expiry
- fresh rotation requirement after arming
- repository reuse and fallback behavior

### Manual Verification

- shake on a supported screen shows the armed hint
- rotation during the armed window reveals the card
- no reveal occurs after timeout expiry
- no reveal occurs on blocked screens
- dismiss returns the app to the prior screen cleanly
- localized fact text matches the selected language
- armed hint and card UI strings match the selected language
- fallback behavior remains safe when Firestore data is unavailable

## Implementation Notes

- Start with a supported-screen allowlist rather than global activation.
- Keep sensor handling and UI state separate.
- Prefer extending `FunFactRepository` for random fact selection instead of introducing new data plumbing.
- Ship the first version without collectibles, sharing, or persistent unlock history.

## Out of Scope

- achievements or badge systems
- persistent easter-egg inventory
- new fun-fact authoring tools
- enabling the feature during quizzes, lessons, or auth
- adding more gesture rituals before the first version lands
