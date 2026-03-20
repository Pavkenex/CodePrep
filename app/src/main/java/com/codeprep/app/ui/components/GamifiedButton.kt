package com.codeprep.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codeprep.app.feedback.FeedbackEvent
import com.codeprep.app.feedback.LocalAppFeedback
import com.codeprep.app.ui.theme.*

@Composable
fun GamifiedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = ElectricCyan,
    shadowColor: Color = SkyBlueDark,
    textColor: Color = Charcoal,
    enabled: Boolean = true,
    height: Dp = 50.dp,
    elevationHeight: Dp = 4.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val feedback = LocalAppFeedback.current
    
    // Animate the press effect
    val topOffset by animateDpAsState(
        targetValue = if (isPressed) elevationHeight else 0.dp,
        label = "button_press"
    )

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = height + elevationHeight)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = {
                    feedback.emit(FeedbackEvent.TapPrimary)
                    onClick()
                }
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .defaultMinSize(minHeight = height)
                .clip(RoundedCornerShape(16.dp))
                .background(if (enabled) shadowColor else AppBackground)
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = topOffset)
                .fillMaxWidth()
                .defaultMinSize(minHeight = height)
                .clip(RoundedCornerShape(16.dp))
                .background(if (enabled) backgroundColor else DeepCharcoal)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 0.2.sp,
                    color = if (enabled) textColor else TextLight
                )
            )
        }
    }
}
