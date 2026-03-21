package com.codeprep.app.feedback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppFeedback = staticCompositionLocalOf<AppFeedback> {
    object : AppFeedback {
        override fun emit(event: FeedbackEvent) = Unit
    }
}

@Composable
fun ProvideAppFeedback(
    appFeedback: AppFeedback,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalAppFeedback provides appFeedback) {
        content()
    }
}
