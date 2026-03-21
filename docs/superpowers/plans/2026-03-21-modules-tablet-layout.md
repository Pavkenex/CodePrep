# Modules Tablet Layout Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Update the Modules list screen so tablets use a centered, bounded single-column layout with stronger spacing and calmer card composition.

**Architecture:** Keep the existing single-screen Compose structure, add a small internal layout spec for phone vs tablet behavior, and apply tablet-only containment plus spacing upgrades inside `CourseListScreen.kt`. Verify the logic with one small JVM layout-spec test and one Compose tablet layout test that checks bounded width and centering.

**Tech Stack:** Android, Kotlin, Jetpack Compose Material 3, Compose UI tests, JUnit 4, Gradle

---

## File Structure

- Create: `app/src/test/java/com/codeprep/app/ui/course/CourseListLayoutSpecTest.kt`
- Create: `app/src/androidTest/java/com/codeprep/app/ui/course/CourseListScreenTabletLayoutTest.kt`
- Modify: `app/src/main/java/com/codeprep/app/ui/course/CourseListScreen.kt`

### Task 1: Add a failing layout-spec test for phone and tablet breakpoints

**Files:**
- Create: `app/src/test/java/com/codeprep/app/ui/course/CourseListLayoutSpecTest.kt`
- Modify: `app/src/main/java/com/codeprep/app/ui/course/CourseListScreen.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.codeprep.app.ui.course

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CourseListLayoutSpecTest {

    @Test
    fun phoneWidth_keepsFullWidthSingleColumnBehavior() {
        val layout = courseListLayoutFor(screenWidthDp = 411)

        assertFalse(layout.useTabletContainer)
        assertTrue(layout.maxContainerWidthDp == 0)
    }

    @Test
    fun tabletWidth_usesBoundedCenteredContainer() {
        val layout = courseListLayoutFor(screenWidthDp = 800)

        assertTrue(layout.useTabletContainer)
        assertTrue(layout.maxContainerWidthDp in 780..800)
        assertTrue(layout.screenPaddingDp in 32..40)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.codeprep.app.ui.course.CourseListLayoutSpecTest"`
Expected: FAIL because `courseListLayoutFor` and the layout spec do not exist yet.

- [ ] **Step 3: Write minimal implementation**

Add an internal `CourseListLayoutSpec` data class and a `courseListLayoutFor(screenWidthDp: Int)` helper in `CourseListScreen.kt` with:

- phone behavior that preserves the current full-width list
- tablet behavior that enables a bounded centered container
- balanced tablet max width near `780-800dp`
- larger tablet padding and spacing values

- [ ] **Step 4: Run test to verify it passes**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.codeprep.app.ui.course.CourseListLayoutSpecTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/test/java/com/codeprep/app/ui/course/CourseListLayoutSpecTest.kt app/src/main/java/com/codeprep/app/ui/course/CourseListScreen.kt
git commit -m "test: define modules tablet layout spec"
```

### Task 2: Add a failing tablet Compose test for bounded width and centering

**Files:**
- Create: `app/src/androidTest/java/com/codeprep/app/ui/course/CourseListScreenTabletLayoutTest.kt`
- Modify: `app/src/main/java/com/codeprep/app/ui/course/CourseListScreen.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.codeprep.app.ui.course

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.codeprep.app.ui.theme.CodePrepTheme
import kotlin.math.abs
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CourseListScreenTabletLayoutTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun tabletLayout_centersBoundedCourseColumn() {
        composeTestRule.setContent {
            CodePrepTheme {
                Box(
                    modifier = Modifier
                        .width(900.dp)
                        .height(1200.dp)
                ) {
                    CourseListScreenContent(
                        courses = sampleModules(),
                        layout = courseListLayoutFor(screenWidthDp = 900),
                        onCourseClick = {}
                    )
                }
            }
        }

        val container = composeTestRule.onNodeWithTag("course-list-container").assertExists()
        val root = composeTestRule.onNodeWithTag("course-list-root").assertExists()

        val containerBounds = container.fetchSemanticsNode().boundsInRoot
        val rootBounds = root.fetchSemanticsNode().boundsInRoot
        val containerCenter = (containerBounds.left + containerBounds.right) / 2f
        val rootCenter = (rootBounds.left + rootBounds.right) / 2f
        val tolerance = with(composeTestRule.density) { 2.dp.toPx() }

        assertTrue(containerBounds.width < rootBounds.width)
        assertTrue(abs(containerCenter - rootCenter) <= tolerance)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.codeprep.app.ui.course.CourseListScreenTabletLayoutTest`
Expected: FAIL because `CourseListScreenContent`, `sampleModules()`, or the needed test tags do not exist yet.

- [ ] **Step 3: Write minimal implementation**

Expose a focused `CourseListScreenContent` composable for testing, add stable test tags for the root and bounded tablet container, and create a minimal local sample-data helper inside the test file.

- [ ] **Step 4: Run test to verify it passes**

Run: `.\gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.codeprep.app.ui.course.CourseListScreenTabletLayoutTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/androidTest/java/com/codeprep/app/ui/course/CourseListScreenTabletLayoutTest.kt app/src/main/java/com/codeprep/app/ui/course/CourseListScreen.kt
git commit -m "test: cover modules tablet container layout"
```

### Task 3: Implement the centered tablet container and stronger screen spacing

**Files:**
- Modify: `app/src/main/java/com/codeprep/app/ui/course/CourseListScreen.kt`

- [ ] **Step 1: Refactor screen entry to use the layout spec**

Read `LocalConfiguration.current.screenWidthDp`, compute `courseListLayoutFor(...)`, and pass the layout into `CourseListScreenContent(...)`.

- [ ] **Step 2: Add the bounded tablet container**

Update the screen shell so:

- the root still fills the screen and paints `AppBackground`
- phone keeps the current full-width column behavior
- tablet wraps the content in a centered container with a max width near the approved balanced range
- title and subtitle get more top and bottom space on tablet

- [ ] **Step 3: Run the focused tests**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.codeprep.app.ui.course.CourseListLayoutSpecTest"`
Expected: PASS

Run: `.\gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.codeprep.app.ui.course.CourseListScreenTabletLayoutTest`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/codeprep/app/ui/course/CourseListScreen.kt
git commit -m "feat: center modules layout on tablets"
```

### Task 4: Improve card rhythm and metadata wrapping without changing screen structure

**Files:**
- Modify: `app/src/main/java/com/codeprep/app/ui/course/CourseListScreen.kt`

- [ ] **Step 1: Write the smallest failing test only if the current Compose coverage misses a clear regression**

If pill wrapping or card spacing changes expose a new behavior worth pinning down, add the smallest focused assertion before implementation. Do not add speculative tests.

- [ ] **Step 2: Update card internals for tablet quality**

Adjust the module card implementation so it keeps the same information model but improves composition:

- slightly larger card padding on tablet
- stronger spacing between top row, description, pills, progress, and status
- pill layout that can wrap cleanly with longer localized strings
- unchanged locked, perfect, and continue states

- [ ] **Step 3: Run verification**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.codeprep.app.ui.course.CourseListLayoutSpecTest"`
Expected: PASS

Run: `.\gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.codeprep.app.ui.course.CourseListScreenTabletLayoutTest`
Expected: PASS

Run: `.\gradlew.bat compileDebugKotlin`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/codeprep/app/ui/course/CourseListScreen.kt app/src/androidTest/java/com/codeprep/app/ui/course/CourseListScreenTabletLayoutTest.kt
git commit -m "feat: refine modules card rhythm on tablets"
```

### Task 5: Final verification and manual tablet pass

**Files:**
- Verify only

- [ ] **Step 1: Run full targeted verification**

Run: `.\gradlew.bat testDebugUnitTest compileDebugKotlin`
Expected: PASS

Run: `.\gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.codeprep.app.ui.course.CourseListScreenTabletLayoutTest`
Expected: PASS

- [ ] **Step 2: Run a debug build check**

Run: `.\gradlew.bat assembleDebug`
Expected: PASS

- [ ] **Step 3: Manual validation checklist**

- Verify the modules content is visibly narrower than the full tablet width.
- Verify the content column is centered.
- Verify the screen still feels like one vertical reading lane, not a grid.
- Verify pills wrap cleanly with longer text.
- Verify locked cards still read clearly.
- Verify phone widths still look close to the current design.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/codeprep/app/ui/course/CourseListScreen.kt app/src/test/java/com/codeprep/app/ui/course/CourseListLayoutSpecTest.kt app/src/androidTest/java/com/codeprep/app/ui/course/CourseListScreenTabletLayoutTest.kt
git commit -m "feat: finish modules tablet layout pass"
```
