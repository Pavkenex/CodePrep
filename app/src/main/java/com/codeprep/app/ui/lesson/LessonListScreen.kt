package com.codeprep.app.ui.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.codeprep.app.R
import com.codeprep.app.ui.components.LessonPathNode
import com.codeprep.app.ui.components.NodeState
import com.codeprep.app.ui.localization.localizedStringResource
import com.codeprep.app.ui.theme.AppBackground
import com.codeprep.app.ui.theme.Charcoal
import com.codeprep.app.ui.theme.ElectricCyan
import com.codeprep.app.ui.theme.IceWhite
import com.codeprep.app.ui.theme.SunYellow
import com.codeprep.app.ui.theme.TextLight

@Composable
fun LessonListScreen(
    onLessonClick: (String) -> Unit,
    viewModel: LessonListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LessonListContent(
        state = state,
        onLessonClick = onLessonClick
    )
}

@Composable
private fun LessonListContent(
    state: LessonListUiState,
    onLessonClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lessons = state.lessons
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val scrollState = rememberScrollState()
    val isTabletLayout = configuration.screenWidthDp >= 600
    val tooltipState = rememberLessonTooltipState(
        lessons = lessons,
        scrollState = scrollState,
        isTabletLayout = isTabletLayout,
        density = density
    )

    val displayedLesson = lessons.firstOrNull { it.lesson.lessonId == tooltipState.displayedLessonId }
    val displayedLessonIndex = tooltipState.displayedLessonId?.let { lessonId ->
        lessons.indexOfFirst { it.lesson.lessonId == lessonId }.takeIf { it >= 0 }
    }
    val tooltipLayout = rememberTooltipLayout(
        displayedLessonIndex = displayedLessonIndex,
        displayedNodeBounds = tooltipState.displayedNodeBounds,
        rootSize = tooltipState.rootSize,
        tooltipSize = tooltipState.tooltipSize,
        lessonCount = lessons.size,
        isTabletLayout = isTabletLayout,
        density = density
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .onGloballyPositioned { coordinates ->
                tooltipState.onRootPositioned(coordinates.size)
            }
            .dismissTooltipOnOutsideTap(
                selectedLessonId = tooltipState.selectedLessonId,
                displayedNodeBounds = tooltipState.displayedNodeBounds,
                tooltipLayout = tooltipLayout,
                onDismiss = tooltipState::dismiss
            )
    ) {
        if (lessons.isEmpty()) {
            EmptyLessonState()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                LessonModuleHeader(state = state)
                LessonPathList(
                    lessons = lessons,
                    tooltipState = tooltipState
                )
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        if (displayedLesson != null && tooltipLayout != null) {
            LessonNodeTooltip(
                lessonItem = displayedLesson,
                visible = tooltipState.tooltipVisible,
                placement = tooltipLayout.placement,
                pointerCenterX = tooltipLayout.pointerCenterX,
                onStartClick = { onLessonClick(displayedLesson.lesson.lessonId) },
                modifier = Modifier
                    .offset { tooltipLayout.offset }
                    .onGloballyPositioned { coordinates ->
                        tooltipState.onTooltipPositioned(coordinates.size)
                    }
            )
        }
    }
}

@Composable
private fun EmptyLessonState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = localizedStringResource(R.string.lesson_list_empty),
            color = TextLight,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun LessonModuleHeader(state: LessonListUiState) {
    Surface(
        color = Charcoal,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = state.moduleTitle,
                style = MaterialTheme.typography.headlineSmall,
                color = IceWhite
            )
            if (state.moduleDescription.isNotBlank()) {
                Text(
                    text = state.moduleDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextLight
                )
            }
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = localizedStringResource(
                        R.string.lesson_list_progress_cleared,
                        state.lessons.count { it.isCompleted },
                        state.lessons.size
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = ElectricCyan
                )
                Text(
                    text = localizedStringResource(
                        R.string.lesson_list_stars,
                        state.lessons.count { it.isPerfect }
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = SunYellow
                )
            }
        }
    }
}

@Composable
private fun LessonPathList(
    lessons: List<LessonListItemUi>,
    tooltipState: LessonTooltipState
) {
    lessons.forEachIndexed { index, lessonItem ->
        val offsetX = nodeOffsetForIndex(index)
        val nextOffsetX = if (index < lessons.lastIndex) nodeOffsetForIndex(index + 1) else 0.dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(164.dp)
        ) {
            if (index < lessons.lastIndex) {
                LessonConnectorPath(
                    startOffsetX = offsetX,
                    endOffsetX = nextOffsetX,
                    isCompleted = lessonItem.isCompleted,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            LessonPathNode(
                state = lessonNodeState(lessonItem),
                keepPressed = tooltipState.isNodePressed(lessonItem.lesson.lessonId),
                onClick = { tooltipState.onLessonTapped(lessonItem) },
                onCirclePositioned = { bounds ->
                    tooltipState.onNodePositioned(lessonItem.lesson.lessonId, bounds)
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = offsetX)
            )
        }
    }
}

private fun lessonNodeState(lessonItem: LessonListItemUi): NodeState = when {
    lessonItem.isPerfect -> NodeState.PERFECT
    lessonItem.isCompleted -> NodeState.COMPLETED
    lessonItem.isUnlocked && !lessonItem.isBlockedByHearts -> NodeState.ACTIVE
    else -> NodeState.LOCKED
}

private fun Modifier.dismissTooltipOnOutsideTap(
    selectedLessonId: String?,
    displayedNodeBounds: Rect?,
    tooltipLayout: TooltipLayout?,
    onDismiss: () -> Unit
): Modifier = pointerInput(selectedLessonId, displayedNodeBounds, tooltipLayout) {
    if (selectedLessonId == null) return@pointerInput

    awaitEachGesture {
        awaitFirstDown(pass = PointerEventPass.Final)
        val up = waitForUpOrCancellation(pass = PointerEventPass.Final)
        if (up != null && !up.isConsumed) {
            val tapPosition = up.position
            val tappedNode = displayedNodeBounds?.contains(tapPosition) == true
            val tappedTooltip = tooltipLayout?.rect?.contains(tapPosition) == true
            if (!tappedNode && !tappedTooltip) {
                onDismiss()
            }
        }
    }
}
