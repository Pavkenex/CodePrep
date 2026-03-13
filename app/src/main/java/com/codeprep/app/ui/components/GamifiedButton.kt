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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codeprep.app.ui.theme.*

@Composable
fun GamifiedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = SkyBlue,
    shadowColor: Color = SkyBlueDark,
    textColor: Color = Color.White,
    enabled: Boolean = true,
    height: Dp = 50.dp,
    elevationHeight: Dp = 4.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Animate the press effect
    val topOffset by animateDpAsState(
        targetValue = if (isPressed) elevationHeight else 0.dp,
        label = "button_press"
    )

    Box(
        modifier = modifier
            .height(height + elevationHeight) // Total height reserves space for shadow
            .clickable(
                interactionSource = interactionSource,
                indication = null, // No ripple, we animate movement
                enabled = enabled,
                onClick = onClick
            )
    ) {
        // Shadow Layer (Static Bottom)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(height) // Same height as main button
                .clip(RoundedCornerShape(16.dp))
                .background(if (enabled) shadowColor else LockedGreyDark)
        )

        // Main Button Layer (Animated Top)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = topOffset) // Moves down when pressed
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(16.dp))
                .background(if (enabled) backgroundColor else LockedGrey)
                .padding(horizontal = 16.dp), // Padding inside button
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = if (enabled) textColor else LockedGreyDark
                )
            )
        }
    }
}
