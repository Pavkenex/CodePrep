package com.codeprep.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.codeprep.app.ui.theme.Charcoal
import com.codeprep.app.ui.theme.ElectricCyan
import com.codeprep.app.ui.theme.LeafGreen
import com.codeprep.app.ui.theme.LeafGreenDark
import com.codeprep.app.ui.theme.LockedGrey
import com.codeprep.app.ui.theme.LockedGreyDark
import com.codeprep.app.ui.theme.SkyBlueDark
import com.codeprep.app.ui.theme.SunYellow
import com.codeprep.app.ui.theme.SunYellowDark
import com.codeprep.app.ui.theme.White

enum class NodeState {
    LOCKED,
    ACTIVE,
    COMPLETED,
    PERFECT
}

private data class NodePalette(
    val faceTop: Color,
    val faceBottom: Color,
    val baseTop: Color,
    val baseBottom: Color,
    val rim: Color,
    val highlight: Color,
    val icon: Color,
    val shadow: Color
)

@Composable
fun LessonPathNode(
    state: NodeState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onCirclePositioned: (Rect) -> Unit = {},
    keepPressed: Boolean = false,
    size: Dp = 72.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val palette = remember(state) { paletteFor(state) }

    val puckDepth = 4.dp
    val faceInset = 1.dp
    val pressTravel = puckDepth
    val showPressedState = isPressed || keepPressed
    val faceSize = size - faceInset * 2
    val faceOffset by animateDpAsState(
        targetValue = if (showPressedState) pressTravel else 0.dp,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 950f),
        label = "lesson_node_face_offset"
    )

    Box(
        modifier = modifier
            .width(size)
            .height(size + puckDepth)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = state != NodeState.LOCKED,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(faceSize)
                .onGloballyPositioned { coordinates ->
                    onCirclePositioned(coordinates.boundsInRoot())
                }
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = puckDepth)
                .size(size)
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(palette.baseTop, palette.baseBottom)
                    )
                )
                .border(width = 1.dp, color = palette.rim.copy(alpha = 0.12f), shape = CircleShape)
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = faceOffset)
                .size(faceSize)
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(palette.faceTop, palette.faceBottom)
                    )
                )
                .border(width = 2.dp, color = palette.rim, shape = CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            palette.shadow.copy(alpha = 0.08f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 10.dp, y = 8.dp)
                    .width(30.dp)
                    .height(13.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(palette.highlight)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-10).dp, y = (-8).dp)
                    .width(22.dp)
                    .height(9.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(palette.highlight.copy(alpha = 0.26f))
            )

            Icon(
                imageVector = when (state) {
                    NodeState.LOCKED -> Icons.Default.Lock
                    NodeState.ACTIVE -> Icons.Default.Star
                    NodeState.COMPLETED -> Icons.Default.Check
                    NodeState.PERFECT -> Icons.Default.Star
                },
                contentDescription = when (state) {
                    NodeState.LOCKED -> "Locked"
                    NodeState.ACTIVE -> "Active"
                    NodeState.COMPLETED -> "Completed"
                    NodeState.PERFECT -> "Perfect"
                },
                tint = palette.icon,
                modifier = Modifier.size(if (state == NodeState.PERFECT) 30.dp else 28.dp)
            )
        }
    }
}

private fun paletteFor(state: NodeState): NodePalette = when (state) {
    NodeState.LOCKED -> NodePalette(
        faceTop = Color(0xFF626262),
        faceBottom = Color(0xFF4B4B4B),
        baseTop = Color(0xFF4A4A4A),
        baseBottom = LockedGreyDark,
        rim = White.copy(alpha = 0.16f),
        highlight = White.copy(alpha = 0.16f),
        icon = LockedGrey,
        shadow = Color.Black
    )
    NodeState.ACTIVE -> NodePalette(
        faceTop = Color(0xFF41E7FF),
        faceBottom = Color(0xFF12BDE8),
        baseTop = Color(0xFF17A8E0),
        baseBottom = SkyBlueDark,
        rim = White.copy(alpha = 0.28f),
        highlight = White.copy(alpha = 0.22f),
        icon = Charcoal,
        shadow = Color(0xFF0A4EA0)
    )
    NodeState.COMPLETED -> NodePalette(
        faceTop = Color(0xFF79E325),
        faceBottom = LeafGreen,
        baseTop = Color(0xFF5FBC14),
        baseBottom = LeafGreenDark,
        rim = White.copy(alpha = 0.22f),
        highlight = White.copy(alpha = 0.2f),
        icon = Charcoal,
        shadow = Color(0xFF25470F)
    )
    NodeState.PERFECT -> NodePalette(
        faceTop = Color(0xFFFFDF57),
        faceBottom = SunYellow,
        baseTop = Color(0xFFFFD234),
        baseBottom = SunYellowDark,
        rim = White.copy(alpha = 0.24f),
        highlight = White.copy(alpha = 0.22f),
        icon = Charcoal,
        shadow = Color(0xFF6E4A00)
    )
}
