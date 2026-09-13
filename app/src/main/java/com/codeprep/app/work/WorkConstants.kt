package com.codeprep.app.work

import java.time.Duration

object WorkConstants {
    const val USER_ID_KEY = "user_id"

    const val HEART_REFILL_ACTION = "com.codeprep.app.action.HEART_REFILL"
    const val HEART_REMINDER_WORK_NAME = "heart-reminder-worker"
    const val LEGACY_HEART_WORK_NAME = "heart-worker"
    const val STREAK_WORK_NAME = "streak-worker"
    const val SYNC_WORK_NAME = "sync-progress-worker"
    const val FUN_FACT_WIDGET_WORK_NAME = "fun-fact-widget-refresh-worker"

    val STREAK_REPEAT_INTERVAL: Duration = Duration.ofMinutes(15)
    val SYNC_REPEAT_INTERVAL: Duration = Duration.ofMinutes(15)
    val FUN_FACT_WIDGET_REPEAT_INTERVAL: Duration = Duration.ofHours(6)

    val STREAK_REMINDER_THRESHOLD: Duration = Duration.ofHours(6)
}
