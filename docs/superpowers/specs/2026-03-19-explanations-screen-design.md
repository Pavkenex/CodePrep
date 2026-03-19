# Explanations Screen Redesign

## Goal

Rename the `AI Coach` tab to `Explanations` and redesign the screen as a calm archive of saved lesson conversations. The screen should feel tied to the app's learning structure, not like a separate activity feed.

## Current App Context

- The bottom navigation currently uses the `AI Coach` label and opens a placeholder-style saved conversation list.
- Saved AI conversations already exist as one saved conversation per lesson.
- The existing conversation screen already supports loading a saved conversation and continuing it.
- Module titles and lesson titles already support English and Serbian localization.

## Primary Product Direction

`Explanations` should be a module-first archive.

- Show modules in the app's normal course or module order.
- Keep all modules collapsed by default.
- Allow only one module to be expanded at a time.
- Inside an expanded module, show only lessons that have a saved conversation.
- Show lessons in the app's normal lesson order.
- Tapping a lesson should open the existing conversation screen for that lesson.
- The existing conversation screen should load the saved conversation and still allow the user to continue asking questions.

This keeps the screen aligned with the rest of the app's course structure and avoids turning it into a recency-based history feed.

## Screen Structure

The phone layout should contain two main regions:

1. A short header
2. A stacked list of module cards

### Header

The header should stay minimal.

- Title: `Explanations`
- One short localized helper line under the title

Do not show counters such as:

- modules saved
- lessons saved
- last reviewed

Those details add noise without improving the screen's purpose.

### Module Cards

Each module should render as a calm card in a stacked list.

- Cards are collapsed by default.
- Expanding one module collapses any previously open module.
- Expanded content reveals saved lessons for that module only.

Do not add extra badges, counters, or status labels unless they add real value.

## Lesson Rows

Each lesson row should stay clean.

- Show the localized lesson title
- Show a preview from the saved conversation exactly as stored

Do not show extra row labels such as `Saved conversation`.

Saved conversation content should not be translated, rewritten, summarized again, or normalized. Whatever was saved should be shown as-is.

## Visual Direction

Use the approved `calm archive` direction.

- Keep the screen visually quieter than the live lesson AI flow.
- Preserve the app's existing dark card language.
- Favor soft contrast, clear spacing, and restrained accents.
- Make the screen feel like a study shelf, not a chat launcher or analytics dashboard.

The redesign should feel intentional, but not loud.

## Empty State

Use a simple empty state.

- Keep the same minimal header.
- Show a calm message that explains saved lesson conversations will appear here after the user saves one.
- Do not add CTA buttons such as `Go to Modules`.

The empty state should explain the screen, not redirect the user somewhere else.

## Navigation And Ordering

Ordering rules:

- Modules follow the app's normal module order
- Lessons follow the app's normal lesson order

Navigation rule:

- Tapping a saved lesson from `Explanations` opens the existing interactive conversation screen, not a separate read-only viewer

`Explanations` is an archive entry point. The lesson conversation screen remains the place where users read and continue saved conversations.

## Copy And Localization

All fixed UI copy introduced for this screen must exist in both English and Serbian.

This includes:

- the bottom nav label
- the screen title
- the short helper line
- the empty-state text

Localized lesson and module titles should continue using the app's existing data and localization flow.

Saved conversation text should remain untouched, regardless of the current app language.

## Implementation Direction

Build `Explanations` as a new presentation of existing saved conversation data rather than a new persistence model.

Implementation should:

- rename the bottom navigation label from `AI Coach` to `Explanations`
- group saved conversations by module
- filter out lessons without a saved conversation
- keep accordion expansion state as UI-only state
- reuse the existing conversation screen when a lesson row is tapped

This redesign does not need multi-thread history, extra archive metadata, or new content transformation rules.

## Verification

- Bottom navigation shows `Explanations` in English and Serbian.
- The `Explanations` screen uses the minimal header and calm archive styling.
- Modules appear in the app's normal module order.
- Modules are collapsed by default.
- Only one module can be expanded at a time.
- Expanded modules show only lessons with saved conversations.
- Lessons appear in normal lesson order.
- Lesson rows show only the localized title and saved preview text.
- Saved preview text is shown exactly as stored.
- Tapping a lesson opens the existing conversation screen with the saved conversation loaded.
- Users can continue asking questions from that conversation screen.
- The empty state renders correctly when no saved conversations exist.

## Out Of Scope

- Tablet-specific or large-screen layouts
- A separate read-only explanation viewer
- Recency-based module or lesson sorting
- Additional archive counters, progress metrics, or CTA-heavy empty states
