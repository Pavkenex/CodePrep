package com.codeprep.app.feedback

import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppFeedback = staticCompositionLocalOf<AppFeedback> {
    object : AppFeedback {
        override fun emit(event: FeedbackEvent) = Unit
    }
}
