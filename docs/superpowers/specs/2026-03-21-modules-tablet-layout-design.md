# Modules Tablet Layout

## Goal

Improve the Modules list screen on tablets so it feels intentionally designed for larger screens instead of a phone layout stretched edge to edge.

## Current App Context

- The modules screen lives in `app/src/main/java/com/codeprep/app/ui/course/CourseListScreen.kt`.
- It currently uses one full-width column with phone-sized outer padding and card spacing.
- Cards already have the right information density for the product, but the tablet composition is weak because content stretches too wide and keeps phone-like internal rhythm.
- The app already has a tablet-specific layout pattern on the home screen, so the modules screen should follow the same idea of larger-screen containment without turning into a desktop-style dashboard.

## Chosen Direction

Use a centered single-column tablet layout.

The tablet screen should keep the full dark background, but the actual modules content should sit inside a bounded center container. The screen should not take the whole available width. The result should feel calmer, more premium, and easier to scan.

This is the approved `Option A2` direction from the browser review:

- single column only
- centered content lane
- balanced container width
- more breathing room in the header and cards
- no two-column grid
- no lesson-screen redesign

## Layout Behavior

### Phone behavior

- Keep the existing single-column structure.
- Keep phone-first spacing close to the current screen.
- Do not widen the layout logic on smaller screens just because the tablet version is improving.

### Tablet behavior

- Switch to a contained center column when `screenWidthDp >= 600`.
- Center the content horizontally inside the full-screen background.
- Cap the content width around the balanced range used in the design review, approximately `780-800dp`.
- Add larger outer horizontal gutters than phone so the content lane clearly reads as intentional tablet composition.
- Increase top spacing for the title block so the screen has more visual lift.
- Keep the list itself as a vertical stack of cards.

## Card Treatment

Cards should improve in quality, not just size.

- Increase internal card padding slightly on tablets.
- Increase vertical spacing between major sections inside the card.
- Give the title and description more room to breathe.
- Keep the icon, status affordance, pills, progress bar, and status line in the same order so the screen remains familiar.
- Preserve active, perfect, and locked states.

## Metadata Row Behavior

The metadata pills should wrap cleanly instead of assuming a single rigid row.

- On tablets, pills should have enough room to breathe.
- On narrower states or with longer localized text, pill wrapping should remain graceful.
- The layout should avoid clipping or awkward compression around the progress section.

## Width Targets

These values are guidance for implementation, not rigid design tokens:

- phone outer horizontal padding: keep near the current `16dp`
- tablet outer horizontal padding: increase into the `32-40dp` range
- tablet content max width: approximately `780-800dp`
- phone card padding: keep close to current
- tablet card padding: increase into the `22-24dp` range
- list item spacing on tablet: increase modestly beyond phone

The most important rule is not the exact number. It is that the modules content remains clearly contained and does not span the whole tablet width.

## Implementation Notes

- Follow the existing home-screen pattern by introducing an internal layout spec function for the course list screen.
- Keep the implementation local to `CourseListScreen.kt` unless a focused helper extract becomes clearly useful.
- Add test tags for the tablet content container and at least one module card so layout tests can verify containment and centering.
- Prefer verification that checks bounded width and centered placement on tablet.
- Avoid unrelated visual refactors or theme changes.

## Verification

The work is correct when all of the following are true:

- On phone widths, the screen still behaves like the current single-column modules list.
- On tablet widths, the modules content is centered and visibly narrower than the full screen.
- The tablet content width stays capped near the approved balanced range.
- Cards feel roomier on tablets through spacing and padding, not through full-width stretching.
- Metadata pills wrap cleanly when space or localization requires it.
- Locked and unlocked cards still communicate state clearly.
- No new clipping or layout regressions appear in Compose UI tests or targeted manual checks.
