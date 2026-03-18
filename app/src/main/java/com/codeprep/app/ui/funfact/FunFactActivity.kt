package com.codeprep.app.ui.funfact

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codeprep.app.MainActivity
import com.codeprep.app.ui.theme.AppBackground
import com.codeprep.app.ui.theme.Charcoal
import com.codeprep.app.ui.theme.CodePrepTheme
import com.codeprep.app.ui.theme.ElectricCyan
import com.codeprep.app.ui.theme.IceWhite
import com.codeprep.app.ui.theme.TextLight
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class FunFactActivity : ComponentActivity() {
    private var hasLaunchedMain = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CodePrepTheme {
                FunFactRoute(
                    onOpenApp = ::openMainActivity
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun openMainActivity() {
        if (hasLaunchedMain) return
        hasLaunchedMain = true

        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}

@Composable
private fun FunFactRoute(
    onOpenApp: () -> Unit,
    viewModel: FunFactViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var shouldAutoForward by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.requiresLoginRedirect) {
        if (uiState.requiresLoginRedirect) {
            onOpenApp()
        }
    }

    LaunchedEffect(uiState.isLoading, uiState.factText) {
        if (!uiState.isLoading && uiState.factText.isNotBlank() && !uiState.requiresLoginRedirect) {
            shouldAutoForward = true
            delay(2200)
            onOpenApp()
        }
    }

    FunFactScreen(
        uiState = uiState,
        onOpenApp = onOpenApp,
        isAutoForwarding = shouldAutoForward
    )
}

@Composable
private fun FunFactScreen(
    uiState: FunFactUiState,
    onOpenApp: () -> Unit,
    isAutoForwarding: Boolean
) {
    val transition = rememberInfiniteTransition(label = "factSplash")
    val pulseScale by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Charcoal, AppBackground, AppBackground)
                )
            )
            .clickable(enabled = !uiState.isLoading, onClick = onOpenApp)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = Charcoal.copy(alpha = 0.92f),
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .scale(pulseScale)
                        .alpha(pulseAlpha)
                        .background(ElectricCyan.copy(alpha = 0.16f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = uiState.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = ElectricCyan,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (uiState.isLoading) "..." else uiState.factText,
                    style = MaterialTheme.typography.headlineSmall,
                    color = IceWhite,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = uiState.tapHint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextLight,
                    textAlign = TextAlign.Center
                )

                if (isAutoForwarding) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = ">> OPENING_HOME()",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ElectricCyan,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onOpenApp,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                ) {
                    Text(
                        text = uiState.buttonText,
                        color = AppBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
