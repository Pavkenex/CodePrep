package com.codeprep.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.codeprep.app.R
import com.codeprep.app.ui.localization.localizedStringResource
import com.codeprep.app.ui.theme.AppBackground
import com.codeprep.app.ui.theme.DeepCharcoal
import com.codeprep.app.ui.theme.ElectricCyan

internal data class AuthScreenLayoutSpec(
    val useCenteredCard: Boolean,
    val screenPaddingDp: Int,
    val verticalPaddingDp: Int,
    val cardWidthDp: Int,
    val cardHorizontalPaddingDp: Int,
    val cardVerticalPaddingDp: Int,
    val formWidthDp: Int,
    val brandBottomPaddingDp: Int
)

private const val COMPACT_HEIGHT_THRESHOLD_DP = 480

internal fun authScreenLayoutFor(
    screenWidthDp: Int,
    screenHeightDp: Int = Int.MAX_VALUE
): AuthScreenLayoutSpec {
    val isCompactHeight = screenHeightDp < COMPACT_HEIGHT_THRESHOLD_DP

    return if (screenWidthDp >= 600) {
        if (isCompactHeight) {
            AuthScreenLayoutSpec(
                useCenteredCard = true,
                screenPaddingDp = 16,
                verticalPaddingDp = 12,
                cardWidthDp = 420,
                cardHorizontalPaddingDp = 28,
                cardVerticalPaddingDp = 20,
                formWidthDp = 360,
                brandBottomPaddingDp = 16
            )
        } else {
            AuthScreenLayoutSpec(
                useCenteredCard = true,
                screenPaddingDp = 72,
                verticalPaddingDp = 48,
                cardWidthDp = 320,
                cardHorizontalPaddingDp = 40,
                cardVerticalPaddingDp = 44,
                formWidthDp = 280,
                brandBottomPaddingDp = 32
            )
        }
    } else {
        AuthScreenLayoutSpec(
            useCenteredCard = false,
            screenPaddingDp = 24,
            verticalPaddingDp = 24,
            cardWidthDp = 0,
            cardHorizontalPaddingDp = 0,
            cardVerticalPaddingDp = 0,
            formWidthDp = 0,
            brandBottomPaddingDp = if (isCompactHeight) 24 else 48
        )
    }
}

@Composable
internal fun AuthScreenFrame(
    showBrand: Boolean,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val layout = authScreenLayoutFor(
        screenWidthDp = configuration.screenWidthDp,
        screenHeightDp = configuration.screenHeightDp
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = layout.screenPaddingDp.dp,
                    vertical = layout.verticalPaddingDp.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val containerModifier = if (layout.useCenteredCard) {
                Modifier
                    .fillMaxWidth()
                    .widthIn(max = layout.cardWidthDp.dp)
            } else {
                Modifier.fillMaxWidth()
            }

            if (layout.useCenteredCard) {
                Surface(
                    modifier = containerModifier,
                    shape = RoundedCornerShape(28.dp),
                    color = DeepCharcoal,
                    tonalElevation = 0.dp,
                    shadowElevation = 18.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = ElectricCyan.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(28.dp)
                            )
                            .padding(
                                horizontal = layout.cardHorizontalPaddingDp.dp,
                                vertical = layout.cardVerticalPaddingDp.dp
                            )
                    ) {
                        if (showBrand) {
                            Text(
                                text = localizedStringResource(R.string.auth_brand_title),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    color = ElectricCyan
                                ),
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(bottom = layout.brandBottomPaddingDp.dp)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .widthIn(max = layout.formWidthDp.dp)
                        ) {
                            content()
                        }
                    }
                }
            } else {
                Column(
                    modifier = containerModifier
                ) {
                    if (showBrand) {
                        Text(
                            text = localizedStringResource(R.string.auth_brand_title),
                            style = MaterialTheme.typography.displayMedium.copy(
                                color = ElectricCyan
                            ),
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(bottom = layout.brandBottomPaddingDp.dp)
                        )
                    }

                    content()
                }
            }
        }
    }
}
