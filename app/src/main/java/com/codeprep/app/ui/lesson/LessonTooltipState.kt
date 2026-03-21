package com.codeprep.app.ui.lesson

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first

internal class LessonTooltipState {
    val nodeBounds = mutableStateMapOf<String, Rect>()

    var selectedLessonId by mutableStateOf<String?>(null)
        private set

    var displayedLessonId by mutableStateOf<String?>(null)
        private set

    var tooltipVisible by mutableStateOf(false)
        private set

    var rootSize by mutableStateOf(IntSize.Zero)
        private set

    var tooltipSize by mutableStateOf(IntSize.Zero)
        private set

    private var autoScrollingTooltip by mutableStateOf(false)

    val displayedNodeBounds: Rect?
        get() = displayedLessonId?.let(nodeBounds::get)

    val isAutoScrollingTooltip: Boolean
        get() = autoScrollingTooltip

    fun onLessonTapped(lessonItem: LessonListItemUi) {
        if (!lessonItem.canOpen) return

        val lessonId = lessonItem.lesson.lessonId
        selectedLessonId = if (selectedLessonId == lessonId) null else lessonId
    }

    fun onNodePositioned(lessonId: String, bounds: Rect) {
        nodeBounds[lessonId] = bounds
    }

    fun onRootPositioned(size: IntSize) {
        rootSize = size
    }

    fun onTooltipPositioned(size: IntSize) {
        tooltipSize = size
    }

    fun isNodePressed(lessonId: String): Boolean {
        return selectedLessonId == lessonId || (tooltipVisible && displayedLessonId == lessonId)
    }

    fun dismiss() {
        selectedLessonId = null
    }

    internal fun clearMissingSelection() {
        selectedLessonId = null
    }

    internal fun clearMissingDisplayedLesson() {
        displayedLessonId = null
        tooltipVisible = false
    }

    internal fun hideTooltip() {
        tooltipVisible = false
    }

    internal fun clearDisplayedTooltip() {
        displayedLessonId = null
        tooltipSize = IntSize.Zero
    }

    internal fun prepareTooltip(lessonId: String) {
        displayedLessonId = lessonId
        tooltipVisible = false
    }

    internal fun showTooltip() {
        tooltipVisible = true
    }

    internal fun beginAutoScroll() {
        autoScrollingTooltip = true
    }

    internal fun endAutoScroll() {
        autoScrollingTooltip = false
    }
}

@Composable
internal fun rememberLessonTooltipState(
    lessons: List<LessonListItemUi>,
    scrollState: ScrollState,
    isTabletLayout: Boolean,
    density: Density
): LessonTooltipState {
    val tooltipState = remember { LessonTooltipState() }
    val currentSelectedLessonId by rememberUpdatedState(tooltipState.selectedLessonId)
    val currentAutoScrollingTooltip by rememberUpdatedState(tooltipState.isAutoScrollingTooltip)

    androidx.compose.runtime.LaunchedEffect(lessons, tooltipState.selectedLessonId) {
        if (
            tooltipState.selectedLessonId != null &&
            lessons.none { it.lesson.lessonId == tooltipState.selectedLessonId }
        ) {
            tooltipState.clearMissingSelection()
        }
        if (
            tooltipState.displayedLessonId != null &&
            lessons.none { it.lesson.lessonId == tooltipState.displayedLessonId }
        ) {
            tooltipState.clearMissingDisplayedLesson()
        }
    }

    androidx.compose.runtime.LaunchedEffect(tooltipState.selectedLessonId, lessons) {
        val selectedLessonId = tooltipState.selectedLessonId
        if (selectedLessonId == null) {
            tooltipState.hideTooltip()
            delay(140)
            if (tooltipState.selectedLessonId == null) {
                tooltipState.clearDisplayedTooltip()
            }
            return@LaunchedEffect
        }

        tooltipState.prepareTooltip(selectedLessonId)

        val lessonIndex = lessons.indexOfFirst { it.lesson.lessonId == selectedLessonId }
        if (lessonIndex >= 0) {
            val initialLayout = snapshotFlow {
                val nodeRect = tooltipState.nodeBounds[selectedLessonId]
                if (
                    tooltipState.selectedLessonId != selectedLessonId ||
                    nodeRect == null ||
                    tooltipState.rootSize == IntSize.Zero
                ) {
                    null
                } else {
                    computeTooltipLayout(
                        nodeBounds = nodeRect,
                        lessonIndex = lessonIndex,
                        lessonCount = lessons.size,
                        rootSize = tooltipState.rootSize,
                        tooltipSize = tooltipState.tooltipSize,
                        isTabletLayout = isTabletLayout,
                        density = density
                    )
                }
            }.first { it != null }

            val scrollAdjustmentPx = initialLayout?.scrollAdjustmentPx ?: 0
            if (scrollAdjustmentPx > 0 && tooltipState.selectedLessonId == selectedLessonId) {
                tooltipState.beginAutoScroll()
                try {
                    val targetScroll = (scrollState.value + scrollAdjustmentPx)
                        .coerceAtMost(scrollState.maxValue)
                    scrollState.animateScrollTo(targetScroll)
                } finally {
                    tooltipState.endAutoScroll()
                }
            }
        }

        delay(35)
        if (tooltipState.displayedLessonId == tooltipState.selectedLessonId) {
            tooltipState.showTooltip()
        }
    }

    androidx.compose.runtime.LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.value }
            .drop(1)
            .collect {
                if (!currentAutoScrollingTooltip && currentSelectedLessonId != null) {
                    tooltipState.dismiss()
                }
            }
    }

    return tooltipState
}
