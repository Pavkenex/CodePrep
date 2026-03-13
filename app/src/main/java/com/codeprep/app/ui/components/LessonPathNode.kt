package com.codeprep.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.codeprep.app.ui.theme.*

enum class NodeState {
    LOCKED,
    ACTIVE,
    COMPLETED,
    PERFECT // Gold with crown/star
}

@Composable
fun LessonPathNode(
    state: NodeState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val elevationHeight = 6.dp
    
    // Colors based on state
    val mainColor = when (state) {
        NodeState.LOCKED -> LockedGrey
        NodeState.ACTIVE -> LeafGreen
        NodeState.COMPLETED -> SunYellow
        NodeState.PERFECT -> SunYellow
    }
    
    val shadowColor = when (state) {
        NodeState.LOCKED -> LockedGreyDark
        NodeState.ACTIVE -> LeafGreenDark
        NodeState.COMPLETED -> SunYellowDark
        NodeState.PERFECT -> SunYellowDark
    }

    val iconColor = if (state == NodeState.LOCKED) LockedGreyDark else Color.White

    val topOffset = if (isPressed) elevationHeight else 0.dp

    Box(
        modifier = modifier
            .size(size)
            .height(size + elevationHeight)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = state != NodeState.LOCKED,
                onClick = onClick
            )
    ) {
        // Shadow
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(size)
                .clip(CircleShape)
                .background(shadowColor)
        )

        // Main Circle
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = topOffset)
                .size(size)
                .clip(CircleShape)
                .background(mainColor)
                .border(
                    width = if (state == NodeState.ACTIVE) 4.dp else 0.dp,
                    color = Color.White.copy(alpha = 0.5f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                NodeState.LOCKED -> Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
                NodeState.ACTIVE -> Icon(
                    imageVector = Icons.Default.Star, // Or play icon
                    contentDescription = "Active",
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
                NodeState.COMPLETED -> Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
                NodeState.PERFECT -> Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Perfect",
                    tint = iconColor,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}
