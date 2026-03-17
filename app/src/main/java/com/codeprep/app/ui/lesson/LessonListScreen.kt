package com.codeprep.app.ui.lesson

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.codeprep.app.ui.components.GamifiedButton
import com.codeprep.app.ui.components.LessonPathNode
import com.codeprep.app.ui.components.NodeState
import com.codeprep.app.ui.theme.AppBackground
import com.codeprep.app.ui.theme.Charcoal
import com.codeprep.app.ui.theme.ElectricCyan
import com.codeprep.app.ui.theme.IceWhite
import com.codeprep.app.ui.theme.LockedGrey
import com.codeprep.app.ui.theme.SunYellow
import com.codeprep.app.ui.theme.TextLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlin.math.roundToInt

private enum class TooltipPlacement {
    ABOVE,
    BELOW
}

private data class TooltipLayout(
    val offset: IntOffset,
    val pointerCenterX: Float,
    val rect: Rect,
    val placement: TooltipPlacement,
    val scrollAdjustmentPx: Int
)

private val TooltipWidth = 228.dp
private val TooltipFallbackHeight = 184.dp
private val TooltipPointerHeight = 6.dp
private val TooltipGap = (-8).dp
private val TooltipHorizontalPadding = 16.dp
private val TooltipVerticalPadding = 16.dp

@Composable
fun LessonListScreen(
    onLessonClick: (String) -> Unit,
    viewModel: LessonListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val lessons = state.lessons
    val nodeBounds = remember { mutableStateMapOf<String, Rect>() }
    val density = LocalDensity.current
    val scrollState = rememberScrollState()

    var selectedLessonId by remember { mutableStateOf<String?>(null) }
    var displayedLessonId by remember { mutableStateOf<String?>(null) }
    var tooltipVisible by remember { mutableStateOf(false) }
    var rootSize by remember { mutableStateOf(IntSize.Zero) }
    var tooltipSize by remember { mutableStateOf(IntSize.Zero) }
    var autoScrollingTooltip by remember { mutableStateOf(false) }

    val currentSelectedLessonId by rememberUpdatedState(selectedLessonId)
    val currentAutoScrollingTooltip by rememberUpdatedState(autoScrollingTooltip)

    val displayedLesson = lessons.firstOrNull { it.lesson.lessonId == displayedLessonId }
    val displayedLessonIndex = displayedLessonId?.let { lessonId ->
        lessons.indexOfFirst { it.lesson.lessonId == lessonId }.takeIf { it >= 0 }
    }
    val displayedNodeBounds = displayedLessonId?.let(nodeBounds::get)

    LaunchedEffect(lessons, selectedLessonId) {
        if (selectedLessonId != null && lessons.none { it.lesson.lessonId == selectedLessonId }) {
            selectedLessonId = null
        }
        if (displayedLessonId != null && lessons.none { it.lesson.lessonId == displayedLessonId }) {
            displayedLessonId = null
            tooltipVisible = false
        }
    }

    LaunchedEffect(selectedLessonId, lessons) {
        if (selectedLessonId == null) {
            tooltipVisible = false
            delay(140)
            if (selectedLessonId == null) {
                displayedLessonId = null
                tooltipSize = IntSize.Zero
            }
        } else {
            val targetLessonId = selectedLessonId ?: return@LaunchedEffect
            displayedLessonId = targetLessonId
            tooltipVisible = false

            val lessonIndex = lessons.indexOfFirst { it.lesson.lessonId == targetLessonId }
            if (lessonIndex >= 0) {
                val initialLayout = snapshotFlow {
                    val nodeRect = nodeBounds[targetLessonId]
                    if (selectedLessonId != targetLessonId || nodeRect == null || rootSize == IntSize.Zero) {
                        null
                    } else {
                        computeTooltipLayout(
                            nodeBounds = nodeRect,
                            lessonIndex = lessonIndex,
                            lessonCount = lessons.size,
                            rootSize = rootSize,
                            tooltipSize = tooltipSize,
                            density = density
                        )
                    }
                }.first { it != null }

                val scrollAdjustmentPx = initialLayout?.scrollAdjustmentPx ?: 0
                if (scrollAdjustmentPx > 0 && selectedLessonId == targetLessonId) {
                    autoScrollingTooltip = true
                    try {
                        val targetScroll = (scrollState.value + scrollAdjustmentPx)
                            .coerceAtMost(scrollState.maxValue)
                        scrollState.animateScrollTo(targetScroll)
                    } finally {
                        autoScrollingTooltip = false
                    }
                }
            }

            delay(35)
            if (displayedLessonId == selectedLessonId) {
                tooltipVisible = true
            }
        }
    }

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.value }
            .drop(1)
            .collect {
                if (!currentAutoScrollingTooltip && currentSelectedLessonId != null) {
                    selectedLessonId = null
                }
            }
    }

    val tooltipLayout = remember(
        displayedLessonId,
        displayedLessonIndex,
        displayedNodeBounds,
        rootSize,
        tooltipSize,
        lessons.size,
        density
    ) {
        if (displayedLessonIndex == null || displayedNodeBounds == null || rootSize == IntSize.Zero) {
            null
        } else {
            computeTooltipLayout(
                nodeBounds = displayedNodeBounds,
                lessonIndex = displayedLessonIndex,
                lessonCount = lessons.size,
                rootSize = rootSize,
                tooltipSize = tooltipSize,
                density = density
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .onGloballyPositioned { coordinates ->
                rootSize = coordinates.size
            }
            .pointerInput(selectedLessonId, displayedNodeBounds, tooltipLayout) {
                if (selectedLessonId == null) return@pointerInput

                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Final)
                    val up = waitForUpOrCancellation(pass = PointerEventPass.Final)
                    if (up != null && !up.isConsumed) {
                        val tapPosition = up.position
                        val tappedNode = displayedNodeBounds?.contains(tapPosition) == true
                        val tappedTooltip = tooltipLayout?.rect?.contains(tapPosition) == true
                        if (!tappedNode && !tappedTooltip) {
                            selectedLessonId = null
                        }
                    }
                }
            }
    ) {
        if (lessons.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No lessons available",
                    color = TextLight,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
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
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "${state.lessons.count { it.isCompleted }}/${state.lessons.size} cleared",
                                style = MaterialTheme.typography.labelLarge,
                                color = ElectricCyan
                            )
                            Text(
                                text = "${state.lessons.count { it.isPerfect }} stars",
                                style = MaterialTheme.typography.labelLarge,
                                color = SunYellow
                            )
                        }
                    }
                }

                state.lessons.forEachIndexed { index, lessonItem ->
                    val offsetX = nodeOffsetForIndex(index)
                    val nextOffsetX = if (index < lessons.lastIndex) nodeOffsetForIndex(index + 1) else 0.dp

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(164.dp)
                    ) {
                        if (index < lessons.lastIndex) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .requiredHeight(176.dp)
                                    .align(Alignment.BottomCenter)
                            ) {
                                val centerX = size.width / 2
                                val startX = centerX + offsetX.toPx()
                                val endX = centerX + nextOffsetX.toPx()
                                val startY = 16.dp.toPx()
                                val endY = size.height
                                val path = Path().apply {
                                    moveTo(startX, startY)
                                    cubicTo(
                                        startX, startY + size.height * 0.35f,
                                        endX, endY - size.height * 0.35f,
                                        endX, endY
                                    )
                                }

                                drawPath(
                                    path = path,
                                    color = if (lessonItem.isCompleted) ElectricCyan else LockedGrey,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                        }

                        LessonPathNode(
                            state = when {
                                lessonItem.isPerfect -> NodeState.PERFECT
                                lessonItem.isCompleted -> NodeState.COMPLETED
                                lessonItem.isUnlocked && !lessonItem.isBlockedByHearts -> NodeState.ACTIVE
                                else -> NodeState.LOCKED
                            },
                            keepPressed = (selectedLessonId == lessonItem.lesson.lessonId) ||
                                (tooltipVisible && displayedLessonId == lessonItem.lesson.lessonId),
                            onClick = {
                                if (lessonItem.canOpen) {
                                    selectedLessonId = if (selectedLessonId == lessonItem.lesson.lessonId) {
                                        null
                                    } else {
                                        lessonItem.lesson.lessonId
                                    }
                                }
                            },
                            onCirclePositioned = { bounds ->
                                nodeBounds[lessonItem.lesson.lessonId] = bounds
                            },
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(x = offsetX)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        if (displayedLesson != null && tooltipLayout != null) {
            LessonNodeTooltip(
                lessonItem = displayedLesson,
                visible = tooltipVisible,
                placement = tooltipLayout.placement,
                pointerCenterX = tooltipLayout.pointerCenterX,
                onStartClick = { onLessonClick(displayedLesson.lesson.lessonId) },
                modifier = Modifier
                    .offset { tooltipLayout.offset }
                    .onGloballyPositioned { coordinates ->
                        tooltipSize = coordinates.size
                    }
            )
        }
    }
}

@Composable
private fun LessonNodeTooltip(
    lessonItem: LessonListItemUi,
    visible: Boolean,
    placement: TooltipPlacement,
    pointerCenterX: Float,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.08f,
        animationSpec = if (visible) {
            spring(dampingRatio = 0.62f, stiffness = 520f)
        } else {
            tween(durationMillis = 120, easing = FastOutSlowInEasing)
        },
        label = "lesson_tooltip_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = if (visible) {
            tween(durationMillis = 170, easing = FastOutSlowInEasing)
        } else {
            tween(durationMillis = 90, easing = FastOutSlowInEasing)
        },
        label = "lesson_tooltip_alpha"
    )

    Column(
        modifier = modifier
            .width(228.dp)
            .graphicsLayer {
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
                transformOrigin = when (placement) {
                    TooltipPlacement.BELOW -> TransformOrigin(0.5f, 0f)
                    TooltipPlacement.ABOVE -> TransformOrigin(0.5f, 1f)
                }
            }
    ) {
        if (placement == TooltipPlacement.BELOW) {
            TooltipPointer(
                placement = placement,
                pointerCenterX = pointerCenterX,
                color = Charcoal
            )
        }

        Surface(
            color = Charcoal,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 18.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = lessonItem.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = IceWhite,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusPill(
                        text = "${lessonItem.lesson.xpReward} XP",
                        accent = ElectricCyan
                    )
                    StatusPill(
                        text = "${lessonItem.lesson.questionCount} questions",
                        accent = if (lessonItem.isPerfect) SunYellow else LockedGrey
                    )
                }
                Text(
                    text = lessonItem.progressLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (lessonItem.isActive) ElectricCyan else TextLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (lessonItem.isCompleted && !lessonItem.isPerfect) {
                    Text(
                        text = "Retry for the star and bonus XP.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SunYellow,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                GamifiedButton(
                    text = "Start",
                    onClick = onStartClick,
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = ElectricCyan,
                    textColor = Charcoal
                )
            }
        }

        if (placement == TooltipPlacement.ABOVE) {
            TooltipPointer(
                placement = placement,
                pointerCenterX = pointerCenterX,
                color = Charcoal
            )
        }
    }
}

@Composable
private fun TooltipPointer(
    placement: TooltipPlacement,
    pointerCenterX: Float,
    color: Color
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(TooltipPointerHeight)
    ) {
        val triangleHalfWidth = (TooltipPointerHeight * 1.1f).toPx()
        val clampedCenter = pointerCenterX.coerceIn(
            minimumValue = triangleHalfWidth + 8.dp.toPx(),
            maximumValue = size.width - triangleHalfWidth - 8.dp.toPx()
        )

        val path = Path().apply {
            if (placement == TooltipPlacement.BELOW) {
                moveTo(clampedCenter, 0f)
                lineTo(clampedCenter - triangleHalfWidth, size.height)
                lineTo(clampedCenter + triangleHalfWidth, size.height)
            } else {
                moveTo(clampedCenter, size.height)
                lineTo(clampedCenter - triangleHalfWidth, 0f)
                lineTo(clampedCenter + triangleHalfWidth, 0f)
            }
            close()
        }

        drawPath(path = path, color = color)
    }
}

private fun nodeOffsetForIndex(index: Int) = when (index % 4) {
    0 -> 0.dp
    1 -> (-56).dp
    2 -> 0.dp
    else -> 56.dp
}

private fun computeTooltipLayout(
    nodeBounds: Rect,
    lessonIndex: Int,
    lessonCount: Int,
    rootSize: IntSize,
    tooltipSize: IntSize,
    density: androidx.compose.ui.unit.Density
): TooltipLayout {
    val horizontalPadding = with(density) { TooltipHorizontalPadding.toPx() }
    val verticalPadding = with(density) { TooltipVerticalPadding.toPx() }
    val gap = with(density) { TooltipGap.toPx() }
    val pointerHeight = with(density) { TooltipPointerHeight.toPx() }
    val fallbackWidth = with(density) { TooltipWidth.toPx() }.roundToInt()
    val fallbackHeight = with(density) { TooltipFallbackHeight.toPx() }.roundToInt()

    val bodyWidth = if (tooltipSize.width > 0) tooltipSize.width else fallbackWidth
    val bodyHeight = if (tooltipSize.height > 0) tooltipSize.height else fallbackHeight
    val totalTooltipHeight = bodyHeight + pointerHeight.roundToInt()
    val placement = if (lessonIndex >= (lessonCount - 2).coerceAtLeast(0)) {
        TooltipPlacement.ABOVE
    } else {
        TooltipPlacement.BELOW
    }

    val desiredX = (nodeBounds.center.x - bodyWidth / 2f).roundToInt()
    val maxX = (rootSize.width - bodyWidth - horizontalPadding).roundToInt().coerceAtLeast(horizontalPadding.roundToInt())
    val clampedX = desiredX.coerceIn(horizontalPadding.roundToInt(), maxX)

    val scrollAdjustmentPx = if (placement == TooltipPlacement.BELOW) {
        (
            nodeBounds.bottom + gap + totalTooltipHeight + verticalPadding - rootSize.height
        ).coerceAtLeast(0f).roundToInt()
    } else {
        0
    }
    val desiredY = when (placement) {
        TooltipPlacement.BELOW -> (nodeBounds.bottom + gap).roundToInt()
        TooltipPlacement.ABOVE -> (nodeBounds.top - totalTooltipHeight - gap).roundToInt()
    }
    val maxY = (rootSize.height - totalTooltipHeight - verticalPadding).roundToInt().coerceAtLeast(verticalPadding.roundToInt())
    val clampedY = desiredY.coerceIn(verticalPadding.roundToInt(), maxY)

    val pointerCenterX = (nodeBounds.center.x - clampedX)
        .coerceIn(24f, bodyWidth - 24f)

    return TooltipLayout(
        offset = IntOffset(clampedX, clampedY),
        pointerCenterX = pointerCenterX,
        rect = Rect(
            offset = Offset(clampedX.toFloat(), clampedY.toFloat()),
            size = Size(bodyWidth.toFloat(), totalTooltipHeight.toFloat())
        ),
        placement = placement,
        scrollAdjustmentPx = scrollAdjustmentPx
    )
}

@Composable
private fun StatusPill(
    text: String,
    accent: Color
) {
    Box(
        modifier = Modifier
            .background(accent.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = accent
        )
    }
}

