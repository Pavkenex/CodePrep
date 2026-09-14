package com.codeprep.app.ui.lesson

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.codeprep.app.R
import com.codeprep.app.ui.components.GamifiedButton
import com.codeprep.app.ui.localization.localizedPluralStringResource
import com.codeprep.app.ui.localization.localizedStringResource
import com.codeprep.app.ui.theme.Charcoal
import com.codeprep.app.ui.theme.ElectricCyan
import com.codeprep.app.ui.theme.IceWhite
import com.codeprep.app.ui.theme.LockedGrey
import com.codeprep.app.ui.theme.SunYellow
import com.codeprep.app.ui.theme.TextLight
import kotlin.math.roundToInt

internal enum class TooltipPlacement {
    ABOVE,
    BELOW,
    LEFT,
    RIGHT
}

internal data class TooltipLayout(
    val offset: IntOffset,
    val pointerCenterX: Float,
    val rect: Rect,
    val placement: TooltipPlacement,
    val scrollAdjustmentPx: Int
)

private val TooltipWidth = 228.dp
private val TooltipFallbackHeight = 184.dp
private val TooltipPointerHeight = 5.dp
private val TooltipGap = (-53).dp
private val TooltipHorizontalPadding = 16.dp
private val TooltipVerticalPadding = 16.dp
private val TabletSideTooltipGap = 16.dp
private val TabletSideTooltipVerticalLift = 44.dp
private val TabletSidePressedNodeCenterOffset = (-53).dp

@Composable
internal fun rememberTooltipLayout(
    displayedLessonIndex: Int?,
    displayedNodeBounds: Rect?,
    rootSize: IntSize,
    tooltipSize: IntSize,
    lessonCount: Int,
    isTabletLayout: Boolean,
    density: Density = LocalDensity.current
): TooltipLayout? = remember(
    displayedLessonIndex,
    displayedNodeBounds,
    rootSize,
    tooltipSize,
    lessonCount,
    isTabletLayout,
    density
) {
    if (displayedLessonIndex == null || displayedNodeBounds == null || rootSize == IntSize.Zero) {
        null
    } else {
        computeTooltipLayout(
            nodeBounds = displayedNodeBounds,
            lessonIndex = displayedLessonIndex,
            lessonCount = lessonCount,
            rootSize = rootSize,
            tooltipSize = tooltipSize,
            isTabletLayout = isTabletLayout,
            density = density
        )
    }
}

@Composable
internal fun LessonNodeTooltip(
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
        modifier = modifier.graphicsLayer {
            this.alpha = alpha
            scaleX = scale
            scaleY = scale
            transformOrigin = when (placement) {
                TooltipPlacement.BELOW -> TransformOrigin(0.5f, 0f)
                TooltipPlacement.ABOVE -> TransformOrigin(0.5f, 1f)
                TooltipPlacement.LEFT -> TransformOrigin(1f, 0.5f)
                TooltipPlacement.RIGHT -> TransformOrigin(0f, 0.5f)
            }
        }
    ) {
        when (placement) {
            TooltipPlacement.BELOW,
            TooltipPlacement.ABOVE -> VerticalTooltipLayout(
                lessonItem = lessonItem,
                placement = placement,
                pointerCenterX = pointerCenterX,
                onStartClick = onStartClick
            )

            TooltipPlacement.LEFT,
            TooltipPlacement.RIGHT -> HorizontalTooltipLayout(
                lessonItem = lessonItem,
                placement = placement,
                pointerCenterY = pointerCenterX,
                onStartClick = onStartClick
            )
        }
    }
}

@Composable
private fun VerticalTooltipLayout(
    lessonItem: LessonListItemUi,
    placement: TooltipPlacement,
    pointerCenterX: Float,
    onStartClick: () -> Unit
) {
    Column(modifier = Modifier.width(TooltipWidth)) {
        if (placement == TooltipPlacement.BELOW) {
            TooltipPointer(
                placement = placement,
                pointerCenter = pointerCenterX,
                color = Charcoal
            )
        }

        TooltipSurface(
            lessonItem = lessonItem,
            onStartClick = onStartClick,
            modifier = Modifier.fillMaxWidth()
        )

        if (placement == TooltipPlacement.ABOVE) {
            TooltipPointer(
                placement = placement,
                pointerCenter = pointerCenterX,
                color = Charcoal
            )
        }
    }
}

@Composable
private fun HorizontalTooltipLayout(
    lessonItem: LessonListItemUi,
    placement: TooltipPlacement,
    pointerCenterY: Float,
    onStartClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .width(TooltipWidth + TooltipPointerHeight)
            .height(IntrinsicSize.Min)
    ) {
        if (placement == TooltipPlacement.RIGHT) {
            TooltipPointer(
                placement = placement,
                pointerCenter = pointerCenterY,
                color = Charcoal
            )
        }

        TooltipSurface(
            lessonItem = lessonItem,
            onStartClick = onStartClick,
            modifier = Modifier
                .width(TooltipWidth)
                .fillMaxHeight()
        )

        if (placement == TooltipPlacement.LEFT) {
            TooltipPointer(
                placement = placement,
                pointerCenter = pointerCenterY,
                color = Charcoal
            )
        }
    }
}

@Composable
private fun TooltipSurface(
    lessonItem: LessonListItemUi,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Charcoal,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 18.dp,
        modifier = modifier
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
                    text = localizedStringResource(
                        R.string.common_xp_amount,
                        lessonItem.lesson.xpReward
                    ),
                    accent = ElectricCyan
                )
                StatusPill(
                    text = localizedPluralStringResource(
                        R.plurals.lesson_label_questions_count,
                        lessonItem.lesson.questionCount,
                        lessonItem.lesson.questionCount
                    ),
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
                    text = localizedStringResource(R.string.lesson_tooltip_retry),
                    style = MaterialTheme.typography.bodySmall,
                    color = SunYellow,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            GamifiedButton(
                text = localizedStringResource(R.string.common_start),
                onClick = onStartClick,
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = ElectricCyan,
                textColor = Charcoal
            )
        }
    }
}

@Composable
private fun TooltipPointer(
    placement: TooltipPlacement,
    pointerCenter: Float,
    color: Color
) {
    Canvas(
        modifier = Modifier
            .then(
                when (placement) {
                    TooltipPlacement.BELOW,
                    TooltipPlacement.ABOVE -> Modifier
                        .fillMaxWidth()
                        .height(TooltipPointerHeight)

                    TooltipPlacement.LEFT,
                    TooltipPlacement.RIGHT -> Modifier
                        .width(TooltipPointerHeight)
                        .fillMaxHeight()
                }
            )
    ) {
        val triangleHalfWidth = (TooltipPointerHeight * 1.1f).toPx()
        val clampedCenter = pointerCenter.coerceIn(
            minimumValue = triangleHalfWidth + 8.dp.toPx(),
            maximumValue = when (placement) {
                TooltipPlacement.BELOW,
                TooltipPlacement.ABOVE -> size.width - triangleHalfWidth - 8.dp.toPx()
                TooltipPlacement.LEFT,
                TooltipPlacement.RIGHT -> size.height - triangleHalfWidth - 8.dp.toPx()
            }
        )

        val path = Path().apply {
            tooltipPointerTrianglePoints(
                placement = placement,
                pointerCenter = clampedCenter,
                canvasSize = size,
                triangleHalfWidth = triangleHalfWidth
            ).forEachIndexed { index, point ->
                if (index == 0) {
                    moveTo(point.x, point.y)
                } else {
                    lineTo(point.x, point.y)
                }
            }
            close()
        }

        drawPath(path = path, color = color)
    }
}

internal fun tooltipPointerTrianglePoints(
    placement: TooltipPlacement,
    pointerCenter: Float,
    canvasSize: Size,
    triangleHalfWidth: Float
): List<Offset> {
    return when (placement) {
        TooltipPlacement.BELOW -> listOf(
            Offset(pointerCenter, 0f),
            Offset(pointerCenter - triangleHalfWidth, canvasSize.height),
            Offset(pointerCenter + triangleHalfWidth, canvasSize.height)
        )

        TooltipPlacement.ABOVE -> listOf(
            Offset(pointerCenter, canvasSize.height),
            Offset(pointerCenter - triangleHalfWidth, 0f),
            Offset(pointerCenter + triangleHalfWidth, 0f)
        )

        TooltipPlacement.LEFT -> listOf(
            Offset(canvasSize.width, pointerCenter),
            Offset(0f, pointerCenter - triangleHalfWidth),
            Offset(0f, pointerCenter + triangleHalfWidth)
        )

        TooltipPlacement.RIGHT -> listOf(
            Offset(0f, pointerCenter),
            Offset(canvasSize.width, pointerCenter - triangleHalfWidth),
            Offset(canvasSize.width, pointerCenter + triangleHalfWidth)
        )
    }
}

internal fun computeTooltipLayout(
    nodeBounds: Rect,
    lessonIndex: Int,
    lessonCount: Int,
    rootSize: IntSize,
    tooltipSize: IntSize,
    isTabletLayout: Boolean,
    density: Density
): TooltipLayout {
    val horizontalPadding = with(density) { TooltipHorizontalPadding.toPx() }
    val verticalPadding = with(density) { TooltipVerticalPadding.toPx() }
    val gap = with(density) { TooltipGap.toPx() }
    val pointerHeight = with(density) { TooltipPointerHeight.toPx() }
    val tabletSideGap = with(density) { TabletSideTooltipGap.toPx() }
    val tabletSideVerticalLift = with(density) { TabletSideTooltipVerticalLift.toPx() }
    val tabletSidePressedNodeCenterOffset = with(density) { TabletSidePressedNodeCenterOffset.toPx() }
    val fallbackWidth = with(density) { TooltipWidth.toPx() }.roundToInt()
    val fallbackHeight = with(density) { TooltipFallbackHeight.toPx() }.roundToInt()

    val placement = tooltipPlacementFor(
        lessonIndex = lessonIndex,
        lessonCount = lessonCount,
        isTabletLayout = isTabletLayout
    )
    val fallbackTotalWidth = when (placement) {
        TooltipPlacement.LEFT,
        TooltipPlacement.RIGHT -> fallbackWidth + pointerHeight.roundToInt()
        TooltipPlacement.ABOVE,
        TooltipPlacement.BELOW -> fallbackWidth
    }
    val fallbackTotalHeight = when (placement) {
        TooltipPlacement.LEFT,
        TooltipPlacement.RIGHT -> fallbackHeight
        TooltipPlacement.ABOVE,
        TooltipPlacement.BELOW -> fallbackHeight + pointerHeight.roundToInt()
    }
    val totalTooltipWidth = if (tooltipSize.width > 0) tooltipSize.width else fallbackTotalWidth
    val totalTooltipHeight = if (tooltipSize.height > 0) tooltipSize.height else fallbackTotalHeight

    val desiredX = when (placement) {
        TooltipPlacement.BELOW,
        TooltipPlacement.ABOVE -> (nodeBounds.center.x - totalTooltipWidth / 2f).roundToInt()
        TooltipPlacement.LEFT -> (nodeBounds.left - totalTooltipWidth - tabletSideGap).roundToInt()
        TooltipPlacement.RIGHT -> (nodeBounds.right + tabletSideGap).roundToInt()
    }
    val maxX = (rootSize.width - totalTooltipWidth - horizontalPadding)
        .roundToInt()
        .coerceAtLeast(horizontalPadding.roundToInt())
    val clampedX = desiredX.coerceIn(horizontalPadding.roundToInt(), maxX)

    val scrollAdjustmentPx = when (placement) {
        TooltipPlacement.BELOW -> (
            nodeBounds.bottom + gap + totalTooltipHeight + verticalPadding - rootSize.height
            ).coerceAtLeast(0f).roundToInt()

        else -> 0
    }
    val desiredY = when (placement) {
        TooltipPlacement.BELOW -> (nodeBounds.bottom + gap).roundToInt()
        TooltipPlacement.ABOVE -> (nodeBounds.top - totalTooltipHeight - gap).roundToInt()
        TooltipPlacement.LEFT,
        TooltipPlacement.RIGHT -> (
            nodeBounds.center.y - totalTooltipHeight / 2f - tabletSideVerticalLift
            ).roundToInt()
    }
    val maxY = (rootSize.height - totalTooltipHeight - verticalPadding)
        .roundToInt()
        .coerceAtLeast(verticalPadding.roundToInt())
    val clampedY = desiredY.coerceIn(verticalPadding.roundToInt(), maxY)

    val pointerCenter = when (placement) {
        TooltipPlacement.BELOW,
        TooltipPlacement.ABOVE -> (nodeBounds.center.x - clampedX)
            .coerceIn(24f, totalTooltipWidth - 24f)

        TooltipPlacement.LEFT,
        TooltipPlacement.RIGHT -> (nodeBounds.center.y - clampedY + tabletSidePressedNodeCenterOffset)
            .coerceIn(24f, totalTooltipHeight - 24f)
    }

    return TooltipLayout(
        offset = IntOffset(clampedX, clampedY),
        pointerCenterX = pointerCenter,
        rect = Rect(
            offset = Offset(clampedX.toFloat(), clampedY.toFloat()),
            size = Size(totalTooltipWidth.toFloat(), totalTooltipHeight.toFloat())
        ),
        placement = placement,
        scrollAdjustmentPx = scrollAdjustmentPx
    )
}

internal fun tooltipPlacementFor(
    lessonIndex: Int,
    lessonCount: Int,
    isTabletLayout: Boolean
): TooltipPlacement {
    if (!isTabletLayout || lessonCount <= 0 || lessonIndex <= 0) {
        return TooltipPlacement.BELOW
    }

    return if (lessonIndex % 2 == 1) {
        TooltipPlacement.LEFT
    } else {
        TooltipPlacement.RIGHT
    }
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
