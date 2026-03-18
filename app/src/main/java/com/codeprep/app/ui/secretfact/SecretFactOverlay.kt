package com.codeprep.app.ui.secretfact

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.codeprep.app.data.model.SecretFactStrings
import com.codeprep.app.ui.theme.AppBackground
import com.codeprep.app.ui.theme.Charcoal
import com.codeprep.app.ui.theme.ElectricCyan
import com.codeprep.app.ui.theme.IceWhite
import com.codeprep.app.ui.theme.TextLight
import com.codeprep.app.ui.theme.TrueBlack

@Composable
fun SecretFactOverlay(
    uiState: SecretFactUiState,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val density = LocalDensity.current
    val sweepProgress = remember { Animatable(-1.1f) }
    val signalAlpha = remember { Animatable(0f) }

    LaunchedEffect(uiState.phase, uiState.revealNonce) {
        if (uiState.phase == SecretFactPhase.Signaling) {
            triggerSignalVibration(context, view)
            signalAlpha.snapTo(1f)
            sweepProgress.snapTo(-1.1f)
            sweepProgress.animateTo(
                targetValue = 1.15f,
                animationSpec = tween(
                    durationMillis = SIGNAL_SWEEP_DURATION_MILLIS,
                    easing = FastOutLinearInEasing
                )
            )
            signalAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = SIGNAL_FADE_DURATION_MILLIS,
                    easing = LinearOutSlowInEasing
                )
            )
        } else {
            signalAlpha.snapTo(0f)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = uiState.phase == SecretFactPhase.Signaling,
            enter = fadeIn(animationSpec = tween(90)),
            exit = fadeOut(animationSpec = tween(150))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppBackground.copy(alpha = 0.82f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(signalAlpha.value)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    ElectricCyan.copy(alpha = 0.08f),
                                    ElectricCyan.copy(alpha = 0.42f),
                                    ElectricCyan.copy(alpha = 0.08f),
                                    Color.Transparent
                                ),
                                startX = 0f,
                                endX = with(density) { 420.dp.toPx() }
                            )
                        )
                        .offset(x = with(density) { (sweepProgress.value * 320.dp.toPx()).toDp() })
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = SecretFactStrings.signalLabel(uiState.languageCode),
                        color = ElectricCyan,
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = SecretFactStrings.signalStatus(uiState.languageCode),
                        color = TextLight,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = uiState.phase == SecretFactPhase.Revealed && uiState.factText.isNotBlank(),
            enter = fadeIn(animationSpec = tween(150)) +
                scaleIn(
                    initialScale = 0.92f,
                    animationSpec = tween(durationMillis = 280, easing = LinearOutSlowInEasing)
                ) +
                slideInVertically(
                    initialOffsetY = { it / 6 },
                    animationSpec = tween(durationMillis = 280, easing = LinearOutSlowInEasing)
                ),
            exit = fadeOut(animationSpec = tween(140)) +
                scaleOut(targetScale = 0.96f, animationSpec = tween(140)) +
                slideOutVertically(targetOffsetY = { it / 10 }, animationSpec = tween(140))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                AppBackground.copy(alpha = 0.9f),
                                AppBackground.copy(alpha = 0.98f)
                            )
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    ElectricCyan.copy(alpha = 0.55f),
                                    Color.Transparent,
                                    ElectricCyan.copy(alpha = 0.2f)
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ),
                    shape = RoundedCornerShape(28.dp),
                    color = Charcoal.copy(alpha = 0.96f),
                    tonalElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = SecretFactStrings.signalLabel(uiState.languageCode),
                                color = ElectricCyan,
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )

                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = ElectricCyan.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = ElectricCyan.copy(alpha = 0.45f)
                                )
                            ) {
                                Text(
                                    text = uiState.categoryLabel,
                                    color = ElectricCyan,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            ElectricCyan.copy(alpha = 0.8f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        Text(
                            text = uiState.title,
                            color = TextLight,
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = FontFamily.Monospace
                        )

                        Text(
                            text = uiState.factText,
                            color = IceWhite,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Start
                        )

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = SecretFactStrings.dismissAction(uiState.languageCode),
                                color = TrueBlack,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun triggerSignalVibration(
    context: Context,
    view: android.view.View
) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(VibratorManager::class.java)
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    if (vibrator?.hasVibrator() == true) {
        val effect = VibrationEffect.createOneShot(
            SIGNAL_VIBRATION_DURATION_MILLIS,
            SIGNAL_VIBRATION_AMPLITUDE
        )
        vibrator.vibrate(effect)
    } else {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
}

private const val SIGNAL_SWEEP_DURATION_MILLIS = 520
private const val SIGNAL_FADE_DURATION_MILLIS = 260
private const val SIGNAL_VIBRATION_DURATION_MILLIS = 140L
private const val SIGNAL_VIBRATION_AMPLITUDE = 220
