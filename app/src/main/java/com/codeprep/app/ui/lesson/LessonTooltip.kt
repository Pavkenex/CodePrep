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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.codeprep.app.ui.components.GamifiedButton
import com.codeprep.app.ui.theme.Charcoal
import com.codeprep.app.ui.theme.ElectricCyan
import com.codeprep.app.ui.theme.IceWhite
import com.codeprep.app.ui.theme.LockedGrey
import com.codeprep.app.ui.theme.SunYellow
import com.codeprep.app.ui.theme.TextLight
import kotlin.math.roundToInt

internal enum class TooltipPlacement {
    ABOVE,
    BELOW
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

@Composable
internal fun rememberTooltipLayout(
    displayedLessonIndex: Int?,
    displayedNodeBounds: Rect?,
    rootSize: IntSize,
    tooltipSize: IntSize,
    lessonCount: Int,
    density: Density = LocalDensity.current
): TooltipLayout? = remember(
    displayedLessonIndex,
    displayedNodeBounds,
    rootSize,
    tooltipSize,
    lessonCount,
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
        modifier = modifier
            .width(TooltipWidth)
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

internal fun computeTooltipLayout(
    nodeBounds: Rect,
    lessonIndex: Int,
    lessonCount: Int,
    rootSize: IntSize,
    tooltipSize: IntSize,
    density: Density
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
    val maxX = (rootSize.width - bodyWidth - horizontalPadding)
        .roundToInt()
        .coerceAtLeast(horizontalPadding.roundToInt())
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
    val maxY = (rootSize.height - totalTooltipHeight - verticalPadding)
        .roundToInt()
        .coerceAtLeast(verticalPadding.roundToInt())
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

